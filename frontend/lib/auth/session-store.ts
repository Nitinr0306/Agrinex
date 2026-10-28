/**
 * The browser session (ADR-0016, ADR-0017, ADR-0042; FRONTEND_CONSUMPTION.md §4).
 *
 * - The access token lives in this module's memory only: never in Web Storage, cookies or React state.
 * - The refresh token is the server's HttpOnly `__Secure-agrinex_refresh` cookie; this code never reads it.
 * - Refreshes are serialized inside the tab (one in-flight promise) and across tabs (a Web Lock), so two tabs
 *   never present the same refresh token and trigger reuse detection.
 * - A 429 from refresh is not a sign-out: wait `Retry-After` and retry once (SR-14).
 */
import { isRecord, parseRetryAfter } from "@/lib/api/errors";
import type { AuthenticatedUser, AuthResponse } from "@/lib/api/types";

export type SessionStatus = "loading" | "signedIn" | "signedOut";

export interface SessionState {
  readonly status: SessionStatus;
  readonly user: AuthenticatedUser | null;
}

const REFRESH_PATH = "/api/v1/auth/refresh";
const LOGOUT_PATH = "/api/v1/auth/logout";
const LOCK_NAME = "agrinex-session-refresh";
const CHANNEL_NAME = "agrinex-session";
const MAX_RETRY_WAIT_SECONDS = 60;

let accessToken: string | null = null;
let state: SessionState = { status: "loading", user: null };
let inFlight: Promise<string | null> | null = null;
/** Settles when the refresh in flight has had its first answer (before any 429 back-off). */
let firstAnswer: Promise<void> = Promise.resolve();
let channel: BroadcastChannel | null = null;
const listeners = new Set<() => void>();

export function getSessionState(): SessionState {
  return state;
}

