import { Suspense } from "react";
import { WhyScreen } from "@/components/compare/why-screen";
import { localizedTitle } from "@/lib/i18n/server";

export const generateMetadata = () => localizedTitle("why.title");

/** S05 "Why recommended?": contextual to the comparison in memory (APPROVED PDD-02). */
export default function WhyPage() {
  return (
    <Suspense>
      <WhyScreen />
    </Suspense>
  );
}
