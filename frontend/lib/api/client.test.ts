// @vitest-environment node
/**
 * The API client's auth flow (FRONTEND_CONSUMPTION.md §4; ADR-0016, ADR-0042; SR-14), against a stubbed fetch.
 * The bodies below are shaped like the contract's AuthResponse and ApiErrorResponse; all values are synthetic.
 */
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { acceptAuthResponse, getAccessToken, getSessionState, refreshSession, resetSessionForTests } from "@/lib/auth/session-store";
import { apiRequest, buildUrl } from "./client";
import { ApiError, ContractViolationError, NetworkError } from "./errors";

const REFRESH = "/api/v1/auth/refresh";

function authBody(token: string) {
  return {
    accessToken: token,
    tokenType: "Bearer",
    expiresIn: 900,
    user: { id: "00000000-0000-4000-8000-000000000001", role: "FARMER", emailVerified: true },
  };
}

function json(status: number, body: unknown, headers: Record<string, string> = {}): Response {
  const type = status >= 400 ? "application/problem+json" : "application/json";
  return new Response(JSON.stringify(body), { status, headers: { "Content-Type": type, ...headers } });
}

function problem(status: number, code: string, headers: Record<string, string> = {}): Response {
  return json(status, { type: `/problems/${code.toLowerCase()}`, title: code, status, code, correlationId: "test-correlation", errors: [] }, headers);
}

interface Call { url: string; authorization: string | null }

/** A fetch stub answering from a queue per URL, recording the Authorization header of each call. */
function stubFetch(answers: Record<string, (() => Response)[]>) {
  const calls: Call[] = [];
  const fetchMock = vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    const url = String(input);
    const headers = new Headers(init?.headers);
    calls.push({ url, authorization: headers.get("Authorization") });
    const queue = answers[url.split("?")[0]];
    const next = queue?.shift();
    if (!next) throw new Error(`Unexpected request to ${url}`);
    return next();
  });
  vi.stubGlobal("fetch", fetchMock);
  return calls;
}

function signIn(token: string) {
  acceptAuthResponse(authBody(token) as Parameters<typeof acceptAuthResponse>[0]);
}

beforeEach(() => resetSessionForTests());
afterEach(() => {
  vi.unstubAllGlobals();
  vi.useRealTimers();
});

describe("apiRequest", () => {
  it("never sends a token to an anonymous-only operation", async () => {
    signIn("token-a");
    const calls = stubFetch({ "/api/v1/commodities": [() => json(200, { items: [] })] });
    await apiRequest("/commodities", { auth: "none", expect: ["items"] });
    expect(calls).toEqual([{ url: "/api/v1/commodities", authorization: null }]);
  });

  it("refreshes once on 401 and retries with the new token", async () => {
    signIn("token-a");
    const calls = stubFetch({
      "/api/v1/alerts": [() => problem(401, "UNAUTHORIZED"), () => json(200, { items: [] })],
      [REFRESH]: [() => json(200, authBody("token-b"))],
    });
    await apiRequest("/alerts", { auth: "required", expect: ["items"] });
    expect(calls.map((call) => [call.url, call.authorization])).toEqual([
      ["/api/v1/alerts", "Bearer token-a"],
      [REFRESH, null],
      ["/api/v1/alerts", "Bearer token-b"],
    ]);
    expect(getAccessToken()).toBe("token-b");
  });

  it("retries an optional operation anonymously when the refresh is refused", async () => {
    signIn("token-a");
    const calls = stubFetch({
      "/api/v1/selling-options/compare": [() => problem(401, "UNAUTHORIZED"), () => json(200, { options: [] })],
      [REFRESH]: [() => problem(401, "REFRESH_TOKEN_INVALID")],
    });
    await apiRequest("/selling-options/compare", { method: "POST", body: {}, auth: "optional", expect: ["options"] });
    expect(calls.map((call) => call.authorization)).toEqual(["Bearer token-a", null, null]);
    expect(getSessionState().status).toBe("signedOut");
  });

  it("fails a required operation with 401 when the refresh is refused, without a second attempt", async () => {
    signIn("token-a");
    const calls = stubFetch({
      "/api/v1/alerts": [() => problem(401, "UNAUTHORIZED")],
      [REFRESH]: [() => problem(401, "REFRESH_TOKEN_INVALID")],
    });
    const failure = await apiRequest("/alerts", { auth: "required" }).catch((error: unknown) => error);
    expect(failure).toBeInstanceOf(ApiError);
    expect((failure as ApiError).status).toBe(401);
    expect(calls).toHaveLength(2);
  });

  it("does not call a required operation at all when no session can be restored", async () => {
    const calls = stubFetch({ [REFRESH]: [() => problem(401, "REFRESH_TOKEN_INVALID")] });
    await expect(apiRequest("/alerts", { auth: "required" })).rejects.toMatchObject({ status: 401 });
    expect(calls.map((call) => call.url)).toEqual([REFRESH]);
  });

  it("shares one refresh between concurrent 401s", async () => {
    signIn("token-a");
    const calls = stubFetch({
      "/api/v1/alerts": [() => problem(401, "UNAUTHORIZED"), () => problem(401, "UNAUTHORIZED"), () => json(200, { items: [] }), () => json(200, { items: [] })],
      [REFRESH]: [() => json(200, authBody("token-b"))],
    });
    await Promise.all([
      apiRequest("/alerts", { auth: "required", expect: ["items"] }),
      apiRequest("/alerts", { auth: "required", expect: ["items"] }),
    ]);
    expect(calls.filter((call) => call.url === REFRESH)).toHaveLength(1);
    expect(calls.slice(-2).map((call) => call.authorization)).toEqual(["Bearer token-b", "Bearer token-b"]);
  });

  it("reads the problem code, correlation id and Retry-After of an error", async () => {
    stubFetch({ "/api/v1/selling-options/compare": [() => problem(429, "RATE_LIMITED", { "Retry-After": "30" })] });
    resetSessionForTests();
    signIn("token-a");
    const failure = await apiRequest("/selling-options/compare", { method: "POST", body: {}, auth: "optional" }).catch((error: unknown) => error);
    expect(failure).toBeInstanceOf(ApiError);
    expect(failure).toMatchObject({ status: 429, code: "RATE_LIMITED", correlationId: "test-correlation", retryAfterSeconds: 30 });
  });

  it("rejects a success body that lacks a required member", async () => {
    stubFetch({ "/api/v1/commodities": [() => json(200, { results: [] })] });
    await expect(apiRequest("/commodities", { auth: "none", expect: ["items"] })).rejects.toBeInstanceOf(ContractViolationError);
  });

  it("reports an unreachable server as a network error", async () => {
    vi.stubGlobal("fetch", vi.fn(async () => { throw new TypeError("Failed to fetch"); }));
    await expect(apiRequest("/commodities", { auth: "none" })).rejects.toBeInstanceOf(NetworkError);
  });
});

