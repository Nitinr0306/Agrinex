"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, Notice, Skeleton } from "@/components/ui/feedback";
import { Field, TextInput } from "@/components/ui/form";
import { adminApi } from "@/lib/api/endpoints";
import type { TransportAssumptionPage, TransportAssumptionResponse, WeightsPage, WeightsResponse } from "@/lib/api/types";
import { Decimal } from "@/lib/decimal";
import { useQuery } from "@/lib/hooks/use-query";
import { AdminError, AdminPage, AdminTable, Pager, td, th, useAdminFormat } from "./admin-ui";

const DECIMAL_2 = /^\d+(\.\d{1,2})?$/;
const WEIGHT = /^(0(\.\d{1,4})?|1(\.0{1,4})?)$/;
const WHOLE = /^\d+$/;
/** Upper bounds of TransportAssumptionsUpdateRequest and FreshnessDays in the contract. */
const TRANSPORT_MAX = { ratePerKm: "99999999.99", minimumTripCharge: "9999999999.99", vehicleCapacityKg: "99999999.99" } as const;
const DAYS_MAX = 2147483647;

/** A03 configuration: each save creates a new version; earlier versions stay (ADR-0020 item 1). */
export function Configuration() {
  return (
    <AdminPage title="Configuration">
      <p className="text-label text-ink-2">
        Secrets, schedules, rate limits and CORS are not editable here (ADR-0020 item 3).
      </p>
      <TransportConfig />
      <WeightsConfig />
    </AdminPage>
  );
}

function TransportConfig() {
  const fmt = useAdminFormat();
  const [version, setVersion] = useState(0);
  const [saved, setSaved] = useState<TransportAssumptionResponse | null>(null);
  const active = useQuery(`transport:${version}`, (signal) => adminApi.getTransportAssumptions(signal));

  return (
    <Card className="flex flex-col gap-4 px-4 py-4">
      <h2 className="text-heading font-semibold">Transport assumptions</h2>
      {active.status === "error" ? <AdminError error={active.error} onRetry={active.retry} /> : null}
      {!active.data ? <Skeleton className="h-24" /> : (
        <>
          <p className="text-label text-ink-2">
            Active version {active.data.versionId}, created {fmt.dateTime(active.data.createdAt)}.
          </p>
          {/* Keyed by version: the form starts again from the values of the version now active. */}
          <TransportForm
            key={active.data.versionId}
            active={active.data}
            onSaved={(response) => {
              setSaved(response);
              setVersion((value) => value + 1);
            }}
          />
        </>
      )}
      {saved ? <Notice tone="success" live>Saved as version {saved.versionId}. It applies to new comparisons.</Notice> : null}
      <Versions kind="transport" refresh={version} />
    </Card>
  );
}

type TransportValues = Record<"ratePerKm" | "minimumTripCharge" | "vehicleCapacityKg", string>;

function TransportForm({ active, onSaved }: { active: TransportAssumptionResponse; onSaved: (response: TransportAssumptionResponse) => void }) {
  const [form, setForm] = useState<TransportValues>(() => ({
    ratePerKm: String(active.ratePerKm),
    minimumTripCharge: String(active.minimumTripCharge),
    vehicleCapacityKg: String(active.vehicleCapacityKg),
  }));
  const [errors, setErrors] = useState<Partial<Record<keyof TransportValues, string>>>({});
  const [busy, setBusy] = useState(false);
  const [failure, setFailure] = useState<unknown>(null);

  const save = async () => {
    const next: typeof errors = {};
    for (const key of Object.keys(form) as (keyof TransportValues)[]) {
      const text = form[key].trim();
      if (!DECIMAL_2.test(text) || !Decimal.of(text).isPositive() || Decimal.of(text).gt(Decimal.of(TRANSPORT_MAX[key]))) {
        next[key] = `Enter a number greater than 0 and at most ${TRANSPORT_MAX[key]}, with at most 2 decimal places.`;
      }
    }
    setErrors(next);
    if (Object.keys(next).length > 0) return;
    setBusy(true);
    setFailure(null);
    try {
      onSaved(await adminApi.updateTransportAssumptions({
        ratePerKm: Number(form.ratePerKm),
        minimumTripCharge: Number(form.minimumTripCharge),
        vehicleCapacityKg: Number(form.vehicleCapacityKg),
      }));
    } catch (error) {
      setFailure(error);
    } finally {
      setBusy(false);
    }
  };

  return (
    <>
      <div className="grid gap-3 tablet:grid-cols-3">
        {([
          ["ratePerKm", "Rate per km (₹)"],
          ["minimumTripCharge", "Minimum trip charge (₹)"],
          ["vehicleCapacityKg", "Vehicle capacity (kg)"],
        ] as const).map(([key, label]) => (
          <Field key={key} label={label} error={errors[key]}>
            {({ inputId, describedBy, invalid }) => (
              <TextInput id={inputId} inputMode="decimal" aria-describedby={describedBy} invalid={invalid} value={form[key]} onChange={(event) => setForm((current) => ({ ...current, [key]: event.target.value }))} className="figure" />
            )}
          </Field>
        ))}
      </div>
      {failure ? <AdminError error={failure} /> : null}
      <Button className="w-fit" busy={busy} onClick={() => void save()}>Save as a new version</Button>
    </>
  );
}

