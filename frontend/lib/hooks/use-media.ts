"use client";

import { useSyncExternalStore } from "react";

/** The desktop breakpoint of role file 02 §13 (1024 px), as the theme's `desktop:` variant. */
const DESKTOP = "(min-width: 64rem)";

function subscribe(onChange: () => void): () => void {
  const media = window.matchMedia(DESKTOP);
  media.addEventListener("change", onChange);
  return () => media.removeEventListener("change", onChange);
}

/**
 * Whether the desktop layout applies. The server and the first client render assume the mobile layout
 * (mobile-first), so hydration never mismatches; the desktop layout follows immediately on wide screens.
 */
export function useIsDesktop(): boolean {
  return useSyncExternalStore(subscribe, () => window.matchMedia(DESKTOP).matches, () => false);
}
