import { cookies } from "next/headers";
import { HomeScreen } from "@/components/home/home-screen";
import { LOCALE_COOKIE, parseLocale } from "@/lib/i18n/config";

/** S01 (first launch) and S02 home. The language choice shows until a language has been chosen on this device. */
export default async function HomePage() {
  const chosen = parseLocale((await cookies()).get(LOCALE_COOKIE)?.value);
  return <HomeScreen firstLaunch={chosen === null} />;
}
