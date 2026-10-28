# AgriNex — Task 12 Security Review

**Status:** REFERENCE (2026-10-15). This is a technical review prepared under CLAUDE.md rule 16. The items of section 6 were signed off on 2026-10-15 under the owner's delegation (ADR-0050, ADR-0052 item 3) as an AI review; section 7 records the re-review. No human has reviewed them.

## 1. CORS rejection format (`ProblemCorsProcessor`, Task 12)

**Change.** A rejected cross-origin request answered Spring's plain-text "Invalid CORS request". It now answers the contract's 403 `ORIGIN_NOT_ALLOWED` ApiErrorResponse.

**Files:**

- `backend/src/main/java/com/agrinex/auth/security/ProblemCorsProcessor.java`
- `SecurityConfiguration.java`: the `corsFilter` bean, and the servlet registration disabled, so the filter runs only inside the security chain.

**Review:**

- **Acceptance logic is unchanged.** `processRequest` calls Spring's `DefaultCorsProcessor` and only writes the error body when Spring rejects. `rejectRequest` writes nothing, so a rejection never produces two bodies. `CorsFilter` stops the chain on rejection and on preflights.
- **The allow-list is exact.**
  - Origins: `AGRINEX_PUBLIC_ORIGIN`, normalized to `scheme://host[:port]`, plus `http://localhost:3000` in the `dev` profile only.
  - There are no wildcards and no patterns. Credentials are allowed for these origins only.
  - Methods: GET, POST, PUT, PATCH, DELETE.
  - Request headers: `Authorization`, `Content-Type`.
  - Exposed headers: the correlation id, `Retry-After`, `Location`, `WWW-Authenticate`.
- **The error body carries nothing secret:** code, title, the fixed detail "The Origin header must equal the public origin." and the correlation id. A rejected response carries no `Access-Control-Allow-*` header, so a browser script on a foreign origin cannot read it.
- **The CORS filter runs before authentication**, so preflights never answer 401.

**Live evidence** (backend on 8081, `dev` profile, 2026-10-15):

| Request | Result |
|---|---|
| GET with `Origin: https://evil.example`, `null`, `http://localhost:3000.evil.example` | 403 `application/problem+json`, no CORS headers |
| GET with `Origin: http://localhost:3000` | 200, `Access-Control-Allow-Origin: http://localhost:3000`, `Access-Control-Allow-Credentials: true` |
| GET without `Origin` | 200 (not a CORS request) |
| Preflight from a foreign origin; preflight with header `X-Evil`; preflight with method `TRACE` | 403 |
| Preflight from `http://localhost:3000` for POST with `authorization, content-type` | 200 with the allowed methods and headers |
| POST `/auth/refresh` from a foreign origin | 403 `ORIGIN_NOT_ALLOWED` |

Automated: `CorsIntegrationTest` (foreign origin on refresh and on a public endpoint, the allowed origin, preflight, no Origin).

**Finding S12-1 (deployment).** `infra/.env.example` sets `SPRING_PROFILES_ACTIVE=dev`, and the `dev` profile adds `http://localhost:3000` to the credentialed CORS list. A deployment must not run with `dev`. This affects the deployment task (Task 13); there is no code change.

## 2. Session restore and rate limits (frontend, Task 12)

**Finding.** A farmer's comparison could wait up to a minute. When the page-load `/auth/refresh` answered 429, the client waited `Retry-After` (up to 60 s, as SR-14 requires), and operations that also allow anonymous access waited with it. The comparison was one of them, although it never needs an account. This was found by the browser journeys: the refresh limit is 10 per minute per client (ADR-0017 item 6), and every full page load restores the session.

**Fix.**

- Operations that also allow anonymous access (compare, simulate) wait only for the refresh's first answer (`tokenForOptionalAccess` in `frontend/lib/auth/session-store.ts`).
- The session itself still waits `Retry-After` and retries once (SR-14), and required-token operations are unchanged.
- An anonymous comparison sends no token; the server answer is the same, and only the link between the recorded search and the farmer profile is lost (contract, `compareSellingOptions`).

