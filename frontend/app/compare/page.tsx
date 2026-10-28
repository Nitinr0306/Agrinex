import { Suspense } from "react";
import { CompareScreen } from "@/components/compare/compare-screen";
import { localizedTitle } from "@/lib/i18n/server";

export const generateMetadata = () => localizedTitle("action.compare");

/** S03 + S04. Search parameters can prefill the crop, so the screen reads them inside a Suspense boundary. */
export default function ComparePage() {
  return (
    <Suspense>
      <CompareScreen />
    </Suspense>
  );
}
