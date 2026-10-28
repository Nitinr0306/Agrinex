# AgriNex — API Contract Index

**Status:** AUTHORITATIVE, the hub of the API contract area (SSOT index area 4). Created by Task 8 on 2026-10-04, contract version 1.0.0, and revised before its first merge by the decisions of Task 8.6 (ADR-0035 to ADR-0042) and of the Task 9 final decision lock (ADR-0045 to ADR-0048).

- The machine-readable contract governs: `contracts/openapi.yaml` and the JSON Schemas in `contracts/`.
- The documents in `docs/api/` explain it, and are corrected wherever they differ from it.
- Product requirements come from the master specification. Architecture comes from the binding decision-log entries and `docs/architecture/`. Data comes from `docs/database/`. The contract invents none of them (CLAUDE.md §C, §H).

## 1. Contract files

| File | Content | Spec §41 producer → consumers | Status |
|---|---|---|---|
| `contracts/openapi.yaml` | OpenAPI 3.1 contract (version 1.1.0): 54 operations under `/api/v1`, parameters, headers, responses, security, component schemas | Backend → Frontend, QA | AUTHORITATIVE |
| `contracts/common.schema.json` | Shared primitives: UUID, date, instant, amounts, coordinates, reference objects, page metadata | Backend → all | AUTHORITATIVE |
| `contracts/price-observation.schema.json` | `PriceObservation` / `PriceQuote` with provenance and freshness; `SourceSyncStatus`; `TrendDirection`; the ML input `DailyPriceSeries` | Data Engineering → Backend, ML, QA | AUTHORITATIVE |
| `contracts/recommendation.schema.json` | `MarketComparisonResponse`, `CompareRequest`, `SimulateRequest`, options, estimates, reason codes, scenario, decision basis | Backend → Frontend, QA | AUTHORITATIVE |
| `contracts/error-response.schema.json` | `ApiErrorResponse` (RFC 9457) with the code catalogue | Backend → Frontend, QA | AUTHORITATIVE |
| `contracts/forecast-output.schema.json` | Optional ML output written to `forecasts` (one generation) | ML → Backend | AUTHORITATIVE (optional track) |
| `frontend/__mocks__/api/*.json` | Synthetic response mocks in an envelope, one per operation and case; index in `README.md` there | Backend (initial) → Frontend | REFERENCE (synthetic; must match the contract exactly, spec §44) |

Documentation in `docs/api/` (AUTHORITATIVE, explanatory):

| Document | Purpose |
|---|---|
| `API_INDEX.md` | This hub: files, inventory, ownership, scope, open items |
| [API_CONVENTIONS.md](API_CONVENTIONS.md) | Versioning and compatibility, formats and precision, calculation rounding, pagination, the error catalogue, security, headers, contract-design choices |
| [PROVENANCE_AND_FRESHNESS.md](PROVENANCE_AND_FRESHNESS.md) | Price provenance, units, freshness, selection, source status, and the data.gov.in outage and no-data semantics |
| [ML_CONTRACT_BOUNDARY.md](ML_CONTRACT_BOUNDARY.md) | The database-mediated ML boundary, input and output contracts, the forecast operation, deterministic fallback |
| [FRONTEND_CONSUMPTION.md](FRONTEND_CONSUMPTION.md) | Screens, operations and states; the client-side What-If algorithm; auth flow; mock mode |
| [CONTRACT_VALIDATION.md](CONTRACT_VALIDATION.md) | What was validated, how, and the results; expectations for contract tests |

Generated material: none exists. Any OpenAPI document generated from code is a GENERATED drift-detection artifact. It never overwrites `contracts/openapi.yaml` (ADR-0022; departure D-11).

## 2. Reading order for implementers

