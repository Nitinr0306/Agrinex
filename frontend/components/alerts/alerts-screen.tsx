"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { BellIcon, InfoIcon, PlusIcon, TrashIcon } from "@/components/icons";
import { PageShell } from "@/components/layout/page-shell";
import { FreshnessBadge, ReportedPrice } from "@/components/data/price-bits";
import { OutageBanner, PriceScreenFooter, SourceStatusLine, datasetLabelKey } from "@/components/data/source";
import { LoadProblem } from "@/components/states/load-problem";
import { Button, ButtonLink } from "@/components/ui/button";
import { Switch } from "@/components/ui/choice";
import { ConfirmDialog } from "@/components/ui/confirm-dialog";
import { Card, EmptyState, Notice, Skeleton } from "@/components/ui/feedback";
import { Message } from "@/components/ui/text";
import { clearVerificationNotice, readVerificationNotice } from "@/lib/alerts/pending-alert";
import { useUnreadAlerts } from "@/lib/alerts/unread-provider";
import { formatThreshold, ruleText } from "@/lib/alerts/wording";
import { alertApi } from "@/lib/api/endpoints";
import { isApiError } from "@/lib/api/errors";
import type { AlertEventPage, AlertEventResponse, AlertPage, AlertResponse } from "@/lib/api/types";
import { useSession } from "@/lib/auth/session-provider";
import { useComparison } from "@/lib/compare/comparison-provider";
import { useI18n } from "@/lib/i18n/i18n-provider";
import { localizedName, publishedNameLang } from "@/lib/i18n/names";

const PAGE_SIZE = 50;

/** S11 price alerts: events, rules and the way to a new alert (role file 02 §8 S11; spec §34; ADR-0019). */
export function AlertsScreen({ created }: { created: boolean }) {
  const { t } = useI18n();
  const { status } = useSession();
  const [expired, setExpired] = useState(false);

  return (
    <PageShell title={t("nav.alerts")} backHref="/" width="medium">
      {status === "loading" ? (
        <div className="flex flex-col gap-3"><Skeleton className="h-32" /><Skeleton className="h-24" /></div>
      ) : status === "signedOut" ? (
        <EmptyState
          icon={<BellIcon size={28} />}
          title={expired ? t("error.signin_again") : t("alerts.sign_in_needed")}
          actions={<ButtonLink href="/sign-in?next=/alerts">{t("auth.tab_signin")}</ButtonLink>}
        >
          {t("auth.why_account")}
        </EmptyState>
      ) : (
        <AlertsContent created={created} onExpired={() => setExpired(true)} />
      )}
    </PageShell>
  );
}

