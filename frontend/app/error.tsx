"use client";

import { RefreshIcon } from "@/components/icons";
import { PageShell } from "@/components/layout/page-shell";
import { Button, ButtonLink } from "@/components/ui/button";
import { EmptyState } from "@/components/ui/feedback";
import { useI18n } from "@/lib/i18n/i18n-provider";

/** S13 unexpected failure while rendering a screen (E19): never technical detail to farmers. */
export default function ErrorPage({ error, retry }: { error: Error & { digest?: string }; retry: () => void }) {
  const { t } = useI18n();
  return (
    <PageShell title="AgriNex" backHref="/">
      <EmptyState
        title={t("error.server")}
        actions={(
          <>
            <Button onClick={() => retry()}><RefreshIcon size={18} /> {t("action.retry")}</Button>
            <ButtonLink href="/" variant="secondary">{t("action.home")}</ButtonLink>
          </>
        )}
      >
        {error.digest ? <span className="figure text-caption">{t("error.reference", { id: error.digest })}</span> : null}
      </EmptyState>
    </PageShell>
  );
}