1. CLAUDE.md, then `docs/00_SSOT_INDEX.md` area 4.
2. [API_CONVENTIONS.md](API_CONVENTIONS.md).
3. `contracts/openapi.yaml`, with the operations of the module being built.
4. The schema files it references.
5. [PROVENANCE_AND_FRESHNESS.md](PROVENANCE_AND_FRESHNESS.md) before any price-serving code.
6. [FRONTEND_CONSUMPTION.md](FRONTEND_CONSUMPTION.md) (frontend) or [ML_CONTRACT_BOUNDARY.md](ML_CONTRACT_BOUNDARY.md) (ML track).
7. The open items in §8, and `docs/REQUIRES_DECISION.md`.

## 3. Endpoint inventory

Generated from `contracts/openapi.yaml` (54 operations: auth 8, geography 3, commodity 3, price 3, recommendation 2, alert 6, admin 29). Version 1.1.0 (2026-10-18) added `GET /api/v1/admin/ingestion/historical-coverage` (ADR-0054), an additive change. Module ownership follows spec §30 (ADR-0022 item 3). Every operation also has a `default` error response, the documented 4xx responses, and `X-Correlation-Id`.

| # | Module | Method and path | operationId | Roles | Phase | Success | Basis |
|---|---|---|---|---|---|---|---|
| 1 | auth | POST `/api/v1/auth/register` | `registerFarmer` | anonymous | 1 | 201 | spec §10, spec §25, spec §28, ADR-0016, ADR-0017, ADR-0047, ADR-0048 |
| 2 | auth | POST `/api/v1/auth/login` | `login` | anonymous | 1 | 200 | spec §10, spec §28, ADR-0016, ADR-0017, ADR-0047 |
| 3 | auth | POST `/api/v1/auth/refresh` | `refreshSession` | anonymous | 1 | 200 | spec §10, spec §27, spec §28, spec §29, ADR-0016, ADR-0017 |
| 4 | auth | POST `/api/v1/auth/logout` | `logout` | anonymous | 1 | 204 | spec §10, ADR-0016, ADR-0017 |
| 5 | auth | POST `/api/v1/auth/password-reset/request` | `requestPasswordReset` | anonymous | 1 | 202 | spec §28, spec §39, ADR-0016, ADR-0017, ADR-0047, ADR-0048 |
| 6 | auth | POST `/api/v1/auth/password-reset/confirm` | `confirmPasswordReset` | anonymous | 1 | 204 | spec §28, ADR-0016, ADR-0017, ADR-0047, ADR-0048 |
| 7 | auth | POST `/api/v1/auth/email-verification/confirm` | `confirmEmailVerification` | anonymous | 1 | 204 | spec §28, ADR-0047, ADR-0048 |
| 8 | auth | POST `/api/v1/auth/email-verification/resend` | `resendEmailVerification` | FARMER, ADMIN | 1 | 202 | spec §28, ADR-0047, ADR-0048 |
| 9 | geography | GET `/api/v1/geography/states` | `listStates` | anonymous | 1 | 200 | spec F1.3, spec §30, ADR-0006, ADR-0015, ADR-0022 |
| 10 | geography | GET `/api/v1/geography/states/{stateId}/districts` | `listDistricts` | anonymous | 1 | 200 | spec F1.3, spec FR-06, spec §21, spec §30, ADR-0014 |
| 11 | geography | GET `/api/v1/geography/districts/{districtId}/localities` | `listLocalities` | anonymous | 1 | 200 | spec F1.3, spec §25, spec §30, ADR-0014 |
| 12 | commodity | GET `/api/v1/commodities` | `listCommodities` | anonymous | 1 | 200 | spec F1.4, spec §25, spec §30, ADR-0006, ADR-0008, ADR-0015, ADR-0035 |
| 13 | commodity | GET `/api/v1/commodities/{commodityId}/varieties` | `listVarieties` | anonymous | 1 | 200 | spec F1.4, spec §30, ADR-0006 |
| 14 | commodity | GET `/api/v1/commodities/{commodityId}/grades` | `listGrades` | anonymous | 1 | 200 | spec F1.4, spec §20, ADR-0006 |
| 15 | price | GET `/api/v1/markets/{marketId}` | `getMarketDetail` | anonymous | 1 | 200 | spec §10, spec §29, spec §30, spec §31, ADR-0005, ADR-0010, ADR-0014, ADR-0022 |
| 16 | price | GET `/api/v1/markets/{marketId}/trends` | `getMarketTrend` | anonymous | 1 | 200 | spec F1.15, spec FR-14, spec §21, spec §33, ADR-0010, ADR-0025 |
| 17 | price | GET `/api/v1/markets/{marketId}/forecast` | `getMarketForecast` | anonymous | optional ML | 200 | spec §29, spec §35, ADR-0029, ADR-0030 |
| 18 | recommendation | POST `/api/v1/selling-options/compare` | `compareSellingOptions` | anonymous | 1 | 200 | spec §16, spec §21, spec §28, spec §29, spec §30, spec §32, ADR-0010, ADR-0022, ADR-0023, ADR-0024, ADR-0025, ADR-0026, ADR-0039, ADR-0040, ADR-0042 |
| 19 | recommendation | POST `/api/v1/selling-options/simulate` | `simulateSellingOptions` | anonymous | 1 | 200 | spec §10, spec §17, spec §30, spec §32, ADR-0026 |
| 20 | alert | GET `/api/v1/alerts` | `listAlerts` | FARMER, ADMIN | 1 | 200 | spec §28, spec §30, spec §34, ADR-0016, ADR-0019, ADR-0022 |
| 21 | alert | POST `/api/v1/alerts` | `createAlert` | FARMER, ADMIN | 1 | 201 | spec §25, spec §28, spec §34, ADR-0008, ADR-0014, ADR-0016, ADR-0019 |
| 22 | alert | PATCH `/api/v1/alerts/{alertId}` | `updateAlertStatus` | FARMER, ADMIN | 1 | 200 | spec §30, spec §34 |
| 23 | alert | DELETE `/api/v1/alerts/{alertId}` | `deleteAlert` | FARMER, ADMIN | 1 | 204 | spec §30, spec §34, ADR-0029 |
| 24 | alert | GET `/api/v1/alerts/events` | `listAlertEvents` | FARMER, ADMIN | 1 | 200 | spec §34, ADR-0019, ADR-0022, ADR-0029 |
| 25 | alert | POST `/api/v1/alerts/events/{eventId}/read` | `markAlertEventRead` | FARMER, ADMIN | 1 | 204 | spec §34, ADR-0019 |
| 26 | admin | GET `/api/v1/admin/system/status` | `getSystemStatus` | ADMIN | 1 | 200 | spec §27, spec §28, ADR-0011, ADR-0018, ADR-0020 |
| 27 | admin | GET `/api/v1/admin/ingestion/health` | `getIngestionHealth` | ADMIN | 1 | 200 | spec F1.20, spec FR-18, spec §30, spec §46, spec §48, ADR-0011, ADR-0012, ADR-0019, ADR-0020 |
| 28 | admin | GET `/api/v1/admin/ingestion/runs` | `listIngestionRuns` | ADMIN | 1 | 200 | spec FR-17, spec FR-18, spec §30, spec §46, ADR-0011, ADR-0012 |
| 29 | admin | GET `/api/v1/admin/ingestion/runs/{runId}` | `getIngestionRun` | ADMIN | 1 | 200 | spec FR-18, spec §30, ADR-0011, ADR-0012 |
| 30 | admin | GET `/api/v1/admin/ingestion/runs/{runId}/errors` | `listIngestionRunErrors` | ADMIN | 1 | 200 | spec FR-18, spec §15, spec §37, ADR-0008, ADR-0009, ADR-0011, ADR-0020, ADR-0029 |
| 31 | admin | POST `/api/v1/admin/ingestion/trigger` | `triggerIngestion` | ADMIN | 1 | 202 | spec §15, spec §30, spec §45, ADR-0006, ADR-0011, ADR-0020; audit `INGESTION_TRIGGERED` |
| 32 | admin | POST `/api/v1/admin/ingestion/datasets/{datasetType}/pause` | `pauseScheduledIngestion` | ADMIN | 1 | 200 | ADR-0011, ADR-0020; audit `INGESTION_PAUSED` |
| 33 | admin | POST `/api/v1/admin/ingestion/datasets/{datasetType}/resume` | `resumeScheduledIngestion` | ADMIN | 1 | 200 | ADR-0011, ADR-0020; audit `INGESTION_RESUMED` |
| 34 | admin | GET `/api/v1/admin/config/transport-assumptions` | `getTransportAssumptions` | ADMIN | 1 | 200 | spec §30, spec §32, ADR-0020 |
| 35 | admin | PUT `/api/v1/admin/config/transport-assumptions` | `updateTransportAssumptions` | ADMIN | 1 | 200 | spec §30, spec §32, UC19, ADR-0020; audit `TRANSPORT_ASSUMPTIONS_UPDATED` |
| 36 | admin | GET `/api/v1/admin/config/transport-assumptions/versions` | `listTransportAssumptionVersions` | ADMIN | 1 | 200 | ADR-0020 |
| 37 | admin | GET `/api/v1/admin/config/recommendation-weights` | `getRecommendationWeights` | ADMIN | 1 | 200 | spec §16, spec §30, ADR-0020, ADR-0024 |
| 38 | admin | PUT `/api/v1/admin/config/recommendation-weights` | `updateRecommendationWeights` | ADMIN | 1 | 200 | spec §16, spec §30, UC20, ADR-0020, ADR-0024; audit `RECOMMENDATION_WEIGHTS_UPDATED` |
| 39 | admin | GET `/api/v1/admin/config/recommendation-weights/versions` | `listRecommendationWeightVersions` | ADMIN | 1 | 200 | ADR-0020 |
| 40 | admin | GET `/api/v1/admin/markets` | `listAdminMarkets` | ADMIN | 1 | 200 | ADR-0014, ADR-0020 |
| 41 | admin | GET `/api/v1/admin/markets/{marketId}` | `getAdminMarket` | ADMIN | 1 | 200 | spec §47 Q4, ADR-0014, ADR-0020 |
| 42 | admin | POST `/api/v1/admin/markets/{marketId}/verify` | `verifyMarket` | ADMIN | 1 | 200 | ADR-0014, ADR-0020; audit `MARKET_VERIFIED` |
| 43 | admin | POST `/api/v1/admin/markets/{marketId}/merge` | `mergeMarket` | ADMIN | 1 | 200 | spec §47 Q4, ADR-0009, ADR-0014, ADR-0020; audit `MARKET_MERGED` |
| 44 | admin | POST `/api/v1/admin/markets/{marketId}/deactivate` | `deactivateMarket` | ADMIN | 1 | 200 | spec §16, spec §20, ADR-0014, ADR-0020; audit `MARKET_DEACTIVATED` |
| 45 | admin | POST `/api/v1/admin/markets/{marketId}/reactivate` | `reactivateMarket` | ADMIN | 1 | 200 | spec §16, ADR-0020; audit `MARKET_REACTIVATED` |
| 46 | admin | PUT `/api/v1/admin/markets/{marketId}/coordinates` | `setMarketCoordinates` | ADMIN | 1 | 200 | spec §16, ADR-0014, ADR-0020; audit `MARKET_COORDINATES_SET` |
| 47 | admin | GET `/api/v1/admin/commodities` | `listAdminCommodities` | ADMIN | 1 | 200 | ADR-0006, ADR-0008, ADR-0020 |
| 48 | admin | POST `/api/v1/admin/commodities` | `addCommodity` | ADMIN | 1 | 201 | spec §14, ADR-0006, ADR-0008, ADR-0020; audit `COMMODITY_ADDED` |
| 49 | admin | PUT `/api/v1/admin/commodities/{commodityId}/price-unit` | `verifyCommodityPriceUnit` | ADMIN | 1 | 200 | spec §31 RULE 3, ADR-0008, ADR-0020; audit `COMMODITY_UNIT_VERIFIED` |
| 50 | admin | PUT `/api/v1/admin/commodities/{commodityId}/names` | `updateCommodityNames` | ADMIN | 1 | 200 | spec F1.2, spec §20, ADR-0015, ADR-0020; audit `COMMODITY_NAMES_UPDATED` |
| 51 | admin | GET `/api/v1/admin/district-aliases` | `listDistrictAliases` | ADMIN | 1 | 200 | ADR-0014, ADR-0020 |
| 52 | admin | POST `/api/v1/admin/district-aliases` | `addDistrictAlias` | ADMIN | 1 | 201 | ADR-0014, ADR-0020; audit `DISTRICT_ALIAS_ADDED` |
| 53 | admin | GET `/api/v1/admin/audit-events` | `listAdminAuditEvents` | ADMIN | 1 | 200 | spec §27, ADR-0020, ADR-0029 |

