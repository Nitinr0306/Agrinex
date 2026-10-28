"use client";

import { useSearchParams } from "next/navigation";
import type { ReactNode } from "react";
import {
  CheckCircleIcon,
  ClockIcon,
  InfoIcon,
  TrendDownIcon,
  TrendFlatIcon,
  TrendUpIcon,
  TruckIcon,
  WarningIcon,
} from "@/components/icons";
import { PageShell } from "@/components/layout/page-shell";
import { EstimatedRupees, ReportedPrice } from "@/components/data/price-bits";
import { PriceScreenFooter, ProvenancePanel } from "@/components/data/source";
import { ButtonLink } from "@/components/ui/button";
import { Card, EmptyState } from "@/components/ui/feedback";
import { AccessibleText, Message } from "@/components/ui/text";
import type { ReasonCode } from "@/lib/api/types";
import { useComparison } from "@/lib/compare/comparison-provider";
import { tradeOffOf } from "@/lib/compare/labels";
import { basisFromResponse, minimumApplies, optionsFromResponse, type OptionView } from "@/lib/compare/view-model";
import { useI18n } from "@/lib/i18n/i18n-provider";
import { localizedName, publishedNameLang } from "@/lib/i18n/names";
import { OptionBadges, optionLinks } from "./ledger";

const REASON_ICON: Record<ReasonCode, ReactNode> = {
  PRICE_ADVANTAGE: <TrendUpIcon />,
  LOWER_TRANSPORT: <TruckIcon />,
  POSITIVE_TREND: <TrendUpIcon />,
  STABLE_TREND: <TrendFlatIcon />,
  NEGATIVE_TREND: <TrendDownIcon />,
  RECENT_DATA: <CheckCircleIcon />,
  STALE_DATA: <WarningIcon />,
  SUITABLE_DISTANCE: <TruckIcon />,
};

