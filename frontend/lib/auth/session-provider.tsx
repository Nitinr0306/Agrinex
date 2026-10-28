"use client";

import { useEffect, useSyncExternalStore } from "react";
import {
  bootstrapSession,
  getSessionState,
  signOut,
  subscribeSession,
  type SessionState,
} from "./session-store";

const SERVER_SNAPSHOT: SessionState = { status: "loading", user: null };

/** Restores the session once per page load: a refresh with the HttpOnly cookie (role file 02 §16 "Session"). */
export function SessionBootstrap() {
  useEffect(() => {
    void bootstrapSession();
  }, []);
  return null;
}

export function useSession(): SessionState & { signOut: () => Promise<void> } {
  const state = useSyncExternalStore(subscribeSession, getSessionState, () => SERVER_SNAPSHOT);
  return { ...state, signOut };
}
