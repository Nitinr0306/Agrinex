/**
 * Account input rules, the same as the contract's (RegisterRequest, LoginRequest; ADR-0016, ADR-0047). The
 * server stays the authority; these checks only give the farmer an answer before a round trip.
 */
const PHONE_DIGITS = /^[6-9]\d{9}$/;
const E164_INDIA = /^\+91[6-9]\d{9}$/;
const EMAIL = /^[^@\s]+@[^@\s]+$/;
export const FARMER_PASSWORD_MIN = 8;
export const PASSWORD_MAX = 64;
const PASSWORD_MAX_BYTES = 72;

/** The ten digits after "+91" as the E.164 number the API expects. */
export function phoneToE164(digits: string): string {
  return `+91${digits}`;
}

export function isValidPhoneDigits(digits: string): boolean {
  return PHONE_DIGITS.test(digits);
}

export function isValidEmail(email: string): boolean {
  const trimmed = email.trim();
  return trimmed.length >= 3 && trimmed.length <= 254 && EMAIL.test(trimmed);
}

export type PasswordProblem = "TOO_SHORT" | "TOO_LONG";

/** 8–64 characters and at most 72 UTF-8 bytes; any characters (no composition rules; ADR-0016). */
export function passwordProblem(password: string, minimum = FARMER_PASSWORD_MIN): PasswordProblem | null {
  const characters = [...password].length;
  if (characters < minimum) return "TOO_SHORT";
  if (characters > PASSWORD_MAX || new TextEncoder().encode(password).length > PASSWORD_MAX_BYTES) return "TOO_LONG";
  return null;
}

/**
 * "Mobile number or email": a typed Indian mobile number (with or without +91, spaces or dashes) becomes E.164;
 * anything else is sent as typed, trimmed (ADR-0047: either identifier of the same account).
 */
export function normalizeIdentifier(text: string): string {
  const trimmed = text.trim();
  const compact = trimmed.replace(/[\s-]/g, "");
  if (PHONE_DIGITS.test(compact)) return phoneToE164(compact);
  if (/^91[6-9]\d{9}$/.test(compact)) return `+${compact}`;
  if (E164_INDIA.test(compact)) return compact;
  return trimmed;
}

const SAME_SITE_BASE = "https://agrinex.invalid";

/**
 * Only same-site paths are followed after sign-in, never another origin (no open redirect). Control characters,
 * whitespace and backslashes are refused outright: the URL parser strips tab and newline, and browsers read "\" as
 * "/", so "/<tab>/host" or "/\host" would otherwise become "//host". What remains must resolve to the same origin.
 */
export function safeNextPath(next: string | null): string | null {
  if (!next || !next.startsWith("/") || /[\s\\\u0000-\u001f\u007f]/.test(next)) return null;
  try {
    const url = new URL(next, SAME_SITE_BASE);
    return url.origin === SAME_SITE_BASE ? `${url.pathname}${url.search}${url.hash}` : null;
  } catch {
    return null;
  }
}