/** S05 "Why recommended?" for one option of the comparison in memory (role file 02 §8 S05). */
export function WhyScreen() {
  const { t, fmt, locale } = useI18n();
  const marketId = useSearchParams().get("market");
  const { result } = useComparison();
  const views = result ? optionsFromResponse(result.response) : [];
  const view = views.find((candidate) => candidate.option.market.id === marketId);

  if (!result || !view) {
    return (
      <PageShell title={t("why.title")} backHref="/compare">
        <EmptyState
          icon={<InfoIcon size={28} />}
          title={t("why.not_found")}
          actions={<ButtonLink href="/compare">{t("action.compare")}</ButtonLink>}
        />
      </PageShell>
    );
  }

  const { response, context } = result;
  const basis = basisFromResponse(response);
  const first = view.rank === 1;
  const tradeOff = first ? tradeOffOf(response) : null;
  const lang = publishedNameLang(locale);
  const crop = localizedName(context.commodity, locale);
  const links = optionLinks(view, context.commodity.id);
  const trips = Number(view.trips);
  const tripsText = t(trips === 1 ? "ledger.trips_one" : "ledger.trips_other", { n: fmt.integer(trips) });

  return (
    <PageShell title={first ? t("why.title_recommended") : t("why.title")} backHref="/compare" width="medium">
      <div className="flex flex-col gap-4">
        <Card className="flex flex-col gap-2 px-4 py-4">
          <span className="flex flex-wrap items-center gap-2">
            <OptionBadges labels={view.labels} />
            <span className="text-label text-ink-2">{t("why.rank_of", { n: view.rank, total: views.length })}</span>
          </span>
          <h2 className="text-title font-bold" lang={lang}>{view.option.market.name}</h2>
          <p className="text-label text-ink-2">
            <AccessibleText text={`≈${fmt.wholeKm(view.option.distanceKm)} ${t("unit.km")}`} />
            {" · "}<span lang={lang}>{view.option.market.district.name}</span>
            {" · "}<span lang={crop.lang}>{crop.text}</span>
            {" · "}<span className="figure">{fmt.decimal(basis.quantityKg, 3)} {t("unit.kg")}</span>
          </p>
        </Card>

        <Card aria-labelledby="calc-title" className="px-4 py-4">
          <h2 id="calc-title" className="eyebrow mb-2 border-b border-rule pb-2">{t("why.calc_title")}</h2>
          <Row label={t("provenance.modal")} value={<ReportedPrice amount={view.option.price.modalPrice} unit={view.option.price.priceUnit} />} />
          <Row
            label={t("why.your_quantity")}
            value={<span className="figure">{fmt.decimal(basis.quantityInQuintal, 5)} {t("unit.quintal")} ({fmt.decimal(basis.quantityKg, 3)} {t("unit.kg")})</span>}
          />
          <Row label={t("ledger.gross")} value={<EstimatedRupees value={view.gross} />} strong />
          <Row label={t("ledger.transport")} value={<EstimatedRupees value={view.transport} />} />
          <p className="mb-2 rounded-control bg-sunken px-3 py-2 text-caption text-ink-2">
            <AccessibleText
              text={minimumApplies(view, basis)
                ? t("ledger.minimum", { min: fmt.decimal(basis.minimumTripCharge, 2) })
                : t("ledger.per_km", { km: fmt.wholeKm(view.option.distanceKm), rate: fmt.decimal(basis.ratePerKm, 2) })}
            />
            {" · "}
            {t("why.transport_terms", {
              trips: tripsText,
              kg: fmt.decimal(basis.vehicleCapacityKg, 2),
              min: fmt.decimal(basis.minimumTripCharge, 2),
            })}
          </p>
          <div className="double-rule flex items-baseline justify-between gap-2 pt-2">
            <span className="font-semibold text-net">
              {t("home.sum_net")}
              <span className="block text-caption font-normal">({t("net.gloss")})</span>
            </span>
            <EstimatedRupees value={view.net} className="text-numeric font-semibold text-net" />
          </div>
          <p className="mt-2 text-caption text-ink-2">{t("transport.label", { rate: fmt.decimal(basis.ratePerKm, 2) })}</p>
        </Card>

        <Card aria-labelledby="reasons-title" className="px-4 py-4">
          <h2 id="reasons-title" className="eyebrow mb-2 border-b border-rule pb-2">
            {first ? t("why.reasons_title_first") : t("why.reasons_title")}
          </h2>
          {view.reasonCodes.length === 0 ? (
            <p className="text-label text-ink-2">{t("why.no_reasons")}</p>
          ) : (
            <ul className="divide-y divide-rule">
              {view.reasonCodes.map((code) => (
                <li key={code} className="flex gap-3 py-3">
                  <span className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-control border ${code === "NEGATIVE_TREND" || code === "STALE_DATA" ? "border-stale bg-stale-bg text-stale" : "border-rule bg-sunken text-net"}`}>
                    {REASON_ICON[code]}
                  </span>
                  <span className="flex flex-col">
                    <span className="font-semibold">{t(`reason.${code}`)}</span>
                    <span className="text-label text-ink-2"><Evidence code={code} view={view} total={views.length} /></span>
                  </span>
                </li>
              ))}
            </ul>
          )}
        </Card>

        {tradeOff ? (
          <Card aria-labelledby="against-title" className="px-4 py-4">
            <h2 id="against-title" className="mb-2 border-b border-rule pb-2 text-body font-semibold">
              <Message id="why.against_title" values={{ market: <span lang={lang}>{tradeOff.priceWinner.market.name}</span> }} />
            </h2>
            <Row label={t("why.gross_diff")} value={<span className="figure">+{fmt.estimatedRupees(tradeOff.grossDiff)}</span>} />
            {tradeOff.transportDiff.isPositive() ? (
              <Row label={t("why.transport_diff")} value={<span className="figure">−{fmt.estimatedRupees(tradeOff.transportDiff)}</span>} />
            ) : null}
            <div className="flex items-baseline justify-between gap-3 border-t border-ink pt-2">
              <span className="font-semibold">{t("why.net_diff")}</span>
              <span className="text-right font-semibold">
                <Message
                  id={tradeOff.kind === "more" ? "why.less_at" : "why.more_at"}
                  values={{
                    n: <span className="figure">{fmt.estimatedRupees(tradeOff.netDiff.abs()).replace("₹", "")}</span>,
                    P: <span lang={lang}>{tradeOff.priceWinner.market.name}</span>,
                  }}
                />
              </span>
            </div>
          </Card>
        ) : null}

        <Card aria-labelledby="how-title" className="bg-sunken px-4 py-4">
          <h2 id="how-title" className="eyebrow mb-2">{t("why.how_title")}</h2>
          <p className="text-label">{t("rank.basis")}</p>
          {response.decisionBasis.mlContributed ? null : <p className="mt-1 text-label">{t("results.no_forecast")}</p>}
        </Card>

        <ProvenancePanel price={view.option.price} defaultOpen />

        <div className="grid gap-2 tablet:grid-cols-2">
          <ButtonLink href={links.trend} variant="secondary">{t("action.trend")}</ButtonLink>
          <ButtonLink href={links.alert} variant="secondary">{t("action.set_alert")}</ButtonLink>
          <ButtonLink href="/compare/what-if" variant="hypothetical" className="tablet:col-span-2">{t("results.whatif_cta")}</ButtonLink>
        </div>

        <PriceScreenFooter attribution={view.option.price.attribution} />
      </div>
    </PageShell>
  );
}

function Row({ label, value, strong = false }: { label: ReactNode; value: ReactNode; strong?: boolean }) {
  return (
    <div className={`flex items-baseline justify-between gap-3 py-1.5 ${strong ? "border-t border-rule font-semibold" : ""}`}>
      <span className="text-label">{label}</span>
      <span className="text-right text-label">{value}</span>
    </div>
  );
}

/** The evidence behind one reason code, from the option's own fields (role file 02 S05). */
function Evidence({ code, view, total }: { code: ReasonCode; view: OptionView; total: number }) {
  const { t, fmt, locale } = useI18n();
  const { option } = view;
  switch (code) {
    case "POSITIVE_TREND":
    case "STABLE_TREND":
    case "NEGATIVE_TREND":
      return option.changePercent7Day !== null ? (
        <Message id="why.ev_trend" values={{ pct: <span className="figure">{fmt.signedPercent(option.changePercent7Day)}</span> }} />
      ) : null;
    case "RECENT_DATA":
    case "STALE_DATA":
      return (
        <span className="inline-flex items-center gap-1">
          <ClockIcon size={14} />
          {t("why.ev_report", { date: fmt.date(option.price.reportDate), fresh: t(`fresh.${option.price.freshnessStatus}`) })}
        </span>
      );
    case "SUITABLE_DISTANCE":
      return (
        <Message
          id="why.ev_distance"
          values={{ km: fmt.wholeKm(option.distanceKm), district: <span lang={publishedNameLang(locale)}>{option.market.district.name}</span> }}
        />
      );
    case "PRICE_ADVANTAGE":
      return (
        <Message
          id="why.ev_price"
          values={{ price: <ReportedPrice amount={option.price.modalPrice} unit={option.price.priceUnit} />, n: total }}
        />
      );
    case "LOWER_TRANSPORT":
      return (
        <Message
          id="why.ev_transport"
          values={{ cost: <span className="figure">{fmt.estimatedRupees(view.transport)}</span>, n: total }}
        />
      );
  }
}

