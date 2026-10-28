# AgriNex — API Contract Conventions

**Status:** AUTHORITATIVE, part of the API contract area (SSOT index area 4). Created by Task 8 on 2026-10-04.

- The machine-readable contract is `contracts/openapi.yaml` with the JSON Schemas in `contracts/`. Where this document and a contract file differ, the contract file governs and this document must be corrected.
- These conventions apply ADR-0022 (contract strategy and conventions) and the other binding entries cited. Rules that no specification section or decision fixes are labelled **[CONTRACT DESIGN]**. They are Task 8 choices within ADR-0022, and they change only through the spec §41 Shared Contract Change Rule.

Labels used in `docs/api/`:

| Label | Meaning |
|---|---|
| [SSOT REQUIREMENT] | Stated by the master specification |
| [ARCHITECTURAL DECISION] | Set by a binding decision-log entry (APPROVED or DECIDED) |
| [VERIFIED TECHNICAL FACT] | Verified against current official documentation (RFC, OpenAPI, JSON Schema, Spring) |
| [DATA EVIDENCE] | Shown by the verified government-data evidence |
| [CONTRACT DESIGN] | A Task 8 contract choice within ADR-0022. Not architecture |
| [ASSUMPTION] | A reversible interpretation, listed in `API_INDEX.md` §8 |
| [REQUIRES_DECISION] | Registered in `docs/REQUIRES_DECISION.md` |

## 1. Scope

The contract covers the public REST API under `/api/v1` for Phase 1 [SSOT REQUIREMENT, spec §30], plus the optional forecast operation [ARCHITECTURAL DECISION, ADR-0030].

Not covered:

- `/actuator/health`, which is internal and answers 404 at Nginx (ADR-0017);
- the frontend's own `/api/health` (spec §45);
- the government API, which only the ingestion module calls (spec §31 RULE 1).

## 2. Versioning and compatibility

[ARCHITECTURAL DECISION, ADR-0022 item 2]:

- The path carries the major version: `/api/v1`.
- `info.version` in `openapi.yaml` follows semantic versioning. Contract version **1.0.0** is the first version.
- Each `contracts/*.schema.json` file carries its own `x-agrinex-contract-version`, and `openapi.yaml` lists them under `info.x-agrinex-contract-files`. Spec §41 step 3 requires the version in the contract file to be incremented on every change.
- A change follows spec §41: a PR with a description and reason, every consuming role tagged, the versions incremented, the mocks updated in the same change, and at least two approvals from consuming roles.
- [CONTRACT DESIGN] A change to any file that `openapi.yaml` references counts as a change of `openapi.yaml` too: its consumers review it, and `info.version` is incremented along with the file's own version.

[CONTRACT DESIGN] What counts as additive (a minor version) and what as breaking (a new major version and path):

| Change | Kind |
|---|---|
| New operation, new optional request property, new optional query parameter | Additive |
| New response property | Additive. Clients must ignore response properties they do not know (tolerant reader) |
| New value of an enumeration marked `x-agrinex-extensible: true` (for example `ErrorCode`, `RankingMethod`, `SourceDataset`) | Additive. Clients must handle unknown values: an unknown error code by its HTTP status, an unknown ranking method by showing no basis label |
| New value of any other enumeration (for example `ReasonCode`, `FreshnessStatus`, `TrendDirection`) | Breaking |
| Removing or renaming an operation, property, parameter or enumeration value; making an optional request property required; tightening a request constraint; changing a type, unit, rounding or meaning | Breaking |
| Changing a description without changing behavior | Patch |

- Requests are closed: a property the contract does not define is rejected with `VALIDATION_FAILED` / `UNKNOWN_FIELD`.
- Responses are closed in the schemas so that contract tests detect drift and leaks. At runtime, clients still apply the tolerant-reader rule above.
- **Deprecation** [CONTRACT DESIGN]: a property or operation to be removed is first marked `deprecated: true` with a replacement named in its description. It stays for at least one minor version, and it is removed only in a new major version.
- Internal changes (implementation, database, ML model) never change the contract version (ADR-0022 item 1).

