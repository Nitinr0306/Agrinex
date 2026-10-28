# AgriNex — Task 8.5 Security Review (preparation for the CLAUDE.md rule 16 review)

**Status:** REFERENCE, prepared on 2026-10-04 (Task 8.5) for the human security review that CLAUDE.md rule 16 and §K require. **Signed off on 2026-10-04 by the Human Project Owner (§4; ADR-0042).** The resolutions are documented in the contract and the security architecture; runtime security code is implemented later, with the backend and frontend.

**Scope:**

- **Covered:**
  - API contract 1.0.0 (`contracts/`, `docs/api/API_CONVENTIONS.md` §7);
  - the binding entries ADR-0016, ADR-0017, ADR-0018, ADR-0020, ADR-0021, ADR-0028 and ADR-0029;
  - `docs/architecture/SECURITY_ARCHITECTURE.md`;
  - the database design where it holds personal data;
  - the open items RD-040 and RD-042.
- **Not covered:**
  - running code (none exists);
  - penetration testing;
  - deployed TLS and header configuration;
  - dependency vulnerabilities (no dependency is installed yet).

**Open security item outside this review:** RD-002, credentials in the untracked `.mcp.json` (ADR-0033), is still awaiting user action. This review does not reproduce any credential value.

**Labels** as in the decision packet. Facts from official documentation are marked [VERIFIED TECHNICAL FACT].

## 1. Severity scale (used in this review)

| Severity | Meaning |
|---|---|
| High | Remotely exploitable without special access. Leads to account takeover, irreversible data loss, or disclosure of personal data at scale |
| Medium | Exploitable with a precondition (privileged access, a modified client, a leaked identifier), or a missing control that a binding requirement expects |
| Low | Limited impact or unlikely preconditions; a defense-in-depth gap |
| Informational | No weakness found; a confirmation or a verification item for implementation |

## 2. Summary

| ID | Area (Task 8.5 list) | Finding | Severity | Human approval? |
|---|---|---|---|---|
| SR-00 | All | The auth and authorization contract has not yet had its rule-16 review | Process | YES |
| SR-01 | 1 Registration conflict | 409 reveals whether a phone number is registered; numbers can be claimed by others | Low | YES (risk acceptance) |
| SR-02 | 2 Run identifier | A `recommendationId` lets its holder read the run's origin and quantity through simulate | Low (Medium if RD-042 → C) | YES |
| SR-03 | 3 Token after deactivation | Access tokens stay valid up to 15 minutes on operations that do not resolve the account | Low | YES (confirm model) |
| SR-04 | 3 Token after deactivation | Compare and simulate with a token of a removed account: behavior undefined, possible 500 | Low | YES |
| SR-05 | 3 Token after deactivation | No defined, audited way to deactivate an account (RD-044) | Medium | YES |
| SR-06 | 4 Cookie | Refresh cookie name has no `__Secure-` prefix (`__Host-` is not possible) | Low | YES |
| SR-07 | 4 Cookie | No absolute session lifetime | Informational | NO |
| SR-08 | 5 RD-040 | No erasure mechanism yet; an unsafeguarded HTTP erasure would be High risk | Medium | YES |
| SR-09 | 6 RD-042 | Backend accepts any coordinates as origin and stores them as personal data | Medium | YES |
| SR-10 | 7 Admin authorization | Admin sign-in is password-only on the public login | Low | YES (risk acceptance) |
| SR-11 | 7 Admin authorization | One origin for admin and farmer screens; the in-memory admin token depends on XSS prevention and the pending CSP | Low | NO (implementation gate) |
| SR-12 | 8 Rate limiting | Per-identifier failed-login limit enables targeted lockout | Low | YES (risk acceptance) |
| SR-13 | 8 Rate limiting | Many endpoints have no limit; per-IP limits under shared mobile IPs | Low | YES only if a limit is added |
| SR-14 | 8 Rate limiting | No client rule for a rate-limited refresh | Low | NO (documentation) |
| SR-15 | 9 Auth error semantics | Consistent and non-revealing, apart from SR-01 | Informational | NO |
| SR-16 | 10 Sensitive data | Responses expose no secrets; the logging rule does not explicitly exclude request bodies | Low | YES (logging rule) |

