"use client";

import { useRouter } from "next/navigation";
import { useEffect, useRef, useState } from "react";
import { LOCALE_NAMES, LOCALES, persistLocale, type Locale } from "@/lib/i18n/config";
import { useI18n } from "@/lib/i18n/i18n-provider";
import { CheckIcon } from "@/components/icons";

/** Switches the interface language; the choice persists on this device (role file 02 S01, S12). */
export function useChangeLocale() {
  const router = useRouter();
  return (locale: Locale) => {
    persistLocale(locale);
    document.documentElement.lang = locale;
    router.refresh();
  };
}

const SHORT: Record<Locale, string> = { mr: "मरा", hi: "हिं", en: "EN" };

/** The language control of the app bar: a segmented bar on desktop, a compact menu on small screens. */
export function LanguageSwitch() {
  const { locale, t } = useI18n();
  const changeLocale = useChangeLocale();
  const [open, setOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!open) return;
    const close = (event: MouseEvent | KeyboardEvent) => {
      if (event instanceof KeyboardEvent && event.key !== "Escape") return;
      if (event instanceof MouseEvent && containerRef.current?.contains(event.target as Node)) return;
      setOpen(false);
    };
    document.addEventListener("mousedown", close);
    document.addEventListener("keydown", close);
    return () => {
      document.removeEventListener("mousedown", close);
      document.removeEventListener("keydown", close);
    };
  }, [open]);

  return (
    <>
      <div role="group" aria-label={t("lang.label")} className="hidden rounded-control border border-rule-strong bg-sunken p-0.5 desktop:flex">
        {LOCALES.map((option) => (
          <button
            key={option}
            type="button"
            lang={option}
            aria-pressed={option === locale}
            onClick={() => changeLocale(option)}
            className={`min-h-10 rounded-control px-3 text-label font-semibold ${option === locale ? "bg-action text-inverse" : "text-ink hover:bg-raised"}`}
          >
            {LOCALE_NAMES[option].native}
          </button>
        ))}
      </div>
      <div ref={containerRef} className="relative desktop:hidden">
        <button
          type="button"
          aria-haspopup="true"
          aria-expanded={open}
          aria-label={t("lang.current", { name: LOCALE_NAMES[locale].native })}
          onClick={() => setOpen((value) => !value)}
          className="flex min-h-12 min-w-12 items-center justify-center rounded-control border border-rule-strong bg-raised px-2 text-label font-semibold"
        >
          <span lang={locale}>{SHORT[locale]}</span>
        </button>
        {open ? (
          <ul className="absolute right-0 top-full z-20 mt-1 w-44 rounded-card border border-rule-strong bg-raised py-1">
            {LOCALES.map((option) => (
              <li key={option}>
                <button
                  type="button"
                  lang={option}
                  aria-current={option === locale || undefined}
                  onClick={() => {
                    setOpen(false);
                    changeLocale(option);
                  }}
                  className="flex min-h-12 w-full items-center justify-between px-4 text-left text-body hover:bg-sunken"
                >
                  {LOCALE_NAMES[option].native}
                  {option === locale ? <CheckIcon size={18} /> : null}
                </button>
              </li>
            ))}
          </ul>
        ) : null}
      </div>
    </>
  );
}
