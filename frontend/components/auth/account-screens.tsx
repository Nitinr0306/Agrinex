"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useRef, useState, type FormEvent } from "react";
import { CheckCircleIcon, InfoIcon } from "@/components/icons";
import { useChangeLocale } from "@/components/layout/language-switch";
import { PageShell } from "@/components/layout/page-shell";
import { LoadProblem } from "@/components/states/load-problem";
import { Button, ButtonLink } from "@/components/ui/button";
import { Card, EmptyState, Notice, Skeleton } from "@/components/ui/feedback";
import { Field, PasswordInput, TextInput } from "@/components/ui/form";
import { authApi } from "@/lib/api/endpoints";
import { isApiError } from "@/lib/api/errors";
import { FARMER_PASSWORD_MIN, normalizeIdentifier, passwordProblem } from "@/lib/auth/identifiers";
import { useLinkToken } from "@/lib/auth/link-token";
import { useSession } from "@/lib/auth/session-provider";
import { clearSession, markEmailVerified } from "@/lib/auth/session-store";
import { useCountdown } from "@/lib/hooks/use-query";
import { LOCALE_NAMES, LOCALES } from "@/lib/i18n/config";
import { useI18n } from "@/lib/i18n/i18n-provider";
import { passwordMessage } from "./sign-in-screen";

/** S12 account menu: who is signed in, email confirmation, language, sign out (role file 02 §8 S12). */
export function AccountScreen() {
  const { t, locale } = useI18n();
  const router = useRouter();
  const changeLocale = useChangeLocale();
  const { status, user, signOut } = useSession();
  const [resend, setResend] = useState<"idle" | "busy" | "sent">("idle");
  const [retryAfter, setRetryAfter] = useState<number | null>(null);
  const [problem, setProblem] = useState<unknown>(null);
  const wait = useCountdown(retryAfter);

  const sendLink = async () => {
    setResend("busy");
    setProblem(null);
    try {
      await authApi.resendEmailVerification();
      setResend("sent");
    } catch (error) {
      setResend("idle");
      if (isApiError(error, "RATE_LIMITED")) setRetryAfter(error.retryAfterSeconds ?? 60);
      else setProblem(error);
    }
  };

  return (
    <PageShell title={t("account.title")} backHref="/" width="narrow">
      {status === "loading" ? <Skeleton className="h-40" /> : null}
      {status === "signedOut" ? (
        <EmptyState
          icon={<InfoIcon size={28} />}
          title={t("account.signed_out")}
          actions={<ButtonLink href="/sign-in">{t("auth.tab_signin")}</ButtonLink>}
        />
      ) : null}
      {status === "signedIn" && user ? (
        <div className="flex flex-col gap-4">
          <Card className="px-4 py-4">
            <p className="text-heading font-semibold">
              {user.displayName ? t("auth.signed_in_as", { name: user.displayName }) : t("auth.signed_in")}
            </p>
          </Card>

          {!user.emailVerified ? (
            <Notice
              tone="warning"
              title={t("verify.unconfirmed")}
              action={resend === "sent" ? null : (
                <Button variant="secondary" busy={resend === "busy"} disabled={wait > 0} onClick={() => void sendLink()}>
                  {t("verify.resend")}
                </Button>
              )}
            >
              {resend === "sent" ? <p role="status">{t("verify.resent")} {t("auth.link_delay")}</p> : null}
              {wait > 0 ? <p role="alert">{t("auth.too_many", { n: wait })}</p> : null}
            </Notice>
          ) : null}
          {problem ? <LoadProblem error={problem} /> : null}

          <Card aria-labelledby="language-title" className="flex flex-col gap-3 px-4 py-4">
            <h2 id="language-title" className="text-label font-semibold">{t("lang.label")}</h2>
            <div className="grid grid-cols-3 gap-2">
              {LOCALES.map((option) => (
                <button
                  key={option}
                  type="button"
                  lang={option}
                  aria-pressed={option === locale}
                  onClick={() => changeLocale(option)}
                  className={`min-h-12 rounded-control border px-2 text-body ${option === locale ? "border-2 border-action font-semibold text-net" : "border-rule-strong bg-raised"}`}
                >
                  {LOCALE_NAMES[option].native}
                </button>
              ))}
            </div>
          </Card>

          {user.role === "ADMIN" ? <ButtonLink href="/admin" variant="secondary" block>{t("nav.admin")}</ButtonLink> : null}
          <Button
            variant="secondary"
            block
            onClick={() => void signOut().then(() => router.replace("/"))}
          >
            {t("auth.sign_out")}
          </Button>
        </div>
      ) : null}
    </PageShell>
  );
}

