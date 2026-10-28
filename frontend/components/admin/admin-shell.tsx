"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import type { ReactNode } from "react";
import { AppBar } from "@/components/layout/app-bar";
import { ButtonLink } from "@/components/ui/button";
import { EmptyState, Skeleton } from "@/components/ui/feedback";
import { useSession } from "@/lib/auth/session-provider";

const NAV = [
  { href: "/admin", label: "Data health" },
  { href: "/admin/runs", label: "Ingestion runs" },
  { href: "/admin/config", label: "Configuration" },
  { href: "/admin/markets", label: "Markets" },
  { href: "/admin/commodities", label: "Commodities" },
  { href: "/admin/district-aliases", label: "District aliases" },
  { href: "/admin/audit", label: "Audit log" },
];

/**
 * The admin area (A01–A05; role file 02 §4.2 admin navigation). English and desktop-first. The role check here only
 * decides what to show; every admin operation is authorized by the server (spec §28).
 */
export function AdminShell({ children }: { children: ReactNode }) {
  const { status, user } = useSession();
  const pathname = usePathname();

  let content: ReactNode;
  if (status === "loading") {
    content = <div className="flex flex-col gap-3"><Skeleton className="h-10" /><Skeleton className="h-48" /></div>;
  } else if (status === "signedOut") {
    content = (
      <EmptyState
        title="Sign in with your admin account."
        actions={<ButtonLink href={`/sign-in?tab=signin&next=${encodeURIComponent(pathname)}`}>Sign in</ButtonLink>}
      />
    );
  } else if (user?.role !== "ADMIN") {
    content = <EmptyState title="You do not have access to this page." actions={<ButtonLink href="/">Go to home</ButtonLink>} />;
  } else {
    content = children;
  }

  return (
    <>
      <AppBar />
      {/* The shared app bar follows the chosen language; the admin area itself is English. */}
      <div lang="en" className="mx-auto flex w-full max-w-7xl flex-col gap-6 px-4 pb-16 pt-4 desktop:flex-row">
        <nav aria-label="Admin" className="desktop:w-56 desktop:shrink-0">
          <p className="eyebrow mb-2 hidden desktop:block">Admin console</p>
          <ul className="flex gap-1 overflow-x-auto desktop:flex-col">
            {NAV.map((item) => {
              const active = item.href === "/admin" ? pathname === "/admin" : pathname.startsWith(item.href);
              return (
                <li key={item.href} className="shrink-0">
                  <Link
                    href={item.href}
                    aria-current={active ? "page" : undefined}
                    className={`flex min-h-12 items-center whitespace-nowrap rounded-control px-3 text-label font-semibold ${active ? "bg-sunken text-ink desktop:border-l-4 desktop:border-net" : "text-ink-2 hover:bg-sunken hover:text-ink"}`}
                  >
                    {item.label}
                  </Link>
                </li>
              );
            })}
          </ul>
        </nav>
        <main id="main" tabIndex={-1} className="min-w-0 flex-1 focus:outline-none">{content}</main>
      </div>
    </>
  );
}
