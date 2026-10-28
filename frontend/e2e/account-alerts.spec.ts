import { expect, test } from "@playwright/test";
import { newFarmer } from "./support";

/**
 * Account and alert journey (spec F1.18-F1.20; role file 02 S11-S14; ADR-0047, ADR-0048): a farmer sets a price
 * alert, creates an account in the same step, manages the alert, sees the unconfirmed-email notice and signs out.
 * A new throwaway farmer is created on every run; its email is a Resend test address.
 */
test("a farmer sets an alert, creates an account in the same step, manages the alert and signs out", async ({ page }) => {
  const farmer = newFarmer();

  await page.goto("/alerts/new");
  await page.getByRole("radio", { name: "At or above a price" }).click();
  await expect(page.getByLabel("Crop", { exact: true }).locator("option", { hasText: "Onion" }).first()).toBeAttached();
  await page.getByLabel("Crop", { exact: true }).selectOption("Onion");
  await page.getByRole("textbox", { name: "Price (₹ per quintal)" }).fill("500");
  await page.getByRole("button", { name: "Save alert" }).click();

  await expect(page).toHaveURL(/\/sign-in\?tab=create/);
  await expect(page.getByText("Price at or above ₹500 per quintal")).toBeVisible();
  await page.getByRole("textbox", { name: "Mobile number" }).fill(farmer.mobile);
  await page.getByRole("textbox", { name: "Email address" }).fill(farmer.email);
  await page.getByRole("textbox", { name: "Password", exact: true }).fill(farmer.password);
  await page.getByRole("button", { name: "Create account and save alert" }).click();

  await expect(page).toHaveURL(/\/alerts\?created=1/);
  await expect(page.getByText("Alert saved.")).toBeVisible();
  await expect(page.getByRole("heading", { name: "My alerts (1)" })).toBeVisible();
  const alertSwitch = page.getByRole("switch", { name: "Alert Onion · All markets · Price at or above ₹500 per quintal" });
  await expect(alertSwitch).toBeChecked();

  await alertSwitch.click();
  await expect(alertSwitch).not.toBeChecked();
  await page.reload();
  await expect(page.getByRole("switch", { name: /^Alert Onion/ })).not.toBeChecked();

  await page.goto("/account");
  await expect(page.getByRole("heading", { level: 1, name: "Account" })).toBeVisible();
  await expect(page.getByText("Email not confirmed yet.")).toBeVisible();

  await page.goto("/alerts");
  await page.getByRole("button", { name: "Delete" }).click();
  await page.getByRole("dialog").getByRole("button", { name: "Delete" }).click();
  await expect(page.getByText("You have no price alerts yet.")).toBeVisible();

  await page.goto("/account");
  await page.getByRole("button", { name: "Sign out" }).click();
  await page.goto("/account");
  await expect(page.getByText("You are not signed in.")).toBeVisible();
});

test("a wrong password is refused without saying which part was wrong", async ({ page }) => {
  await page.goto("/sign-in?tab=signin");
  await page.getByRole("textbox", { name: "Mobile number or email" }).fill("9000000000");
  await page.getByRole("textbox", { name: "Password", exact: true }).fill("not-the-password");
  await page.getByRole("button", { name: "Sign in", exact: true }).click();
  await expect(page.getByText("Mobile number, email or password is not correct.")).toBeVisible();
});

test("an emailed link that does not work says so", async ({ page }) => {
  await page.goto("/verify-email#token=not-a-real-token");
  await expect(page.getByText("This link no longer works.", { exact: false })).toBeVisible();
  await page.goto("/");
  await page.goto("/reset-password#token=not-a-real-token");
  await expect(page.getByRole("heading", { name: /^This link no longer works\./ })).toBeVisible();
});
