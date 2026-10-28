import "server-only";

import type { Metadata } from "next";
import { cookies } from "next/headers";
import { DEFAULT_LOCALE, LOCALE_COOKIE, parseLocale, type Locale } from "./config";
import { messagesFor, type MessageKey } from "./messages";

/** The language chosen on this device, read on the server for the first render. */
export async function currentLocale(): Promise<Locale> {
  return parseLocale((await cookies()).get(LOCALE_COOKIE)?.value) ?? DEFAULT_LOCALE;
}

/** A page title in the reader's language (farmer screens have no untranslated strings; spec §48). */
export async function localizedTitle(key: MessageKey): Promise<Metadata> {
  return { title: messagesFor(await currentLocale())[key] };
}
