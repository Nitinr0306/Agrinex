"use client";

import { useEffect, useState, type ReactNode } from "react";
import { AlertsUnreadProvider } from "@/lib/alerts/unread-provider";
import { SessionBootstrap } from "@/lib/auth/session-provider";
import { ComparisonProvider } from "@/lib/compare/comparison-provider";
import { MOCK_MODE } from "@/lib/env";

/**
 * Client-side application state. In mock mode (NEXT_PUBLIC_USE_MOCK_API=true; spec §44) the synthetic API is
 * started before anything can call the network, so even the page-load session refresh is answered by a mock.
 */
export function Providers({ children }: { children: ReactNode }) {
  const [mockReady, setMockReady] = useState(!MOCK_MODE);

  useEffect(() => {
    if (!MOCK_MODE) return;
    let cancelled = false;
    import("@/lib/mocks/browser")
      .then(({ startMockWorker }) => startMockWorker())
      .then(() => {
        if (!cancelled) setMockReady(true);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  if (!mockReady) return null;
  return (
    <>
      <SessionBootstrap />
      <AlertsUnreadProvider>
        <ComparisonProvider>{children}</ComparisonProvider>
      </AlertsUnreadProvider>
    </>
  );
}
