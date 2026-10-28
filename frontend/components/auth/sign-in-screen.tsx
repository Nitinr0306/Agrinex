"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";
import { PageShell } from "@/components/layout/page-shell";
import { LoadProblem } from "@/components/states/load-problem";
import { Button } from "@/components/ui/button";
import { Segmented } from "@/components/ui/choice";
import { Card, Notice } from "@/components/ui/feedback";
import { Field, PasswordInput, PhoneInput, Select, TextInput } from "@/components/ui/form";
import { clearPendingAlert, readPendingAlert, saveVerificationNotice, usePendingAlert, type PendingAlert } from "@/lib/alerts/pending-alert";
import { ruleText } from "@/lib/alerts/wording";
import { alertApi, authApi } from "@/lib/api/endpoints";
import { ApiError, isApiError } from "@/lib/api/errors";
import type { AuthResponse, AuthenticatedUser } from "@/lib/api/types";
import {
  FARMER_PASSWORD_MIN,
  isValidEmail,
  isValidPhoneDigits,
  normalizeIdentifier,
  passwordProblem,
  phoneToE164,
  safeNextPath,
} from "@/lib/auth/identifiers";
import { acceptAuthResponse, announceSignedIn } from "@/lib/auth/session-store";
import { useComparison } from "@/lib/compare/comparison-provider";
import { useCountdown, useQuery } from "@/lib/hooks/use-query";
import { useI18n, type Translate } from "@/lib/i18n/i18n-provider";
import { publishedNameLang } from "@/lib/i18n/names";
import { referenceData } from "@/lib/reference/reference-data";

type Tab = "create" | "signin";
type Errors = Partial<Record<"phone" | "email" | "password" | "name" | "district" | "identifier" | "form", string>>;

/** S10 sign in / quick registration (role file 02 §8 S10; ADR-0016, ADR-0047). */
export function SignInScreen({ initialTab, next, resetDone }: { initialTab: Tab; next: string | null; resetDone: boolean }) {
  const { t } = useI18n();
  const router = useRouter();
  const [tab, setTab] = useState<Tab>(initialTab);
  const pending = usePendingAlert();

  /** After any successful sign-in: create the waiting alert, then open S11 (journey J3). */
  const finish = async (response: AuthResponse, registeredEmail: string | null) => {
    acceptAuthResponse(response);
    announceSignedIn();
    if (registeredEmail) saveVerificationNotice(registeredEmail);
    const waiting = readPendingAlert();
    if (waiting) {
      clearPendingAlert();
      try {
        await alertApi.createAlert({
          alertType: waiting.alertType,
          commodityId: waiting.commodityId,
          marketId: waiting.marketId,
          thresholdValue: waiting.thresholdValue,
        });
        router.replace("/alerts?created=1");
      } catch {
        const query = new URLSearchParams({ crop: waiting.commodityId });
        if (waiting.marketId) query.set("market", waiting.marketId);
        router.replace(`/alerts/new?${query.toString()}`);
      }
      return;
    }
    router.replace(destination(response.user, next));
  };

  return (
    <PageShell title={tab === "create" ? t("auth.tab_create") : t("auth.tab_signin")} backHref="/" width="narrow">
      <div className="flex flex-col gap-4">
        {resetDone ? <Notice tone="success" live>{t("reset.done")}</Notice> : null}
        {pending ? <PendingSummary alert={pending} /> : null}
        <p className="border-l-4 border-rule-strong pl-3 text-label text-ink-2">{t("auth.why_account")}</p>
        <Segmented<Tab>
          label={t("nav.account")}
          value={tab}
          onChange={setTab}
          options={[
            { value: "create", label: t("auth.tab_create") },
            { value: "signin", label: t("auth.tab_signin") },
          ]}
        />
        {tab === "create"
          ? <CreateAccountForm pending={pending !== null} onDone={finish} onSignInInstead={() => setTab("signin")} />
          : <SignInForm pending={pending !== null} onDone={finish} />}
      </div>
    </PageShell>
  );
}

/** Where a sign-in continues: the requested same-site page, the admin area for an ADMIN, else the alerts. */
function destination(user: AuthenticatedUser, next: string | null): string {
  return safeNextPath(next) ?? (user.role === "ADMIN" ? "/admin" : "/alerts");
}

/** The alert waiting for the account, read-only (role file 02 S10). */
function PendingSummary({ alert }: { alert: PendingAlert }) {
  const { t, fmt, locale } = useI18n();
  return (
    <Card className="px-4 py-3">
      <h2 className="eyebrow mb-1 border-b border-rule pb-1">{t("auth.pending_title")}</h2>
      <p className="font-semibold">
        {alert.cropName} · <span lang={alert.marketName ? publishedNameLang(locale) : undefined}>{alert.marketName ?? t("alerts.all_markets")}</span>
      </p>
      <p className="text-label">{ruleText(alert.alertType, alert.thresholdValue, t, fmt)}</p>
    </Card>
  );
}

