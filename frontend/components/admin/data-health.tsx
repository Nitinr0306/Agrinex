"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, Notice, Skeleton } from "@/components/ui/feedback";
import { Field, Select, TextInput } from "@/components/ui/form";
import { adminApi } from "@/lib/api/endpoints";
import type { DatasetHealth, IngestionRunResponse, ReportDateStatus, SourceDataset } from "@/lib/api/types";
import { useQuery } from "@/lib/hooks/use-query";
import { AdminError, AdminPage, AdminTable, KeyValues, RunStatus, td, th, useAdminFormat } from "./admin-ui";

/** A01 data health (role file 02 §8 A01; FR-18; ADR-0019 item 7, ADR-0020). */
export function DataHealth() {
  const fmt = useAdminFormat();
  const [attempt, setAttempt] = useState(0);
  const health = useQuery(`health:${attempt}`, (signal) => adminApi.getIngestionHealth(signal));
  const system = useQuery(`system:${attempt}`, (signal) => adminApi.getSystemStatus(signal));
  const runs = useQuery(`recent-runs:${attempt}`, (signal) => adminApi.listIngestionRuns({ page: 0, size: 8 }, signal));
  const reload = () => setAttempt((value) => value + 1);

  const failing = health.data?.datasets.filter((dataset) => dataset.consecutiveFailures > 0) ?? [];

  return (
    <AdminPage title="Data health" actions={<Button variant="secondary" onClick={reload}>Refresh</Button>}>
      {health.status === "error" ? <AdminError error={health.error} onRetry={reload} /> : null}
      {failing.map((dataset) => (
        <Notice key={dataset.datasetType} tone="error" title={`${dataset.title}: latest run failed (${dataset.consecutiveFailures} consecutive ${dataset.consecutiveFailures === 1 ? "failure" : "failures"}).`}>
          Last successful run: {dataset.lastSuccessfulRunAt ? fmt.dateTime(dataset.lastSuccessfulRunAt) : "never"}.
          {dataset.latestCompletedRun ? (
            <> <Link className="font-semibold text-net underline" href={`/admin/runs/${dataset.latestCompletedRun.id}`}>View the run and its errors</Link></>
          ) : null}
        </Notice>
      ))}

      {!health.data ? <Skeleton className="h-64" /> : (
        <div className="grid gap-4 desktop:grid-cols-2">
          {health.data.datasets.map((dataset) => <DatasetCard key={dataset.datasetType} dataset={dataset} onChanged={reload} />)}
        </div>
      )}

      <HistoricalCoverage attempt={attempt} />

      {health.data ? (
        <Card className="px-4 py-4">
          <h2 className="eyebrow mb-3">Market freshness, {fmt.date(health.data.marketFreshness.asOfDate)} ({health.data.marketFreshness.totalMarkets} verified markets)</h2>
          <div className="grid grid-cols-2 gap-2 tablet:grid-cols-4">
            {([
              ["Fresh", health.data.marketFreshness.fresh, "border-fresh bg-fresh-bg text-fresh"],
              ["Recent", health.data.marketFreshness.recent, "border-recent bg-recent-bg text-recent"],
              ["Stale", health.data.marketFreshness.stale, "border-stale bg-stale-bg text-stale"],
              ["Unavailable", health.data.marketFreshness.unavailable, "border-unavailable bg-unavailable-bg text-unavailable"],
            ] as const).map(([label, count, style]) => (
              <div key={label} className={`rounded-card border px-3 py-2 ${style}`}>
                <p className="figure text-title font-semibold">{count}</p>
                <p className="text-label font-semibold">{label}</p>
              </div>
            ))}
          </div>
        </Card>
      ) : null}

      {system.status === "error" ? <AdminError error={system.error} onRetry={reload} /> : null}
      {system.data ? (
        <Card className="px-4 py-4">
          <h2 className="eyebrow mb-3">System</h2>
          <ul className="flex flex-col gap-1 text-label">
            <li>Database: <strong>{system.data.components.database}</strong></li>
            <li>
              Redis: <strong>{system.data.components.redis}</strong>
              {system.data.components.redis !== "UP" ? " — rate limiting and caching run in-process" : ""}
            </li>
            <li>Government API key: <strong>{system.data.governmentApiKey}</strong> (the value is never shown)</li>
            <li>Runs at {system.data.schedule.runTimes.join(", ")} ({system.data.schedule.timeZone}); historical window {system.data.schedule.historicalWindowDaysBack} days back</li>
            <li>Coverage: {system.data.coverage.sourceStates.join(", ")}</li>
            <li className="text-caption text-ink-2">Status at {fmt.dateTime(system.data.generatedAt)}</li>
          </ul>
        </Card>
      ) : null}

      <TriggerForm />

      <section className="flex flex-col gap-2">
        <div className="flex items-baseline justify-between">
          <h2 className="eyebrow">Recent runs</h2>
          <Link href="/admin/runs" className="text-label font-semibold text-net underline">All runs</Link>
        </div>
        {runs.status === "error" ? <AdminError error={runs.error} onRetry={reload} /> : null}
        {runs.data ? <RunsTable runs={runs.data.items} /> : <Skeleton className="h-32" />}
      </section>
    </AdminPage>
  );
}

