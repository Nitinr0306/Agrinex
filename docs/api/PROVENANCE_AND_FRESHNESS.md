# AgriNex — Price Provenance, Units, Freshness and Outage Semantics

**Status:** AUTHORITATIVE, part of the API contract area (SSOT index area 4). Created by Task 8 on 2026-10-04. The contract files govern where they differ, mainly `contracts/price-observation.schema.json`. Labels as in [API_CONVENTIONS.md](API_CONVENTIONS.md).

## 1. One price, many questions

Every object that carries a government price is a `PriceQuote` [ARCHITECTURAL DECISION, ADR-0022 item 9; ADR-0010 item 5]. It answers these questions:

| Question | Field | Meaning |
|---|---|---|
| Which stored observation is this? | `observationId` | Stable identifier of one row of `price_observations` |
| What date does it describe? | `reportDate` | The source `arrival_date` / `Arrival_Date`, parsed strictly from `dd/MM/yyyy` (ADR-0007). Never the ingestion date, never moved forward (ADR-0005) |
| What exactly was priced? | `variety`, `grade` (+ the enclosing market and commodity) | Source values as reported, or `null` when omitted (ADR-0006) |
| What was the price? | `minPrice`, `maxPrice`, `modalPrice` | Exact source values, rounded half-up to 2 dp at ingestion; `null` min or max when omitted (ADR-0008) |
| In what unit? | `priceUnit` | See §2 |
| Which dataset produced it? | `sourceDataset`, `sourceResourceId` | `CURRENT_PRICE` (resource `9ef84268-d588-465a-a308-a864a43d0070`) or `HISTORICAL_PRICE` (resource `35985678-0d79-46b4-9ed6-6f13308a1d24`), both DMI on data.gov.in (spec §1) |
| When did AgriNex store it? | `ingestedAt` | When the current values were written: at insert or at the latest source revision (ADR-0009) |
| Is it fresh or historical? | `freshnessStatus` | See §3 |
| Who must be credited? | `attribution` | The constant `Source: Ministry of Agriculture & FW / DMI / data.gov.in` (spec §31 RULE 7) |

The self-contained `PriceObservation`, the root of `price-observation.schema.json`, adds `state`, `district`, `market` and `commodity`.

### 1.1 From source record to API

[DATA EVIDENCE, evidence file §19.1–§19.3; ARCHITECTURAL DECISION, ADR-0006 to ADR-0010, ADR-0014.]

| Source JSON (historical / current) | Stored | API |
|---|---|---|
| `State` / `state` | resolved state | `state` (`StateRef`) |
| `District` / `district` | resolved through `district_source_aliases` to an LGD district | `district` |
| `Market` / `market` | resolved through `market_source_aliases`; a new spelling becomes a PROVISIONAL market | `market` (VERIFIED markets only on public operations) |
| `Commodity` / `commodity` | exact match against the known list | `commodity` |
| `Variety`, `Grade` / `variety`, `grade` | registered on first appearance | `variety`, `grade` |
| `Arrival_Date` / `arrival_date` (`dd/MM/yyyy`) | `report_date` (date) | `reportDate` (`yyyy-MM-dd`) |
| `Min_Price`, `Max_Price`, `Modal_Price` / `min_price`, `max_price`, `modal_price` | `numeric(14,2)` | `minPrice`, `maxPrice`, `modalPrice` |
| (none: the payload has **no** unit field) | `price_unit`, copied from the commodity's verified unit | `priceUnit` |
| resource of the request | `source_dataset_id` | `sourceDataset`, `sourceResourceId` |
| (time of writing) | `ingested_at` | `ingestedAt` |

These internal columns never appear in the API: `business_key`, `ingestion_run_id`, alias rows (except on the admin curation operations) and revision rows (see `API_INDEX.md` §8).

## 2. Price units

