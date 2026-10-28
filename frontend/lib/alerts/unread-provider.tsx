"use client";

import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { alertApi } from "@/lib/api/endpoints";
import { useSession } from "@/lib/auth/session-provider";

/** APPROVED PDD-01: poll on start, on focus, and every 5 minutes while a page is open and visible. */
const POLL_INTERVAL_MS = 5 * 60 * 1000;

interface UnreadValue {
  /** Unread alert events (`listAlertEvents?unreadOnly=true` → `totalItems`), or null when unknown. */
  unreadCount: number | null;
  refreshUnread: () => void;
}

const UnreadContext = createContext<UnreadValue>({ unreadCount: null, refreshUnread: () => undefined });

export function AlertsUnreadProvider({ children }: { children: ReactNode }) {
  const { status, user } = useSession();
  const userId = status === "signedIn" ? user?.id ?? null : null;
  // The count belongs to the account it was fetched for; any other state shows no count.
  const [known, setKnown] = useState<{ userId: string; count: number } | null>(null);
  const [tick, setTick] = useState(0);
  const refreshUnread = useCallback(() => setTick((value) => value + 1), []);
  const unreadCount = known !== null && known.userId === userId ? known.count : null;

  useEffect(() => {
    if (userId === null) return;
    const controller = new AbortController();
    alertApi
      .listAlertEvents({ unreadOnly: true, size: 1 }, controller.signal)
      .then((page) => setKnown({ userId, count: page.totalItems }))
      .catch(() => {
        // The badge is informative only; a failed poll leaves the last known count.
      });
    return () => controller.abort();
  }, [userId, tick]);

  useEffect(() => {
    if (status !== "signedIn") return;
    let timer: ReturnType<typeof setInterval> | null = null;
    const start = () => {
      if (timer === null) timer = setInterval(refreshUnread, POLL_INTERVAL_MS);
    };
    const stop = () => {
      if (timer !== null) clearInterval(timer);
      timer = null;
    };
    const onVisibility = () => {
      if (document.visibilityState === "visible") {
        refreshUnread();
        start();
      } else {
        stop();
      }
    };
    if (document.visibilityState === "visible") start();
    document.addEventListener("visibilitychange", onVisibility);
    window.addEventListener("focus", refreshUnread);
    return () => {
      stop();
      document.removeEventListener("visibilitychange", onVisibility);
      window.removeEventListener("focus", refreshUnread);
    };
  }, [status, refreshUnread]);

  const value = useMemo(() => ({ unreadCount, refreshUnread }), [unreadCount, refreshUnread]);
  return <UnreadContext.Provider value={value}>{children}</UnreadContext.Provider>;
}

export function useUnreadAlerts(): UnreadValue {
  return useContext(UnreadContext);
}
