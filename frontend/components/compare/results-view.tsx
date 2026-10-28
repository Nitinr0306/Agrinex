"use client";

import { useMemo } from "react";
import { DocumentIcon, SlidersIcon } from "@/components/icons";
import { OutageBanner, PriceScreenFooter, SourceStatusLine } from "@/components/data/source";
import { Distance } from "@/components/data/price-bits";
import { Button, ButtonLink } from "@/components/ui/button";
import { EmptyState } from "@/components/ui/feedback";
import type { ExcludedMarket, MarketComparisonResponse } from "@/lib/api/types";
import type { AppliedWhatIf, ComparisonContext } from "@/lib/compare/comparison-provider";
import { tradeOffOf } from "@/lib/compare/labels";
import {
  basisFromResponse,
  basisFromWhatIf,
  optionsFromResponse,
  optionsFromWhatIf,
  type OptionView,
} from "@/lib/compare/view-model";
import { recalculate } from "@/lib/compare/whatif";
import { Decimal } from "@/lib/decimal";
import { useIsDesktop } from "@/lib/hooks/use-media";
import { useI18n } from "@/lib/i18n/i18n-provider";
import { localizedName, publishedNameLang } from "@/lib/i18n/names";
import { LedgerCard } from "./ledger";
import { ComparisonSheet } from "./sheet";
import { AnswerStrip, ExcludedMarkets, ResultsNotes, StaleNote, WinnerCallout } from "./summary";

interface ResultsViewProps {
  response: MarketComparisonResponse;
  context: ComparisonContext;
  applied: AppliedWhatIf | null;
  onEdit: () => void;
  onResetWhatIf: () => void;
  /** Re-runs the comparison with a larger radius (E04 "Try a larger distance"). */
  onWiden: () => void;
  onChooseCrop: () => void;
}