**Tests:** `frontend/lib/api/client.test.ts`. The new case fails without the fix.

**Observation, not changed.** The refresh limit is per client IP. Farmers behind one carrier NAT share an IP, so heavy shared use could reach 429 more often than one user would. The limits are deployment configuration (ADR-0017 item 6); whether to change them is a human decision.

## 3. Secrets and personal data

- **Environment file.** `infra/.env` holds live secrets and was not ignored by Git. A root `.gitignore` now ignores `.env`, `infra/.env` and `local-reference-data/`, with the owner's approval. Nothing has been committed.
- **Logs.** The backend log of the live email tests was searched for the test addresses, phone number, tokens, link paths, `Authorization` values and the Resend key prefix: 0 matches (ADR-0042, ADR-0048).
- **Exposed key.** The data.gov.in API key appeared in plain text in the Task 12 instructions. Rotating it is the owner's decision.
- **Local admin account.** The normal local database held a Task 10 test ADMIN with an `.invalid` address and an unrecorded password. With the owner's approval it was given the bootstrap email and password of `infra/.env`, with a BCrypt cost-12 hash computed outside the application. This was a one-time SQL change on the local database only. No audit action exists for it, so it is recorded here.

## 4. Account email (ADR-0048), live

Resend (`AgriNex <noreply@managio.me>`, DKIM-signed with selector `resend`, delivered through Amazon SES) delivered every message to two disposable mail.tm inboxes within seconds: 6 messages. The browser journeys sent 2 more to Resend's test address `delivered+…@resend.dev`. The managio.me DNS (DKIM, SPF on the return path, DMARC `p=none`) was only read, never changed. Verified:

- verification link: `http://localhost:3000/verify-email#token=…` (the public origin, token in the fragment, removed from the address bar);
- single use: a reused link answers 422 and the page says the link no longer works;
- server-side lifetimes of 24 h (verification) and 30 min (reset); an expired token answers 422;
- a resend replaces the earlier verification token;
- a reset request for an unregistered address answers 202 and sends nothing;
- a reset ends every session (the old refresh cookie answers 401), the old password fails, and a "password changed" message arrives;
- no automatic sign-in after a reset.

AR-07 (a live send must succeed before relying on it) is met for the local setup. The email texts were decided in the Task 12 closure (ADR-0052 item 7, RD-049 resolved) and re-tested live (section 7).

## 5. Outages (RESILIENCE_ARCHITECTURE.md, ADR-0018)

**Finding S12-2 (fixed).** With PostgreSQL stopped, every request waited 30 s, Hikari's default connection timeout, before answering 503. The architecture requires "fail fast with 503".

- **Fix:** `spring.datasource.hikari.connection-timeout: 3000` and `validation-timeout: 2000` in `application.yml`.
- **Test:** `DatabaseSchemaIntegrationTest.aDatabaseOutageFailsFastInsteadOfWaitingThirtySeconds`.

Live, on the normal local database, 2026-10-15:

| Outage | Result |
|---|---|
| PostgreSQL container stopped | API calls answer 503 `SERVICE_UNAVAILABLE` in 3.0 s (previously 30 s); readiness 503 with `db: DOWN`; liveness 200. After the database restarts, requests succeed again within about 4 s, with no backend restart |
| Redis container stopped | Commodity list 200 (cache bypassed to PostgreSQL); comparison 200; overall health 503 with `redis: DOWN`; readiness 200. Rate limiting continues in process: 10 failed logins answer 401, the 11th and 12th 429 |

Deployment health checks must use `/actuator/health/readiness`, as ADR-0021 item 7 already states.

## 6. Sign-off