No High finding exists in the current design. SR-08 becomes High only if an HTTP erasure operation is added without safeguards.

## 3. Findings

### SR-00 — The auth contract awaits its rule-16 review

| Field | Content |
|---|---|
| Finding | Registration, login, refresh, logout, the bearer scheme, roles per operation, the refresh cookie, the `Origin` rule, the account-status rules and the rate-limit table were written into contract 1.0.0 in Task 8. They are security-sensitive (CLAUDE.md §K) and have not been reviewed by a human |
| Affected | `contracts/openapi.yaml` (auth operations, `securitySchemes`, per-operation `security`); `docs/api/API_CONVENTIONS.md` §7; ADR-0016, ADR-0017 |
| Severity | Process |
| Exploit / impact | Unreviewed security semantics could be implemented as if approved |
| Current mitigation | Every rule traces to ADR-0016, ADR-0017, ADR-0018, ADR-0020 or ADR-0028, or is labelled [CONTRACT DESIGN] in `API_CONVENTIONS.md` §7 and §9 |
| Recommended resolution | Review SR-01 to SR-16 and record the outcome in §4 before the first `contracts/` commit is merged (spec §41) |
| HUMAN APPROVAL REQUIRED? | **YES** |

### SR-01 — Registration conflict reveals registered numbers

| Field | Content |
|---|---|
| Finding | `registerFarmer` answers 409 `CONFLICT` when the phone number already has an account, so anyone can test whether a number is registered. Phone numbers are not verified in Phase 1, so a person can also register someone else's number first |
| Affected | `registerFarmer` (409); ADR-0016 items 1 and 3; ADR-0017 item 6 (register 10 per minute per IP); spec §25, §39 (no SMS or OTP) |
| Severity | Low |
| Exploit / impact | **Enumeration:** probing candidate numbers reveals which belong to AgriNex farmers, which helps targeted phishing ("AgriNex support"). Distributed probing from many IPs bypasses the per-IP limit. Only membership is disclosed: no name, district or other data. **Number claiming:** the real owner then gets 409 and cannot register. Anonymous comparison still works, but alerts need an account |
| Current mitigation | <ul><li>Register limit of 10 per minute per IP.</li><li>Login answers the same 401 for unknown, wrong and inactive accounts.</li><li>Per-identifier failed-login limit.</li><li>Unknown identifiers cost one BCrypt check.</li><li>No response contains a phone number.</li><li>A claimed number can be freed by erasure (RD-040).</li></ul> |
| Recommended resolution | Accept as residual risk for Phase 1. A registration flow that reveals nothing needs out-of-band confirmation (SMS or OTP), which §39 excludes from Phase 1. Revisit when Phase 2 brings OTP (RD-025, deferred). The erasure procedure (RD-040) must cover "my number was registered by someone else" |
| HUMAN APPROVAL REQUIRED? | **YES** (risk acceptance) |

### SR-02 — A run identifier grants read access through simulate

| Field | Content |
|---|---|
| Finding | `simulateSellingOptions` accepts any `recommendationId` without checking the caller. Its response carries the stored origin, quantity, commodity and the ranked options |
| Affected | `simulateSellingOptions`; `MarketComparisonResponse.origin` and `quantity`; ADR-0026 item 3; `recommendation_runs.id` (`gen_random_uuid()`) |
| Severity | Low. Medium if RD-042 is resolved with option C |
| Exploit / impact | <ul><li>Guessing an id is infeasible: `gen_random_uuid()` returns a version 4 (random) UUID [VERIFIED TECHNICAL FACT, PostgreSQL 16 documentation §9.14]. The version 4 layout leaves 122 random bits.</li><li>An id leaks only through the requester's own channels: shared screenshots or HAR files, browser extensions, or logs.</li><li>The holder learns a district centroid, a commodity and a quantity, but no identity, because the response carries no profile data.</li><li>If non-centroid origins were accepted, a precise location could leak.</li></ul> |
| Current mitigation | <ul><li>Unguessable id.</li><li>The id travels only in POST bodies, never in URLs.</li><li>Simulate persists nothing.</li><li>Shared limit of 60 per minute per IP.</li></ul> |
| Recommended resolution | Resolve RD-042 with option A, so the exposed origin is always a public district centroid, then accept. Keep request bodies out of logs (SR-16). **Stricter alternative** (a contract change): when a run is linked to a farmer profile, simulate requires that farmer's token |
| HUMAN APPROVAL REQUIRED? | **YES** |

