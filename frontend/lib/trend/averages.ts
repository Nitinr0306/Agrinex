/**
 * The two window averages shown next to the 7-day change (role file 02 S07). The contract carries the change
 * (ADR-0025) and the daily series but not the averages, so they are derived from the observations of the two
 * windows the response names, and shown only when they reproduce the server's own change percentage. Nothing is
 * shown when a window is not covered (for example the 7-day chart, which has no baseline days).
 */
import type { TrendResponse } from "@/lib/api/types";
import { Decimal } from "@/lib/decimal";

export interface WindowAverages {
  recent: Decimal;
  baseline: Decimal;
}

export function windowAverages(trend: TrendResponse): WindowAverages | null {
  if (trend.changePercent7Day === null || trend.trendDirection === "INSUFFICIENT_DATA") return null;
  const { recentFrom, recentTo, baselineFrom, baselineTo, chartFrom } = trend.window;
  if (chartFrom > baselineFrom) return null;
  const within = (from: string, to: string) =>
    trend.observations.filter((point) => point.reportDate >= from && point.reportDate <= to);
  const recent = mean(within(recentFrom, recentTo).map((point) => Decimal.of(point.modalPrice)));
  const baseline = mean(within(baselineFrom, baselineTo).map((point) => Decimal.of(point.modalPrice)));
  if (!recent || !baseline || !baseline.isPositive()) return null;
  const change = recent.sub(baseline).mul(Decimal.ofInteger(100)).divFloor(baseline, 12).round(1);
  return change.eq(Decimal.of(trend.changePercent7Day).round(1)) ? { recent: recent.round(2), baseline: baseline.round(2) } : null;
}

function mean(values: Decimal[]): Decimal | null {
  if (values.length === 0) return null;
  const sum = values.reduce((total, value) => total.add(value), Decimal.ZERO);
  return sum.divFloor(Decimal.ofInteger(values.length), 12);
}
