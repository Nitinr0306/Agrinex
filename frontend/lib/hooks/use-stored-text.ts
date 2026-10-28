"use client";

import { useSyncExternalStore } from "react";

export type StorageArea = "local" | "session";

function subscribe(onChange: () => void): () => void {
  window.addEventListener("storage", onChange);
  return () => window.removeEventListener("storage", onChange);
}

function readText(area: StorageArea, key: string): string | null {
  try {
    return (area === "local" ? localStorage : sessionStorage).getItem(key);
  } catch {
    return null; // Storage may be unavailable (private mode, blocked site data).
  }
}

/**
 * The text stored under `key`, read without a hydration mismatch: null on the server and while hydrating, the
 * stored text afterwards (react.dev `useSyncExternalStore`, getServerSnapshot). The snapshot is the raw string,
 * so it compares stably; callers parse it. Changes from other tabs arrive through the `storage` event.
 */
export function useStoredText(area: StorageArea, key: string): string | null {
  return useSyncExternalStore(subscribe, () => readText(area, key), () => null);
}
