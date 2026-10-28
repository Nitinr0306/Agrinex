# AgriNex API mocks (synthetic)

**Status:** REFERENCE. These files are synthetic examples that conform to the authoritative contract in `contracts/`. They are not a source of requirements (SSOT index area 4).

> **SYNTHETIC TEST DATA.** Every market, price, date, forecast, account, token and phone number in these files is invented, for frontend mock mode and contract tests only. None of it is government data, and none of it may be shown as real (spec P10, §31 RULE 5; ADR-0028). Screens rendered from mocks are prototypes and must be labelled as such (CLAUDE.md §M).

## Use

`NEXT_PUBLIC_USE_MOCK_API=true` enables MSW handlers backed by these files (spec §44). Each file is an envelope:

| Member | Meaning |
|---|---|
| `synthetic` | Always `true` |
| `notice` | The SYNTHETIC warning |
| `operationId` | Operation in `contracts/openapi.yaml` |
| `scenario` | The case the file shows |
| `context` | Optional. The `today` (2026-10-03, Asia/Kolkata) and the freshness thresholds (1, 4 and 10 days) that the synthetic values assume |
| `request` | `method`, `path` (with query), and optional `headers` and `body` |
| `response` | `status`, `headers` (including `Content-Type` and `X-Correlation-Id`), and `body` |

A handler matches `request.method` and the path template, then returns `response.body` with `response.status` and `response.headers`. A response without a body (204) has `body: null`.

## Rules

- **Exactness.** Every body must match `contracts/openapi.yaml` exactly for the declared operation and status, including headers, decimal places and formats. "A mock with wrong field names is a bug" (spec §44). The synthetic numbers follow the contract's formulas (`docs/api/API_CONVENTIONS.md` §4.1): estimates, ranks, reason codes, winners, freshness and the trend statistic are consistent.
- **One data set.** The files describe one synthetic data set: the same `today`, origin, markets, coordinates and observations. Two kinds of file depart from it on purpose and say so in their case: scenarios with an alternative source state (the latest current-dataset run failed, or none has ever succeeded), and admin actions, which show the state after the change. Distances are haversine distances between the synthetic coordinates with the approved Earth radius of ADR-0036 (6 371.0088 km). Every comparison origin is a district centroid from the districts file (ADR-0039).
- **Change control.** These files change in the same change as the contract (spec §41 step 4; ADR-0022 item 2). Contract tests validate every file against the contract (ADR-0022 item 1). The validation done in Task 8 is described in `docs/api/CONTRACT_VALIDATION.md`.
- **Naming.** Synthetic names: markets `TEST Market A` to `G` plus one merged alias twin, districts `Test District North` and `South`, localities `Test Taluka …`. Commodity, variety and grade names are real vocabulary from the official commodity master. Localized names are `null` because no curated translation exists yet (ADR-0015).
- **Placeholders.** Identifiers use the synthetic range `00000000-0000-4000-8000-…`. Tokens are placeholders (`mock.farmer.token`; reset and verification tokens start with `MOCK`), phone numbers are placeholders that must never be dialled, and email addresses use the reserved `.invalid` domain (RFC 2606), so no message can ever be delivered.

## Files

