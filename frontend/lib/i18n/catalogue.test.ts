// @vitest-environment node
/**
 * The three message catalogues (spec F1.21, §46; role file 02 §10) stay complete and consistent: the same keys,
 * the same {placeholders} and no empty text. The product name is AgriNex only (spec header; CLAUDE.md §A).
 */
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { describe, expect, it } from "vitest";
import { LOCALES } from "./config";

const catalogue = (locale: string) =>
  JSON.parse(readFileSync(join(process.cwd(), "public", "locales", `${locale}.json`), "utf8")) as Record<string, string>;
const placeholders = (text: string) => [...text.matchAll(/\{(\w+)\}/g)].map((match) => match[1]).sort();

const english = catalogue("en");

describe.each(LOCALES)("%s catalogue", (locale) => {
  const messages = catalogue(locale);

  it("has exactly the English keys", () => {
    expect(Object.keys(messages).sort()).toEqual(Object.keys(english).sort());
  });

  it("keeps every placeholder of the English text", () => {
    const mismatched = Object.keys(english).filter((key) =>
      JSON.stringify(placeholders(messages[key] ?? "")) !== JSON.stringify(placeholders(english[key])));
    expect(mismatched).toEqual([]);
  });

  it("has no empty or untrimmed text", () => {
    expect(Object.entries(messages).filter(([, text]) => typeof text !== "string" || text.trim() === "" || text !== text.trim()).map(([key]) => key)).toEqual([]);
  });

  it("never names a retired product or the internal design name", () => {
    expect(Object.entries(messages).filter(([, text]) => /mandiwise|krishisetu|hisaab/i.test(text)).map(([key]) => key)).toEqual([]);
  });
});
