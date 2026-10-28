"use client";

import { useI18n } from "@/lib/i18n/i18n-provider";

/** Every mock-mode screen is labelled as a prototype on synthetic data (CLAUDE.md §M; role file 02 §7). */
export function PrototypeBanner() {
  const { t } = useI18n();
  return (
    <div className="border-b border-rule-strong bg-sunken px-4 py-1.5 text-center" role="note">
      <p className="eyebrow text-ink">{t("prototype.tag")}</p>
      <p className="text-caption text-ink-2">{t("prototype.note")}</p>
    </div>
  );
}

export function SkipLink() {
  const { t } = useI18n();
  return (
    <a
      href="#main"
      className="sr-only-focusable fixed left-2 top-2 z-50 rounded-control bg-action px-4 py-3 text-body font-semibold text-inverse"
    >
      {t("nav.skip")}
    </a>
  );
}
