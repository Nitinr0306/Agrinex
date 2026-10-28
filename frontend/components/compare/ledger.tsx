"use client";

import Link from "next/link";
import type { ReactNode } from "react";
import { BellIcon, ChevronRightIcon } from "@/components/icons";
import { Distance, EstimatedRupees, FreshnessBadge, ReasonChips, ReportedPrice, TrendIndicator } from "@/components/data/price-bits";
import { AccessibleText, Message } from "@/components/ui/text";
import type { OptionLabels } from "@/lib/compare/labels";
import { minimumApplies, type OptionView, type TransportBasis } from "@/lib/compare/view-model";
import { Decimal } from "@/lib/decimal";
import { useI18n } from "@/lib/i18n/i18n-provider";
import { publishedNameLang } from "@/lib/i18n/names";

/** Links from an option to its contextual screens (PDD-02: never top-level). */
export function optionLinks(view: OptionView, commodityId: string) {
  const market = view.option.market.id;
  // The comparison's own filter, so the trend page shows the series the card's trend was computed on.
  const filter = new URLSearchParams({ crop: commodityId });
  if (view.filter.varietyId) filter.set("variety", view.filter.varietyId);
  if (view.filter.gradeId) filter.set("grade", view.filter.gradeId);
  return {
    why: `/compare/why?market=${encodeURIComponent(market)}`,
    trend: `/markets/${encodeURIComponent(market)}/trend?${filter.toString()}`,
    detail: `/markets/${encodeURIComponent(market)}?crop=${encodeURIComponent(commodityId)}`,
    alert: `/alerts/new?crop=${encodeURIComponent(commodityId)}&market=${encodeURIComponent(market)}`,
  };
}

/** Badges computed from the response only (ADR-0045; FRONTEND_CONSUMPTION §2.1). */
export function OptionBadges({ labels }: { labels: OptionLabels }) {
  const { t } = useI18n();
  return (
    <span className="flex flex-wrap gap-1.5">
      {labels.recommended ? (
        <span className="eyebrow rounded-control bg-net px-2 py-0.5 text-inverse">{t("badge.recommended")}</span>
      ) : null}
      {labels.whatIfTop ? (
        <span className="eyebrow rounded-control border-2 border-dashed border-hyp bg-hyp-surface px-2 py-0.5 text-hyp">
          {t("whatif.top")}
        </span>
      ) : null}
      {labels.highestPrice ? (
        <span className="eyebrow rounded-control border border-ink px-2 py-0.5 text-ink">{t("badge.highest_price")}</span>
      ) : null}
      {labels.highestNet ? (
        <span className="eyebrow rounded-control border border-net px-2 py-0.5 text-net">{t("badge.highest_net")}</span>
      ) : null}
    </span>
  );
}

/** "≈42 km × ₹8/km × 1 trip" or "minimum charge ₹300 per trip × 1 trip": the working of the transport line. */
export function TransportWorking({ view, basis }: { view: OptionView; basis: TransportBasis }) {
  const { t, fmt } = useI18n();
  const trips = Number(view.trips);
  const tripsText = t(trips === 1 ? "ledger.trips_one" : "ledger.trips_other", { n: fmt.integer(trips) });
  const working = minimumApplies(view, basis)
    ? t("ledger.minimum", { min: fmt.decimal(basis.minimumTripCharge, 2) })
    : t("ledger.per_km", { km: fmt.wholeKm(view.option.distanceKm), rate: fmt.decimal(basis.ratePerKm, 2) });
  return (
    <Message
      id="ledger.working"
      values={{ working: <span className="figure"><AccessibleText text={working} /></span>, trips: tripsText }}
    />
  );
}

/**
 * The shared-scale transport bar (borrowing 4 from C): every bar on one scale from ₹0 to the largest estimated
 * transport shown, with the amount in text. It is never truncated (role file 02 S04).
 */
export function TransportBar({ value, max, reported }: { value: Decimal; max: Decimal; reported?: Decimal | null }) {
  const percent = (amount: Decimal) => (max.isPositive() ? Math.min(100, (amount.toNumber() / max.toNumber()) * 100) : 0);
  const hypothetical = reported !== undefined && reported !== null;
  return (
    <div className="relative h-3 w-full rounded-control bg-transport-track" aria-hidden="true">
      <div
        className={`absolute inset-y-0 left-0 rounded-control ${hypothetical ? "bg-hyp" : "bg-transport-fill"}`}
        style={{ width: `${percent(value)}%` }}
      />
      {hypothetical ? (
        <div className="absolute -top-1 h-5 w-0.5 bg-ink" style={{ left: `${percent(reported)}%` }} title="" />
      ) : null}
    </div>
  );
}

function LedgerLine({ label, value, sub }: { label: ReactNode; value: ReactNode; sub?: ReactNode }) {
  return (
    <div className="py-1">
      <div className="flex items-end">
        <span className="text-label text-ink">{label}</span>
        <span className="leader" aria-hidden="true" />
        <span className="text-label font-medium">{value}</span>
      </div>
      {sub ? <div className="text-caption text-ink-2">{sub}</div> : null}
    </div>
  );
}