### SR-03 — Access tokens outlive deactivation on non-resolving operations

| Field | Content |
|---|---|
| Finding | Access tokens are stateless 15-minute JWTs (ADR-0016 item 4). For a deactivated or erased account, the contract already cuts access: <ul><li>refresh is refused and the token family revoked;</li><li>alert and admin operations, which resolve the caller's account, answer 401.</li></ul>Other operations rely on the 15-minute expiry |
| Affected | ADR-0016 items 4 and 5; `API_CONVENTIONS.md` §7; the `Unauthorized` response description in `openapi.yaml` |
| Severity | Low |
| Exploit / impact | Within the remaining 15 minutes, the token works only on operations that also allow anonymous access, which anyone can call without a token. Admin and alert access stops at once |
| Current mitigation | Per-request account check on admin and alert operations; refresh refusal with family revocation; 15-minute expiry |
| Recommended resolution | Accept, together with the clarification in SR-04. The per-request account check is [CONTRACT DESIGN] within ADR-0016; ADR-0016 does not state it, so confirm it as the intended model |
| HUMAN APPROVAL REQUIRED? | **YES** (confirm the model) |

### SR-04 — Compare and simulate with a token of a removed account

| Field | Content |
|---|---|
| Finding | When a token is sent, compare links the recorded search to the caller's farmer profile. If the account was erased while the token was still valid, the profile no longer exists. The contract does not say whether the server answers 401, ignores the link, or fails. A naive implementation would insert a reference to a deleted profile and fail with a 500 (foreign-key violation) |
| Affected | `compareSellingOptions` and `simulateSellingOptions` (`security: [{}, bearerAuth]`); `API_CONVENTIONS.md` §7; `selling_searches.farmer_profile_id` (ON DELETE SET NULL); ADR-0029 |
| Severity | Low (robustness) |
| Exploit / impact | An erased or deactivated user's open tab could produce 500 responses or link a search to the wrong state |
| Current mitigation | None specified |
| Recommended resolution | When a token is present, treat compare and simulate as account-resolving: an inactive or removed account answers 401 `UNAUTHORIZED`. The client then refreshes, the refresh is refused, and it retries without the token (`FRONTEND_CONSUMPTION.md` §4). This is a wording change only, because 401 is already declared on both operations |
| HUMAN APPROVAL REQUIRED? | **YES** (security-sensitive contract semantics) |

### SR-05 — No defined, audited way to deactivate an account

| Field | Content |
|---|---|
| Finding | `users.is_active` exists (spec §20) and the contract enforces it. However: <ul><li>no operation or procedure sets it;</li><li>the closed audit vocabulary has no deactivation action;</li><li>ADR-0029 says admins "are deactivated, never deleted" without saying how.</li></ul>Registered as RD-044 |
| Affected | Spec §20; ADR-0020 item 6; ADR-0029 item 2; `ck_admin_audit_events_action` |
| Severity | Medium (incident response and audit gap) |
| Exploit / impact | A compromised or departing admin account can only be disabled by unaudited manual SQL. During an incident this is slow and leaves no audit trail |
| Current mitigation | Once `is_active` is false, refresh, alert and admin operations refuse the account immediately |
| Recommended resolution | Decide RD-044 together with RD-040, using the same mechanism, and add deactivation and reactivation actions to the audit vocabulary |
| HUMAN APPROVAL REQUIRED? | **YES** |

### SR-06 — Refresh cookie without a `__Secure-` prefix