## 3. Media types and JSON shape

- Request and success bodies use `application/json` in UTF-8. Error bodies use `application/problem+json` (RFC 9457; ADR-0022 item 4).
- Property names are camelCase.
- **Presence and null** [CONTRACT DESIGN]:
  - every property a response schema lists is present, and `null` means "no value" (for example `minPrice: null` when the source omitted it);
  - optional request properties may be omitted or sent as `null`, and both mean "not set".
- **Lists** [CONTRACT DESIGN]: every list is wrapped in an object with `items`, so that metadata can be added later without breaking clients. Reference lists (states, districts, localities, commodities, varieties, grades) are not paginated (ADR-0022 item 5).

## 4. Data types and precision

| Kind | JSON representation | Source |
|---|---|---|
| Identifier | UUID string, lower case in responses, opaque | ADR-0022 item 11 |
| Calendar date | `yyyy-MM-dd`. Date rules use the Asia/Kolkata calendar ("today" is the IST date) | ADR-0007, ADR-0022 |
| Instant | RFC 3339 with an offset. The server emits UTC (`Z`) [CONTRACT DESIGN] | ADR-0022 |
| Source price (`minPrice`, `maxPrice`, `modalPrice`) | JSON number > 0, at most 2 decimal places, in `priceUnit`. Never converted | ADR-0008 |
| `priceUnit` | String in the source's notation, for example `Rs./Quintal` | ADR-0008 |
| Computed amount (`estimatedTransportCost`, `estimatedGrossValue`, `estimatedNetRealization`) | JSON number in INR, rounded half-up to 2 decimal places, with `currency: "INR"`. Net realization can be negative | ADR-0008, ADR-0022 item 10 |
| Farmer quantity | JSON number > 0, at most 3 decimal places, with `quantityUnit` `KG` or `QUINTAL` | ADR-0022, ADR-0023, `selling_searches.quantity numeric(12,3)` |
| `quantityInQuintal` | Exact conversion (KG / 100), at most 5 decimal places | spec §32 |
| Distance (`distanceKm`) | JSON number ≥ 0, rounded half-up to 2 decimal places from the shortest decimal form of the computed double (§4.1) | `selling_options.distance_km numeric(8,2)` [CONTRACT DESIGN: rounding point] |
| Radius (`maxDistanceKm`) | Integer km | `selling_searches.max_distance_km integer` |
| Coordinates | JSON numbers, at most 6 decimal places, WGS84 | `numeric(9,6)` |
| `changePercent7Day` | Percent, rounded half-up to 1 decimal place for display; `null` with `INSUFFICIENT_DATA`. The direction threshold (±3 %) and percentage alerts use the unrounded value | ADR-0025 [CONTRACT DESIGN: display rounding]; alerts: T8-A6 |
| `completenessPercent` | Percent, rounded half-up to 2 decimal places; `null` while RUNNING or with a zero denominator | ADR-0012 [CONTRACT DESIGN: rounding] |
| Alert threshold and trigger value | JSON number, at most 4 decimal places (price in `priceUnit`, or percent) | `numeric(14,4)`, spec §34 |
| Weights | 0 to 1, at most 4 decimal places | ADR-0020, `numeric(6,4)` |
| Confidence | **No confidence value exists.** A forecast carries only the bounds the model produced, without a coverage level | ADR-0030; `forecasts` table |
| Enumerations | UPPER_SNAKE strings | ADR-0022 item 4 |

- Every number schema declares `x-agrinex-max-decimal-places`. Contract tests enforce it, and Bean Validation uses `@Digits` with the same fraction digits. `multipleOf` is not used, because binary floating point makes decimal `multipleOf` unreliable in many validators.
- Numbers are serialized without exponent notation.

### 4.1 Calculation precision

