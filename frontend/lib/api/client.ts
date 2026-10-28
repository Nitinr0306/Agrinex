/**
 * The one place that talks HTTP to the AgriNex API (`/api/v1`, same origin; ADR-0017). Every call starts in
 * the browser; server-side code never calls the backend in Phase 1 (ADR-0017 item 4).
 *
 * `auth` mirrors each operation's `security` in the contract:
 * - "none": anonymous only ([{}]); no Authorization header is sent.
 * - "optional": anonymous or bearer ([{}, bearerAuth]: compare, simulate); the token is sent when held.
 * - "required": bearer only.
 * On 401 the client refreshes once (serialized) and retries. If the refresh is refused, an "optional" call is
 * retried without the token and a "required" call fails with 401 so the screen can ask the user to sign in
 * (FRONTEND_CONSUMPTION.md §4 step 4).
 */
import { getAccessToken, getSessionState, refreshSession, tokenForOptionalAccess } from "@/lib/auth/session-store";
import { ApiError, ContractViolationError, NetworkError, isRecord, toApiError } from "./errors";

export const API_BASE = "/api/v1";

export type AuthMode = "none" | "optional" | "required";
export type HttpMethod = "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
export type QueryValue = string | number | boolean | null | undefined;

export interface RequestOptions<T> {
  method?: HttpMethod;
  query?: Record<string, QueryValue>;
  body?: unknown;
  auth: AuthMode;
  signal?: AbortSignal;
  /** Shape check of a 2xx body: the members every response must carry. Omit for answers without a body. */
  expect?: readonly (keyof T & string)[];
}

export async function apiRequest<T>(path: string, options: RequestOptions<T>): Promise<T> {
  const url = buildUrl(path, options.query);
  let token = options.auth === "none" ? null : await tokenFor(options.auth);
  if (options.auth === "required" && token === null) {
    throw new ApiError(401, "UNAUTHORIZED", [], null, null, null);
  }

  const sendOptions: SendOptions = { method: options.method, body: options.body, signal: options.signal };
  let response = await send(url, sendOptions, token);
  if (response.status === 401 && token !== null) {
    // Another request may already have rotated the token while this one was in flight.
    const current = getAccessToken();
    token = current !== null && current !== token ? current : await refreshSession();
    if (token !== null) {
      response = await send(url, sendOptions, token);
    } else if (options.auth === "optional") {
      response = await send(url, sendOptions, null);
    }
  }
  return readResponse<T>(response, options.expect);
}

/**
 * The token to send: after the initial page-load refresh has answered, so a valid session is never missed. An
 * "optional" operation does not wait out a rate-limited refresh's back-off (tokenForOptionalAccess).
 */
async function tokenFor(auth: Exclude<AuthMode, "none">): Promise<string | null> {
  const held = getAccessToken();
  if (held !== null) return held;
  if (auth === "required") return refreshSession();
  if (getSessionState().status === "loading") return tokenForOptionalAccess();
  return null;
}

type SendOptions = Pick<RequestOptions<unknown>, "method" | "body" | "signal">;

async function send(url: string, options: SendOptions, token: string | null): Promise<Response> {
  const headers: Record<string, string> = { Accept: "application/json" };
  if (options.body !== undefined) headers["Content-Type"] = "application/json";
  if (token !== null) headers.Authorization = `Bearer ${token}`;
  try {
    return await fetch(url, {
      method: options.method ?? "GET",
      headers,
      body: options.body === undefined ? undefined : JSON.stringify(options.body),
      credentials: "same-origin",
      signal: options.signal,
    });
  } catch (error) {
    if (error instanceof DOMException && error.name === "AbortError") throw error;
    throw new NetworkError(error);
  }
}

async function readResponse<T>(response: Response, expect: readonly string[] | undefined): Promise<T> {
  const body = await readJson(response);
  if (!response.ok) {
    throw toApiError(response.status, response.headers, body);
  }
  if (response.status === 202 || response.status === 204 || expect === undefined) {
    return body as T;
  }
  if (!isRecord(body)) {
    throw new ContractViolationError(`Expected a JSON object from ${response.url}`);
  }
  const missing = expect.filter((member) => !(member in body));
  if (missing.length > 0) {
    throw new ContractViolationError(`Response from ${response.url} lacks ${missing.join(", ")}`);
  }
  return body as T;
}

async function readJson(response: Response): Promise<unknown> {
  const type = response.headers.get("Content-Type") ?? "";
  if (response.status === 204 || !/json/i.test(type)) return null;
  try {
    return await response.json();
  } catch {
    return null;
  }
}

export function buildUrl(path: string, query?: Record<string, QueryValue>): string {
  const search = new URLSearchParams();
  for (const [key, value] of Object.entries(query ?? {})) {
    if (value !== undefined && value !== null && value !== "") search.set(key, String(value));
  }
  const text = search.toString();
  return `${API_BASE}${path}${text ? `?${text}` : ""}`;
}
