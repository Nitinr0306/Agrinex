# AgriNex — Database Index

**Status:** AUTHORITATIVE. Database architecture from Task 7 (2026-10-03), approved in the decision lock of the same day ([DECISION_LOG.md](../architecture/DECISION_LOG.md), ADR-0001 to ADR-0034).

- This is the hub of the database architecture set in `docs/database/`. The set designs the PostgreSQL schema from:
  - the master specification (`docs/01_AGRINEX_MASTER_PRODUCT_PHASES.md`);
  - the Task 6 architecture documents;
  - the verified government-data evidence (`docs/data/AGRINEX_VERIFIED_GOVERNMENT_API_DATA_REFERENCE.md`);
  - the official report exports in `docs/data/raw/`.
- Statements traced to the spec carry its authority. The set's ARCHITECTURAL DECISIONS were approved under the delegation recorded in ADR-0001.
- Where a document in this set and a binding decision-log entry differ, the entry governs.

> **Nothing in this set has been implemented:** no migration, table, entity, repository or seed file exists. Migrations may now be written from this design (CLAUDE.md §I). Each one names the tests it needs (CLAUDE.md §L). Seeding also needs the human prerequisites in section 7.

## 1. Database document map

| Document | Purpose | Status |
|---|---|---|
| [DATABASE_INDEX.md](DATABASE_INDEX.md) | Hub: map, ownership, dependencies, evidence, prerequisites, decisions, conflicts, audit, approval checklist | AUTHORITATIVE |
| [DATABASE_ARCHITECTURE.md](DATABASE_ARCHITECTURE.md) | Strategy and rationale: scope, phase boundaries, domains, reference data, observations, ingestion, recommendation, alerts, auth, ML, ownership, integrity, indexing, lifecycle, decisions | AUTHORITATIVE |
| [ERD.md](ERD.md) | Mermaid ER diagrams (high-level, Phase 1, optional ML, Phase 2 extension), relationships, cardinalities, keys, uniqueness | AUTHORITATIVE |
| [DATA_DICTIONARY.md](DATA_DICTIONARY.md) | Every table and column: type, nullability, default, keys, description, basis, source, sensitivity | AUTHORITATIVE |
| [TABLE_OWNERSHIP.md](TABLE_OWNERSHIP.md) | Owner module of every table, access patterns, prohibited direct access, approved module dependencies (ADR-0013) | AUTHORITATIVE |
| [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md) | Primary keys, foreign keys, unique constraints, business keys, CHECK constraints, indexes, query patterns | AUTHORITATIVE |
| [SEED_AND_REFERENCE_DATA.md](SEED_AND_REFERENCE_DATA.md) | Maharashtra reference data, the official commodity master and units, markets and aliases, source datasets, initial configuration | AUTHORITATIVE |

`ERD.md`, `DATA_DICTIONARY.md`, `INDEXES_AND_CONSTRAINTS.md` and the ownership table in `TABLE_OWNERSHIP.md` were generated from one table model and checked by script, so their tables, columns and keys agree. They were generated in Task 7 and regenerated for the decision lock. The model and generator are working files outside the repository, so any later change must update all four together.

## 2. Table and domain map

