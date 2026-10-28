import "server-only";

import en from "@/public/locales/en.json";
import hi from "@/public/locales/hi.json";
import mr from "@/public/locales/mr.json";
import type { Locale } from "./config";

/** Every interface string, keyed as in role file 02 §10. English is the reference catalogue. */
export type Messages = typeof en;
export type MessageKey = keyof Messages;

/* The assignments below fail to compile if Marathi or Hindi lacks a key that English has (spec §48: no
   untranslated strings on farmer screens). */
const catalogues: Record<Locale, Messages> = { en, mr, hi };

/** Server only: only the active language's strings are sent to the browser. */
export function messagesFor(locale: Locale): Messages {
  return catalogues[locale];
}
