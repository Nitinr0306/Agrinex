# AgriNex — Frontend Consumption of the API Contract

**Status:** AUTHORITATIVE, part of the API contract area (SSOT index area 4). Created by Task 8 on 2026-10-04.

- The contract files govern where they differ.
- Screen design stays with role file 02 and Task 9 (ADR-0027; CLAUDE.md §M). This document defines only what the API gives each screen.
- Labels as in [API_CONVENTIONS.md](API_CONVENTIONS.md).

## 1. What the frontend never needs to know

Database tables, ingestion runs (except on the admin screens), Redis, ML infrastructure, module internals and the government API. Every screen works from the documented operations and fields only (FRONTEND_ARCHITECTURE §4.4).

## 2. Screens, operations and states

"Loading" is the time until the response arrives. Its visual design belongs to Task 9 (FRONTEND_ARCHITECTURE §4.8).

| Screen | Operation | Empty / no data | Stale or outage | Error states | Auth |
|---|---|---|---|---|---|
| Location wizard | `listStates`, `listDistricts`, `listLocalities` | `items: []` | — | 404 for an unknown parent; 503 | Anonymous |
| Commodity selector | `listCommodities`, `listVarieties`, `listGrades` | `items: []`: no commodity has recent data | — | 404; 503 | Anonymous |
| Comparison results | `compareSellingOptions` | `options: []` + `noDataReason: NO_RECENT_PRICE_DATA` (show `excludedMarkets`); 422 `NO_MARKETS_IN_RANGE` → "No data in range. Try increasing distance." with an adjust-distance control (spec §25) | `sourceStatus.latestSyncFailed` → §29 banner; `price.freshnessStatus` badges; `STALE_DATA` reason | 400 field errors, including a non-centroid origin (send the `listDistricts` centroid unchanged; ADR-0039); 401 only when a sent token is invalid or its account inactive (§4); 422 `UNSUPPORTED_PRICE_UNIT`, `REFERENCE_NOT_FOUND`, `ESTIMATE_OUT_OF_RANGE` (ADR-0040); 429 with `Retry-After` | Anonymous (an optional token links the search to the profile) |
| Winner banner | same response | Hidden when `rankingsDisagree` is false. Wording: §2.1 | — | — | — |
| "Why recommended?" | `options[].reasonCodes`, `estimates`, `trendDirection`, `price` | `reasonCodes: []` possible | — | — | — |
| Trend chart | `getMarketTrend` (`days` 7 or 30) | `INSUFFICIENT_DATA` with `changePercent7Day: null`, `observations` possibly empty → "Insufficient historical data …" (spec §33) | `sourceStatus`; per-point freshness | 404 market; 422 commodity | Anonymous |
| Market detail | `getMarketDetail` | `latestPrices: []` + `noDataReason` | `sourceStatus`; badges | 404 for an unverified market | Anonymous |
| What-If simulator | client-side (§3); `simulateSellingOptions` only when the radius grows | as comparison | as comparison | as comparison | Anonymous |
| Forecast section (optional ML only) | `getMarketForecast` | `available: false` → "Forecast unavailable." (spec §29) | — | Never a 5xx because of ML | Anonymous |
| Alerts | `listAlerts`, `createAlert`, `updateAlertStatus`, `deleteAlert` | `items: []` | — | 401 → refresh; 403; 404; 422 | FARMER or ADMIN |
| Alert notifications | `listAlertEvents` (poll on focus and while open, ADR-0019), `markAlertEventRead` (204) | `items: []`; `unreadOnly=true` gives the unread count | `sourceStatus` | 401; 404 | FARMER or ADMIN |
| Login and quick registration | `login`, `registerFarmer`, `refreshSession`, `logout` | — | — | 401 `INVALID_CREDENTIALS`; 409 (number or email registered; `errors[].field`); 429 | — |
| Email verification | `confirmEmailVerification` (from the link), `resendEmailVerification` (signed in, while `user.emailVerified` is false) | — | — | 422 `EMAIL_VERIFICATION_TOKEN_INVALID`; 401; 429 | Link: anonymous; resend: FARMER or ADMIN |
| Password recovery | `requestPasswordReset` (202 always), `confirmPasswordReset` (from the link; 204) | — | — | 400 field errors; 422 `PASSWORD_RESET_TOKEN_INVALID`; 429 | Anonymous |
| Admin dashboard (English) | `getIngestionHealth`, `listIngestionRuns`, `getIngestionRun`, `listIngestionRunErrors`, `getSystemStatus`, `listAdminAuditEvents` | Empty pages | Banner from `consecutiveFailures > 0`; `marketFreshness` counts | 401; 403 | ADMIN |
| Admin actions | `triggerIngestion` (202, follow `getIngestionRun`), pause and resume, configuration `PUT`s, market, commodity and alias curation | — | — | 400; 404; 409 with `detail`; 422 | ADMIN |