function AlertsContent({ created, onExpired }: { created: boolean; onExpired: () => void }) {
  const { t } = useI18n();
  const { refreshUnread } = useUnreadAlerts();
  const { noteSourceStatus } = useComparison();
  const [events, setEvents] = useState<AlertEventPage | null>(null);
  const [alerts, setAlerts] = useState<AlertPage | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [attempt, setAttempt] = useState(0);
  // This part renders only once signed in, never on the server, so the tab's storage can be read at once.
  const [sentTo, setSentTo] = useState(readVerificationNotice);
  useEffect(() => clearVerificationNotice(), []);

  const fail = useCallback((problem: unknown) => {
    if (isApiError(problem, "UNAUTHORIZED")) onExpired();
    else setError(problem);
  }, [onExpired]);

  useEffect(() => {
    const controller = new AbortController();
    Promise.all([
      alertApi.listAlertEvents({ page: 0, size: PAGE_SIZE }, controller.signal),
      alertApi.listAlerts({ page: 0, size: 100 }, controller.signal),
    ]).then(([eventPage, alertPage]) => {
      setEvents(eventPage);
      setAlerts(alertPage);
      noteSourceStatus(eventPage.sourceStatus);
    }, (problem: unknown) => {
      if (!controller.signal.aborted) fail(problem);
    });
    return () => controller.abort();
  }, [attempt, fail, noteSourceStatus]);

  const markRead = async (event: AlertEventResponse) => {
    try {
      await alertApi.markAlertEventRead(event.id);
      setEvents((page) => page && {
        ...page,
        items: page.items.map((item) => (item.id === event.id ? { ...item, readAt: new Date().toISOString() } : item)),
      });
      refreshUnread();
    } catch (problem) {
      fail(problem);
    }
  };

  const updateAlert = (next: AlertResponse) =>
    setAlerts((page) => page && { ...page, items: page.items.map((item) => (item.id === next.id ? next : item)) });
  const removeAlert = (alertId: string) => {
    setAlerts((page) => page && { ...page, items: page.items.filter((item) => item.id !== alertId), totalItems: page.totalItems - 1 });
    setEvents((page) => page && { ...page, items: page.items.filter((item) => item.alertId !== alertId) });
    refreshUnread();
  };

  if (error) {
    return (
      <LoadProblem
        error={error}
        onRetry={() => {
          setError(null);
          setAttempt((value) => value + 1);
        }}
      />
    );
  }
  if (!events || !alerts) return <div className="flex flex-col gap-3"><Skeleton className="h-32" /><Skeleton className="h-24" /></div>;

  const unread = events.items.filter((event) => event.readAt === null);
  const read = events.items.filter((event) => event.readAt !== null);

  return (
    <div className="flex flex-col gap-5">
      {created ? <Notice tone="success" live>{t("alerts.saved")}</Notice> : null}
      {sentTo ? (
        <Notice
          tone="info"
          live
          title={<Message id="verify.sent" values={{ email: <strong className="break-all">{sentTo}</strong> }} />}
          action={<Button variant="tertiary" onClick={() => setSentTo(null)}>{t("alerts.dismiss")}</Button>}
        >
          {t("auth.link_delay")}
        </Notice>
      ) : null}
      <OutageBanner status={events.sourceStatus} />

      <section aria-labelledby="new-events" className="flex flex-col gap-3">
        <div className="flex items-baseline justify-between">
          <h2 id="new-events" className="eyebrow">{t("alerts.new_section")}</h2>
          <span className="text-label text-ink-2">{t("alerts.count_new", { n: unread.length })}</span>
        </div>
        {unread.length === 0 ? (
          <p className="rounded-card border border-rule bg-raised px-4 py-3 text-label text-ink-2">
            {read.length > 0 ? t("alerts.no_new_events") : t("alerts.no_events")}
          </p>
        ) : (
          unread.map((event) => <EventCard key={event.id} event={event} onRead={() => void markRead(event)} />)
        )}
        <SourceStatusLine status={events.sourceStatus} />
      </section>

      <section aria-labelledby="my-alerts" className="flex flex-col gap-3">
        <h2 id="my-alerts" className="eyebrow">{t("alerts.my_alerts", { n: alerts.totalItems })}</h2>
        {alerts.items.length === 0 ? (
          <EmptyState
            icon={<BellIcon size={28} />}
            title={t("alerts.empty")}
            actions={<ButtonLink href="/alerts/new"><PlusIcon size={18} /> {t("alerts.new")}</ButtonLink>}
          />
        ) : (
          <Card className="divide-y divide-rule">
            {alerts.items.map((alert) => (
              <AlertRow key={alert.id} alert={alert} onChange={updateAlert} onDeleted={removeAlert} onError={fail} />
            ))}
          </Card>
        )}
        {alerts.items.length > 0 ? (
          <ButtonLink href="/alerts/new" variant="secondary" block><PlusIcon size={18} /> {t("alerts.new")}</ButtonLink>
        ) : null}
      </section>

      {read.length > 0 ? (
        <section aria-labelledby="earlier-events" className="flex flex-col gap-3">
          <h2 id="earlier-events" className="eyebrow">{t("alerts.earlier")}</h2>
          {read.map((event) => <EventCard key={event.id} event={event} />)}
        </section>
      ) : null}

      <p className="flex gap-3 rounded-card border border-rule bg-sunken px-4 py-3 text-label">
        <InfoIcon className="mt-0.5 shrink-0 text-ink-2" />
        {t("alerts.info")}
      </p>
      <PriceScreenFooter attribution={events.items[0]?.observation.attribution} />
    </div>
  );
}

