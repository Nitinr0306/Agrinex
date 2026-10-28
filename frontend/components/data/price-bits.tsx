"use client";

import type { FreshnessStatus, ReasonCode, TrendDirection } from "@/lib/api/types";
import { RUPEES_PER_QUINTAL } from "@/lib/api/types";
import { isCaution } from "@/lib/compare/labels";
import type { Decimal } from "@/lib/decimal";
import { useI18n } from "@/lib/i18n/i18n-provider";
import { todayInIndia } from "@/lib/i18n/format";
import {
  CheckCircleIcon,
  ClockIcon,
  MinusCircleIcon,
  TrendDownIcon,
  TrendFlatIcon,
  TrendUpIcon,
  WarningIcon,
  XCircleIcon,
} from "@/components/icons";
import { Approx, Message } from "@/components/ui/text";

const FRESHNESS_STYLE: Record<FreshnessStatus, { className: string; icon: typeof CheckCircleIcon }> = {
  FRESH: { className: "border-fresh bg-fresh-bg text-fresh", icon: CheckCircleIcon },
  RECENT: { className: "border-recent bg-recent-bg text-recent", icon: ClockIcon },
  STALE: { className: "border-stale bg-stale-bg text-stale", icon: WarningIcon },
  UNAVAILABLE: { className: "border-unavailable bg-unavailable-bg text-unavailable", icon: XCircleIcon },
};

/** Freshness as icon + word (+ its plain meaning when asked), never colour alone (role file 02 §6, PDD-12). */
export function FreshnessBadge({ status, reportDate, describe = false }: {
  status: FreshnessStatus;
  reportDate?: string;
  describe?: boolean;
}) {
  const { t } = useI18n();
  const { className, icon: Icon } = FRESHNESS_STYLE[status];
  // "reported today" only when the report date is today (design skill rule; PDD-12).
  const description = status === "FRESH" && reportDate === todayInIndia()
    ? t("fresh.today")
    : t(`fresh_desc.${status}`);
  return (
    <span className={`inline-flex items-center gap-1 rounded-control border px-2 py-0.5 text-label font-semibold ${className}`}>
      <Icon size={16} />
      {t(`fresh.${status}`)}
      {describe ? <span className="font-normal">· {description}</span> : null}
    </span>
  );
}

const TREND_STYLE: Record<TrendDirection, { className: string; icon: typeof TrendUpIcon }> = {
  RISING: { className: "text-rising", icon: TrendUpIcon },
  STABLE: { className: "text-steady", icon: TrendFlatIcon },
  FALLING: { className: "text-falling", icon: TrendDownIcon },
  INSUFFICIENT_DATA: { className: "text-steady", icon: MinusCircleIcon },
};

/** Arrow + word + signed one-decimal percentage with "7-day change" (role file 02 §6; ADR-0025). */
export function TrendIndicator({ direction, changePercent, compact = false }: {
  direction: TrendDirection;
  changePercent: number | null;
  compact?: boolean;
}) {
  const { t, fmt } = useI18n();
  const { className, icon: Icon } = TREND_STYLE[direction];
  return (
    <span className={`inline-flex flex-wrap items-center gap-1 text-label font-semibold ${className}`}>
      <Icon size={18} />
      <span>{t(`trend.${direction}`)}</span>
      {changePercent !== null && direction !== "INSUFFICIENT_DATA" ? (
        compact ? (
          <span className="figure font-medium">{fmt.signedPercent(changePercent)}</span>
        ) : (
          <span className="font-medium">
            <Message id="trend.change" values={{ pct: <span className="figure">{fmt.signedPercent(changePercent)}</span> }} />
          </span>
        )
      ) : null}
    </span>
  );
}

/** Reason codes as chips; the two cautions take the caution treatment with a warning icon. */
export function ReasonChips({ codes, hypothetical = false }: { codes: ReasonCode[]; hypothetical?: boolean }) {
  const { t } = useI18n();
  if (codes.length === 0) return null;
  return (
    <ul className="flex flex-wrap gap-1.5" aria-label={t("reason.list")}>
      {codes.map((code) => {
        const caution = isCaution(code);
        const recomputed = hypothetical && (code === "PRICE_ADVANTAGE" || code === "LOWER_TRANSPORT");
        return (
          <li
            key={code}
            className={[
              "inline-flex items-center gap-1 rounded-control border px-2 py-1 text-label",
              caution ? "border-stale bg-stale-bg text-stale font-medium" : "border-rule bg-sunken text-ink",
              recomputed ? "border-dashed border-hyp" : "",
            ].join(" ")}
          >
            {caution ? <WarningIcon size={15} /> : null}
            {t(`reason.${code}`)}
          </li>
        );
      })}
    </ul>
  );
}

/** A reported price exactly as reported, with its unit: "₹3,000/quintal" (role file 02 §7; PDD-07). */
export function ReportedPrice({ amount, unit }: { amount: number; unit: string }) {
  const { t, fmt } = useI18n();
  return (
    <span className="figure whitespace-nowrap">
      {fmt.reportedRupees(amount)}
      {unit === RUPEES_PER_QUINTAL ? t("unit.per_quintal") : <span lang="en">{` (${unit})`}</span>}
    </span>
  );
}

/** An estimate to the whole rupee with "≈" (PDD-07); negative estimates keep their sign. */
export function EstimatedRupees({ value, className = "" }: { value: number | Decimal; className?: string }) {
  const { fmt } = useI18n();
  return (
    <span className={`figure whitespace-nowrap ${className}`}>
      <Approx />
      {fmt.estimatedRupees(value)}
    </span>
  );
}

/** "≈42 km" — every distance is approximate (spec §47 Q6). */
export function Distance({ km, approximateLocation = false }: { km: number; approximateLocation?: boolean }) {
  const { t, fmt } = useI18n();
  return (
    <span>
      <span className="figure whitespace-nowrap">
        <Approx />
        {fmt.wholeKm(km)} {t("unit.km")}
      </span>
      {approximateLocation ? <span className="text-ink-2"> ({t("distance.location_approximate")})</span> : null}
    </span>
  );
}
