"use client";

import { useRouter } from "next/navigation";
import { useMemo, useState } from "react";
import { PageShell } from "@/components/layout/page-shell";
import { LoadProblem } from "@/components/states/load-problem";
import { Button } from "@/components/ui/button";
import { ChipGroup } from "@/components/ui/choice";
import { Card, Notice } from "@/components/ui/feedback";
import { Field, Select, TextInput } from "@/components/ui/form";
import { savePendingAlert } from "@/lib/alerts/pending-alert";
import { ALERT_TYPES, isPriceAlert } from "@/lib/alerts/wording";
import { alertApi, marketApi } from "@/lib/api/endpoints";
import { isApiError } from "@/lib/api/errors";
import type { AlertType } from "@/lib/api/types";
import { useSession } from "@/lib/auth/session-provider";
import { useComparison } from "@/lib/compare/comparison-provider";
import { Decimal } from "@/lib/decimal";
import { useQuery } from "@/lib/hooks/use-query";
import { useI18n } from "@/lib/i18n/i18n-provider";
import { localizedName, publishedNameLang } from "@/lib/i18n/names";
import { referenceData } from "@/lib/reference/reference-data";

const UUID = /^[0-9a-fA-F-]{36}$/;
/** CreateAlertRequest.thresholdValue: > 0, at most 9,999,999,999.9999, at most 4 decimals (contract). */
const THRESHOLD_MAX = Decimal.of("9999999999.9999");

export function thresholdProblem(text: string): boolean {
  const trimmed = text.trim();
  if (!/^\d+(\.\d{1,4})?$/.test(trimmed)) return true;
  const value = Decimal.of(trimmed);
  return !value.isPositive() || value.gt(THRESHOLD_MAX);
}

