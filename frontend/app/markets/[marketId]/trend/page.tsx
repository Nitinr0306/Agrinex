import { TrendScreen } from "@/components/market/trend-screen";
import { localizedTitle } from "@/lib/i18n/server";

export const generateMetadata = () => localizedTitle("action.trend");

/** S07 trend with the optional S08 forecast section (role file 02 §8 S07, S08). */
export default async function TrendPage({ params, searchParams }: {
  params: Promise<{ marketId: string }>;
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const { marketId } = await params;
  const query = await searchParams;
  const one = (value: string | string[] | undefined) => (typeof value === "string" ? value : null);
  return (
    <TrendScreen
      marketId={marketId}
      crop={one(query.crop)}
      variety={one(query.variety)}
      grade={one(query.grade)}
      days={one(query.days) === "7" ? 7 : 30}
    />
  );
}
