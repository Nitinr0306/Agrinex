/**
 * The client What-If on the SYNTHETIC hero comparison (__mocks__/api/selling-options-compare.200-recommendation.json),
 * against hand-computed values (spec §32, §48):
 *   500 kg, ₹8/km, minimum ₹300 per trip, vehicle 1,000 kg → 1 trip
 *   C 42 km  ₹3,000/q: max(300, 336) = 336;   gross 15,000; net 14,664
 *   B 128 km ₹3,100/q: max(300, 1,024) = 1,024; gross 15,500; net 14,476
 *   A 15 km  ₹2,800/q: max(300, 120) = 300;   gross 14,000; net 13,700
 */
import { describe, expect, it } from "vitest";
import compareMock from "@/__mocks__/api/selling-options-compare.200-recommendation.json";
import simulateQuantityMock from "@/__mocks__/api/selling-options-simulate.200-quantity-override.json";
import type { MarketComparisonResponse } from "@/lib/api/types";
import { Decimal } from "@/lib/decimal";
import { NO_OVERRIDES, buildSimulateRequest, originalInputs, recalculate } from "./whatif";

const base = compareMock.response.body as unknown as MarketComparisonResponse;
const names = (result: ReturnType<typeof recalculate>) => result.options.map((entry) => entry.option.market.name);
const money = (value: Decimal) => value.toFixed(2);

describe("recalculate", () => {
  it("reproduces the server's estimates exactly with the comparison's own inputs (ADR-0026 item 1)", () => {
    const result = recalculate(base, originalInputs(base));
    for (const entry of result.options) {
      expect(money(entry.transport)).toBe(entry.option.estimates.estimatedTransportCost.toFixed(2));
      expect(money(entry.gross)).toBe(entry.option.estimates.estimatedGrossValue.toFixed(2));
      expect(money(entry.net)).toBe(entry.option.estimates.estimatedNetRealization.toFixed(2));
      expect(entry.trips).toBe(BigInt(entry.option.estimates.trips));
    }
  });

  it("ranks by estimated net realization only and finds the price winner", () => {
    const result = recalculate(base, originalInputs(base));
    expect(names(result)).toEqual(["TEST Market C", "TEST Market B", "TEST Market A"]);
    expect(result.options.map((entry) => money(entry.net))).toEqual(["14664.00", "14476.00", "13700.00"]);
    expect(result.priceWinnerId).toBe("00000000-0000-4000-8000-000000000702");
    expect(result.rankingsDisagree).toBe(true);
  });

  it("re-evaluates only PRICE_ADVANTAGE and LOWER_TRANSPORT over the kept options", () => {
    const result = recalculate(base, originalInputs(base));
    const codes = Object.fromEntries(result.options.map((entry) => [entry.option.market.name, entry.reasonCodes]));
    expect(codes["TEST Market B"]).toEqual(["PRICE_ADVANTAGE", "RECENT_DATA", "NEGATIVE_TREND"]);
    expect(codes["TEST Market A"]).toEqual(["LOWER_TRANSPORT", "STABLE_TREND", "SUITABLE_DISTANCE", "STALE_DATA"]);
    expect(codes["TEST Market C"]).toEqual(["POSITIVE_TREND", "RECENT_DATA", "SUITABLE_DISTANCE"]);
  });

  it("applies a hypothetical rate: ₹12/km gives C 504 and B 1,536", () => {
    const result = recalculate(base, { ...originalInputs(base), ratePerKm: Decimal.of(12) });
    expect(result.options.map((entry) => [entry.option.market.name, money(entry.transport), money(entry.net)])).toEqual([
      ["TEST Market C", "504.00", "14496.00"],
      ["TEST Market B", "1536.00", "13964.00"],
      ["TEST Market A", "300.00", "13700.00"],
    ]);
  });

  it("states the break-even rate of the role file example: below ₹6.25/km", () => {
    const result = recalculate(base, originalInputs(base));
    expect(result.breakeven?.ratePerKm.toFixed(2)).toBe("6.25");
    expect(result.breakeven?.priceWinner.market.name).toBe("TEST Market B");
    expect(result.breakeven?.rankOne.market.name).toBe("TEST Market C");
  });

  it("re-ranks for a smaller quantity: 150 kg puts A second and lowers the break-even to ₹3.51/km", () => {
    const result = recalculate(base, { ...originalInputs(base), quantityKg: Decimal.of(150) });
    expect(names(result)).toEqual(["TEST Market C", "TEST Market A", "TEST Market B"]);
    expect(result.options.map((entry) => money(entry.net))).toEqual(["4164.00", "3900.00", "3626.00"]);
    // (150 + 300) / 128 = 3.515625, floored to paise.
    expect(result.breakeven?.ratePerKm.toFixed(2)).toBe("3.51");
  });

  it("agrees with the server's arithmetic of a simulation response (server mode, 150 kg)", () => {
    const simulated = simulateQuantityMock.response.body as unknown as MarketComparisonResponse;
    const result = recalculate(simulated, originalInputs(simulated));
    expect(result.options).toHaveLength(simulated.options.length);
    for (const option of simulated.options) {
      const mine = result.options.find((entry) => entry.option.market.id === option.market.id);
      expect(mine && money(mine.transport)).toBe(option.estimates.estimatedTransportCost.toFixed(2));
      expect(mine && money(mine.gross)).toBe(option.estimates.estimatedGrossValue.toFixed(2));
      expect(mine && money(mine.net)).toBe(option.estimates.estimatedNetRealization.toFixed(2));
    }
  });

  it("filters by a smaller radius and keeps the excluded markets inside it", () => {
    const result = recalculate(base, { ...originalInputs(base), maxDistanceKm: 100 });
    expect(names(result)).toEqual(["TEST Market C", "TEST Market A"]);
    expect(result.excluded.map((market) => market.market.name)).toEqual(["TEST Market D"]);
    expect(result.rankingsDisagree).toBe(false);
    expect(result.breakeven).toBeNull();
  });

  it("returns the server's empty states for a radius with no ranked option (ADR-0023 item 3)", () => {
    expect(recalculate(base, { ...originalInputs(base), maxDistanceKm: 10 }).state).toBe("noMarketInRange");
    const onlyExcluded: MarketComparisonResponse = {
      ...base,
      options: base.options.filter((option) => option.distanceKm > 100),
    };
    expect(recalculate(onlyExcluded, { ...originalInputs(base), maxDistanceKm: 100 }).state).toBe("noRecentData");
  });
});

describe("buildSimulateRequest", () => {
  // The synthetic comparison: 500 kg, ₹8/km applied, 150 km radius.
  it("sends only the raised radius when nothing else moved", () => {
    expect(buildSimulateRequest(base, NO_OVERRIDES, 200)).toEqual({ recommendationId: base.recommendationId, maxDistanceKm: 200 });
  });

  it("adds a moved rate and a moved quantity, the quantity in kilograms", () => {
    const request = buildSimulateRequest(base, { ratePerKm: Decimal.of("12.5"), quantityKg: Decimal.of("750"), maxDistanceKm: 200 }, 200);
    expect(request).toEqual({ recommendationId: base.recommendationId, maxDistanceKm: 200, quantity: 750, transportRatePerKm: 12.5 });
  });

  it("omits a slider moved back to the comparison's own value", () => {
    const request = buildSimulateRequest(base, { ratePerKm: Decimal.of("8.00"), quantityKg: Decimal.of("500"), maxDistanceKm: 200 }, 200);
    expect(request).toEqual({ recommendationId: base.recommendationId, maxDistanceKm: 200 });
  });
});
