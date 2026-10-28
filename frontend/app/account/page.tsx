import { AccountScreen } from "@/components/auth/account-screens";
import { localizedTitle } from "@/lib/i18n/server";

export const generateMetadata = () => localizedTitle("account.title");

/** S12 account menu (role file 02 §8 S12). */
export default function AccountPage() {
  return <AccountScreen />;
}
