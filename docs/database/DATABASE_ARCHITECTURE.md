# AgriNex — Database Architecture

**Status:** AUTHORITATIVE. Database architecture from Task 7 (2026-10-03), approved in the decision lock of the same day ([DECISION_LOG.md](../architecture/DECISION_LOG.md), ADR-0001 to ADR-0034).

- This document set designs the AgriNex PostgreSQL database from these sources:
  - the master specification (`docs/01_AGRINEX_MASTER_PRODUCT_PHASES.md`, "spec");
  - the Task 6 architecture documents;
  - the verified government-data evidence (`docs/data/AGRINEX_VERIFIED_GOVERNMENT_API_DATA_REFERENCE.md`, "evidence");
  - the official report exports in `docs/data/raw/`.
- Statements traced to the spec carry the spec's authority. The ARCHITECTURAL DECISIONS of this set were approved under the delegation recorded in ADR-0001.
- Where this set and a binding decision-log entry differ, the entry governs.
- **Nothing here is implemented.** No migration, table, entity, repository or database exists. SQL in this document set is illustrative documentation, not an executable migration.

**Versions:** PostgreSQL 16 (16.15) and Flyway 12.4.0, as the approved baseline sets them (ADR-0002; `docs/architecture/CURRENT_TECHNOLOGY_BASELINE.md`). The only version-sensitive feature used is `UNIQUE NULLS NOT DISTINCT`, which PostgreSQL 16 documents (VERIFIED TECHNICAL FACT, PostgreSQL 16 documentation §5.4.3 and §11.6, fetched 2026-10-03).

**Labels used in this document set:**

