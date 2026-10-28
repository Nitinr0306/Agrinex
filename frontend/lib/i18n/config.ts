/**
 * Languages (spec F1.2: Marathi, Hindi, English). The chosen language persists on the device in a cookie the
 * server reads to render the right language (role file 02 S01; FRONTEND_ARCHITECTURE §4.6 leaves the mechanism
 * to the implementation). It holds no personal data.
 */
export const LOCALES = ["mr", "hi", "en"] as const;
export type Locale = (typeof LOCALES)[number];

export const DEFAULT_LOCALE: Locale = "en";
export const LOCALE_COOKIE = "agrinex_locale";
const ONE_YEAR_SECONDS = 365 * 24 * 60 * 60;

/** Each language named in its own script, with the English hint S01 shows. */
export const LOCALE_NAMES: Record<Locale, { native: string; english: string }> = {
  mr: { native: "मराठी", english: "Marathi" },
  hi: { native: "हिंदी", english: "Hindi" },
  en: { native: "English", english: "English" },
};

export function parseLocale(value: string | undefined | null): Locale | null {
  return LOCALES.find((locale) => locale === value) ?? null;
}

/** Intl locale with Latin digits in every language (APPROVED PDD-06) and Indian grouping. */
export function intlLocale(locale: Locale): string {
  return locale === "en" ? "en-IN" : `${locale}-IN-u-nu-latn`;
}

/** Persists the choice on this device (client only). */
export function persistLocale(locale: Locale): void {
  document.cookie = `${LOCALE_COOKIE}=${locale}; Path=/; Max-Age=${ONE_YEAR_SECONDS}; SameSite=Lax`;
}
