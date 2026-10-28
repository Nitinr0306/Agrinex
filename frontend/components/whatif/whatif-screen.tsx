"use client";

import { useRouter } from "next/navigation";
import { useMemo, useState } from "react";
import { LedgerCard } from "@/components/compare/ledger";
import { ComparisonSheet } from "@/components/compare/sheet";
import { ExcludedMarkets } from "@/components/compare/summary";
import { DocumentIcon, InfoIcon, LightbulbIcon, RefreshIcon } from "@/components/icons";
import { PageShell } from "@/components/layout/page-shell";
import { PriceScreenFooter } from "@/components/data/source";
import { LoadProblem } from "@/components/states/load-problem";
import { Button, ButtonLink } from "@/components/ui/button";
import { Card, EmptyState } from "@/components/ui/feedback";
import { Message } from "@/components/ui/text";
import { sellingOptionsApi } from "@/lib/api/endpoints";
import type { MarketComparisonResponse } from "@/lib/api/types";
import { useComparison } from "@/lib/compare/comparison-provider";
import { basisFromWhatIf, optionsFromWhatIf } from "@/lib/compare/view-model";
import {
  NO_OVERRIDES,
  WHAT_IF_BOUNDS,
  buildSimulateRequest,
  originalInputs,
  recalculate,
  type WhatIfInputs,
  type WhatIfOverrides,
} from "@/lib/compare/whatif";
import { Decimal } from "@/lib/decimal";
import { latestOnly } from "@/lib/hooks/latest-only";
import { useIsDesktop } from "@/lib/hooks/use-media";
import { useI18n } from "@/lib/i18n/i18n-provider";
import { publishedNameLang } from "@/lib/i18n/names";
import { AssumptionSlider } from "./assumption-slider";


/** S09 What-If simulator (role file 02 §8 S09, §16; spec §17; ADR-0026). */
export function WhatIfScreen() {
  const { t } = useI18n();
  const { result } = useComparison();
  if (!result || result.response.options.length === 0) {
    return (
      <PageShell title={t("whatif.title")} backHref="/compare">
        <EmptyState
          icon={<InfoIcon size={28} />}
          title={t("whatif.no_base")}
          actions={<ButtonLink href="/compare">{t("action.compare")}</ButtonLink>}
        />
      </PageShell>
    );
  }
  return <Simulator comparison={result.response} commodityId={result.context.commodity.id} />;
}