This rule keeps the browser What-If and the server identical [ARCHITECTURAL DECISION, ADR-0026 item 1; CONTRACT DESIGN: rounding points]. All arithmetic is exact decimal arithmetic, and only the named results are rounded, half-up to 2 decimal places (`round2`):

```text
quantityInKg            = quantity × (1 if quantityUnit = KG else 100)
quantityInQuintal       = quantityInKg / 100                                   (exact)
distanceKm              = round2(shortestDecimal(haversine(origin, market)))   (the rounded value is used below)
trips                   = ceil(quantityInKg / vehicleCapacityKg)
perTripCost             = max(minimumTripCharge, distanceKm × appliedRatePerKm)  (exact)
estimatedTransportCost  = round2(perTripCost × trips)
estimatedGrossValue     = round2(modalPrice × quantityInQuintal)
estimatedNetRealization = estimatedGrossValue − estimatedTransportCost         (exact on 2-dp values)
```

- The haversine Earth radius is 6 371.0088 km, the WGS 84 / GRS 80 mean radius [ARCHITECTURAL DECISION, ADR-0036]. Only the server computes distances, so clients are unaffected.
- `shortestDecimal` converts the double result of the haversine formula to the shortest decimal that converts back to the same double, before rounding. In Java 25, `BigDecimal.valueOf(double)` does this, because it uses `Double.toString(double)` [VERIFIED TECHNICAL FACT: OpenJDK 25 API documentation, via Context7]. Python's `repr` of a float gives the same digits; the synthetic mocks use it.
- In JavaScript, use a decimal library or integer paise arithmetic. Never compare floating-point results directly with server values.

## 5. Pagination

[ARCHITECTURAL DECISION, ADR-0022 item 5]; ordering rules are [CONTRACT DESIGN].

| Rule | Value |
|---|---|
| Parameters | `page` (zero-based, default 0), `size` (default 20, 1 to 100). A larger `size` is rejected with 400, not clamped |
| Response | `items`, `page`, `size`, `totalItems`, `totalPages` |
| Empty result | `items: []`, `totalItems: 0`, `totalPages: 0` |
| Page past the end | 200 with `items: []` and the true totals |
| Ordering | Fixed per operation and documented on it, always ending with `id` as the tie-break, so pages are deterministic |
| Name order | Unicode code-point order of the stored name, then `id` ascending. It equals Java's `String.compareTo` order for names within the Basic Multilingual Plane. PostgreSQL 16 gives it with `COLLATE "C"` or `ucs_basic` in a UTF8 database; locale collations do not [VERIFIED TECHNICAL FACT: PostgreSQL 16 documentation §24.2.2.1] |

Paginated operations and their order:

| Operation | Order |
|---|---|
| `listAlerts` | `lastTriggeredAt` descending (never-triggered last), then `id` |
| `listAlertEvents` | `triggeredAt` descending, then `id` |
| `listIngestionRuns` | `startedAt` descending, then `id` |
| `listIngestionRunErrors` | `createdAt` ascending, then `id` |
| `listTransportAssumptionVersions`, `listRecommendationWeightVersions` | `createdAt` descending, then `id` |
| `listAdminMarkets`, `listAdminCommodities` | name (code-point order), then `id` |
| `listDistrictAliases` | source state name, then source district name (code-point order), then `id` |
| `listAdminAuditEvents` | `createdAt` descending, then `id` |

## 6. Errors

[ARCHITECTURAL DECISION, ADR-0022 item 4; VERIFIED TECHNICAL FACT, RFC 9457.] Every non-2xx response has the `ApiErrorResponse` body defined in `contracts/error-response.schema.json`:

| Member | Rule |
|---|---|
| `type` | `/problems/<code in kebab case>`, a relative URI reference with the full path. RFC 9457 §3.1.1 allows relative and non-resolvable type URIs [CONTRACT DESIGN] |
| `title` | Fixed English title per code |
| `status` | The HTTP status |
| `detail` | Optional English text about this occurrence |
| `instance` | Optional: the request path, without the query string |
| `code` | Stable identifier, and the frontend's key for localized messages |
| `correlationId` | Equals the `X-Correlation-Id` header |
| `errors[]` | Field problems: `field`, `code`, `message` |

- Stack traces, SQL, class names, tokens, secrets and other users' data never appear (spec §28).
- The schema enforces one status, `type` and `title` per code.

**Status split** [ARCHITECTURAL DECISION, ADR-0022 item 4; CONTRACT DESIGN: the 404 and 422 assignment]:

- **400**: the request fails checks that need no data. JSON syntax gives `MALFORMED_REQUEST`; types, formats, required properties, ranges, enumerations, unknown properties and cross-field rules give `VALIDATION_FAILED`.
- **404**: the resource named in the path does not exist or is not visible to the caller.
- **422**: a well-formed request that the data cannot satisfy: `NO_MARKETS_IN_RANGE`, `UNSUPPORTED_PRICE_UNIT`, `REFERENCE_NOT_FOUND` (an id in the body or query names nothing that exists or qualifies), `ESTIMATE_OUT_OF_RANGE` (ADR-0040), and `PASSWORD_RESET_TOKEN_INVALID` and `EMAIL_VERIFICATION_TOKEN_INVALID` (a well-formed link token that names no usable token, ADR-0048). A malformed token is 400 `VALIDATION_FAILED`.
- **One documented exception** [ARCHITECTURAL DECISION, ADR-0039]: a comparison origin that is not a stored district centroid answers 400 `VALIDATION_FAILED` with `INVALID_VALUE` on `originLat` and `originLon`. The set of valid origins is published reference data that the client copies from `listDistricts`, so a non-matching origin is an invalid field value, not a missing record.

### 6.1 Error codes

| `code` | Status | `title` | When |
|---|---|---|---|
| `MALFORMED_REQUEST` | 400 | Malformed request | The body is not valid JSON or not a JSON object |
| `VALIDATION_FAILED` | 400 | Validation failed | One or more field problems; `errors[]` is non-empty |
| `UNAUTHORIZED` | 401 | Unauthorized | Missing, malformed, expired or invalid access token (spec §29). The client refreshes once, then retries |
| `INVALID_CREDENTIALS` | 401 | Invalid credentials | Login failed. Unknown identifier, wrong password and inactive account give the same answer |
| `REFRESH_TOKEN_INVALID` | 401 | Session cannot be refreshed | The refresh cookie is missing, unknown, expired, revoked or reused, or its account is inactive or gone. The client signs in again and never retries the refresh |
| `FORBIDDEN` | 403 | Forbidden | The caller's role is not allowed (spec §28) |
| `ORIGIN_NOT_ALLOWED` | 403 | Origin not allowed | Refresh or logout with an absent, `null` or different `Origin` (ADR-0017), checked before the cookie is read |
| `NOT_FOUND` | 404 | Not found | Unknown path resource; another user's alert or event; a PROVISIONAL or MERGED market on a public operation (ADR-0014) |
| `METHOD_NOT_ALLOWED` | 405 | Method not allowed | Wrong HTTP method |
| `NOT_ACCEPTABLE` | 406 | Not acceptable | `Accept` excludes JSON |
| `CONFLICT` | 409 | Conflict | State conflict, for example a run already RUNNING (ADR-0011), a market already verified, a phone number or email address already registered (`errors[]` names the field with `ALREADY_EXISTS`; ADR-0047) |
| `UNSUPPORTED_MEDIA_TYPE` | 415 | Unsupported media type | The body is not `application/json` |
| `NO_MARKETS_IN_RANGE` | 422 | Insufficient data | No VERIFIED, active market with coordinates within `maxDistanceKm` (spec §16; ADR-0023) |
| `UNSUPPORTED_PRICE_UNIT` | 422 | Unsupported price unit | Comparison or alert for a commodity whose verified unit is not `Rs./Quintal`, or is unverified (ADR-0008) |
| `REFERENCE_NOT_FOUND` | 422 | Referenced resource not found | An id in the body or query names nothing that exists or qualifies; `errors[]` names each field with `NOT_FOUND` |
| `ESTIMATE_OUT_OF_RANGE` | 422 | Estimate out of range | `compareSellingOptions` only: an estimate of some option would exceed the stored precision (absolute value above 999 999 999 999.99 INR). `errors[]` names `quantity` with `OUT_OF_RANGE`. Nothing is recorded, and no quantity limit applies (ADR-0023 item 1; ADR-0040) |
| `PASSWORD_RESET_TOKEN_INVALID` | 422 | Reset link invalid or expired | `confirmPasswordReset`: the token is unknown, expired, spent or replaced by a newer link, or its account is no longer active (ADR-0048). The client offers to request a new link |
| `EMAIL_VERIFICATION_TOKEN_INVALID` | 422 | Verification link invalid or expired | `confirmEmailVerification`: the token is unknown, expired, spent (the email is already verified) or replaced, or its account is no longer active (ADR-0048) |
| `RATE_LIMITED` | 429 | Too many requests | A rate limit was exceeded; `Retry-After` gives the seconds (ADR-0017) |
| `INTERNAL_ERROR` | 500 | Internal error | An unexpected server fault. `detail` is always absent (the schema enforces it) |
| `SERVICE_UNAVAILABLE` | 503 | Service unavailable | A required dependency, such as the database, is unavailable (spec §29). `detail` is always absent |

