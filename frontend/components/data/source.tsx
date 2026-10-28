"use client";

import type { PriceQuote, SourceDataset, SourceSyncStatus } from "@/lib/api/types";
import { useI18n } from "@/lib/i18n/i18n-provider";
import { RefreshIcon, WarningIcon } from "@/components/icons";
import { FreshnessBadge, ReportedPrice } from "./price-bits";

/** The attribution required on every price screen (spec §31 RULE 7). The API sends it as a contract constant. */
export const ATTRIBUTION = "Source: Ministry of Agriculture & FW / DMI / data.gov.in";

/** "Latest reported prices · Last synchronized …", or the never-synchronized state (role file 02 §7, E23). */
export function SourceStatusLine({ status }: { status: SourceSyncStatus }) {
  const { t, fmt } = useI18n();
  return (
    <p className="flex items-start gap-1.5 text-caption text-ink-2">
      <RefreshIcon size={16} className="mt-0.5 shrink-0" />
      <span>
        {status.lastSuccessfulSyncAt
          ? t("sync.status", { time: fmt.dateTime(status.lastSuccessfulSyncAt) })
          : t("state.never_synced")}
      </span>
    </p>
  );
}

/** The §29 outage banner, shown above content while the latest current-dataset run failed (E02, E24). */
export function OutageBanner({ status }: { status: SourceSyncStatus }) {
  const { t, fmt } = useI18n();
  if (!status.latestSyncFailed) return null;
  return (
    <div role="status" className="flex gap-3 rounded-card border-2 border-outage-border bg-outage-bg px-4 py-3 text-outage">
      <WarningIcon className="mt-0.5 shrink-0" />
      <div>
        <p className="text-body font-semibold">{t("state.outage")}</p>
        {status.lastSuccessfulSyncAt ? (
          <p className="text-label">{t("sync.last", { time: fmt.dateTime(status.lastSuccessfulSyncAt) })}</p>
        ) : null}
      </div>
    </div>
  );
}

/** Attribution (verbatim, PDD-04) and the standard disclaimer (spec §49, verbatim) closing a price screen. */
export function PriceScreenFooter({ attribution = ATTRIBUTION, children }: { attribution?: string; children?: React.ReactNode }) {
  const { t } = useI18n();
  return (
    <footer className="mt-8 flex flex-col gap-3 border-t border-rule pt-5 text-caption text-ink-2">
      {children}
      <p className="font-semibold text-ink" lang="en">{attribution}</p>
      <p>{t("disclaimer")}</p>
    </footer>
  );
}

export function datasetLabelKey(dataset: SourceDataset): "dataset.CURRENT_PRICE" | "dataset.HISTORICAL_PRICE" | null {
  return dataset === "CURRENT_PRICE" || dataset === "HISTORICAL_PRICE" ? `dataset.${dataset}` : null;
}

/** "About this price": the full provenance of one observation (role file 02 S05; spec F1.17). */
export function ProvenancePanel({ price, defaultOpen = false }: { price: PriceQuote; defaultOpen?: boolean }) {
  const { t, fmt } = useI18n();
  const datasetKey = datasetLabelKey(price.sourceDataset);
  const rows: [string, React.ReactNode][] = [
    [t("provenance.modal"), <ReportedPrice key="m" amount={price.modalPrice} unit={price.priceUnit} />],
    [
      t("provenance.range"),
      price.minPrice !== null && price.maxPrice !== null ? (
        <span key="r" className="figure">
          {fmt.reportedRupees(price.minPrice)} – {fmt.reportedRupees(price.maxPrice)}
        </span>
      ) : (
        t("provenance.range_absent")
      ),
    ],
    [t("provenance.report_date"), <span key="d">{fmt.date(price.reportDate)}</span>],
    [t("provenance.freshness"), <FreshnessBadge key="f" status={price.freshnessStatus} reportDate={price.reportDate} describe />],
    [t("provenance.variety"), <span key="v" lang="en">{price.variety?.name ?? t("provenance.not_reported")}</span>],
    [t("provenance.grade"), <span key="g" lang="en">{price.grade?.name ?? t("provenance.not_reported")}</span>],
    [t("provenance.dataset"), datasetKey ? t(datasetKey) : <span key="ds" lang="en">{price.sourceDataset}</span>],
    [t("provenance.resource"), <span key="res" className="figure break-all" lang="en">{price.sourceResourceId}</span>],
    [t("provenance.stored"), <span key="s">{fmt.dateTime(price.ingestedAt)}</span>],
  ];
  return (
    <details className="group rounded-card border border-rule bg-raised" open={defaultOpen}>
      <summary className="flex min-h-12 items-center justify-between px-4 text-label font-semibold text-ink">
        {t("provenance.title")}
        <span className="text-ink-2 group-open:rotate-180" aria-hidden="true">▾</span>
      </summary>
      <dl className="grid grid-cols-1 gap-x-4 gap-y-2 border-t border-rule px-4 py-3 text-label tablet:grid-cols-[auto_1fr]">
        {rows.map(([label, value]) => (
          <div key={label} className="contents">
            <dt className="text-ink-2">{label}</dt>
            <dd className="text-ink">{value}</dd>
          </div>
        ))}
        <div className="contents">
          <dt className="text-ink-2">{t("provenance.source")}</dt>
          <dd className="text-ink" lang="en">{price.attribution}</dd>
        </div>
      </dl>
    </details>
  );
}