| Field | Content |
|---|---|
| Finding | The cookie is `agrinex_refresh; HttpOnly; Secure; SameSite=Strict; Path=/api/v1/auth`. A `__Host-` prefix is impossible, because it requires `Path=/` and no `Domain`. A `__Secure-` prefix is compatible, because it requires only `Secure` from an HTTPS origin [VERIFIED TECHNICAL FACT, MDN `Set-Cookie`, cookie prefixes] |
| Affected | `Set-Cookie-Refresh` and `Set-Cookie-Clear` headers in `openapi.yaml`. The cookie name is [CONTRACT DESIGN]; ADR-0017 item 3 fixes only the attributes, which stay unchanged |
| Severity | Low (defense in depth) |
| Exploit / impact | Without a prefix, a party that can set cookies for the host or a parent domain can plant a cookie with the same name ("cookie tossing") and attempt session fixation. Examples: a compromised sibling subdomain, or a network attacker before HSTS applies |
| Current mitigation | <ul><li>`HttpOnly`, `Secure`, `SameSite=Strict` and the narrow `Path`.</li><li>The `Origin` check on refresh and logout.</li><li>HSTS once TLS is verified (ADR-0017 item 7).</li><li>One public domain (ADR-0021; RD-037).</li></ul> |
| Recommended resolution | Rename to `__Secure-agrinex_refresh` before release. This changes the contract headers and mocks, and the server must check the full prefixed name. Browsers that do not support prefixes ignore the rule (MDN), so it remains defense in depth. Check the behavior in the `http://localhost` development profile during implementation |
| HUMAN APPROVAL REQUIRED? | **YES** (security configuration and contract change) |

### SR-07 — No absolute session lifetime

| Field | Content |
|---|---|
| Finding | Each refresh issues a token valid for 7 days from issue (ADR-0016 item 5), so a session can be extended indefinitely by refreshing at least once a week. The spec defines only the 7-day TTL (§28) |
| Affected | ADR-0016 item 5 |
| Severity | Informational |
| Exploit / impact | A stolen refresh token stays bounded by rotation and reuse detection: when either party next uses an already-rotated token, the whole family is revoked (RFC 9700 §4.14.2) |
| Current mitigation | Rotation, family revocation, logout revocation, the `HttpOnly` cookie |
| Recommended resolution | Accept for Phase 1. An absolute lifetime would be a new rule and need a decision |
| HUMAN APPROVAL REQUIRED? | NO (no change proposed) |

### SR-08 — Account erasure (RD-040)

| Field | Content |
|---|---|
| Finding | No erasure mechanism is decided, so the ADR-0029 obligation cannot yet be met through an audited path. If an HTTP erasure operation (option A) were added without safeguards, a stolen admin access token or a compromised admin account could erase farmer accounts irreversibly |
| Affected | ADR-0029 item 2; ADR-0020 items 1 and 7; PROPOSED ADR-0037; `admin_audit_events` (`ACCOUNT_ERASED`); `TABLE_OWNERSHIP.md` §4 (admin → auth) |
| Severity | Medium. It would be High for option A without safeguards |
| Exploit / impact | <ul><li>A: remote and irreversible.</li><li>B: privileged database access for a routine request, with a hand-written audit row.</li><li>C: needs server access, which is already fully trusted.</li></ul> |
| Current mitigation | No erasure path exists in contract 1.0.0. The cascades and SET NULL are designed (ADR-0029) |
| Recommended resolution | **Option C** (decision packet): an in-application operator procedure without HTTP. It is audited as `ACCOUNT_ERASED` with a named active ADMIN as actor and the number masked. **If option A is chosen**, require at least: <ul><li>ADMIN only;</li><li>a separate low rate limit;</li><li>a request reference and a reason;</li><li>explicit confirmation of the masked number;</li><li>FARMER accounts only, with no bulk erasure;</li><li>masked audit details.</li></ul> |
| HUMAN APPROVAL REQUIRED? | **YES** |

### SR-09 — Origin coordinates are not validated server-side (RD-042)

