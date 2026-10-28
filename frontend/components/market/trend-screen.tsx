"use client";

import dynamic from "next/dynamic";
import { useEffect, useState } from "react";
import { BellIcon, InfoIcon } from "@/components/icons";
import { PageShell } from "@/components/layout/page-shell";
import { FreshnessBadge, ReportedPrice, TrendIndicator } from "@/components/data/price-bits";
import { OutageBanner, PriceScreenFooter, SourceStatusLine, datasetLabelKey } from "@/components/data/source";
import { LoadProblem } from "@/components/states/load-problem";
import { Button, ButtonLink } from "@/components/ui/button";
import { Segmented } from "@/components/ui/choice";
import { Card, EmptyState, Skeleton } from "@/components/ui/feedback";
import { Message } from "@/components/ui/text";
import { marketApi } from "@/lib/api/endpoints";
import { isApiError } from "@/lib/api/errors";
import { RUPEES_PER_QUINTAL, type TrendResponse } from "@/lib/api/types";
import { useComparison } from "@/lib/compare/comparison-provider";
import { useDelayedFlag, useQuery } from "@/lib/hooks/use-query";
import { useI18n } from "@/lib/i18n/i18n-provider";
import { localizedName, publishedNameLang } from "@/lib/i18n/names";
import { windowAverages } from "@/lib/trend/averages";
import { ForecastSection } from "./forecast-section";

// Recharts is loaded only on this screen, after the page is interactive.
const TrendChart = dynamic(() => import("./trend-chart").then((module) => module.TrendChart), {
  ssr: false,
  loading: () => <Skeleton className="h-56" />,
});

const UUID = /^[0-9a-fA-F-]{36}$/;
const DIARY_PAGE = 10;

/** S07 historical trend (role file 02 §8 S07; spec §33; ADR-0025). */
export function TrendScreen({ marketId, crop, variety, grade, days: initialDays }: {
  marketId: string;
  crop: string | null;
  variety: string | null;
  grade: string | null;
  days: 7 | 30;
}) {
  const { t, locale } = useI18n();
  const { noteSourceStatus } = useComparison();
  const [days, setDays] = useState<7 | 30>(initialDays);
  const valid = UUID.test(marketId) && crop !== null && UUID.test(crop);
  const trend = useQuery(valid ? `trend:${marketId}:${crop}:${variety}:${grade}:${days}` : null, (signal) =>
    marketApi.getMarketTrend(marketId, { commodityId: crop as string, varietyId: variety, gradeId: grade, days }, signal));
  const slow = useDelayedFlag(trend.status === "loading");

  useEffect(() => {
    if (trend.status === "success") noteSourceStatus(trend.data.sourceStatus);
  }, [trend.status, trend.data, noteSourceStatus]);

  if (!valid) {
    return (
      <PageShell title={t("action.trend")} backHref="/compare">
        <EmptyState icon={<InfoIcon size={28} />} title={t("trend.missing_crop")} actions={<ButtonLink href="/compare">{t("action.compare")}</ButtonLink>} />
      </PageShell>
    );
  }
  if (trend.status === "error" && isApiError(trend.error, "NOT_FOUND", "REFERENCE_NOT_FOUND", "VALIDATION_FAILED")) {
    return (
      <PageShell title={t("action.trend")} backHref="/compare">
        <EmptyState icon={<InfoIcon size={28} />} title={t("error.market_unavailable")} actions={<ButtonLink href="/compare">{t("action.compare")}</ButtonLink>} />
      </PageShell>
    );
  }

  const data = trend.data;
  const lang = publishedNameLang(locale);
  return (
    <PageShell title={t("action.trend")} backHref="/compare" width="medium">
      <div className="flex flex-col gap-4">
        {data ? <OutageBanner status={data.sourceStatus} /> : null}
        {data ? (
          <Card className="px-4 py-4">
            <h2 className="text-title font-bold">
              <span lang={lang}>{data.market.name}</span> · <span lang={localizedName(data.commodity, locale).lang}>{localizedName(data.commodity, locale).text}</span>
            </h2>
            <p className="text-label text-ink-2">
              {data.priceUnit === RUPEES_PER_QUINTAL || data.priceUnit === null
                ? t("trend.prices_per")
                : <Message id="trend.unit_note" values={{ unit: <span lang="en">{data.priceUnit}</span> }} />}
              {data.variety ? <> · <span lang={lang}>{t("ledger.variety", { v: data.variety.name })}</span></> : null}
              {data.grade ? <> · <span lang={lang}>{t("ledger.grade", { g: data.grade.name })}</span></> : null}
            </p>
          </Card>
        ) : null}

        <Segmented<7 | 30>
          label={t("trend.period")}
          value={days}
          onChange={setDays}
          options={[
            { value: 7, label: t("trend.days", { n: 7 }) },
            { value: 30, label: t("trend.days", { n: 30 }) },
          ]}
        />

        {trend.status === "error" ? <LoadProblem error={trend.error} onRetry={trend.retry} /> : null}
        {trend.status === "loading" && slow && !data ? <Skeleton className="h-72" /> : null}
        {data ? <TrendBody trend={data} /> : null}

        {data ? (
          <ButtonLink
            href={`/alerts/new?crop=${encodeURIComponent(data.commodity.id)}&market=${encodeURIComponent(data.market.id)}`}
            variant="secondary"
            block
          >
            <BellIcon size={18} />
            <Message id="trend.set_alert" values={{ market: <span lang={lang}>{data.market.name}</span> }} />
          </ButtonLink>
        ) : null}

        {crop ? <ForecastSection marketId={marketId} commodityId={crop} /> : null}

        {data ? <PriceScreenFooter attribution={data.observations[0]?.attribution} /> : null}
      </div>
    </PageShell>
  );
}