/** One market as a ledger card (mobile; role file 02 S04 item 5). */
export function LedgerCard({ view, basis, maxTransport, commodityId, showGloss }: {
  view: OptionView;
  basis: TransportBasis;
  maxTransport: Decimal;
  commodityId: string;
  showGloss: boolean;
}) {
  const { t, fmt, locale } = useI18n();
  const { option } = view;
  const links = optionLinks(view, commodityId);
  const recommended = view.labels.recommended;
  const frame = view.hypothetical
    ? "border-2 border-dashed border-hyp"
    : recommended
      ? "border-2 border-net"
      : "border border-rule";
  const quantity = fmt.decimal(basis.quantityInQuintal, 5);

  return (
    <article className={`rounded-card bg-raised ${frame}`} aria-labelledby={`option-${option.market.id}`}>
      <header className="flex flex-col gap-2 border-b border-rule px-4 pb-3 pt-4">
        <div className="flex items-start justify-between gap-2">
          <OptionBadges labels={view.labels} />
          <FreshnessBadge status={option.price.freshnessStatus} reportDate={option.price.reportDate} />
        </div>
        <h3 id={`option-${option.market.id}`} className="text-heading font-semibold text-ink">
          <span className="sr-only">{t("results.rank", { n: view.rank })}: </span>
          <span lang={publishedNameLang(locale)}>{option.market.name}</span>
          {view.hypothetical ? <span className="ml-2 align-middle text-caption font-semibold text-hyp">[{t("whatif.stamp")}]</span> : null}
        </h3>
        <p className="text-label text-ink-2">
          <Distance km={option.distanceKm} approximateLocation={option.approximateMarketLocation} />
          {" · "}
          <span lang={publishedNameLang(locale)}>{option.market.district.name}</span>
        </p>
      </header>

      <div className="px-4 py-3">
        <LedgerLine
          label={t("price.latest")}
          value={
            <span className="figure">
              <ReportedPrice amount={option.price.modalPrice} unit={option.price.priceUnit} /> × {quantity} {t("unit.quintal")}
            </span>
          }
        />
        <LedgerLine
          label={t("ledger.gross")}
          value={<EstimatedRupees value={view.gross} className={view.hypothetical ? "hyp-value" : ""} />}
        />
        <LedgerLine
          label={t("ledger.transport")}
          value={<EstimatedRupees value={view.transport} className={view.hypothetical ? "hyp-value" : ""} />}
          sub={<TransportWorking view={view} basis={basis} />}
        />
        <div className="mb-2 mt-1">
          <TransportBar value={view.transport} max={maxTransport} reported={view.reported?.transport ?? null} />
        </div>
        <div className="double-rule mt-2 flex items-baseline justify-between gap-2 pt-2">
          <span className={`text-body font-semibold ${view.hypothetical ? "text-hyp" : "text-net"}`}>
            {t("home.sum_net")}
            {showGloss ? <span className="block text-caption font-normal">({t("net.gloss")})</span> : null}
          </span>
          <EstimatedRupees
            value={view.net}
            className={`text-heading font-semibold ${view.hypothetical ? "hyp-value" : "text-net"}`}
          />
        </div>
        {view.reported && !view.reported.net.eq(view.net) ? (
          <p className="text-right text-caption text-ink-2">
            <Message id="whatif.was" values={{ value: <EstimatedRupees value={view.reported.net} /> }} />
          </p>
        ) : null}
      </div>

      <div className="flex flex-col gap-2 border-t border-dashed border-rule px-4 py-3">
        <p className="flex flex-wrap items-center justify-between gap-2 text-label text-ink-2">
          <span>
            {t("results.reported_on", { date: fmt.date(option.price.reportDate) })}
            {option.price.variety ? <> · <span lang={publishedNameLang(locale)}>{t("ledger.variety", { v: option.price.variety.name })}</span></> : null}
            {option.price.grade ? <> · <span lang={publishedNameLang(locale)}>{t("ledger.grade", { g: option.price.grade.name })}</span></> : null}
          </span>
          <TrendIndicator direction={option.trendDirection} changePercent={option.changePercent7Day} />
        </p>
        <ReasonChips codes={view.reasonCodes} hypothetical={view.hypothetical} />
      </div>

      <nav aria-label={option.market.name} className="flex flex-wrap items-center justify-between gap-1 border-t border-rule px-2 py-1">
        <OptionLink href={links.why}>{t("action.why")}</OptionLink>
        <OptionLink href={links.trend}>{t("action.trend")}</OptionLink>
        <OptionLink href={links.alert}>
          <BellIcon size={16} /> {t("action.set_alert")}
        </OptionLink>
      </nav>
    </article>
  );
}

export function OptionLink({ href, children }: { href: string; children: ReactNode }) {
  return (
    <Link href={href} className="inline-flex min-h-12 items-center gap-1 rounded-control px-3 text-label font-semibold text-net hover:bg-sunken">
      {children}
      <ChevronRightIcon size={14} />
    </Link>
  );
}
