/**
 * The answers summary of the comparison input repeats the farmer's own answers (role file 02 S03): the quantity
 * stays in the unit that was entered. Found in Task 12: "20 quintal" was shown as "2,000 kg".
 */
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { cleanup, render } from "@testing-library/react";
import { afterEach, describe, expect, it } from "vitest";
import { emptyInput, type CompareInput } from "@/lib/compare/input";
import { I18nProvider } from "@/lib/i18n/i18n-provider";
import type { Messages } from "@/lib/i18n/messages";
import { AnswerSummary } from "./input-forms";

const en = JSON.parse(readFileSync(join(process.cwd(), "public", "locales", "en.json"), "utf8")) as Messages;

function summaryText(input: CompareInput): string {
  const { container } = render(
    <I18nProvider locale="en" messages={en}>
      <AnswerSummary input={input} names={{}} upTo={4} onChange={() => undefined} />
    </I18nProvider>,
  );
  return (container.textContent ?? "").replace(/\s+/g, " ");
}

afterEach(cleanup);

describe("AnswerSummary", () => {
  it("shows a quantity entered in quintals in quintals", () => {
    expect(summaryText({ ...emptyInput(), quantity: "20", unit: "QUINTAL" })).toContain("Quantity20 quintal");
  });

  it("shows a quantity entered in kilograms in kilograms", () => {
    expect(summaryText({ ...emptyInput(), quantity: "450", unit: "KG" })).toContain("Quantity450 kg");
  });

  it("shows a dash while the quantity is not valid", () => {
    expect(summaryText({ ...emptyInput(), quantity: "", unit: "KG" })).toContain("Quantity—");
  });
});