type WeightsForm = Record<"wNetRealization" | "wPrice" | "wTrend" | "wFreshness" | "wTransportPenalty" | "freshnessFreshDays" | "freshnessRecentDays" | "freshnessStaleDays", string>;

const WEIGHT_FIELDS: [keyof WeightsForm, string][] = [
  ["wNetRealization", "Weight: net realization"],
  ["wPrice", "Weight: price"],
  ["wTrend", "Weight: trend"],
  ["wFreshness", "Weight: freshness"],
  ["wTransportPenalty", "Weight: transport penalty"],
  ["freshnessFreshDays", "Fresh: up to (days)"],
  ["freshnessRecentDays", "Recent: up to (days)"],
  ["freshnessStaleDays", "Stale: up to (days)"],
];

function WeightsConfig() {
  const fmt = useAdminFormat();
  const [version, setVersion] = useState(0);
  const [saved, setSaved] = useState<WeightsResponse | null>(null);
  const active = useQuery(`weights:${version}`, (signal) => adminApi.getRecommendationWeights(signal));

  return (
    <Card className="flex flex-col gap-4 px-4 py-4">
      <h2 className="text-heading font-semibold">Recommendation weights and freshness thresholds</h2>
      {active.status === "error" ? <AdminError error={active.error} onRetry={active.retry} /> : null}
      {!active.data ? <Skeleton className="h-32" /> : (
        <>
          <p className="text-label text-ink-2">Active version {active.data.versionId}, created {fmt.dateTime(active.data.createdAt)}.</p>
          <WeightsFormFields
            key={active.data.versionId}
            active={active.data}
            onSaved={(response) => {
              setSaved(response);
              setVersion((value) => value + 1);
            }}
          />
        </>
      )}
      {saved ? <Notice tone="success" live>Saved as version {saved.versionId}. It applies to new comparisons.</Notice> : null}
      <Versions kind="weights" refresh={version} />
    </Card>
  );
}