function DatasetCard({ dataset, onChanged }: { dataset: DatasetHealth; onChanged: () => void }) {
  const fmt = useAdminFormat();
  const run = dataset.latestCompletedRun;
  return (
    <Card className="flex flex-col gap-3 px-4 py-4">
      <div className="flex items-start justify-between gap-3">
        <div>
          <h2 className="text-heading font-semibold">{dataset.title}</h2>
          <p className="figure text-caption text-ink-2">{dataset.datasetType} · resource {dataset.resourceId}</p>
        </div>
        {run ? <RunStatus status={run.status} /> : <span className="text-caption text-ink-2">No completed run</span>}
      </div>
      <p className="text-label text-ink-2">
        Last success {dataset.lastSuccessfulRunAt ? fmt.dateTime(dataset.lastSuccessfulRunAt) : "never"}
        {" · "}Latest report date {dataset.latestReportDate ? fmt.date(dataset.latestReportDate) : "none"}
        {dataset.runningRunId ? <> · <Link className="font-semibold text-net underline" href={`/admin/runs/${dataset.runningRunId}`}>A run is in progress</Link></> : null}
      </p>
      {run ? (
        <KeyValues
          rows={[
            ["Fetched", run.recordsFetched],
            ["Inserted", run.recordsInserted],
            ["Updated", run.recordsUpdated],
            ["Duplicates skipped", run.duplicatesSkipped],
            ["Validation errors", run.validationErrorsCount],
            ["Conflicts", run.recordsConflicted],
            ["Out of scope", run.recordsOutOfScope],
            ["Completeness", run.completenessPercent === null ? "—" : `${fmt.decimal(run.completenessPercent, 2)}%`],
            ["Pages fetched / failed", `${run.pagesFetched} / ${run.pagesFailed}`],
            ["Duration", run.durationSeconds === null ? "—" : `${run.durationSeconds} s`],
          ]}
        />
      ) : null}
      <ScheduleControl dataset={dataset} onChanged={onChanged} />
    </Card>
  );
}

const COVERAGE_POLL_MS = 5000;

const DATE_STATUS_STYLE: Record<ReportDateStatus, string> = {
  SETTLED: "border-fresh bg-fresh-bg text-fresh",
  PENDING: "border-recent bg-recent-bg text-recent",
  UNSETTLED: "border-stale bg-stale-bg text-stale",
  FAILED: "border-error bg-error-bg text-error",
  MISSING: "border-unavailable bg-unavailable-bg text-unavailable",
};

/**
 * Coverage of the historical report dates and the progress of a running historical run (ADR-0054 item 6). A date
 * is complete once fetched at least the settle age after its report date; the daily gap check and the weekly
 * reconciliation fetch the incomplete ones. Followed every 5 seconds while a run is in progress.
 */
