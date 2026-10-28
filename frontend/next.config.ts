import type { NextConfig } from "next";

/**
 * In production every `/api/v1/` request reaches the backend through Nginx on the same origin (ADR-0017 item 1).
 * For local development only, `AGRINEX_DEV_API_ORIGIN` (for example http://localhost:8080) forwards `/api/v1/`
 * to a locally running backend so the browser still sees one origin. It is ignored outside `next dev`.
 */
const devApiOrigin = process.env.NODE_ENV === "development" ? process.env.AGRINEX_DEV_API_ORIGIN : undefined;

/* Pages opened from emailed links carry a token in the URL fragment; they never leak a Referer (ADR-0048 item 2). */
const noReferrer = [{ key: "Referrer-Policy", value: "no-referrer" }];

/* The container image (Dockerfile) builds the standalone server; local `next build` / `next start` are unchanged. */
const standalone = process.env.AGRINEX_STANDALONE === "true";

/* The deployed image never serves the synthetic mock API (spec §44 mock mode is a development aid only). */
if (standalone && process.env.NEXT_PUBLIC_USE_MOCK_API === "true") {
  throw new Error("The container image must not be built with NEXT_PUBLIC_USE_MOCK_API=true (mock mode).");
}

const nextConfig: NextConfig = {
  ...(standalone ? { output: "standalone" as const } : {}),
  poweredByHeader: false,
  reactStrictMode: true,
  async headers() {
    return [
      { source: "/reset-password", headers: noReferrer },
      { source: "/verify-email", headers: noReferrer },
    ];
  },
  async rewrites() {
    return devApiOrigin
      ? [{ source: "/api/v1/:path*", destination: `${devApiOrigin.replace(/\/$/, "")}/api/v1/:path*` }]
      : [];
  },
};

export default nextConfig;