| Item | Decision | By | Date |
|---|---|---|---|
| §1 CORS rejection format | SIGNED OFF (AI review, section 7) | Claude under ADR-0050 | 2026-10-15 |
| §2 Optional-access session handling (frontend) | SIGNED OFF (AI review, section 7) | Claude under ADR-0050 | 2026-10-15 |
| S12-1 `dev` profile must not be used on a deployment | ACCEPTED as a Task 13 deployment condition | Claude under ADR-0050 | 2026-10-15 |
| S12-2 Database fail-fast connection timeout (3 s) | SIGNED OFF (AI review, section 7) | Claude under ADR-0050 | 2026-10-15 |
| D12 data.gov.in key rotation | OPEN: owner action (section 7) | Human Project Owner | |

## 7. Task 12 closure re-review (2026-10-15, under ADR-0050)

The Human Project Owner delegated the decisions of `docs/decision-review/TASK_12_OWNER_DECISION_PACKET.md`, including this sign-off, to Claude (ADR-0050). **This is an AI review recorded under that delegation. No human has reviewed these items.**

**Re-examination.**

- **CORS.** `ProblemCorsProcessor` only adds the contract body when Spring's `DefaultCorsProcessor` rejects. The allow-list is Spring's exact-origin list, and preflights stop in the filter. No defect was found.
- **Session.** `tokenForOptionalAccess` waits only for the refresh's first answer and never sends a stale token on a required operation. No defect was found.
- **Fail-fast.** The 3 s connection timeout turns a database outage into a 503 within 3 s. Under the 10-user load test no request waited for a connection. No defect was found.

**Changes in the closure that touch security-relevant code:**

- the account emails are trilingual (ADR-0052 item 7); the token, link and lifetime handling of ADR-0048 is unchanged;
- 242 market merges through the audited admin API (ADR-0052 item 11);
- comparison rows deleted locally after an export (ADR-0052 item 9).

**Live results, local stack, 2026-10-15:**

| Check | Result |
|---|---|
| CORS, refresh `Origin` check, authentication, admin token, health endpoints, `/actuator/env` not exposed, error shape without stack traces | 20 of 20 probes |
| PostgreSQL stopped: comparison 503 `SERVICE_UNAVAILABLE` in 3.0 s; readiness 503; liveness 200; recovery 1 s after restart, without a backend restart | 5 of 5 |
| Redis stopped: commodity list and comparison 200; health 503 with `redis: DOWN`; readiness 200; login rate limit in process (10 × 401, then 429); health 200 after restart | 6 of 6 |
| Account email through Resend to a disposable mail.tm inbox: trilingual text, link once on the public origin with the token in the fragment, resend replaces the token, single use, an expired token refused, every session ended by a reset, old password refused, password-changed email, no email for an unknown address | 23 of 23 |
| Backend log after these tests: test address, inbox domain, token fragments, Bearer values, refresh-cookie values, Resend key, data.gov.in key | 0 matches each |

**Finding S12-3 (not changed; recorded).** `experimental.inlineCss` (Next.js 16.3.8) writes the stylesheet into a `<style>` element without the per-request nonce. The production CSP (`style-src 'self' 'nonce-…'`) therefore blocks it and the page renders unstyled. It was reverted (ADR-0052 item 10). The CSP must not be weakened with `'unsafe-inline'` for this. A hash-based `style-src` would need its own security review.

**Data.gov.in key rotation (D12; owner action, not done).** The key appeared in plain text in the Task 12 instructions and is treated as compromised. Rotation needs the owner's data.gov.in account, so Claude did not rotate it and does not claim it was rotated. Steps:

1. Sign in at data.gov.in with the account that owns the key, and generate a new key from the user profile. Check on the portal that the old key is no longer active; if the portal keeps both, revoke the old one there. (Whether generating a key revokes the previous one was not verified.)
2. Replace the value of `DATA_GOV_API_KEY` in `infra/.env`, and only there. Never in chat, documents, commits or logs.
3. Restart the backend (guide §12).
4. As ADMIN, trigger a `CURRENT_PRICE` run (*Admin → Runs → Run now*, or `POST /api/v1/admin/ingestion/trigger`), and confirm it ends SUCCESS with pages fetched.
5. Confirm the old key is refused: send one request with it from a terminal that does not echo it (the key read from a local file, never typed into a shared log). Then delete that file.
