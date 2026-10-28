import type { ErrorCode, FieldError } from "./types";

const ERROR_CODES: readonly ErrorCode[] = [
  "MALFORMED_REQUEST",
  "VALIDATION_FAILED",
  "UNAUTHORIZED",
  "INVALID_CREDENTIALS",
  "REFRESH_TOKEN_INVALID",
  "FORBIDDEN",
  "ORIGIN_NOT_ALLOWED",
  "NOT_FOUND",
  "METHOD_NOT_ALLOWED",
  "NOT_ACCEPTABLE",
  "CONFLICT",
  "UNSUPPORTED_MEDIA_TYPE",
  "NO_MARKETS_IN_RANGE",
  "UNSUPPORTED_PRICE_UNIT",
  "REFERENCE_NOT_FOUND",
  "ESTIMATE_OUT_OF_RANGE",
  "PASSWORD_RESET_TOKEN_INVALID",
  "EMAIL_VERIFICATION_TOKEN_INVALID",
  "RATE_LIMITED",
  "INTERNAL_ERROR",
  "SERVICE_UNAVAILABLE",
];

/**
 * A non-2xx answer from the API (`ApiErrorResponse`, RFC 9457). `code` is null when the body is missing or
 * carries a code this client does not know; screens then fall back to the HTTP status (FRONTEND_CONSUMPTION §5).
 * `detail` is English developer text: admin screens may show it, farmer screens never do.
 */
export class ApiError extends Error {
  constructor(
    readonly status: number,
    readonly code: ErrorCode | null,
    readonly fieldErrors: FieldError[],
    readonly correlationId: string | null,
    readonly retryAfterSeconds: number | null,
    readonly detail: string | null,
  ) {
    super(`API ${status}${code ? ` ${code}` : ""}`);
    this.name = "ApiError";
  }

  hasCode(...codes: ErrorCode[]): boolean {
    return this.code !== null && codes.includes(this.code);
  }

  fieldError(field: string): FieldError | undefined {
    return this.fieldErrors.find((error) => error.field === field);
  }
}

/** The request never got an HTTP answer (offline, DNS, connection reset). */
export class NetworkError extends Error {
  constructor(cause?: unknown) {
    super("Network request failed", { cause });
    this.name = "NetworkError";
  }
}

/** A 2xx answer whose body does not have the shape the contract promises. Treated like a server fault. */
export class ContractViolationError extends Error {
  constructor(message: string) {
    super(message);
    this.name = "ContractViolationError";
  }
}

export function isApiError(error: unknown, ...codes: ErrorCode[]): error is ApiError {
  return error instanceof ApiError && (codes.length === 0 || error.hasCode(...codes));
}

/** Reads an error answer defensively: the body is validated before any of it is trusted. */
export function toApiError(status: number, headers: Headers, body: unknown): ApiError {
  const record = isRecord(body) ? body : {};
  const code = typeof record.code === "string" && (ERROR_CODES as string[]).includes(record.code)
    ? (record.code as ErrorCode)
    : null;
  const fieldErrors = Array.isArray(record.errors)
    ? record.errors.filter(isFieldError)
    : [];
  const correlationId = typeof record.correlationId === "string"
    ? record.correlationId
    : headers.get("X-Correlation-Id");
  const detail = typeof record.detail === "string" ? record.detail : null;
  return new ApiError(status, code, fieldErrors, correlationId, parseRetryAfter(headers.get("Retry-After")), detail);
}

/** `Retry-After` in delta-seconds (the backend never sends an HTTP date). */
export function parseRetryAfter(value: string | null): number | null {
  if (value === null || !/^\d+$/.test(value.trim())) return null;
  return Number(value.trim());
}

export function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function isFieldError(value: unknown): value is FieldError {
  return isRecord(value) && typeof value.field === "string" && typeof value.code === "string"
    && typeof value.message === "string";
}
