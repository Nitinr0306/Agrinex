"use client";

import { useEffect, useMemo, useState } from "react";
import { SearchIcon } from "@/components/icons";
import { ChipGroup, Segmented } from "@/components/ui/choice";
import { Notice } from "@/components/ui/feedback";
import { Field, Select, TextInput } from "@/components/ui/form";
import type { CommodityResponse, QuantityUnit } from "@/lib/api/types";
import { DISTANCE_CHOICES, quantityProblem, toKg, toQuintal, type CompareInput } from "@/lib/compare/input";
import { useQuery } from "@/lib/hooks/use-query";
import { useStoredText } from "@/lib/hooks/use-stored-text";
import { useI18n, type Translate } from "@/lib/i18n/i18n-provider";
import { localizedName, publishedNameLang } from "@/lib/i18n/names";
import { referenceData } from "@/lib/reference/reference-data";
import { LoadProblem } from "@/components/states/load-problem";

export type FieldErrors = Partial<Record<"district" | "crop" | "quantity" | "distance", string>>;

interface FieldsProps {
  input: CompareInput;
  update: (patch: Partial<CompareInput>) => void;
  errors: FieldErrors;
}

/** State → District → Locality; the locality is shown but optional (APPROVED PDD-03). */
export function LocationFields({ input, update, errors }: FieldsProps) {
  const { t, locale } = useI18n();
  const states = useQuery("states", () => referenceData.states());
  const districts = useQuery(input.stateId ? `districts:${input.stateId}` : null, () =>
    referenceData.districts(input.stateId as string));
  const localities = useQuery(input.districtId ? `localities:${input.districtId}` : null, () =>
    referenceData.localities(input.districtId as string));

  // Phase 1 covers one state; choose it for the farmer when it is the only one.
  useEffect(() => {
    if (!input.stateId && states.status === "success" && states.data.length === 1) {
      update({ stateId: states.data[0].id });
    }
  }, [input.stateId, states.status, states.data, update]);

  if (states.status === "error") return <LoadProblem error={states.error} onRetry={states.retry} />;
  if (states.status === "success" && states.data.length === 0) {
    return <Notice>{t("input.no_states")}</Notice>;
  }

  const stateOptions = (states.data ?? []).map((state) => {
    const name = localizedName(state, locale);
    return { value: state.id, label: name.text, lang: name.lang };
  });

  return (
    <div className="flex flex-col gap-4">
      <div className="flex flex-col gap-1.5">
        <span className="text-label font-semibold text-ink" id="state-label">{t("input.state")}</span>
        {states.status !== "success" ? (
          <p className="text-label text-ink-2">{t("input.loading")}</p>
        ) : stateOptions.length <= 4 ? (
          <ChipGroup
            label={t("input.state")}
            hideLabel
            options={stateOptions}
            value={input.stateId}
            onChange={(stateId) => update({ stateId, districtId: null, localityId: null })}
          />
        ) : (
          <Select
            aria-labelledby="state-label"
            value={input.stateId ?? ""}
            onChange={(event) => update({ stateId: event.target.value || null, districtId: null, localityId: null })}
          >
            <option value="">{t("input.choose")}</option>
            {stateOptions.map((option) => (
              <option key={option.value} value={option.value} lang={option.lang}>{option.label}</option>
            ))}
          </Select>
        )}
      </div>

      {input.stateId ? (
        districts.status === "error" ? (
          <LoadProblem error={districts.error} onRetry={districts.retry} />
        ) : districts.status === "success" && districts.data.length === 0 ? (
          <Notice>{t("input.no_districts")}</Notice>
        ) : (
          <Field label={t("input.district")} error={errors.district}>
            {({ inputId, describedBy, invalid }) => (
              <Select
                id={inputId}
                aria-describedby={describedBy}
                invalid={invalid}
                disabled={districts.status !== "success"}
                value={input.districtId ?? ""}
                onChange={(event) => update({ districtId: event.target.value || null, localityId: null })}
              >
                <option value="">{districts.status === "success" ? t("input.choose") : t("input.loading")}</option>
                {(districts.data ?? []).map((district) => (
                  <option key={district.id} value={district.id} lang={publishedNameLang(locale)}>{district.name}</option>
                ))}
              </Select>
            )}
          </Field>
        )
      ) : null}

      {input.districtId && localities.status === "success" && localities.data.length > 0 ? (
        <Field label={t("input.locality")} optionalLabel={t("auth.optional")}>
          {({ inputId, describedBy }) => (
            <Select
              id={inputId}
              aria-describedby={describedBy}
              value={input.localityId ?? ""}
              onChange={(event) => update({ localityId: event.target.value || null })}
            >
              <option value="">{t("input.choose")}</option>
              {localities.data.map((locality) => (
                <option key={locality.id} value={locality.id} lang={publishedNameLang(locale)}>{locality.name}</option>
              ))}
            </Select>
          )}
        </Field>
      ) : null}

      <p className="text-caption text-ink-2">{t("input.location_help")}</p>
    </div>
  );
}