/** S04 comparison results (role file 02 §8 S04), reported or with an applied What-If (PDD-11). */
export function ResultsView({ response, context, applied, onEdit, onResetWhatIf, onWiden, onChooseCrop }: ResultsViewProps) {
  const { t, fmt, locale } = useI18n();
  const isDesktop = useIsDesktop();

  const { views, basis, excluded, whatIfState } = useMemo(() => {
    if (applied) {
      const result = recalculate(applied.base, applied.inputs);
      return {
        views: optionsFromWhatIf(result, applied.base),
        basis: basisFromWhatIf(applied.base, applied.inputs),
        excluded: result.excluded,
        whatIfState: result.state,
      };
    }
    return {
      views: optionsFromResponse(response),
      basis: basisFromResponse(response),
      excluded: response.excludedMarkets,
      whatIfState: "ok" as const,
    };
  }, [applied, response]);

  const maxTransport = views.reduce((max, view) => Decimal.max(max, view.transport), Decimal.ZERO);
  const hypothetical = applied !== null || response.scenario.kind === "SIMULATION";
  const tradeOff = hypothetical ? null : tradeOffOf(response);
  const crop = localizedName(context.commodity, locale);
  const quantityText = `${fmt.decimal(basis.quantityKg, 3)} ${t("unit.kg")}`;

  return (
    <div className="flex flex-col gap-4">
      <OutageBanner status={response.sourceStatus} />

      <div className="flex flex-col gap-2 rounded-card border border-rule bg-raised px-4 py-3">
        <div className="flex items-start justify-between gap-3">
          <p className="text-body font-medium">
            {t("results.summary", {
              crop: crop.text,
              qty: quantityText,
              district: context.district.name,
              km: fmt.integer(applied ? applied.inputs.maxDistanceKm : response.maxDistanceKm),
            })}
          </p>
          <Button variant="tertiary" onClick={onEdit} className="!min-h-10 shrink-0 !px-2">{t("input.change")}</Button>
        </div>
        <SourceStatusLine status={response.sourceStatus} />
      </div>

      {hypothetical ? (
        <div className="hyp-frame flex flex-col gap-2 rounded-card px-4 py-3 text-hyp" role="status">
          <p className="flex flex-wrap items-center justify-between gap-2 font-semibold">
            <span className="eyebrow rounded-control border-2 border-dashed border-hyp px-2 py-0.5 text-hyp">{t("whatif.stamp")}</span>
            {applied ? (
              <span className="flex items-center gap-2 text-label">
                {t("whatif.applied")} ·
                <Button variant="tertiary" onClick={onResetWhatIf} className="!min-h-10 !px-2 !text-hyp">{t("whatif.reset_short")}</Button>
              </span>
            ) : null}
          </p>
          <p className="text-label">{t("whatif.banner")}</p>
        </div>
      ) : null}

      {views.length === 0 ? (
        <NoRecentData
          neverSynced={response.sourceStatus.lastSuccessfulSyncAt === null && !response.sourceStatus.latestSyncFailed}
          excluded={excluded}
          noMarket={whatIfState === "noMarketInRange"}
          onWiden={onWiden}
          onChooseCrop={onChooseCrop}
        />
      ) : (
        <>
          {!hypothetical ? <AnswerStrip top={views[0]} /> : null}
          {tradeOff ? <WinnerCallout tradeOff={tradeOff} /> : null}
          <StaleNote views={views} />
          <section aria-labelledby="markets-heading" className="flex flex-col gap-3">
            <h2 id="markets-heading" className="sr-only">{t("results.markets_heading")}</h2>
            <p className="text-caption text-ink-2">
              {t("transport.bar_caption", { max: fmt.estimatedRupees(maxTransport).replace("₹", "") })}
            </p>
            {isDesktop ? (
              <ComparisonSheet views={views} basis={basis} maxTransport={maxTransport} commodityId={context.commodity.id} />
            ) : (
              <div className="flex flex-col gap-3">
                {views.map((view: OptionView, index) => (
                  <LedgerCard
                    key={view.option.market.id}
                    view={view}
                    basis={basis}
                    maxTransport={maxTransport}
                    commodityId={context.commodity.id}
                    showGloss={hypothetical && index === 0}
                  />
                ))}
              </div>
            )}
          </section>
        </>
      )}

      {views.length > 0 ? <ExcludedMarkets markets={excluded} /> : null}

      {views.length > 0 && !applied ? (
        <ButtonLink href="/compare/what-if" variant="hypothetical" block>
          <SlidersIcon size={18} />
          {t("results.whatif_cta")}
        </ButtonLink>
      ) : null}

      <PriceScreenFooter attribution={views[0]?.option.price.attribution}>
        {views.length > 0 ? <ResultsNotes response={response} basis={basis} whatIf={hypothetical} /> : null}
      </PriceScreenFooter>
    </div>
  );
}

/** E04 (no recent observation), E23 (never synchronized) and, in What-If, the no-market state (role file 02 §9). */
function NoRecentData({ neverSynced, excluded, noMarket, onWiden, onChooseCrop }: {
  neverSynced: boolean;
  excluded: ExcludedMarket[];
  noMarket: boolean;
  onWiden: () => void;
  onChooseCrop: () => void;
}) {
  const { t, locale } = useI18n();
  return (
    <EmptyState
      icon={<DocumentIcon size={28} />}
      title={neverSynced ? t("state.never_synced") : noMarket ? t("state.no_market") : t("state.no_recent")}
      actions={
        <>
          <Button variant="secondary" onClick={onChooseCrop}>{t("results.choose_other_crop")}</Button>
          <Button onClick={onWiden}>{t("results.try_larger")}</Button>
        </>
      }
    >
      {excluded.length > 0 ? (
        <ul className="mt-2 flex flex-col gap-1 text-left text-label">
          {excluded.map((market) => (
            <li key={market.market.id} className="flex justify-between gap-3">
              <span lang={publishedNameLang(locale)}>{market.market.name}</span>
              <Distance km={market.distanceKm} approximateLocation={market.approximateMarketLocation} />
            </li>
          ))}
        </ul>
      ) : null}
    </EmptyState>
  );
}
