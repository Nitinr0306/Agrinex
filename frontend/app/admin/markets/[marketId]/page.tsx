import { MarketCuration } from "@/components/admin/markets";

export const metadata = { title: "Market" };

/** A04 one market. */
export default async function AdminMarketPage({ params }: { params: Promise<{ marketId: string }> }) {
  const { marketId } = await params;
  return <MarketCuration marketId={marketId} />;
}