| Field | Content |
|---|---|
| Finding | The backend accepts any in-range coordinates as a comparison's origin and stores them in `selling_searches.origin_lat` and `origin_lon` (PERSONAL, kept indefinitely). §28 assigns "district centroid only" to the backend as well, and ADR-0029's anonymization assumes centroids |
| Affected | Spec §28, FR-06; ADR-0023 item 1; ADR-0029 item 2; `CompareRequest`; `DATA_DICTIONARY.md` (`selling_searches`) |
| Severity | Medium |
| Exploit / impact | A buggy or modified client, for example one using device geolocation, could store precise home locations with every search. They would survive account erasure (searches are kept, only unlinked) and be visible through simulate to id holders (SR-02) |
| Current mitigation | The frontend is told to send the `listDistricts` centroid; run ids are unguessable |
| Recommended resolution | RD-042 option A: the server accepts only stored district centroids, through a new approved recommendation → geography edge |
| HUMAN APPROVAL REQUIRED? | **YES** |

### SR-10 — Admin sign-in is password-only on the public login

| Field | Content |
|---|---|
| Finding | Admins sign in at the same public `login` operation as farmers, with email and password (15–64 characters, BCrypt cost 12). There is no second factor and no network restriction for admin accounts |
| Affected | ADR-0016 items 1–3; ADR-0017 item 6; ADR-0028; spec §28 (no MFA requirement) |
| Severity | Low |
| Exploit / impact | A phished or reused admin password gives the full admin capability set: configuration, curation, triggers, pauses. Every change is audited, and no secret is reachable (ADR-0020) |
| Current mitigation | <ul><li>15-character minimum.</li><li>Per-IP and per-identifier login limits.</li><li>Admins are provisioned, never self-registered; no published credentials (ADR-0028).</li><li>Audit trail.</li><li>Per-request account check.</li></ul> |
| Recommended resolution | Accept for Phase 1, since the spec requires no MFA. Optionally restrict admin access at Nginx, which would be a deployment decision. The deployment runbook must confirm that the bootstrap password is removed after first start (ADR-0028 item 2) |
| HUMAN APPROVAL REQUIRED? | **YES** (risk acceptance) |

### SR-11 — Shared origin and the in-memory admin token

| Field | Content |
|---|---|
| Finding | Admin and farmer screens are one Next.js application on one origin (ADR-0017; `FRONTEND_ARCHITECTURE.md`). An XSS flaw anywhere in the application could read an admin's in-memory access token during an admin session. The CSP is "finalized with the frontend implementation" (ADR-0017 item 7) |
| Affected | ADR-0017 items 3 and 7; `SECURITY_ARCHITECTURE.md` §4.2 |
| Severity | Low (no flaw exists; depends on future implementation) |
| Exploit / impact | Token theft lasts at most 15 minutes, and refresh still needs the `HttpOnly` cookie |
| Current mitigation | <ul><li>Token in memory only.</li><li>CSP with `default-src 'self'` and `frame-ancestors 'none'`.</li><li>`X-Frame-Options: DENY`.</li><li>15-minute tokens.</li></ul> |
| Recommended resolution | Make the finalized CSP a release gate of the frontend implementation. No change now |
| HUMAN APPROVAL REQUIRED? | NO (implementation verification item) |

### SR-12 — Targeted login lockout

| Field | Content |
|---|---|
| Finding | After 10 failed logins for one identifier within 15 minutes, every login for it answers 429, even with the correct password. Anyone who knows a farmer's number can keep that farmer locked out |
| Affected | ADR-0017 item 6; `login` description |
| Severity | Low. Core farmer flows need no login; alerts do |
| Exploit / impact | Denial of sign-in for a chosen account, lasting as long as the attacker keeps sending failures |
| Current mitigation | Per-IP limit of 10 per minute; the lockout expires after 15 minutes. Answering the same 429 for known and unknown identifiers prevents an existence oracle |
| Recommended resolution | Accept for Phase 1. Any change to the counting key needs a decision and must keep the no-oracle property |
| HUMAN APPROVAL REQUIRED? | **YES** (risk acceptance) |

