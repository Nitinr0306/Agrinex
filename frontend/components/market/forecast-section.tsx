"use client";

import { Card } from "@/components/ui/feedback";
import { marketApi } from "@/lib/api/endpoints";
import { useQuery } from "@/lib/hooks/use-query";
import { useI18n } from "@/lib/i18n/i18n-provider";

/**
 * S08, the optional forecast section (role file 02 S08; ADR-0030): a separate section titled as a model
 * prediction, never mixed into prices or comparisons, with no confidence percentage. When no forecast exists
 * (`available: false`, or the call fails) it says "Forecast unavailable." and nothing else; a failure here never
 * affects the rest of the screen (spec §35).
 */
export function ForecastSection({ marketId, commodityId }: { marketId: string; commodityId: string }) {
  const { t, fmt } = useI18n();
  const forecast = useQuery(`forecast:${marketId}:${commodityId}`, (signal) =>
    marketApi.getMarketForecast(marketId, commodityId, signal));

  if (forecast.status === "loading" || forecast.status === "idle") return null;
  const data = forecast.status === "success" ? forecast.data : null;
  const available = data !== null && data.available && data.forecasts.length > 0;

  return (
    <Card aria-labelledby="forecast-title" className="flex flex-col gap-3 border-dashed px-4 py-4">
      <h2 id="forecast-title" className="text-heading font-semibold">{t("forecast.title")}</h2>
      {!available ? (
        <p className="text-label text-ink-2">{t("state.forecast_unavailable")}</p>
      ) : (
        <>
          <p className="text-label text-ink-2">{t("forecast.note")}</p>
          {data.forecasts.map((series) => (
            <section key={`${series.forecastHorizonDays}:${series.generatedAt}`} className="rounded-control border border-rule px-3 py-3">
              <h3 className="text-label font-semibold">{t("forecast.horizon", { n: series.forecastHorizonDays })}</h3>
              <p className="text-caption text-ink-2" lang="en">{t("forecast.model", { name: series.modelName, version: series.modelVersion })}</p>
              <p className="text-caption text-ink-2">
                {t("forecast.generated", { time: fmt.dateTime(series.generatedAt), date: fmt.date(series.trainingDataThrough) })}
              </p>
              <table className="mt-2 w-full text-label">
                <thead>
                  <tr className="text-left text-caption text-ink-2">
                    <th scope="col" className="py-1 font-semibold">{t("trend.col_date")}</th>
                    <th scope="col" className="py-1 text-right font-semibold">{t("forecast.predicted")}</th>
                    <th scope="col" className="py-1 text-right font-semibold">{t("forecast.range")}</th>
                  </tr>
                </thead>
                <tbody>
                  {series.forecast.map((point) => (
                    <tr key={point.date} className="border-t border-rule">
                      <th scope="row" className="py-1 text-left font-medium">{fmt.date(point.date)}</th>
                      <td className="figure py-1 text-right">{fmt.reportedRupees(point.predictedPrice)}</td>
                      <td className="figure py-1 text-right">
                        {point.lowerBound !== null && point.upperBound !== null
                          ? `${fmt.reportedRupees(point.lowerBound)} – ${fmt.reportedRupees(point.upperBound)}`
                          : "—"}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {series.mae !== null && series.rmse !== null ? (
                <p className="mt-2 text-caption text-ink-2">
                  {t("forecast.errors", { mae: fmt.reportedRupees(series.mae), rmse: fmt.reportedRupees(series.rmse) })}
                </p>
              ) : null}
            </section>
          ))}
        </>
      )}
    </Card>
  );
}
