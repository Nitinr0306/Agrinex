# AgriNex — Phase 1 Data and ML Readiness Report

**Status:** REFERENCE (2026-10-18). Read-only measurements of the normal local database (PostgreSQL 16 on port 5433), taken on 2026-10-18 with `scripts/data_readiness.sql` and `scripts/run_evaluation.sql`. It records evidence; it decides nothing. Decisions are in `docs/architecture/DECISION_LOG.md`.

## 1. Source and history

| Dataset (resource) | Observations | Report dates | Range |
|---|---|---|---|
| Historical `35985678-…` | 414,545 | 366 | 2025-10-15 to 2026-10-15 |
| Current `9ef84268-…` | 2,548 | 3 | 2026-10-14 to 2026-10-16 |

- Only the two approved data.gov.in resources are used (ADR-0005), through the ingestion module.
- Every report date in the year has data, including Sundays (32,587 rows over 52 Sundays, against 50–71 thousand on other weekdays).
- The last ingestion ran on 2026-10-16. No backend ran between then and this measurement, so no market is FRESH today: 281 VERIFIED markets are RECENT, 45 STALE and 28 UNAVAILABLE. This is the freshness rule working (`fresh` ≤ 1 day, `recent` ≤ 4, `stale` ≤ 10), not a data fault. A running backend refreshes every 6 hours.
- **Older history is available.** On 2026-10-18 the historical resource returned Maharashtra records for every probed date back to 2022: 15-10-2022 (1,081), 15-10-2023 (431), 15-04-2024 (1,138), 15-10-2024 (1,415), 15-01-2025 (1,197), 15-04-2025 (1,523), 15-07-2025 (1,393).
  - Two full 2024 days resolve with the current reference data: every commodity spelling is known, and every district spelling except `Murum`, a town the seed deliberately leaves unmapped (SEED_AND_REFERENCE_DATA §3.2). Those records would be rejected as `UNKNOWN_DISTRICT` and kept for review.
  - A further backfill (ADR-0011: at most 366 report dates per run, for example 2024-10-15 to 2025-10-14 through *Admin → Ingestion runs*) would roughly double the history and raise ML coverage. It is not needed for Phase 1 (≥ 60 days required; 366 present), so it was not run. It is a ready option for Experiment 3 or the ML track.
  - The data.gov.in gateway now answers 502 to Python's default `User-Agent` header. The backend's own client and any explicit `User-Agent` are served normally, so this does not affect ingestion.

## 2. Integrity

| Check | Result |
|---|---|
| Observation identities with more than one row | 0 |
| Duplicate business keys | 0 (also a database UNIQUE constraint) |
| Report dates in the future, or malformed | 0 |
| Rows whose unit differs from the commodity's verified unit | 0 |
| Rows of commodities without a verified unit | 0 |
| Repeated CURRENT run (2026-10-15) | 0 new rows |
| Re-ingestion of the stored date 2026-10-14 (HISTORICAL, 2026-10-18) | 1,496 fetched, 0 inserted, 1,470 recognized as already stored, 14 rejected, 12 conflicts quarantined; stored rows unchanged at 1,468; completeness 98.26 % |
| CURRENT feed on Sunday 2026-10-18 morning | 0 Maharashtra records (both runs SUCCESS with nothing fetched): an empty delivery, not an outage |

## 3. Units

- 298 of the 605 master commodities have a verified unit: 277 `Rs./Quintal`, 18 `Rs./Unit` and 3 `Rs./Bundle`.
- The other 307 master commodities have none. 10 of them occur in the Maharashtra feed and stay rejected as `UNIT_UNVERIFIED` (ADR-0052 item 2).
- Stored rows: 410,223 `Rs./Quintal`, 5,769 `Rs./Bundle`, 1,101 `Rs./Unit`. Comparison and alerts accept only `Rs./Quintal` (ADR-0008, ADR-0023). Other units are stored and shown, and a comparison of such a crop answers `UNSUPPORTED_PRICE_UNIT`.
- No unit was inferred: each comes from an official report export (`docs/database/SEED_AND_REFERENCE_DATA.md` §12.4).

## 4. Geography and talukas

- **Districts:** all 36 LGD districts of Maharashtra are seeded with Survey of India centroids. All 40 district spellings of the feed resolve (V10).
- **Talukas:** 358 LGD sub-districts (talukas) in 35 districts. LGD lists none for Mumbai (city). Talukas are offered for orientation only; distances are measured from the district centroid (ADR-0039), never from a taluka.
- **Markets:** 354 VERIFIED, 477 PROVISIONAL, 242 MERGED.

| District | VERIFIED markets | Reporting in the last 30 days |
|---|---|---|
| Ahilyanagar | 28 | 22 |
| Nashik | 26 | 25 |
| Pune | 22 | 21 |
| Buldhana, Yavatmal | 19 each | 18, 19 |
| … (all 36 districts: `scripts/data_readiness.sql` section 5) | | |
| Ratnagiri | 1 | 1 |
| Bhandara, Gondia, Palghar | 2–3 | 2 |
| **Sindhudurg, Mumbai Suburban** | **0** | 0 |