### 2.1 Recommendation wording (ADR-0045)

[ARCHITECTURAL DECISION, ADR-0045, departure D-16.] `netRealizationWinnerId` is the rank-1 option by composite score (ADR-0023 item 2), not necessarily the option with the highest estimated net realization. The labels below are computed only from fields of the response, so each one is true for every response:

| Label (EN; localized copy in role file 02 §10) | Shown on | Computed from |
|---|---|---|
| "Recommended" | The rank-1 option (`options[0]`, the market of `netRealizationWinnerId`) in a comparison | `rank` = 1 |
| "Highest price" | The option whose market is `priceOnlyWinnerId` | `priceOnlyWinnerId` |
| "Highest estimated net realization" | Every option whose `estimates.estimatedNetRealization` equals the maximum over `options[]` (ties: all of them) | `estimates.estimatedNetRealization` |
| Trade-off sentence in the winner banner | Only when `rankingsDisagree` is true | The price winner (P) and the rank-1 option (R): `grossDiff` = P.`estimatedGrossValue` − R.`estimatedGrossValue`; `transportDiff` = P.`estimatedTransportCost` − R.`estimatedTransportCost`; `netDiff` = R.`estimatedNetRealization` − P.`estimatedNetRealization`. Exact decimals; rounded to whole rupees only for display |

- When `netDiff` > 0, the sentence says that R may leave about ₹`netDiff` more after estimated transport, and shows the arithmetic (+₹`grossDiff` from the higher price, −₹`transportDiff` more estimated transport).
- When `netDiff` ≤ 0, it never claims that R leaves more. It says that P has the higher estimated net realization (or the same) and that the ranking also weighs price trend, data freshness and transport, naming P's caution reasons (`NEGATIVE_TREND`, `STALE_DATA`) when present.
- In a simulation or the client-side What-If, ranking is by estimated net realization only (ADR-0026), so rank 1 is labelled "Highest estimated net (what-if)" and never "Recommended".
- The composite score is never shown or recomputed. These labels are presentation only; they do not re-rank anything.

Mandatory content (spec §31, §32, §49) and where its data comes from:

| Content | Data |
|---|---|
| Attribution and report date | `price.attribution` and `price.reportDate` on every price |
| "Latest reported price", never "live" | — |
| "Estimated — based on ₹{appliedRatePerKm}/km assumption. Actual costs will vary." (spec §32, §49; ADR-0038, departure D-14). Never a fixed ₹8/km when the applied rate differs | `transportAssumptions.appliedRatePerKm`; in the client-side What-If, the rate the browser applied |
| "Estimated net realization" | `estimates.estimatedNetRealization` |
| Approximate distance | Every distance; more so when `approximateMarketLocation` is true |
| Standard disclaimer on every price screen | Interface text, localized by the frontend (ADR-0015 item 4) |
| Localized names | `nameMr` / `nameHi` when not null, otherwise `name` (ADR-0015) |

## 3. Client-side What-If (ADR-0026, departure D-13)

The comparison response contains everything the browser needs. No other call is made unless the radius grows beyond `maxDistanceKm`:

```text
inputs : response.options[]    (price.modalPrice, distanceKm, market.name, market.id)
         response.transportAssumptions (minimumTripCharge, vehicleCapacityKg, ratePerKm)
         response.quantity, response.quantityUnit, response.maxDistanceKm
user   : quantityKg in [100, 5000], ratePerKm in [3, 20], maxKm in [25, 300]

if maxKm > response.maxDistanceKm:
    call simulateSellingOptions { recommendationId, maxDistanceKm: maxKm,
                                  plus quantity: quantityKg and transportRatePerKm: ratePerKm only if the user changed them }
    render its response (ranked by NET_REALIZATION; scenario.kind SIMULATION) and use it as the new base
else:
    kept     = options with distanceKm <= maxKm
    excluded = excludedMarkets with distanceKm <= maxKm
    if kept is empty: show the no-recent-data state when excluded is not empty,
                      otherwise the no-market-in-range state (the two server states of ADR-0023 item 3)
    for each kept option (exact decimal arithmetic; rounding as in API_CONVENTIONS.md §4.1):
        trips   = ceil(quantityKg / vehicleCapacityKg)
        perTrip = max(minimumTripCharge, distanceKm × ratePerKm)
        transport = round2(perTrip × trips)
        gross     = round2(modalPrice × quantityKg / 100)
        net       = gross − transport
    rank kept options by net descending, then distanceKm ascending, then market name
    PRICE_ADVANTAGE, LOWER_TRANSPORT = recomputed over the kept options (ADR-0024 item 6)
    netWinner   = rank 1
    priceWinner = highest modalPrice; ties: higher net, shorter distance, market name (ADR-0023)
    rankingsDisagree = priceWinner ≠ netWinner
```