### SR-13 — Unlimited endpoints and shared IPs

| Field | Content |
|---|---|
| Finding | ADR-0017 limits only compare and simulate, login, register and refresh. Public reads (geography, commodities, market detail, trends, forecast), alerts and admin operations have no limit. Separately, many mobile users can share one public IP (carrier-grade NAT), so per-IP limits can throttle legitimate farmers together |
| Affected | ADR-0017 item 6; ADR-0018 |
| Severity | Low |
| Exploit / impact | Scraping of public reference and price data, which is government data already public, and database load. Shared-IP users may receive 429 on compare |
| Current mitigation | One backend instance with Redis counters and an in-process fallback (ADR-0018); 429 with `Retry-After` |
| Recommended resolution | Decide whether to add a general per-IP limit for `/api/v1/` at Nginx. It is deployment configuration, but the ADR-0017 table would need an amending entry. Monitor 429 rates after deployment before tuning |
| HUMAN APPROVAL REQUIRED? | **YES** only if a new limit is added |

### SR-14 — No client rule for a rate-limited refresh

| Field | Content |
|---|---|
| Finding | `refreshSession` declares 429 (10 per minute per IP). `FRONTEND_CONSUMPTION.md` §4 says "never retry `refreshSession` itself", which was written for `REFRESH_TOKEN_INVALID`. A client could therefore treat a 429 as a sign-out |
| Affected | `refreshSession`; `FRONTEND_CONSUMPTION.md` §4; ADR-0017 item 6 |
| Severity | Low (availability) |
| Exploit / impact | Users behind a shared IP could be signed out unnecessarily |
| Current mitigation | None specified |
| Recommended resolution | Document: on 429 from refresh, wait `Retry-After` and retry once. [ASSUMPTION, to verify in implementation] A request over the limit is rejected before the token is processed, so the token stays valid |
| HUMAN APPROVAL REQUIRED? | NO (documentation clarification within ADR-0017) |

### SR-15 — Authentication error semantics

| Field | Content |
|---|---|
| Finding | Consistent and non-revealing, apart from SR-01: <ul><li>the same 401 `INVALID_CREDENTIALS` for unknown, wrong and inactive accounts;</li><li>429 regardless of password while over the limit;</li><li>a dummy BCrypt check for unknown identifiers;</li><li>400 `TOO_LONG` for passwords over 72 bytes, never truncated;</li><li>`WWW-Authenticate: Bearer realm="agrinex"` with `error="invalid_token"` for rejected tokens;</li><li>the same `REFRESH_TOKEN_INVALID` for missing, expired, revoked and reused tokens;</li><li>403 `FORBIDDEN` for a role mismatch, and 403 for a wrong or missing `Origin` on refresh and logout;</li><li>404 for another user's alert or event;</li><li>500 and 503 bodies without `detail`.</li></ul> |
| Affected | `login`, `refreshSession`, `logout`, the `Unauthorized`, `Forbidden` and `OriginNotAllowed` responses; `error-response.schema.json` |
| Severity | Informational |
| Exploit / impact | None found |
| Current mitigation | As listed |
| Recommended resolution | Confirm as part of SR-00. Implementation tests should cover each case |
| HUMAN APPROVAL REQUIRED? | NO, beyond SR-00 |

### SR-16 — Sensitive data in responses and logs

