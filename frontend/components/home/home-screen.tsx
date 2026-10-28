"use client";

import { useState } from "react";
import { ArrowRightIcon, CheckIcon, InfoIcon, TruckIcon } from "@/components/icons";
import { AppBar } from "@/components/layout/app-bar";
import { useChangeLocale } from "@/components/layout/language-switch";
import { FreshnessBadge } from "@/components/data/price-bits";
import { PriceScreenFooter, SourceStatusLine } from "@/components/data/source";
import { ButtonLink } from "@/components/ui/button";
import { Card } from "@/components/ui/feedback";
import type { FreshnessStatus } from "@/lib/api/types";
import { useComparison } from "@/lib/compare/comparison-provider";
import { LOCALE_NAMES, LOCALES } from "@/lib/i18n/config";
import { useI18n } from "@/lib/i18n/i18n-provider";

const LEVELS: FreshnessStatus[] = ["FRESH", "RECENT", "STALE", "UNAVAILABLE"];

/** S01 + S02 (role file 02 §8; approved drafts m01 mobile and d01 desktop, layout only). */
export function HomeScreen({ firstLaunch }: { firstLaunch: boolean }) {
  const { t } = useI18n();
  const { lastSourceStatus } = useComparison();
  const [showLanguage] = useState(firstLaunch);

  return (
    <>
      <AppBar />
      <main id="main" tabIndex={-1} className="mx-auto w-full max-w-6xl px-4 pb-16 pt-5 focus:outline-none">
        {showLanguage ? <LanguageChoice /> : null}

        <div className="grid gap-6 desktop:mt-6 desktop:grid-cols-[1.2fr_1fr] desktop:items-start desktop:gap-12">
          <div className="flex flex-col gap-4">
            <h1 className="text-display font-bold text-ink desktop:text-[2.75rem] desktop:leading-[3.25rem]">
              {t("home.title")}
            </h1>
            <p className="text-body text-ink-2 desktop:text-heading">{t("home.subtitle")}</p>
            <div className="mt-2 hidden gap-3 desktop:flex">
              <ButtonLink href="/compare">
                {t("action.compare")} <ArrowRightIcon size={18} />
              </ButtonLink>
              <ButtonLink href="/alerts" variant="secondary">{t("action.alerts")}</ButtonLink>
            </div>
          </div>
          <SumCard />
          <div className="flex flex-col gap-3 desktop:hidden">
            <ButtonLink href="/compare" block>{t("action.compare")}</ButtonLink>
            <ButtonLink href="/alerts" variant="secondary" block>{t("action.alerts")}</ButtonLink>
          </div>
        </div>

        <Card className="mt-8" aria-labelledby="about-prices">
          <h2 id="about-prices" className="border-b border-rule px-4 py-3 text-heading font-semibold desktop:px-6 desktop:pt-5">
            {t("about.title")}
          </h2>
          <ul className="divide-y divide-rule">
            <li className="flex gap-3 px-4 py-3 desktop:px-6">
              <InfoIcon className="mt-0.5 shrink-0 text-ink-2" />
              <span>{t("about.source")}</span>
            </li>
            <li className="flex flex-col gap-3 px-4 py-3 desktop:px-6">
              <span>{t("about.freshness")}</span>
              <ul className="grid gap-2 tablet:grid-cols-2 desktop:grid-cols-4">
                {LEVELS.map((level) => (
                  <li key={level}>
                    <FreshnessBadge status={level} describe />
                  </li>
                ))}
              </ul>
            </li>
            <li className="flex gap-3 px-4 py-3 desktop:px-6">
              <TruckIcon className="mt-0.5 shrink-0 text-ink-2" />
              <span>{t("about.transport")}</span>
            </li>
            {lastSourceStatus ? (
              <li className="bg-sunken px-4 py-2 desktop:px-6">
                <SourceStatusLine status={lastSourceStatus} />
              </li>
            ) : null}
          </ul>
        </Card>

        <PriceScreenFooter />
      </main>
    </>
  );
}

/** S01: three large options in their own script, with an English hint (role file 02 §8 S01). */
function LanguageChoice() {
  const { t, locale } = useI18n();
  const changeLocale = useChangeLocale();
  return (
    <section aria-labelledby="choose-language" className="mb-6">
      <h2 id="choose-language" className="mb-2 text-label text-ink-2">{t("lang.choose")}</h2>
      <div className="grid grid-cols-3 gap-2 desktop:max-w-xl">
        {LOCALES.map((option) => (
          <button
            key={option}
            type="button"
            lang={option}
            aria-pressed={option === locale}
            onClick={() => changeLocale(option)}
            className={`flex min-h-14 items-center justify-center gap-2 rounded-card border px-2 text-heading ${option === locale ? "border-2 border-action font-semibold text-net" : "border-rule-strong bg-raised text-ink hover:bg-sunken"}`}
          >
            {option === locale ? <CheckIcon size={18} /> : null}
            {LOCALE_NAMES[option].native}
          </button>
        ))}
      </div>
    </section>
  );
}

/** The worked sum every result follows (approved home draft, without the internal concept name). */
function SumCard() {
  const { t } = useI18n();
  return (
    <Card className="overflow-hidden" aria-labelledby="sum-title">
      <h2 id="sum-title" className="eyebrow border-b border-rule bg-sunken px-4 py-3">{t("home.sum_title")}</h2>
      <div className="divide-y divide-rule text-body">
        <p className="flex flex-wrap items-baseline justify-between gap-2 px-4 py-3">
          <span>{t("home.sum_price_qty")}</span>
          <span className="font-semibold text-ink-2">{t("home.sum_gross")}</span>
        </p>
        <p className="px-4 py-3">{t("home.sum_transport")}</p>
        <p className="double-rule flex flex-col px-4 py-3">
          <span className="font-semibold text-net">{t("home.sum_net")}</span>
          <span className="text-label text-net">({t("net.gloss")})</span>
        </p>
      </div>
    </Card>
  );
}
