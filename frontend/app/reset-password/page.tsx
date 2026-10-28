import type { Metadata } from "next";
import { ResetPasswordScreen } from "@/components/auth/account-screens";
import { localizedTitle } from "@/lib/i18n/server";

/** The token travels in the fragment; this page also asks for no Referer at all (ADR-0048 item 2). */
export async function generateMetadata(): Promise<Metadata> {
  return { ...(await localizedTitle("reset.new_title")), referrer: "no-referrer" };
}

/** S15 new password, opened from the reset link (role file 02 §8 S15). */
export default function ResetPasswordPage() {
  return <ResetPasswordScreen />;
}
