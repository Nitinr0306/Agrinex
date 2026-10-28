// @vitest-environment node
/**
 * Frontend mock tests (docs/api/CONTRACT_VALIDATION.md §3; ADR-0022 item 1; spec §44 "a mock with wrong field
 * names is a bug"): every file in __mocks__/api is validated against the operation and status it declares in
 * contracts/openapi.yaml, with JSON Schema 2020-12 (unevaluatedProperties, if/then, relative external $refs) and
 * the contract's decimal-place keyword. Success mocks also have a valid request body and query.
 */
import { readFileSync, readdirSync } from "node:fs";
import { join } from "node:path";
import Ajv2020 from "ajv/dist/2020";
import addFormats from "ajv-formats";
import { describe, expect, it } from "vitest";
import { parse } from "yaml";
import type { MockEnvelope } from "@/lib/mocks/envelope";

const BASE = "https://contracts.agrinex.invalid/";
const CONTRACTS = join(process.cwd(), "..", "contracts");
const MOCKS = join(process.cwd(), "__mocks__", "api");

type Json = Record<string, unknown>;
const contract = parse(readFileSync(join(CONTRACTS, "openapi.yaml"), "utf8")) as Json;

const ajv = new Ajv2020({ strict: false, allErrors: true });
addFormats(ajv);
ajv.addKeyword({
  keyword: "x-agrinex-max-decimal-places",
  type: "number",
  schemaType: "number",
  validate: (places: number, data: number) => {
    const text = String(data);
    if (/e/i.test(text)) return false;
    return (text.split(".")[1]?.length ?? 0) <= places;
  },
});
for (const file of readdirSync(CONTRACTS).filter((name) => name.endsWith(".json"))) {
  ajv.addSchema({ ...JSON.parse(readFileSync(join(CONTRACTS, file), "utf8")), $id: `${BASE}${file}` });
}
ajv.addSchema({ ...contract, $id: `${BASE}openapi.yaml` });

const UNEXPECTED_STATUSES = [405, 406, 415, 500, 503];

