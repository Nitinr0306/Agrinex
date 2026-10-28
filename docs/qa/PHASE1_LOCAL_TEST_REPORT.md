# AgriNex — Phase 1 Local Test Report

**Status:** REFERENCE (2026-10-18). Results of verifying Phase 1 on the local stack started with `docs/LOCAL_DEVELOPMENT_GUIDE.md`, against the normal local database (real government data). Results marked "(2026-10-15)" were measured on the same backend and frontend code and were not repeated; every other result was measured on 2026-10-18.

## 1. Environment

- **Machine:** Windows 11, 16 GB RAM, Docker Desktop (engine 29.2.0).
- **Containers:** PostgreSQL 16 and Redis 7.4 in compose, port 5433 and 6379.
- **Backend:** Spring Boot 4.1.1 on JDK 25, port 8081, started as guide §5 describes, with the E2E rate-limit setting of guide §10.
- **Frontend:** Next.js 16.3.8 dev server on Node 24, port 3000 (guide §6).
- **Data:** 417,093 observations, 2025-10-15 to 2026-10-16; 354 VERIFIED markets.

## 2. Automated suites

| Suite | Command | Result |
|---|---|---|
| Backend: unit, integration, migration, contract (Testcontainers) | `backend`: `.\mvnw verify` | **101 / 101**, BUILD SUCCESS |
| Frontend: unit and component (Vitest) | `frontend`: `npm run test` | **195 / 195** in 17 files |
| Frontend: types | `npm run typecheck` | pass |
| Frontend: lint | `npm run lint` | pass |
| Frontend: production build | `npm run build` | pass (22 routes) |
| ML (synthetic data only) | `ml/forecasting`: `python -m pytest` | **15 / 15** |
| Browser journeys (Playwright, Chrome, real stack) | `frontend`: `npm run e2e` | **10 / 10** |

New in this verification: `EvaluationScriptIntegrationTest` (2 tests) checks `scripts/run_evaluation.sql` against hand-computed Experiment 1 figures.

## 3. Critical journeys on the running application

| Check | Result |
|---|---|
| Defect sweep: every farmer route in English, Marathi and Hindi at 375 px and 1,280 px, the comparison results with Why / Trend / What-If, and the admin area signed in | 122 page visits. 0 sideways scrolling, 0 untranslated keys, 0 "undefined"/"NaN", 0 script errors, 0 unexpected HTTP errors; all 6 result journeys completed |
| Console messages in the sweep | 96 × 401 from the session check on pages opened without a refresh cookie (by design, E12), and 12 × 404 for a provisional market and an unknown address (by design, ADR-0014). Lighthouse counts these as "errors in console" (Best Practices 96 on the home page) |
| Accessibility (Lighthouse) | Home 100 (mobile); comparison results 100 (desktop snapshot) |
| Comparisons against an independent recomputation (observation selection, haversine, transport, gross, net, ranking, winners) | **11 / 11**, plus the expected 422 `UNSUPPORTED_PRICE_UNIT` for a per-bundle crop |
| Trends against an independent recomputation | **15 / 15** |
| Freshness badge against report date, every option of 11 comparisons | **594 / 594** |
| Security probes: CORS allow-list and rejections, refresh `Origin` check, 401 without a token or with a malformed one, non-revealing login error, admin access with the admin token, health endpoints, `/actuator/env` not exposed, `ApiErrorResponse` without stack traces | **20 / 20** |
| Outages: PostgreSQL stopped (503 in 3.0 s, readiness 503, liveness 200, recovery 1 s after restart); Redis stopped (reads and comparison served, overall health 503, readiness 200, in-process login rate limit) | **11 / 11** |
| Live ingestion: two CURRENT runs | Both SUCCESS. The Sunday-morning feed held 0 Maharashtra records |
| Live ingestion: re-ingesting the stored date 2026-10-14 | 1,496 fetched, 0 inserted, 1,470 recognized as stored; stored rows unchanged |
| Market visibility | PROVISIONAL and MERGED markets 404 publicly; VERIFIED 200; no comparison option names a non-VERIFIED market |
| Units | 0 observations with a unit other than their commodity's verified unit; 0 of unverified commodities |
| Admin health API against the database (2026-10-15) | Exact match |
| Government key disabled (2026-10-16) | Run FAILED without changing data; comparisons served with the §29 outage banner (checked in the browser) |
| Live account email through Resend (2026-10-15) | 23 / 23: trilingual text, single-use links, expiry, resend, sign-out of every session on reset |
| Log scan after the live tests (2026-10-15) | 0 personal data, tokens or keys |

## 4. Performance (2026-10-15)

Comparison endpoint, 10 concurrent users for 60 s, backend jar, every comparison recorded:

- p50 64–70 ms, p95 165–182 ms, p99 251–275 ms, 0 errors.
- With the dev server and a browser running on the same laptop, p95 rose to 1,512 ms. The target (p95 < 1 s) must be re-measured on the deployment VM.

LCP, production build, Chrome DevTools "Fast 3G", 4× CPU slowdown, 375 px: home 1,567 ms, compare 1,624 ms. On "Slow 3G" the target is not met (5.0–6.7 s; ADR-0052 item 10).

The load test was not repeated on 2026-10-18. The backend runtime code is unchanged since then, and each load test adds thousands of recorded comparisons that must be exported and deleted.

## 5. Failures and how they were handled

- **Sweep script (2026-10-18):** the first run waited for "network idle", which never happens on pages that poll, and was stopped. The second run used a fixed settle time. This was a test-script fault, not a product fault.
- **Ingestion check (2026-10-18):** "every fetched record accounted for" failed with 0 of 0, because the Sunday feed was empty. The accounting invariant holds trivially. Duplicate prevention was then shown on real records by re-ingesting 2026-10-14.
- **Direct API probes:** these answered 502 until a `User-Agent` header was set; the data.gov.in gateway rejects Python's default one. The backend client is unaffected (its runs succeed).
- No product defect was found in this verification, so none was fixed. The product changes since Task 12 are the Experiment 1 script and the repository safeguards (`.gitignore`).

## 6. Not covered locally

- Deployment (Docker images, Nginx, TLS) and the deployed load and LCP measurements: Task 13, excluded.
- Experiment 1 and 2 results on real use: they need deployment.
- Experiment 3 (ML): blocked by spec §35 until ADR-0053 is decided (RD-051).
- A native-speaker review of the Marathi and Hindi copy, and a human security review: recommended, not done.
