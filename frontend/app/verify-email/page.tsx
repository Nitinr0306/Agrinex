import type { Metadata } from "next";
import { VerifyEmailScreen } from "@/components/auth/account-screens";
import { localizedTitle } from "@/lib/i18n/server";

export async function generateMetadata(): Promise<Metadata> {
  return { ...(await localizedTitle("verify.title")), referrer: "no-referrer" };
}

/** S16 email confirmation, opened from the verification link (role file 02 §8 S16). */
export default function VerifyEmailPage() {
  return <VerifyEmailScreen />;
}
