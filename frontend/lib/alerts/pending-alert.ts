/**
 * The alert a signed-out farmer filled in before signing in (journey J3: S04 → "Set alert" → S10 → S11). It is
 * kept for this browser tab only, holds no personal data, and is created right after sign-in or registration.
 * The email typed at registration is kept the same way, only to show S11's "We sent a link to {email}" notice
 * (the API never returns the email; role file 02 S10).
 */
import { useMemo } from "react";
import type { AlertType } from "@/lib/api/types";
import { useStoredText } from "@/lib/hooks/use-stored-text";
import { ALERT_TYPES } from "./wording";

export interface PendingAlert {
  alertType: AlertType;
  commodityId: string;
  marketId: string | null;
  thresholdValue: number;
  cropName: string;
  marketName: string | null;
}

const PENDING_KEY = "agrinex.pendingAlert";
const VERIFY_KEY = "agrinex.verificationNotice";

export function savePendingAlert(alert: PendingAlert): void {
  write(PENDING_KEY, JSON.stringify(alert));
}

export function readPendingAlert(): PendingAlert | null {
  return parsePendingAlert(read(PENDING_KEY));
}

/** The pending alert for a screen that may render on the server (S10): null until hydrated. */
export function usePendingAlert(): PendingAlert | null {
  const text = useStoredText("session", PENDING_KEY);
  return useMemo(() => parsePendingAlert(text), [text]);
}

/** Storage is untrusted input: only a well-formed alert is used. */
function parsePendingAlert(value: string | null): PendingAlert | null {
  if (!value) return null;
  try {
    const parsed = JSON.parse(value) as Partial<Record<keyof PendingAlert, unknown>>;
    return (ALERT_TYPES as readonly unknown[]).includes(parsed.alertType) && typeof parsed.commodityId === "string"
      && (parsed.marketId === null || typeof parsed.marketId === "string")
      && typeof parsed.thresholdValue === "number" && Number.isFinite(parsed.thresholdValue)
      && typeof parsed.cropName === "string"
      && (parsed.marketName === null || typeof parsed.marketName === "string")
      ? (parsed as PendingAlert)
      : null;
  } catch {
    return null;
  }
}

export function clearPendingAlert(): void {
  remove(PENDING_KEY);
}

export function saveVerificationNotice(email: string): void {
  write(VERIFY_KEY, email);
}

/** Reads the notice without forgetting it (safe to call twice, as React may do with a state initializer). */
export function readVerificationNotice(): string | null {
  return read(VERIFY_KEY);
}

/** Forgets the notice once it has been shown, so it shows once. */
export function clearVerificationNotice(): void {
  remove(VERIFY_KEY);
}

function read(key: string): string | null {
  try {
    return sessionStorage.getItem(key);
  } catch {
    return null;
  }
}

function write(key: string, value: string): void {
  try {
    sessionStorage.setItem(key, value);
  } catch {
    // Without storage the pending alert is simply not kept; the farmer can set it again after signing in.
  }
}

function remove(key: string): void {
  try {
    sessionStorage.removeItem(key);
  } catch {
    // Nothing to remove.
  }
}
