import { describe, expect, it } from "vitest";
import { describeError } from "./describe-error";
import { ApiError, NetworkError } from "./errors";

const api = (status: number) => new ApiError(status, null, [], "correlation-1", status === 429 ? 30 : null, null);

/** Role file 02 E11 and E16–E20: the message chosen for each failure. */
describe("describeError", () => {
  it.each([
    [503, "error.unavailable"],
    [502, "error.unavailable"],
    [504, "error.unavailable"],
    [500, "error.server"],
    [429, "error.retry_in"],
    [403, "error.forbidden"],
    [404, "error.not_found"],
  ])("maps HTTP %i to %s and keeps the correlation id", (status, key) => {
    expect(describeError(api(status))).toMatchObject({ key, correlationId: "correlation-1" });
  });

  it("maps a failure without a response to the network message", () => {
    expect(describeError(new NetworkError(new TypeError("Failed to fetch"))).key).toBe("error.network");
  });

  it("carries Retry-After for 429", () => {
    expect(describeError(api(429)).retryAfterSeconds).toBe(30);
  });
});
