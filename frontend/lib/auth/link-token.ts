"use client";

import { useEffect, useRef, useState } from "react";

const TOKEN_FRAGMENT = /^#token=([A-Za-z0-9_-]{43})$/;

/**
 * The single-use token of an emailed link (ADR-0048 item 2; FRONTEND_CONSUMPTION §4 step 7). It arrives in the URL
 * fragment, which no server ever receives; this hook reads it once, removes it from the address bar at once
 * (`history.replaceState`), and keeps it only in memory. It is never logged, stored or shown. A missing or
 * malformed token reads as null. The ref keeps the token across React's development double effect.
 */
export function useLinkToken(): { token: string | null; ready: boolean } {
  const tokenRef = useRef<string | null | undefined>(undefined);
  const [state, setState] = useState<{ token: string | null; ready: boolean }>({ token: null, ready: false });

  useEffect(() => {
    if (tokenRef.current === undefined) {
      const { hash, pathname, search } = window.location;
      tokenRef.current = TOKEN_FRAGMENT.exec(hash)?.[1] ?? null;
      if (hash) window.history.replaceState(window.history.state, "", `${pathname}${search}`);
    }
    setState({ token: tokenRef.current, ready: true });
  }, []);

  return state;
}
