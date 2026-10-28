import { act, cleanup, renderHook, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { useCountdown, useDelayedFlag, useQuery } from "./use-query";

/** A promise the test settles by hand. */
function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason: unknown) => void;
  const promise = new Promise<T>((yes, no) => {
    resolve = yes;
    reject = no;
  });
  return { promise, resolve, reject };
}

afterEach(() => {
  cleanup();
  vi.useRealTimers();
});

describe("useQuery", () => {
  it("is idle without a key and loads nothing", () => {
    const load = vi.fn();
    const { result } = renderHook(() => useQuery(null, load));
    expect(result.current.status).toBe("idle");
    expect(load).not.toHaveBeenCalled();
  });

  it("goes from loading to success", async () => {
    const answer = deferred<string>();
    const { result } = renderHook(() => useQuery("a", () => answer.promise));
    expect(result.current.status).toBe("loading");
    await act(async () => answer.resolve("A"));
    expect(result.current).toMatchObject({ status: "success", data: "A" });
  });

  it("keeps the last answer while the next key loads, and ignores the answer of an abandoned key", async () => {
    const answers: Record<string, ReturnType<typeof deferred<string>>> = { a: deferred(), b: deferred(), c: deferred() };
    const signals: Record<string, AbortSignal> = {};
    const { result, rerender } = renderHook(({ key }) => useQuery(key, (signal) => {
      signals[key] = signal;
      return answers[key].promise;
    }), { initialProps: { key: "a" } });
    await act(async () => answers.a.resolve("A"));

    rerender({ key: "b" });
    expect(result.current).toMatchObject({ status: "loading", data: "A" });
    rerender({ key: "c" });
    expect(signals.b.aborted).toBe(true);
    await act(async () => answers.b.resolve("B"));
    expect(result.current).toMatchObject({ status: "loading", data: "A" });
    await act(async () => answers.c.resolve("C"));
    expect(result.current).toMatchObject({ status: "success", data: "C" });
  });

  it("reports an error and loads again on retry", async () => {
    let calls = 0;
    const { result } = renderHook(() => useQuery("a", () => {
      calls += 1;
      return calls === 1 ? Promise.reject(new Error("down")) : Promise.resolve("A");
    }));
    await waitFor(() => expect(result.current.status).toBe("error"));
    act(() => result.current.retry());
    expect(result.current.status).toBe("loading");
    await waitFor(() => expect(result.current).toMatchObject({ status: "success", data: "A" }));
    expect(calls).toBe(2);
  });

  it("uses the latest load function without reloading when only the function changes", async () => {
    const first = vi.fn(() => Promise.resolve("first"));
    const second = vi.fn(() => Promise.resolve("second"));
    const { result, rerender } = renderHook(({ load }) => useQuery("a", load), { initialProps: { load: first } });
    await waitFor(() => expect(result.current.status).toBe("success"));
    rerender({ load: second });
    expect(second).not.toHaveBeenCalled();
    act(() => result.current.retry());
    await waitFor(() => expect(result.current.data).toBe("second"));
  });
});

describe("useDelayedFlag", () => {
  it("turns on only after the delay and off at once, and waits again on the next activation", () => {
    vi.useFakeTimers();
    const { result, rerender } = renderHook(({ active }) => useDelayedFlag(active, 300), { initialProps: { active: true } });
    expect(result.current).toBe(false);
    act(() => vi.advanceTimersByTime(300));
    expect(result.current).toBe(true);
    rerender({ active: false });
    expect(result.current).toBe(false);
    rerender({ active: true });
    expect(result.current).toBe(false);
    act(() => vi.advanceTimersByTime(299));
    expect(result.current).toBe(false);
  });
});

describe("useCountdown", () => {
  it("counts down from Retry-After to zero", () => {
    vi.useFakeTimers();
    const { result, rerender } = renderHook(({ seconds }) => useCountdown(seconds), { initialProps: { seconds: null as number | null } });
    expect(result.current).toBe(0);
    rerender({ seconds: 3 });
    expect(result.current).toBe(3);
    act(() => vi.advanceTimersByTime(1000));
    expect(result.current).toBe(2);
    act(() => vi.advanceTimersByTime(2000));
    expect(result.current).toBe(0);
  });
});
