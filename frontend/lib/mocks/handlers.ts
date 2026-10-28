/**
 * Mock mode (spec §44; FRONTEND_CONSUMPTION.md §6): MSW handlers that answer every contract operation from the
 * synthetic files in __mocks__/api. SYNTHETIC TEST DATA ONLY: screens in mock mode carry the prototype tag.
 *
 * Choosing a file for a request:
 * 1. a scenario chosen for this browser tab with `?mock=<file name>` (for example `?mock=selling-options-compare.422-no-markets-in-range`);
 * 2. otherwise the file whose request path and query match the request best, preferring a 2xx answer;
 * 3. a path the synthetic data set does not cover answers 404 NOT_FOUND, never another resource's data.
 * The mock sign-in lasts for the tab (sessionStorage) and alert changes until a reload (memory), so the prototype
 * can be clicked through.
 */
import { http, HttpResponse, type HttpHandler } from "msw";
import { MOCK_FILES, OPERATIONS } from "./catalog.generated";
import type { MockEnvelope } from "./envelope";

const SCENARIO_KEY = "agrinex.mock.scenarios";
const SIGNED_IN_KEY = "agrinex.mock.signedIn";

type Json = Record<string, unknown>;

/** Reads `?mock=` from the page address into this tab's scenario choices. */
export function captureScenarioChoice(search: string): void {
  const chosen = new URLSearchParams(search).getAll("mock");
  if (chosen.length === 0) return;
  const scenarios = readScenarios();
  for (const file of chosen) {
    const match = MOCK_FILES.find((entry) => entry.file === file);
    if (match) scenarios[match.envelope.operationId] = file;
  }
  sessionStorage.setItem(SCENARIO_KEY, JSON.stringify(scenarios));
}

export function buildHandlers(): HttpHandler[] {
  const state = createState();
  return Object.entries(OPERATIONS).map(([operationId, route]) => {
    const method = route.method.toLowerCase() as "get" | "post" | "put" | "patch" | "delete";
    return http[method](route.path, async ({ request }) => {
      const special = await statefulAnswer(operationId, request, state);
      if (special) return special;
      const body = request.method === "GET" || request.method === "DELETE"
        ? null
        : await request.clone().json().catch(() => null);
      const envelope = chooseEnvelope(operationId, new URL(request.url), route.path, body);
      return envelope ? respond(envelope) : notFound(new URL(request.url).pathname);
    });
  });
}

function chooseEnvelope(operationId: string, url: URL, template: string, body: unknown): MockEnvelope | null {
  const candidates = MOCK_FILES.filter((entry) => entry.envelope.operationId === operationId);
  const chosen = readScenarios()[operationId];
  const forced = candidates.find((entry) => entry.file === chosen);
  if (forced) return forced.envelope;

  const hasPathParams = template.includes(":");
  let best: { envelope: MockEnvelope; score: number } | null = null;
  for (const { envelope } of candidates) {
    const mockUrl = new URL(envelope.request.path, url.origin);
    const samePath = mockUrl.pathname === url.pathname;
    if (hasPathParams && !samePath) continue;
    let score = envelope.response.status < 300 ? 10 : 0;
    mockUrl.searchParams.forEach((value, key) => {
      if (url.searchParams.get(key) === value) score += 1;
    });
    score += sharedMembers(envelope.request.body, body);
    if (!best || score > best.score) best = { envelope, score };
  }
  return best?.envelope ?? null;
}

/** How many top-level members of a request body equal the mock's request body (picks the closest scenario). */
function sharedMembers(mockBody: unknown, body: unknown): number {
  if (!isJson(mockBody) || !isJson(body)) return 0;
  return Object.entries(mockBody).filter(([key, value]) => JSON.stringify(body[key]) === JSON.stringify(value)).length;
}

