"use client";

import { useSearchParams } from "next/navigation";
import { useCallback, useEffect, useRef, useState } from "react";
import { AppBar } from "@/components/layout/app-bar";
import { SourceStatusLine } from "@/components/data/source";
import { LoadProblem } from "@/components/states/load-problem";
import { Notice, Skeleton } from "@/components/ui/feedback";
import { sellingOptionsApi } from "@/lib/api/endpoints";
import { isApiError } from "@/lib/api/errors";
import { useComparison } from "@/lib/compare/comparison-provider";
import { inputFromSearch, toCompareRequest, type CompareInput } from "@/lib/compare/input";
import { useCountdown, useDelayedFlag } from "@/lib/hooks/use-query";
import { useIsDesktop } from "@/lib/hooks/use-media";
import { useI18n } from "@/lib/i18n/i18n-provider";
import { forgetCommodities, forgetGeography, referenceData } from "@/lib/reference/reference-data";
import { rememberCrop, validateInput, type FieldErrors } from "./input-fields";
import { InputPanel, InputWizard, type WizardStep } from "./input-forms";
import { NoMarketInRange } from "./no-market";
import { ResultsView } from "./results-view";

type Problem =
  | { kind: "noMarket"; km: number; districtName: string }
  | { kind: "rateLimited"; retryAfter: number | null }
  | { kind: "location" }
  | { kind: "failed"; error: unknown };

const STEP_OF_FIELD: Record<keyof FieldErrors, WizardStep> = { district: 1, crop: 2, quantity: 3, distance: 4 };

