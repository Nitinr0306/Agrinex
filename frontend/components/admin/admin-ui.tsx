"use client";

import { useMemo, type ReactNode } from "react";
import { Button } from "@/components/ui/button";
import { Notice } from "@/components/ui/feedback";
import { ApiError, NetworkError } from "@/lib/api/errors";
import type { IngestionRunStatus } from "@/lib/api/types";
import { createFormatter } from "@/lib/i18n/format";

/** Admin screens are English (spec §6 persona 3; role file 02 §11). */
export function useAdminFormat() {
  return useMemo(() => createFormatter("en"), []);
}

export function AdminPage({ title, actions, children }: { title: string; actions?: ReactNode; children: ReactNode }) {
  return (
    <div className="flex flex-col gap-5">
      <div className="flex flex-wrap items-end justify-between gap-3 border-b border-rule pb-3">
        <h1 className="text-title font-bold">{title}</h1>
        {actions ? <div className="flex flex-wrap gap-2">{actions}</div> : null}
      </div>
      {children}
    </div>
  );
}

/**
 * Admin error with the API's English `detail` (allowed on admin screens, FRONTEND_CONSUMPTION §5), the code and
 * the correlation id for support.
 */
export function AdminError({ error, onRetry }: { error: unknown; onRetry?: () => void }) {
  let title = "Something went wrong on our side. Please try again.";
  let detail: string | null = null;
  let reference: string | null = null;
  if (error instanceof NetworkError) title = "Could not reach AgriNex. Check your connection and try again.";
  if (error instanceof ApiError) {
    reference = error.correlationId;
    detail = error.detail;
    title = error.status === 403
      ? "You do not have access to this page."
      : error.status === 503
        ? "AgriNex is temporarily unavailable. Please try again in a few minutes."
        : error.status === 409
          ? "This change conflicts with the current state."
          : error.status === 429
            ? `Too many requests. Try again in ${error.retryAfterSeconds ?? 60} seconds.`
            : `${error.status} ${error.code ?? "error"}`;
  }
  return (
    <Notice tone="error" live title={title} action={onRetry ? <Button variant="secondary" onClick={onRetry}>Try again</Button> : null}>
      {detail ? <p>{detail}</p> : null}
      {error instanceof ApiError && error.fieldErrors.length > 0 ? (
        <ul className="list-disc pl-5">
          {error.fieldErrors.map((field) => <li key={`${field.field}:${field.code}`}><code>{field.field}</code>: {field.message}</li>)}
        </ul>
      ) : null}
      {reference ? <p className="figure text-caption text-ink-2">Reference: {reference}</p> : null}
    </Notice>
  );
}

const RUN_STYLE: Record<IngestionRunStatus, string> = {
  RUNNING: "border-recent bg-recent-bg text-recent",
  SUCCESS: "border-fresh bg-fresh-bg text-fresh",
  PARTIAL_FAILURE: "border-stale bg-stale-bg text-stale",
  FAILED: "border-error bg-error-bg text-error",
};

/** A run status as a word in a box, never colour alone. */
export function RunStatus({ status }: { status: IngestionRunStatus }) {
  return <span className={`inline-block rounded-control border px-2 py-0.5 text-caption font-semibold ${RUN_STYLE[status]}`}>{status}</span>;
}

export function Pager({ page, totalPages, onPage }: { page: number; totalPages: number; onPage: (page: number) => void }) {
  if (totalPages <= 1) return null;
  return (
    <nav aria-label="Pages" className="flex items-center justify-between gap-2">
      <Button variant="secondary" disabled={page === 0} onClick={() => onPage(page - 1)}>Previous</Button>
      <span className="text-label text-ink-2">Page {page + 1} of {totalPages}</span>
      <Button variant="secondary" disabled={page >= totalPages - 1} onClick={() => onPage(page + 1)}>Next</Button>
    </nav>
  );
}

export function KeyValues({ rows }: { rows: [string, ReactNode][] }) {
  return (
    <dl className="grid grid-cols-[auto_1fr] gap-x-6 gap-y-1.5 text-label">
      {rows.map(([label, value]) => (
        <div key={label} className="contents">
          <dt className="text-ink-2">{label}</dt>
          <dd className="text-right figure">{value}</dd>
        </div>
      ))}
    </dl>
  );
}

/** A table that scrolls inside its own frame on narrow screens, never the page. */
export function AdminTable({ caption, head, children }: { caption: string; head: ReactNode; children: ReactNode }) {
  return (
    <div className="relative overflow-x-auto rounded-card border border-rule bg-raised">
      <table className="w-full min-w-[44rem] border-collapse text-label">
        <caption className="sr-only">{caption}</caption>
        <thead className="bg-sunken text-left text-caption uppercase text-ink-2">{head}</thead>
        <tbody>{children}</tbody>
      </table>
    </div>
  );
}

export const th = "px-3 py-2 font-semibold";
export const td = "border-t border-rule px-3 py-2 align-top";
