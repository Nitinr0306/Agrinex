import type { Metadata, Viewport } from "next";
import type { ReactNode } from "react";
import { PrototypeBanner, SkipLink } from "@/components/layout/prototype-banner";
import { MOCK_MODE } from "@/lib/env";
import { I18nProvider } from "@/lib/i18n/i18n-provider";
import { messagesFor } from "@/lib/i18n/messages";
import { currentLocale } from "@/lib/i18n/server";
import { plexDevanagari, plexMono, plexSans } from "./fonts";
import "./globals.css";
import { Providers } from "./providers";

/** The description follows the chosen language, like every page title (spec §48). */
export async function generateMetadata(): Promise<Metadata> {
  return {
    title: { default: "AgriNex", template: "%s · AgriNex" },
    description: messagesFor(await currentLocale())["home.subtitle"],
  };
}

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  themeColor: "#faf6ee",
};

export default async function RootLayout({ children }: { children: ReactNode }) {
  const locale = await currentLocale();
  return (
    <html
      lang={locale}
      className={`${plexSans.variable} ${plexDevanagari.variable} ${plexMono.variable}`}
    >
      <body>
        <I18nProvider locale={locale} messages={messagesFor(locale)}>
          <SkipLink />
          {MOCK_MODE ? <PrototypeBanner /> : null}
          <Providers>{children}</Providers>
        </I18nProvider>
      </body>
    </html>
  );
}