| Label | Meaning |
|---|---|
| REQUIREMENT FROM SSOT | Stated by the master specification, cited as (§n) |
| VERIFIED OBSERVATION | Visible in the supplied government captures (evidence file, Appendix A) or in the official exports in `docs/data/raw/`. True for those files only; future source behavior needs production verification |
| VERIFIED TECHNICAL FACT | Stated by current official documentation, with source and date |
| ARCHITECTURAL DECISION | A design choice made by this document set, approved in the decision lock. The ADR-NNNN cited with it records the decision |
| ASSUMPTION | A temporary, reversible interpretation (A-DB-n, section 20.3) |
| CONFLICT | Incompatible statements between authoritative sources (DB-C-n, [DATABASE_INDEX.md §9](DATABASE_INDEX.md#9-conflicts)); all are resolved |

## 1. Purpose

This document is the database blueprint for Task 8 (API contracts), Task 9 (UI/design), Task 10 (scaffolding), and the later backend, ingestion, recommendation and optional ML work. It states:

- which data AgriNex persists, in which tables, owned by which module;
- how government observations are identified, normalized, deduplicated and audited;
- which rules the database enforces and which stay with ingestion validation;
- which decision-log entries shape it (section 20).

Column-level detail lives in [DATA_DICTIONARY.md](DATA_DICTIONARY.md), keys and indexes in [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md), ownership in [TABLE_OWNERSHIP.md](TABLE_OWNERSHIP.md), diagrams in [ERD.md](ERD.md), and reference data in [SEED_AND_REFERENCE_DATA.md](SEED_AND_REFERENCE_DATA.md). This document does not repeat that detail.

## 2. Scope

**In scope:**

- the Phase 1 relational schema: 27 tables;
- the optional ML `forecasts` table;
- the names, owners and dependency direction of the Phase 2 tables (PHASE 2 / DEFERRED, no columns);
- reference-data and seeding strategy;
- integrity rules, indexes, ownership, lifecycle.

**Geographic scope (ADR-0006):** Phase 1 covers **Maharashtra statewide**, not a district or pilot region.

- The schema itself is state-agnostic, as §27 requires ("Should support additional states/commodities without code changes") and as §38 and §47 Q20 describe.
- Maharashtra is a data scope, set in deployment configuration, not a schema constraint.

**Commodity scope (ADR-0006):** broad and source-driven.

- The four persona commodities in §6 (onion, tomato, cotton, soybean) are not the scope.
- The commodity known list is the official Agmarknet 2.0 commodity master: 605 names in 16 groups. All 52 Maharashtra commodity values in the captures match it exactly ([SEED_AND_REFERENCE_DATA.md §4](SEED_AND_REFERENCE_DATA.md#4-commodities)).

**Out of scope:**

- migrations, DDL files, entities, repositories and services;
- ingestion and API implementation;
- Phase 2 column design;
- PostGIS, and any geocoding or map API (§39; ADR-0014);
- additional database types (§47 Q10).

## 3. Phase 1 database boundary

Phase 1 must stand alone (P8, §7). Its database contains only what these need: the Phase 1 features (F1.1–F1.25), the functional requirements (FR-01–FR-20), the freeze checklist (§10), and the decisions that complete them:

| Domain | Tables | Phase 1 capability served |
|---|---|---|
| Geography reference | `states`, `districts`, `district_source_aliases`, `localities` | Location wizard (F1.3); district-centroid origin (FR-06); deterministic district resolution (ADR-0014) |
| Commodity reference | `commodities`, `varieties`, `grades`, `commodity_source_aliases` | Commodity and variety/grade selection (F1.4); known list and verified units (ADR-0006, ADR-0008); legacy source spellings (ADR-0051) |
| Government source and observations | `source_datasets`, `markets`, `market_source_aliases`, `price_observations`, `price_observation_revisions` | Latest reported prices (F1.7), trends (F1.15), freshness (F1.16), attribution (F1.17), duplicate prevention (FR-04), revision audit (ADR-0009) |
| Ingestion audit and control | `ingestion_runs`, `ingestion_errors`, `ingestion_source_settings` | Audit on every run (FR-17), admin health (FR-18, F1.20), Experiment 2 (§36), pause switch (ADR-0020) |
| Identity and access | `users`, `farmer_profiles`, `refresh_tokens` | JWT authentication (§10), RBAC (FR-20), alerts for registered farmers (§25), token rotation (ADR-0016), email verification and password recovery (ADR-0047, ADR-0048) |
| Recommendation configuration | `transport_assumptions`, `recommendation_weights` | Configurable transport model (F1.9, §32) and scoring (F1.12, §16) |
| Recommendation results | `selling_searches`, `recommendation_runs`, `selling_options` | Comparison persistence (§16), winners (F1.13), reasons (F1.14), Experiment 1 (§36) |
| Alerts | `price_alerts`, `alert_events` | Price alerts (F1.19, FR-19), in-app delivery (ADR-0019) |
| Administration | `admin_audit_events` | Audit of every admin action (ADR-0020) |

How the §10 DATA freeze items map onto the schema:

| §10 DATA item | Database support |
|---|---|
| Current API integrated and paginated correctly | `ingestion_runs.source_total_records`, `pages_fetched`, `pages_failed`, `request_scope` (section 11) |
| Historical bootstrapped with ≥60 days | `price_observations` rows of the `HISTORICAL_PRICE` dataset; `report_date` coverage is measurable (ADR-0011) |
| Data normalized (trim, canonical market names, commodity names, units verified) | Normalization CHECKs, `market_source_aliases`, `district_source_aliases`, exact commodity names, and `price_unit` copied from the commodity's verified unit (sections 9 and 10; ADR-0008) |
| Duplicate prevention (business key + DB unique constraint) | `uq_price_observations_business_key` plus the null-safe identity constraint (section 10) |
| Freshness metadata on every observation | `report_date` and `ingested_at` on every row (section 10.6) |
| Ingestion audit on every run | `ingestion_runs`, `ingestion_errors`, the accounting invariant (section 11) |
| Scheduler on 6-hour cadence | `ingestion_runs.triggered_by` and `started_at` make the cadence auditable (ADR-0011) |

No Phase 1 table references a Phase 2 table, and no Phase 1 query needs one.

## 4. Phase 2 deferred database boundary

**PHASE 2 / DEFERRED.** HARD RULE 1 (§10) puts Phase 2 development after the Phase 1 Freeze. The spec names these Phase 2 persistence elements; this document set records only their names, owners and dependency direction:

| Table | Owner module | Source |
|---|---|---|
| `produce_listings` | listing | §23, F2.3, F2.4, §47 Q23 |
| `bids` | bid | §23, F2.5–F2.7 |
| `transactions` | transaction | §18, §23, F2.8 |
| `transaction_state_history` | transaction | §23, F2.11, FR-24 |
| `buyer_verifications` | auth (ADR-0031) | §19, §23, F2.2 |
| Rating storage (no table name in the spec) | rating | §23, F2.10, FR-25 |

- **Direction of dependency:** Phase 2 tables may reference Phase 1 tables (`users`, `commodities`, `recommendation_runs`). Phase 1 tables never reference Phase 2 tables, and a Phase 1 database without any Phase 2 table is complete.
- **Decided placements (ADR-0031):**
  - buyer verification belongs to auth;
  - verification documents are stored in PostgreSQL with access control;
  - system-initiated transitions (for example listing expiry) run as a scheduled job in the listing module.
- **Not designed here:** columns, states, keys and constraints. The state model (RD-012), the OTP provider and the listing TTL value are deferred by ADR-0032 until Phase 2 starts.
- **Phase 1 already provides** the `BUYER` role value (§28, FR-20) and `recommendation_runs.created_at`, which identifies a farmer's "last" run for listing linkage (§47 Q23).

## 5. PostgreSQL role

- **System of record.** PostgreSQL stores all domain data (§12, §20) and is the only database type (§47 Q10). Farmer-facing answers come from stored data only (§31 RULE 1, §37 "Cache all data in PostgreSQL", FR-16); the government API is never on a request path.
- **Schema management.** Flyway migrations run automatically on startup (§10, §12), from `backend/src/main/resources/db/migration/` (§43). The spec plans "Database migrations V1–V6 (Phase 1 schema)" (§42 item 4). A non-binding grouping that respects the foreign-key order:

  | Migration | Tables |
  |---|---|
  | V1 | `states`, `districts`, `district_source_aliases`, `localities`, `commodities`, `varieties`, `grades` |
  | V2 | `source_datasets`, `ingestion_source_settings`, `markets`, `market_source_aliases` |
  | V3 | `ingestion_runs`, `ingestion_errors` |
  | V4 | `price_observations`, `price_observation_revisions` |
  | V5 | `users`, `farmer_profiles`, `refresh_tokens`, `admin_audit_events` |
  | V6 | `transport_assumptions`, `recommendation_weights`, `selling_searches`, `recommendation_runs`, `selling_options`, `price_alerts`, `alert_events` |

  - Reference data and initial configuration load through Flyway as well (ADR-0021; [SEED_AND_REFERENCE_DATA.md](SEED_AND_REFERENCE_DATA.md)).
  - `forecasts` gets its own later migration only if ML proceeds (§35).
  - V12 adds `commodity_source_aliases` and `price_observations.source_commodity_name` (ADR-0051).
  - Phase 2 migrations come after the freeze.
  - Applied migrations are never edited (CLAUDE.md §I).
- **One schema, module ownership by convention and code.** All tables live in one PostgreSQL schema. Ownership is enforced by the module rule (§27), in code review and by repository placement.
  - The application uses one database role.
  - The only other role is the optional ML job's restricted role: SELECT on a daily-series view, and INSERT/DELETE on `forecasts` (ADR-0030).
- **Not the database's job:**
  - rate-limit counters and the reference-data cache, which live in Redis (ADR-0018);
  - geospatial computation: haversine runs in the transport module (§16), and there is no PostGIS.
- **Expected volume (ESTIMATED).**
  - A Maharashtra-scoped current capture reported a `total` of 314 records for one day (C-314-P0). The filter string was not retained (ASSUMPTION A-DB-01).
  - Historical Maharashtra onion captures reported 17–103 records per day.
  - Statewide ingestion of all commodities from both datasets therefore stores on the order of 10⁵–10⁶ observation rows per year. Even the spec's own planning figure of "thousands of records per day" (§47 Q10) means only about 10⁶ rows per year.
  - PostgreSQL B-tree indexes handle that without partitioning, so none is designed.

## 6. Domain decomposition

| Domain | Owner module(s) | Source |
|---|---|---|
| Geography reference | geography | §20 STATES, DISTRICTS, LOCALITIES; F1.3; FR-06; §30 geography group; ADR-0014 |
| Commodity reference | commodity | §20 COMMODITIES, VARIETIES, GRADES; F1.4; §30 commodity group; ADR-0006, ADR-0008 |
| Government source and observations | price | §1; §14–§15; §20 PRICE_OBSERVATIONS, MARKETS, SOURCE_DATASETS; §22; §30 `/markets/*`; §31; §47 Q4, Q8; evidence §19–§20; ADR-0009, ADR-0010 |
| Ingestion audit and control | ingestion | §14 AU1; §15; §20 INGESTION_RUNS, INGESTION_ERRORS; §22; §29; FR-17, FR-18; ADR-0011, ADR-0020 |
| Identity and access | auth | §10; §20 USERS, FARMER_PROFILES; §25; §27; §28; ADR-0016 |
| Recommendation configuration | transport, recommendation | §16; §20 TRANSPORT_ASSUMPTIONS, RECOMMENDATION_WEIGHTS; §32; UC19–UC20; ADR-0024 |
| Recommendation results | recommendation | §16 PERSIST; §20 SELLING_SEARCHES, RECOMMENDATION_RUNS, SELLING_OPTIONS; §36; ADR-0023 |
| Alerts | alert | §20 PRICE_ALERTS, ALERT_EVENTS; §34; FR-19; ADR-0019 |
| Administration | admin | ADR-0020 (audit of admin actions) |
| Optional ML | price | §35; ADR-0030; `docs/architecture/ML_ARCHITECTURE.md` |
| Phase 2 (deferred) | listing, bid, transaction, rating; verification: auth | §18, §19, §23; ADR-0031, ADR-0032 |

The admin module owns one table, `admin_audit_events`. It reads ingestion health through the ingestion module, and it changes configuration and reference data through the owning modules (§13, §30; ADR-0013, ADR-0020).

## 7. Table groups

| Group | Table | Status | Basis for the table |
|---|---|---|---|
| Geography reference | `states` | REQUIRED | §20 |
| Geography reference | `districts` | REQUIRED | §20 |
| Geography reference | `district_source_aliases` | REQUIRED | ADR-0014: source district spellings, renamed districts |
| Geography reference | `localities` | REQUIRED | §20 |
| Commodity reference | `commodities` | REQUIRED | §20 |
| Commodity reference | `varieties` | REQUIRED | §20 |
| Commodity reference | `grades` | REQUIRED | §20 |
| Commodity reference | `commodity_source_aliases` | REQUIRED | ADR-0051: legacy DMI commodity spellings |
| Government source and observations | `source_datasets` | REQUIRED | §20 |
| Government source and observations | `markets` | REQUIRED | §20 |
| Government source and observations | `market_source_aliases` | REQUIRED | §47 Q4 ("canonical mapping table"); evidence |
| Government source and observations | `price_observations` | REQUIRED | §20 |
| Government source and observations | `price_observation_revisions` | REQUIRED | ADR-0009: no silent overwrite of source values |
| Ingestion audit | `ingestion_runs` | REQUIRED | §20 |
| Ingestion audit | `ingestion_errors` | REQUIRED | §20 |
| Ingestion audit | `ingestion_source_settings` | REQUIRED | ADR-0020: admin pause switch |
| Identity and access | `users` | REQUIRED | §20 |
| Identity and access | `farmer_profiles` | REQUIRED | §20 |
| Identity and access | `refresh_tokens` | REQUIRED | §28 ("Backend refresh_tokens table") |
| Recommendation configuration | `transport_assumptions` | REQUIRED | §20, §32 |
| Recommendation configuration | `recommendation_weights` | REQUIRED | §20 |
| Recommendation results | `selling_searches` | REQUIRED | §20 |
| Recommendation results | `recommendation_runs` | REQUIRED | §20 |
| Recommendation results | `selling_options` | REQUIRED | §20 |
| Alerts | `price_alerts` | REQUIRED | §20 |
| Alerts | `alert_events` | REQUIRED | §20 |
| Administration | `admin_audit_events` | REQUIRED | ADR-0020: attributable admin changes |
| Optional ML | `forecasts` | OPTIONAL | §35 ("written to forecasts table") |

**Tables beyond §20, §28 and §35, answered against the five Task 7 questions:**

| Question | `market_source_aliases` | `district_source_aliases` | `price_observation_revisions` | `ingestion_source_settings` | `admin_audit_events` |
|---|---|---|---|---|---|
| 1. Which requirement needs it? | §47 Q4 canonical mapping table; FR-03; §10. Evidence: alias twins (47 candidate groups in H-01AUG-2026-P0), whitespace variants (73 trailing and 1 doubled of 131 strings), `Sirsa APMC` in two states | ADR-0014. The official Agmarknet master has 38 Maharashtra entries for 36 LGD districts, including `Osmanabad` and `Dharashiv`; feed spellings such as `Amarawati` | ADR-0009 (owner rule "NEVER silently overwrite conflicting source observations"); §14 "DO UPDATE if newer ingestion_run"; §49 "revised"; §48 exact admin counts | ADR-0020 (pause during known source maintenance); ADR-0011 scheduler | ADR-0020 (who changed configuration and reference data); §27 correlation IDs; CLAUDE.md §K |
| 2. Which module owns it? | price | geography | price | ingestion | admin |
| 3. Why can an existing table not represent it? | `markets` holds one canonical name per market; variants as extra rows would split prices | `districts` holds the official LGD name; source spellings are many-to-one | An update in place would lose the replaced values | No existing table holds runtime ingestion control; secrets and schedules stay in the environment | Configuration versions record what changed, not who changed it or why; curation of reference data has no history otherwise |
| 4. Phase? | Phase 1 | Phase 1 | Phase 1 | Phase 1 | Phase 1 |
| 5. Status | REQUIRED | REQUIRED | REQUIRED | REQUIRED | REQUIRED |

No other entity was added. In particular there is no commodity mapping table (commodity names match the official master exactly), no notification table, no identity-provider table and no quality-flag column (sections 9.5, 10.5, 13 and 14).

## 8. Entity relationships

The full diagrams, cardinalities and key lists are in [ERD.md](ERD.md). The relationships that shape the design:

- **Observation hub.** `price_observations` references `markets`, `commodities`, `varieties`, `grades`, `source_datasets` and `ingestion_runs`. Every price is traceable to where it was reported, what it is, which dataset supplied it and which run stored it (§31 RULE 7). `price_observation_revisions` records every later change, with the revising run (ADR-0009).
- **Variety and grade belong to the observation's commodity.** Composite foreign keys (`variety_id`, `commodity_id`) → `varieties` (`id`, `commodity_id`), and the same for grades, make it impossible to attach a Tomato variety to an Onion observation. With the default MATCH SIMPLE, a NULL `variety_id` skips the check (VERIFIED TECHNICAL FACT, PostgreSQL 16 `CREATE TABLE`, fetched 2026-10-03). The same pattern applies to `selling_searches`.
- **Decision records reference, not copy.** `selling_options` points at the exact observation, market, run, weights version and transport version used. `estimated_gross_value` preserves the price × quantity actually applied, so prices are not duplicated (§16, §20).
- **Market identity via aliases.** Ingestion resolves the source triple (state, district, market) through `market_source_aliases` to one `markets` row. A new spelling registers a PROVISIONAL market. A merge points the merged market at its target (`merged_into_market_id`) (ADR-0014).
- **Alert events point at their cause:** the run, the market and the observation that met the condition (ADR-0019).
- **Cross-module foreign keys are integrity constraints, not access.** §20 itself draws foreign keys across module boundaries (for example `selling_options` → `price_observations`). Declaring them in migrations lets PostgreSQL enforce integrity; module code still reads and writes only its own tables (section 16).

## 9. Reference-data strategy

### 9.1 Principles

- **Data-driven.** States, districts, localities, markets, commodities, varieties and grades are rows. No commodity, market or state name appears in a CHECK constraint, enum or code path (§27, §38).
- **Identified sources only.** Reference values come from the government feed (as observed) or from an identified official source: LGD for names and codes, Survey of India for boundaries, and DMI's Agmarknet 2.0 masters and report exports for commodities, groups and units. Nothing is invented (CLAUDE.md §C; P10; ADR-0006, ADR-0014).
- **Normalization without loss.** Normalized values are stored for matching; raw source spellings are preserved where the evidence shows variation (market strings).

### 9.2 Normalization rule (ARCHITECTURAL DECISION)

For every source text value used for matching: collapse each run of whitespace to one space and trim both ends. Case, punctuation and parentheses are **not** changed, and no fuzzy matching is applied. This implements "Trim all string fields" (§15) and removes the trailing and doubled spaces observed in market strings (evidence §19.7; `SHRI.SIDDHESHWAR  APMC` has a doubled space). The database enforces it with CHECK constraints of the form `x = btrim(regexp_replace(x, '\s+', ' ', 'g'))` on canonical and normalized columns, so an un-normalized value cannot create a near-duplicate row (`regexp_replace` with the `g` flag replaces all matches: VERIFIED TECHNICAL FACT, PostgreSQL 16 §9.7, fetched 2026-10-03).

### 9.3 Geography

- **Districts:** the 36 Maharashtra districts, with names and codes as published by LGD (`lgd_code`), for example Ahilyanagar 466, Chhatrapati Sambhajinagar 469, Dharashiv 488 (ADR-0014).
- **Centroids are mandatory** (`centroid_lat`, `centroid_lon` NOT NULL), because FR-06 computes every distance from the farmer's district centroid.
  - They are computed once from Survey of India district boundaries, in WGS84.
  - The method is the geometric centroid, or a point on the surface when the centroid falls outside the polygon.
  - `centroid_source` records the dataset, version and method.
- **District aliases.** The government feed's district spellings are not a district list.
  - The captures contain 17 Maharashtra districts, spelled as the feed spells them (for example `Amarawati`, `Chattrapati Sambhajinagar`).
  - The official Agmarknet master has 38 entries, including `Osmanabad` and `Dharashiv` and the non-district entries `Bandra(E)` and `Murum`.
  - Each spelling maps to one LGD district through `district_source_aliases`, with an official evidence note. An unmapped spelling rejects the record as `UNKNOWN_DISTRICT`.
- **Localities:** the LGD sub-districts (talukas; 358 in Maharashtra), with LGD codes and without coordinates. The farmer origin stays the district centroid (FR-06, §21 step 3, §28, §47 Q6), so locality coordinates are neither seeded nor used in Phase 1 (ADR-0014).
- **State codes:** ISO 3166-2 (`IN-MH`). The LGD state code is stored as `lgd_code` (ADR-0006, ADR-0014).

### 9.4 Markets and aliases

- **Coordinates.**
  - At registration a market gets its district centroid as approximate coordinates (`is_coordinate_approximate = true`), the §16 case "market.lat derived from district centroid".
  - Exact coordinates are set only by an admin, from an identified source such as the DMI National Agriculture Market Atlas, and they clear the flag. `coordinate_source` always accompanies coordinates.
  - No geocoding API, map service or PostGIS is used (ADR-0014).
- **Alias resolution.**
  - Ingestion normalizes the source (state, district, market) triple and looks it up in `market_source_aliases`.
  - The key includes source state and district because market names repeat across regions: `Sirsa APMC` appears in Haryana (district Sirsa) and in Uttar Pradesh (district Prayagraj) in C-647-P0 (VERIFIED OBSERVATION).
- **Unknown spellings register PROVISIONAL markets** (ADR-0014). A triple with no alias creates:
  - a `markets` row with `review_status = 'PROVISIONAL'`;
  - an alias row whose `evidence_note` names the registering run.

  Its observations are stored, so no data is lost. A PROVISIONAL market is never a comparison or alert candidate until an admin verifies it.
- **No automatic merges.** Merging is an admin action that needs evidence. Price equality alone is not proof of identity. The evidence contains both:
  - genuine twins: in H-01AUG-2026-P0, 47 groups of records have identical district, commodity, variety, grade, date and prices under two spellings. Most are `X APMC` vs `APMC X` (for example `Manmad APMC` / `APMC Manmad`, `Lasalgaon APMC` / `APMC Lasalgaon`), and some pairs are less obvious, such as `APMC Solapur` / `SHRI.SIDDHESHWAR APMC`;
  - coincidences: `APMC Karjat` and `Karjat(Rashin)` have identical Wheat prices on 14/09/2026 but different Black Gram prices; `Pune(Pimpri)` and `Pune(Moshi)` match for Onion on 14/09/2026 but not on 13/09/2026; five Tamil Nadu market pairs in C-647-P0 also coincide.
- **Merge procedure (ADR-0014).**
  1. The merged market's aliases move to the target.
  2. Its observations are re-pointed under the ADR-0009 identity rules: an identical copy is dropped; a conflicting copy keeps the target's value, and the dropped values are recorded in the admin audit event.
  3. The merged market keeps its row with status MERGED, `merged_into_market_id` and `is_active = false`.
- **Slugs.** `canonical_slug` stays globally unique: the lowercase canonical name with every run of non-alphanumeric characters replaced by one hyphen. On collision, `-<district slug>` is appended, then, if needed, `-<state code>` (ADR-0014).

### 9.5 Commodities, varieties and grades

- **Exact identity.** `commodities.canonical_name` is the normalized source value, matched exactly. `Onion` and `Onion Green` are separate rows (evidence §20.3; C-19-MH-ONION returns 16 `Onion` and 3 `Onion Green` rows). Varieties and grades are separate tables per commodity (§20), never folded into the commodity.
- **Source values kept as they are.** `Other` and `Local` are real source values (`Local` occurs as both a variety and a grade) and are stored as such. They are not mapped to "unknown".
- **Known list (ADR-0006).** The `commodities` table is seeded from the official Agmarknet 2.0 commodity master: 605 names in 16 groups, of which two carry a trailing space that normalization removes.
  - All 52 Maharashtra commodity values in the captures, and all 11 Maharashtra headings in the official exports, match master names exactly.
  - A record with an unknown commodity is rejected as `UNKNOWN_COMMODITY`, and admins add commodities only with an official reference.
  - Varieties and grades register automatically on first appearance, per commodity. §14 sets no known-list check for them.
- **Units (ADR-0008).** Each commodity carries its evidence-verified `price_unit` and `price_unit_source`. A commodity without a verified unit cannot be ingested (`UNIT_UNVERIFIED`).
- **`Commodity_Code` is not persisted (ARCHITECTURAL DECISION).**
  - It exists only in the historical source (evidence §19.3), and only `23` (Onion) was observed.
  - Agmarknet's own codes differ between portal generations: Tomato is 78 in a legacy Agmarknet URL and 65 in the Agmarknet 2.0 master.
  - Commodity names match the master exactly, so no code mapping is needed. The code stays visible in the raw payload of rejected records.
- **Localized names** (`name_hi`, `name_mr` on `states` and `commodities`, §20) are curated with recorded provenance. A missing value falls back to the English name (ADR-0015). §20 gives no localized names for districts, markets, varieties or grades, and none are added.
- **Category:** the official Agmarknet commodity-group name (ADR-0006).

### 9.6 Source datasets

Two rows, identified by `resource_id` (§1; VERIFIED OBSERVATION of `index_name` in every capture), with `dataset_type` `CURRENT_PRICE` (§45) and `HISTORICAL_PRICE` (ADR-0006). One resource per role (`uq_source_datasets_dataset_type`).

The §1 roles describe what each dataset is fetched for. Selection reads both datasets, and for the same identity the current dataset takes precedence (ADR-0010, departure D-08).

## 10. Observation strategy

### 10.1 Identity

A source observation is identified by **source dataset + market + commodity + variety + grade + report date** (§14 business key; evidence §20.4). The evidence shows why every component matters:

- one market, commodity and date carries several varieties: `APMC Shevgaon` Onion on 07/09/2026 has `2nd Sort`, `1st Sort` and `Other` rows; `APMC Nagpur` has `Red` and `White` (VERIFIED OBSERVATION);
- current and historical rows for the same market, commodity and date must coexist, so the dataset is part of the key (§14);
- `Onion` and `Onion Green` differ (section 9.5).

Deduplicating by market alone, or assuming a fixed number of markets, is never done.

### 10.2 Null-safe uniqueness (ARCHITECTURAL DECISION)

The spec makes market, commodity, date and price the required fields (§14 V1); variety and grade are not required. A missing variety or grade must therefore not reject the record, and the identity must still be unique.

| Option | Assessment |
|---|---|
| Non-null FKs plus invented UNKNOWN rows | Invents a reference value that the source never sends and that could be confused with the real source value `Other` |
| Nullable FKs with an ordinary UNIQUE | Unsafe: by default PostgreSQL treats NULLs as distinct, so two rows with NULL `variety_id` and otherwise equal values would both be accepted (VERIFIED TECHNICAL FACT, PostgreSQL 16 §5.4.3) |
| **Nullable FKs with `UNIQUE NULLS NOT DISTINCT`** | **Chosen.** NULL means "the source did not report it"; NULLs compare equal for uniqueness, so true duplicates are rejected (VERIFIED TECHNICAL FACT, PostgreSQL 16 §11.6) |

No captured record lacks a variety or grade (evidence §19.7), so NULLs are a defensive case. Empty strings are normalized to NULL.

The resulting constraints on `price_observations`:

1. `uq_price_observations_business_key` UNIQUE (`business_key`) — required by §27, §31 RULE 6 and FR-04, and the `ON CONFLICT` arbiter of §14–§15.
2. `uq_price_observations_identity` UNIQUE NULLS NOT DISTINCT (`market_id`, `commodity_id`, `report_date`, `variety_id`, `grade_id`, `source_dataset_id`) — enforces the same identity from the resolved IDs, so a bug in hash computation cannot let a duplicate through ("the DB is the last line of defense", §31 RULE 6). Its column order also serves the latest-price and trend queries (section 18).

### 10.3 Business key (ARCHITECTURAL DECISION)

`business_key` = lowercase hexadecimal SHA-256 (64 characters) of the six identity components **as resolved IDs**: `source_dataset_id`, `market_id`, `commodity_id`, `variety_id` (empty when NULL), `grade_id` (empty when NULL), `report_date` (ISO `yyyy-MM-dd`), joined with a fixed delimiter. The exact serialization belongs to the ingestion contract (`contracts/price-observation.schema.json`, §41).

- The spec lists "canonical market + canonical commodity + variety + grade + report_date + source_dataset_id" (§14, §47 Q8) and already uses an ID for the dataset. Hashing the resolved canonical IDs keeps the key stable when a display name is corrected and makes it equivalent to constraint 2.
- It is deterministic and idempotent within one database (§47 Q8). Keys differ between environments, which is acceptable because they are internal.

### 10.4 Dates

| Aspect | Design |
|---|---|
| Storage | `report_date` DATE: the date the observation refers to. Never VARCHAR, never overwritten |
| Source field | `Arrival_Date` (historical) / `arrival_date` (current), mapped explicitly per source adapter (evidence §19.3–§19.4); JSON field names are never lowercased globally |
| Observed format (VERIFIED OBSERVATION) | Every record in both datasets uses `dd/MM/yyyy` (JSON-escaped as `13\/09\/2026`); observed values `01/08/2026`, `07/09/2026`, `13/09/2026`, `14/09/2026` |
| Metadata inconsistency | Historical metadata declares the `Arrival_Date` filter format `dd-MM-yyyy` (`field_exposed`) and the field type `date`; records use slashes. The current metadata exposes no date filter |
| Parsing | Strict, source-specific `dd/MM/yyyy` (ADR-0007, departure D-06). No lenient multi-format guessing: a parser accepting both day-first and month-first would silently read `01/08/2026` as 8 January. §15's "MM/dd/yyyy" contradicts the observed records (DB-C-1, resolved) |
| Malformed dates | Record rejected: an `ingestion_errors` row with `error_type` `VALIDATION`, the field and reason, and the raw payload (§14 "valid date format", §15) |
| Future-dated records | Rejected when `report_date` is after today in IST (`DATE_IN_FUTURE`, ADR-0007) |
| Stability | The captures do not guarantee format stability. A changed format is rejected record by record (`DATE_FORMAT_INVALID`), and structural changes fail the page as SCHEMA_ERROR (§37; ADR-0011). Production verification is required |

### 10.5 Prices

- **Exact decimals.** `modal_price`, `min_price` and `max_price` are `numeric(14,2)`. Java parses the JSON number text straight to `BigDecimal` (evidence §19.4), never through `double`. No floating-point column exists.
- **Rounding (ADR-0008).**
  - Observed raw literals carry binary-floating artifacts (VERIFIED OBSERVATION): `3963.4099999999999` and `4144.0699999999997` (H-07SEP-2026), and `3210.1100000000001` (C-647-P0).
  - Ingestion rounds half-up to 2 dp before storage, giving 3963.41, 4144.07 and 3210.11, the values the evidence file itself reports. PostgreSQL would also round on input (VERIFIED TECHNICAL FACT, PostgreSQL 16 §8.1.2), but the rule is applied explicitly.
- **Unit (ADR-0008).**
  - The payloads carry no unit field (evidence §0.8), and none is invented.
  - The unit is verified per commodity from official DMI evidence: every one of the 2,732 rows in the Agmarknet 2.0 report exports states `Rs./Quintal`, and the Market-wise dashboard states "Price (Rs./Quintal)" for its 27 commodities, including Onion, Potato and Tomato.
  - `price_unit` on each observation is copied from `commodities.price_unit` at load time.
  - No conversion is encoded anywhere: AgriNex never multiplies or divides a source price.
- **Validation before load (ADR-0008, rules V1–V8).** Each failure is rejected with the raw payload kept in `ingestion_errors` (§37 "Reject record, log validation error"):
  - **V1:** required fields;
  - **V2:** the `dd/MM/yyyy` date, not in the future;
  - **V3:** known commodity with a verified unit;
  - **V4:** resolvable district;
  - **V5:** numeric and positive prices. An absent min or max is NULL, and a zero is rejected, never read as missing;
  - **V6:** min ≤ modal ≤ max;
  - **V7:** modal ≥ 100.00 when the unit is `Rs./Quintal`, which catches per-kg entries such as `Onion Green` 4/12/20 at `APMC Pune` or `Methi(Leaves)` 10/13/15 at `Pune(Moshi)` (evidence, 14/09/2026);
  - **V8:** in a delivery unit, for every commodity and date with at least five records, a modal more than 10× or less than 0.1× the group median is rejected as `PRICE_OUTLIER`.
- **Database checks** (defense in depth):
  - `modal_price > 0` (§14, §15);
  - `min_price` and `max_price` > 0 when present;
  - `min_price ≤ modal_price ≤ max_price` when present;
  - the `Rs./Quintal` floor (V7).

  All 505 captured records satisfy the first three (VERIFIED OBSERVATION).

### 10.6 Freshness

- `report_date` answers "which date does the source observation describe?"; `ingested_at` answers "when were the current values written?" (evidence §20.5; ADR-0009). Both are on every row (§10 "Freshness metadata on every observation").
- **Freshness status is derived, not stored on observations:** FRESH, RECENT, STALE or UNAVAILABLE, from `report_date`, today and the active `recommendation_weights` thresholds (defaults 1/4/10, §16). A stored status would go stale the next day.
- **Decisions keep a snapshot.** `selling_options.freshness_status` records the status at run time, so a recommendation can be explained later.
- **"Today" is the IST date** (`Asia/Kolkata`), computed explicitly by the application (ADR-0007).
- `report_date` is part of the identity, so it is never overwritten. An identical re-delivery writes nothing; changed prices update `ingested_at` and leave a revision (ADR-0009).

### 10.7 Duplicates and conflicting records

Four situations occur, each with a decided outcome (ADR-0009):

| Situation | Evidence | Database outcome |
|---|---|---|
| Exact re-delivery (same key, same prices) | Running ingestion twice (§48) | Nothing written; counted in `duplicates_skipped` |
| Alias twins (same observation under two market spellings) | 47 candidate groups in H-01AUG-2026-P0 | Before an admin merge, the spellings are different PROVISIONAL markets with separate rows. After the merge, the twins collapse under the identity rules (section 9.4) |
| **Same key, different prices in one delivery** | H-07SEP-2026, all Onion / Unhali / Local / 07/09/2026: `APMC Manmad` (1000/4200/4545 and 1330/4400/4720), `APMC Yeola` (801/4500/5000 and 801/4350/4850), `Pimpalgaon Baswant(Saykheda)` (2000/4101/4611 and 200/4150/4611), `APMC Nampur` (700/4600/5205 and 715/4600/5250) | Quarantined: no row stored. Every variant is logged as CONFLICT `SAME_KEY_DIFFERENT_PRICES` with its raw payload and counted in `records_conflicted` |
| Stored key, different prices from a later run | Source revisions (§49 "revised") | The observation is updated (the newer run wins, §14), and the replaced values are kept in `price_observation_revisions`; counted in `records_updated` |

`INSERT … ON CONFLICT DO UPDATE` refuses to affect the same row twice in one statement (a cardinality violation), and requires that proposed rows not duplicate each other on the arbiter (VERIFIED TECHNICAL FACT, PostgreSQL 16 `INSERT`, fetched 2026-10-03). Ingestion therefore resolves duplicates and conflicts for the whole delivery unit before the load statement (ADR-0011).

### 10.8 Current vs historical

`source_dataset_id` is part of the identity, so the same market, commodity, variety, grade and date can exist once per dataset. The two datasets are never merged into an ambiguous record type. Fifteen Maharashtra market strings appear in both datasets' captures, although on different dates.

Calculations read both datasets (ADR-0010):

- for the same identity, the current-dataset row takes precedence;
- without a variety or grade filter, the lower-median observation of the latest date is used, and it is always a real observation;
- its observation ID, variety, grade, date, dataset and unit are disclosed.

### 10.9 Evidence from the CSV files in `docs/data/raw/` (profiled 2026-10-03)


**Official Agmarknet 2.0 report exports** (VERIFIED OBSERVATION). Five "Commodity-wise, Market-wise Daily Report (Weighted Average)" files for 01-Oct-2026:

| Measure | Value |
|---|---|
| Market rows | 2,732 |
| Coverage | 41 commodity headings in 25 states, in five commodity groups (Beverages, Cereals, Drug and Narcotics, Dry Fruits, Fibre Crops) |
| Units | Every row has "Unit of Price" = `Rs./Quintal` and "Unit of Arrivals" = `Metric Tonnes` |
| Maharashtra | 295 rows, 11 commodity headings, 123 normalized markets (114 raw names with edge or doubled spaces), grades `FAQ`, `Local` and `Non-FAQ` |
| Price shape | No violation of min ≤ modal ≤ max; no non-positive price; minimum modal 600; no repeated identity (date, commodity, state, market, variety, grade) |
| Relative spread | In the 72 commodity-and-state groups with at least five rows, no modal lies outside 0.1×–10× of the group median |
| Totals | Every state "Total" row equals the arrival-weighted average of its rows' modal prices (142 of 142), as the report's note defines |

Report-only formatting:

- some commodity headings carry an MSP annotation, for example `Cotton (MSP: Rs. 8267.00/Quintal)`;
- Total and Grand Total rows are weighted averages.

Neither appears in the data.gov.in payloads, and neither is stored.

**Official Marketwise Price & Arrival Report** (01-10-2026). Fifteen cereal, oil-seed and pulse commodities, under the headers "Price (Rs./Quintal)", "Arrival (Metric Tonnes)" and "MSP (Rs./Quintal) 2026-27".

**`Agriculture_price_dataset.csv`** (unknown provenance; validation evidence only, ADR-0005):

| Measure | Value |
|---|---|
| Rows | 737,392 |
| Columns | STATE, District Name, Market Name, Commodity, Variety, Grade, Min_Price, Max_Price, Modal_Price, Price Date |
| States | 29 state strings, including variant spellings (`Chattisgarh` / `Chhattisgarh`, `Tamilnadu` / `Tamil Nadu`, `Gao`) |
| Commodities | 5 (Onion, Potato, Rice, Tomato, Wheat) |
| Dates | 737 distinct `M/D/YYYY` strings from 2023-06-06 to 2025-06-11. 297,181 rows would be ambiguous under an unknown day/month order |
| Maharashtra | 51,816 rows, 730 dates, 206 markets, 26 districts under old names (for example `ahmednagar`, `aurangabad`) |
| Duplicates | No duplicate identity rows |
| Anomalies | 1,498 rows with 0 < modal < 100 (mostly Potato and Onion in per-kg ranges); 1,860 rows violating min ≤ modal ≤ max (mostly a max of 0); 241 rows outside 0.1×–10× of their commodity, state and date median |

These anomalies are exactly what rules V5–V8 reject. The file is never loaded.

## 11. Ingestion persistence

### 11.1 Tables

The ingestion module owns three tables:

- `ingestion_runs`: one row per run;
- `ingestion_errors`: one row per rejected record, conflict variant or failure, with an `error_code`;
- `ingestion_source_settings`: the per-dataset pause switch.

`source_datasets` (owned by price) identifies the dataset. Observations and their revisions are written only through the price module (ADR-0013).

### 11.2 Historical bootstrap and backfill (FR-02, §14)

| Step | Database effect |
|---|---|
| Date range chosen (manual trigger, at most 366 days; scheduled trailing window today − 3 to today) | `ingestion_runs` row with `status` `RUNNING`, `triggered_by`, `request_scope` (state, date range, page size) and `started_at`. At most one RUNNING run per dataset (`uq_ingestion_runs_one_running`, ADR-0011) |
| Per report date (one delivery unit): request and paginate | `source_total_records` from the envelope `total`; `pages_fetched` per page. After retries, a failed page adds to `pages_failed` and writes an `API_ERROR` or `SCHEMA_ERROR` row with `page_offset` and `request_report_date` (§22, §29). The offset advances by the records actually returned (evidence §0.5) |
| Normalize | No write (section 9.2 rule) |
| Validate (V1–V8) | A rejected record writes a `VALIDATION` row with `error_code`, the reason and the raw payload, and adds 1 to `validation_errors_count` |
| Resolve reference entities | District through `district_source_aliases`; market through `market_source_aliases` (new spellings register PROVISIONAL markets); commodity by exact match, then through `commodity_source_aliases` (ADR-0051); varieties and grades registered when new |
| Deduplicate and resolve conflicts | Across the whole delivery unit (section 10.7) |
| Load | Through the price module, in one transaction per delivery unit. Each record counts as inserted, updated (with a revision), duplicate or conflicted |
| Audit | `finished_at` and the terminal `status` (ADR-0011). Alert evaluation runs after commit when the run changed observations (ADR-0019) |

The evidence's 103-record capture returned 100 records on page 0. A run must request offset 100, and must not record 103 records fetched after page 0 (evidence §21 and Appendix B).

### 11.3 Current synchronization (FR-01, §15)

Same tables and counters. Specific points (ADR-0011):

- `triggered_by` is `SCHEDULER` every 6 hours (00:00, 06:00, 12:00, 18:00 IST), `MANUAL` for admin triggers, and `STARTUP` at boot when no current run has succeeded in the last 6 hours.
- The whole run is one delivery unit. A page that still fails after retries ends fetching, and the pages already fetched are loaded.
- **Exact filtering after retrieval:** records outside coverage (another state) are counted in `records_out_of_scope`, not as errors (evidence §0.11, §20.3).
- A response that is not the expected JSON (an HTML maintenance page, HTTP 403 or 5xx, or a missing records array) is a page failure, never "zero records" (ADR-0005).
- After the run commits, alert evaluation runs against the observations the run inserted or revised (§15, §34; ADR-0019), found through `ix_price_observations_ingestion_run_id` and `ix_price_observation_revisions_run`.
- At startup, any run still RUNNING is marked FAILED (`RUN_INTERRUPTED`).

### 11.4 Counter reconciliation (ARCHITECTURAL DECISION)

For a run ending SUCCESS or PARTIAL_FAILURE, every fetched record lands in exactly one bucket (ADR-0012):

```text
records_fetched = records_inserted + records_updated + duplicates_skipped
                + validation_errors_count + records_conflicted + records_out_of_scope
```

- **Enforcement.** The CHECK `ck_ingestion_runs_accounting` enforces this identity when the run is finalized. Counters may be partial while RUNNING, and a FAILED run keeps the counts it reached.
- **Checked against database rows** (§48 "exact match"):
  - `records_inserted` = observations with that `ingestion_run_id`;
  - `records_updated` = revisions with that run;
  - `validation_errors_count` and `records_conflicted` = that run's `ingestion_errors` of types VALIDATION and CONFLICT.
- **Completeness** = (`records_inserted` + `records_updated` + `duplicates_skipped`) ÷ (`records_fetched` − `records_out_of_scope`). The §48 threshold is at least 95% per run (ADR-0012).

### 11.5 Security of audit data

`request_scope`, `error_message` and `raw_payload` never contain the API key (§28, §47 Q21). Request URLs, which carry `api-key=…`, are never stored. A CHECK rejects `request_scope` objects with the top-level keys `api-key`, `api_key` or `apikey` as defense in depth (`?|` operator: VERIFIED TECHNICAL FACT, PostgreSQL 16 §9.16, fetched 2026-10-03).

### 11.6 Decisions applied

None remain. The ingestion design applies:

- ADR-0005: source and outage behavior;
- ADR-0006: coverage and known list;
- ADR-0007: dates and time zone;
- ADR-0008: unit and validation;
- ADR-0009: identity, conflicts and revisions;
- ADR-0011: orchestration;
- ADR-0012: counters;
- ADR-0014: district and market resolution;
- ADR-0020: pause switch.

## 12. Recommendation persistence

The chain Task 7 asks for maps onto the tables like this:

| Element | Where it is persisted |
|---|---|
| Farmer input | `selling_searches`: commodity, optional variety and grade, quantity and unit, origin (district centroid), radius |
| Candidate market | `selling_options.market_id` |
| Price observation | `selling_options.price_observation_id` (reference, not a copy) |
| Distance | `selling_options.distance_km` |
| Transport assumption | `recommendation_runs.transport_id` → the exact `transport_assumptions` version |
| Estimated transport cost | `selling_options.estimated_transport_cost` |
| Estimated gross value and net realization | `selling_options.estimated_gross_value`, `estimated_net_realization` |
| Ranking and reasons | `selling_options.rank`, `score`, `trend_direction`, `freshness_status`, `reason_codes`; winners and `rankings_disagree` on `recommendation_runs` |
| Scoring configuration | `recommendation_runs.weights_id` → the exact `recommendation_weights` version |

- **No formulas in the database.** Transport, gross, net, normalization, scores and reasons are computed in code (§16, §32) and covered by deterministic unit tests (§27). The database stores results and enforces only vocabulary and consistency: allowed `trend_direction`, `freshness_status` and `reason_codes` values, ranks unique within a run, and `rankings_disagree` equal to "the two winners differ", which is §16's own definition.
- **Configuration versioning (ARCHITECTURAL DECISION).** An admin change inserts a new `transport_assumptions` or `recommendation_weights` row and deactivates the previous one in the same transaction; rows referenced by runs are never updated. A partial unique index allows at most one active row per table, so "load active" (§16) is deterministic (VERIFIED TECHNICAL FACT: partial unique indexes, PostgreSQL 16 §11.8).
- **Anonymous comparisons** (F1.1, §28) store `farmer_profile_id` NULL. BUYER and ADMIN callers without a farmer profile are stored the same way.
- **Variety and grade selection** are stored when made (F1.4, §16 input). Without a selection, the lower-median representative observation is used, and the chosen observation is always disclosed (ADR-0010).
- **Excluded markets** (`NO_RECENT_PRICE_DATA`) are returned in the response but not persisted: `selling_options` holds eligible, ranked candidates only, so `price_observation_id` and `rank` are NOT NULL (ADR-0023).
- **No eligible option.** The search and the run are persisted, with both winners NULL and `rankings_disagree` FALSE (ADR-0023).
- **Not persisted:** What-If overrides, which are "session-only" (§32), client-side simulation (§17), and the server-side simulate call (ADR-0026).
- **Auditability.** For any run the database can show the input, the configuration versions, each candidate's observation (with dataset, run and `ingested_at`), the computed estimates, the snapshot freshness and the reasons (Task 7 auditability; §36 Experiment 1).

## 13. Alert persistence

- `price_alerts` holds the four §34 alert types for a commodity and an optional market (§25 "marketId optional"). Each alert has a threshold (prices in `Rs./Quintal`, ADR-0008), a status of `ACTIVE` (the default on creation, §34) or `PAUSED`, and `last_triggered_at`.
- **Deletion semantics** (§34): a farmer delete is a real `DELETE`, cascading to `alert_events` (`ON DELETE CASCADE`). Pausing is a status change. Viewing or dismissing an event leaves the alert ACTIVE.
- **Events (ADR-0019).** `alert_events` stores:
  - the trigger value, which can be negative for a percentage alert, so there is no sign check;
  - the run, market and observation that met the condition;
  - `read_at`, set when the farmer views or dismisses the event.

  `uq_alert_events_alert_run` allows at most one event per alert per run, so re-evaluation is idempotent.
- **Delivery** is in-app polling only. There is no push channel, no email and no notification table (ADR-0019).
- **Evaluation path.** After every run that inserted or revised observations, the ingestion module invokes evaluation through the alert module, and the alert module reads prices through the price module (ADR-0013). The partial index `ix_price_alerts_active_commodity` serves "ACTIVE alerts for the commodities just ingested" (§22).
- **Alert variety and grade:** §20 has none, so alerts use the representative observation (ADR-0010).
- **ADMIN-owned alerts** (§28): an ADMIN gets a `farmer_profiles` row on first need, so `price_alerts.farmer_profile_id` keeps its §20 meaning (ADR-0016).

## 14. User/auth persistence

- **SSOT fields plus audit timestamps.**
  - `users` has the §20 fields (`phone_number` UK, `email` UK, `password_hash`, `role`, `is_active`) plus `created_at` (ADR-0016) and the email-verification and password-reset columns (`email_verified_at`, `email_verification_token_hash`, `email_verification_expires_at`, `password_reset_token_hash`, `password_reset_expires_at`; ADR-0048).
  - `farmer_profiles` has exactly the §20 fields.
  - No identity-provider, OAuth, session, role or permission tables exist; §28 defines three roles, held in `users.role`.
- **Identifiers (ADR-0016).**
  - FARMER: an Indian mobile number in E.164 form (`+91` and 10 digits starting 6–9) and an email stored in lower case; either one signs in to the same account (ADR-0047).
  - ADMIN: an email stored in lower case.
  - CHECK constraints enforce presence by role and the formats. Login takes one normalized `identifier`.
- **`refresh_tokens`** (§28: "SHA-256 hashed, stored in DB, 7-day TTL"; §27: rotation).
  - Columns: `user_id`, `token_hash`, `expires_at`, `family_id`, `created_at` and `revoked_at`.
  - Rotation and logout revoke rows; they do not delete them.
  - Presenting a revoked token revokes its whole family (RFC 9700 §4.14.2; ADR-0016).
- **Credentials.**
  - Passwords are stored only as BCrypt hashes, cost 12 (§28). Lengths: FARMER 8–64 characters, ADMIN 15–64, never more than 72 UTF-8 bytes.
  - Refresh tokens, email-verification tokens and password-reset tokens are stored only as SHA-256 hashes. An account has at most one active verification token and one active reset token; a new one replaces the old (ADR-0048).
  - Neither is ever returned by an API or logged.
  - This area is security-sensitive and needs explicit human review before merge (CLAUDE.md rule 16). Browser-side token handling is ADR-0017.
- **Location privacy.** No address is stored anywhere (§28). The only location data linked to a person are the district-level `default_district_id` and the district-centroid origin in `selling_searches`.

## 15. ML persistence

- **Optional.** `forecasts` exists only if ML proceeds (P9, HARD RULE 2, §35 criteria). Phase 1 migrations do not include it.
- **Shape.** One row per forecast point, carrying the §35 output fields:
  - market, commodity, horizon (3 or 7 days) and forecast date;
  - predicted price, and lower and upper bounds (nullable, because a naive baseline produces none);
  - model name and version, MAE and RMSE;
  - `training_data_through` and `generated_at`.

  CHECKs keep the bounds around the prediction, and the forecast dates after the training cutoff (§35 chronological validation, §47 Q14).
- **Ownership and access (ADR-0030).**
  - The table belongs to the price module.
  - The ML job reads a daily-series view (the representative observation per date, ADR-0010) and writes forecasts through a dedicated, restricted database role.
  - The backend never calls the ML service synchronously.
- **Availability semantics.** With no forecast dated today or later for a market and commodity, the response is `ForecastResponse { available: false }` (§29, §35).
  - Nothing in comparison, recommendation, transport or alerts reads `forecasts`, so its absence or staleness cannot cause a 5xx response or change a ranking (§16 has no forecast term).
  - Rows are deleted 90 days after their forecast date (ADR-0029).

## 16. Data ownership

Every table has exactly one owner (ADR-0013). The full table, with reasons and access patterns, is [TABLE_OWNERSHIP.md](TABLE_OWNERSHIP.md).

| Module | Owns |
|---|---|
| geography | `states`, `districts`, `district_source_aliases`, `localities` |
| commodity | `commodities`, `varieties`, `grades`, `commodity_source_aliases` |
| price | `source_datasets`, `markets`, `market_source_aliases`, `price_observations`, `price_observation_revisions`, and `forecasts` (optional) |
| ingestion | `ingestion_runs`, `ingestion_errors`, `ingestion_source_settings` |
| auth | `users`, `farmer_profiles`, `refresh_tokens` |
| transport | `transport_assumptions` |
| recommendation | `recommendation_weights`, `selling_searches`, `recommendation_runs`, `selling_options` |
| alert | `price_alerts`, `alert_events` |
| admin | `admin_audit_events` |

How this resolves CONFLICT C-02 (ADR-0013):

- **§15 and §22 describe the data effects of a run.** They show `IngestionService` upserting `price_observations`, selecting `price_alerts` and inserting `alert_events`. The writes happen through the price and alert module interfaces, not by ingestion code touching those tables (§27).
- **The access paths use approved dependencies.** ADR-0013 approved the module dependencies these paths need, in addition to the four in §13. The graph is acyclic. They are listed in [TABLE_OWNERSHIP.md §4](TABLE_OWNERSHIP.md#4-module-dependencies-required-by-this-ownership).
- **Tooling outside the module rule** (approved with the ownership):
  - Flyway migrations, including the reference seed;
  - demo-profile fixtures;
  - offline evaluation SQL (`scripts/run_evaluation.sql`, §43; read-only);
  - the optional ML job's restricted role.

  None of these is backend module code.

## 17. Integrity rules

The constraint catalogue is [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md). The rules, by kind:

| Rule | How |
|---|---|
| Referential integrity | 45 foreign keys. `RESTRICT` by default, between independent objects. `CASCADE` where the child is part of the parent (`ingestion_errors`, `alert_events` (§34), `recommendation_runs`, `selling_options`) or is erased with an account (`farmer_profiles`, `refresh_tokens`, `price_alerts`). `SET NULL` on `selling_searches.farmer_profile_id` (ADR-0029) |
| Observation uniqueness | Business key UNIQUE (§27) plus the null-safe identity constraint (section 10.2). One revision per observation per run |
| Run uniqueness | At most one RUNNING run per dataset; at most one alert event per alert per run |
| Natural keys | Unique names within their parent (districts per state, localities per district, varieties and grades per commodity); unique LGD codes; unique source alias triples and district alias pairs; one dataset per role; one option per market and per rank in a run |
| Scoped reference integrity | A variety or grade must belong to the referencing row's commodity (composite FKs, section 8) |
| Normalized text | Whitespace-normalization CHECKs (section 9.2) |
| Prices | Positive modal price (§14); positive min and max when present; min ≤ modal ≤ max; the `Rs./Quintal` floor (section 10.5) |
| Controlled vocabularies | CHECK lists for: roles, locales, quantity units, dataset types, market review statuses, run triggers and statuses, error types and the code format, alert types and statuses, trend directions, freshness statuses, reason codes, admin audit actions and forecast horizons. Each list comes from the spec or a cited decision |
| Coordinates | Valid ranges. Coordinates and their provenance present together. The approximation flag only with coordinates |
| Configuration | At most one active row each for weights and transport. Non-negative values; weights at most 1. Ordered freshness thresholds. Positive vehicle capacity (a divisor in §32). A pause always carries its reason |
| Derived consistency | Both winners present or both absent; `rankings_disagree` matches the winners; MERGED markets name another market and are inactive; the ingestion accounting invariant |
| Accounts | Identifier presence by role; E.164 phone and lower-case email formats; valid token timelines |
| Credentials hygiene | Hex SHA-256 format for `business_key` and `token_hash`; no credential keys in `request_scope` or in admin audit details |

**Kept in ingestion validation, not in the database.** These are rules the source may legitimately break, or that a CHECK cannot express:

- the date format and calendar validity before conversion to DATE (section 10.4), and future dates. CHECK conditions must be immutable, so `CURRENT_DATE` cannot be used (VERIFIED TECHNICAL FACT, PostgreSQL 16 §5.4.1);
- unit verification, rounding and the relative outlier rule V8 (ADR-0008);
- required source fields and their types (evidence §19.7);
- unknown commodities and districts, which are rejected; new markets, varieties and grades, which are registered (ADR-0006, ADR-0014);
- within-delivery duplicates and conflicts (ADR-0009);
- cross-row rules: a winner must be one of the run's options; a FARMER needs a profile. CHECKs cannot reference other rows or tables (VERIFIED TECHNICAL FACT, PostgreSQL 16 §5.4.1).

## 18. Indexing strategy

- **Query-driven.** Every secondary index serves a named query pattern from the spec. PostgreSQL does not index foreign-key columns automatically (VERIFIED TECHNICAL FACT, PostgreSQL 16 §5.4.5), so FK columns get an index only where a query or a cascade path needs one.
- **One index, several jobs.** `uq_price_observations_identity` leads with (`market_id`, `commodity_id`, `report_date`): equality on the first two columns plus a backward scan on `report_date` gives "latest price by market and commodity", and a range on `report_date` gives the trend window (B-tree backward scans satisfy `ORDER BY … DESC`; leading-column rule: VERIFIED TECHNICAL FACT, PostgreSQL 16 §11.3–§11.4). A separate `(market_id, report_date)` or `DESC` index would be redundant.
- **Small tables are scanned.** Comparison candidates are the VERIFIED, active markets within the radius. Maharashtra markets number in the hundreds (the official master lists 429; the captures show 131 distinct strings), so a sequential scan plus haversine is cheaper than any index, and none is added.
- **Totals:** 47 indexes besides primary keys. Most come from unique constraints; 13 are plain secondary indexes, one of which (`ix_selling_searches_farmer_profile`) is not needed in Phase 1. Patterns, queries and Phase 1 status for each: [INDEXES_AND_CONSTRAINTS.md §5](INDEXES_AND_CONSTRAINTS.md#5-indexes).

## 19. Data lifecycle

Retention follows ADR-0029. Purges run as daily scheduled jobs in the owning modules.

| Record class | Tables | Deletion behavior | Retention |
|---|---|---|---|
| Historical facts | `price_observations`, `price_observation_revisions` | Never deleted by the application; `RESTRICT` from decision records and events. Prices change only through a recorded revision (ADR-0009) | Indefinite |
| Reference data | `states`, `districts`, `district_source_aliases`, `localities`, `markets`, `market_source_aliases`, `commodities`, `commodity_source_aliases`, `varieties`, `grades`, `source_datasets` | Not deleted while referenced (`RESTRICT`). Markets are withdrawn with `is_active` (§20) or merged (ADR-0014) | Indefinite |
| Mutable configuration | `transport_assumptions`, `recommendation_weights` (versioned), `ingestion_source_settings` | New version inserted and the old one deactivated; referenced versions are never updated or deleted | Indefinite |
| User-owned data | `users`, `farmer_profiles`, `price_alerts` | Accounts are deactivated with `is_active` (§20). Erasure deletes the user, with CASCADE to profile, alerts, events and tokens. ADMIN accounts are only deactivated. Alerts are deleted by the farmer, with cascade (§34) | Until erasure (DPDP Act 2023 §8(7), §12; ADR-0029) |
| Security and session data | `refresh_tokens` | Revoked on rotation and logout; cascade with the account | Deleted one day after expiry |
| Security and session data | `users` email-verification and password-reset columns | Cleared when used or replaced; deleted with the account | Expired values cleared by the auth module's daily purge (ADR-0048) |
| Audit data | `ingestion_runs`, `admin_audit_events` | Append-only | Indefinite |
| Audit data | `ingestion_errors` | Append-only; cascades with its run | 180 days |
| Derived data | `selling_searches`, `recommendation_runs`, `selling_options` | Decision records cascade from search to run to options. On erasure, a search's profile reference is set to NULL | Indefinite (Experiment 1) |
| Derived data | `alert_events` | Cascade with the alert | 180 days |
| Derived data | `forecasts` | Optional | 90 days after the forecast date |

## 20. Decisions and assumptions

Every database question raised by Task 6 and Task 7 is decided in [DECISION_LOG.md](../architecture/DECISION_LOG.md). The map from items to decisions is in [DATABASE_INDEX.md §8](DATABASE_INDEX.md#8-decisions-applied-and-remaining-open-items).

### 20.1 Decision-log entries and their effect on the database

| Decision | Database effect |
|---|---|
| ADR-0002 | PostgreSQL 16.15; Flyway 12.4.0 |
| ADR-0006 | Commodity known list from the official master; `category` vocabulary; `states.code` = `IN-MH`; dataset types; coverage as configuration |
| ADR-0007 | `dd/MM/yyyy` parsing; IST "today"; future dates rejected |
| ADR-0008 | `commodities.price_unit` / `price_unit_source`; unit copied to observations; validation rules V1–V8; the floor CHECK |
| ADR-0009 | `price_observation_revisions`; CONFLICT quarantine; column meanings of `ingestion_run_id` and `ingested_at` |
| ADR-0010 | No schema change: selection rules for queries |
| ADR-0011 | One RUNNING run per dataset; error types, `error_code` and `request_report_date` |
| ADR-0012 | `records_conflicted`; the accounting invariant CHECK |
| ADR-0013 | Ownership and access paths (section 16) |
| ADR-0014 | LGD codes; `district_source_aliases`; market `review_status`, `merged_into_market_id` and `created_at`; alias `evidence_note`; slug rule |
| ADR-0015 | Curated `name_hi` / `name_mr` |
| ADR-0016 | Account CHECKs and `created_at`; refresh-token families |
| ADR-0018 | None: Redis holds no system-of-record data |
| ADR-0019 | `alert_events` run, market, observation and `read_at`; one event per alert per run |
| ADR-0020 | `ingestion_source_settings`; `admin_audit_events` |
| ADR-0021 | Flyway reference seed at first boot; named volume; daily dump |
| ADR-0023 | Option-less runs persisted; excluded markets never persisted |
| ADR-0024 | Weights seed values |
| ADR-0025 | `INSUFFICIENT_DATA` stays an allowed trend direction |
| ADR-0028 | Demo fixtures and test accounts only with the demo profile |
| ADR-0029 | Retention periods; CASCADE and SET NULL for erasure |
| ADR-0030 | `forecasts` owned by price; the ML role and the daily-series view |
| ADR-0031, ADR-0032 | Phase 2 placements decided; state model and vendor items deferred |

### 20.2 Items registered by Task 7

The items Task 7 registered (RD-027 to RD-036) are resolved:

| Item | Decision |
|---|---|
| RD-027 | ADR-0008 |
| RD-028 | ADR-0009 |
| RD-029 | ADR-0012 |
| RD-030, RD-032 | ADR-0014 |
| RD-031 | ADR-0006 |
| RD-033 | ADR-0015 |
| RD-034 | ADR-0016 |
| RD-035 | ADR-0029 |
| RD-036 | ADR-0007 |

The items still open in `docs/REQUIRES_DECISION.md` (RD-002, RD-012, RD-025, RD-037) have no Phase 1 schema impact.

### 20.3 Assumptions

| ID | Assumption | Why it was unavoidable | Reversible? |
|---|---|---|---|
| A-DB-01 | C-314-P0 (`total` 314) is the full Maharashtra current dataset for 14/09/2026 | The capture's filter string was not retained (evidence §0.11). Used only for the volume estimate in section 5 | Yes. Only an estimate depends on it |
| A-DB-02 | Coordinates are decimal degrees on the WGS84 datum | Haversine (§16) needs angular coordinates, and the spec names no datum. If the Survey of India dataset uses another datum or a projection, it is transformed to WGS84, with the transformation recorded in `centroid_source`, before centroids are computed (ADR-0014) | Yes |

Conflicts found by Task 7 and how they were resolved are listed in [DATABASE_INDEX.md §9](DATABASE_INDEX.md#9-conflicts).
