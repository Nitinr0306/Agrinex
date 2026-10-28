/**
 * Reference data (geography and commodities), fetched once per page session and shared by every screen. The
 * district centroids come from here: the comparison origin is copied from `listDistricts` unchanged (ADR-0039).
 * A 400 on the origin means the cached geography is out of date, so it can be dropped (role file 02 E10).
 */
import { referenceApi } from "@/lib/api/endpoints";
import type {
  CommodityResponse,
  DistrictResponse,
  GradeResponse,
  LocalityResponse,
  StateResponse,
  VarietyResponse,
} from "@/lib/api/types";

const cache = new Map<string, Promise<unknown>>();

function cached<T>(key: string, load: () => Promise<T>): Promise<T> {
  const existing = cache.get(key) as Promise<T> | undefined;
  if (existing) return existing;
  const promise = load().catch((error: unknown) => {
    cache.delete(key);
    throw error;
  });
  cache.set(key, promise);
  return promise;
}

export const referenceData = {
  states: (): Promise<StateResponse[]> => cached("states", () => referenceApi.listStates().then((page) => page.items)),
  districts: (stateId: string): Promise<DistrictResponse[]> =>
    cached(`districts:${stateId}`, () => referenceApi.listDistricts(stateId).then((page) => page.items)),
  localities: (districtId: string): Promise<LocalityResponse[]> =>
    cached(`localities:${districtId}`, () => referenceApi.listLocalities(districtId).then((page) => page.items)),
  commodities: (): Promise<CommodityResponse[]> =>
    cached("commodities", () => referenceApi.listCommodities().then((page) => page.items)),
  varieties: (commodityId: string): Promise<VarietyResponse[]> =>
    cached(`varieties:${commodityId}`, () => referenceApi.listVarieties(commodityId).then((page) => page.items)),
  grades: (commodityId: string): Promise<GradeResponse[]> =>
    cached(`grades:${commodityId}`, () => referenceApi.listGrades(commodityId).then((page) => page.items)),
};

/** Drops cached geography (E10: the server rejected an origin, so the published centroids changed). */
export function forgetGeography(): void {
  for (const key of [...cache.keys()]) {
    if (key === "states" || key.startsWith("districts:") || key.startsWith("localities:")) cache.delete(key);
  }
}

/** Drops cached commodity lists (E08: a referenced crop, variety or grade is no longer available). */
export function forgetCommodities(): void {
  for (const key of [...cache.keys()]) {
    if (key === "commodities" || key.startsWith("varieties:") || key.startsWith("grades:")) cache.delete(key);
  }
}
