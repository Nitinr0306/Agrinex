import type { ReactNode } from "react";
import { InfoIcon, WarningIcon, XCircleIcon } from "@/components/icons";

type NoticeTone = "info" | "warning" | "error" | "success";

const TONES: Record<NoticeTone, { box: string; icon: ReactNode }> = {
  info: { box: "border-rule-strong bg-sunken text-ink", icon: <InfoIcon /> },
  warning: { box: "border-outage-border bg-outage-bg text-outage", icon: <WarningIcon /> },
  error: { box: "border-error bg-error-bg text-ink", icon: <XCircleIcon className="text-error" /> },
  success: { box: "border-fresh bg-fresh-bg text-ink", icon: <InfoIcon className="text-fresh" /> },
};

/** A boxed message with an icon; `live` announces it to screen readers when it appears. */
export function Notice({ tone = "info", title, children, live = false, action }: {
  tone?: NoticeTone;
  title?: ReactNode;
  children?: ReactNode;
  live?: boolean;
  action?: ReactNode;
}) {
  const { box, icon } = TONES[tone];
  return (
    <div
      className={`flex gap-3 rounded-card border px-4 py-3 ${box}`}
      role={live ? (tone === "error" ? "alert" : "status") : undefined}
    >
      <span className="mt-0.5 shrink-0">{icon}</span>
      <div className="flex min-w-0 flex-1 flex-col gap-1">
        {title ? <p className="text-body font-semibold">{title}</p> : null}
        {children ? <div className="text-label">{children}</div> : null}
        {action ? <div className="mt-2">{action}</div> : null}
      </div>
    </div>
  );
}

/** Placeholder shapes of a layout while its data loads (APPROVED PDD-13: only after 300 ms). */
export function Skeleton({ className = "" }: { className?: string }) {
  return <div className={`animate-pulse rounded-control bg-sunken motion-reduce:animate-none ${className}`} aria-hidden="true" />;
}

/** A section card: white ledger sheet with a hairline border, no shadow (role file 02 §1.1). */
export function Card({ children, className = "", as: Tag = "section", ...rest }: {
  children: ReactNode;
  className?: string;
  as?: "section" | "div" | "article" | "aside";
  "aria-labelledby"?: string;
  "aria-label"?: string;
}) {
  return (
    <Tag className={`rounded-card border border-rule bg-raised ${className}`} {...rest}>
      {children}
    </Tag>
  );
}

export function EmptyState({ icon, title, children, actions }: {
  icon?: ReactNode;
  title: ReactNode;
  children?: ReactNode;
  actions?: ReactNode;
}) {
  return (
    <Card className="flex flex-col items-center gap-3 px-5 py-8 text-center">
      {icon ? <span className="flex h-14 w-14 items-center justify-center rounded-card border border-rule bg-sunken text-ink-2">{icon}</span> : null}
      <h2 className="text-heading font-semibold text-ink">{title}</h2>
      {children ? <div className="max-w-prose text-body text-ink-2">{children}</div> : null}
      {actions ? <div className="mt-2 flex w-full flex-col gap-2 tablet:w-auto tablet:flex-row">{actions}</div> : null}
    </Card>
  );
}
