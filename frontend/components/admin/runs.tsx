"use client";

import { useEffect, useState } from "react";
import { Card, Skeleton } from "@/components/ui/feedback";
import { Field, Select, TextInput } from "@/components/ui/form";
import { adminApi } from "@/lib/api/endpoints";
import type { IngestionErrorType, IngestionRunStatus, SourceDataset } from "@/lib/api/types";
import { useQuery } from "@/lib/hooks/use-query";
import { AdminError, AdminPage, AdminTable, KeyValues, Pager, RunStatus, td, th, useAdminFormat } from "./admin-ui";
import { RunsTable } from "./data-health";

const STATUSES: IngestionRunStatus[] = ["RUNNING", "SUCCESS", "PARTIAL_FAILURE", "FAILED"];
const ERROR_TYPES: IngestionErrorType[] = ["VALIDATION", "API_ERROR", "CONFLICT", "SCHEMA_ERROR", "SYSTEM"];
const POLL_MS = 5000;

/** A02 run history, newest first (listIngestionRuns). */
export function RunsList() {
  const [datasetType, setDatasetType] = useState<SourceDataset | "">("");
  const [status, setStatus] = useState<IngestionRunStatus | "">("");
  const [page, setPage] = useState(0);
  const runs = useQuery(`runs:${datasetType}:${status}:${page}`, (signal) =>
    adminApi.listIngestionRuns({ page, size: 20, datasetType: datasetType || undefined, status: status || undefined }, signal));

  return (
    <AdminPage title="Ingestion runs">
      <div className="grid gap-3 tablet:grid-cols-2 desktop:max-w-xl">
        <Field label="Dataset">
          {({ inputId }) => (
            <Select id={inputId} value={datasetType} onChange={(event) => { setDatasetType(event.target.value as SourceDataset | ""); setPage(0); }}>
              <option value="">All</option>
              <option value="CURRENT_PRICE">CURRENT_PRICE</option>
              <option value="HISTORICAL_PRICE">HISTORICAL_PRICE</option>
            </Select>
          )}
        </Field>
        <Field label="Status">
          {({ inputId }) => (
            <Select id={inputId} value={status} onChange={(event) => { setStatus(event.target.value as IngestionRunStatus | ""); setPage(0); }}>
              <option value="">All</option>
              {STATUSES.map((value) => <option key={value} value={value}>{value}</option>)}
            </Select>
          )}
        </Field>
      </div>
      {runs.status === "error" ? <AdminError error={runs.error} onRetry={runs.retry} /> : null}
      {runs.data ? <RunsTable runs={runs.data.items} /> : <Skeleton className="h-48" />}
      {runs.data ? <Pager page={page} totalPages={runs.data.totalPages} onPage={setPage} /> : null}
    </AdminPage>
  );
}

/** A02 run detail with every counter and its errors (getIngestionRun, listIngestionRunErrors). */
export function RunDetail({ runId }: { runId: string }) {
  const fmt = useAdminFormat();
  const [tick, setTick] = useState(0);
  const run = useQuery(`run:${runId}:${tick}`, (signal) => adminApi.getIngestionRun(runId, signal));
  const running = run.data?.status === "RUNNING";

  // Follow a run that is still RUNNING (ADR-0011: a triggered run answers 202 and is followed by getIngestionRun).
  useEffect(() => {
    if (!running) return;
    const timer = setTimeout(() => setTick((value) => value + 1), POLL_MS);
    return () => clearTimeout(timer);
  }, [running, tick]);

  return (
    <AdminPage title="Ingestion run">
      {run.status === "error" ? <AdminError error={run.error} onRetry={run.retry} /> : null}
      {!run.data ? <Skeleton className="h-48" /> : (
        <Card className="flex flex-col gap-3 px-4 py-4">
          <div className="flex flex-wrap items-center gap-3">
            <h2 className="text-heading font-semibold">{run.data.datasetType}</h2>
            <RunStatus status={run.data.status} />
            <span className="text-label text-ink-2">{run.data.triggeredBy}</span>
            {running ? <span className="text-caption text-ink-2" role="status">Refreshing every 5 seconds while running.</span> : null}
          </div>
          <KeyValues
            rows={[
              ["Run id", run.data.id],
              ["Started", fmt.dateTime(run.data.startedAt)],
              ["Finished", run.data.finishedAt ? fmt.dateTime(run.data.finishedAt) : "—"],
              ["Duration", run.data.durationSeconds === null ? "—" : `${run.data.durationSeconds} s`],
              ["Source total records", run.data.sourceTotalRecords ?? "—"],
              ["Pages fetched / failed", `${run.data.pagesFetched} / ${run.data.pagesFailed}`],
              ["Fetched", run.data.recordsFetched],
              ["Inserted", run.data.recordsInserted],
              ["Updated", run.data.recordsUpdated],
              ["Duplicates skipped", run.data.duplicatesSkipped],
              ["Validation errors", run.data.validationErrorsCount],
              ["Conflicts", run.data.recordsConflicted],
              ["Out of scope", run.data.recordsOutOfScope],
              ["Completeness", run.data.completenessPercent === null ? "—" : `${fmt.decimal(run.data.completenessPercent, 2)}%`],
              ["Error rows", run.data.errorCount],
            ]}
          />
          <details className="rounded-control border border-rule">
            <summary className="min-h-10 px-3 py-2 text-label font-semibold">Request scope</summary>
            <pre className="overflow-x-auto bg-sunken px-3 py-2 text-caption">{JSON.stringify(run.data.requestScope, null, 2)}</pre>
          </details>
        </Card>
      )}
      <RunErrors runId={runId} />
    </AdminPage>
  );
}

