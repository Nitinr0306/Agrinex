"use client";

import { useEffect, useRef, type ReactNode } from "react";
import { ArrowLeftIcon, ArrowRightIcon } from "@/components/icons";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/feedback";
import type { CommodityResponse, DistrictResponse, StateResponse } from "@/lib/api/types";
import { toKg, toQuintal, type CompareInput } from "@/lib/compare/input";
import { useQuery } from "@/lib/hooks/use-query";
import { useI18n } from "@/lib/i18n/i18n-provider";
import { localizedName, publishedNameLang } from "@/lib/i18n/names";
import { referenceData } from "@/lib/reference/reference-data";
import { CropFields, DistanceField, LocationFields, QuantityField, type FieldErrors } from "./input-fields";

export interface InputNames {
  state?: StateResponse;
  district?: DistrictResponse;
  commodity?: CommodityResponse;
}

/** Names of the chosen place and crop, from the cached reference lists. */
export function useInputNames(input: CompareInput): InputNames {
  const key = `${input.stateId}|${input.districtId}|${input.commodityId}`;
  const names = useQuery(key, async () => {
    const [states, districts, commodities] = await Promise.all([
      referenceData.states(),
      input.stateId ? referenceData.districts(input.stateId) : Promise.resolve([]),
      input.commodityId ? referenceData.commodities() : Promise.resolve([]),
    ]);
    return {
      state: states.find((state) => state.id === input.stateId),
      district: districts.find((district) => district.id === input.districtId),
      commodity: commodities.find((commodity) => commodity.id === input.commodityId),
    } satisfies InputNames;
  });
  return names.data ?? {};
}

interface FormProps {
  input: CompareInput;
  update: (patch: Partial<CompareInput>) => void;
  errors: FieldErrors;
  busy: boolean;
  disabled: boolean;
  onFind: () => void;
  footer?: ReactNode;
}

export type WizardStep = 1 | 2 | 3 | 4;

/**
 * Below 1024 px: one question per screen (borrowing 1 from B, in Hisaab style; role file 02 §1.1, S03).
 * Each step keeps its answer when the user goes back.
 */
export function InputWizard({ input, update, errors, busy, disabled, onFind, footer, step, setStep, onExit }: FormProps & {
  step: WizardStep;
  setStep: (step: WizardStep) => void;
  onExit: () => void;
}) {
  const { t } = useI18n();
  const headingRef = useRef<HTMLHeadingElement>(null);
  const names = useInputNames(input);
  const questions: Record<WizardStep, string> = {
    1: t("input.q_place"),
    2: t("input.q_crop"),
    3: t("input.q_qty"),
    4: t("input.q_distance"),
  };

  useEffect(() => {
    headingRef.current?.focus();
  }, [step]);

  const canAdvance = step === 1 ? input.districtId !== null
    : step === 2 ? input.commodityId !== null
      : step === 3 ? toKg(input.quantity, input.unit) !== null
        : true;

  return (
    <div className="flex flex-col gap-5">
      <div>
        <div className="flex gap-1.5" aria-hidden="true">
          {[1, 2, 3, 4].map((index) => (
            <span key={index} className={`h-1.5 flex-1 rounded-control ${index <= step ? "bg-action" : "bg-rule"}`} />
          ))}
        </div>
        <p className="mt-2 text-label text-ink-2">{t("input.step", { n: step })}</p>
      </div>

      <h1 ref={headingRef} tabIndex={-1} className="text-display font-bold text-ink focus:outline-none">{questions[step]}</h1>

      {step > 1 && step < 4 ? <AnswerSummary input={input} names={names} upTo={(step - 1) as WizardStep} onChange={setStep} /> : null}

      {step === 1 ? <LocationFields input={input} update={update} errors={errors} /> : null}
      {step === 2 ? <CropFields input={input} update={update} errors={errors} /> : null}
      {step === 3 ? <QuantityField input={input} update={update} errors={errors} /> : null}
      {step === 4 ? <DistanceField input={input} update={update} errors={errors} /> : null}
      {step === 4 ? <AnswerSummary input={input} names={names} upTo={4} onChange={setStep} title /> : null}

      <div className="flex flex-col gap-2">
        {step < 4 ? (
          <Button block disabled={!canAdvance} onClick={() => setStep((step + 1) as WizardStep)}>
            {t("input.next")} <ArrowRightIcon size={18} />
          </Button>
        ) : (
          <Button block busy={busy} disabled={disabled} onClick={onFind}>
            {t("action.find")} <ArrowRightIcon size={18} />
          </Button>
        )}
        <Button variant="tertiary" block onClick={() => (step === 1 ? onExit() : setStep((step - 1) as WizardStep))}>
          <ArrowLeftIcon size={18} /> {t("nav.back")}
        </Button>
      </div>
      {footer}
    </div>
  );
}

