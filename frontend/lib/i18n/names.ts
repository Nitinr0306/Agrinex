import type { Locale } from "./config";

interface LocalizedNames {
  name: string;
  nameMr?: string | null;
  nameHi?: string | null;
}

/**
 * States and commodities use the reviewed Marathi or Hindi name when it exists, otherwise the official name
 * (ADR-0015). A fallback name is shown as-is and marked as English (APPROVED PDD-05); `lang` is undefined when
 * the localized name is used.
 */
export function localizedName(item: LocalizedNames, locale: Locale): { text: string; lang: string | undefined } {
  const localized = locale === "mr" ? item.nameMr : locale === "hi" ? item.nameHi : null;
  if (localized) return { text: localized, lang: undefined };
  return { text: item.name, lang: locale === "en" ? undefined : "en" };
}

/** District, market, variety and grade names are shown exactly as officially published (ADR-0015). */
export function publishedNameLang(locale: Locale): string | undefined {
  return locale === "en" ? undefined : "en";
}
