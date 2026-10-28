import { defineConfig, devices } from "@playwright/test";

/**
 * Browser journeys against the real local stack, with nothing mocked: the Next.js dev server (default
 * http://localhost:3000) forwarding /api/v1 to a running backend whose database holds ingested government data
 * (docs/LOCAL_DEVELOPMENT_GUIDE.md section 8). The installed Google Chrome is used (channel "chrome"), so no browser
 * download is needed. One worker keeps the run inside the backend's rate limits (ADR-0017 item 6).
 */
export default defineConfig({
  testDir: "./e2e",
  fullyParallel: false,
  workers: 1,
  retries: 0,
  timeout: 90_000,
  expect: { timeout: 20_000 },
  reporter: [["list"]],
  outputDir: "./test-results/e2e",
  use: {
    baseURL: process.env.E2E_BASE_URL ?? "http://localhost:3000",
    trace: "retain-on-failure",
  },
  projects: [{ name: "chrome", use: { ...devices["Desktop Chrome"], channel: "chrome" } }],
});
