import { expect, test } from "@playwright/test";
import { byName, compareViaApi, estimatedRupees, reference, runComparison } from "./support";

/**
 * The farmer journey without an account (spec F1.3-F1.17; role file 02 S01-S06): compare markets for real
 * ingested prices, read why the recommendation wins, open a market's price trend and try different numbers. Every
 * figure checked on screen is taken from the same backend's API for the same inputs.
 */
test.describe("Farmer comparison on real data", () => {
  test("the recommendation and the ranking match the comparison API", async ({ page, request }) => {
    const ref = await reference(request);
    const district = byName(ref.districts, "Nashik");
    const onion = byName(ref.commodities, "Onion");
    const api = await compareViaApi(request, {
      commodityId: onion.id, quantity: 20, quantityUnit: "QUINTAL",
      originLat: district.centroid.lat, originLon: district.centroid.lon,
    });
    expect(api.options.length).toBeGreaterThan(0);
    const winner = api.options.find((option) => option.rank === 1)!;

    await runComparison(page, { district: "Nashik", crop: "Onion", quantity: "20", unit: "quintal" });

    await expect(page.getByText("Onion · 2,000 kg · Nashik · within 150 km")).toBeVisible();
    await expect(page.getByRole("heading", { level: 2, name: `Recommended: ${winner.market.name}` })).toBeVisible();
    const recommendation = page.getByRole("region").filter({ has: page.getByRole("heading", { name: /^Recommended: / }) });
    await expect(recommendation).toContainText(`${estimatedRupees(winner.estimates.estimatedNetRealization)} estimated net realization`);
    const table = page.getByRole("table", { name: "Market comparison" });
    for (const option of api.options.slice(0, 5)) {
      await expect(table.getByRole("columnheader", { name: new RegExp(`Rank ${option.rank}: ${escape(option.market.name)}$`) }))
        .toBeVisible();
    }
    await expect(table.getByRole("columnheader", { name: /^(.* )?Rank \d+:/ })).toHaveCount(api.options.length);
    await expect(page.getByText("Latest reported prices · Last synchronized").first()).toBeVisible();
  });

  test("why, price trend and what-if open from the results", async ({ page, request }) => {
    await runComparison(page, { district: "Pune", crop: "Onion", quantity: "700", unit: "kg" });

    await page.getByRole("link", { name: "Why?" }).first().click();
    await expect(page).toHaveURL(/\/compare\/why/);
    await expect(page.getByRole("heading", { level: 1 })).toBeVisible();
    await page.goBack();

    const trendLink = page.getByRole("link", { name: "Price trend" }).first();
    const href = await trendLink.getAttribute("href");
    await trendLink.click();
    await expect(page).toHaveURL(/\/markets\/[0-9a-f-]+\/trend\?crop=/);
    const marketId = /\/markets\/([0-9a-f-]+)\/trend/.exec(href!)![1];
    const crop = new URL(href!, "http://local").searchParams.get("crop")!;
    const trend = (await (await request.get(`/api/v1/markets/${marketId}/trends?commodityId=${crop}&days=30`)).json()) as {
      trendDirection: string;
    };
    const directionWord = { RISING: "Rising", FALLING: "Falling", STABLE: "Stable", INSUFFICIENT_DATA: "Not enough data" }[trend.trendDirection];
    await expect(page.getByRole("main")).toContainText(directionWord!);
    await page.goBack();

    await page.getByRole("link", { name: "Try different numbers (what-if)" }).click();
    await expect(page).toHaveURL(/\/compare\/what-if/);
    await expect(page.getByRole("heading", { level: 1 })).toBeVisible();
  });

  test("a crop that is not priced per quintal is explained, not compared", async ({ page, request }) => {
    const ref = await reference(request);
    const perBundle = ref.commodities.find((commodity) => commodity.priceUnit === "Rs./Bundle");
    test.skip(!perBundle, "no crop priced per bundle has recent data");
    await page.goto("/compare");
    await page.getByLabel("District", { exact: true }).selectOption("Pune");
    await page.getByRole("searchbox", { name: "Crop" }).fill(perBundle!.name);
    await page.getByRole("radio", { name: perBundle!.name, exact: true }).click();
    await page.getByRole("textbox", { name: "Quantity" }).fill("2");
    await page.getByRole("radio", { name: "quintal", exact: true }).click();
    await page.getByRole("button", { name: "Find markets" }).click();
    await expect(page.getByText("Prices for this crop are not reported per quintal, so AgriNex cannot compare it yet."))
      .toBeVisible();
  });
});

function escape(text: string): string {
  return text.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}
