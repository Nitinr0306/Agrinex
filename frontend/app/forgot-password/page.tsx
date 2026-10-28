import { ForgotPasswordScreen } from "@/components/auth/account-screens";
import { localizedTitle } from "@/lib/i18n/server";

export const generateMetadata = () => localizedTitle("reset.title");

/** S14 forgot password (role file 02 §8 S14; ADR-0047, ADR-0048). */
export default function ForgotPasswordPage() {
  return <ForgotPasswordScreen />;
}
