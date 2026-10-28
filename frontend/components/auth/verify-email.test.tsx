/**
 * S16 email confirmation from the emailed link (ADR-0048 items 2–4): the token is read from the URL fragment,
 * removed from the address bar, sent once, and a link without a well-formed token never reaches the server.
 * The token below is synthetic.
 */
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { resetSessionForTests, clearSession } from "@/lib/auth/session-store";
import { I18nProvider } from "@/lib/i18n/i18n-provider";
import type { Messages } from "@/lib/i18n/messages";
import { VerifyEmailScreen } from "./account-screens";

vi.mock("next/navigation", () => ({
  usePathname: () => "/verify-email",
  useRouter: () => ({ push: vi.fn(), replace: vi.fn(), refresh: vi.fn(), back: vi.fn(), prefetch: vi.fn() }),
}));

const en = JSON.parse(readFileSync(join(process.cwd(), "public", "locales", "en.json"), "utf8")) as Messages & Record<string, string>;
const TOKEN = "A".repeat(42) + "w";

function renderScreen() {
  return render(<I18nProvider locale="en" messages={en}><VerifyEmailScreen /></I18nProvider>);
}

beforeEach(() => {
  resetSessionForTests();
  clearSession(false);
});
afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
  window.history.replaceState(null, "", "/");
});

describe("VerifyEmailScreen", () => {
  it("treats a link without a token as invalid and calls nothing", async () => {
    const fetchMock = vi.fn();
    vi.stubGlobal("fetch", fetchMock);
    window.history.replaceState(null, "", "/verify-email");
    renderScreen();
    await waitFor(() => expect(screen.getByText(en["verify.invalid"])).toBeTruthy());
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("treats a malformed token as invalid and still clears the fragment", async () => {
    const fetchMock = vi.fn();
    vi.stubGlobal("fetch", fetchMock);
    window.history.replaceState(null, "", "/verify-email#token=short");
    renderScreen();
    await waitFor(() => expect(screen.getByText(en["verify.invalid"])).toBeTruthy());
    expect(window.location.hash).toBe("");
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("confirms a well-formed token once, without a bearer token, and removes it from the address bar", async () => {
    const fetchMock = vi.fn(async () => new Response(null, { status: 204 }));
    vi.stubGlobal("fetch", fetchMock);
    window.history.replaceState(null, "", `/verify-email#token=${TOKEN}`);
    renderScreen();
    await waitFor(() => expect(screen.getByText(en["verify.done"])).toBeTruthy());
    expect(window.location.hash).toBe("");
    expect(fetchMock).toHaveBeenCalledTimes(1);
    const [url, init] = fetchMock.mock.calls[0] as unknown as [string, RequestInit];
    expect(url).toBe("/api/v1/auth/email-verification/confirm");
    expect(JSON.parse(init.body as string)).toEqual({ token: TOKEN });
    expect(new Headers(init.headers).get("Authorization")).toBeNull();
  });
});