describe("refreshSession", () => {
  it("waits Retry-After and retries once on 429, without signing out", async () => {
    vi.useFakeTimers();
    signIn("token-a");
    const calls = stubFetch({
      [REFRESH]: [() => problem(429, "RATE_LIMITED", { "Retry-After": "2" }), () => json(200, authBody("token-b"))],
    });
    const pending = refreshSession();
    await vi.advanceTimersByTimeAsync(1999);
    expect(calls).toHaveLength(1);
    await vi.advanceTimersByTimeAsync(1);
    await expect(pending).resolves.toBe("token-b");
    expect(calls).toHaveLength(2);
    expect(getSessionState().status).toBe("signedIn");
  });

  it("keeps the session when the refresh is rate limited twice", async () => {
    vi.useFakeTimers();
    signIn("token-a");
    stubFetch({
      [REFRESH]: [() => problem(429, "RATE_LIMITED", { "Retry-After": "1" }), () => problem(429, "RATE_LIMITED", { "Retry-After": "1" })],
    });
    const pending = refreshSession();
    await vi.advanceTimersByTimeAsync(1000);
    await expect(pending).resolves.toBe("token-a");
    expect(getSessionState().status).toBe("signedIn");
  });

  /**
   * Found in Task 12 on the real stack: the page-load refresh answered 429 with a long Retry-After, and an anonymous
   * farmer's comparison waited for the back-off. The optional operation now goes at once without a token, while
   * the session still waits Retry-After and retries once.
   */
  it("does not hold an optional operation while a rate-limited page-load refresh backs off", async () => {
    vi.useFakeTimers();
    const calls = stubFetch({
      [REFRESH]: [() => problem(429, "RATE_LIMITED", { "Retry-After": "60" }), () => problem(401, "REFRESH_TOKEN_INVALID")],
      "/api/v1/selling-options/compare": [() => json(200, { options: [] })],
    });
    const restore = refreshSession();
    const compare = apiRequest("/selling-options/compare", { method: "POST", body: {}, auth: "optional", expect: ["options"] });
    await vi.advanceTimersByTimeAsync(0);
    await expect(compare).resolves.toEqual({ options: [] });
    expect(calls.map((call) => [call.url, call.authorization])).toEqual([
      [REFRESH, null],
      ["/api/v1/selling-options/compare", null],
    ]);
    await vi.advanceTimersByTimeAsync(60_000);
    await expect(restore).resolves.toBeNull();
    expect(calls).toHaveLength(3);
  });

  it("still sends the token of a session restored by the page-load refresh", async () => {
    const calls = stubFetch({
      [REFRESH]: [() => json(200, authBody("token-r"))],
      "/api/v1/selling-options/compare": [() => json(200, { options: [] })],
    });
    await apiRequest("/selling-options/compare", { method: "POST", body: {}, auth: "optional", expect: ["options"] });
    expect(calls.map((call) => [call.url, call.authorization])).toEqual([
      [REFRESH, null],
      ["/api/v1/selling-options/compare", "Bearer token-r"],
    ]);
  });

  it("ignores a 200 whose body is not an AuthResponse", async () => {
    stubFetch({ [REFRESH]: [() => json(200, { token: "x" })] });
    await expect(refreshSession()).resolves.toBeNull();
    expect(getSessionState().status).toBe("signedOut");
  });
});

describe("buildUrl", () => {
  it("drops empty query values and encodes the rest", () => {
    expect(buildUrl("/markets/m1/trend", { commodityId: "c 1", days: 30, varietyId: undefined, gradeId: null, x: "" }))
      .toBe("/api/v1/markets/m1/trend?commodityId=c+1&days=30");
  });
});
