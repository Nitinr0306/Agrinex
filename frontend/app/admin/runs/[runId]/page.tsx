import { RunDetail } from "@/components/admin/runs";

export const metadata = { title: "Ingestion run" };

/** A02 run detail and errors. */
export default async function RunPage({ params }: { params: Promise<{ runId: string }> }) {
  const { runId } = await params;
  return <RunDetail runId={runId} />;
}