function HistoricalCoverage({ attempt }: { attempt: number }) {
  const fmt = useAdminFormat();
  const [tick, setTick] = useState(0);
  const coverage = useQuery(`coverage:${attempt}:${tick}`, (signal) => adminApi.getHistoricalCoverage(signal));
  const running = coverage.data?.runningRun ?? null;

  useEffect(() => {
    if (!running) return;
    const timer = setTimeout(() => setTick((value) => value + 1), COVERAGE_POLL_MS);
    return () => clearTimeout(timer);
  }, [running, tick]);

  if (coverage.status === "error") return <AdminError error={coverage.error} onRetry={coverage.retry} />;
  const data = coverage.data;
  if (!data) return <Skeleton className="h-40" />;
  const { counts } = data;
  return (
    <Card className="flex flex-col gap-3 px-4 py-4">
      <h2 className="eyebrow">Historical report dates, {fmt.date(data.coverageFrom)} to {fmt.date(data.coverageTo)}</h2>
      {data.scheduledRunsPaused ? (
        <Notice tone="warning" title="Scheduled historical runs are paused.">The daily gap check and the weekly reconciliation are skipped until they are resumed.</Notice>
      ) : null}
      <KeyValues
        rows={[
          ["Complete (settled)", counts.settled],
          [`Recent, under ${data.settleAfterDays} days old`, counts.pending],
          ["Fetched too early", counts.unsettled],
          ["Failed", counts.failed],
          ["Never fetched", counts.missing],
          ["Next gap check", data.nextGapCheckAt ? fmt.dateTime(data.nextGapCheckAt) : "—"],
          ["Next reconciliation", data.nextReconciliationAt ? `${fmt.dateTime(data.nextReconciliationAt)} (last ${data.reconciliationWindowDays} days)` : "—"],
        ]}
      />
      {running ? (
        <div className="flex flex-col gap-1 border-t border-rule pt-3">
          <p className="text-label">
            <Link className="font-semibold text-net underline" href={`/admin/runs/${running.runId}`}>{running.purpose ?? "Historical"} run in progress</Link>
            {`: ${running.datesAttempted} of ${running.datesInScope} report dates (${fmt.date(running.reportDateFrom)} to ${fmt.date(running.reportDateTo)})`}
          </p>
          <progress className="h-2 w-full" max={running.datesInScope} value={running.datesAttempted} aria-label="Report dates attempted" />
        </div>
      ) : null}
      {data.incompleteDates.length === 0 ? (
        <p className="text-label text-ink-2">Every report date old enough to be complete has been fetched completely.</p>
      ) : (
        <AdminTable
          caption="Incomplete historical report dates, newest first"
          head={(
            <tr>
              <th scope="col" className={th}>Report date</th>
              <th scope="col" className={th}>Status</th>
              <th scope="col" className={th}>Last attempt (IST)</th>
              <th scope="col" className={th}>Last success (IST)</th>
              <th scope="col" className={`${th} text-right`}>Records</th>
              <th scope="col" className={`${th} text-right`}>Failures</th>
            </tr>
          )}
        >
          {data.incompleteDates.slice(0, data.maxDatesPerGapRun).map((date) => (
            <tr key={date.reportDate}>
              <td className={td}>{fmt.date(date.reportDate)}</td>
              <td className={td}><span className={`inline-block rounded-control border px-2 py-0.5 text-caption font-semibold ${DATE_STATUS_STYLE[date.status]}`}>{date.status}</span></td>
              <td className={td}>{date.lastAttemptAt ? fmt.dateTime(date.lastAttemptAt) : "Never"}</td>
              <td className={td}>{date.lastSuccessAt ? fmt.dateTime(date.lastSuccessAt) : "Never"}</td>
              <td className={`${td} figure text-right`}>{date.recordsFetched ?? "—"}</td>
              <td className={`${td} figure text-right`}>{date.consecutiveFailures}</td>
            </tr>
          ))}
        </AdminTable>
      )}
      {data.incompleteDates.length > data.maxDatesPerGapRun ? (
        <p className="text-caption text-ink-2">Showing the newest {data.maxDatesPerGapRun} of {data.incompleteDates.length} incomplete dates.</p>
      ) : null}
    </Card>
  );
}