/** One alert event: what was reported against the threshold, with provenance (role file 02 S11). */
function EventCard({ event, onRead }: { event: AlertEventResponse; onRead?: () => void }) {
  const { t, fmt, locale } = useI18n();
  const crop = localizedName(event.commodity, locale);
  const lang = publishedNameLang(locale);
  const price = event.alertType === "PRICE_ABOVE" || event.alertType === "PRICE_BELOW";
  const datasetKey = datasetLabelKey(event.observation.sourceDataset);
  return (
    <Card as="article" className={`flex flex-col gap-3 px-4 py-4 ${event.readAt === null ? "border-2 border-net" : ""}`}>
      <div className="flex items-start justify-between gap-3">
        <h3 className="text-heading font-semibold">
          <span lang={crop.lang}>{crop.text}</span> · <span lang={lang}>{event.market.name}</span>
        </h3>
        <FreshnessBadge status={event.observation.freshnessStatus} reportDate={event.observation.reportDate} />
      </div>
      <p className="rounded-control bg-sunken px-3 py-2 text-body">
        <Message
          id={`alerts.event.${event.alertType}`}
          values={{
            value: price
              ? <strong className="text-net"><ReportedPrice amount={event.triggerValue} unit={event.priceUnit} /></strong>
              : <strong className="figure">{fmt.signedPercent(event.triggerValue)}</strong>,
            date: fmt.date(event.observation.reportDate),
            threshold: <span className="figure">{formatThreshold(event.alertType, event.thresholdValue, fmt)}</span>,
          }}
        />
      </p>
      <p className="text-caption text-ink-2">
        {t("alerts.checked", { time: fmt.dateTime(event.triggeredAt) })}
        {event.observation.variety ? <> · <span lang={lang}>{t("ledger.variety", { v: event.observation.variety.name })}</span></> : null}
        {event.observation.grade ? <> · <span lang={lang}>{t("ledger.grade", { g: event.observation.grade.name })}</span></> : null}
        {" · "}{datasetKey ? t(datasetKey) : <span lang="en">{event.observation.sourceDataset}</span>}
      </p>
      <div className="grid gap-2 tablet:grid-cols-2">
        {onRead ? <Button variant="secondary" onClick={onRead}>{t("alerts.mark_read")}</Button> : null}
        <ButtonLink href={`/compare?crop=${encodeURIComponent(event.commodity.id)}`}>{t("alerts.compare_now")}</ButtonLink>
      </div>
    </Card>
  );
}

/** One alert rule: words, threshold, market, ACTIVE/PAUSED switch, delete with a named confirmation. */
function AlertRow({ alert, onChange, onDeleted, onError }: {
  alert: AlertResponse;
  onChange: (alert: AlertResponse) => void;
  onDeleted: (alertId: string) => void;
  onError: (error: unknown) => void;
}) {
  const { t, fmt, locale } = useI18n();
  const [busy, setBusy] = useState(false);
  const [confirming, setConfirming] = useState(false);
  const crop = localizedName(alert.commodity, locale);
  const marketName = alert.market?.name ?? t("alerts.all_markets");
  const rule = ruleText(alert.alertType, alert.thresholdValue, t, fmt);
  const name = t("alerts.rule_name", { crop: crop.text, market: marketName, rule });

  const toggle = async (active: boolean) => {
    setBusy(true);
    try {
      onChange(await alertApi.updateAlertStatus(alert.id, active ? "ACTIVE" : "PAUSED"));
    } catch (error) {
      onError(error);
    } finally {
      setBusy(false);
    }
  };

  const remove = async () => {
    setBusy(true);
    try {
      await alertApi.deleteAlert(alert.id);
      setConfirming(false);
      onDeleted(alert.id);
    } catch (error) {
      setConfirming(false);
      onError(error);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="flex flex-col gap-2 px-4 py-4">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="text-body font-semibold">
            <span lang={crop.lang}>{crop.text}</span> · <span lang={alert.market ? publishedNameLang(locale) : undefined}>{marketName}</span>
          </p>
          <p className="text-body">{rule}</p>
          <p className="text-caption text-ink-2">
            {alert.lastTriggeredAt ? t("alerts.last_triggered", { time: fmt.dateTime(alert.lastTriggeredAt) }) : t("alerts.never_triggered")}
          </p>
        </div>
        <span className={`shrink-0 rounded-control border px-2 py-0.5 text-caption font-semibold ${alert.status === "ACTIVE" ? "border-fresh bg-fresh-bg text-fresh" : "border-unavailable bg-unavailable-bg text-unavailable"}`}>
          {alert.status === "ACTIVE" ? t("alerts.active") : t("alerts.paused")}
        </span>
      </div>
      <div className="flex items-center justify-between gap-3">
        <Switch
          checked={alert.status === "ACTIVE"}
          onChange={(active) => void toggle(active)}
          label={t("alerts.toggle_label", { name })}
          onText={t("alerts.active")}
          offText={t("alerts.paused")}
          disabled={busy}
        />
        <Button variant="destructive" onClick={() => setConfirming(true)} disabled={busy} className="!px-3">
          <TrashIcon size={18} /> {t("alerts.delete")}
        </Button>
      </div>
      {alert.market ? (
        <Link
          href={`/markets/${encodeURIComponent(alert.market.id)}/trend?crop=${encodeURIComponent(alert.commodity.id)}`}
          className="w-fit text-label font-semibold text-net underline underline-offset-4"
        >
          {t("action.trend")}
        </Link>
      ) : null}
      <ConfirmDialog
        open={confirming}
        title={t("alerts.delete_confirm", { name })}
        confirmLabel={t("alerts.delete")}
        cancelLabel={t("action.cancel")}
        busy={busy}
        onConfirm={() => void remove()}
        onCancel={() => setConfirming(false)}
      />
    </div>
  );
}
