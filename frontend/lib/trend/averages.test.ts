/** Trend window averages on the SYNTHETIC trend mocks (__mocks__/api/markets-trends.*.json). */
import { describe, expect, it } from "vitest";
import insufficientMock from "@/__mocks__/api/markets-trends.200-insufficient-data.json";
import trendMock from "@/__mocks__/api/markets-trends.200.json";
import type { TrendResponse } from "@/lib/api/types";
import { windowAverages } from "./averages";

const trend = trendMock.response.body as unknown as TrendResponse;

describe("windowAverages", () => {
  it("reproduces the documented means: 3,101.85 against 3,390.00 for −8.5 %", () => {
    const averages = windowAverages(trend);
    expect(averages?.recent.toFixed(2)).toBe("3101.85");
    expect(averages?.baseline.toFixed(2)).toBe("3390.00");
  });

  it("shows nothing for insufficient data", () => {
    expect(windowAverages(insufficientMock.response.body as unknown as TrendResponse)).toBeNull();
  });

  it("shows nothing when the derived averages would not reproduce the server's change", () => {
    expect(windowAverages({ ...trend, changePercent7Day: -3.2 })).toBeNull();
  });

  it("shows nothing when the chart does not cover the baseline window", () => {
    expect(windowAverages({ ...trend, window: { ...trend.window, chartFrom: trend.window.recentFrom } })).toBeNull();
  });
});