## 4. Response names (spec §30 and §29)

| Spec name | Contract schema |
|---|---|
| `AuthResponse` | `AuthResponse` |
| `StateResponse`, `DistrictResponse`, `LocalityResponse` | Same names, each in an `items` list |
| `CommodityResponse`, `VarietyResponse` | Same names (and `GradeResponse`), in `items` lists |
| `MarketDetailResponse`, `TrendResponse` | Same names |
| `MarketComparisonResponse`, `SellingOptionResponse` | Same names (`recommendation.schema.json`) |
| `AlertResponse` | `AlertResponse`; events are `AlertEventResponse` (ADR-0019) |
| `IngestionHealthResponse`, `IngestionRunResponse`, `TriggerResponse` | Same names |
| `TransportAssumptionResponse`, `WeightsResponse` | Same names |
| `ApiErrorResponse` (§10) | `ApiErrorResponse` (`error-response.schema.json`) |
| `ForecastResponse` (§29, §35) | `ForecastResponse` |
| `PaginatedResponse<T>` (§30) | `PageMetadata` + `items`, one concrete `…Page` schema per item type |

Spec field names are kept: `quantityUnit`, `originLat`, `originLon`, `maxDistanceKm`, `recommendationId`, `priceOnlyWinnerId`, `netRealizationWinnerId`, `rankingsDisagree`, `options`, `transportRatePerKm`, `trendDirection`, `changePercent7Day`, `observations` (spec §21 step 32), `datasetType`, `available`, and the §35 forecast fields (`forecastHorizonDays`, `modelName`, `modelVersion`, `generatedAt`, `trainingDataThrough`, `mae`, `rmse`, `forecast`), served as the `forecasts` list of `ForecastResponse`.