/** Pause or resume scheduled runs of one dataset; manual triggers keep working (ADR-0020). */
function ScheduleControl({ dataset, onChanged }: { dataset: DatasetHealth; onChanged: () => void }) {
  const fmt = useAdminFormat();
  const [reason, setReason] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<unknown>(null);
  const { schedule } = dataset;

  const act = async () => {
    setBusy(true);
    setError(null);
    try {
      if (schedule.paused) await adminApi.resumeScheduledIngestion(dataset.datasetType);
      else await adminApi.pauseScheduledIngestion(dataset.datasetType, reason.trim());
      setReason("");
      onChanged();
    } catch (failure) {
      setError(failure);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="flex flex-col gap-2 border-t border-rule pt-3">
      <p className="text-label">
        Schedule: <strong>{schedule.paused ? "PAUSED" : "ACTIVE"}</strong>
        {schedule.paused && schedule.pausedReason ? ` — ${schedule.pausedReason}` : ""} (since {fmt.dateTime(schedule.updatedAt)})
      </p>
      {!schedule.paused ? (
        <Field label="Reason for pausing" help="Required, up to 500 characters.">
          {({ inputId, describedBy }) => (
            <TextInput id={inputId} aria-describedby={describedBy} value={reason} maxLength={500} onChange={(event) => setReason(event.target.value)} />
          )}
        </Field>
      ) : null}
      {error ? <AdminError error={error} /> : null}
      <Button variant="secondary" busy={busy} disabled={!schedule.paused && reason.trim().length === 0} onClick={() => void act()}>
        {schedule.paused ? "Resume scheduled runs" : "Pause scheduled runs"}
      </Button>
    </div>
  );
}

/** Manual run, with an optional report-date range for a historical backfill (ADR-0011 item 3). */
function TriggerForm() {
  const router = useRouter();
  const [datasetType, setDatasetType] = useState<SourceDataset>("CURRENT_PRICE");
  const [fromDate, setFromDate] = useState("");
  const [toDate, setToDate] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<unknown>(null);

  const trigger = async () => {
    setBusy(true);
    setError(null);
    try {
      const historical = datasetType === "HISTORICAL_PRICE";
      const response = await adminApi.triggerIngestion({
        datasetType,
        fromDate: historical && fromDate ? fromDate : null,
        toDate: historical && toDate ? toDate : null,
      });
      router.push(`/admin/runs/${response.runId}`);
    } catch (failure) {
      setError(failure);
    } finally {
      setBusy(false);
    }
  };

  return (
    <Card className="flex flex-col gap-3 px-4 py-4">
      <h2 className="eyebrow">Trigger a run</h2>
      <div className="grid gap-3 tablet:grid-cols-3">
        <Field label="Dataset">
          {({ inputId }) => (
            <Select id={inputId} value={datasetType} onChange={(event) => setDatasetType(event.target.value as SourceDataset)}>
              <option value="CURRENT_PRICE">CURRENT_PRICE</option>
              <option value="HISTORICAL_PRICE">HISTORICAL_PRICE</option>
            </Select>
          )}
        </Field>
        {datasetType === "HISTORICAL_PRICE" ? (
          <>
            <Field label="From report date" optionalLabel="(optional)">
              {({ inputId }) => <TextInput id={inputId} type="date" value={fromDate} onChange={(event) => setFromDate(event.target.value)} />}
            </Field>
            <Field label="To report date" optionalLabel="(optional)">
              {({ inputId }) => <TextInput id={inputId} type="date" value={toDate} onChange={(event) => setToDate(event.target.value)} />}
            </Field>
          </>
        ) : null}
      </div>
      {error ? <AdminError error={error} /> : null}
      <Button busy={busy} onClick={() => void trigger()} className="w-fit">Trigger run</Button>
    </Card>
  );
}

export function RunsTable({ runs }: { runs: IngestionRunResponse[] }) {
  const fmt = useAdminFormat();
  return (
    <AdminTable
      caption="Ingestion runs"
      head={(
        <tr>
          <th scope="col" className={th}>Dataset</th>
          <th scope="col" className={th}>Trigger</th>
          <th scope="col" className={th}>Status</th>
          <th scope="col" className={th}>Started (IST)</th>
          <th scope="col" className={`${th} text-right`}>Duration</th>
          <th scope="col" className={`${th} text-right`}>Fetched</th>
          <th scope="col" className={`${th} text-right`}>Inserted</th>
          <th scope="col" className={`${th} text-right`}>Errors</th>
          <th scope="col" className={`${th} text-right`}>Complete</th>
        </tr>
      )}
    >
      {runs.length === 0 ? (
        <tr><td className={td} colSpan={9}>No runs yet.</td></tr>
      ) : runs.map((run) => (
        <tr key={run.id}>
          <td className={td}><Link className="font-semibold text-net underline" href={`/admin/runs/${run.id}`}>{run.datasetType}</Link></td>
          <td className={td}>{run.triggeredBy}</td>
          <td className={td}><RunStatus status={run.status} /></td>
          <td className={td}>{fmt.dateTime(run.startedAt)}</td>
          <td className={`${td} figure text-right`}>{run.durationSeconds === null ? "—" : `${run.durationSeconds} s`}</td>
          <td className={`${td} figure text-right`}>{run.recordsFetched}</td>
          <td className={`${td} figure text-right`}>{run.recordsInserted}</td>
          <td className={`${td} figure text-right`}>{run.errorCount}</td>
          <td className={`${td} figure text-right`}>{run.completenessPercent === null ? "—" : `${fmt.decimal(run.completenessPercent, 2)}%`}</td>
        </tr>
      ))}
    </AdminTable>
  );
}