/** S11 create form (role file 02 S11): alert type, crop, optional market, threshold. */
export function AlertForm({ crop: initialCrop, market: initialMarket }: { crop: string | null; market: string | null }) {
  const { t, locale } = useI18n();
  const router = useRouter();
  const { status } = useSession();
  const { result } = useComparison();
  const commodities = useQuery("commodities", () => referenceData.commodities());
  const marketId = initialMarket && UUID.test(initialMarket) ? initialMarket : null;

  const [alertType, setAlertType] = useState<AlertType | null>(null);
  const [commodityId, setCommodityId] = useState<string | null>(initialCrop && UUID.test(initialCrop) ? initialCrop : null);
  const [oneMarket, setOneMarket] = useState(marketId !== null);
  const [threshold, setThreshold] = useState("");
  const [errors, setErrors] = useState<{ type?: string; crop?: string; threshold?: string }>({});
  const [busy, setBusy] = useState(false);
  const [problem, setProblem] = useState<unknown>(null);

  // The market's name: from the comparison in memory, otherwise from its public detail.
  const knownName = useMemo(() => {
    const response = result?.response;
    return response?.options.find((option) => option.market.id === marketId)?.market.name
      ?? response?.excludedMarkets.find((excluded) => excluded.market.id === marketId)?.market.name
      ?? null;
  }, [result, marketId]);
  const detail = useQuery(marketId && !knownName ? `alert-market:${marketId}` : null, (signal) =>
    marketApi.getMarketDetail(marketId as string, {}, signal));
  const marketName = knownName ?? (detail.status === "success" ? detail.data.market.name : null);
  const marketMissing = detail.status === "error";
  // A market that cannot be found is never offered: the alert then covers all markets.
  const marketFixed = oneMarket && marketId !== null && !marketMissing;

  const commodity = commodities.data?.find((item) => item.id === commodityId) ?? null;

  const submit = async () => {
    const next: typeof errors = {};
    if (!alertType) next.type = t("alerts.choose_type");
    if (!commodityId) next.crop = t("input.required_crop");
    if (thresholdProblem(threshold)) next.threshold = t("alerts.threshold_invalid");
    setErrors(next);
    if (Object.keys(next).length > 0 || !alertType || !commodityId || !commodity) return;

    const request = {
      alertType,
      commodityId,
      marketId: marketFixed ? marketId : null,
      thresholdValue: Number(threshold.trim()),
    };
    if (status !== "signedIn") {
      savePendingAlert({ ...request, cropName: localizedName(commodity, locale).text, marketName: marketFixed ? marketName : null });
      router.push("/sign-in?tab=create");
      return;
    }
    setBusy(true);
    setProblem(null);
    try {
      await alertApi.createAlert(request);
      router.push("/alerts?created=1");
    } catch (error) {
      if (isApiError(error, "UNSUPPORTED_PRICE_UNIT")) setErrors({ crop: t("error.unsupported_unit") });
      else if (isApiError(error, "REFERENCE_NOT_FOUND")) setErrors({ crop: t("error.choice_gone") });
      else if (isApiError(error, "VALIDATION_FAILED")) setErrors({ threshold: t("alerts.threshold_invalid") });
      else if (isApiError(error, "UNAUTHORIZED")) {
        savePendingAlert({ ...request, cropName: localizedName(commodity, locale).text, marketName: marketFixed ? marketName : null });
        router.push("/sign-in?tab=signin");
      } else setProblem(error);
    } finally {
      setBusy(false);
    }
  };

  const priceAlert = alertType === null || isPriceAlert(alertType);
  return (
    <PageShell title={t("alerts.new")} backHref="/alerts" width="narrow">
      <Card className="flex flex-col gap-5 p-4">
        <div className="flex flex-col gap-2">
          <span className="text-label font-semibold" id="type-label">{t("alerts.type_label")}</span>
          <ChipGroup
            label={t("alerts.type_label")}
            hideLabel
            variant="rows"
            options={ALERT_TYPES.map((type) => ({ value: type, label: t(`alerts.choice.${type}`) }))}
            value={alertType}
            onChange={(type) => {
              setAlertType(type);
              setErrors((current) => ({ ...current, type: undefined }));
            }}
          />
          {errors.type ? <p className="text-label font-medium text-error" role="alert">{errors.type}</p> : null}
        </div>

        {commodities.status === "error" ? <LoadProblem error={commodities.error} onRetry={commodities.retry} /> : (
          <Field label={t("input.crop")} error={errors.crop}>
            {({ inputId, describedBy, invalid }) => (
              <Select
                id={inputId}
                aria-describedby={describedBy}
                invalid={invalid}
                value={commodityId ?? ""}
                onChange={(event) => setCommodityId(event.target.value || null)}
              >
                <option value="">{commodities.status === "success" ? t("input.choose") : t("input.loading")}</option>
                {(commodities.data ?? []).map((item) => {
                  const name = localizedName(item, locale);
                  return <option key={item.id} value={item.id} lang={name.lang}>{name.text}</option>;
                })}
              </Select>
            )}
          </Field>
        )}

        <div className="flex flex-col gap-2">
          <span className="text-label font-semibold">{t("alerts.market")}</span>
          {marketId && !marketMissing ? (
            <ChipGroup
              label={t("alerts.market")}
              hideLabel
              variant="rows"
              options={[
                { value: "one", label: marketName ?? t("input.loading"), lang: publishedNameLang(locale) },
                { value: "all", label: t("alerts.all_markets") },
              ]}
              value={marketFixed ? "one" : "all"}
              onChange={(value) => setOneMarket(value === "one")}
            />
          ) : (
            <p className="rounded-control border border-rule bg-sunken px-3 py-2">{t("alerts.all_markets")}</p>
          )}
          <p className="text-caption text-ink-2">{marketFixed ? t("alerts.market_fixed_help") : t("alerts.all_markets_help")}</p>
        </div>

        <Field
          label={priceAlert ? t("alerts.threshold_price") : t("alerts.threshold_pct")}
          help={priceAlert ? undefined : t("alerts.threshold_help_pct")}
          error={errors.threshold}
        >
          {({ inputId, describedBy, invalid }) => (
            <TextInput
              id={inputId}
              inputMode="decimal"
              aria-describedby={describedBy}
              invalid={invalid}
              value={threshold}
              onChange={(event) => setThreshold(event.target.value.replace(/[^\d.]/g, ""))}
              className="figure max-w-48 text-right"
            />
          )}
        </Field>

        {problem ? <LoadProblem error={problem} onRetry={() => void submit()} /> : null}
        {status !== "signedIn" ? <Notice>{t("auth.why_account")}</Notice> : null}
        <Button block busy={busy} onClick={() => void submit()}>{t("alerts.save")}</Button>
        <p className="text-caption text-ink-2">{t("alerts.info")}</p>
      </Card>
    </PageShell>
  );
}
