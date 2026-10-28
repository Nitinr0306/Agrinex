/** E03 (role file 02; ADR-0052 item 8): the stale-prices note appears only when every price shown is STALE. Data is synthetic. */
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { cleanup, render } from "@testing-library/react";
import { afterEach, describe, expect, it } from "vitest";
import compareMock from "@/__mocks__/api/selling-options-compare.200-recommendation.json";
import type { MarketComparisonResponse } from "@/lib/api/types";
import { optionsFromResponse } from "@/lib/compare/view-model";
import { I18nProvider } from "@/lib/i18n/i18n-provider";
import type { Messages } from "@/lib/i18n/messages";
import { StaleNote } from "./summary";

const en = JSON.parse(readFileSync(join(process.cwd(), "public", "locales", "en.json"), "utf8")) as Messages & Record<string, string>;
const base = compareMock.response.body as unknown as MarketComparisonResponse;

function withFreshness(statuses: string[]): MarketComparisonResponse {
  return { ...base, options: base.options.map((option, i) => ({ ...option, price: { ...option.price, freshnessStatus: statuses[i] } })) } as MarketComparisonResponse;
}

function noteShown(response: MarketComparisonResponse): boolean {
  const { container } = render(<I18nProvider locale="en" messages={en}><StaleNote views={optionsFromResponse(response)} /></I18nProvider>);
  return (container.textContent ?? "").includes(en["state.stale_note"]);
}

afterEach(cleanup);

describe("StaleNote", () => {
  it("is shown when every price is stale", () => {
    expect(noteShown(withFreshness(base.options.map(() => "STALE")))).toBe(true);
  });

  it("is not shown when only some prices are stale", () => {
    expect(noteShown(withFreshness(base.options.map((_, i) => (i === 0 ? "FRESH" : "STALE"))))).toBe(false);
  });

  it("is not shown when every price is fresh or recent", () => {
    expect(noteShown(withFreshness(base.options.map((_, i) => (i % 2 === 0 ? "FRESH" : "RECENT"))))).toBe(false);
  });

  it("is not shown without options", () => {
    expect(noteShown({ ...base, options: [] } as MarketComparisonResponse)).toBe(false);
  });
});