/** The answers so far, as a ruled summary with a way back to each step. */
export function AnswerSummary({ input, names, upTo, onChange, title = false }: {
  input: CompareInput;
  names: InputNames;
  upTo: WizardStep;
  onChange: (step: WizardStep) => void;
  title?: boolean;
}) {
  const { t, fmt, locale } = useI18n();
  const crop = names.commodity ? localizedName(names.commodity, locale) : null;
  // The quantity as the farmer entered it, in the unit they chose (the conversion is shown on the quantity step).
  const quantity = input.unit === "KG" ? toKg(input.quantity, input.unit) : toQuintal(input.quantity, input.unit);
  const rows: { step: WizardStep; label: string; value: ReactNode }[] = [
    {
      step: 1,
      label: t("input.district"),
      value: names.district ? <span lang={publishedNameLang(locale)}>{names.district.name}</span> : "—",
    },
    { step: 2, label: t("input.crop"), value: crop ? <span lang={crop.lang}>{crop.text}</span> : "—" },
    {
      step: 3,
      label: t("input.quantity"),
      value: quantity
        ? <span className="figure">{fmt.decimal(quantity, input.unit === "KG" ? 3 : 5)} {t(input.unit === "KG" ? "unit.kg" : "unit.quintal")}</span>
        : "—",
    },
    { step: 4, label: t("input.distance"), value: t("input.km_choice", { n: input.maxDistanceKm }) },
  ];
  return (
    <section aria-label={t("input.review")} className="rounded-card border border-rule bg-raised">
      {title ? <h2 className="eyebrow border-b border-rule px-4 py-2">{t("input.review")}</h2> : null}
      <dl className="divide-y divide-rule">
        {rows.filter((row) => row.step <= upTo).map((row) => (
          <div key={row.step} className="flex items-center justify-between gap-3 px-4 py-2">
            <div className="min-w-0">
              <dt className="text-caption text-ink-2">{row.label}</dt>
              <dd className="truncate text-body font-medium">{row.value}</dd>
            </div>
            <Button variant="tertiary" className="!min-h-10 shrink-0 !px-2" onClick={() => onChange(row.step)}>
              {t("input.change")}
            </Button>
          </div>
        ))}
      </dl>
    </section>
  );
}

/** At 1024 px and wider: the single input panel beside the results (approved draft d02, layout). */
export function InputPanel({ input, update, errors, busy, disabled, onFind, footer }: FormProps) {
  const { t } = useI18n();
  return (
    <Card className="flex flex-col gap-5 p-5">
      <h2 className="text-heading font-semibold">{t("input.panel_title")}</h2>
      <LocationFields input={input} update={update} errors={errors} />
      <hr className="border-rule" />
      <CropFields input={input} update={update} errors={errors} />
      <hr className="border-rule" />
      <QuantityField input={input} update={update} errors={errors} />
      <hr className="border-rule" />
      <DistanceField input={input} update={update} errors={errors} />
      <Button block busy={busy} disabled={disabled} onClick={onFind}>
        {t("action.find")} <ArrowRightIcon size={18} />
      </Button>
      {footer}
    </Card>
  );
}
