/**
 * The admin area shows its screens to ADMIN accounts only (spec §28; ADR-0020). The server authorizes every admin
 * operation; this guard only decides what the browser shows. Accounts and ids are synthetic.
 */
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { act, cleanup, render, screen } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import type { AuthResponse } from "@/lib/api/types";
import { acceptAuthResponse, clearSession, resetSessionForTests } from "@/lib/auth/session-store";
import { I18nProvider } from "@/lib/i18n/i18n-provider";
import type { Messages } from "@/lib/i18n/messages";
import { AdminShell } from "./admin-shell";

vi.mock("next/navigation", () => ({
  usePathname: () => "/admin/runs",
  useRouter: () => ({ push: vi.fn(), replace: vi.fn(), refresh: vi.fn(), back: vi.fn(), prefetch: vi.fn() }),
}));

const messages = JSON.parse(readFileSync(join(process.cwd(), "public", "locales", "mr.json"), "utf8")) as Messages;

function signIn(role: "FARMER" | "ADMIN") {
  acceptAuthResponse({
    accessToken: "synthetic-token",
    tokenType: "Bearer",
    expiresIn: 900,
    user: { id: "00000000-0000-4000-8000-000000000001", role, emailVerified: true },
  } as unknown as AuthResponse);
}

function renderShell() {
  return render(
    <I18nProvider locale="mr" messages={messages}>
      <AdminShell><p>Admin content</p></AdminShell>
    </I18nProvider>,
  );
}

beforeEach(() => resetSessionForTests());
afterEach(cleanup);

describe("AdminShell", () => {
  it("shows nothing of the admin area while the session is restored", () => {
    renderShell();
    expect(screen.queryByText("Admin content")).toBeNull();
  });

  it("asks a signed-out visitor to sign in and come back to the same page", () => {
    act(() => clearSession(false));
    renderShell();
    expect(screen.queryByText("Admin content")).toBeNull();
    expect(screen.getByRole("link", { name: "Sign in" }).getAttribute("href")).toBe("/sign-in?tab=signin&next=%2Fadmin%2Fruns");
  });

  it("refuses a farmer account", () => {
    act(() => signIn("FARMER"));
    renderShell();
    expect(screen.queryByText("Admin content")).toBeNull();
    expect(screen.getByText("You do not have access to this page.")).toBeTruthy();
  });

  it("shows the admin area to an admin, in English, with the current page marked", () => {
    act(() => signIn("ADMIN"));
    const { container } = renderShell();
    const content = screen.getByText("Admin content");
    expect(content.closest("[lang]")?.getAttribute("lang")).toBe("en");
    expect(screen.getByRole("link", { name: "Ingestion runs" }).getAttribute("aria-current")).toBe("page");
    // The shared app bar keeps the chosen language: it is outside the English region.
    expect(container.querySelector("header")?.closest("[lang='en']")).toBeNull();
  });
});