Field error codes (`errors[].code`):

| Code | Meaning |
|---|---|
| `REQUIRED` | Missing or null required value |
| `INVALID_TYPE` | Wrong JSON type |
| `INVALID_FORMAT` | Pattern or format mismatch (phone, UUID, date) |
| `INVALID_VALUE` | Not one of the allowed values |
| `OUT_OF_RANGE` | Below the minimum or above the maximum |
| `TOO_SHORT` / `TOO_LONG` | Length out of bounds. Passwords: characters, or more than 72 UTF-8 bytes |
| `PRECISION_EXCEEDED` | Too many decimal places |
| `INVALID_RANGE` | Cross-field rule, for example `fromDate` after `toDate`, a range of more than 366 report dates counted inclusively (T8-A7), or freshness days out of order. Named on the later field |
| `NOT_ALLOWED` | Not allowed in this context, for example `fromDate` for `CURRENT_PRICE` |
| `UNKNOWN_FIELD` | Property not defined by the contract |
| `NOT_FOUND` | With `REFERENCE_NOT_FOUND` |
| `ALREADY_EXISTS` | With `CONFLICT` |

Client handling: choose the message by `code`, and by `errors[].code` with `errors[].field`. Fall back to the HTTP status for an unknown code. Log `correlationId`.

## 7. Security

[SSOT REQUIREMENT, spec §27, §28; ARCHITECTURAL DECISION, ADR-0016, ADR-0017, ADR-0018, ADR-0022 item 7.] Security-sensitive: any change needs explicit human review (CLAUDE.md rule 16).

