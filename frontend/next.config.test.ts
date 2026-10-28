import { afterEach, describe, expect, it, vi } from "vitest";

/**
 * The page-level security settings of next.config.ts. The emailed-link pages send `Referrer-Policy: no-referrer`
 * (ADR-0048 item 2); the edge keeps that value and adds its own default only to responses without one
 * (infra/nginx/templates/default.conf.template). The container image refuses mock mode.
 */
describe("next.config", () => {
  afterEach(() => {
    vi.unstubAllEnvs();
    vi.resetModules();
  });

  it("sends no-referrer on the emailed-link pages, and sets no referrer policy elsewhere", async () => {
    const { default: config } = await import("./next.config");
    const rules = await config.headers!();
    expect(rules).toEqual([
      { source: "/reset-password", headers: [{ key: "Referrer-Policy", value: "no-referrer" }] },
      { source: "/verify-email", headers: [{ key: "Referrer-Policy", value: "no-referrer" }] },
    ]);
  });

  it("builds the standalone server for the container image only", async () => {
    vi.stubEnv("AGRINEX_STANDALONE", "true");
    const { default: image } = await import("./next.config");
    expect(image.output).toBe("standalone");

    vi.unstubAllEnvs();
    vi.resetModules();
    const { default: local } = await import("./next.config");
    expect(local.output).toBeUndefined();
  });

  it("refuses to build the container image in mock mode", async () => {
    vi.stubEnv("AGRINEX_STANDALONE", "true");
    vi.stubEnv("NEXT_PUBLIC_USE_MOCK_API", "true");
    await expect(import("./next.config")).rejects.toThrow("must not be built with NEXT_PUBLIC_USE_MOCK_API=true");
  });
});
