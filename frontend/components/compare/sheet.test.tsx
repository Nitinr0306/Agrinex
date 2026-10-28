/**
 * The comparison table's header structure (WCAG 1.3.1; axe rule td-has-header, found by Lighthouse in Task 12 on
 * the real results page): every visible data cell has a column header (the market) and a row header (the figure),
 * and the corner cell is the header of the row-header column, not a data cell. Rendered from the synthetic
 * contract mock.
 */
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { cleanup, render } from "@testing-library/react";
import { afterEach, expect, it } from "vitest";
import compareMock from "@/__mocks__/api/selling-options-compare.200-recommendation.json";
import type { MarketComparisonResponse } from "@/lib/api/types";
import { basisFromResponse, optionsFromResponse } from "@/lib/compare/view-model";
import { Decimal } from "@/lib/decimal";
import { I18nProvider } from "@/lib/i18n/i18n-provider";
import type { Messages } from "@/lib/i18n/messages";
import { ComparisonSheet } from "./sheet";

const base = compareMock.response.body as unknown as MarketComparisonResponse;
const messages = JSON.parse(readFileSync(join(process.cwd(), "public", "locales", "en.json"), "utf8")) as Messages;

afterEach(cleanup);

it("gives every visible data cell a column header and a row header", () => {
  const views = optionsFromResponse(base);
  const { container } = render(
    <I18nProvider locale="en" messages={messages}>
      <ComparisonSheet views={views} basis={basisFromResponse(base)} maxTransport={Decimal.of("1000")} commodityId="commodity-1" />
    </I18nProvider>,
  );
  const table = container.querySelector("table") as HTMLTableElement;
  expect(table.querySelectorAll("thead td")).toHaveLength(0);
  const columnHeaders = table.querySelectorAll("thead th[scope='col']");
  expect(columnHeaders).toHaveLength(views.length + 1);

  const rows = Array.from(table.querySelectorAll("tbody tr"));
  expect(rows.length).toBeGreaterThan(0);
  for (const row of rows) {
    expect(row.querySelector("th[scope='row']")?.textContent).toBeTruthy();
    expect(row.querySelectorAll("td")).toHaveLength(views.length);
  }
});