- **Evidence** [DATA EVIDENCE, evidence file §0.8]: the data.gov.in JSON payloads carry no unit field. The unit was verified from official DMI/Agmarknet report exports and the Agmarknet dashboard (`Rs./Quintal`), per commodity (ADR-0008).
- **Representation:** `priceUnit` is the unit verified for the observation's commodity and stored on the observation. The contract never claims it came from the upstream record.
- **No conversion:** AgriNex never multiplies or divides a source price. Only the farmer's quantity is converted, to quintals (spec §32).
- **Calculations** are per quintal, so comparison and alerts accept only commodities whose unit is `Rs./Quintal`. Any other unit answers 422 `UNSUPPORTED_PRICE_UNIT` (ADR-0008, ADR-0023). `MarketComparisonResponse.priceUnit` is the constant `Rs./Quintal`.
- **Market detail and trends** show a price in whatever unit was verified for its commodity.
- **Computed amounts** are INR totals with `currency: "INR"`, labelled ESTIMATED (spec §32, §49).

## 3. Freshness and selection

[SSOT REQUIREMENT, spec §16, F1.16, FR-13; ARCHITECTURAL DECISION, ADR-0007, ADR-0010, ADR-0024.]

**Freshness.** `today` is the Asia/Kolkata date. The thresholds (defaults 1, 4 and 10 days) come from a recommendation-weights version: in comparisons and simulations the comparison's own version (`decisionBasis.weightsVersionId`; ADR-0026 item 3), elsewhere the active version. The stale days also bound the selection window (ADR-0010 item 2), so they decide which observation is shown, not only its label.

| Status | Rule |
|---|---|
| FRESH | `reportDate ≥ today − freshDays` |
| RECENT | otherwise, `reportDate ≥ today − recentDays` |
| STALE | otherwise, `reportDate ≥ today − staleDays` |
| UNAVAILABLE | older |

The status is derived on every response, never stored on observations.

**Selection.** Comparison, market detail, trends and alerts choose observations by ADR-0010:

- candidates come from both datasets, and the current dataset wins for the same market, commodity, variety, grade and date;
- the observation date is the latest qualifying date within the stale window;
- a selected variety or grade must match exactly, and nothing else is substituted;
- with no variety or grade selected, the lower-median representative is taken, which is always a real observation.

**No synthesis.** No displayed price is ever an average or a blend. The only averages are inside the labelled trend statistic `changePercent7Day` (ADR-0025).

## 4. Source synchronization status

Responses that serve price data carry `sourceStatus` (`SourceSyncStatus`) [ARCHITECTURAL DECISION, ADR-0022 item 9; ADR-0005 item 5]:

| Field | Meaning |
|---|---|
| `sourceDataset` | Always `CURRENT_PRICE`: the status describes the current-price dataset |
| `lastSuccessfulSyncAt` | End of the most recent CURRENT_PRICE run that ended SUCCESS or PARTIAL_FAILURE (that loaded data); `null` if none |
| `latestSyncFailed` | `true` while the most recent completed CURRENT_PRICE run ended FAILED: nothing was fetched, a load transaction failed, or the run was interrupted (ADR-0011 item 8) |

Operations that carry it: `getMarketDetail`, `getMarketTrend`, `compareSellingOptions`, `simulateSellingOptions`, `listAlertEvents`.

Ingestion status reaches the price, recommendation and alert modules, and the active thresholds reach price and alert, through read-only ports over approved dependencies. A comparison passes its own thresholds to price's selection [ARCHITECTURAL DECISION, ADR-0035].

## 5. States the API distinguishes

[SSOT REQUIREMENT, spec §29, §31 RULE 5, §33; ARCHITECTURAL DECISION, ADR-0005, ADR-0023, ADR-0025, ADR-0030.]

