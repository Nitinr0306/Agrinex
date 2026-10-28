"use client";

import { RefreshIcon } from "@/components/icons";
import { Button } from "@/components/ui/button";
import { Notice } from "@/components/ui/feedback";
import { describeError } from "@/lib/api/describe-error";
import { useCountdown } from "@/lib/hooks/use-query";
import { useI18n } from "@/lib/i18n/i18n-provider";

/**
 * A failed load with a retry (E18 unavailable, E19 server fault, E20 network, 429 wait). The inputs on the
 * screen are kept; the correlation id is shown in small print for support (role file 02 E18, E19).
 */
export function LoadProblem({ error, onRetry }: { error: unknown; onRetry?: () => void }) {
  const { t } = useI18n();
  const description = describeError(error);
  const wait = useCountdown(description.retryAfterSeconds);
  return (
    <Notice
      tone="error"
      live
      title={t(description.key, { n: wait })}
      action={onRetry ? (
        <Button variant="secondary" onClick={onRetry} disabled={wait > 0}>
          <RefreshIcon size={18} />
          {t("action.retry")}
        </Button>
      ) : null}
    >
      {description.correlationId ? (
        <span className="figure text-caption text-ink-2">{t("error.reference", { id: description.correlationId })}</span>
      ) : null}
    </Notice>
  );
}