export function subscribeSession(listener: () => void): () => void {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

export function getAccessToken(): string | null {
  return accessToken;
}

/** Stores the result of `login`, `registerFarmer` or `refreshSession`. */
export function acceptAuthResponse(response: AuthResponse): void {
  accessToken = response.accessToken;
  setState({ status: "signedIn", user: response.user });
}

/** Marks the stored user's email as confirmed after `confirmEmailVerification` (the token is unchanged). */
export function markEmailVerified(): void {
  if (state.user && !state.user.emailVerified) {
    setState({ status: state.status, user: { ...state.user, emailVerified: true } });
  }
}

/** Discards the in-memory token (sign-out, refused refresh, or a password reset that ends every session). */
export function clearSession(broadcast = true): void {
  accessToken = null;
  setState({ status: "signedOut", user: null });
  if (broadcast) post("signed-out");
}

/**
 * Restores the session on page load: one refresh. Safe to call more than once; it joins the refresh in flight.
 */
export function bootstrapSession(): Promise<string | null> {
  listenToOtherTabs();
  return refreshSession();
}

/**
 * Obtains a new access token with the refresh cookie. Returns null when the server refuses (signed out) or
 * cannot be reached. Concurrent callers share one request.
 */
export function refreshSession(): Promise<string | null> {
  if (!inFlight) {
    let answered = () => {};
    firstAnswer = new Promise<void>((resolve) => {
      answered = resolve;
    });
    inFlight = withCrossTabLock(() => refreshWithRetry(answered)).finally(() => {
      answered();
      inFlight = null;
    });
  }
  return inFlight;
}

/**
 * The token to send with an operation that also allows anonymous access (compare, simulate): it waits for the
 * refresh's first answer but not for a 429 back-off, so a rate-limited session restore never holds a farmer's
 * comparison for up to a minute; the request then goes without a token, which changes nothing in the answer
 * (found in Task 12). The session itself still waits `Retry-After` and retries once (SR-14).
 */
export async function tokenForOptionalAccess(): Promise<string | null> {
  const pending = refreshSession();
  await Promise.race([pending, firstAnswer]);
  return accessToken;
}

/** Ends the session on the server (revokes the token family, clears the cookie), then locally and in other tabs. */
export async function signOut(): Promise<void> {
  try {
    await fetch(LOGOUT_PATH, { method: "POST", credentials: "same-origin", headers: { Accept: "application/json" } });
  } catch {
    // Signing out locally must not depend on the network.
  } finally {
    clearSession();
  }
}

/** For tests only: resets module state between cases. */
export function resetSessionForTests(): void {
  accessToken = null;
  state = { status: "loading", user: null };
  inFlight = null;
  firstAnswer = Promise.resolve();
  listeners.clear();
}

async function refreshWithRetry(answered: () => void): Promise<string | null> {
  const first = await refreshOnce();
  if (first.kind !== "rateLimited") return finish(first);
  answered();
  await delay(Math.min(first.retryAfterSeconds ?? 1, MAX_RETRY_WAIT_SECONDS) * 1000);
  const second = await refreshOnce();
  if (second.kind === "rateLimited") {
    // Not a sign-out (SR-14): keep any token we hold; only leave the loading state.
    if (state.status === "loading") setState({ status: "signedOut", user: null });
    return accessToken;
  }
  return finish(second);
}

type RefreshOutcome =
  | { kind: "ok"; response: AuthResponse }
  | { kind: "refused" }
  | { kind: "rateLimited"; retryAfterSeconds: number | null }
  | { kind: "unreachable" };

async function refreshOnce(): Promise<RefreshOutcome> {
  let response: Response;
  try {
    response = await fetch(REFRESH_PATH, {
      method: "POST",
      credentials: "same-origin",
      headers: { Accept: "application/json" },
    });
  } catch {
    return { kind: "unreachable" };
  }
  if (response.status === 429) {
    return { kind: "rateLimited", retryAfterSeconds: parseRetryAfter(response.headers.get("Retry-After")) };
  }
  if (response.status === 200) {
    const body: unknown = await response.json().catch(() => null);
    return isAuthResponse(body) ? { kind: "ok", response: body } : { kind: "unreachable" };
  }
  // 401 REFRESH_TOKEN_INVALID (expired, revoked, reused, or no cookie) and 403 ORIGIN_NOT_ALLOWED: signed out.
  return { kind: "refused" };
}

function finish(outcome: RefreshOutcome): string | null {
  switch (outcome.kind) {
    case "ok":
      acceptAuthResponse(outcome.response);
      return accessToken;
    case "refused":
      clearSession(false);
      return null;
    default:
      // Unreachable server: keep any token we hold; a first load without one counts as signed out.
      if (state.status === "loading") setState({ status: "signedOut", user: null });
      return accessToken;
  }
}

export function isAuthResponse(body: unknown): body is AuthResponse {
  if (!isRecord(body) || typeof body.accessToken !== "string" || body.tokenType !== "Bearer") return false;
  const user = body.user;
  return isRecord(user) && typeof user.id === "string" && typeof user.role === "string"
    && typeof user.emailVerified === "boolean";
}

function setState(next: SessionState): void {
  state = next;
  listeners.forEach((listener) => listener());
}

async function withCrossTabLock<T>(task: () => Promise<T>): Promise<T> {
  const locks = typeof navigator !== "undefined" ? navigator.locks : undefined;
  if (!locks) return task();
  return locks.request(LOCK_NAME, task);
}

function listenToOtherTabs(): void {
  if (channel || typeof BroadcastChannel === "undefined") return;
  channel = new BroadcastChannel(CHANNEL_NAME);
  channel.onmessage = (event: MessageEvent) => {
    if (event.data === "signed-out") clearSession(false);
    if (event.data === "signed-in" && state.status !== "signedIn") void refreshSession();
  };
}

/** Tells other tabs that this tab signed in, so they can pick up the new session with their own refresh. */
export function announceSignedIn(): void {
  post("signed-in");
}

function post(message: "signed-in" | "signed-out"): void {
  channel?.postMessage(message);
}

function delay(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}
