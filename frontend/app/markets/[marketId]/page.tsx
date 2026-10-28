import { MarketDetailScreen } from "@/components/market/market-detail-screen";
import { localizedTitle } from "@/lib/i18n/server";

export const generateMetadata = () => localizedTitle("action.detail");

/** S06 market detail (role file 02 §8 S06): reached from a comparison or an alert, never from a directory. */
export default async function MarketPage({ params, searchParams }: {
  params: Promise<{ marketId: string }>;
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const { marketId } = await params;
  const { crop } = await searchParams;
  return <MarketDetailScreen marketId={marketId} crop={typeof crop === "string" ? crop : null} />;
}