function Simulator({ comparison, commodityId }: { comparison: MarketComparisonResponse; commodityId: string }) {
  const { t, fmt, locale } = useI18n();
  const router = useRouter();
  const isDesktop = useIsDesktop();
  const { applied, setApplied } = useComparison();
  const reported = useMemo(() => originalInputs(comparison), [comparison]);

  // An applied What-If resumes where the user left it (PDD-11).
  const [overrides, setOverrides] = useState<WhatIfOverrides>(() => applied
    ? {
      ratePerKm: applied.inputs.ratePerKm.eq(reported.ratePerKm) ? null : applied.inputs.ratePerKm,
      quantityKg: applied.inputs.quantityKg.eq(reported.quantityKg) ? null : applied.inputs.quantityKg,
      maxDistanceKm: applied.inputs.maxDistanceKm === reported.maxDistanceKm ? null : applied.inputs.maxDistanceKm,
    }
    : NO_OVERRIDES);
  const [serverBase, setServerBase] = useState<MarketComparisonResponse | null>(
    applied && applied.base !== comparison ? applied.base : null,
  );
  const [checking, setChecking] = useState(false);
  const [serverError, setServerError] = useState<unknown>(null);
  // Each slider release can start a check; only the newest answer may update the screen.
  const [checks] = useState(() => latestOnly<number>());

  const base = serverBase ?? comparison;
  const inputs = useMemo<WhatIfInputs>(() => ({
    ratePerKm: overrides.ratePerKm ?? reported.ratePerKm,
    quantityKg: overrides.quantityKg ?? reported.quantityKg,
    maxDistanceKm: overrides.maxDistanceKm ?? reported.maxDistanceKm,
  }), [overrides, reported]);
  const outcome = useMemo(() => recalculate(base, inputs), [base, inputs]);
  const views = optionsFromWhatIf(outcome, base);
  const basis = basisFromWhatIf(base, inputs);
  const maxTransport = views.reduce((max, view) => Decimal.max(max, view.transport), Decimal.ZERO);
  const lang = publishedNameLang(locale);

  /** Server mode: only when the radius grows beyond the comparison's (ADR-0026 item 3), on release. */
  const commitDistance = async (km: number) => {
    if (km <= base.maxDistanceKm) return;
    const check = checks.start(km);
    if (check === null) return;
    setChecking(true);
    setServerError(null);
    try {
      const response = await sellingOptionsApi.simulate(buildSimulateRequest(comparison, overrides, km));
      if (check.isLatest()) setServerBase(response);
    } catch (error) {
      if (check.isLatest()) setServerError(error);
    } finally {
      check.done();
      if (check.isLatest()) setChecking(false);
    }
  };

  const reset = () => {
    checks.cancel();
    setChecking(false);
    setOverrides(NO_OVERRIDES);
    setServerBase(null);
    setServerError(null);
    setApplied(null);
  };

  const useNumbers = () => {
    setApplied({ inputs, base });
    router.push("/compare");
  };

  const quantityInRange = reported.quantityKg.gte(Decimal.ofInteger(WHAT_IF_BOUNDS.quantityKg.min))
    && reported.quantityKg.lte(Decimal.ofInteger(WHAT_IF_BOUNDS.quantityKg.max));
  const rateText = (rate: Decimal) => t("whatif.rate_value", { rate: fmt.decimal(rate, 2) });
  const kgText = (kg: Decimal) => t("whatif.qty_value", { q: fmt.decimal(kg, 3) });
  const kmText = (km: number) => t("input.km_choice", { n: fmt.integer(km) });

  return (
    <PageShell title={t("whatif.title")} backHref="/compare" width="wide">
      <div className="flex flex-col gap-4 rounded-card border-2 border-dashed border-hyp p-3 tablet:p-4">
        <div className="hyp-frame flex flex-col gap-2 rounded-card px-4 py-3 text-hyp" role="note">
          <span className="eyebrow w-fit rounded-control border-2 border-dashed border-hyp px-2 py-0.5 text-hyp">{t("whatif.stamp")}</span>
          <p className="text-label font-medium">{t("whatif.banner")}</p>
        </div>

        <div className="grid gap-4 desktop:grid-cols-[22rem_minmax(0,1fr)] desktop:items-start">
          <Card aria-labelledby="assumptions-title" className="flex flex-col gap-5 p-4">
            <h2 id="assumptions-title" className="eyebrow border-b border-rule pb-2">{t("whatif.assumptions")}</h2>
            <AssumptionSlider
              label={t("whatif.rate")}
              min={WHAT_IF_BOUNDS.ratePerKm.min}
              max={WHAT_IF_BOUNDS.ratePerKm.max}
              step={0.25}
              value={overrides.ratePerKm?.toNumber() ?? null}
              original={reported.ratePerKm.toNumber()}
              format={(value) => rateText(Decimal.of(value))}
              originalNote={overrides.ratePerKm
                ? t("whatif.was_configured", { rate: fmt.decimal(Decimal.of(comparison.transportAssumptions.ratePerKm), 2) })
                : t("whatif.unchanged")}
              onChange={(value) => setOverrides((current) => ({ ...current, ratePerKm: Decimal.of(value) }))}
            />
            <AssumptionSlider
              label={t("input.quantity")}
              min={WHAT_IF_BOUNDS.quantityKg.min}
              max={WHAT_IF_BOUNDS.quantityKg.max}
              step={10}
              value={overrides.quantityKg?.toNumber() ?? null}
              original={quantityInRange ? reported.quantityKg.toNumber() : null}
              format={(value) => kgText(Decimal.of(value))}
              originalNote={!quantityInRange && !overrides.quantityKg
                ? t("whatif.out_of_range", { qty: fmt.decimal(reported.quantityKg, 3) })
                : overrides.quantityKg
                  ? t("whatif.was_value", { value: kgText(reported.quantityKg) })
                  : t("whatif.unchanged")}
              onChange={(value) => setOverrides((current) => ({ ...current, quantityKg: Decimal.of(value) }))}
            />
            <AssumptionSlider
              label={t("input.distance")}
              min={WHAT_IF_BOUNDS.maxDistanceKm.min}
              max={WHAT_IF_BOUNDS.maxDistanceKm.max}
              step={1}
              value={overrides.maxDistanceKm}
              original={reported.maxDistanceKm >= WHAT_IF_BOUNDS.maxDistanceKm.min && reported.maxDistanceKm <= WHAT_IF_BOUNDS.maxDistanceKm.max ? reported.maxDistanceKm : null}
              format={kmText}
              originalNote={overrides.maxDistanceKm ? t("whatif.was_value", { value: kmText(reported.maxDistanceKm) }) : t("whatif.unchanged")}
              onChange={(value) => setOverrides((current) => ({ ...current, maxDistanceKm: value }))}
              onCommit={(value) => void commitDistance(value)}
            />
          </Card>

          <section aria-labelledby="whatif-results" aria-busy={checking} className="flex flex-col gap-3">
            <h2 id="whatif-results" className="eyebrow">{t("whatif.results_title", { n: views.length })}</h2>
            {checking ? (
              <p className="flex items-center gap-2 rounded-card border border-hyp bg-hyp-surface px-4 py-3 text-label text-hyp" role="status">
                <RefreshIcon size={18} className="animate-spin motion-reduce:animate-none" />
                {t("whatif.checking")}
              </p>
            ) : null}
            {serverBase && !checking ? (
              <p className="text-caption text-hyp">{t("results.prices_rechecked", { time: fmt.dateTime(serverBase.generatedAt) })}</p>
            ) : null}
            {serverError ? <LoadProblem error={serverError} onRetry={() => void commitDistance(inputs.maxDistanceKm)} /> : null}

            {outcome.state !== "ok" ? (
              <EmptyState
                icon={<DocumentIcon size={28} />}
                title={outcome.state === "noRecentData" ? t("state.no_recent") : t("state.no_market")}
              />
            ) : (
              <>
                <p className="text-caption text-ink-2">
                  {t("transport.bar_caption", { max: fmt.estimatedRupees(maxTransport).replace("₹", "") })}
                </p>
                {isDesktop ? (
                  <ComparisonSheet views={views} basis={basis} maxTransport={maxTransport} commodityId={commodityId} />
                ) : (
                  views.map((view, index) => (
                    <LedgerCard
                      key={view.option.market.id}
                      view={view}
                      basis={basis}
                      maxTransport={maxTransport}
                      commodityId={commodityId}
                      showGloss={index === 0}
                    />
                  ))
                )}
              </>
            )}

            {outcome.breakeven ? (
              <p className="flex items-start gap-3 rounded-card border border-hyp bg-hyp-surface px-4 py-3 text-label text-ink">
                <LightbulbIcon className="mt-0.5 shrink-0 text-hyp" />
                <span>
                  <strong className="text-hyp">{t("whatif.breakeven_label")}: </strong>
                  <Message
                    id="whatif.breakeven"
                    values={{
                      rate: <span className="figure">{fmt.decimal(outcome.breakeven.ratePerKm, 2)}</span>,
                      P: <strong lang={lang}>{outcome.breakeven.priceWinner.market.name}</strong>,
                      R: <strong lang={lang}>{outcome.breakeven.rankOne.market.name}</strong>,
                    }}
                  />
                </span>
              </p>
            ) : null}
            <ExcludedMarkets markets={outcome.excluded} />
          </section>
        </div>

        <ul className="flex list-disc flex-col gap-1 pl-5 text-label text-ink-2">
          <li>{t("rank.basis_whatif")}</li>
          <li className="text-hyp">{t("transport.label", { rate: fmt.decimal(inputs.ratePerKm, 2) })}</li>
        </ul>

        <div className="grid gap-2 tablet:grid-cols-2">
          <Button onClick={useNumbers} disabled={outcome.state !== "ok" || checking}>{t("whatif.use")}</Button>
          <Button variant="secondary" onClick={reset}>{t("whatif.reset")}</Button>
        </div>
      </div>
      <PriceScreenFooter attribution={views[0]?.option.price.attribution} />
    </PageShell>
  );
}