/** S14 forgot password: always the same answer, never whether an account exists (ADR-0048 item 5). */
export function ForgotPasswordScreen() {
  const { t } = useI18n();
  const [identifier, setIdentifier] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [sent, setSent] = useState(false);
  const [busy, setBusy] = useState(false);
  const [problem, setProblem] = useState<unknown>(null);
  const [retryAfter, setRetryAfter] = useState<number | null>(null);
  const wait = useCountdown(retryAfter);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    const normalized = normalizeIdentifier(identifier);
    if (!normalized || normalized.length > 254) {
      setError(normalized ? t("err.field.TOO_LONG") : t("err.field.REQUIRED"));
      return;
    }
    setError(null);
    setBusy(true);
    setProblem(null);
    try {
      await authApi.requestPasswordReset(normalized);
      setSent(true);
    } catch (failure) {
      if (isApiError(failure, "VALIDATION_FAILED", "MALFORMED_REQUEST")) setError(t("err.field.INVALID"));
      else if (isApiError(failure, "RATE_LIMITED")) setRetryAfter(failure.retryAfterSeconds ?? 60);
      else setProblem(failure);
    } finally {
      setBusy(false);
    }
  };

  return (
    <PageShell title={t("reset.title")} backHref="/sign-in" width="narrow">
      <Card className="flex flex-col gap-4 p-4">
        {sent ? (
          <>
            <Notice tone="success" live title={t("reset.sent")}>{t("auth.link_delay")}</Notice>
            <ButtonLink href="/sign-in?tab=signin" variant="secondary" block>{t("reset.back_to_signin")}</ButtonLink>
          </>
        ) : (
          <form noValidate onSubmit={(event) => void submit(event)} className="flex flex-col gap-4">
            <p className="text-body">{t("reset.help")}</p>
            <Field label={t("auth.identifier")} error={error}>
              {({ inputId, describedBy, invalid }) => (
                <TextInput id={inputId} autoComplete="username" aria-describedby={describedBy} invalid={invalid} value={identifier} onChange={(event) => setIdentifier(event.target.value)} />
              )}
            </Field>
            {wait > 0 ? <Notice tone="error" live>{t("auth.too_many", { n: wait })}</Notice> : null}
            {problem ? <LoadProblem error={problem} /> : null}
            <Button type="submit" block busy={busy} disabled={wait > 0}>{t("reset.submit")}</Button>
            <Link href="/sign-in?tab=signin" className="mx-auto inline-flex min-h-12 items-center text-label font-semibold text-net underline underline-offset-4">
              {t("reset.back_to_signin")}
            </Link>
          </form>
        )}
      </Card>
    </PageShell>
  );
}

/** S15 new password from the reset link. A reset never signs the user in (ADR-0048 item 3). */
export function ResetPasswordScreen() {
  const { t } = useI18n();
  const router = useRouter();
  const { token, ready } = useLinkToken();
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [invalidLink, setInvalidLink] = useState(false);
  const [busy, setBusy] = useState(false);
  const [problem, setProblem] = useState<unknown>(null);
  const [retryAfter, setRetryAfter] = useState<number | null>(null);
  const wait = useCountdown(retryAfter);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    if (!token) return;
    const issue = passwordProblem(password);
    if (issue) {
      setError(passwordMessage(issue, t, FARMER_PASSWORD_MIN));
      return;
    }
    setError(null);
    setBusy(true);
    setProblem(null);
    try {
      await authApi.confirmPasswordReset(token, password);
      // Every session was signed out; drop any in-memory token too (E29).
      clearSession();
      router.replace("/sign-in?tab=signin&reset=done");
    } catch (failure) {
      if (isApiError(failure, "PASSWORD_RESET_TOKEN_INVALID")) setInvalidLink(true);
      else if (isApiError(failure, "VALIDATION_FAILED")) {
        const field = failure.fieldError("newPassword");
        if (failure.fieldError("token")) setInvalidLink(true);
        else if (field?.code === "TOO_SHORT") setError(t("err.password.TOO_SHORT_ACCOUNT"));
        else if (field?.code === "TOO_LONG") setError(t("err.password.TOO_LONG"));
        else setError(t("err.field.INVALID"));
      } else if (isApiError(failure, "RATE_LIMITED")) setRetryAfter(failure.retryAfterSeconds ?? 60);
      else setProblem(failure);
    } finally {
      setBusy(false);
    }
  };

  return (
    <PageShell title={t("reset.new_title")} width="narrow">
      {!ready ? <Skeleton className="h-40" /> : !token || invalidLink ? (
        <EmptyState
          icon={<InfoIcon size={28} />}
          title={t("reset.invalid")}
          actions={<ButtonLink href="/forgot-password">{t("reset.ask_new")}</ButtonLink>}
        />
      ) : (
        <Card className="p-4">
          <form noValidate onSubmit={(event) => void submit(event)} className="flex flex-col gap-4">
            <Field label={t("reset.new_password")} help={t("auth.password_help")} error={error}>
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
            {wait > 0 ? <Notice tone="error" live>{t("auth.too_many", { n: wait })}</Notice> : null}
            {problem ? <LoadProblem error={problem} /> : null}
            <Button type="submit" block busy={busy} disabled={wait > 0}>{t("reset.save")}</Button>
          </form>
        </Card>
      )}
    </PageShell>
  );
}