function isJson(value: unknown): value is Json {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

interface MockState {
  alerts: Json[] | null;
  events: Json[] | null;
}

function createState(): MockState {
  return { alerts: null, events: null };
}

async function statefulAnswer(operationId: string, request: Request, state: MockState): Promise<Response | null> {
  if (readScenarios()[operationId]) return null;
  switch (operationId) {
    case "refreshSession":
      return respond(fileFor(operationId, sessionStorage.getItem(SIGNED_IN_KEY) === "1" ? 200 : 401));
    case "login":
    case "registerFarmer": {
      sessionStorage.setItem(SIGNED_IN_KEY, "1");
      return null;
    }
    case "logout":
    case "confirmPasswordReset":
      sessionStorage.removeItem(SIGNED_IN_KEY);
      return null;
    case "listAlerts": {
      const page = pageOf(fileFor(operationId, 200));
      state.alerts ??= [...(page.items as Json[])];
      return HttpResponse.json({ ...page, items: state.alerts, totalItems: state.alerts.length, totalPages: 1 },
        { headers: { "X-Correlation-Id": "mock-correlation" } });
    }
    case "createAlert": {
      const body = (await request.json().catch(() => ({}))) as Json;
      const created = { ...(fileFor(operationId, 201).response.body as Json) };
      created.id = `00000000-0000-4000-8000-${String(Date.now()).slice(-12).padStart(12, "0")}`;
      created.alertType = body.alertType ?? created.alertType;
      created.thresholdValue = body.thresholdValue ?? created.thresholdValue;
      state.alerts ??= [...(pageOf(fileFor("listAlerts", 200)).items as Json[])];
      state.alerts = [created, ...state.alerts];
      return HttpResponse.json(created, { status: 201, headers: { "X-Correlation-Id": "mock-correlation" } });
    }
    case "updateAlertStatus": {
      const body = (await request.json().catch(() => ({}))) as Json;
      const id = new URL(request.url).pathname.split("/").pop();
      state.alerts ??= [...(pageOf(fileFor("listAlerts", 200)).items as Json[])];
      const alert = state.alerts.find((item) => item.id === id);
      if (!alert) return notFound(new URL(request.url).pathname);
      alert.status = body.status;
      return HttpResponse.json(alert, { headers: { "X-Correlation-Id": "mock-correlation" } });
    }
    case "deleteAlert": {
      const id = new URL(request.url).pathname.split("/").pop();
      state.alerts ??= [...(pageOf(fileFor("listAlerts", 200)).items as Json[])];
      state.alerts = state.alerts.filter((item) => item.id !== id);
      return new HttpResponse(null, { status: 204 });
    }
    case "listAlertEvents": {
      const page = pageOf(fileFor(operationId, 200));
      state.events ??= [...(page.items as Json[])];
      const unreadOnly = new URL(request.url).searchParams.get("unreadOnly") === "true";
      const items = unreadOnly ? state.events.filter((event) => event.readAt === null) : state.events;
      return HttpResponse.json({ ...page, items, totalItems: items.length, totalPages: items.length ? 1 : 0 },
        { headers: { "X-Correlation-Id": "mock-correlation" } });
    }
    case "markAlertEventRead": {
      const id = new URL(request.url).pathname.split("/").at(-2);
      state.events ??= [...(pageOf(fileFor("listAlertEvents", 200)).items as Json[])];
      const event = state.events.find((item) => item.id === id);
      if (!event) return notFound(new URL(request.url).pathname);
      event.readAt = new Date().toISOString();
      return new HttpResponse(null, { status: 204 });
    }
    default:
      return null;
  }
}

function fileFor(operationId: string, status: number): MockEnvelope {
  const entry = MOCK_FILES.find((item) => item.envelope.operationId === operationId
    && item.envelope.response.status === status);
  if (!entry) throw new Error(`No mock for ${operationId} ${status}`);
  return entry.envelope;
}

function pageOf(envelope: MockEnvelope): Json {
  return { ...(envelope.response.body as Json) };
}

function respond(envelope: MockEnvelope): Response {
  const headers = { ...envelope.response.headers };
  if (envelope.response.body === null) {
    delete headers["Content-Type"];
    return new HttpResponse(null, { status: envelope.response.status, headers });
  }
  return HttpResponse.json(envelope.response.body as Json, { status: envelope.response.status, headers });
}

function notFound(path: string): Response {
  return HttpResponse.json(
    {
      type: "/problems/not-found",
      title: "Not found",
      status: 404,
      detail: "The synthetic mock data set has no answer for this resource.",
      instance: path,
      code: "NOT_FOUND",
      correlationId: "mock-not-found",
      errors: [],
    },
    { status: 404, headers: { "Content-Type": "application/problem+json", "X-Correlation-Id": "mock-not-found" } },
  );
}

function readScenarios(): Record<string, string> {
  try {
    return JSON.parse(sessionStorage.getItem(SCENARIO_KEY) ?? "{}") as Record<string, string>;
  } catch {
    return {};
  }
}
