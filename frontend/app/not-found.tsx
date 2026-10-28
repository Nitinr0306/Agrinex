"use client";

import { InfoIcon } from "@/components/icons";
import { PageShell } from "@/components/layout/page-shell";
import { ButtonLink } from "@/components/ui/button";
import { EmptyState } from "@/components/ui/feedback";
import { useI18n } from "@/lib/i18n/i18n-provider";

/** A path that is not a screen of AgriNex. */
export default function NotFound() {
  const { t } = useI18n();
  return (
    <PageShell title="AgriNex" backHref="/">
      <EmptyState
        icon={<InfoIcon size={28} />}
        title={t("error.not_found")}
        actions={(
          <>
            <ButtonLink href="/">{t("action.home")}</ButtonLink>
            <ButtonLink href="/compare" variant="secondary">{t("action.compare")}</ButtonLink>
          </>
        )}
      />
    </PageShell>
  );
}
