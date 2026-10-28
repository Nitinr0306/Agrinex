"use client";

import { Distance, EstimatedRupees, FreshnessBadge, ReportedPrice } from "@/components/data/price-bits";
import { WarningIcon } from "@/components/icons";
import { Card } from "@/components/ui/feedback";
import { Message } from "@/components/ui/text";
import type { ExcludedMarket, MarketComparisonResponse } from "@/lib/api/types";
import type { TradeOff } from "@/lib/compare/labels";
import type { OptionView, TransportBasis } from "@/lib/compare/view-model";
import { useI18n } from "@/lib/i18n/i18n-provider";
import { publishedNameLang } from "@/lib/i18n/names";

/**
 * The answer strip (borrowing 2 from B, in Hisaab style; ADR-0045 item 4): the recommended market, its estimated
 * net realization with the plain gloss, then distance, report date and freshness, and the link to the basis.
 */
export function AnswerStrip({ top }: { top: OptionView }) {
  const { t, fmt, locale } = useI18n();
  const { option } = top;
  return (
    <section aria-labelledby="answer-title" className="rounded-card border-y-2 border-net bg-raised px-4 py-4 desktop:px-6">
      <h2 id="answer-title" className="text-title font-bold text-ink">
        <Message id="answer.title" values={{ market: <span lang={publishedNameLang(locale)}>{option.market.name}</span> }} />
      </h2>
      <p className="mt-1 text-body text-net">
        <Message
          id="answer.net"
          values={{ net: <span className="figure text-numeric font-semibold">{fmt.estimatedRupees(top.net).replace("₹", "")}</span> }}
        />
      </p>
      <p className="mt-2 flex flex-wrap items-center gap-x-2 gap-y-1 text-label text-ink-2">
        <Distance km={option.distanceKm} approximateLocation={option.approximateMarketLocation} />
        <span aria-hidden="true">·</span>
        <span>{t("results.reported_on", { date: fmt.date(option.price.reportDate) })}</span>
        <FreshnessBadge status={option.price.freshnessStatus} reportDate={option.price.reportDate} />
      </p>
      <a href="#ranking-basis" className="mt-2 inline-flex min-h-12 items-center text-label font-semibold text-net underline underline-offset-4">
        {t("answer.basis_link")}
      </a>
    </section>
  );
}

/**
 * "Why the highest price is not ranked first" (borrowing 3 from C; ADR-0045 item 5). Shown only when
 * `rankingsDisagree`; the sentence never claims R leaves more money unless R's estimate is higher.
 */
export function WinnerCallout({ tradeOff }: { tradeOff: TradeOff }) {
  const { t, fmt, locale } = useI18n();
  const lang = publishedNameLang(locale);
  const P = <strong lang={lang}>{tradeOff.priceWinner.market.name}</strong>;
  const R = <strong lang={lang}>{tradeOff.rankOne.market.name}</strong>;
  const amount = (value: Parameters<typeof fmt.estimatedRupees>[0]) =>
    <span className="figure font-semibold">{fmt.estimatedRupees(value).replace("₹", "")}</span>;
  const price = <span className="figure">{fmt.reportedRupees(tradeOff.priceWinner.price.modalPrice).replace("₹", "")}</span>;
  const n = amount(tradeOff.netDiff.abs());

  return (
    <section aria-labelledby="callout-title" className="rounded-card border border-rule-strong bg-raised px-4 py-4 desktop:px-6">
      <h2 id="callout-title" className="text-heading font-semibold text-ink">{t("callout.title")}</h2>
      <p className="mt-2 text-body">
        {tradeOff.kind === "more" ? (
          <Message id="callout.more" values={{ P, R, price, n }} />
        ) : tradeOff.kind === "equal" ? (
          <Message id="callout.equal" values={{ P, R }} />
        ) : (
          <Message id="callout.not_more" values={{ P, R, n }} />
        )}
      </p>
      {tradeOff.cautions.length > 0 ? (
        <p className="mt-2 flex flex-wrap items-center gap-2 text-label text-stale">
          <WarningIcon size={16} />
          {tradeOff.cautions.map((code) => t(`reason.${code}`)).join(" · ")}
        </p>
      ) : null}
      {tradeOff.showArithmetic ? (
        <p className="mt-3 rounded-control bg-sunken px-3 py-2 text-label">
          <Message
            id={tradeOff.kind === "more" ? "callout.arith_less" : "callout.arith_more"}
            values={{ gross: amount(tradeOff.grossDiff), transport: amount(tradeOff.transportDiff), n, P }}
          />
        </p>
      ) : null}
    </section>
  );
}

