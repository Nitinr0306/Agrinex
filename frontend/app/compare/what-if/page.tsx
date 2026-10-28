import { WhatIfScreen } from "@/components/whatif/whatif-screen";
import { localizedTitle } from "@/lib/i18n/server";

export const generateMetadata = () => localizedTitle("whatif.title");

/** S09 What-If: contextual to the comparison in memory (APPROVED PDD-02); overrides are never stored. */
export default function WhatIfPage() {
  return <WhatIfScreen />;
}