One documented mapping: spec §16's `commodityName` is `commodity.name`, because reference names are returned as `name`, `nameHi` and `nameMr` (ADR-0022 item 11).

## 5. Scope and phase boundaries

- **Phase 1 only.** No Phase 2 operation exists (ADR-0032). The spec §30 Phase 2 groups (`/api/v1/listings`, `/bids`, `/transactions`, `/ratings`) are reserved for a later minor version, after RD-012 and RD-025 are resolved and the Phase 1 freeze (HARD RULE 1).
- `Role` already lists BUYER (spec §28), but Phase 1 never issues it.
- **Optional ML.** Only `getMarketForecast` belongs to the ML track (`x-agrinex-phase: OPTIONAL_ML`). It is implemented after HARD RULE 2. No other operation reads forecasts or depends on ML.
- **Outside the contract:** `/actuator/health` (internal, 404 at Nginx, ADR-0017), the frontend's `/api/health` (spec §45), and the data.gov.in API, which only the ingestion module calls (spec §31 RULE 1).

## 6. What the contract deliberately does not expose

- **Internal identifiers and keys:** `business_key`, `ingestion_run_id` on observations, token families, refresh-token hashes, password hashes, the hashes and expiry times of email-verification and password-reset tokens, profile ids.
- **Email addresses** are accepted (registration, login, password-reset request) but never returned; `AuthenticatedUser.emailVerified` says only whether the account's email is verified (ADR-0047).
- **Internal job state on public operations.** Only the derived `sourceStatus` appears there; full run detail is admin-only.
- **Secrets:** no API reads or writes one, and the government API key appears only as CONFIGURED or NOT_CONFIGURED (ADR-0020).
- **Scoring internals.** Composite scores and the weight values are not returned on public operations. Rankings ship with reason codes, estimates, trend and freshness (spec P2), and `decisionBasis.weightsVersionId` identifies the weights version used (`recommendation_runs.weights_id`; ADR-0026 item 3).
- **Revision history of observations** (`price_observation_revisions`). `ingestedAt` reflects the latest revision.
- **Forecasts anywhere except** the optional forecast operation.

