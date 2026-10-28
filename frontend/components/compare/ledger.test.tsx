/**
 * The transport working line names the rate actually applied (ADR-0045; role file 02 S04): the comparison's
 * appliedRatePerKm, or the hypothetical What-If rate, and the minimum charge when it applies. Expected texts are
 * hand-computed from the synthetic comparison (500 kg; ₹8/km; minimum ₹300; capacity 1,000 kg).
 */
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { cleanup, render } from "@testing-library/react";
import { afterEach, describe, expect, it } from "vitest";
import compareMock from "@/__mocks__/api/selling-options-compare.200-recommendation.json";
import type { MarketComparisonResponse } from "@/lib/api/types";
import { basisFromResponse, basisFromWhatIf, optionsFromResponse, optionsFromWhatIf, type OptionView, type TransportBasis } from "@/lib/compare/view-model";
import { recalculate } from "@/lib/compare/whatif";
import { Decimal } from "@/lib/decimal";
import type { Locale } from "@/lib/i18n/config";
import { I18nProvider } from "@/lib/i18n/i18n-provider";
import type { Messages } from "@/lib/i18n/messages";
import { TransportWorking, optionLinks } from "./ledger";

const base = compareMock.response.body as unknown as MarketComparisonResponse;
const messages = (locale: Locale) =>
  JSON.parse(readFileSync(join(process.cwd(), "public", "locales", `${locale}.json`), "utf8")) as Messages;

function working(view: OptionView, basis: TransportBasis, locale: Locale = "en"): string {
  const { container } = render(
    <I18nProvider locale={locale} messages={messages(locale)}>
      <TransportWorking view={view} basis={basis} />
    </I18nProvider>,
  );
  return (container.textContent ?? "").replace(/\s+/g, " ").trim();
}

const byMarket = (views: OptionView[], name: string) => views.find((view) => view.option.market.name === name) as OptionView;

afterEach(cleanup);

describe("TransportWorking", () => {
  it("shows the applied rate per km when distance × rate is above the minimum (42 km × ₹8 = ₹336)", () => {
    expect(working(byMarket(optionsFromResponse(base), "TEST Market C"), basisFromResponse(base)))
      .toBe("≈approximately 42 km × ₹8/km × 1 trip");
  });

  it("shows the minimum charge when it applies (15 km × ₹8 = ₹120 < ₹300)", () => {
    expect(working(byMarket(optionsFromResponse(base), "TEST Market A"), basisFromResponse(base)))
      .toBe("minimum charge ₹300 per trip × 1 trip");
  });

  it("shows the hypothetical What-If rate, not the configured one (42 km × ₹12.5 = ₹525)", () => {
    const inputs = { quantityKg: Decimal.of("500"), ratePerKm: Decimal.of("12.5"), maxDistanceKm: base.maxDistanceKm };
    const views = optionsFromWhatIf(recalculate(base, inputs), base);
    expect(working(byMarket(views, "TEST Market C"), basisFromWhatIf(base, inputs))).toBe("≈approximately 42 km × ₹12.5/km × 1 trip");
  });

  it("counts trips from the vehicle capacity (2,500 kg on 1,000 kg vehicles = 3 trips)", () => {
    const inputs = { quantityKg: Decimal.of("2500"), ratePerKm: Decimal.of("8"), maxDistanceKm: base.maxDistanceKm };
    const views = optionsFromWhatIf(recalculate(base, inputs), base);
    expect(working(byMarket(views, "TEST Market C"), basisFromWhatIf(base, inputs))).toBe("≈approximately 42 km × ₹8/km × 3 trips");
  });

  it("keeps Latin digits in Marathi (PDD-06)", () => {
    const text = working(byMarket(optionsFromResponse(base), "TEST Market C"), basisFromResponse(base), "mr");
    expect(text).toContain("42");
    expect(text).toContain("₹8");
    expect(text).not.toMatch(/[०-९]/);
  });
});

/**
 * The trend link opens the series the card's trend was computed on: the comparison's own variety and grade
 * filter (contract: SellingOptionResponse.trendDirection), never the variety of the option's representative
 * observation. Found in Task 12 on real data, where the two series disagreed (e.g. STABLE -0.2 % vs RISING 7.5 %).
 */
describe("optionLinks", () => {
  const withVariety = base.options.find((option) => option.price.variety) as MarketComparisonResponse["options"][number];

  it("opens the unfiltered trend for an unfiltered comparison, whatever variety the observation has", () => {
    expect(base.variety).toBeNull();
    const view = optionsFromResponse(base).find((candidate) => candidate.option === withVariety) as OptionView;
    const trend = new URL(optionLinks(view, "commodity-1").trend, "https://agrinex.invalid");
    expect([...trend.searchParams.keys()]).toEqual(["crop"]);
  });

  it("keeps the comparison's variety and grade filter, also on a What-If", () => {
    const filtered = { ...base, variety: { id: "variety-1", name: "Synthetic Variety" }, grade: { id: "grade-1", name: "Synthetic Grade" } } as MarketComparisonResponse;
    const inputs = { quantityKg: Decimal.of("500"), ratePerKm: Decimal.of("8"), maxDistanceKm: base.maxDistanceKm };
    for (const view of [optionsFromResponse(filtered)[0], optionsFromWhatIf(recalculate(filtered, inputs), filtered)[0]]) {
      const trend = new URL(optionLinks(view, "commodity-1").trend, "https://agrinex.invalid");
      expect(Object.fromEntries(trend.searchParams)).toEqual({ crop: "commodity-1", variety: "variety-1", grade: "grade-1" });
    }
  });
});