Sindhudurg and Mumbai Suburban have no market in the feed. Farmers there are compared with markets in other districts within the chosen distance; with none in range, the screen says so (E05).

## 5. Ingestion quality

- Runs: 9 CURRENT and 6 HISTORICAL SUCCESS runs with records, plus 3 failed CURRENT runs:
  - 2 `API_KEY_NOT_CONFIGURED` from before the key was set;
  - 1 deliberate disabled-key test on 2026-10-16.
- Completeness (stored, updated or already stored ÷ in-scope fetched):
  - CURRENT: minimum 96.77 %, average 98.28 %;
  - HISTORICAL: minimum 93.59 %, average 97.15 %.

  The 93.59 % run is the original year backfill, before the RD-050 aliases (ADR-0051); every later run is at least 96.3 %.
- Records rejected or quarantined over all runs:

  | Code | Records | Meaning |
  |---|---|---|
  | `PRICE_BELOW_UNIT_FLOOR` | 12,308 | Below 100 Rs./Quintal: mostly leafy vegetables reported at per-kg magnitudes (ADR-0008 V7) |
  | `UNKNOWN_COMMODITY` | 11,337 | The legacy spellings, rejected before ADR-0051. Since then they are resolved, and 8,978 of them are stored |
  | `SAME_KEY_DIFFERENT_PRICES` | 4,905 | Conflicting copies, quarantined (ADR-0009) |
  | `PRICE_OUTLIER` | 4,677 | More than 10× from the group median (ADR-0008 V8) |
  | `UNIT_UNVERIFIED` | 1,276 | The 10 crops without unit evidence |
  | Other | 13 | Invalid ranges, non-positive prices, failed runs |

## 6. ML readiness (spec §35)

The measure is the representative daily series of each market–commodity pair: the view `daily_price_series`, with one selected observation per date (ADR-0010).

| Markets | Pairs | ≥ 90 observations | ≥ 90 with §35's split | 60–89 | < 60 |
|---|---|---|---|---|---|
| VERIFIED | 3,571 | **1,301** | **1,179** | 350 | 1,920 |
| PROVISIONAL | 3,717 | 325 | 0 | 213 | 3,179 |

- **"With §35's split"** means at least 30 observations in January–July 2026, and at least 5 in each of August and September 2026, so that §35's chronological split (training, validation, test) can be applied. The 30/5/5 minimums are an ASSUMPTION made for this measurement; §35 sets none.
- **Eligible VERIFIED pairs** span 103 commodities and 184 markets, and 1,277 of them are priced per quintal. A pair has a median of 183 observations (maximum 358). The median longest gap between consecutive report dates is 9 days (90th percentile: 42).
- **Most eligible pairs by commodity:**
  - Wheat 76, Soyabean 69, Red gram/Arhar/Tur(whole) 59;
  - Bengal Gram 58, Onion 53, Jowar 40;
  - Brinjal, Green Chilli and Tomato 39 each.
- **The evaluation code is ready.**
  - `ml/forecasting` provides the two §35 baselines and a chronological, expanding-window evaluation: a model receives only the history up to each origin date.
  - Its 15 tests run on synthetic data and include a leakage test (`test_the_model_never_sees_data_after_the_origin`) and hand-computed MAE, RMSE and directional accuracy.
  - The daily series is selected per date only, so it carries no future information. Price revisions (ADR-0009) are the one caveat: an evaluation sees the revised value, not the value first published.
- **Not run on real data.** Spec §35 criteria 2 ("deployed") and 3 ("after the Phase 1 freeze") are not met, and `docs/api/ML_CONTRACT_BOUNDARY.md` §8 forbids the job before then. The smallest change that would allow an evaluation is ADR-0053 (PROPOSED; RD-051): one offline, read-only run of the baselines, with nothing written or exposed. It needs the owner's approval. No forecast exists, and `GET /markets/{id}/forecast` answers `available: false`.

## 7. Experiment 1 instrumentation (spec §36)

- Every comparison is recorded (`selling_searches`, `recommendation_runs`, `selling_options`; ADR-0029).
- The experiment query is `scripts/run_evaluation.sql`. It is tested on hand-computed data by `EvaluationScriptIntegrationTest`.
- Run on the local database (2026-10-18):
  - 87 scenarios, 40 disagreements (45.98 %);
  - average estimated net difference −1,085.74, median −580.00, 90th percentile 44.33;
  - 35 of the 40 differences are negative.

  **These figures are not evidence**: the comparisons are tests, Task 12 checks, and 8 from the owner's own use on 2026-10-16.
- **Observation for the owner.** In these comparisons the recommended (rank-1) market had the highest estimated net realization in 50 of 87 cases. When the price-only winner and rank 1 differed, rank 1 usually had the *lower* estimate.
  - This follows from the composite score (ADR-0024: 0.55 net, 0.15 price, 0.10 each for trend, freshness and transport). The recorded rows are consistent: the stored winner is rank 1 in 87 of 87, and the price winner has the highest price in 87 of 87.
  - ADR-0045 allows negative differences and requires reporting them. If real use shows the same pattern, the default weights (runtime configuration, ADR-0024) may deserve review against FR-10 ("net realization as dominant factor"). That is a product decision for the owner.
