"use client";

import { useState } from "react";
import { ArrowRightIcon, DocumentIcon } from "@/components/icons";
import { Button } from "@/components/ui/button";
import { ChipGroup } from "@/components/ui/choice";
import { Card, EmptyState } from "@/components/ui/feedback";
import { useI18n } from "@/lib/i18n/i18n-provider";
import { publishedNameLang } from "@/lib/i18n/names";
import { Message } from "@/components/ui/text";

const RADII = [25, 50, 100, 150, 200, 300];

/** E05: 422 NO_MARKETS_IN_RANGE, with the adjust-distance control (spec §25 L; approved draft E05, layout). */
export function NoMarketInRange({ km, districtName, busy, disabled, onSearch, onChange }: {
  km: number;
  districtName: string;
  busy: boolean;
  disabled: boolean;
  onSearch: (km: number) => void;
  onChange: () => void;
}) {
  const { t, locale } = useI18n();
  const larger = RADII.filter((radius) => radius > km);
  const choices = larger.length > 0 ? larger : RADII;
  const [radius, setRadius] = useState(choices[0]);
  return (
    <div className="flex flex-col gap-4">
      <EmptyState icon={<DocumentIcon size={28} />} title={t("state.no_market")}>
        <Message
          id="results.no_market_detail"
          values={{ n: km, district: <span lang={publishedNameLang(locale)}>{districtName}</span> }}
        />
      </EmptyState>
      <Card className="flex flex-col gap-4 p-4" aria-labelledby="adjust-distance">
        <div className="flex items-baseline justify-between gap-2 border-b border-rule pb-2">
          <h2 id="adjust-distance" className="eyebrow">{t("results.adjust_distance")}</h2>
          <span className="text-label text-ink-2">{t("results.current_km", { n: km })}</span>
        </div>
        <ChipGroup
          label={t("results.adjust_distance")}
          hideLabel
          options={choices.map((value) => ({ value, label: t("input.km_choice", { n: value }) }))}
          value={radius}
          onChange={setRadius}
        />
        <Button block busy={busy} disabled={disabled} onClick={() => onSearch(radius)}>
          {t("results.search_within", { n: radius })} <ArrowRightIcon size={18} />
        </Button>
        <Button variant="tertiary" block onClick={onChange}>{t("results.change_crop_place")}</Button>
      </Card>
      <p className="text-caption text-ink-2">{t("input.distance_help")}</p>
    </div>
  );
}
