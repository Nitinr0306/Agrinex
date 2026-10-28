import { RunsList } from "@/components/admin/runs";

export const metadata = { title: "Ingestion runs" };

/** A02 run history. */
export default function RunsPage() {
  return <RunsList />;
}
