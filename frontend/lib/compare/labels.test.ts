/** Recommendation labels and trade-off wording (ADR-0045) on SYNTHETIC mock responses. */
import { describe, expect, it } from "vitest";
import compareMock from "@/__mocks__/api/selling-options-compare.200-recommendation.json";
import simulateMock from "@/__mocks__/api/selling-options-simulate.200.json";
import type { MarketComparisonResponse, SellingOptionResponse } from "@/lib/api/types";
import { labelsFor, tradeOffOf } from "./labels";

const base = compareMock.response.body as unknown as MarketComparisonResponse;
const byName = (response: MarketComparisonResponse, name: string) =>
  response.options.find((option) => option.market.name === name) as SellingOptionResponse;

describe("labelsFor", () => {
  it("labels rank 1 Recommended, the price winner Highest price, and the maximum estimate", () => {
    expect(labelsFor(base, byName(base, "TEST Market C"))).toEqual({
      recommended: true,
      whatIfTop: false,
      highestPrice: false,
      highestNet: true,
    });
    expect(labelsFor(base, byName(base, "TEST Market B"))).toEqual({
      recommended: false,
      whatIfTop: false,
      highestPrice: true,
      highestNet: false,
    });
  });

  it("never calls a simulation's rank 1 Recommended (ADR-0045 item 6)", () => {
    const simulation = simulateMock.response.body as unknown as MarketComparisonResponse;
    const top = simulation.options[0];
    const labels = labelsFor(simulation, top);
    expect(labels.recommended).toBe(false);
    expect(labels.whatIfTop).toBe(true);
  });

  it("puts Highest estimated net realization on every option tied at the maximum", () => {
    const tied: MarketComparisonResponse = {
      ...base,
      options: base.options.map((option) => ({
        ...option,
        estimates: { ...option.estimates, estimatedNetRealization: 14664 },
      })),
    };
    expect(tied.options.every((option) => labelsFor(tied, option).highestNet)).toBe(true);
  });
});

describe("tradeOffOf", () => {
  it("says R may leave more when its estimate is higher: +₹500 · −₹688 = ₹188 less at B", () => {
    const tradeOff = tradeOffOf(base);
    expect(tradeOff?.kind).toBe("more");
    expect(tradeOff?.priceWinner.market.name).toBe("TEST Market B");
    expect(tradeOff?.rankOne.market.name).toBe("TEST Market C");
    expect(tradeOff?.grossDiff.toFixed(0)).toBe("500");
    expect(tradeOff?.transportDiff.toFixed(0)).toBe("688");
    expect(tradeOff?.netDiff.toFixed(0)).toBe("188");
    expect(tradeOff?.showArithmetic).toBe(true);
  });

  it("never claims R leaves more when P's estimate is higher, and names P's cautions", () => {
    const pHigher: MarketComparisonResponse = {
      ...base,
      options: base.options.map((option) =>
        option.market.name === "TEST Market B"
          ? { ...option, estimates: { ...option.estimates, estimatedNetRealization: 14800 } }
          : option),
    };
    const tradeOff = tradeOffOf(pHigher);
    expect(tradeOff?.kind).toBe("notMore");
    expect(tradeOff?.netDiff.toFixed(0)).toBe("-136");
    expect(tradeOff?.cautions).toEqual(["NEGATIVE_TREND"]);
  });

  it("uses the equal wording when both estimates are the same", () => {
    const equal: MarketComparisonResponse = {
      ...base,
      options: base.options.map((option) =>
        option.market.name === "TEST Market B"
          ? { ...option, estimates: { ...option.estimates, estimatedNetRealization: 14664 } }
          : option),
    };
    expect(tradeOffOf(equal)?.kind).toBe("equal");
  });

  it("is absent when the rankings agree", () => {
    expect(tradeOffOf({ ...base, rankingsDisagree: false })).toBeNull();
  });
});
