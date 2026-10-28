import type { ReactNode } from "react";
import { AppBar } from "./app-bar";

/**
 * A farmer screen: the app bar, then one main column. "narrow" is the mobile-first reading width; "wide" is for
 * the desktop comparison sheet (role file 02 §13). The title is the page's h1: shown in the app bar below
 * 1024 px (and read by screen readers from the h1), shown above the content at desktop widths.
 */
export function PageShell({ title, backHref, width = "narrow", showHeading = true, children }: {
  title?: string;
  backHref?: string;
  width?: "narrow" | "medium" | "wide";
  /** False when the page renders its own h1. */
  showHeading?: boolean;
  children: ReactNode;
}) {
  const max = width === "wide" ? "max-w-6xl" : width === "medium" ? "max-w-3xl" : "max-w-xl";
  return (
    <>
      <AppBar title={title} backHref={backHref} />
      <main id="main" tabIndex={-1} className={`mx-auto w-full ${max} px-4 pb-16 pt-5 focus:outline-none`}>
        {title && showHeading ? (
          <h1 className="sr-only desktop:not-sr-only desktop:mb-5 desktop:block desktop:text-display desktop:font-bold">
            {title}
          </h1>
        ) : null}
        {children}
      </main>
    </>
  );
}