const pointer = (...parts: string[]) => parts.map((part) => part.replace(/~/g, "~0").replace(/\//g, "~1")).join("/");

/** The operation of an envelope: path template, method and the operation object. */
function operationOf(operationId: string): { path: string; method: string; operation: Json } {
  for (const [path, item] of Object.entries(contract.paths as Record<string, Json>)) {
    for (const method of ["get", "post", "put", "patch", "delete"]) {
      const operation = item[method] as Json | undefined;
      if (operation?.operationId === operationId) return { path, method, operation };
    }
  }
  throw new Error(`Unknown operation ${operationId}`);
}

/** The JSON pointer of the response schema for a status, following a components/responses $ref. */
function responseSchemaPointer(path: string, method: string, operation: Json, status: number): string | null {
  const responses = operation.responses as Record<string, Json>;
  const key = String(status) in responses ? String(status) : "default";
  let response = responses[key];
  let location = `/paths/${pointer(path)}/${method}/responses/${key}`;
  if (typeof response?.$ref === "string") {
    const name = (response.$ref as string).split("/").pop() as string;
    response = (contract.components as Json & { responses: Record<string, Json> }).responses[name];
    location = `/components/responses/${pointer(name)}`;
  }
  const content = response?.content as Record<string, Json> | undefined;
  if (!content) return null;
  const mediaType = Object.keys(content)[0];
  return `${location}/content/${pointer(mediaType)}/schema`;
}

function validate(schemaPointer: string, data: unknown): string[] {
  const check = ajv.compile({ $ref: `${BASE}openapi.yaml#${schemaPointer}` });
  return check(data) ? [] : (check.errors ?? []).map((error) => `${error.instancePath} ${error.message}`);
}

const files = readdirSync(MOCKS).filter((name) => name.endsWith(".json")).sort();

describe("synthetic mocks match the contract", () => {
  it("cover every contract operation with at least one success answer", () => {
    const operations = new Set<string>();
    for (const [, item] of Object.entries(contract.paths as Record<string, Json>)) {
      for (const method of ["get", "post", "put", "patch", "delete"]) {
        const operation = item[method] as Json | undefined;
        if (operation) operations.add(operation.operationId as string);
      }
    }
    const succeeding = new Set(files.map((file) => JSON.parse(readFileSync(join(MOCKS, file), "utf8")) as MockEnvelope)
      .filter((envelope) => envelope.response.status < 300)
      .map((envelope) => envelope.operationId));
    expect(operations.size).toBe(54);
    expect([...operations].filter((id) => !succeeding.has(id))).toEqual([]);
  });

  it.each(files)("%s", (file) => {
    const envelope = JSON.parse(readFileSync(join(MOCKS, file), "utf8")) as MockEnvelope;
    expect(envelope.synthetic).toBe(true);
    const { path, method, operation } = operationOf(envelope.operationId);
    expect(envelope.request.method.toLowerCase()).toBe(method);

    const template = new RegExp(`^${path.replace(/\{\w+\}/g, "[^/?]+")}(\\?.*)?$`);
    expect(envelope.request.path).toMatch(template);

    const { status, headers, body } = envelope.response;
    const declared = Object.keys(operation.responses as Json);
    if (!declared.includes(String(status))) {
      // components/responses/UnexpectedError, used as `default`, names exactly these statuses.
      expect(declared).toContain("default");
      expect(UNEXPECTED_STATUSES).toContain(status);
    }
    expect(headers["X-Correlation-Id"]).toBeTruthy();

    const schema = responseSchemaPointer(path, method, operation, status);
    if (body === null) {
      expect(schema).toBeNull();
    } else {
      expect(schema).not.toBeNull();
      expect(headers["Content-Type"]).toMatch(status >= 400 ? /application\/problem\+json/ : /application\/json/);
      expect(validate(schema as string, body)).toEqual([]);
    }

    if (status < 300 && envelope.request.body !== undefined && envelope.request.body !== null) {
      const content = (operation.requestBody as Json | undefined)?.content as Record<string, Json> | undefined;
      expect(content).toBeDefined();
      const requestPointer = `/paths/${pointer(path)}/${method}/requestBody/content/${pointer(Object.keys(content as Json)[0])}/schema`;
      expect(validate(requestPointer, envelope.request.body)).toEqual([]);
    }
  });
});

/** Negative controls: the validator above must be able to fail (a test that cannot fail proves nothing). */
describe("the contract validator rejects broken mocks", () => {
  const hero = JSON.parse(readFileSync(join(MOCKS, "selling-options-compare.200-recommendation.json"), "utf8")) as MockEnvelope;
  const { path, method, operation } = operationOf(hero.operationId);
  const schema = responseSchemaPointer(path, method, operation, 200) as string;
  type Body = { options: Record<string, unknown>[] } & Record<string, unknown>;
  const mutate = (change: (body: Body) => void) => {
    const body = structuredClone(hero.response.body) as Body;
    change(body);
    return validate(schema, body);
  };

  it("accepts the unchanged answer", () => {
    expect(mutate(() => undefined)).toEqual([]);
  });
  it("rejects a renamed field", () => {
    expect(mutate((body) => {
      const estimates = body.options[0].estimates as Record<string, unknown>;
      estimates.netRealization = estimates.estimatedNetRealization;
      delete estimates.estimatedNetRealization;
    })).not.toEqual([]);
  });
  it("rejects a third decimal place on a distance", () => {
    expect(mutate((body) => { body.options[0].distanceKm = 42.125; })).not.toEqual([]);
  });
  it("rejects an unknown field", () => {
    expect(mutate((body) => { body.bestMarket = "TEST Market B"; })).not.toEqual([]);
  });
  it("rejects a value outside an enum", () => {
    expect(mutate((body) => { body.options[0].trendDirection = "UP"; })).not.toEqual([]);
  });
});
