import { describe, expect, it } from "vitest";
import { latestOnly } from "./latest-only";

describe("latestOnly", () => {
  it("lets only the newest request update, whatever order the answers arrive in", () => {
    const gate = latestOnly<number>();
    const first = gate.start(151);
    const second = gate.start(152);
    expect(first?.isLatest()).toBe(false);
    expect(second?.isLatest()).toBe(true);
  });

  it("does not start a second request for the same key while the first is pending", () => {
    const gate = latestOnly<number>();
    const first = gate.start(200);
    expect(gate.start(200)).toBeNull();
    first?.done();
    expect(gate.start(200)?.isLatest()).toBe(true);
  });

  it("makes pending requests stale on cancel", () => {
    const gate = latestOnly<number>();
    const pending = gate.start(250);
    gate.cancel();
    expect(pending?.isLatest()).toBe(false);
    expect(gate.start(250)).not.toBeNull();
  });
});