## 7. Traceability labels

Every contract statement in `docs/api/` carries one of the labels defined in [API_CONVENTIONS.md](API_CONVENTIONS.md): [SSOT REQUIREMENT], [ARCHITECTURAL DECISION], [VERIFIED TECHNICAL FACT], [DATA EVIDENCE], [CONTRACT DESIGN], [ASSUMPTION], [REQUIRES_DECISION].

- Each operation in `openapi.yaml` lists its sources in `x-agrinex-trace`.
- Each admin write lists its audit action in `x-agrinex-audit-action`.
- The contract-design choices are summarized in [API_CONVENTIONS.md](API_CONVENTIONS.md) §9.

## 8. Open items, assumptions and notes

**Decisions recorded after the first draft** (Task 8.6, approved by the Human Project Owner). No open item blocks the contract.

| Decision | Resolves | Effect on the contract |
|---|---|---|
| ADR-0035 | RD-038 | No shape change. `sourceStatus`, freshness classification and the `listCommodities` filter reach their modules through read-only ports; comparisons pass their own thresholds |
| ADR-0036 | RD-039 | `distanceKm` uses the Earth radius 6 371.0088 km (API_CONVENTIONS.md §4.1) |
| ADR-0037 | RD-040 | No erasure operation. `ACCOUNT_ERASED` is written only by the in-application operator procedure |
| ADR-0038 | RD-041 | The transport label shows `appliedRatePerKm` (departure D-14) |
| ADR-0039 | RD-042 | `compareSellingOptions` accepts only stored district centroids as origin: 400 `VALIDATION_FAILED`, `INVALID_VALUE` otherwise |
| ADR-0040 | RD-043 | New 422 `ESTIMATE_OUT_OF_RANGE` on `compareSellingOptions` (`CompareUnprocessable`) |
| ADR-0041 | RD-044 | `AdminAuditAction` gains `ACCOUNT_DEACTIVATED` and `ACCOUNT_REACTIVATED`, with no operation |
| ADR-0042 | Security sign-off | Cookie `__Secure-agrinex_refresh`; token use resolves the account (401 for inactive or removed accounts); a rate-limited refresh is retried after `Retry-After` |