function RunErrors({ runId }: { runId: string }) {
  const fmt = useAdminFormat();
  const [errorType, setErrorType] = useState<IngestionErrorType | "">("");
  const [errorCode, setErrorCode] = useState("");
  const [page, setPage] = useState(0);
  const code = /^[A-Z][A-Z0-9_]*$/.test(errorCode) ? errorCode : "";
  const errors = useQuery(`run-errors:${runId}:${errorType}:${code}:${page}`, (signal) =>
    adminApi.listIngestionRunErrors(runId, { page, size: 20, errorType: errorType || undefined, errorCode: code || undefined }, signal));

  return (
    <section className="flex flex-col gap-3">
      <h2 className="eyebrow">Errors</h2>
      <div className="grid gap-3 tablet:grid-cols-2 desktop:max-w-xl">
        <Field label="Error type">
          {({ inputId }) => (
            <Select id={inputId} value={errorType} onChange={(event) => { setErrorType(event.target.value as IngestionErrorType | ""); setPage(0); }}>
              <option value="">All</option>
              {ERROR_TYPES.map((value) => <option key={value} value={value}>{value}</option>)}
            </Select>
          )}
        </Field>
        <Field label="Error code" help="Upper case, for example UNKNOWN_DISTRICT.">
          {({ inputId, describedBy }) => (
            <TextInput id={inputId} aria-describedby={describedBy} value={errorCode} onChange={(event) => { setErrorCode(event.target.value.toUpperCase()); setPage(0); }} />
          )}
        </Field>
      </div>
      {errors.status === "error" ? <AdminError error={errors.error} onRetry={errors.retry} /> : null}
      {!errors.data ? <Skeleton className="h-32" /> : (
        <AdminTable
          caption="Run errors"
          head={(
            <tr>
              <th scope="col" className={th}>Type</th>
              <th scope="col" className={th}>Code</th>
              <th scope="col" className={th}>Message</th>
              <th scope="col" className={th}>Page / date</th>
              <th scope="col" className={th}>Recorded</th>
            </tr>
          )}
        >
          {errors.data.items.length === 0 ? (
            <tr><td className={td} colSpan={5}>No error rows.</td></tr>
          ) : errors.data.items.map((item) => (
            <tr key={item.id}>
              <td className={td}>{item.errorType}</td>
              <td className={`${td} figure`}>{item.errorCode}</td>
              <td className={td}>
                {item.errorMessage}
                {item.rawPayloadExcerpt ? (
                  <details className="mt-1">
                    <summary className="text-caption font-semibold text-net">Raw payload excerpt{item.rawPayloadTruncated ? " (truncated)" : ""}</summary>
                    <pre className="mt-1 max-h-48 overflow-auto whitespace-pre-wrap break-all bg-sunken p-2 text-caption">{item.rawPayloadExcerpt}</pre>
                  </details>
                ) : null}
              </td>
              <td className={`${td} figure`}>
                {item.pageOffset !== null ? `offset ${item.pageOffset}` : "—"}
                {item.requestReportDate ? ` · ${fmt.date(item.requestReportDate)}` : ""}
              </td>
              <td className={td}>{fmt.dateTime(item.createdAt)}</td>
            </tr>
          ))}
        </AdminTable>
      )}
      {errors.data ? <Pager page={page} totalPages={errors.data.totalPages} onPage={setPage} /> : null}
    </section>
  );
}
