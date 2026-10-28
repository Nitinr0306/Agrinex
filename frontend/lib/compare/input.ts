/**
 * The comparison form (role file 02 S03; contract CompareRequest; ADR-0023). Quantity is kept as typed until the
 * request is built; the origin is always the selected district's published centroid (ADR-0039).
 */
import type { CompareRequest, DistrictResponse, QuantityUnit } from "@/lib/api/types";
import { Decimal } from "@/lib/decimal";

/** Default search radius (spec F1.6; role file 02 S03). */
export const DEFAULT_MAX_DISTANCE_KM = 150;
/** Quick distance choices of the approved input design. */
export const DISTANCE_CHOICES = [50, 100, 150, 200, 300] as const;
/** CompareRequest.quantity: > 0, at most 3 decimal places, at most 999,999,999.999 (contract). */
const QUANTITY_MAX = Decimal.of("999999999.999");

export interface CompareInput {
  stateId: string | null;
  districtId: string | null;
  localityId: string | null;
  commodityId: string | null;
  varietyId: string | null;
  gradeId: string | null;
  quantity: string;
  unit: QuantityUnit;
  maxDistanceKm: number;
}

export function emptyInput(): CompareInput {
  return {
    stateId: null,
    districtId: null,
    localityId: null,
    commodityId: null,
    varietyId: null,
    gradeId: null,
    quantity: "",
    unit: "KG",
    maxDistanceKm: DEFAULT_MAX_DISTANCE_KM,
  };
}

export type QuantityProblem = "REQUIRED" | "OUT_OF_RANGE" | "PRECISION_EXCEEDED";

/** The same rules the server applies (contract CompareRequest.quantity), checked before sending. */
export function quantityProblem(text: string): QuantityProblem | null {
  const trimmed = text.trim();
  if (trimmed === "") return "REQUIRED";
  if (!/^\d+(\.\d+)?$/.test(trimmed)) return "OUT_OF_RANGE";
  const [, fraction = ""] = trimmed.split(".");
  if (fraction.length > 3) return "PRECISION_EXCEEDED";
  const value = Decimal.of(trimmed);
  if (!value.isPositive() || value.gt(QUANTITY_MAX)) return "OUT_OF_RANGE";
  return null;
}

/** Quantity in quintal for the live conversion "= 5 quintal" (spec §32: 1 quintal = 100 kg). */
export function toQuintal(text: string, unit: QuantityUnit): Decimal | null {
  if (quantityProblem(text) !== null) return null;
  const value = Decimal.of(text.trim());
  return unit === "QUINTAL" ? value : value.mul(Decimal.of("0.01"));
}

/** Quantity in kg, for the What-If and the conversion line. */
export function toKg(text: string, unit: QuantityUnit): Decimal | null {
  if (quantityProblem(text) !== null) return null;
  const value = Decimal.of(text.trim());
  return unit === "KG" ? value : value.mul(Decimal.ofInteger(100));
}

export function isComplete(input: CompareInput): boolean {
  return input.districtId !== null && input.commodityId !== null && quantityProblem(input.quantity) === null
    && input.maxDistanceKm > 0;
}

/**
 * Builds the request. The origin is the district centroid exactly as `listDistricts` published it, never a
 * device location or an address (spec §28; ADR-0039). The locality is not part of the request (PDD-03).
 */
export function toCompareRequest(input: CompareInput, district: DistrictResponse): CompareRequest {
  if (input.commodityId === null || quantityProblem(input.quantity) !== null) {
    throw new Error("Incomplete comparison input");
  }
  return {
    commodityId: input.commodityId,
    varietyId: input.varietyId,
    gradeId: input.gradeId,
    quantity: Number(input.quantity.trim()),
    quantityUnit: input.unit,
    originLat: district.centroid.lat,
    originLon: district.centroid.lon,
    maxDistanceKm: input.maxDistanceKm,
  };
}

const PARAMS = {
  state: "stateId",
  district: "districtId",
  locality: "localityId",
  crop: "commodityId",
  variety: "varietyId",
  grade: "gradeId",
} as const;

/** Prefill from a link (for example "Compare markets now" on an alert event names the crop). */
export function inputFromSearch(search: URLSearchParams, base: CompareInput = emptyInput()): CompareInput {
  const next: CompareInput = { ...base };
  for (const [param, field] of Object.entries(PARAMS)) {
    const value = search.get(param);
    if (value && /^[0-9a-fA-F-]{36}$/.test(value)) next[field] = value;
  }
  const quantity = search.get("qty");
  if (quantity && quantityProblem(quantity) === null) next.quantity = quantity;
  const unit = search.get("unit");
  if (unit === "KG" || unit === "QUINTAL") next.unit = unit;
  const km = Number(search.get("km"));
  if (Number.isInteger(km) && km > 0) next.maxDistanceKm = km;
  return next;
}
