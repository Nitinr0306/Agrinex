"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useUnreadAlerts } from "@/lib/alerts/unread-provider";
import { useSession } from "@/lib/auth/session-provider";
import { useI18n } from "@/lib/i18n/i18n-provider";
import { ArrowLeftIcon, BellIcon, UserIcon } from "@/components/icons";
import { LanguageSwitch } from "./language-switch";

/**
 * The top app bar, the only navigation (APPROVED PDD-02): back, title, language, alerts with the unread count,
 * account. On desktop the two farmer destinations (Compare, Price alerts) sit next to the product name.
 */
export function AppBar({ title, backHref, onBack }: { title?: string; backHref?: string; onBack?: () => void }) {
  const { t } = useI18n();
  const pathname = usePathname();
  const { status, user } = useSession();
  const { unreadCount } = useUnreadAlerts();
  const signedIn = status === "signedIn";
  const unread = signedIn && unreadCount !== null && unreadCount > 0 ? unreadCount : 0;

  return (
    <header className="sticky top-0 z-10 border-b border-rule bg-paper">
      <div className="mx-auto flex min-h-16 max-w-6xl items-center gap-2 px-4">
        {onBack ? (
          <button
            type="button"
            onClick={onBack}
            className="flex min-h-12 min-w-12 items-center justify-center rounded-control text-ink hover:bg-sunken desktop:hidden"
            aria-label={t("nav.back")}
          >
            <ArrowLeftIcon size={24} />
          </button>
        ) : backHref ? (
          <Link
            href={backHref}
            className="flex min-h-12 min-w-12 items-center justify-center rounded-control text-ink hover:bg-sunken desktop:hidden"
            aria-label={t("nav.back")}
          >
            <ArrowLeftIcon size={24} />
          </Link>
        ) : null}
        <Link
          href="/"
          className={`${backHref || onBack ? "hidden desktop:flex" : "flex"} min-h-12 items-center text-title font-bold text-net`}
          lang="en"
        >
          AgriNex
        </Link>
        <nav aria-label={t("nav.main")} className="ml-6 hidden h-full items-stretch gap-1 desktop:flex">
          <NavLink href="/compare" active={pathname.startsWith("/compare") || pathname.startsWith("/markets")}>
            {t("nav.compare")}
          </NavLink>
          <NavLink href="/alerts" active={pathname.startsWith("/alerts")}>
            {t("nav.alerts")}
          </NavLink>
        </nav>
        {title ? (
          <p className="min-w-0 flex-1 truncate text-heading font-semibold text-ink desktop:hidden">{title}</p>
        ) : (
          <span className="flex-1 desktop:hidden" />
        )}
        <span className="hidden flex-1 desktop:block" />
        <LanguageSwitch />
        <Link
          href="/alerts"
          className="relative flex min-h-12 min-w-12 items-center justify-center rounded-control text-ink hover:bg-sunken"
          aria-label={unread > 0 ? t("nav.alerts_unread", { n: unread }) : t("nav.alerts")}
        >
          <BellIcon size={24} />
          {unread > 0 ? (
            <span
              className="figure absolute right-1 top-1 flex h-5 min-w-5 items-center justify-center rounded-full bg-falling px-1 text-caption font-semibold text-inverse"
              aria-hidden="true"
            >
              {unread > 99 ? "99+" : unread}
            </span>
          ) : null}
        </Link>
        <Link
          href={signedIn ? "/account" : "/sign-in"}
          className="flex min-h-12 min-w-12 items-center justify-center gap-2 rounded-control px-2 text-ink hover:bg-sunken"
          aria-label={signedIn ? t("nav.account") : t("auth.tab_signin")}
        >
          <UserIcon size={24} />
          <span className="hidden max-w-40 truncate text-label font-semibold desktop:inline">
            {signedIn ? (user?.displayName ?? t("nav.account")) : t("auth.tab_signin")}
          </span>
        </Link>
      </div>
    </header>
  );
}

function NavLink({ href, active, children }: { href: string; active: boolean; children: React.ReactNode }) {
  return (
    <Link
      href={href}
      aria-current={active ? "page" : undefined}
      className={`flex min-h-12 items-center border-b-2 px-3 text-body font-semibold ${active ? "border-net text-net" : "border-transparent text-ink-2 hover:text-ink"}`}
    >
      {children}
    </Link>
  );
}