function TrendBody({ trend }: { trend: TrendResponse }) {
  const { t, fmt } = useI18n();
  const averages = windowAverages(trend);
  const insufficient = trend.trendDirection === "INSUFFICIENT_DATA";
  return (
    <>
      <Card className="flex flex-col gap-2 px-4 py-4">
        <TrendIndicator direction={trend.trendDirection} changePercent={trend.changePercent7Day} />
        {insufficient ? <p className="text-body font-medium">{t("state.insufficient_history")}</p> : null}
        {averages ? (
          <p className="text-label text-ink-2">
            <Message
              id="trend.averages"
              values={{
                recent: <span className="figure">{fmt.rupees2(averages.recent)}</span>,
                baseline: <span className="figure">{fmt.rupees2(averages.baseline)}</span>,
              }}
            />
          </p>
        ) : null}
        <SourceStatusLine status={trend.sourceStatus} />
      </Card>

      {!insufficient && trend.observations.length > 0 ? (
        <Card className="px-3 py-3"><TrendChart trend={trend} /></Card>
      ) : null}

      <PriceDiary trend={trend} />
    </>
  );
}

/** The dated list behind the chart: its text equivalent (role file 02 S07, §12; PDD-09). */
function PriceDiary({ trend }: { trend: TrendResponse }) {
  const { t, fmt, locale } = useI18n();
  const [shown, setShown] = useState(DIARY_PAGE);
  const rows = [...trend.observations].sort((a, b) => (a.reportDate < b.reportDate ? 1 : a.reportDate > b.reportDate ? -1 : 0));
  const lang = publishedNameLang(locale);
  if (rows.length === 0) {
    return <p className="rounded-card border border-rule bg-raised px-4 py-3 text-label text-ink-2">{t("trend.no_reports")}</p>;
  }
  return (
    <Card aria-labelledby="diary-title" className="overflow-hidden">
      <div className="flex items-baseline justify-between gap-2 border-b border-rule bg-sunken px-4 py-2">
        <h2 id="diary-title" className="eyebrow">{t("trend.diary")}</h2>
        <span className="text-caption text-ink-2">{t("trend.entries", { n: rows.length })}</span>
      </div>
      {/* Three columns keep the diary inside 375 px: variety and dataset share the last one (role file 02 §13). */}
      <table className="w-full table-fixed text-label">
        <thead>
          <tr className="border-b border-rule text-left text-caption text-ink-2">
            <th scope="col" className="w-[34%] px-4 py-2 font-semibold">{t("trend.col_date")}</th>
            <th scope="col" className="w-[28%] px-2 py-2 text-right font-semibold">{t("trend.col_price")}</th>
            <th scope="col" className="px-4 py-2 text-right font-semibold">{t("input.variety")} · {t("provenance.dataset")}</th>
          </tr>
        </thead>
        <tbody>
          {rows.slice(0, shown).map((point, index) => {
            const datasetKey = datasetLabelKey(point.sourceDataset);
            return (
              <tr key={point.observationId} className="border-b border-rule align-top last:border-b-0">
                <th scope="row" className="px-4 py-2 text-left font-medium">{fmt.date(point.reportDate)}</th>
                <td className="figure px-2 py-2 text-right font-semibold">
                  {point.priceUnit === RUPEES_PER_QUINTAL
                    ? fmt.reportedRupees(point.modalPrice)
                    : <ReportedPrice amount={point.modalPrice} unit={point.priceUnit} />}
                </td>
                <td className="px-4 py-2 text-right">
                  <span className="block" lang={lang}>{point.variety?.name ?? "—"}</span>
                  {index === 0
                    ? <FreshnessBadge status={point.freshnessStatus} reportDate={point.reportDate} />
                    : <span className="text-caption text-ink-2">{datasetKey ? t(datasetKey) : <span lang="en">{point.sourceDataset}</span>}</span>}
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
      {trend.variety === null ? <p className="border-t border-rule px-4 py-2 text-caption text-ink-2">{t("trend.mixed_variety")}</p> : null}
      {shown < rows.length ? (
        <div className="border-t border-rule p-2">
          <Button variant="tertiary" block onClick={() => setShown((value) => value + DIARY_PAGE)}>{t("trend.show_older")}</Button>
        </div>
      ) : null}
    </Card>
  );
}
