"use client";

import { createContext, useContext, useMemo, type ReactNode } from "react";
import type { Locale } from "./config";
import { createFormatter, type Formatter } from "./format";
import type { MessageKey, Messages } from "./messages";

export type MessageParams = Record<string, string | number>;
export type Translate = (key: MessageKey, params?: MessageParams) => string;

interface I18nValue {
  locale: Locale;
  t: Translate;
  /** The untranslated template, for messages whose placeholders are React nodes (components/ui/text Message). */
  raw: (key: MessageKey) => string;
  fmt: Formatter;
}

const I18nContext = createContext<I18nValue | null>(null);

export function I18nProvider({ locale, messages, children }: {
  locale: Locale;
  messages: Messages;
  children: ReactNode;
}) {
  const value = useMemo<I18nValue>(() => ({
    locale,
    t: (key, params) => interpolate(messages[key], params),
    raw: (key) => messages[key],
    fmt: createFormatter(locale),
  }), [locale, messages]);
  return <I18nContext.Provider value={value}>{children}</I18nContext.Provider>;
}

export function useI18n(): I18nValue {
  const value = useContext(I18nContext);
  if (!value) throw new Error("useI18n must be used inside I18nProvider");
  return value;
}

/** Replaces each {name} with its value. A missing value is left visible rather than silently dropped. */
export function interpolate(template: string, params?: MessageParams): string {
  if (!params) return template;
  return template.replace(/\{(\w+)\}/g, (whole, name: string) => (name in params ? String(params[name]) : whole));
}
