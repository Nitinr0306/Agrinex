"use client";

import type { ReactNode } from "react";
import { BellIcon } from "@/components/icons";
import { Distance, FreshnessBadge, ReasonChips, ReportedPrice, TrendIndicator } from "@/components/data/price-bits";
import type { OptionView, TransportBasis } from "@/lib/compare/view-model";
import type { Decimal } from "@/lib/decimal";
import { useI18n } from "@/lib/i18n/i18n-provider";
import { publishedNameLang } from "@/lib/i18n/names";
import { OptionBadges, OptionLink, TransportBar, TransportWorking, optionLinks } from "./ledger";
import { EstimateCell } from "./summary";

/**
 * The desktop comparison sheet (approved draft d02, layout): markets as columns in API rank order, one row per
 * ledger line. A real table with row and column headers, so screen readers can read it cell by cell.
 */
export function ComparisonSheet({ views, basis, maxTransport, commodityId }: {
  views: OptionView[];
  basis: TransportBasis;
  maxTransport: Decimal;
  commodityId: string;
}) {
  const { t, fmt, locale } = useI18n();
  const lang = publishedNameLang(locale);
  const quantity = fmt.decimal(basis.quantityInQuintal, 5);
  const hypothetical = views.some((view) => view.hypothetical);

  const row = (label: ReactNode, cell: (view: OptionView) => ReactNode, emphasis = false) => (
    <tr className={emphasis ? "double-rule bg-fresh-bg/40" : "border-t border-rule"}>
      <th scope="row" className={`px-4 py-3 text-left align-top text-label ${emphasis ? "font-semibold text-net" : "font-medium text-ink"}`}>
        {label}
      </th>
      {views.map((view) => (
        <td key={view.option.market.id} className={`px-4 py-3 text-right align-top text-label ${view.labels.recommended ? "bg-fresh-bg/30" : ""}`}>
          {cell(view)}
        </td>
      ))}
    </tr>
  );

  return (
    <div className={`relative overflow-x-auto rounded-card bg-raised ${hypothetical ? "border-2 border-dashed border-hyp" : "border border-rule"}`}>
      <table className="w-full min-w-[40rem] border-collapse">
        <caption className="sr-only">{t("results.sheet_caption")}</caption>
        <thead>
          <tr>
            {/* The corner heads the column of row headers: a text cell without a header fails WCAG 1.3.1 (axe
                td-has-header, found in Task 12 on the real results page). */}
            <th scope="col" className="eyebrow w-56 px-4 py-3 text-left align-bottom">{t("results.sheet_caption")}</th>
            {views.map((view) => (
              <th
                key={view.option.market.id}
                scope="col"
                className={`px-4 py-3 text-right align-bottom ${view.labels.recommended ? "border-t-4 border-net bg-fresh-bg/30" : "border-t-4 border-transparent"}`}
              >
                <div className="flex flex-col items-end gap-2">
                  <OptionBadges labels={view.labels} />
                  <span className="text-heading font-semibold text-ink" lang={lang}>
                    <span className="sr-only">{t("results.rank", { n: view.rank })}: </span>
                    {view.option.market.name}
                  </span>
                </div>
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {row(t("results.row_distance"), (view) => (
            <Distance km={view.option.distanceKm} approximateLocation={view.option.approximateMarketLocation} />
          ))}
          {row(t("price.latest"), (view) => (
            <ReportedPrice amount={view.option.price.modalPrice} unit={view.option.price.priceUnit} />
          ))}
          {row(t("provenance.report_date"), (view) => (
            <span className="flex flex-wrap items-center justify-end gap-2">
              {fmt.date(view.option.price.reportDate)}
              <FreshnessBadge status={view.option.price.freshnessStatus} reportDate={view.option.price.reportDate} />
            </span>
          ))}
          {row(t("results.row_variety"), (view) => (
            <span lang={lang}>
              {[view.option.price.variety?.name, view.option.price.grade?.name].filter(Boolean).join(" · ") || "—"}
            </span>
          ))}
          {row(
            <span>
              {t("ledger.gross")}
              <span className="block text-caption font-normal text-ink-2 figure">× {quantity} {t("unit.quintal")}</span>
            </span>,
            (view) => <EstimateCell value={view.gross} hypothetical={view.hypothetical} />,
          )}
          {row(t("ledger.transport"), (view) => (
            <span className="flex flex-col items-end gap-1.5">
              <EstimateCell value={view.transport} hypothetical={view.hypothetical} />
              <span className="text-caption text-ink-2"><TransportWorking view={view} basis={basis} /></span>
              <span className="w-28">
                <TransportBar value={view.transport} max={maxTransport} reported={view.reported?.transport ?? null} />
              </span>
            </span>
          ))}
          {row(
            <span>
              {t("home.sum_net")}
              <span className="block text-caption font-normal">({t("net.gloss")})</span>
            </span>,
            (view) => <EstimateCell value={view.net} hypothetical={view.hypothetical} strong />,
            true,
          )}
          {row(t("results.row_trend"), (view) => (
            <TrendIndicator direction={view.option.trendDirection} changePercent={view.option.changePercent7Day} compact />
          ))}
          {row(t("reason.list"), (view) => (
            <span className="flex justify-end">
              <ReasonChips codes={view.reasonCodes} hypothetical={view.hypothetical} />
            </span>
          ))}
          {row(t("results.row_explore"), (view) => {
            const links = optionLinks(view, commodityId);
            return (
              <span className="flex flex-wrap justify-end">
                <OptionLink href={links.why}>{t("action.why")}</OptionLink>
                <OptionLink href={links.trend}>{t("action.trend")}</OptionLink>
                <OptionLink href={links.alert}><BellIcon size={16} /> {t("action.set_alert")}</OptionLink>
              </span>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