/** S03 comparison input and S04 results (role file 02 §8; spec §21, §25). */
export function CompareScreen() {
  const { t } = useI18n();
  const searchParams = useSearchParams();
  const { input, setInput, result, setResult, applied, setApplied, lastSourceStatus } = useComparison();
  const [step, setStep] = useState<WizardStep>(1);
  const [mobileView, setMobileView] = useState<"input" | "results">(result ? "results" : "input");
  const [errors, setErrors] = useState<FieldErrors>({});
  const [problem, setProblem] = useState<Problem | null>(null);
  const [busy, setBusy] = useState(false);
  const resultsRef = useRef<HTMLDivElement>(null);
  const showSkeleton = useDelayedFlag(busy);
  const isDesktop = useIsDesktop();
  const wait = useCountdown(problem?.kind === "rateLimited" ? problem.retryAfter : null);

  // A link may name the crop to compare (for example "Compare markets now" on an alert event).
  useEffect(() => {
    if (searchParams.size > 0) setInput((current) => inputFromSearch(searchParams, current));
  }, [searchParams, setInput]);

  const update = useCallback((patch: Partial<CompareInput>) => {
    setInput((current) => ({ ...current, ...patch }));
    setErrors((current) => {
      const next = { ...current };
      if ("districtId" in patch || "stateId" in patch) delete next.district;
      if ("commodityId" in patch || "varietyId" in patch || "gradeId" in patch) delete next.crop;
      if ("quantity" in patch || "unit" in patch) delete next.quantity;
      if ("maxDistanceKm" in patch) delete next.distance;
      return next;
    });
  }, [setInput]);

  const showFieldErrors = (next: FieldErrors) => {
    setErrors(next);
    const first = (Object.keys(STEP_OF_FIELD) as (keyof FieldErrors)[]).find((field) => next[field]);
    if (first) {
      setStep(STEP_OF_FIELD[first]);
      setMobileView("input");
    }
  };

  const find = async (radiusKm?: number) => {
    const effective: CompareInput = radiusKm ? { ...input, maxDistanceKm: radiusKm } : input;
    if (radiusKm) setInput(() => effective);
    const invalid = validateInput(effective, t);
    if (Object.keys(invalid).length > 0) {
      showFieldErrors(invalid);
      return;
    }
    setBusy(true);
    setProblem(null);
    setErrors({});
    try {
      const [districts, commodities] = await Promise.all([
        referenceData.districts(effective.stateId as string),
        referenceData.commodities(),
      ]);
      const district = districts.find((candidate) => candidate.id === effective.districtId);
      const commodity = commodities.find((candidate) => candidate.id === effective.commodityId);
      if (!district || !commodity) {
        showFieldErrors(district ? { crop: t("error.choice_gone") } : { district: t("error.choice_gone") });
        return;
      }
      const request = toCompareRequest(effective, district);
      const response = await sellingOptionsApi.compare(request);
      rememberCrop(commodity.id);
      setResult({ request, response, context: { district, commodity } });
      setMobileView("results");
      requestAnimationFrame(() => resultsRef.current?.focus());
    } catch (error) {
      handleFailure(error, effective);
    } finally {
      setBusy(false);
    }
  };

  const handleFailure = async (error: unknown, effective: CompareInput) => {
    if (isApiError(error, "VALIDATION_FAILED")) {
      if (error.fieldError("originLat") || error.fieldError("originLon")) {
        // E10: the published centroids changed; choose the district again from fresh data.
        forgetGeography();
        setInput((current) => ({ ...current, districtId: null, localityId: null }));
        setProblem({ kind: "location" });
        setStep(1);
        setMobileView("input");
        return;
      }
      const quantity = error.fieldError("quantity");
      showFieldErrors({
        ...(quantity ? { quantity: quantityMessage(quantity.code) } : {}),
        ...(error.fieldError("commodityId") ? { crop: t("error.choice_gone") } : {}),
        ...(error.fieldError("maxDistanceKm") ? { distance: t("input.km_invalid") } : {}),
      });
      return;
    }
    if (isApiError(error, "NO_MARKETS_IN_RANGE")) {
      const district = (await referenceData.districts(effective.stateId as string))
        .find((candidate) => candidate.id === effective.districtId);
      setProblem({ kind: "noMarket", km: effective.maxDistanceKm, districtName: district?.name ?? "" });
      setResult(null);
      setMobileView("results");
      return;
    }
    if (isApiError(error, "UNSUPPORTED_PRICE_UNIT")) {
      showFieldErrors({ crop: t("error.unsupported_unit") });
      return;
    }
    if (isApiError(error, "REFERENCE_NOT_FOUND")) {
      forgetCommodities();
      setInput((current) => ({ ...current, varietyId: null, gradeId: null }));
      showFieldErrors({ crop: t("error.choice_gone") });
      return;
    }
    if (isApiError(error, "ESTIMATE_OUT_OF_RANGE")) {
      showFieldErrors({ quantity: t("error.too_large") });
      return;
    }
    if (isApiError(error, "RATE_LIMITED")) {
      setProblem({ kind: "rateLimited", retryAfter: error.retryAfterSeconds });
      return;
    }
    setProblem({ kind: "failed", error });
  };

  const quantityMessage = (code: string) =>
    code === "REQUIRED" || code === "OUT_OF_RANGE" || code === "PRECISION_EXCEEDED"
      ? t(`err.quantity.${code}`)
      : t("err.quantity.OUT_OF_RANGE");

  const editInput = () => {
    setStep(4);
    setMobileView("input");
  };

  const findDisabled = wait > 0;
  const statusLine = lastSourceStatus ? <SourceStatusLine status={lastSourceStatus} /> : null;
  const inputNotice = problem?.kind === "location" ? <Notice tone="error" live>{t("error.location")}</Notice>
    : problem?.kind === "rateLimited" && wait > 0 ? <Notice tone="error" live>{t("error.rate_compare", { n: wait })}</Notice>
      : null;

  const resultsArea = (
    <div ref={resultsRef} tabIndex={-1} className="flex flex-col gap-4 focus:outline-none" aria-live="polite" aria-busy={busy}>
      <h2 className="sr-only">{t("results.heading")}</h2>
      {busy && showSkeleton ? <ResultsSkeleton /> : null}
      {!busy && problem?.kind === "failed" ? <LoadProblem error={problem.error} onRetry={() => void find()} /> : null}
      {!busy && problem?.kind === "noMarket" ? (
        <NoMarketInRange
          km={problem.km}
          districtName={problem.districtName}
          busy={busy}
          disabled={findDisabled}
          onSearch={(km) => void find(km)}
          onChange={() => {
            setStep(1);
            setMobileView("input");
          }}
        />
      ) : null}
      {!busy && result && problem === null ? (
        <ResultsView
          response={result.response}
          context={result.context}
          applied={applied}
          onEdit={editInput}
          onResetWhatIf={() => setApplied(null)}
          onWiden={() => void find(Math.min(300, nextRadius(result.response.maxDistanceKm)))}
          onChooseCrop={() => {
            setStep(2);
            setMobileView("input");
          }}
        />
      ) : null}
      {!busy && !result && problem === null ? (
        <p className="hidden rounded-card border border-dashed border-rule-strong px-6 py-10 text-center text-ink-2 desktop:block">
          {t("results.placeholder")}
        </p>
      ) : null}
    </div>
  );

  return (
    <>
      <AppBar
        title={t("action.compare")}
        backHref={mobileView === "results" ? undefined : "/"}
        onBack={mobileView === "results" && !isDesktop ? editInput : undefined}
      />
      <main id="main" tabIndex={-1} className="mx-auto w-full max-w-6xl px-4 pb-16 pt-5 focus:outline-none">
        {/* On mobile the wizard's step question is the page heading. */}
        {isDesktop || mobileView === "results" ? (
          <h1 className="sr-only desktop:not-sr-only desktop:mb-5 desktop:block desktop:text-display desktop:font-bold">
            {t("action.compare")}
          </h1>
        ) : null}

        {isDesktop ? (
          <div className="grid grid-cols-[22rem_minmax(0,1fr)] items-start gap-8">
            <div>
              <InputPanel
                input={input}
                update={update}
                errors={errors}
                busy={busy}
                disabled={findDisabled}
                onFind={() => void find()}
                footer={<>{inputNotice}{statusLine}</>}
              />
            </div>
            {resultsArea}
          </div>
        ) : mobileView === "input" ? (
          <InputWizard
            input={input}
            update={update}
            errors={errors}
            busy={busy}
            disabled={findDisabled}
            onFind={() => void find()}
            step={step}
            setStep={setStep}
            onExit={() => history.back()}
            footer={<>{inputNotice}{statusLine}</>}
          />
        ) : (
          resultsArea
        )}
      </main>
    </>
  );
}

function nextRadius(current: number): number {
  return [50, 100, 150, 200, 300].find((km) => km > current) ?? 300;
}

/** The results layout while a comparison is on its way (PDD-13: after 300 ms; never over stale results). */
function ResultsSkeleton() {
  return (
    <div className="flex flex-col gap-3">
      <Skeleton className="h-28" />
      <Skeleton className="h-20" />
      <Skeleton className="h-64" />
      <Skeleton className="h-64" />
    </div>
  );
}