| Group | `security` | Roles |
|---|---|---|
| `register`, `login` | none | — |
| `requestPasswordReset`, `confirmPasswordReset`, `confirmEmailVerification` | none | — |
| `resendEmailVerification` | Bearer | FARMER, ADMIN |
| `refresh`, `logout` | refresh cookie (`logout` also accepts none), plus `Origin` equal to the public origin | — |
| geography, commodities, markets (detail, trends, forecast) | none | anonymous |
| `compare`, `simulate` | none, or Bearer (links the recorded search to the caller's farmer profile) | anonymous |
| alerts | Bearer | FARMER, ADMIN |
| `/api/v1/admin/` | Bearer | ADMIN |

- **Access token:** HS256 JWT, 15 minutes, with the claims `sub`, `role`, `iat`, `exp` and `iss=agrinex`. Kept in memory only (ADR-0017).
- **Refresh token:** opaque, 7 days, rotated on every refresh with token-family reuse detection (RFC 9700 §4.14.2). It travels only in the `__Secure-agrinex_refresh` cookie (`HttpOnly; Secure; SameSite=Strict; Path=/api/v1/auth`) and never appears in a body. The `__Secure-` prefix is part of the name, and the server reads only the full name. `__Host-` is not possible, because it requires `Path=/` [ARCHITECTURAL DECISION, ADR-0042; VERIFIED TECHNICAL FACT, MDN `Set-Cookie`, cookie prefixes].
- **Which operations read the token:** only operations whose `security` lists `bearerAuth` evaluate an `Authorization` header, and there an invalid or expired token answers 401. Every other operation ignores it, including `refresh` and `logout`, so a stale header never blocks a refresh or a logout.
- **On 401 `UNAUTHORIZED`** the client calls `refreshSession` once (serialized across tabs) and retries the request. On `REFRESH_TOKEN_INVALID` it retries operations that also allow anonymous access without the token, and otherwise goes to sign-in. A 429 from `refreshSession` is not a sign-out: the client waits `Retry-After` seconds and retries once (ADR-0042, SR-14).
- **Deactivated or removed accounts** [ARCHITECTURAL DECISION, ADR-0042 (SR-03, SR-04); ADR-0041]:
  - login answers 401 `INVALID_CREDENTIALS`;
  - `refresh` answers 401 `REFRESH_TOKEN_INVALID` and revokes the token family;
  - every operation that evaluates the access token resolves the caller's account and answers 401 `UNAUTHORIZED` for it. That covers the alert and admin operations, and compare and simulate when a token is sent;
  - the 15-minute stateless access token is otherwise accepted for Phase 1 (SR-03).
- **Operator procedures:** account erasure, deactivation and reactivation have no HTTP operation. They are in-application operator procedures, audited as `ACCOUNT_ERASED`, `ACCOUNT_DEACTIVATED` and `ACCOUNT_REACTIVATED` (ADR-0037, ADR-0041).
- **401 responses** carry `WWW-Authenticate: Bearer realm="agrinex"`, with `error="invalid_token"` when a presented token was rejected [VERIFIED TECHNICAL FACT, RFC 9110 §15.5.2; RFC 6750 §3].
- **Token responses** carry `Cache-Control: no-store` [CONTRACT DESIGN, following RFC 6749 §5.1 practice].
- **Rate limits** use fixed one-minute windows per client IP, and still apply when Redis is down (ADR-0018). Exceeding one answers 429 `RATE_LIMITED` with `Retry-After`:

  | Operation | Limit |
  |---|---|
  | `compare` + `simulate` (shared) | 60 per minute |
  | `login`, `register`, `refresh` | 10 per minute each |
  | Failed logins per identifier | 10 per 15 minutes |
  | `requestPasswordReset`, `confirmPasswordReset`, `confirmEmailVerification`, `resendEmailVerification` | 10 per minute each (ADR-0048) |
  | Password-reset requests per identifier | 3 per hour, counting unknown identifiers like known ones (ADR-0048) |
  | Verification-link resends per account | 3 per hour (ADR-0048) |

- **Email verification and password recovery** [ARCHITECTURAL DECISION, ADR-0047, ADR-0048; OWASP Forgot Password Cheat Sheet]:
  - Registration requires an email address (lower case, unique). It is a second login identifier for the same account and password, and the only recovery channel. There is no SMS or OTP (spec §39).
  - Links carry a single-use token of 256 random bits (base64url), sent only to the account's email address, built on the configured public origin (never the request's `Host`), and placed in the URL fragment so that it reaches no server log or `Referer`. Only the SHA-256 hash is stored. Verification links last 24 hours, reset links 30 minutes; a new link replaces the previous one.
  - A reset link is sent only to a verified email of an active account. A successful reset revokes every refresh-token family, clears the refresh cookie, does not sign the user in, and sends a "password changed" notice.
  - Email addresses, tokens and links are never logged and never returned.
