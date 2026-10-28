"use client";

import { CartesianGrid, Line, LineChart, ReferenceArea, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import type { TrendResponse } from "@/lib/api/types";
import { useI18n } from "@/lib/i18n/i18n-provider";

const DAY_MS = 24 * 60 * 60 * 1000;
const time = (isoDate: string) => Date.parse(`${isoDate}T00:00:00Z`);

/**
 * The trend chart (role file 02 S07): dots only on report days, lines only joining reports, the two 7-day windows
 * shaded. It is a picture of the price diary below, which is its text equivalent (role file 02 §12).
 */
export function TrendChart({ trend }: { trend: TrendResponse }) {
  const { t, fmt } = useI18n();
  const points = trend.observations
    .map((point) => ({ t: time(point.reportDate), price: point.modalPrice, date: point.reportDate }))
    .sort((a, b) => a.t - b.t);
  const from = time(trend.window.chartFrom);
  const to = time(trend.window.chartTo);
  const showBaseline = trend.window.baselineFrom >= trend.window.chartFrom;

  return (
    <figure className="flex flex-col gap-2">
      <div
        role="img"
        aria-label={t("trend.chart_label", { from: fmt.date(trend.window.chartFrom), to: fmt.date(trend.window.chartTo) })}
        className="h-56 w-full"
      >
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={points} margin={{ top: 24, right: 12, bottom: 4, left: 4 }}>
            <CartesianGrid stroke="var(--color-rule)" strokeDasharray="2 4" vertical={false} />
            {showBaseline ? (
              <ReferenceArea
                x1={time(trend.window.baselineFrom)}
                x2={time(trend.window.baselineTo) + DAY_MS / 2}
                fill="var(--color-sunken)"
                fillOpacity={0.9}
                label={{ value: t("trend.prev7"), position: "insideTopLeft", fontSize: 11, fill: "var(--color-ink-2)" }}
              />
            ) : null}
            <ReferenceArea
              x1={time(trend.window.recentFrom) - DAY_MS / 2}
              x2={to}
              fill="var(--color-unavailable-bg)"
              fillOpacity={0.9}
              label={{ value: t("trend.last7"), position: "insideTopRight", fontSize: 11, fill: "var(--color-ink)" }}
            />
            <XAxis
              dataKey="t"
              type="number"
              scale="time"
              domain={[from, to]}
              tickFormatter={(value: number) => fmt.date(new Date(value).toISOString().slice(0, 10))}
              tick={{ fontSize: 11, fill: "var(--color-ink-2)" }}
              stroke="var(--color-rule-strong)"
              minTickGap={24}
            />
            <YAxis
              domain={["auto", "auto"]}
              tickFormatter={(value: number) => fmt.reportedRupees(Math.round(value))}
              tick={{ fontSize: 11, fill: "var(--color-ink-2)" }}
              stroke="var(--color-rule-strong)"
              width={64}
            />
            <Tooltip
              formatter={(value) => [fmt.reportedRupees(Number(value)), t("trend.col_price")]}
              labelFormatter={(value) => fmt.date(new Date(Number(value)).toISOString().slice(0, 10))}
              contentStyle={{ border: "1px solid var(--color-rule-strong)", borderRadius: 4, fontSize: 13 }}
            />
            <Line
              dataKey="price"
              type="linear"
              stroke="var(--color-ink)"
              strokeWidth={2}
              dot={{ r: 4, fill: "var(--color-ink)", stroke: "var(--color-raised)", strokeWidth: 1.5 }}
              activeDot={{ r: 6 }}
              isAnimationActive={false}
              connectNulls={false}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
      <figcaption className="text-caption italic text-ink-2">{t("trend.caption")}</figcaption>
    </figure>
  );
}
