import { SignInScreen } from "@/components/auth/sign-in-screen";
import { localizedTitle } from "@/lib/i18n/server";

export const generateMetadata = () => localizedTitle("auth.tab_signin");

/** S10 sign in / quick registration (role file 02 §8 S10). */
export default async function SignInPage({ searchParams }: { searchParams: Promise<Record<string, string | string[] | undefined>> }) {
  const { tab, next, reset } = await searchParams;
  return (
    <SignInScreen
      initialTab={tab === "create" ? "create" : "signin"}
      next={typeof next === "string" ? next : null}
      resetDone={reset === "done"}
    />
  );
}