- **No secret is readable or writable** through any operation. Admins see only whether the government API key is configured (ADR-0020).
- **Account enumeration:**
  - Login answers the same 401 for unknown, wrong and inactive accounts.
  - The failed-login limit counts the normalized identifier and treats unknown identifiers like known ones. While an identifier is over the limit, every login for it answers 429, even with the correct password.
  - An unknown identifier still costs one BCrypt verification, so timing does not reveal existence.
  - A password over 72 UTF-8 bytes answers 400, never truncated.
  - Another user's alert or event answers 404.
  - Registration necessarily reveals that a number or an email address is registered (409); the register rate limit bounds probing. This residual risk is accepted for Phase 1 and revisited when Phase 2 brings OTP (ADR-0042, SR-01; extended to email by ADR-0048).
  - A password-reset request answers the same 202 for every well-formed identifier, sends its email after the response, and counts unknown identifiers like known ones in its limit (ADR-0048).
  - The mobile number and the email of one account are separate identifiers for every limit, so no response links the two (ADR-0048).
- **No secret in stored text:** ingestion removes the configured API key and any `api-key` parameter before storing error messages or raw payloads that admins can read (ADR-0020 item 5).

## 8. Headers

| Header | Where | Rule |
|---|---|---|
| `X-Correlation-Id` | Every response | Server-generated, 8 to 100 characters of `[A-Za-z0-9-]`. A client value is ignored [CONTRACT DESIGN; spec §27 requires a correlation id per request] |
| `Retry-After` | 429 | Seconds |
| `WWW-Authenticate` | 401 | As in §7 |
| `Set-Cookie` | register, login, refresh (set); logout and refresh 401 (clear) | ADR-0017 attributes |
| `Cache-Control: no-store` | register, login, refresh | As in §7 |
| `Location` | 202 of `triggerIngestion` | Path of the run |
| `Origin` (request) | refresh, logout | Must equal the public origin (ADR-0017). Absent or different answers 403, never 400 |

## 9. Contract-design choices (summary)

Each choice below is Task 8 contract content, not an architecture decision. It can be changed through spec §41:

1. Error `type` URIs of the form `/problems/<code>`, with one fixed title per code.
2. The 400 / 404 / 422 assignment above, including `REFERENCE_NOT_FOUND`.
3. Closed request and response schemas; extensible-enumeration marking; the tolerant-reader rule.
4. Instants emitted in UTC.
5. The `X-Correlation-Id` header; `Cache-Control: no-store` on token responses. The cookie name `__Secure-agrinex_refresh` is fixed by ADR-0042.
6. List wrapper objects; the ordering of each paginated operation.
7. Rounding points (§4.1) and the decimal-place annotation.
8. `markAlertEventRead` answers 204 without a body.
9. Simulation quantities in kilograms (spec §17 slider), echoed in `scenario.userOverrides`. A simulation sends only changed values; an omitted value keeps the comparison's.
10. A raw-payload excerpt cut at 4,000 characters for admins (ADR-0020 item 2 "raw-payload excerpts").
11. Deactivated and deleted accounts are refused by `refresh` and by account-resolving operations (§7); 500 and 503 bodies never carry `detail`.
12. Name ordering by code point, then `id` (§5).
13. Admin name inputs (`AddCommodityRequest.name`, district-alias source names) are trimmed and each run of whitespace collapsed to one space before storage, as the database checks require; a value empty after that answers 400 `TOO_SHORT`.
14. `decisionBasis.weightsVersionId` identifies the weights version behind a ranking and its freshness labels.
15. The public forecast point (`ForecastPointResponse`) is defined in `openapi.yaml`, independent of the ML output file.