| Domain | Table | Owner module | Phase | Status |
|---|---|---|---|---|
| Geography reference | [`states`](DATA_DICTIONARY.md#states) | geography | Phase 1 | REQUIRED |
| Geography reference | [`districts`](DATA_DICTIONARY.md#districts) | geography | Phase 1 | REQUIRED |
| Geography reference | [`localities`](DATA_DICTIONARY.md#localities) | geography | Phase 1 | REQUIRED |
| Geography reference | [`district_source_aliases`](DATA_DICTIONARY.md#district_source_aliases) | geography | Phase 1 | REQUIRED |
| Commodity reference | [`commodities`](DATA_DICTIONARY.md#commodities) | commodity | Phase 1 | REQUIRED |
| Commodity reference | [`varieties`](DATA_DICTIONARY.md#varieties) | commodity | Phase 1 | REQUIRED |
| Commodity reference | [`grades`](DATA_DICTIONARY.md#grades) | commodity | Phase 1 | REQUIRED |
| Commodity reference | [`commodity_source_aliases`](DATA_DICTIONARY.md#commodity_source_aliases) | commodity | Phase 1 | REQUIRED |
| Government source and observations | [`source_datasets`](DATA_DICTIONARY.md#source_datasets) | price | Phase 1 | REQUIRED |
| Government source and observations | [`markets`](DATA_DICTIONARY.md#markets) | price | Phase 1 | REQUIRED |
| Government source and observations | [`market_source_aliases`](DATA_DICTIONARY.md#market_source_aliases) | price | Phase 1 | REQUIRED |
| Government source and observations | [`price_observations`](DATA_DICTIONARY.md#price_observations) | price | Phase 1 | REQUIRED |
| Government source and observations | [`price_observation_revisions`](DATA_DICTIONARY.md#price_observation_revisions) | price | Phase 1 | REQUIRED |
| Ingestion audit | [`ingestion_runs`](DATA_DICTIONARY.md#ingestion_runs) | ingestion | Phase 1 | REQUIRED |
| Ingestion audit | [`ingestion_errors`](DATA_DICTIONARY.md#ingestion_errors) | ingestion | Phase 1 | REQUIRED |
| Ingestion audit | [`ingestion_source_settings`](DATA_DICTIONARY.md#ingestion_source_settings) | ingestion | Phase 1 | REQUIRED |
| Identity and access | [`users`](DATA_DICTIONARY.md#users) | auth | Phase 1 | REQUIRED |
| Identity and access | [`farmer_profiles`](DATA_DICTIONARY.md#farmer_profiles) | auth | Phase 1 | REQUIRED |
| Identity and access | [`refresh_tokens`](DATA_DICTIONARY.md#refresh_tokens) | auth | Phase 1 | REQUIRED |
| Recommendation configuration | [`transport_assumptions`](DATA_DICTIONARY.md#transport_assumptions) | transport | Phase 1 | REQUIRED |
| Recommendation configuration | [`recommendation_weights`](DATA_DICTIONARY.md#recommendation_weights) | recommendation | Phase 1 | REQUIRED |
| Recommendation results | [`selling_searches`](DATA_DICTIONARY.md#selling_searches) | recommendation | Phase 1 | REQUIRED |
| Recommendation results | [`recommendation_runs`](DATA_DICTIONARY.md#recommendation_runs) | recommendation | Phase 1 | REQUIRED |
| Recommendation results | [`selling_options`](DATA_DICTIONARY.md#selling_options) | recommendation | Phase 1 | REQUIRED |
| Alerts | [`price_alerts`](DATA_DICTIONARY.md#price_alerts) | alert | Phase 1 | REQUIRED |
| Alerts | [`alert_events`](DATA_DICTIONARY.md#alert_events) | alert | Phase 1 | REQUIRED |
| Administration | [`admin_audit_events`](DATA_DICTIONARY.md#admin_audit_events) | admin | Phase 1 | REQUIRED |
| Optional ML | [`forecasts`](DATA_DICTIONARY.md#forecasts) | price | Optional (ML) | OPTIONAL |
| Phase 2 (deferred) | `produce_listings` | listing | PHASE 2 / DEFERRED | DEFERRED (columns not specified) |
| Phase 2 (deferred) | `bids` | bid | PHASE 2 / DEFERRED | DEFERRED (columns not specified) |
| Phase 2 (deferred) | `transactions` | transaction | PHASE 2 / DEFERRED | DEFERRED (columns not specified) |
| Phase 2 (deferred) | `transaction_state_history` | transaction | PHASE 2 / DEFERRED | DEFERRED (columns not specified) |
| Phase 2 (deferred) | `buyer_verifications` | auth | PHASE 2 / DEFERRED | DEFERRED (columns not specified) |
| Phase 2 (deferred) | `rating_storage_unnamed` | rating | PHASE 2 / DEFERRED | DEFERRED (columns not specified) |

## 3. Phase 1 vs Phase 2 map

| Scope | Tables | Count |
|---|---|---|
| Phase 1 (must stand alone, P8) | `states`, `districts`, `district_source_aliases`, `localities`, `commodities`, `varieties`, `grades`, `commodity_source_aliases`, `source_datasets`, `markets`, `market_source_aliases`, `price_observations`, `price_observation_revisions`, `ingestion_runs`, `ingestion_errors`, `ingestion_source_settings`, `users`, `farmer_profiles`, `refresh_tokens`, `transport_assumptions`, `recommendation_weights`, `selling_searches`, `recommendation_runs`, `selling_options`, `price_alerts`, `alert_events`, `admin_audit_events` | 27 |
| Optional (ML, only after HARD RULE 2 and the §35 criteria) | `forecasts` | 1 |
| PHASE 2 / DEFERRED (after the Phase 1 Freeze, HARD RULE 1) | `produce_listings`, `bids`, `transactions`, `transaction_state_history`, `buyer_verifications`, rating storage (unnamed in the spec) | 6 |

- No Phase 1 table references a Phase 2 or optional table.
- Phase 2 tables may reference `users`, `commodities` and `recommendation_runs`. Their columns are not designed: placements are decided in ADR-0031, and RD-012 and the rest of RD-025 are deferred by ADR-0032.
- The Task 12 closure added `commodity_source_aliases` (ADR-0051, migration V12).
- The decision lock added four Phase 1 tables to the 22 of Task 7 (ADR-0009, ADR-0014, ADR-0020):
  - `district_source_aliases`;
  - `price_observation_revisions`;
  - `ingestion_source_settings`;
  - `admin_audit_events`.

## 4. Module ownership

| Module | Owns (Phase 1) | Owns (optional / Phase 2) |
|---|---|---|
| auth | `users`, `farmer_profiles`, `refresh_tokens` | `buyer_verifications` (PHASE 2 / DEFERRED, ADR-0031) |
| geography | `states`, `districts`, `localities`, `district_source_aliases` | — |
| commodity | `commodities`, `varieties`, `grades`, `commodity_source_aliases` | — |
| price | `source_datasets`, `markets`, `market_source_aliases`, `price_observations`, `price_observation_revisions` | `forecasts` (optional ML, ADR-0030) |
| ingestion | `ingestion_runs`, `ingestion_errors`, `ingestion_source_settings` | — |
| transport | `transport_assumptions` | — |
| recommendation | `recommendation_weights`, `selling_searches`, `recommendation_runs`, `selling_options` | — |
| alert | `price_alerts`, `alert_events` | — |
| admin | `admin_audit_events` | — |
| listing (Phase 2) | — | `produce_listings` (PHASE 2 / DEFERRED) |
| bid (Phase 2) | — | `bids` (PHASE 2 / DEFERRED) |
| transaction (Phase 2) | — | `transactions`, `transaction_state_history` (PHASE 2 / DEFERRED) |
| rating (Phase 2) | — | `rating_storage_unnamed` (PHASE 2 / DEFERRED) |

Full table with reasons, access patterns and prohibited access: [TABLE_OWNERSHIP.md](TABLE_OWNERSHIP.md). ADR-0013 decided the ownership and the approved module dependencies ([TABLE_OWNERSHIP.md §4](TABLE_OWNERSHIP.md#4-module-dependencies-required-by-this-ownership)).

## 5. Dependency relationships

**Documents this set depends on:**

| Upstream | Used for |
|---|---|
| Master specification §1, §10, §12–§17, §20–§23, §25–§36, §41–§48 | Entities, fields, flows, rules, acceptance criteria |
| `docs/architecture/DECISION_LOG.md` (ADR-0001 to ADR-0048) | Every decision this set applies |
| `docs/architecture/COMPONENT_ARCHITECTURE.md` | Modules, dependencies, referenced tables |
| `docs/architecture/DATA_FLOW.md`, `INGESTION_ARCHITECTURE.md`, `RECOMMENDATION_ENGINE.md` | Flows the schema must support |
| `docs/architecture/SECURITY_ARCHITECTURE.md` | Credentials, roles, sensitive data |
| `docs/architecture/RESILIENCE_ARCHITECTURE.md`, `ML_ARCHITECTURE.md` | Failure semantics; optional forecasts |
| `docs/architecture/CURRENT_TECHNOLOGY_BASELINE.md` | PostgreSQL 16 (16.15), Flyway 12.4.0 (ADR-0002) |
| Evidence file, Appendix A; `docs/data/raw/` | Observed source shapes, values and units (section 6) |
| `docs/REQUIRES_DECISION.md` | Remaining open items (section 8) |

**Documents that depend on this set:**

- Task 8: `contracts/openapi.yaml`, `contracts/price-observation.schema.json`, `contracts/recommendation.schema.json`, `contracts/forecast-output.schema.json`;
- Task 9: the data available to screens;
- Task 10: the migration folder and scaffolding;
- role file 04 (`04_AgriNex_Backend_Phase1_API_Database.md`, §43);
- the backend, ingestion and recommendation implementation.

**Table dependency order** (foreign keys):

1. geography and commodity reference data;
2. source datasets and ingestion settings;
3. markets and aliases;
4. ingestion runs and errors;
5. price observations and revisions;
6. accounts;
7. configuration;
8. recommendation results, alerts and the admin audit.

The non-binding V1–V6 migration grouping is in [DATABASE_ARCHITECTURE.md §5](DATABASE_ARCHITECTURE.md#5-postgresql-role).

## 6. API-evidence dependencies

**Live data.gov.in verification is still unavailable.** On 2026-10-03 the data.gov.in resource pages answered HTTP 403, and the portal is under maintenance. Two official DMI sources were reachable and were used as corroborating evidence (ADR-0005):

- the state catalog page (`karnataka.data.gov.in`);
- Agmarknet 2.0 (`agmarknet.gov.in`): its public masters and Market-wise dashboard, and the owner-supplied report exports in `docs/data/raw/`.

Re-verify everything below against the live data.gov.in source when ingestion is implemented.

| Design element | Evidence (VERIFIED OBSERVATION) | Decision |
|---|---|---|
| Separate source adapters with explicit field mapping | Historical keys `Arrival_Date`, `Commodity`, `Commodity_Code`, `District`, `Grade`, `Market`, `Max_Price`, `Min_Price`, `Modal_Price`, `State`, `Variety`. Current keys `state`, `district`, `market`, `commodity`, `variety`, `grade`, `arrival_date`, `min_price`, `max_price`, `modal_price` (evidence §19.3) | Schema drift fails loudly as SCHEMA_ERROR (ADR-0011) |
| `report_date` DATE, strict `dd/MM/yyyy` parsing | All records use `dd/MM/yyyy`. The historical metadata filter format is `dd-MM-yyyy` (evidence §19.1, §19.7) | ADR-0007 |
| `numeric(14,2)` prices, exact decimals | JSON numbers with artifacts such as `3963.4099999999999` (evidence §19.7; Appendix A) | Half-up to 2 dp (ADR-0008) |
| `price_unit` on every row, no conversion | No unit field in the payloads (evidence §0.8). All 2,732 official export rows state `Rs./Quintal`. The dashboard states "Price (Rs./Quintal)" for Onion, Potato and Tomato | Per-commodity verified unit; validation rules V1–V8 (ADR-0008) |
| Pagination audit columns | `total`, `count`, and string `limit` and `offset`. One capture has a total of 103 with 100 records on page 0 (evidence §0.5, §19.6, Appendix B) | ADR-0011 |
| Normalization CHECKs, raw spelling kept | Leading or trailing whitespace in 73 of the 131 distinct Maharashtra market strings, and a doubled internal space in 1 (evidence §19.7). The official master has 337 such names out of 429 | — |
| `market_source_aliases` keyed by source state and district | Alias twins: 47 candidate groups in H-01AUG-2026-P0. `Sirsa APMC` exists in two states (C-647-P0) | PROVISIONAL registration, evidence-backed merges (ADR-0014) |
| `district_source_aliases` | The official master has 38 Maharashtra district entries for 36 LGD districts, including `Osmanabad` and `Dharashiv`, `Bandra(E)` and `Murum` | ADR-0014 |
| Variety and grade in the identity, NULL-safe | Several varieties per market, commodity and date. `Other` and `Local` are real values. No NULLs in the captures (evidence §19.7) | Selection rules (ADR-0010) |
| Exact commodity identity | 16 `Onion` and 3 `Onion Green` records in C-19-MH-ONION (evidence §0.11, §20.3). All 52 Maharashtra values match the official master | Known list from the master (ADR-0006) |
| Conflict handling outside the key | Same key, different prices: four pairs in H-07SEP-2026 | Quarantine (ADR-0009) |
| `commodity_source_aliases` | Until 2026-05-06 the historical resource used 20 commodity spellings that are not master names; each carries the same DMI `Commodity_Code` as one master spelling in that resource (Task 12 harvest) | ADR-0051 |
| `Commodity_Code` not persisted | Historical only; only `23` observed (evidence §19.3). Agmarknet codes differ between portal generations (Tomato is 78 in a legacy URL and 65 in the 2.0 master) | — |
| Coordinates from another source | Neither dataset has coordinate fields (evidence §19.3) | LGD, Survey of India, district-centroid approximation (ADR-0014) |

## 7. Implementation prerequisites

Every database decision is taken. What remains before or during implementation is human action:

| Work | Prerequisite |
|---|---|
| Any migration | None beyond this approved design and the approved baseline (ADR-0002). Each migration states its owning module and its tests |
| District seed | Survey of India district boundaries (registration by a team member) and the LGD Maharashtra lists (ADR-0014) |
| Commodity units beyond the current evidence | Official Daily Price and Arrival Report exports per commodity group (ADR-0008) |
| Localized names | A curated list with provenance (ADR-0015) |
| Public deployment and its first admin | RD-037 (hosting procurement); bootstrap credentials in the deployment environment (ADR-0028) |
| Any commit including `.mcp.json` | RD-002 user actions (ADR-0033) |
| `forecasts` (optional) | HARD RULE 2 and the §35 criteria (ADR-0030) |
| Phase 2 tables | Phase 1 Freeze (HARD RULE 1); RD-012 and RD-025 deferred by ADR-0032 |

## 8. Decisions applied, and remaining open items

| Item | Database effect | Decision |
|---|---|---|
| RD-001 | PostgreSQL 16.15, Flyway 12.4.0 | ADR-0002 |
| RD-003, RD-031 | Commodity known list from the official master. Categories are official groups. `states.code` is `IN-MH`. Dataset types are `CURRENT_PRICE` and `HISTORICAL_PRICE` | ADR-0006 |
| RD-004, RD-036 | Strict `dd/MM/yyyy`. IST for every date rule | ADR-0007 |
| RD-005, RD-028 | Unchanged re-deliveries write nothing. Revisions are kept in `price_observation_revisions`. Same-key conflicts are quarantined | ADR-0009 |
| RD-006 | Option-less runs persisted. Excluded markets never persisted | ADR-0023 |
| RD-007 | Weights seed 0.55 / 0.15 / 0.10 / 0.10 / 0.10 | ADR-0024 |
| RD-008 | `INSUFFICIENT_DATA` is a trend direction | ADR-0025 |
| RD-011, RD-026 | Test accounts and demo observations only with the demo profile, and only as real exports | ADR-0028 |
| RD-015 | Dataset precedence and lower-median selection | ADR-0010 |
| RD-016 | One RUNNING run per dataset. Error types and codes. No resume state | ADR-0011 |
| RD-017 | Ownership and dependencies | ADR-0013 |
| RD-018, RD-030, RD-032 | LGD codes, SoI centroids, PROVISIONAL markets, merges, slugs, localities | ADR-0014 |
| RD-019 | No database impact (Redis is ephemeral) | ADR-0018 |
| RD-020 | `alert_events` gains run, market, observation and `read_at`; one event per alert per run | ADR-0019 |
| RD-022 | Flyway seed at first boot, named volume, daily dump | ADR-0021 |
| RD-024 | `forecasts` owned by price; restricted ML role | ADR-0030 |
| RD-027 | `commodities.price_unit` and `price_unit_source`; validation rules V1–V8 | ADR-0008 |
| RD-029 | Accounting invariant; `records_conflicted` | ADR-0012 |
| RD-033 | Curated `name_hi` / `name_mr` with provenance | ADR-0015 |
| RD-034 | Identifier CHECKs, refresh-token families, admin-owned alerts through a profile | ADR-0016 |
| RD-035 | Retention periods; CASCADE and SET NULL for erasure | ADR-0029 |
| RD-012, RD-025 (part) | Phase 2: deferred | ADR-0032 (DEFERRED) |
| RD-002, RD-037 | No database effect | Open (`docs/REQUIRES_DECISION.md`) |

## 9. Conflicts

Conflicts found while designing the database, and how the decision lock resolved each.

| ID | Source A | Source B | Exact conflict | Resolution |
|---|---|---|---|---|
| DB-C-1 | Spec §15 step 5: "Parse date (dd-MM-yyyy or MM/dd/yyyy)"; §14: "Arrival_Date=dd-MM-yyyy" | Evidence: every record in both datasets uses `dd/MM/yyyy` (`13/09/2026` cannot be month-first) | The spec's formats do not match the records. A month-first parse would misread `01/08/2026` as 8 January | RESOLVED by ADR-0007: strict `dd/MM/yyyy` (departure D-06) |
| DB-C-2 | Spec §14, §47 Q8: the business key identifies an observation; §27, §31 RULE 6: unique at DB level | Evidence H-07SEP-2026: four pairs share market, commodity, variety, grade and date but differ in price | The source does not respect the key, so one row of each pair cannot be stored | RESOLVED by ADR-0009: every variant quarantined and logged as CONFLICT |
| DB-C-3 | Spec §48: "Count records fetched vs count in price_observations ≥ 95% of API records ingested per run" | Evidence: alias twins and same-key records mean that correct deduplication stores fewer rows than were fetched | Correct ingestion could fail the criterion as worded | RESOLVED by ADR-0012: completeness counts stored-or-already-stored in-scope records |
| DB-C-4 | Spec §20: `markets.canonical_slug` UK (global); §47 Q4: slug = lowercase, spaces to hyphens | Evidence C-647-P0: `Sirsa APMC` exists in Haryana and in Uttar Pradesh | Two real markets would derive the same slug | RESOLVED by ADR-0014: global slug, disambiguated by district slug, then state code |
| DB-C-5 | Spec §1 ("Price fields need unit verification"), §31 RULE 3, FR-05; per-quintal framing in §2, §21, §36 | Evidence §0.8 (no unit field); same-day values about 100-fold apart for the same leafy vegetable at different markets | A single per-quintal unit could not be assumed | RESOLVED by ADR-0008: per-commodity unit verified from official Agmarknet evidence (`Rs./Quintal`); unit-inconsistent rows rejected |
| DB-C-6 | Spec §27: no cross-module direct DB access | Spec §15, §22: `IngestionService` writes `price_observations`, reads `price_alerts`, writes `alert_events` | A literal reading breaks the module rule (Task 6 C-02) | RESOLVED by ADR-0013: interface paths only |
| DB-C-7 | Spec §13: only four module dependencies | Spec flows that need data from other modules | The spec's own flows need dependencies §13 does not draw | RESOLVED by ADR-0013: approved, acyclic dependency set |
| DB-C-8 | Spec §28: ADMIN may "Create price alert" | Spec §20: `PRICE_ALERTS.farmer_profile_id` | An admin has no farmer profile, so cannot own an alert as modeled | RESOLVED by ADR-0016: an ADMIN gets a profile on demand |

**Reconciled by the source hierarchy, not conflicts:**

- The evidence file's `bigint` keys vs §20's `uuid` keys: the evidence is authoritative for observed payloads, not for key strategy, so §20 applies (CLAUDE.md §B).
- The evidence file's run status `PARTIAL` vs §15's `PARTIAL_FAILURE`: the spec applies.
- The evidence's "arrival_date" vs §20's `report_date`: the spec's column name is kept, and the mapping is documented.

Task 6 conflicts C-01 to C-07 are resolved in the decision log (`docs/architecture/ARCHITECTURE_INDEX.md` §12).

## 10. Database quality audit

### 10.1 Task 7 audit, re-run for the decision lock (2026-10-03)

| # | Check | Result | Evidence |
|---|---|---|---|
| 1 | Every table has one owner | PASS | All 27 Phase 1 tables and `forecasts` have one owner (ADR-0013, ADR-0030). `buyer_verifications` (Phase 2) belongs to auth (ADR-0031) |
| 2 | Every FK points to the correct owner table | PASS | All 45 foreign keys reference the table that owns the entity. Cross-module ones are listed in INDEXES_AND_CONSTRAINTS.md §3 |
| 3 | No cross-module direct DB access is required | PASS | Every flow has a module-interface path ([TABLE_OWNERSHIP.md §5–§6](TABLE_OWNERSHIP.md#5-prohibited-direct-cross-module-database-access)), using the dependencies approved in ADR-0013 |
| 4 | ERD and data dictionary match | PASS | Generated from one model and re-checked by script: same tables, columns, base types, keys and nullability |
| 5 | All required Phase 1 tables are present | PASS | All 20 §20 tables, `refresh_tokens` (§28), `market_source_aliases` (§47 Q4), and the four tables the decisions require |
| 6 | Phase 2 does not become a Phase 1 dependency | PASS | No Phase 1 foreign key or query references a Phase 2 table |
| 7 | Current and historical observations remain source-distinguishable | PASS | `source_dataset_id` is NOT NULL and part of both identity constraints. Precedence is applied only at selection (ADR-0010) |
| 8 | Variety and grade remain distinguishable | PASS | Separate tables, part of the identity, scoped to the commodity by composite FKs |
| 9 | Onion and Onion Green remain distinct | PASS | Exact `commodities.canonical_name` UK; no fuzzy matching |
| 10 | Market whitespace and canonicalization are supported | PASS | Normalization CHECKs; raw spelling in `market_source_aliases.source_market_name_raw` |
| 11 | Market aliases are not silently merged | PASS | Merges only by admin action with evidence (ADR-0014); every alias carries an `evidence_note` |
| 12 | Date is stored as DATE | PASS | `report_date` DATE; strict parsing (ADR-0007) |
| 13 | `arrival_date` and `ingested_at` remain separate | PASS | `report_date` (from the source arrival date) and `ingested_at` on every row |
| 14 | Monetary values use exact decimals | PASS | Every money and price column is `numeric`; no floating-point column |
| 15 | No unverified price-unit conversion is encoded | PASS | No conversion anywhere. Units are verified per commodity with evidence (ADR-0008) |
| 16 | Pagination and audit persistence are supported | PASS | `source_total_records`, `pages_fetched`, `pages_failed`, `request_scope`, `page_offset`, `request_report_date`, `error_code`, the §20 counters, `records_out_of_scope` and `records_conflicted` |
| 17 | Duplicate business-key behavior is represented correctly | PASS | Business-key UK plus identity UK, the rules of ADR-0009, the revisions table, and the accounting invariant (ADR-0012) |
| 18 | Nullable uniqueness is safely handled | PASS | `UNIQUE NULLS NOT DISTINCT` on the identity |
| 19 | Indexes correspond to actual query patterns | PASS | Every secondary index maps to a query pattern (INDEXES_AND_CONSTRAINTS.md §7). Redundant candidates were rejected (§8) |
| 20 | No unnecessary tables were introduced | PASS | Each added table is required by a decision (ADR-0009, ADR-0014, ADR-0020) |
| 21 | No fabricated commodity list was introduced | PASS | The known list is the official Agmarknet 2.0 master (ADR-0006). Observed values are labelled as observations |
| 22 | Maharashtra remains the Phase 1 geographic scope | PASS | Statewide data scope (ADR-0006); state-agnostic schema |
| 23 | Many commodities are supported, not Onion only | PASS | Commodities, varieties and grades are data; no commodity-specific structure |
| 24 | Nearby-market location is supported by reference coordinates | PASS | District centroids from Survey of India boundaries; market coordinates with provenance and an approximation flag (ADR-0014) |
| 25 | ML remains optional | PASS | `forecasts` is optional and never read by core flows (ADR-0030) |
| 26 | Security-sensitive data is handled appropriately | PASS | Hashes only; sensitivity classes; no secrets; credential guards on `request_scope` and on audit details; token families (ADR-0016) |
| 27 | Decisions are recorded, and open items are explicit | PASS | Every database decision is in the decision log. The remaining open items (RD-002, RD-012, RD-025, RD-037) have no Phase 1 schema impact |

## DATABASE APPROVAL CHECKLIST

Approved on 2026-10-03 in the decision lock, under the delegation recorded in ADR-0001. The entries cited in section 8 record each approval.

- [x] Phase 1 database scope approved
- [x] Geography/reference-data model approved
- [x] Commodity model approved
- [x] Variety/grade model approved
- [x] Price observation model approved
- [x] Ingestion persistence approved
- [x] Table ownership approved
- [x] Primary/foreign key strategy approved
- [x] Business-key uniqueness approved
- [x] Index strategy approved
- [x] Constraint strategy approved
- [x] User/auth persistence approved
- [x] Recommendation persistence approved
- [x] Alert persistence approved
- [x] Optional ML persistence approved
- [x] Phase 1 / Phase 2 database boundary approved
- [x] All blocking database REQUIRES_DECISION items resolved
