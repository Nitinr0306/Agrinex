import { NextResponse, type NextRequest } from "next/server";

/**
 * Content-Security-Policy for every page (ADR-0017 item 7: built on default-src 'self' and frame-ancestors 'none';
 * ADR-0042 SR-11: finalized and verified with the frontend). A fresh nonce per request lets Next.js mark its own
 * scripts, so no inline script runs without it ('strict-dynamic'). React and Recharts set style attributes, which
 * nonces cannot cover, hence style-src-attr 'unsafe-inline'; style elements still need the nonce. The MSW service
 * worker of mock mode is a same-origin worker script (worker-src 'self').
 *
 * Because the policy carries a per-request nonce, the edge proxy must pass this header through and must not add
 * a second CSP for page responses (two policies are both enforced).
 */
export function proxy(request: NextRequest) {
  const nonce = btoa(crypto.randomUUID());
  const development = process.env.NODE_ENV === "development";
  const https = request.headers.get("x-forwarded-proto") === "https" || request.nextUrl.protocol === "https:";

  const directives = [
    "default-src 'self'",
    `script-src 'self' 'nonce-${nonce}' 'strict-dynamic'${development ? " 'unsafe-eval'" : ""}`,
    development ? "style-src 'self' 'unsafe-inline'" : `style-src 'self' 'nonce-${nonce}'`,
    "style-src-attr 'unsafe-inline'",
    "img-src 'self' data: blob:",
    "font-src 'self'",
    "connect-src 'self'",
    "worker-src 'self'",
    "object-src 'none'",
    "base-uri 'self'",
    "form-action 'self'",
    "frame-ancestors 'none'",
    ...(https ? ["upgrade-insecure-requests"] : []),
  ];
  const policy = directives.join("; ");

  const requestHeaders = new Headers(request.headers);
  requestHeaders.set("x-nonce", nonce);
  requestHeaders.set("Content-Security-Policy", policy);

  const response = NextResponse.next({ request: { headers: requestHeaders } });
  response.headers.set("Content-Security-Policy", policy);
  return response;
}

export const config = {
  matcher: [
    {
      source: "/((?!api/|_next/static|_next/image|favicon.ico|mockServiceWorker.js).*)",
      missing: [
        { type: "header", key: "next-router-prefetch" },
        { type: "header", key: "purpose", value: "prefetch" },
      ],
    },
  ],
};
