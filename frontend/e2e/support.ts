import { expect, type APIRequestContext, type Page } from "@playwright/test";

/**
 * Helpers for the browser journeys. Expected values are read from the same running backend through
 * /api/v1, so the journeys check what the screens show against the API instead of against fixed prices: the data
 * are real government prices and change with every ingestion run.
 */

export interface Named {
  id: string;
  name: string;
}

export interface Reference {
  stateId: string;
  districts: (Named & { centroid: { lat: number; lon: number } })[];
  commodities: (Named & { priceUnit: string | null })[];
}

export async function reference(request: APIRequestContext): Promise<Reference> {
  const states = await getJson<{ items: Named[] }>(request, "/api/v1/geography/states");
  const stateId = states.items[0].id;
  const districts = await getJson<{ items: Reference["districts"] }>(request,
    `/api/v1/geography/states/${stateId}/districts`);
  const commodities = await getJson<{ items: Reference["commodities"] }>(request, "/api/v1/commodities");
  return { stateId, districts: districts.items, commodities: commodities.items };
}

export async function getJson<T>(request: APIRequestContext, path: string): Promise<T> {
  const response = await request.get(path);
  expect(response.status(), `GET ${path}`).toBe(200);
  return (await response.json()) as T;
}

export function byName<T extends Named>(items: T[], name: string): T {
  const found = items.find((item) => item.name === name);
  if (!found) throw new Error(`${name} is not offered; the local database needs ingested data (guide section 6)`);
  return found;
}

/** The comparison the wizard requests for a district centroid (ADR-0039), with the default 150 km. */
export async function compareViaApi(request: APIRequestContext, body: {
  commodityId: string;
  quantity: number;
  quantityUnit: "KG" | "QUINTAL";
  originLat: number;
  originLon: number;
  maxDistanceKm?: number;
}): Promise<ComparisonResponse> {
  const response = await request.post("/api/v1/selling-options/compare", { data: body });
  expect(response.status(), "POST /api/v1/selling-options/compare").toBe(200);
  return (await response.json()) as ComparisonResponse;
}

export interface ComparisonResponse {
  options: {
    rank: number;
    market: Named;
    distanceKm: number;
    price: { modalPrice: number; reportDate: string };
    estimates: { estimatedNetRealization: number; estimatedTransportCost: number; trips: number };
  }[];
  netRealizationWinnerId: string;
  priceOnlyWinnerId: string;
}

/** An estimate as the screens show it: half-up to the whole rupee, Indian grouping (role file 02 PDD-06, PDD-07). */
export function estimatedRupees(value: number): string {
  const [whole, fraction = ""] = Math.abs(value).toFixed(2).split(".");
  let rupees = BigInt(whole);
  if (Number(fraction[0]) >= 5) rupees += BigInt(1);
  const text = new Intl.NumberFormat("en-IN", { maximumFractionDigits: 0 }).format(rupees);
  return value < 0 && rupees > BigInt(0) ? `−₹${text}` : `₹${text}`;
}

/** A throwaway farmer. The email is a Resend test address, so a real delivery never reaches a person. */
export function newFarmer() {
  const digits = Array.from({ length: 9 }, () => Math.floor(Math.random() * 10)).join("");
  const tag = `${Date.now().toString(36)}${digits.slice(0, 4)}`;
  return {
    mobile: `9${digits}`,
    email: `delivered+agrinex-e2e-${tag}@resend.dev`,
    password: `e2e-pass-${tag}`,
  };
}

/** No page may scroll sideways (spec section 48: no horizontal scroll at 375 px). */
export async function expectNoHorizontalScroll(page: Page) {
  const overflow = await page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth);
  expect(overflow, "horizontal overflow in px").toBeLessThanOrEqual(0);
}

/**
 * Fills the comparison form the way a farmer does and waits for the results. On a phone the form is a wizard of
 * four steps with a "Next" button (role file 02 S02); on a desktop it is one panel.
 */
export async function runComparison(page: Page, input: { district: string; crop: string; quantity: string; unit: "kg" | "quintal" }) {
  await page.goto("/compare");
  const next = page.getByRole("button", { name: "Next", exact: true });
  const stepIfWizard = async () => {
    if (await next.isVisible()) await next.click();
  };
  await page.getByLabel("District", { exact: true }).selectOption(input.district);
  await stepIfWizard();
  await page.getByRole("searchbox", { name: "Crop" }).fill(input.crop);
  await page.getByRole("radio", { name: input.crop, exact: true }).click();
  await stepIfWizard();
  await page.getByRole("textbox", { name: "Quantity" }).fill(input.quantity);
  await page.getByRole("radio", { name: input.unit, exact: true }).click();
  await stepIfWizard();
  await page.getByRole("button", { name: "Find markets" }).click();
  await expect(page.getByRole("heading", { level: 2, name: /^Recommended: / })).toBeVisible();
}

/** Switches the interface language through the header: buttons on a desktop, a menu on a phone. */
export async function switchLanguage(page: Page, nativeName: string) {
  const direct = page.getByRole("group", { name: /Language|भाषा/ }).getByRole("button", { name: nativeName });
  if (await direct.isVisible()) {
    await direct.click();
    return;
  }
  await page.getByRole("banner").locator("button[aria-haspopup='true']").click();
  await page.getByRole("banner").getByRole("button", { name: nativeName }).click();
}
