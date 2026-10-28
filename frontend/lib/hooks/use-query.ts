"use client";

import { useCallback, useEffect, useEffectEvent, useState } from "react";

export type QueryState<T> =
  | { status: "idle"; data: undefined; error: undefined }
  | { status: "loading"; data: T | undefined; error: undefined }
  | { status: "success"; data: T; error: undefined }
  | { status: "error"; data: T | undefined; error: unknown };

interface Settled<T> {
  /** The request this answer belongs to: the attempt and the key. */
  request: string;
  data: T | undefined;
  error: unknown;
  failed: boolean;
}

const IDLE = { status: "idle", data: undefined, error: undefined } as const;

/**
 * Loads data for a screen and aborts the request when its inputs change or the screen unmounts. `key` names the
 * inputs; a null key means "nothing to load yet". `retry` runs the same request again. While a new request
 * loads, `data` keeps the last answer, so a refreshed table does not flash empty.
 *
 * The status is derived during render from the request the last answer belongs to; state is only set when a
 * request settles (react.dev, eslint-plugin-react-hooks `set-state-in-effect`).
 */
export function useQuery<T>(key: string | null, load: (signal: AbortSignal) => Promise<T>) {
  const [attempt, setAttempt] = useState(0);
  const [settled, setSettled] = useState<Settled<T> | null>(null);
  const run = useEffectEvent((signal: AbortSignal) => load(signal));
  const request = key === null ? null : `${attempt}:${key}`;

  useEffect(() => {
    if (request === null) return;
    const controller = new AbortController();
    run(controller.signal).then(
      (data) => {
        if (!controller.signal.aborted) setSettled({ request, data, error: undefined, failed: false });
      },
      (error: unknown) => {
        if (!controller.signal.aborted) setSettled((previous) => ({ request, data: previous?.data, error, failed: true }));
      },
    );
    return () => controller.abort();
  }, [request]);

  const retry = useCallback(() => setAttempt((value) => value + 1), []);

  let state: QueryState<T>;
  if (request === null) {
    state = IDLE;
  } else if (settled?.request !== request) {
    state = { status: "loading", data: settled?.data, error: undefined };
  } else if (settled.failed) {
    state = { status: "error", data: settled.data, error: settled.error };
  } else {
    state = { status: "success", data: settled.data as T, error: undefined };
  }
  return { ...state, retry };
}

/** True once `active` has stayed true for `delayMs` (skeletons only after 300 ms; APPROVED PDD-13). */
export function useDelayedFlag(active: boolean, delayMs = 300): boolean {
  const [elapsed, setElapsed] = useState(false);
  const [wasActive, setWasActive] = useState(active);
  // Adjusting state when a prop changes, during render (react.dev "You might not need an effect").
  if (active !== wasActive) {
    setWasActive(active);
    if (!active) setElapsed(false);
  }
  useEffect(() => {
    if (!active) return;
    const timer = setTimeout(() => setElapsed(true), delayMs);
    return () => clearTimeout(timer);
  }, [active, delayMs]);
  return active && elapsed;
}

/** Seconds left of a `Retry-After` wait, counting down once a second (E11, E31). */
export function useCountdown(seconds: number | null): number {
  const start = seconds !== null && seconds > 0 ? seconds : 0;
  const [left, setLeft] = useState(start);
  const [counting, setCounting] = useState(seconds);
  if (seconds !== counting) {
    setCounting(seconds);
    setLeft(start);
  }
  useEffect(() => {
    if (seconds === null || seconds <= 0) return;
    const deadline = Date.now() + seconds * 1000;
    const timer = setInterval(() => {
      const remaining = Math.max(0, Math.ceil((deadline - Date.now()) / 1000));
      setLeft(remaining);
      if (remaining === 0) clearInterval(timer);
    }, 1000);
    return () => clearInterval(timer);
  }, [seconds]);
  return left;
}