function CreateAccountForm({ pending, onDone, onSignInInstead }: {
  pending: boolean;
  onDone: (response: AuthResponse, email: string | null) => Promise<void>;
  onSignInInstead: () => void;
}) {
  const { t, locale } = useI18n();
  const { input } = useComparison();
  const [phone, setPhone] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [districtId, setDistrictId] = useState<string>(input.districtId ?? "");
  const [errors, setErrors] = useState<Errors>({});
  const [busy, setBusy] = useState(false);
  const [problem, setProblem] = useState<unknown>(null);
  const [retryAfter, setRetryAfter] = useState<number | null>(null);
  const wait = useCountdown(retryAfter);
  const districts = useQuery(input.stateId ? `districts:${input.stateId}` : "districts:auto", async () => {
    const stateId = input.stateId ?? (await referenceData.states())[0]?.id;
    return stateId ? referenceData.districts(stateId) : [];
  });

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    const local: Errors = {};
    if (!isValidPhoneDigits(phone)) local.phone = t("err.phone.INVALID");
    if (!isValidEmail(email)) local.email = t("err.email.INVALID");
    const passwordIssue = passwordProblem(password);
    if (passwordIssue) local.password = passwordMessage(passwordIssue, t, FARMER_PASSWORD_MIN);
    if (name.trim().length > 100) local.name = t("err.field.TOO_LONG");
    setErrors(local);
    if (Object.keys(local).length > 0) return;

    setBusy(true);
    setProblem(null);
    try {
      const response = await authApi.registerFarmer({
        phoneNumber: phoneToE164(phone),
        email: email.trim(),
        password,
        displayName: name.trim() ? name.trim() : null,
        defaultDistrictId: districtId || null,
      });
      await onDone(response, email.trim());
    } catch (error) {
      if (isApiError(error, "CONFLICT")) {
        setErrors({
          ...(error.fieldError("phoneNumber") ? { phone: t("auth.phone_taken") } : {}),
          ...(error.fieldError("email") ? { email: t("auth.email_taken") } : {}),
        });
      } else if (isApiError(error, "VALIDATION_FAILED")) {
        setErrors(validationErrors(error, t, { phoneNumber: "phone", email: "email", password: "password", displayName: "name" }));
      } else if (isApiError(error, "REFERENCE_NOT_FOUND")) {
        setErrors({ district: t("error.choice_gone") });
      } else if (isApiError(error, "RATE_LIMITED")) {
        setRetryAfter(error.retryAfterSeconds ?? 60);
      } else {
        setProblem(error);
      }
    } finally {
      setBusy(false);
    }
  };

  const taken = errors.phone === t("auth.phone_taken") || errors.email === t("auth.email_taken");
  return (
    <Card as="div" className="p-4">
      <form noValidate onSubmit={(event) => void submit(event)} className="flex flex-col gap-4">
        <Field label={t("auth.phone")} help={t("auth.no_sms")} error={errors.phone}>
          {({ inputId, describedBy, invalid }) => (
            <PhoneInput id={inputId} aria-describedby={describedBy} invalid={invalid} value={phone} onChange={setPhone} />
          )}
        </Field>
        <Field label={t("auth.email")} help={t("auth.email_help")} error={errors.email}>
          {({ inputId, describedBy, invalid }) => (
            <TextInput id={inputId} type="email" autoComplete="email" aria-describedby={describedBy} invalid={invalid} value={email} onChange={(event) => setEmail(event.target.value)} />
          )}
        </Field>
        <Field label={t("auth.password")} help={t("auth.password_help")} error={errors.password}>
          {({ inputId, describedBy, invalid }) => (
            <PasswordInput
              id={inputId}
              autoComplete="new-password"
              aria-describedby={describedBy}
              invalid={invalid}
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              showLabel={t("auth.password_show")}
              hideLabel={t("auth.password_hide")}
            />
          )}
        </Field>
        <Field label={t("auth.name")} optionalLabel={t("auth.optional")} error={errors.name}>
          {({ inputId, describedBy, invalid }) => (
            <TextInput id={inputId} autoComplete="name" aria-describedby={describedBy} invalid={invalid} value={name} onChange={(event) => setName(event.target.value)} />
          )}
        </Field>
        {districts.status === "success" && districts.data.length > 0 ? (
          <Field label={t("auth.default_district")} optionalLabel={t("auth.optional")} error={errors.district}>
            {({ inputId, describedBy, invalid }) => (
              <Select id={inputId} aria-describedby={describedBy} invalid={invalid} value={districtId} onChange={(event) => setDistrictId(event.target.value)}>
                <option value="">{t("auth.no_district")}</option>
                {districts.data.map((district) => (
                  <option key={district.id} value={district.id} lang={publishedNameLang(locale)}>{district.name}</option>
                ))}
              </Select>
            )}
          </Field>
        ) : null}
        {errors.form ? <Notice tone="error" live>{errors.form}</Notice> : null}
        {wait > 0 ? <Notice tone="error" live>{t("auth.too_many", { n: wait })}</Notice> : null}
        {problem ? <LoadProblem error={problem} /> : null}
        <Button type="submit" block busy={busy} disabled={wait > 0}>
          {pending ? t("auth.create_and_save") : t("auth.tab_create")}
        </Button>
        {taken ? <Button variant="tertiary" block onClick={onSignInInstead}>{t("auth.go_signin")}</Button> : null}
      </form>
    </Card>
  );
}

