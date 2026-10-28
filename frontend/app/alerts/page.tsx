import { AlertsScreen } from "@/components/alerts/alerts-screen";
import { localizedTitle } from "@/lib/i18n/server";

export const generateMetadata = () => localizedTitle("nav.alerts");

/** S11 price alerts (FARMER or ADMIN; role file 02 §8 S11). */
export default async function AlertsPage({ searchParams }: { searchParams: Promise<Record<string, string | string[] | undefined>> }) {
  const { created } = await searchParams;
  return <AlertsScreen created={created === "1"} />;
}
