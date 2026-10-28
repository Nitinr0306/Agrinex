import { expect, test } from "@playwright/test";

/**
 * Admin journey (spec §10 FRONTEND checklist "admin dashboard"; role file 02 §11; ADR-0020): data health, ingestion
 * runs and their counters, curated markets, commodity units and the audit log, on the real local database.
 * Needs the local admin's credentials in E2E_ADMIN_IDENTIFIER and E2E_ADMIN_PASSWORD (for example the bootstrap
 * values of infra/.env, loaded into the shell); without them the journey is skipped. Nothing is changed.
 */
const identifier = process.env.E2E_ADMIN_IDENTIFIER;
const password = process.env.E2E_ADMIN_PASSWORD;

test.skip(!identifier || !password, "set E2E_ADMIN_IDENTIFIER and E2E_ADMIN_PASSWORD to run the admin journey");

test("an admin reads data health, runs, markets, commodities and the audit log", async ({ page, request }) => {
  await page.goto("/sign-in?tab=signin");
  await page.getByRole("textbox", { name: "Mobile number or email" }).fill(identifier!);
  await page.getByRole("textbox", { name: "Password", exact: true }).fill(password!);
  await page.getByRole("button", { name: "Sign in", exact: true }).click();
  await expect(page.getByRole("navigation", { name: "Admin" })).toBeVisible();

  await page.goto("/admin");
  await expect(page.getByRole("heading", { level: 1, name: "Data health" })).toBeVisible();
  // Historical coverage (ADR-0054 item 6): the status counts and the next maintenance runs.
  const coverage = page.getByRole("heading", { level: 2, name: /^Historical report dates, / }).locator("..");
  await expect(coverage).toBeVisible();
  for (const label of ["Complete (settled)", "Fetched too early", "Failed", "Never fetched", "Next gap check", "Next reconciliation"]) {
    await expect(coverage.getByText(label, { exact: false }).first()).toBeVisible();
  }
  await expect(coverage.getByText(/^\d+$/).first()).toBeVisible();

  await page.goto("/admin/runs");
  await expect(page.getByRole("heading", { level: 1, name: "Ingestion runs" })).toBeVisible();
  const firstRun = page.getByRole("table").getByRole("link").first();
  await expect(firstRun).toBeVisible();
  await firstRun.click();
  await expect(page.getByRole("heading", { level: 1, name: "Ingestion run" })).toBeVisible();
  await expect(page.getByRole("main")).toContainText("Fetched");
  await expect(page.getByRole("main")).toContainText("Completeness");

  await page.goto("/admin/markets");
  await expect(page.getByRole("heading", { level: 1, name: "Markets" })).toBeVisible();
  await page.getByLabel("Review status").selectOption("VERIFIED");
  await expect(page.getByRole("table").getByText("VERIFIED").first()).toBeVisible();

  await page.goto("/admin/commodities");
  await expect(page.getByRole("heading", { level: 1, name: "Commodities" })).toBeVisible();

  await page.goto("/admin/audit");
  await expect(page.getByRole("heading", { level: 1, name: "Audit log" })).toBeVisible();
  await expect(page.getByRole("table")).toBeVisible();

  // The admin API itself refuses a request without a token.
  expect((await request.get("/api/v1/admin/ingestion/runs")).status()).toBe(401);
});