/** Markets in range without a recent price (ADR-0023 item 3): never compared, never given an estimate. */
export function ExcludedMarkets({ markets }: { markets: ExcludedMarket[] }) {
  const { t, locale } = useI18n();
  if (markets.length === 0) return null;
  return (
    <Card aria-labelledby="excluded-title" className="bg-sunken px-4 py-3">
      <h2 id="excluded-title" className="eyebrow mb-2">{t("results.excluded_title")}</h2>
      <ul className="divide-y divide-rule">
        {markets.map((excluded) => (
          <li key={excluded.market.id} className="flex flex-wrap items-baseline justify-between gap-2 py-2 text-label">
            <span className="font-semibold" lang={publishedNameLang(locale)}>{excluded.market.name}</span>
            <Distance km={excluded.distanceKm} approximateLocation={excluded.approximateMarketLocation} />
          </li>
        ))}
      </ul>
    </Card>
  );
}

/** Ranking basis, transport label (with the applied rate), and the stale note (role file 02 §7, E03). */
export function ResultsNotes({ response, basis, whatIf }: {
  response: MarketComparisonResponse;
  basis: TransportBasis;
  whatIf: boolean;
}) {
  const { t, fmt } = useI18n();
  const ranking = whatIf || response.decisionBasis.rankingMethod === "NET_REALIZATION" ? t("rank.basis_whatif") : t("rank.basis");
  return (
    <ul id="ranking-basis" tabIndex={-1} className="flex list-disc flex-col gap-1 pl-5 text-label text-ink-2">
      <li>{ranking}</li>
      <li className={whatIf ? "text-hyp" : ""}>
        {t("transport.label", { rate: fmt.decimal(basis.ratePerKm, 2) })}
      </li>
    </ul>
  );
}

/**
 * E03's note "These prices are old…" is shown only when every price on the screen is STALE. When some are, each
 * stale option already carries its Stale badge and STALE_DATA reason (role file 02 E03), and the note would
 * contradict the fresh prices beside it (found in Task 12 on real data: 5 stale of 44, rank 1 fresh). Decided under
 * ADR-0050 (ADR-0052 item 8).
 */
export function StaleNote({ views }: { views: OptionView[] }) {
  const { t } = useI18n();
  if (views.length === 0 || !views.every((view) => view.option.price.freshnessStatus === "STALE")) return null;
  return (
    <p className="flex items-start gap-2 rounded-card border border-stale bg-stale-bg px-4 py-3 text-label text-stale">
      <WarningIcon className="mt-0.5 shrink-0" size={18} />
      {t("state.stale_note")}
    </p>
  );
}

/** The price a ledger figure is based on, for screen readers in the sheet header. */
export function PriceLine({ view }: { view: OptionView }) {
  return <ReportedPrice amount={view.option.price.modalPrice} unit={view.option.price.priceUnit} />;
}

export function EstimateCell({ value, hypothetical, strong }: { value: OptionView["net"]; hypothetical: boolean; strong?: boolean }) {
  return (
    <EstimatedRupees
      value={value}
      className={`${strong ? "text-heading font-semibold" : ""} ${hypothetical ? "hyp-value" : strong ? "text-net" : ""}`}
    />
  );
}