**Decisions of the Task 9 final decision lock** (2026-10-04, Human Project Owner; contract still 1.0.0, revised before its first merge; `CONTRACT_VALIDATION.md` §2.1):

| Decision | Resolves | Effect on the contract |
|---|---|---|
| ADR-0045 | RD-045 | No shape change. The recommendation wording is computed from existing fields; `netRealizationWinnerId` remains the composite rank 1 (ADR-0023 item 2) and is labelled "Recommended", never "highest net" (`FRONTEND_CONSUMPTION.md` §2.1) |
| ADR-0047 | RD-047 | `registerFarmer` requires `email` (409 may name `email`); `login`'s `identifier` is the mobile number or the email of the same account; `AuthenticatedUser.emailVerified` |
| ADR-0048 | — (mechanism of ADR-0047) | `requestPasswordReset` (202 always), `confirmPasswordReset` (204, sessions revoked, cookie cleared), `confirmEmailVerification` (204), `resendEmailVerification` (202); `PASSWORD_RESET_TOKEN_INVALID` and `EMAIL_VERIFICATION_TOKEN_INVALID` (422); new per-identifier and per-account limits |

**ASSUMPTIONS** (reversible interpretations):

| ID | Assumption | Basis |
|---|---|---|
| T8-A1 | `lastSuccessfulSyncAt` counts CURRENT_PRICE runs that ended SUCCESS or PARTIAL_FAILURE, that is, runs that loaded data | ADR-0011 item 2 treats both as useful runs; ADR-0022 item 9 does not define "successful" |
| T8-A2 | "Recent observations in coverage" (ADR-0006) means at least one observation in the active stale window at a VERIFIED, active market with coordinates, the same eligibility a comparison uses | ADR-0010 item 2, ADR-0014 item 5 |
| T8-A3 | `consecutiveFailures` counts FAILED runs back from the latest completed run until a SUCCESS or PARTIAL_FAILURE run | ADR-0019 item 7 ("count of consecutive failures") |
| T8-A4 | `marketFreshness` classifies each VERIFIED, active market by its latest observation across commodities; markets without observations count as unavailable | Spec §46 ("3 markets STALE"); INGESTION_ARCHITECTURE §4.3 ("stale markets") |
| T8-A5 | A simulation ranks by estimated net realization, like the client-side What-If, and selects observations as of the simulation time with the original run's configuration versions | ADR-0026 items 2 and 3 |
| T8-A6 | A PERCENTAGE_RISE or PERCENTAGE_FALL alert compares the unrounded change with the threshold; `triggerValue` stores it to 4 decimal places | Spec §34; ADR-0019; `alert_events.trigger_value` numeric(14,4) |
| T8-A7 | ADR-0011's "range of at most 366 days" counts report dates inclusively (`fromDate` to `toDate` is at most 366 dates) | ADR-0011 item 3; the stricter reading never exceeds the limit |

