import { DataHealth } from "@/components/admin/data-health";

export const metadata = { title: "Data health" };

/** A01 data health. */
export default function AdminHomePage() {
  return <DataHealth />;
}