/** S16 email confirmation from the verification link: confirms at once (ADR-0048 items 3, 4). */
export function VerifyEmailScreen() {
  const { t } = useI18n();
  const { status } = useSession();
  const { token, ready } = useLinkToken();
  const [result, setOutcome] = useState<"checking" | "done" | "invalid" | "failed" | "rateLimited">("checking");
  const [problem, setProblem] = useState<unknown>(null);
  const [attempt, setAttempt] = useState(0);
  const [retryAfter, setRetryAfter] = useState<number | null>(null);
  const wait = useCountdown(retryAfter);
  const sentFor = useRef<string | null>(null);

  useEffect(() => {
    if (!ready || !token) return;
    // One confirmation per token and attempt (React may run this effect twice in development).
    const key = `${token}:${attempt}`;
    if (sentFor.current === key) return;
    sentFor.current = key;
    authApi.confirmEmailVerification(token).then(
      () => {
        markEmailVerified();
        setOutcome("done");
      },
      (failure: unknown) => {
        if (isApiError(failure, "EMAIL_VERIFICATION_TOKEN_INVALID", "VALIDATION_FAILED")) setOutcome("invalid");
        else if (isApiError(failure, "RATE_LIMITED")) {
          setRetryAfter(failure.retryAfterSeconds ?? 60);
          setOutcome("rateLimited");
        }
        else {
          setProblem(failure);
          setOutcome("failed");
        }
      },
    );
  }, [ready, token, attempt]);

  const retry = () => {
    setOutcome("checking");
    setAttempt((value) => value + 1);
  };
  // A link without a well-formed token never reaches the server.
  const outcome = ready && !token ? "invalid" : result;

  return (
    <PageShell title={t("verify.title")} width="narrow">
      {outcome === "checking" ? (
        <Card className="flex items-center gap-3 px-4 py-6" aria-busy="true">
          <span className="h-3 w-3 animate-pulse rounded-full bg-action motion-reduce:animate-none" aria-hidden="true" />
          <p role="status">{t("verify.checking")}</p>
        </Card>
      ) : null}
      {outcome === "done" ? (
        <EmptyState
          icon={<CheckCircleIcon size={28} className="text-fresh" />}
          title={t("verify.done")}
          actions={<ButtonLink href={status === "signedIn" ? "/alerts" : "/"}>{t("action.continue")}</ButtonLink>}
        />
      ) : null}
      {outcome === "invalid" ? (
        <EmptyState
          icon={<InfoIcon size={28} />}
          title={t("verify.invalid")}
          actions={status === "signedIn"
            ? <ButtonLink href="/account">{t("verify.resend")}</ButtonLink>
            : <ButtonLink href="/sign-in?tab=signin">{t("auth.tab_signin")}</ButtonLink>}
        />
      ) : null}
      {outcome === "rateLimited" ? (
        <Notice
          tone="error"
          live
          action={<Button variant="secondary" disabled={wait > 0} onClick={retry}>{t("action.retry")}</Button>}
        >
          {t("auth.too_many", { n: wait })}
        </Notice>
      ) : null}
      {outcome === "failed" ? <LoadProblem error={problem} onRetry={retry} /> : null}
    </PageShell>
  );
}