**Notes** (no decision needed now):

- **`farmer_profiles.preferred_locale`** has no write path in contract 1.0.0. ADR-0016 lists the registration fields without it, and no operation reads it. Adding it is an additive change once a consumer exists.
- **"Most common pinned"** in the commodity selector (spec §25) is left to Task 9: computed or configured (`docs/database/DATA_DICTIONARY.md` §5).
- **Market renames.** ADR-0014, ADR-0020 and the closed audit vocabulary provide no rename action, so the contract offers none. `DATA_DICTIONARY.md` was corrected to match on 2026-10-04 (Task 8.6).
- **Weight dominance.** The weights operation validates only ADR-0020's bounds (each weight 0 to 1; freshness days ordered). FR-10's "net realization as dominant factor" holds for the defaults (ADR-0024); configuring other weights is the product owner's runtime choice (ADR-0001 consequences).
- **Change control.** Under spec §41, the first commit of `contracts/` is reviewed by the consuming roles (Frontend, QA; Backend for the ML output), and later changes follow the Shared Contract Change Rule. A change to any file that `openapi.yaml` references counts as a change of `openapi.yaml`.
- **Operations beyond ADR-0020's table.** `reactivateMarket` and `verifyCommodityPriceUnit` have no row of their own in ADR-0020 item 1. They exist because the closed audit vocabulary, which the database design derives from ADR-0020, has `MARKET_REACTIVATED` and `COMMODITY_UNIT_VERIFIED` (`docs/database/DATA_DICTIONARY.md`, `admin_audit_events.action`), and because ADR-0008 item 1 leaves a commodity unverified until its unit evidence exists.
- **Freshness thresholds in comparisons.** A comparison or simulation classifies freshness with the thresholds of its own weights version (`recommendation_runs.weights_id`; ADR-0026 item 3), which recommendation passes to price's selection (ADR-0035 item 2).