const RECENT_CROPS_KEY = "agrinex.recentCrops";
const PINNED_COUNT = 4;

/** Crops chosen before on this device (a per-device convenience; role file 02 S03 "pinned"). */
function readRecentCrops(): string[] {
  try {
    return parseRecentCrops(localStorage.getItem(RECENT_CROPS_KEY));
  } catch {
    return [];
  }
}

function parseRecentCrops(text: string | null): string[] {
  try {
    const value: unknown = JSON.parse(text ?? "[]");
    return Array.isArray(value) ? value.filter((item): item is string => typeof item === "string") : [];
  } catch {
    return [];
  }
}

export function rememberCrop(commodityId: string): void {
  try {
    const next = [commodityId, ...readRecentCrops().filter((id) => id !== commodityId)].slice(0, PINNED_COUNT);
    localStorage.setItem(RECENT_CROPS_KEY, JSON.stringify(next));
  } catch {
    // Storage may be unavailable (private mode); pinning then uses the API order only.
  }
}

/** Searchable crop choice with pinned crops; variety and grade under "More options", default Any. */
export function CropFields({ input, update, errors }: FieldsProps) {
  const { t, locale } = useI18n();
  const commodities = useQuery("commodities", () => referenceData.commodities());
  const [query, setQuery] = useState("");
  const recentText = useStoredText("local", RECENT_CROPS_KEY);
  const recent = useMemo(() => parseRecentCrops(recentText), [recentText]);

  const items = useMemo(() => commodities.data ?? [], [commodities.data]);
  const label = (commodity: CommodityResponse) => localizedName(commodity, locale);

  // "Pinned" = crops chosen before on this device, then the first items in the order the API returns
  // (no popularity data exists in the contract).
  const pinned = useMemo(() => {
    const byId = new Map(items.map((item) => [item.id, item]));
    const chosen = [input.commodityId, ...recent].filter((id): id is string => Boolean(id) && byId.has(id as string));
    const ids = [...new Set([...chosen, ...items.map((item) => item.id)])].slice(0, PINNED_COUNT);
    return ids.map((id) => byId.get(id) as CommodityResponse);
  }, [items, recent, input.commodityId]);

  const matches = useMemo(() => {
    const needle = query.trim().toLowerCase();
    if (!needle) return [];
    return items.filter((item) =>
      [item.name, item.nameMr, item.nameHi].some((name) => name?.toLowerCase().includes(needle))).slice(0, 12);
  }, [items, query]);

  if (commodities.status === "error") return <LoadProblem error={commodities.error} onRetry={commodities.retry} />;
  if (commodities.status === "success" && items.length === 0) return <Notice>{t("input.no_crops")}</Notice>;

  const choose = (commodityId: string) => {
    update({ commodityId, varietyId: null, gradeId: null });
    setQuery("");
  };

  return (
    <div className="flex flex-col gap-4">
      <Field label={t("input.crop")} error={errors.crop}>
        {({ inputId, describedBy, invalid }) => (
          <div className="relative">
            <SearchIcon className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-ink-2" />
            <TextInput
              id={inputId}
              type="search"
              aria-describedby={describedBy}
              invalid={invalid}
              placeholder={t("input.crop_search")}
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              className="pl-10"
              autoComplete="off"
            />
          </div>
        )}
      </Field>

      {query.trim() ? (
        matches.length > 0 ? (
          <ChipGroup
            label={t("input.crop")}
            variant="rows"
            options={matches.map((item) => ({ value: item.id, label: label(item).text, lang: label(item).lang }))}
            value={input.commodityId}
            onChange={choose}
          />
        ) : (
          <p className="text-label text-ink-2" role="status">{t("input.no_match", { q: query.trim() })}</p>
        )
      ) : commodities.status === "success" ? (
        <div className="flex flex-col gap-2">
          <span className="text-label text-ink-2">{t("input.pinned")}</span>
          <ChipGroup
            label={t("input.pinned")}
            hideLabel
            options={pinned.map((item) => ({ value: item.id, label: label(item).text, lang: label(item).lang }))}
            value={input.commodityId}
            onChange={choose}
          />
        </div>
      ) : (
        <p className="text-label text-ink-2">{t("input.loading")}</p>
      )}

      {input.commodityId ? <VarietyGrade input={input} update={update} errors={errors} /> : null}
    </div>
  );
}