| State | How the API expresses it | What the client shows (spec) |
|---|---|---|
| Normal | `latestSyncFailed: false`; prices with true freshness | Freshness badges, attribution, report date |
| Current source unavailable (403, 5xx, timeout, maintenance page, unexpected structure) | `sourceStatus.latestSyncFailed: true`. Stored observations are still served with their true `reportDate` and `freshnessStatus` | "Latest source synchronization is unavailable. Showing most recently stored government-reported data." (§29) |
| Last successful stored data | `lastSuccessfulSyncAt` | "Last synchronized …" |
| Stale data | `freshnessStatus: STALE`, plus the `STALE_DATA` reason code in comparisons | Stale-data warning or STALE banner (§10, §37) |
| No recent data, markets in range | Comparison: 200, `options: []`, `noDataReason: NO_RECENT_PRICE_DATA`, `excludedMarkets[]`. Market detail: `latestPrices: []` with the same reason | "No recent market observation available for this commodity in your area." (§29) |
| No market in range | 422 `NO_MARKETS_IN_RANGE` | "No data in range. Try increasing distance." (§25) |
| Thin history | `trendDirection: INSUFFICIENT_DATA`, `changePercent7Day: null` (`observations` possibly empty). A comparison option whose `price.reportDate` is before today − 6 always has this value, because the recent window is then empty (ADR-0010 item 2, ADR-0025 item 2) | "Insufficient historical data for this market/commodity" (§33) |
| Never synchronized (new deployment) | `lastSuccessfulSyncAt: null`, `latestSyncFailed: false`; empty results as above | Empty states |
| Current dataset never synchronized successfully | `lastSuccessfulSyncAt: null`, `latestSyncFailed: true`. Historical-dataset observations, if any, are served with their true dates (ADR-0010 item 1) | The §29 outage banner |
| ML forecast unavailable | `ForecastResponse.available: false`, `forecasts: []` | "Forecast unavailable." only in the optional forecast section (§29) |

AgriNex never fills a gap: no zero price, no carried-forward price, no interpolation, no other source, and no relabelling of old data as current (ADR-0005 item 5).

## 6. data.gov.in outage semantics

[ARCHITECTURAL DECISION, ADR-0005, ADR-0011.]

- data.gov.in remains the only ingestion source. Maintenance is temporary unavailability. The contract makes no promise about when the portal returns.
- A failed response is never read as "zero records" and never changes stored data. The run ends FAILED or PARTIAL_FAILURE, and the admin health view shows it (`consecutiveFailures`, `latestCompletedRun`).
- The public API keeps its shape during an outage. Only `latestSyncFailed`, the freshness values and the dates change.
- After recovery, the next scheduled run fetches again, and an admin backfills the missed dates with `triggerIngestion` (`HISTORICAL_PRICE` with `fromDate`/`toDate`).

## 7. Source, configuration, hypothesis, estimate, prediction

The contract keeps each kind of value in its own place, so that no client can mistake one for another:

| Kind | Where | Example |
|---|---|---|
| Observed government price | `PriceQuote` (`price`, `latestPrices[].price`, `observations[]`, `observation`) | `modalPrice: 3100` |
| Configured assumption | `transportAssumptions` (`ratePerKm`, `minimumTripCharge`, `vehicleCapacityKg`, `versionId`) | ₹8/km |
| User hypothesis (What-If) | `scenario.userOverrides`; `transportAssumptions.appliedRatePerKm` with `ratePerKmSource: USER_OVERRIDE` | ₹12/km |
| Derived estimate | `estimates.*` (ESTIMATED, INR) | `estimatedNetRealization` |
| Trend statistic | `trendDirection`, `changePercent7Day` | −8.5 % |
| ML prediction (optional) | `ForecastResponse` only (`kind: ML_FORECAST`) | `predictedPrice` |

- Simulation responses are never persisted (`scenario.persisted: false`) and are never presented as government data.
- Forecasts are never mixed into comparison, market detail, trends or alerts (ADR-0030; `docs/database/TABLE_OWNERSHIP.md`).
