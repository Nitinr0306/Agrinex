import { readFileSync } from "node:fs";
import { join } from "node:path";
import { expect, test } from "@playwright/test";
import { expectNoHorizontalScroll, runComparison, switchLanguage } from "./support";

/**
 * Marathi and Hindi (spec F1.2, §27; ADR-0046) and the 375 px phone layout (spec §48) in a real browser, on real
 * data. Expected texts come from the shipped catalogues, so the test follows any approved copy change.
 */
type Catalogue = Record<string, string>;
const catalogue = (locale: string) =>
  JSON.parse(readFileSync(join(__dirname, "..", "public", "locales", `${locale}.json`), "utf8")) as Catalogue;

for (const [locale, button] of [["mr", "मराठी"], ["hi", "हिंदी"]] as const) {
  test(`${locale}: the language switch translates the farmer screens and keeps the results`, async ({ page }) => {
    const text = catalogue(locale);
    await runComparison(page, { district: "Nashik", crop: "Onion", quantity: "20", unit: "quintal" });
    await switchLanguage(page, button);

    await expect(page.locator("html")).toHaveAttribute("lang", locale);
    await expect(page.getByRole("heading", { level: 2, name: text["results.markets_heading"] })).toBeVisible();
    await expect(page.getByRole("heading", { level: 2, name: new RegExp(`^${text["answer.title"].replace("{market}", "")}`) }))
      .toBeVisible();
    // Latin digits in every language (role file 02 PDD-06).
    await expect(page.getByRole("main")).not.toContainText(/[०-९]/);

    await page.goto("/");
    await expect(page.getByRole("heading", { level: 1, name: text["home.title"] })).toBeVisible();
    await page.goto("/alerts/new");
    await expect(page.getByRole("heading", { level: 1, name: text["alerts.new"] })).toBeVisible();

    await page.context().clearCookies({ name: "agrinex_locale" });
  });
}

test.describe("phone layout at 375 px", () => {
  test.use({ viewport: { width: 375, height: 812 } });

  test("no screen of the farmer journey scrolls sideways", async ({ page }) => {
    for (const path of ["/", "/compare", "/alerts/new", "/sign-in?tab=create", "/forgot-password"]) {
      await page.goto(path);
      await expect(page.getByRole("heading", { level: 1 })).toBeVisible();
      await expectNoHorizontalScroll(page);
    }
    await runComparison(page, { district: "Pune", crop: "Onion", quantity: "700", unit: "kg" });
    await expectNoHorizontalScroll(page);
    for (const [locale, button] of [["mr", "मराठी"], ["hi", "हिंदी"]] as const) {
      await switchLanguage(page, button);
      await expect(page.locator("html")).toHaveAttribute("lang", locale);
      await expectNoHorizontalScroll(page);
    }
  });
});