function WeightsFormFields({ active, onSaved }: { active: WeightsResponse; onSaved: (response: WeightsResponse) => void }) {
  const [form, setForm] = useState<WeightsForm>(() => ({
    wNetRealization: String(active.wNetRealization),
    wPrice: String(active.wPrice),
    wTrend: String(active.wTrend),
    wFreshness: String(active.wFreshness),
    wTransportPenalty: String(active.wTransportPenalty),
    freshnessFreshDays: String(active.freshnessFreshDays),
    freshnessRecentDays: String(active.freshnessRecentDays),
    freshnessStaleDays: String(active.freshnessStaleDays),
  }));
  const [errors, setErrors] = useState<Partial<Record<keyof WeightsForm | "days", string>>>({});
  const [busy, setBusy] = useState(false);
  const [failure, setFailure] = useState<unknown>(null);

  const save = async () => {
    const next: typeof errors = {};
    for (const key of ["wNetRealization", "wPrice", "wTrend", "wFreshness", "wTransportPenalty"] as const) {
      if (!WEIGHT.test(form[key].trim())) next[key] = "Enter a weight from 0 to 1 with at most 4 decimal places.";
    }
    for (const key of ["freshnessFreshDays", "freshnessRecentDays", "freshnessStaleDays"] as const) {
      if (!WHOLE.test(form[key].trim()) || Number(form[key].trim()) > DAYS_MAX) next[key] = "Enter a whole number of days.";
    }
    if (Object.keys(next).length === 0) {
      const [fresh, recent, stale] = [form.freshnessFreshDays, form.freshnessRecentDays, form.freshnessStaleDays].map(Number);
      if (!(fresh <= recent && recent <= stale)) next.days = "Fresh days must be at most recent days, and recent days at most stale days.";
    }
    setErrors(next);
    if (Object.keys(next).length > 0) return;
    setBusy(true);
    setFailure(null);
    try {
      onSaved(await adminApi.updateRecommendationWeights({
        wNetRealization: Number(form.wNetRealization),
        wPrice: Number(form.wPrice),
        wTrend: Number(form.wTrend),
        wFreshness: Number(form.wFreshness),
        wTransportPenalty: Number(form.wTransportPenalty),
        freshnessFreshDays: Number(form.freshnessFreshDays),
        freshnessRecentDays: Number(form.freshnessRecentDays),
        freshnessStaleDays: Number(form.freshnessStaleDays),
      }));
    } catch (error) {
      setFailure(error);
    } finally {
      setBusy(false);
    }
  };

  return (
    <>
      <div className="grid gap-3 tablet:grid-cols-2 desktop:grid-cols-4">
        {WEIGHT_FIELDS.map(([key, label]) => (
          <Field key={key} label={label} error={errors[key]}>
            {({ inputId, describedBy, invalid }) => (
              <TextInput id={inputId} inputMode="decimal" aria-describedby={describedBy} invalid={invalid} value={form[key]} onChange={(event) => setForm((current) => ({ ...current, [key]: event.target.value }))} className="figure" />
            )}
          </Field>
        ))}
      </div>
      {errors.days ? <p className="text-label font-medium text-error" role="alert">{errors.days}</p> : null}
      {failure ? <AdminError error={failure} /> : null}
      <Button className="w-fit" busy={busy} onClick={() => void save()}>Save as a new version</Button>
    </>
  );
}

function Versions({ kind, refresh }: { kind: "transport" | "weights"; refresh: number }) {
  const fmt = useAdminFormat();
  const [page, setPage] = useState(0);
  const versions = useQuery(`${kind}-versions:${page}:${refresh}`, (signal): Promise<TransportAssumptionPage | WeightsPage> => kind === "transport"
    ? adminApi.listTransportAssumptionVersions({ page, size: 10 }, signal)
    : adminApi.listRecommendationWeightVersions({ page, size: 10 }, signal));
  if (versions.status === "error") return <AdminError error={versions.error} onRetry={versions.retry} />;
  if (!versions.data) return <Skeleton className="h-24" />;
  return (
    <details>
      <summary className="min-h-10 text-label font-semibold text-net">Version history ({versions.data.totalItems})</summary>
      <div className="mt-2 flex flex-col gap-2">
        <AdminTable
          caption="Versions"
          head={<tr><th scope="col" className={th}>Created</th><th scope="col" className={th}>Values</th><th scope="col" className={th}>Active</th></tr>}
        >
          {(versions.data.items as (TransportAssumptionPage["items"][number] | WeightsPage["items"][number])[]).map((item) => (
            <tr key={item.versionId}>
              <td className={td}>{fmt.dateTime(item.createdAt)}</td>
              <td className={`${td} figure`}>
                {"ratePerKm" in item
                  ? `₹${item.ratePerKm}/km · minimum ₹${item.minimumTripCharge} · ${item.vehicleCapacityKg} kg`
                  : `net ${item.wNetRealization} · price ${item.wPrice} · trend ${item.wTrend} · freshness ${item.wFreshness} · transport ${item.wTransportPenalty} · days ${item.freshnessFreshDays}/${item.freshnessRecentDays}/${item.freshnessStaleDays}`}
              </td>
              <td className={td}>{item.active ? "Active" : "—"}</td>
            </tr>
          ))}
        </AdminTable>
        <Pager page={page} totalPages={versions.data.totalPages} onPage={setPage} />
      </div>
    </details>
  );
}
