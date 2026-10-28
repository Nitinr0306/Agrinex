"use client";

import Link from "next/link";
import { BellIcon, ChevronRightIcon, InfoIcon } from "@/components/icons";
import { PageShell } from "@/components/layout/page-shell";
import { FreshnessBadge, ReportedPrice } from "@/components/data/price-bits";
import { OutageBanner, PriceScreenFooter, ProvenancePanel, SourceStatusLine } from "@/components/data/source";
import { LoadProblem } from "@/components/states/load-problem";
import { ButtonLink } from "@/components/ui/button";
import { Card, EmptyState, Skeleton } from "@/components/ui/feedback";
import { marketApi } from "@/lib/api/endpoints";
import { isApiError } from "@/lib/api/errors";
import { useComparison } from "@/lib/compare/comparison-provider";
import { useDelayedFlag, useQuery } from "@/lib/hooks/use-query";
import { useI18n } from "@/lib/i18n/i18n-provider";
import { localizedName, publishedNameLang } from "@/lib/i18n/names";
import { useEffect } from "react";

const UUID = /^[0-9a-fA-F-]{36}$/;

/** S06 market detail: the latest reported price per crop at one market (role file 02 §8 S06). */
export function MarketDetailScreen({ marketId, crop }: { marketId: string; crop: string | null }) {
  const { t, fmt, locale } = useI18n();
  const { noteSourceStatus } = useComparison();
  const valid = UUID.test(marketId) && (crop === null || UUID.test(crop));
  const detail = useQuery(valid ? `market:${marketId}:${crop ?? ""}` : null, (signal) =>
    marketApi.getMarketDetail(marketId, { commodityId: crop ?? undefined }, signal));
  const slow = useDelayedFlag(detail.status === "loading");
  const lang = publishedNameLang(locale);

  useEffect(() => {
    if (detail.status === "success") noteSourceStatus(detail.data.sourceStatus);
  }, [detail.status, detail.data, noteSourceStatus]);

  if (!valid || (detail.status === "error" && (isApiError(detail.error, "NOT_FOUND") || isApiError(detail.error, "VALIDATION_FAILED", "REFERENCE_NOT_FOUND")))) {
    return (
      <PageShell title={t("action.detail")} backHref="/compare">
        <EmptyState
          icon={<InfoIcon size={28} />}
          title={t("error.market_unavailable")}
          actions={<ButtonLink href="/compare">{t("action.compare")}</ButtonLink>}
        />
      </PageShell>
    );
  }

  const data = detail.status === "success" ? detail.data : null;
  return (
    <PageShell title={data ? data.market.name : t("action.detail")} backHref="/compare" showHeading={false} width="medium">
      {detail.status === "error" ? <LoadProblem error={detail.error} onRetry={detail.retry} /> : null}
      {detail.status === "loading" && slow ? (
        <div className="flex flex-col gap-3"><Skeleton className="h-24" /><Skeleton className="h-40" /></div>
      ) : null}
      {data ? (
        <div className="flex flex-col gap-4">
          <OutageBanner status={data.sourceStatus} />
          <Card className="flex flex-col gap-1 px-4 py-4">
            <h1 className="text-title font-bold" lang={lang}>{data.market.name}</h1>
            <p className="text-label text-ink-2">
              <span lang={lang}>{data.market.district.name}</span>
              {" · "}
              <span lang={localizedName(data.market.state, locale).lang}>{localizedName(data.market.state, locale).text}</span>
            </p>
            {data.approximateMarketLocation ? <p className="text-label text-ink-2">{t("distance.location_approximate")}</p> : null}
            {!data.active ? <p className="text-label font-semibold text-stale">{t("market.inactive")}</p> : null}
            <SourceStatusLine status={data.sourceStatus} />
          </Card>

          <section aria-labelledby="latest-title" className="flex flex-col gap-3">
            <h2 id="latest-title" className="eyebrow">{t("market.latest_title")}</h2>
            {data.latestPrices.length === 0 ? (
              <EmptyState
                icon={<InfoIcon size={28} />}
                title={crop ? t("market.no_recent") : t("market.no_recent_any")}
                actions={crop ? <ButtonLink href={`/markets/${encodeURIComponent(marketId)}`} variant="secondary">{t("market.all_crops")}</ButtonLink> : null}
              />
            ) : (
              data.latestPrices.map(({ commodity, price }) => {
                const name = localizedName(commodity, locale);
                const filter = new URLSearchParams({ crop: commodity.id });
                if (price.variety) filter.set("variety", price.variety.id);
                if (price.grade) filter.set("grade", price.grade.id);
                return (
                  <Card key={`${commodity.id}:${price.observationId}`} as="article" className="flex flex-col gap-3 px-4 py-4">
                    <div className="flex items-start justify-between gap-3">
                      <h3 className="text-heading font-semibold" lang={name.lang}>{name.text}</h3>
                      <FreshnessBadge status={price.freshnessStatus} reportDate={price.reportDate} />
                    </div>
                    <p className="flex flex-wrap items-baseline gap-2">
                      <span className="text-label text-ink-2">{t("price.latest")}</span>
                      <span className="text-heading font-semibold"><ReportedPrice amount={price.modalPrice} unit={price.priceUnit} /></span>
                    </p>
                    <p className="text-label text-ink-2">{t("results.reported_on", { date: fmt.date(price.reportDate) })}</p>
                    <ProvenancePanel price={price} />
                    <div className="flex flex-wrap gap-1">
                      <Link href={`/markets/${encodeURIComponent(marketId)}/trend?${filter.toString()}`} className="inline-flex min-h-12 items-center gap-1 px-2 text-label font-semibold text-net">
                        {t("action.trend")} <ChevronRightIcon size={14} />
                      </Link>
                      <Link href={`/alerts/new?crop=${encodeURIComponent(commodity.id)}&market=${encodeURIComponent(marketId)}`} className="inline-flex min-h-12 items-center gap-1 px-2 text-label font-semibold text-net">
                        <BellIcon size={16} /> {t("action.set_alert")}
                      </Link>
                    </div>
                  </Card>
                );
              })
            )}
          </section>
          <PriceScreenFooter attribution={data.latestPrices[0]?.price.attribution} />
        </div>
      ) : null}
    </PageShell>
  );
}