| File | Operation | Request | Status | Case |
|---|---|---|---|---|
| `admin-audit-events.200.json` | `listAdminAuditEvents` | GET `/api/v1/admin/audit-events` | 200 | Audited admin actions with masked before and after details (ADR-0020), including a deactivation by the operator procedure (ADR-0041), which no HTTP operation performs. |
| `admin-commodities.200.json` | `listAdminCommodities` | GET `/api/v1/admin/commodities` | 200 | The known list with unit evidence; the second commodity has no verified unit yet, so ingestion rejects its records (UNIT_UNVERIFIED). |
| `admin-commodity-add.201.json` | `addCommodity` | POST `/api/v1/admin/commodities` | 201 | Adding a commodity with an official source reference and its verified unit (ADR-0006, ADR-0008). |
| `admin-commodity-names.200.json` | `updateCommodityNames` | PUT `/api/v1/admin/commodities/00000000-0000-4000-8000-000000000406/names` | 200 | Setting localized names with a provenance note (ADR-0015). The strings are placeholders meaning "test name", not translations. |
| `admin-commodity-price-unit.200.json` | `verifyCommodityPriceUnit` | PUT `/api/v1/admin/commodities/00000000-0000-4000-8000-000000000405/price-unit` | 200 | Recording the verified unit of a commodity that had none (ADR-0008 item 1). |
| `admin-config-transport-update.200.json` | `updateTransportAssumptions` | PUT `/api/v1/admin/config/transport-assumptions` | 200 | A new transport-assumptions version becomes active; earlier versions stay unchanged. |
| `admin-config-transport-versions.200.json` | `listTransportAssumptionVersions` | GET `/api/v1/admin/config/transport-assumptions/versions` | 200 | Every version, newest first. |
| `admin-config-transport.200.json` | `getTransportAssumptions` | GET `/api/v1/admin/config/transport-assumptions` | 200 | The active transport assumptions (spec §32 defaults). |
| `admin-config-weights-update.200.json` | `updateRecommendationWeights` | PUT `/api/v1/admin/config/recommendation-weights` | 200 | A new weights version becomes active (values synthetic). |
| `admin-config-weights-versions.200.json` | `listRecommendationWeightVersions` | GET `/api/v1/admin/config/recommendation-weights/versions` | 200 | Every version, newest first. |
| `admin-config-weights.200.json` | `getRecommendationWeights` | GET `/api/v1/admin/config/recommendation-weights` | 200 | The active weights (ADR-0024 defaults). |
| `admin-district-alias-add.201.json` | `addDistrictAlias` | POST `/api/v1/admin/district-aliases` | 201 | Adding a district alias with a mandatory evidence note. |
| `admin-district-aliases.200.json` | `listDistrictAliases` | GET `/api/v1/admin/district-aliases` | 200 | A former district spelling mapped to its current district with evidence (ADR-0014). |
| `admin-ingestion-health.200.json` | `getIngestionHealth` | GET `/api/v1/admin/ingestion/health` | 200 | The current dataset synced; the historical run failed once (admin banner from consecutiveFailures). |
| `admin-ingestion-pause.200.json` | `pauseScheduledIngestion` | POST `/api/v1/admin/ingestion/datasets/CURRENT_PRICE/pause` | 200 | Pausing scheduled CURRENT_PRICE runs during announced source maintenance; manual triggers still work (ADR-0020). |
| `admin-ingestion-resume.200.json` | `resumeScheduledIngestion` | POST `/api/v1/admin/ingestion/datasets/CURRENT_PRICE/resume` | 200 | Resuming scheduled CURRENT_PRICE runs. |
| `admin-ingestion-run-errors.200.json` | `listIngestionRunErrors` | GET `/api/v1/admin/ingestion/runs/00000000-0000-4000-8000-000000000f01/errors` | 200 | The first page (size 2) of the run's 23 error rows: a validation rejection and a same-key conflict, with raw-payload excerpts. |
| `admin-ingestion-run.200.json` | `getIngestionRun` | GET `/api/v1/admin/ingestion/runs/00000000-0000-4000-8000-000000000f03` | 200 | A triggered historical backfill still RUNNING: counters so far, no end time, completeness not yet applicable (ADR-0012). |
| `admin-ingestion-runs.200.json` | `listIngestionRuns` | GET `/api/v1/admin/ingestion/runs` | 200 | Run history, newest first, with every counter. |
| `admin-ingestion-trigger.202.json` | `triggerIngestion` | POST `/api/v1/admin/ingestion/trigger` | 202 | A historical backfill of four report dates after an outage (ADR-0011 item 3). |
| `admin-ingestion-trigger.409-conflict.json` | `triggerIngestion` | POST `/api/v1/admin/ingestion/trigger` | 409 | A run of the same dataset is still RUNNING (ADR-0011 item 4). |
| `admin-market-coordinates.200.json` | `setMarketCoordinates` | PUT `/api/v1/admin/markets/00000000-0000-4000-8000-000000000704/coordinates` | 200 | Exact coordinates from an identified source replace the district centroid and clear the approximate flag (ADR-0014). |
| `admin-market-deactivate.200.json` | `deactivateMarket` | POST `/api/v1/admin/markets/00000000-0000-4000-8000-000000000705/deactivate` | 200 | Withdrawing a market from comparisons; its observations stay stored. |
| `admin-market-merge.200.json` | `mergeMarket` | POST `/api/v1/admin/markets/00000000-0000-4000-8000-000000000708/merge` | 200 | An alias twin is merged into TEST Market B with an evidence note; it becomes MERGED and inactive (ADR-0014). |
| `admin-market-reactivate.200.json` | `reactivateMarket` | POST `/api/v1/admin/markets/00000000-0000-4000-8000-000000000705/reactivate` | 200 | Returning the market to comparisons. |
| `admin-market-verify.200.json` | `verifyMarket` | POST `/api/v1/admin/markets/00000000-0000-4000-8000-0000000007ff/verify` | 200 | Verification makes the market a comparison and alert candidate (ADR-0014). |
| `admin-market.200.json` | `getAdminMarket` | GET `/api/v1/admin/markets/00000000-0000-4000-8000-0000000007ff` | 200 | A PROVISIONAL market with the source spelling that registered it (note the trailing space kept in the raw value). |
| `admin-markets.200.json` | `listAdminMarkets` | GET `/api/v1/admin/markets` | 200 | A PROVISIONAL market registered from the feed awaits verification (ADR-0014). |
| `admin-system-status.200.json` | `getSystemStatus` | GET `/api/v1/admin/system/status` | 200 | Read-only status: Redis is down, so rate limiting and caching run in-process (ADR-0018); the API key value is never shown. |
| `admin-system-status.403-forbidden.json` | `getSystemStatus` | GET `/api/v1/admin/system/status` | 403 | A FARMER token on an ADMIN operation (spec §28). |
| `alerts-create.201.json` | `createAlert` | POST `/api/v1/alerts` | 201 | A price alert for one market. |
| `alerts-create.422-unsupported-price-unit.json` | `createAlert` | POST `/api/v1/alerts` | 422 | Alerts accept only commodities priced in Rs./Quintal (ADR-0008). |
| `alerts-delete.204.json` | `deleteAlert` | DELETE `/api/v1/alerts/00000000-0000-4000-8000-000000000c02` | 204 | Deleting an alert also deletes its events (spec §34). |
| `alerts-events-read.204.json` | `markAlertEventRead` | POST `/api/v1/alerts/events/00000000-0000-4000-8000-000000000d01/read` | 204 | Marking an event read; the alert stays ACTIVE (spec §34). |
| `alerts-events.200.json` | `listAlertEvents` | GET `/api/v1/alerts/events` | 200 | Polling unread events; totalItems is the unread count. |
| `alerts-list.200.json` | `listAlerts` | GET `/api/v1/alerts` | 200 | The caller's alerts, most recently triggered first. |
| `alerts-list.401-unauthorized.json` | `listAlerts` | GET `/api/v1/alerts` | 401 | Expired access token: the client refreshes once and retries (spec §29). |
| `alerts-update.200.json` | `updateAlertStatus` | PATCH `/api/v1/alerts/00000000-0000-4000-8000-000000000c01` | 200 | Pausing an alert (spec §34 "Farmer deactivates"). |
| `auth-email-verification-confirm.204.json` | `confirmEmailVerification` | POST `/api/v1/auth/email-verification/confirm` | 204 | Opening the verification link confirms the account's email; no sign-in is needed (ADR-0048). |
| `auth-email-verification-confirm.422-token-invalid.json` | `confirmEmailVerification` | POST `/api/v1/auth/email-verification/confirm` | 422 | The verification link was already used, has expired, or was replaced (ADR-0048). |
| `auth-email-verification-resend.202.json` | `resendEmailVerification` | POST `/api/v1/auth/email-verification/resend` | 202 | A signed-in farmer with an unverified email asks for a new link; earlier links stop working (ADR-0048). |
| `auth-login.200-email-identifier.json` | `login` | POST `/api/v1/auth/login` | 200 | The same farmer signs in with the email address instead of the mobile number: one account, one password (ADR-0047). |
| `auth-login.200.json` | `login` | POST `/api/v1/auth/login` | 200 | A farmer signs in with the mobile number; the account's email was verified earlier. |
| `auth-login.401-invalid-credentials.json` | `login` | POST `/api/v1/auth/login` | 401 | Wrong password; the response does not reveal whether the account exists. |
| `auth-logout.204.json` | `logout` | POST `/api/v1/auth/logout` | 204 | Logout revokes the token family and clears the cookie. |
| `auth-password-reset-confirm.204.json` | `confirmPasswordReset` | POST `/api/v1/auth/password-reset/confirm` | 204 | A new password set with the reset link's token: every session is signed out and the refresh cookie is cleared; the user then signs in (ADR-0048). |
| `auth-password-reset-confirm.422-token-invalid.json` | `confirmPasswordReset` | POST `/api/v1/auth/password-reset/confirm` | 422 | The reset link was already used, has expired, or was replaced; the client offers a new link (ADR-0048). |
| `auth-password-reset-request.202.json` | `requestPasswordReset` | POST `/api/v1/auth/password-reset/request` | 202 | Asking for a reset link: the same 202 whether or not an account exists; a link goes only to a verified email (ADR-0048). |
| `auth-refresh.200.json` | `refreshSession` | POST `/api/v1/auth/refresh` | 200 | Rotation: a new access token and a new refresh cookie. |
| `auth-refresh.401-refresh-token-invalid.json` | `refreshSession` | POST `/api/v1/auth/refresh` | 401 | The refresh token is expired, revoked or reused; the client must sign in again and never retry. |
| `auth-register.201.json` | `registerFarmer` | POST `/api/v1/auth/register` | 201 | Quick registration with a mobile number and an email signs the new farmer in; the email is not verified yet, so a verification link is sent (ADR-0047, ADR-0048). |
| `auth-register.409-email-already-registered.json` | `registerFarmer` | POST `/api/v1/auth/register` | 409 | The email address already belongs to another account; 409 names the field (ADR-0047; SR-01 extended by ADR-0048). |
| `commodities.200.json` | `listCommodities` | GET `/api/v1/commodities` | 200 | Commodities with a recent observation at a VERIFIED, active market, by name; localized names not yet curated (null, English fallback). |
| `commodity-grades.200.json` | `listGrades` | GET `/api/v1/commodities/00000000-0000-4000-8000-000000000401/grades` | 200 | Grades exactly as the source reports them. |
| `commodity-varieties.200.json` | `listVarieties` | GET `/api/v1/commodities/00000000-0000-4000-8000-000000000401/varieties` | 200 | Varieties exactly as the source reports them. |
| `commodity-varieties.404.json` | `listVarieties` | GET `/api/v1/commodities/00000000-0000-4000-8000-0000000004ff/varieties` | 404 | Unknown commodity id in the path. |
| `geography-districts.200.json` | `listDistricts` | GET `/api/v1/geography/states/00000000-0000-4000-8000-000000000101/districts` | 200 | Districts with synthetic centroids (real centroids come from Survey of India boundaries, ADR-0014). |
| `geography-localities.200.json` | `listLocalities` | GET `/api/v1/geography/districts/00000000-0000-4000-8000-000000000201/localities` | 200 | Localities carry no coordinates in Phase 1. |
| `geography-states.200.json` | `listStates` | GET `/api/v1/geography/states` | 200 | Phase 1 coverage: one state. |
| `markets-detail.200-current-never-synced.json` | `getMarketDetail` | GET `/api/v1/markets/00000000-0000-4000-8000-000000000701` | 200 | Every CURRENT_PRICE run has failed so far, so lastSuccessfulSyncAt is null and the banner shows; the price comes from the historical bootstrap, with its true date (ADR-0005, ADR-0010). |
| `markets-detail.200-no-recent-data.json` | `getMarketDetail` | GET `/api/v1/markets/00000000-0000-4000-8000-000000000705` | 200 | No observation of the requested commodity within the stale window: empty latestPrices with noDataReason; nothing is fabricated. |
| `markets-detail.200.json` | `getMarketDetail` | GET `/api/v1/markets/00000000-0000-4000-8000-000000000702` | 200 | Latest reported prices per commodity at one VERIFIED market, each chosen by ADR-0010 with full provenance. |
| `markets-detail.404.json` | `getMarketDetail` | GET `/api/v1/markets/00000000-0000-4000-8000-0000000007ff` | 404 | A PROVISIONAL market is not visible on public operations (ADR-0014). |
| `markets-detail.500-internal-error.json` | `getMarketDetail` | GET `/api/v1/markets/00000000-0000-4000-8000-000000000702` | 500 | An unexpected server fault: no detail is ever given (spec §28). |
| `markets-forecast.200-available.json` | `getMarketForecast` | GET `/api/v1/markets/00000000-0000-4000-8000-000000000702/forecast` | 200 | OPTIONAL ML track: an invented forecast from a placeholder model. It is a model prediction, never a reported price, and never affects comparison or alerts (ADR-0030). Metrics and bounds are as an ML job would write them. |
| `markets-forecast.200-unavailable.json` | `getMarketForecast` | GET `/api/v1/markets/00000000-0000-4000-8000-000000000703/forecast` | 200 | No forecast dated today or later (ML not built, stopped or failed): available false and no prediction. Never a 5xx (spec §29, §35). |
| `markets-trends.200-insufficient-data.json` | `getMarketTrend` | GET `/api/v1/markets/00000000-0000-4000-8000-000000000703/trends` | 200 | Only 2 daily values in the last 14 days: INSUFFICIENT_DATA and a null change; the UI shows "Insufficient historical data" (spec §33). |
| `markets-trends.200.json` | `getMarketTrend` | GET `/api/v1/markets/00000000-0000-4000-8000-000000000702/trends` | 200 | 30-day chart; the 7-day mean (3101.85) is 8.5 % below the previous 7-day mean (3390.00), so FALLING (spec §33; ADR-0025). Dates without data are absent, not interpolated. |
| `selling-options-compare.200-no-recent-data.json` | `compareSellingOptions` | POST `/api/v1/selling-options/compare` | 200 | Markets are in range but none has a recent observation: 200 with empty options and noDataReason (spec §29; ADR-0023). maxDistanceKm was omitted, so the default 150 applies. Cabbage is still listed because its only recent observation is at TEST Market F, 180 km away; every market inside the radius is listed as excluded. |
| `selling-options-compare.200-recommendation.json` | `compareSellingOptions` | POST `/api/v1/selling-options/compare` | 200 | Hero flow: the highest price is at TEST Market B, but TEST Market C ranks first on the composite score, so rankingsDisagree is true. Two markets in range are excluded for lack of recent data. TEST Market A's observation is STALE but inside the 7-day recent window, so its trend is defined. |
| `selling-options-compare.200-stale-sync-failed.json` | `compareSellingOptions` | POST `/api/v1/selling-options/compare` | 200 | Source outage: the latest current-dataset run FAILED, so latestSyncFailed is true and the UI shows the spec §29 banner. Stored observations are served with their true report dates and STALE freshness; nothing is carried forward (ADR-0005). One price comes from the historical dataset (ADR-0010 item 1). TEST Market A's observation is older than the 7-day recent window, so its trend is INSUFFICIENT_DATA (ADR-0025). |
| `selling-options-compare.400-origin-not-centroid.json` | `compareSellingOptions` | POST `/api/v1/selling-options/compare` | 400 | The origin is not the centroid of any district (for example a device location): rejected, and nothing is recorded (spec §28; ADR-0039). |
| `selling-options-compare.400-validation-failed.json` | `compareSellingOptions` | POST `/api/v1/selling-options/compare` | 400 | Field-level validation errors; the frontend localizes them by code and field. |
| `selling-options-compare.401-account-inactive.json` | `compareSellingOptions` | POST `/api/v1/selling-options/compare` | 401 | A still-valid token of an account deactivated or erased within its 15-minute lifetime: the account is resolved and the request answers 401; the client refreshes (refused) and retries without the token (ADR-0042, SR-04). |
| `selling-options-compare.401-unauthorized.json` | `compareSellingOptions` | POST `/api/v1/selling-options/compare` | 401 | An expired access token on an operation that lists bearerAuth: the client refreshes once and retries, or retries without the token after REFRESH_TOKEN_INVALID. |
| `selling-options-compare.422-estimate-out-of-range.json` | `compareSellingOptions` | POST `/api/v1/selling-options/compare` | 422 | A schema-valid but extreme quantity whose estimates would exceed the stored precision: 422, nothing recorded, and no quantity limit (ADR-0023 item 1; ADR-0040). |
| `selling-options-compare.422-no-markets-in-range.json` | `compareSellingOptions` | POST `/api/v1/selling-options/compare` | 422 | No VERIFIED, active market within 10 km (spec §16; ADR-0023). The UI offers to increase the distance (spec §25). |
| `selling-options-compare.422-unsupported-price-unit.json` | `compareSellingOptions` | POST `/api/v1/selling-options/compare` | 422 | The commodity's verified unit is not Rs./Quintal, so no per-quintal calculation is possible (ADR-0008). |
| `selling-options-compare.429-rate-limited.json` | `compareSellingOptions` | POST `/api/v1/selling-options/compare` | 429 | More than 60 compare or simulate requests in one minute from this IP (spec §28; ADR-0017). |
| `selling-options-compare.503-service-unavailable.json` | `compareSellingOptions` | POST `/api/v1/selling-options/compare` | 503 | The database is unavailable: the backend fails fast with 503 and the UI shows the standard error page (spec §29). |
| `selling-options-simulate.200-quantity-override.json` | `simulateSellingOptions` | POST `/api/v1/selling-options/simulate` | 200 | What-If server mode on a comparison made in QUINTAL: only the changed quantity is sent (150 kg), so the response is in KG; the rate stays CONFIGURED. |
| `selling-options-simulate.200.json` | `simulateSellingOptions` | POST `/api/v1/selling-options/simulate` | 200 | What-If server mode: the radius grows from 150 to 200 km and the rate is overridden to 12 per km. Ranked by estimated net realization; nothing is persisted; observations are selected again at simulation time and never altered by the overrides (ADR-0026). |
| `selling-options-simulate.422-reference-not-found.json` | `simulateSellingOptions` | POST `/api/v1/selling-options/simulate` | 422 | The recommendationId names no recorded comparison. |