| Field | Content |
|---|---|
| Finding | **Responses:** no secret or internal key is exposed. <ul><li>`AuthResponse` carries the access token and `{id, role, displayName, defaultDistrictId}`, with no phone number or email.</li><li>The refresh token appears only in the `HttpOnly` cookie.</li><li>The audit actor is `{userId}` only.</li><li>System status shows only CONFIGURED or NOT_CONFIGURED for the API key.</li><li>Raw payloads have the API key removed (ADR-0020 item 5).</li><li>The correlation id is generated by the server, and a client value is ignored.</li></ul>The only residual exposure is SR-02. **Logs:** ADR-0020 item 5 and `SECURITY_ARCHITECTURE.md` exclude secrets, `Authorization` headers, cookies and the `api-key` parameter. They do not explicitly exclude request bodies, which contain passwords (auth), phone numbers and coordinates |
| Affected | ADR-0020 item 5; `SECURITY_ARCHITECTURE.md` (logging); `DATA_DICTIONARY.md` sensitivity classes |
| Severity | Low (logging rule gap) |
| Exploit / impact | A debug-level body logger could write passwords, phone numbers or coordinates to logs |
| Current mitigation | "Logs never contain secrets" (ADR-0020 item 5) already covers passwords by implication |
| Recommended resolution | Add an explicit rule to the security architecture when implementation starts: request and response bodies are not logged, and personal data (phone numbers, coordinates) never appears in logs. Test it |
| HUMAN APPROVAL REQUIRED? | **YES** (adds a logging rule to the security architecture) |

## 4. Human sign-off

The reviewer records one outcome per finding: ACCEPT (risk accepted as is), RESOLVE (apply the recommended resolution), or OTHER (state it). Resolutions that change the contract follow spec §41. Resolutions that change architecture need a decision-log entry.

| ID | Outcome | Reviewer | Date | Notes |
|---|---|---|---|---|
| SR-00 | RESOLVE | Human Project Owner | 2026-10-04 | Security-sensitive contract approved with this review incorporated (ADR-0042) |
| SR-01 | ACCEPT | Human Project Owner | 2026-10-04 | Residual Phase 1 risk; OTP/SMS is outside Phase 1. Revisit when Phase 2 OTP exists |
| SR-02 | ACCEPT | Human Project Owner | 2026-10-04 | After RD-042 option A (ADR-0039): runs carry only district centroids |
| SR-03 | ACCEPT | Human Project Owner | 2026-10-04 | 15-minute stateless access tokens accepted for Phase 1 |
| SR-04 | RESOLVE | Human Project Owner | 2026-10-04 | Token use resolves the account; inactive or removed accounts answer 401 on compare and simulate too (ADR-0042) |
| SR-05 | RESOLVE | Human Project Owner | 2026-10-04 | Through RD-044 option C (ADR-0041) |
| SR-06 | RESOLVE | Human Project Owner | 2026-10-04 | Cookie renamed to `__Secure-agrinex_refresh`; attributes unchanged (ADR-0042) |
| SR-07 | ACCEPT | Human Project Owner | 2026-10-04 | Informational; no absolute session lifetime in Phase 1 |
| SR-08 | RESOLVE | Human Project Owner | 2026-10-04 | Through RD-040 option C (ADR-0037); no HTTP erasure endpoint |
| SR-09 | RESOLVE | Human Project Owner | 2026-10-04 | Through RD-042 option A (ADR-0039) |
| SR-10 | ACCEPT | Human Project Owner | 2026-10-04 | Password-only admin sign-in; no MFA, because the SSOT does not require it |
| SR-11 | OTHER | Human Project Owner | 2026-10-04 | Implementation gate: final CSP verified during frontend implementation |
| SR-12 | ACCEPT | Human Project Owner | 2026-10-04 | Targeted login lockout accepted for Phase 1 |
| SR-13 | OTHER | Human Project Owner | 2026-10-04 | No new general per-IP limit; ADR-0017 limits kept; tuning is deployment and observability work |
| SR-14 | RESOLVE | Human Project Owner | 2026-10-04 | Client rule documented: wait `Retry-After` and retry once; no new product decision |
| SR-15 | ACCEPT | Human Project Owner | 2026-10-04 | Existing non-revealing authentication semantics |
| SR-16 | RESOLVE | Human Project Owner | 2026-10-04 | Request and response bodies never logged; no phone numbers or coordinates in logs (ADR-0042) |

The outcomes were given in the conversation of 2026-10-04 ("TASK 8.6 — FINAL DECISION LOCK + SECURITY SIGN-OFF + SKILL POPULATION + PROJECT READINESS") and are recorded as ADR-0042 in `docs/architecture/DECISION_LOG.md`.

