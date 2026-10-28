import type { MessageKey } from "@/lib/i18n/messages";
import { ApiError, ContractViolationError, NetworkError } from "./errors";

export interface ErrorDescription {
  key: MessageKey;
  correlationId: string | null;
  retryAfterSeconds: number | null;
}

/**
 * The farmer-facing message for a failure that has no screen-specific meaning (role file 02 §9): never the
 * English `detail`, always the localized message by status or code, with the correlation id for support.
 */
export function describeError(error: unknown): ErrorDescription {
  if (error instanceof NetworkError) {
    return { key: "error.network", correlationId: null, retryAfterSeconds: null };
  }
  if (error instanceof ApiError) {
    const base = { correlationId: error.correlationId, retryAfterSeconds: error.retryAfterSeconds };
    // 503 from the API (E18), and 502/504 from the edge proxy when the API cannot be reached (E19: by status).
    if (error.status === 502 || error.status === 503 || error.status === 504) return { key: "error.unavailable", ...base };
    if (error.status === 429) return { key: "error.retry_in", ...base };
    if (error.status === 403) return { key: "error.forbidden", ...base };
    if (error.status === 404) return { key: "error.not_found", ...base };
    return { key: "error.server", ...base };
  }
  if (error instanceof ContractViolationError) {
    return { key: "error.server", correlationId: null, retryAfterSeconds: null };
  }
  return { key: "error.server", correlationId: null, retryAfterSeconds: null };
}