function VarietyGrade({ input, update }: FieldsProps) {
  const { t, locale } = useI18n();
  const commodityId = input.commodityId as string;
  const varieties = useQuery(`varieties:${commodityId}`, () => referenceData.varieties(commodityId));
  const grades = useQuery(`grades:${commodityId}`, () => referenceData.grades(commodityId));
  const lang = publishedNameLang(locale);
  // Options appear only once this crop's lists have arrived: the previous crop's must never be offered.
  return (
    <details className="rounded-card border border-rule bg-raised" open={Boolean(input.varietyId || input.gradeId)}>
      <summary className="flex min-h-12 items-center px-4 text-label font-semibold text-net">{t("input.more_options")}</summary>
      <div className="grid gap-3 border-t border-rule px-4 py-3 tablet:grid-cols-2">
        <Field label={t("input.variety")}>
          {({ inputId }) => (
            <Select id={inputId} value={input.varietyId ?? ""} onChange={(event) => update({ varietyId: event.target.value || null })}>
              <option value="">{t("input.any")}</option>
              {(varieties.status === "success" ? varieties.data : []).map((variety) => (
                <option key={variety.id} value={variety.id} lang={lang}>{variety.name}</option>
              ))}
            </Select>
          )}
        </Field>
        <Field label={t("input.grade")}>
          {({ inputId }) => (
            <Select id={inputId} value={input.gradeId ?? ""} onChange={(event) => update({ gradeId: event.target.value || null })}>
              <option value="">{t("input.any")}</option>
              {(grades.status === "success" ? grades.data : []).map((grade) => (
                <option key={grade.id} value={grade.id} lang={lang}>{grade.name}</option>
              ))}
            </Select>
          )}
        </Field>
        <p className="text-caption text-ink-2 tablet:col-span-2">{t("input.any_help")}</p>
      </div>
    </details>
  );
}

/** Quantity with the kg | quintal toggle and the live conversion (role file 02 S03; spec §32). */
export function QuantityField({ input, update, errors }: FieldsProps) {
  const { t, fmt } = useI18n();
  const quintal = toQuintal(input.quantity, input.unit);
  const kg = toKg(input.quantity, input.unit);
  const conversion = input.unit === "KG"
    ? quintal && t("input.equals_quintal", { q: fmt.decimal(quintal, 5) })
    : kg && t("input.equals_kg", { q: fmt.decimal(kg, 3) });
  return (
    <div className="flex flex-col gap-2">
      <Field label={t("input.quantity")} error={errors.quantity}>
        {({ inputId, describedBy, invalid }) => (
          <div className="flex gap-2">
            <TextInput
              id={inputId}
              inputMode="decimal"
              autoComplete="off"
              aria-describedby={describedBy}
              invalid={invalid}
              value={input.quantity}
              onChange={(event) => update({ quantity: event.target.value.replace(/[^\d.]/g, "") })}
              className="figure flex-1 text-right text-heading"
            />
            <Segmented<QuantityUnit>
              label={t("input.unit")}
              value={input.unit}
              onChange={(unit) => update({ unit })}
              options={[
                { value: "KG", label: t("unit.kg") },
                { value: "QUINTAL", label: t("unit.quintal") },
              ]}
            />
          </div>
        )}
      </Field>
      <p className="figure text-label text-ink-2" aria-live="polite">
        {conversion ? `${conversion} (${t("input.quintal_rule")})` : t("input.quintal_rule")}
      </p>
    </div>
  );
}

/** Quick distance chips, default 150 km, or any whole number of kilometres (spec F1.6; role file 02 S03). */
export function DistanceField({ input, update, errors }: FieldsProps) {
  const { t } = useI18n();
  const chip = (DISTANCE_CHOICES as readonly number[]).includes(input.maxDistanceKm);
  const [other, setOther] = useState(chip ? "" : String(input.maxDistanceKm));
  return (
    <div className="flex flex-col gap-3">
      <span className="text-label font-semibold text-ink">{t("input.distance")}</span>
      <ChipGroup
        label={t("input.distance")}
        hideLabel
        options={DISTANCE_CHOICES.map((km) => ({ value: km, label: t("input.km_choice", { n: km }) }))}
        value={chip ? input.maxDistanceKm : null}
        onChange={(km) => {
          setOther("");
          update({ maxDistanceKm: km });
        }}
      />
      <Field label={t("input.other_km")} error={errors.distance}>
        {({ inputId, describedBy, invalid }) => (
          <TextInput
            id={inputId}
            inputMode="numeric"
            aria-describedby={describedBy}
            invalid={invalid}
            value={other}
            onChange={(event) => {
              const digits = event.target.value.replace(/\D/g, "").slice(0, 5);
              setOther(digits);
              const km = Number(digits);
              if (digits && km > 0) update({ maxDistanceKm: km });
            }}
            className="figure max-w-40"
          />
        )}
      </Field>
      <p className="text-caption text-ink-2">{t("input.distance_help")}</p>
    </div>
  );
}

/** Client-side checks with the server's own rules; the server stays the authority (E09). */
export function validateInput(input: CompareInput, t: Translate): FieldErrors {
  const errors: FieldErrors = {};
  if (!input.districtId) errors.district = t("input.required_district");
  if (!input.commodityId) errors.crop = t("input.required_crop");
  const quantity = quantityProblem(input.quantity);
  if (quantity) errors.quantity = t(`err.quantity.${quantity}`);
  if (!Number.isInteger(input.maxDistanceKm) || input.maxDistanceKm <= 0) errors.distance = t("input.km_invalid");
  return errors;
}
