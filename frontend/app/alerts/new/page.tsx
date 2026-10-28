import { AlertForm } from "@/components/alerts/alert-form";
import { localizedTitle } from "@/lib/i18n/server";

export const generateMetadata = () => localizedTitle("alerts.new");

/** S11 create form; reached from "Set alert" on a comparison, trend or market page, or from the alerts list. */
export default async function NewAlertPage({ searchParams }: { searchParams: Promise<Record<string, string | string[] | undefined>> }) {
  const { crop, market } = await searchParams;
  return <AlertForm crop={typeof crop === "string" ? crop : null} market={typeof market === "string" ? market : null} />;
}