function SignInForm({ pending, onDone }: { pending: boolean; onDone: (response: AuthResponse, email: string | null) => Promise<void> }) {
  const { t } = useI18n();
  const [identifier, setIdentifier] = useState("");
  const [password, setPassword] = useState("");
  const [errors, setErrors] = useState<Errors>({});
  const [busy, setBusy] = useState(false);
  const [problem, setProblem] = useState<unknown>(null);
  const [retryAfter, setRetryAfter] = useState<number | null>(null);
  const wait = useCountdown(retryAfter);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    const local: Errors = {};
    const normalized = normalizeIdentifier(identifier);
    if (!normalized || normalized.length > 254) local.identifier = t("err.field.REQUIRED");
    if (!password || [...password].length > 64) local.password = password ? t("err.password.TOO_LONG") : t("err.field.REQUIRED");
    setErrors(local);
    if (Object.keys(local).length > 0) return;

    setBusy(true);
    setProblem(null);
    try {
      await onDone(await authApi.login({ identifier: normalized, password }), null);
    } catch (error) {
      if (isApiError(error, "INVALID_CREDENTIALS")) setErrors({ form: t("auth.invalid_credentials") });
      else if (isApiError(error, "VALIDATION_FAILED")) setErrors(validationErrors(error, t, { identifier: "identifier", password: "password" }));
      else if (isApiError(error, "RATE_LIMITED")) setRetryAfter(error.retryAfterSeconds ?? 60);
      else setProblem(error);
    } finally {
      setBusy(false);
    }
  };

  return (
    <Card as="div" className="p-4">
      <form noValidate onSubmit={(event) => void submit(event)} className="flex flex-col gap-4">
        <Field label={t("auth.identifier")} error={errors.identifier}>
          {({ inputId, describedBy, invalid }) => (
            <TextInput id={inputId} autoComplete="username" aria-describedby={describedBy} invalid={invalid} value={identifier} onChange={(event) => setIdentifier(event.target.value)} />
          )}
        </Field>
        <Field label={t("auth.password")} error={errors.password}>
          {({ inputId, describedBy, invalid }) => (
            <PasswordInput
              id={inputId}
              autoComplete="current-password"
              aria-describedby={describedBy}
              invalid={invalid}
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              showLabel={t("auth.password_show")}
              hideLabel={t("auth.password_hide")}
            />
          )}
        </Field>
        {errors.form ? <Notice tone="error" live>{errors.form}</Notice> : null}
        {wait > 0 ? <Notice tone="error" live>{t("auth.too_many", { n: wait })}</Notice> : null}
        {problem ? <LoadProblem error={problem} /> : null}
        <Button type="submit" block busy={busy} disabled={wait > 0}>
          {pending ? t("auth.signin_and_save") : t("auth.tab_signin")}
        </Button>
        <Link href="/forgot-password" className="mx-auto inline-flex min-h-12 items-center text-label font-semibold text-net underline underline-offset-4">
          {t("auth.forgot")}
        </Link>
      </form>
    </Card>
  );
}

export function passwordMessage(problem: "TOO_SHORT" | "TOO_LONG", t: Translate, minimum: number | null): string {
  if (problem === "TOO_LONG") return t("err.password.TOO_LONG");
  return minimum === null ? t("err.password.TOO_SHORT_ACCOUNT") : t("err.password.TOO_SHORT", { min: minimum });
}

/** Field errors by `errors[].field` and `code` (ADR-0022 item 11), never the English `message`. */
export function validationErrors(error: ApiError, t: Translate, fields: Record<string, keyof Errors>): Errors {
  const result: Errors = {};
  for (const fieldError of error.fieldErrors) {
    const target = fields[fieldError.field];
    if (!target || result[target]) continue;
    if (target === "phone") result.phone = t("err.phone.INVALID");
    else if (target === "email") result.email = t("err.email.INVALID");
    else if (target === "password" && (fieldError.code === "TOO_SHORT" || fieldError.code === "TOO_LONG")) {
      result.password = passwordMessage(fieldError.code, t, FARMER_PASSWORD_MIN);
    } else if (fieldError.code === "REQUIRED") result[target] = t("err.field.REQUIRED");
    else if (fieldError.code === "TOO_LONG") result[target] = t("err.field.TOO_LONG");
    else if (fieldError.code === "TOO_SHORT") result[target] = t("err.field.TOO_SHORT");
    else result[target] = t("err.field.INVALID");
  }
  if (Object.keys(result).length === 0) result.form = t("err.field.INVALID");
  return result;
}