- Label the ranking basis as "net realization" (ADR-0026 item 2).
- Composite scores are never recomputed in the browser (ADR-0026 item 2). Of the reason codes, only `PRICE_ADVANTAGE` and `LOWER_TRANSPORT` depend on the set of options and on transport costs, so the browser re-evaluates those two with ADR-0024 item 6, as the server does in server mode. The others belong to each option and stay as received.
- Send only what the user changed. An omitted value keeps the comparison's value, so a quantity outside the slider range (compare has no such bounds, ADR-0023 item 1) is never sent as an override. How such a slider is displayed is a Task 9 design matter.
- Call `simulateSellingOptions` when the user releases the distance slider, not on every movement: compare and simulate share 60 requests per minute per IP (ADR-0017).
- A server-mode response can contain newer observations than the comparison, because the server selects them again (ADR-0026 item 3). Later client-side changes start from that response.
- The breakeven insight (spec §17) is computed from the same per-option values.
- Overrides are session-only (spec §32): never persisted, never sent anywhere except `simulateSellingOptions`.

## 4. Authentication flow

[ARCHITECTURAL DECISION, ADR-0016, ADR-0017.]

1. `login` or `registerFarmer` returns `AuthResponse`, and the server sets the refresh cookie. Keep `accessToken` in memory only.
2. Send `Authorization: Bearer <accessToken>` only on operations whose `security` lists `bearerAuth`. Other operations ignore the header.
3. On page load, call `refreshSession` (same origin; the browser sends the `__Secure-agrinex_refresh` cookie and `Origin`). A 200 restores the session; `REFRESH_TOKEN_INVALID` means the user is signed out. A 429 is not a sign-out: wait `Retry-After` seconds and retry once (ADR-0042, SR-14).
4. On 401 `UNAUTHORIZED`, call `refreshSession` once, serialized across tabs, then retry the original request. Never retry `refreshSession` itself. If the refresh answers `REFRESH_TOKEN_INVALID`, discard the token: an operation whose `security` also allows anonymous access (`{}`) is retried once without `Authorization`; any other operation leads to sign-in.
5. `logout` answers 204 and clears the cookie. Discard the in-memory token.
6. **Registration and login identifiers** (ADR-0047): `registerFarmer` sends the mobile number and the email address; `login` sends either one as `identifier`, with the same password.
7. **Email links** (ADR-0048). Verification and reset links open frontend pages on the public origin, with the token in the URL fragment (`#token=…`). The page reads the token from the fragment, removes it from the address bar at once (`history.replaceState`), and sends it in the request body; the token never travels in a query string and is never logged or stored. The reset page is served with `Referrer-Policy: no-referrer`.
8. **Verification:** `confirmEmailVerification` answers 204 (verified) or 422 `EMAIL_VERIFICATION_TOKEN_INVALID`. While `user.emailVerified` is false, the account menu offers `resendEmailVerification` (202).
9. **Recovery:** `requestPasswordReset` always answers 202, so the screen always shows the same message and never says whether an account exists. `confirmPasswordReset` answers 204: every session is signed out and the refresh cookie is cleared; the client discards any in-memory token and sends the user to sign in with the new password. A 422 `PASSWORD_RESET_TOKEN_INVALID` offers to request a new link.

## 5. Errors in the UI

- Map `code` to localized messages, and for `VALIDATION_FAILED` map `errors[].field` with `errors[].code` (ADR-0022 item 11). For an unknown code, fall back to the HTTP status.
- Show `correlationId` in support or diagnostic views.
- Never show `detail` to farmers verbatim: it is English developer text. Admin screens may show it.

## 6. Mock mode

`NEXT_PUBLIC_USE_MOCK_API=true` enables MSW handlers backed by `frontend/__mocks__/api/*.json` (spec §44). Each file is an envelope:

| Member | Meaning |
|---|---|
| `synthetic` | Always `true` |
| `notice` | Standard SYNTHETIC warning |
| `operationId` | The contract operation |
| `scenario` | Description of the case |
| `context` | Optional: the `today`, time zone and freshness thresholds the synthetic values assume |
| `request` | `method`, `path` (with query), optional `headers` and `body` |
| `response` | `status`, `headers` (including `Content-Type` and `X-Correlation-Id`), `body` |

- A handler returns `response.body` with `response.status` and `response.headers`.
- Every file is validated against the contract operation and status it declares: schema, headers and request, plus arithmetic and consistency (see [CONTRACT_VALIDATION.md](CONTRACT_VALIDATION.md)).
- All values are invented (`TEST Market …`, `Test District …`, placeholder accounts and tokens) and must never be shown as real data. Screens running on mocks are prototypes and must be labelled as such (CLAUDE.md §M).
- The file list and the case each file covers are in `frontend/__mocks__/api/README.md`.
