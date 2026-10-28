# AgriNex — Indexes and Constraints

**Status:** AUTHORITATIVE. Database architecture from Task 7 (2026-10-03), approved in the decision lock of the same day (ADR-0001, ADR-0013; [DECISION_LOG.md](../architecture/DECISION_LOG.md)). Where this document and a binding decision-log entry differ, the entry governs. Design rationale: [DATABASE_ARCHITECTURE.md §17–§18](DATABASE_ARCHITECTURE.md#17-integrity-rules). Generated from the same table model as [DATA_DICTIONARY.md](DATA_DICTIONARY.md) and [ERD.md](ERD.md); a later change must be made in all three.

> **Documentation only.** The SQL fragments below describe constraints and indexes precisely. They are not a migration, are not meant to be executed, and omit column definitions (which are in the data dictionary). Migrations are written later, in Flyway, from this approved design (§10, §12, §43).

## 1. Conventions

- **Names:** `pk_<table>`, `fk_<table>_<target>`, `uq_<table>_<meaning>`, `ck_<table>_<meaning>`, `ix_<table>_<columns>`.
- **Constraint indexes.** PostgreSQL creates a unique B-tree index automatically for every primary key and unique constraint (VERIFIED TECHNICAL FACT, PostgreSQL 16 §11.6, fetched 2026-10-03). They are listed in section 5 as "(from constraint)" so that no redundant index is added on top.
- **Foreign-key columns are not indexed automatically** (VERIFIED TECHNICAL FACT, PostgreSQL 16 §5.4.5). An FK column gets an index only when a query pattern or a cascading delete needs it (section 8).
- **Basis.** Each item is REQUIREMENT FROM SSOT (cited) or ARCHITECTURAL DECISION (approved in the decision lock; the cited ADR gives the reason).
- **Counts:** 28 primary keys, 45 foreign keys, 49 indexes besides primary keys (two added on 2026-10-04 by ADR-0048, two on 2026-10-15 by ADR-0051).

## 2. Primary keys

| Table | Constraint | Column | Implicit index |
|---|---|---|---|
| `states` | `pk_states` | `id` | unique B-tree (automatic) |
| `districts` | `pk_districts` | `id` | unique B-tree (automatic) |
| `localities` | `pk_localities` | `id` | unique B-tree (automatic) |
| `district_source_aliases` | `pk_district_source_aliases` | `id` | unique B-tree (automatic) |
| `commodities` | `pk_commodities` | `id` | unique B-tree (automatic) |
| `varieties` | `pk_varieties` | `id` | unique B-tree (automatic) |
| `grades` | `pk_grades` | `id` | unique B-tree (automatic) |
| `commodity_source_aliases` | `pk_commodity_source_aliases` | `id` | unique B-tree (automatic) |
| `source_datasets` | `pk_source_datasets` | `id` | unique B-tree (automatic) |
| `markets` | `pk_markets` | `id` | unique B-tree (automatic) |
| `market_source_aliases` | `pk_market_source_aliases` | `id` | unique B-tree (automatic) |
| `price_observations` | `pk_price_observations` | `id` | unique B-tree (automatic) |
| `price_observation_revisions` | `pk_price_observation_revisions` | `id` | unique B-tree (automatic) |
| `ingestion_runs` | `pk_ingestion_runs` | `id` | unique B-tree (automatic) |
| `ingestion_errors` | `pk_ingestion_errors` | `id` | unique B-tree (automatic) |
| `ingestion_source_settings` | `pk_ingestion_source_settings` | `id` | unique B-tree (automatic) |
| `ingestion_report_dates` | `pk_ingestion_report_dates` | `id` | unique B-tree (automatic) |
| `users` | `pk_users` | `id` | unique B-tree (automatic) |
| `farmer_profiles` | `pk_farmer_profiles` | `id` | unique B-tree (automatic) |
| `refresh_tokens` | `pk_refresh_tokens` | `id` | unique B-tree (automatic) |
| `transport_assumptions` | `pk_transport_assumptions` | `id` | unique B-tree (automatic) |
| `recommendation_weights` | `pk_recommendation_weights` | `id` | unique B-tree (automatic) |
| `selling_searches` | `pk_selling_searches` | `id` | unique B-tree (automatic) |
| `recommendation_runs` | `pk_recommendation_runs` | `id` | unique B-tree (automatic) |
| `selling_options` | `pk_selling_options` | `id` | unique B-tree (automatic) |
| `price_alerts` | `pk_price_alerts` | `id` | unique B-tree (automatic) |
| `alert_events` | `pk_alert_events` | `id` | unique B-tree (automatic) |
| `admin_audit_events` | `pk_admin_audit_events` | `id` | unique B-tree (automatic) |
| `forecasts` | `pk_forecasts` | `id` | unique B-tree (automatic) |

## 3. Foreign keys

| Constraint | From | To | ON DELETE | Crosses module boundary | Why this deletion behavior |
|---|---|---|---|---|---|
| `fk_districts_state` | `districts` (`state_id`) | `states` (`id`) | RESTRICT | no | independent objects: deleting a referenced row must be explicit |
| `fk_localities_district` | `localities` (`district_id`) | `districts` (`id`) | RESTRICT | no | independent objects: deleting a referenced row must be explicit |
| `fk_district_source_aliases_district` | `district_source_aliases` (`district_id`) | `districts` (`id`) | RESTRICT | no | independent objects: deleting a referenced row must be explicit |
| `fk_varieties_commodity` | `varieties` (`commodity_id`) | `commodities` (`id`) | RESTRICT | no | independent objects: deleting a referenced row must be explicit |
| `fk_grades_commodity` | `grades` (`commodity_id`) | `commodities` (`id`) | RESTRICT | no | independent objects: deleting a referenced row must be explicit |
| `fk_commodity_source_aliases_commodity` | `commodity_source_aliases` (`commodity_id`) | `commodities` (`id`) | RESTRICT | no | independent objects: deleting a referenced row must be explicit |
| `fk_markets_district` | `markets` (`district_id`) | `districts` (`id`) | RESTRICT | yes (price → geography) | independent objects: deleting a referenced row must be explicit |
| `fk_markets_merged_into` | `markets` (`merged_into_market_id`) | `markets` (`id`) | RESTRICT | no | independent objects: deleting a referenced row must be explicit |
| `fk_market_source_aliases_market` | `market_source_aliases` (`market_id`) | `markets` (`id`) | RESTRICT | no | independent objects: deleting a referenced row must be explicit |
| `fk_price_observations_market` | `price_observations` (`market_id`) | `markets` (`id`) | RESTRICT | no | independent objects: deleting a referenced row must be explicit |
| `fk_price_observations_commodity` | `price_observations` (`commodity_id`) | `commodities` (`id`) | RESTRICT | yes (price → commodity) | independent objects: deleting a referenced row must be explicit |
| `fk_price_observations_variety` | `price_observations` (`variety_id`, `commodity_id`) | `varieties` (`id`, `commodity_id`) | RESTRICT | yes (price → commodity) | independent objects: deleting a referenced row must be explicit |
| `fk_price_observations_grade` | `price_observations` (`grade_id`, `commodity_id`) | `grades` (`id`, `commodity_id`) | RESTRICT | yes (price → commodity) | independent objects: deleting a referenced row must be explicit |
| `fk_price_observations_source_dataset` | `price_observations` (`source_dataset_id`) | `source_datasets` (`id`) | RESTRICT | no | independent objects: deleting a referenced row must be explicit |
| `fk_price_observations_ingestion_run` | `price_observations` (`ingestion_run_id`) | `ingestion_runs` (`id`) | RESTRICT | yes (price → ingestion) | independent objects: deleting a referenced row must be explicit |
| `fk_price_observation_revisions_observation` | `price_observation_revisions` (`price_observation_id`) | `price_observations` (`id`) | RESTRICT | no | independent objects: deleting a referenced row must be explicit |
| `fk_price_observation_revisions_run` | `price_observation_revisions` (`ingestion_run_id`) | `ingestion_runs` (`id`) | RESTRICT | yes (price → ingestion) | independent objects: deleting a referenced row must be explicit |
| `fk_ingestion_runs_source_dataset` | `ingestion_runs` (`source_dataset_id`) | `source_datasets` (`id`) | RESTRICT | yes (ingestion → price) | independent objects: deleting a referenced row must be explicit |
| `fk_ingestion_errors_run` | `ingestion_errors` (`ingestion_run_id`) | `ingestion_runs` (`id`) | CASCADE | no | child is part of the parent (component, or erased with it: ADR-0029) |
| `fk_ingestion_source_settings_dataset` | `ingestion_source_settings` (`source_dataset_id`) | `source_datasets` (`id`) | RESTRICT | yes (ingestion → price) | independent objects: deleting a referenced row must be explicit |
| `fk_ingestion_report_dates_source_dataset` | `ingestion_report_dates` (`source_dataset_id`) | `source_datasets` (`id`) | RESTRICT | yes (ingestion → price) | independent objects: deleting a referenced row must be explicit |
| `fk_ingestion_report_dates_attempt_run` | `ingestion_report_dates` (`last_attempt_run_id`) | `ingestion_runs` (`id`) | RESTRICT | no | independent objects: deleting a referenced row must be explicit |
| `fk_ingestion_report_dates_success_run` | `ingestion_report_dates` (`last_success_run_id`) | `ingestion_runs` (`id`) | RESTRICT | no | independent objects: deleting a referenced row must be explicit |
| `fk_farmer_profiles_user` | `farmer_profiles` (`user_id`) | `users` (`id`) | CASCADE | no | child is part of the parent (component, or erased with it: ADR-0029) |
| `fk_farmer_profiles_default_district` | `farmer_profiles` (`default_district_id`) | `districts` (`id`) | RESTRICT | yes (auth → geography) | independent objects: deleting a referenced row must be explicit |
| `fk_refresh_tokens_user` | `refresh_tokens` (`user_id`) | `users` (`id`) | CASCADE | no | child is part of the parent (component, or erased with it: ADR-0029) |
| `fk_selling_searches_farmer_profile` | `selling_searches` (`farmer_profile_id`) | `farmer_profiles` (`id`) | SET NULL | yes (recommendation → auth) | history is kept and anonymized when the parent is erased (ADR-0029) |
| `fk_selling_searches_commodity` | `selling_searches` (`commodity_id`) | `commodities` (`id`) | RESTRICT | yes (recommendation → commodity) | independent objects: deleting a referenced row must be explicit |
| `fk_selling_searches_variety` | `selling_searches` (`variety_id`, `commodity_id`) | `varieties` (`id`, `commodity_id`) | RESTRICT | yes (recommendation → commodity) | independent objects: deleting a referenced row must be explicit |
| `fk_selling_searches_grade` | `selling_searches` (`grade_id`, `commodity_id`) | `grades` (`id`, `commodity_id`) | RESTRICT | yes (recommendation → commodity) | independent objects: deleting a referenced row must be explicit |
| `fk_recommendation_runs_search` | `recommendation_runs` (`selling_search_id`) | `selling_searches` (`id`) | CASCADE | no | child is part of the parent (component, or erased with it: ADR-0029) |
| `fk_recommendation_runs_weights` | `recommendation_runs` (`weights_id`) | `recommendation_weights` (`id`) | RESTRICT | no | independent objects: deleting a referenced row must be explicit |
| `fk_recommendation_runs_transport` | `recommendation_runs` (`transport_id`) | `transport_assumptions` (`id`) | RESTRICT | yes (recommendation → transport) | independent objects: deleting a referenced row must be explicit |
| `fk_recommendation_runs_price_winner` | `recommendation_runs` (`price_only_winner_market_id`) | `markets` (`id`) | RESTRICT | yes (recommendation → price) | independent objects: deleting a referenced row must be explicit |
| `fk_recommendation_runs_net_winner` | `recommendation_runs` (`net_winner_market_id`) | `markets` (`id`) | RESTRICT | yes (recommendation → price) | independent objects: deleting a referenced row must be explicit |
| `fk_selling_options_run` | `selling_options` (`recommendation_run_id`) | `recommendation_runs` (`id`) | CASCADE | no | child is part of the parent (component, or erased with it: ADR-0029) |
| `fk_selling_options_market` | `selling_options` (`market_id`) | `markets` (`id`) | RESTRICT | yes (recommendation → price) | independent objects: deleting a referenced row must be explicit |
| `fk_selling_options_observation` | `selling_options` (`price_observation_id`) | `price_observations` (`id`) | RESTRICT | yes (recommendation → price) | independent objects: deleting a referenced row must be explicit |
| `fk_price_alerts_farmer_profile` | `price_alerts` (`farmer_profile_id`) | `farmer_profiles` (`id`) | CASCADE | yes (alert → auth) | child is part of the parent (component, or erased with it: ADR-0029) |
| `fk_price_alerts_commodity` | `price_alerts` (`commodity_id`) | `commodities` (`id`) | RESTRICT | yes (alert → commodity) | independent objects: deleting a referenced row must be explicit |
| `fk_price_alerts_market` | `price_alerts` (`market_id`) | `markets` (`id`) | RESTRICT | yes (alert → price) | independent objects: deleting a referenced row must be explicit |
| `fk_alert_events_alert` | `alert_events` (`price_alert_id`) | `price_alerts` (`id`) | CASCADE | no | child is part of the parent (component, or erased with it: ADR-0029) |
| `fk_alert_events_run` | `alert_events` (`ingestion_run_id`) | `ingestion_runs` (`id`) | RESTRICT | yes (alert → ingestion) | independent objects: deleting a referenced row must be explicit |
| `fk_alert_events_market` | `alert_events` (`market_id`) | `markets` (`id`) | RESTRICT | yes (alert → price) | independent objects: deleting a referenced row must be explicit |
| `fk_alert_events_observation` | `alert_events` (`price_observation_id`) | `price_observations` (`id`) | RESTRICT | yes (alert → price) | independent objects: deleting a referenced row must be explicit |
| `fk_admin_audit_events_actor` | `admin_audit_events` (`actor_user_id`) | `users` (`id`) | RESTRICT | yes (admin → auth) | independent objects: deleting a referenced row must be explicit |
| `fk_forecasts_market` | `forecasts` (`market_id`) | `markets` (`id`) | RESTRICT | no | independent objects: deleting a referenced row must be explicit |
| `fk_forecasts_commodity` | `forecasts` (`commodity_id`) | `commodities` (`id`) | RESTRICT | yes (price → commodity) | independent objects: deleting a referenced row must be explicit |

**Deletion behavior.** `RESTRICT` is used between independent objects, so deleting a referenced row fails instead of silently removing history. `CASCADE` is used only where the child is part of the parent or is erased with a user account, and `SET NULL` only where history is kept anonymized after erasure (ADR-0029; VERIFIED TECHNICAL FACT: PostgreSQL 16 §5.4.5 describes these choices). Two composite foreign keys per referencing table (`price_observations`, `selling_searches`) force a variety or grade to belong to the row's commodity; under the default MATCH SIMPLE a NULL `variety_id` or `grade_id` skips the check (VERIFIED TECHNICAL FACT, PostgreSQL 16 `CREATE TABLE`). Their targets need the unique constraints `uq_varieties_id_commodity` and `uq_grades_id_commodity`, because a foreign key must reference a primary key, a unique constraint or a non-partial unique index (same source).

## 4. Unique constraints and business keys

| Constraint / index | Table | Columns | Rule | Basis |
|---|---|---|---|---|
| `uq_states_code` | `states` | `code` | UNIQUE | REQUIREMENT FROM SSOT: §20 UK |
| `uq_states_name` | `states` | `name` | UNIQUE | ARCHITECTURAL DECISION: prevents duplicate state rows |
| `uq_states_lgd_code` | `states` | `lgd_code` | UNIQUE | ARCHITECTURAL DECISION: ADR-0014 (one row per LGD state) |
| `uq_districts_state_name` | `districts` | `state_id`, `name` | UNIQUE | ARCHITECTURAL DECISION: natural key; also serves districts-by-state lookups |
| `uq_districts_lgd_code` | `districts` | `lgd_code` | UNIQUE | ARCHITECTURAL DECISION: ADR-0014 (one row per LGD district) |
| `uq_localities_district_name` | `localities` | `district_id`, `name` | UNIQUE | ARCHITECTURAL DECISION: natural key; serves localities-by-district lookups |
| `uq_localities_lgd_code` | `localities` | `lgd_code` | UNIQUE | ARCHITECTURAL DECISION: ADR-0014 (one row per LGD sub-district) |
| `uq_district_source_aliases_source` | `district_source_aliases` | `source_state_name`, `source_district_name` | UNIQUE | ARCHITECTURAL DECISION: one mapping per source spelling (ADR-0014) |
| `uq_commodities_canonical_name` | `commodities` | `canonical_name` | UNIQUE | REQUIREMENT FROM SSOT: §20 UK; exact-match resolution |
| `uq_varieties_commodity_name` | `varieties` | `commodity_id`, `canonical_name` | UNIQUE | ARCHITECTURAL DECISION: natural key; exact-match resolution |
| `uq_varieties_id_commodity` | `varieties` | `id`, `commodity_id` | UNIQUE | ARCHITECTURAL DECISION: target of composite FKs that force a variety to belong to the referencing row's commodity |
| `uq_grades_commodity_name` | `grades` | `commodity_id`, `canonical_name` | UNIQUE | ARCHITECTURAL DECISION: natural key; exact-match resolution |
| `uq_grades_id_commodity` | `grades` | `id`, `commodity_id` | UNIQUE | ARCHITECTURAL DECISION: target of composite FKs (grade belongs to the row's commodity) |
| `uq_commodity_source_aliases_source` | `commodity_source_aliases` | `source_commodity_name` | UNIQUE | ARCHITECTURAL DECISION: one master commodity per source spelling (ADR-0051) |
| `uq_source_datasets_resource_id` | `source_datasets` | `resource_id` | UNIQUE | REQUIREMENT FROM SSOT: §20 UK |
| `uq_source_datasets_dataset_type` | `source_datasets` | `dataset_type` | UNIQUE | ARCHITECTURAL DECISION: one resource per dataset role (ADR-0006) |
| `uq_markets_canonical_slug` | `markets` | `canonical_slug` | UNIQUE | REQUIREMENT FROM SSOT: §20 UK |
| `uq_market_source_aliases_source` | `market_source_aliases` | `source_state_name`, `source_district_name`, `source_market_name` | UNIQUE | ARCHITECTURAL DECISION: one mapping per source spelling within its source state and district |
| `uq_price_observations_business_key` | `price_observations` | `business_key` | UNIQUE | REQUIREMENT FROM SSOT: §27, §31 RULE 6, FR-04 |
| `uq_price_observations_identity` | `price_observations` | `market_id`, `commodity_id`, `report_date`, `variety_id`, `grade_id`, `source_dataset_id` | UNIQUE NULLS NOT DISTINCT | ARCHITECTURAL DECISION: null-safe identity enforced from the resolved IDs; independent of hashing correctness; leading columns serve latest-price and trend queries |
| `uq_price_observation_revisions_observation_run` | `price_observation_revisions` | `price_observation_id`, `ingestion_run_id` | UNIQUE | ARCHITECTURAL DECISION: at most one revision per observation per run (ADR-0009) |
| `uq_ingestion_runs_one_running` | `ingestion_runs` | `source_dataset_id` | partial unique index WHERE status = 'RUNNING' | ARCHITECTURAL DECISION: at most one RUNNING run per dataset (ADR-0011) |
| `uq_ingestion_source_settings_dataset` | `ingestion_source_settings` | `source_dataset_id` | UNIQUE | ARCHITECTURAL DECISION: one setting row per dataset |
| `uq_users_phone_number` | `users` | `phone_number` | UNIQUE | REQUIREMENT FROM SSOT: §20 UK (NULLs remain distinct by default) |
| `uq_users_email` | `users` | `email` | UNIQUE | REQUIREMENT FROM SSOT: §20 UK (NULLs remain distinct by default) |
| `uq_users_email_verification_token_hash` | `users` | `email_verification_token_hash` | UNIQUE | ARCHITECTURAL DECISION: lookup of the presented verification token (ADR-0048) |
| `uq_users_password_reset_token_hash` | `users` | `password_reset_token_hash` | UNIQUE | ARCHITECTURAL DECISION: lookup of the presented reset token (ADR-0048) |
| `uq_farmer_profiles_user` | `farmer_profiles` | `user_id` | UNIQUE | REQUIREMENT FROM SSOT: §20 FK UK |
| `uq_refresh_tokens_token_hash` | `refresh_tokens` | `token_hash` | UNIQUE | ARCHITECTURAL DECISION: lookup of the presented token |
| `uq_transport_assumptions_single_active` | `transport_assumptions` | (is_active) | partial unique index WHERE is_active | ARCHITECTURAL DECISION: at most one active row, so "load active" (§16) is deterministic |
| `uq_recommendation_weights_single_active` | `recommendation_weights` | (is_active) | partial unique index WHERE is_active | ARCHITECTURAL DECISION: at most one active row (§16 deterministic engine) |
| `uq_selling_options_run_market` | `selling_options` | `recommendation_run_id`, `market_id` | UNIQUE | ARCHITECTURAL DECISION: one option per candidate market (§16 "one per candidate") |
| `uq_selling_options_run_rank` | `selling_options` | `recommendation_run_id`, `rank` | UNIQUE | ARCHITECTURAL DECISION: §16 "Assign rank 1..N" |
| `uq_alert_events_alert_run` | `alert_events` | `price_alert_id`, `ingestion_run_id` | UNIQUE | ARCHITECTURAL DECISION: at most one event per alert per run; idempotent re-evaluation (ADR-0019) |
| `uq_forecasts_point` | `forecasts` | `market_id`, `commodity_id`, `forecast_horizon_days`, `generated_at`, `forecast_date` | UNIQUE | ARCHITECTURAL DECISION: one point per forecast date per generation; serves "latest forecast" lookups |

### 4.1 Observation identity

- **`uq_price_observations_business_key`** is the spec's rule: "Unique constraint on `business_key` in `price_observations` ensures deduplication even if application code has a bug" (§31 RULE 6; §27; FR-04). It is also the arbiter of `INSERT … ON CONFLICT (business_key)` (§14, §15, §22). The value is the lowercase hex SHA-256 of the six resolved identity components ([DATABASE_ARCHITECTURE.md §10.3](DATABASE_ARCHITECTURE.md#103-business-key-architectural-decision)).
- **`uq_price_observations_identity`** enforces the same identity directly on the resolved columns, as `UNIQUE NULLS NOT DISTINCT`. Without `NULLS NOT DISTINCT`, two rows with a NULL `variety_id` (or `grade_id`) and otherwise identical values would both be accepted, because PostgreSQL treats NULLs as distinct by default (VERIFIED TECHNICAL FACT, PostgreSQL 16 §5.4.3, §11.6). With it, true duplicates are rejected whether or not variety and grade are present.
- If application code ever computes two different business keys for one identity, the identity constraint raises an error and the run fails loudly (§37) instead of storing a duplicate.

### 4.2 Duplicate handling the constraints do not decide

- **Same key, same prices** (re-delivery, alias twins): one row; nothing is written and the record counts in `duplicates_skipped` (ADR-0009).
- **Same key, different prices in one delivery** (four cases in H-07SEP-2026): no row is stored; every variant is logged as a CONFLICT error and counted in `records_conflicted` (ADR-0009). A batched `ON CONFLICT DO UPDATE` raises a cardinality violation when two proposed rows share the arbiter key (VERIFIED TECHNICAL FACT, PostgreSQL 16 `INSERT`), so ingestion deduplicates and resolves conflicts before the statement.
- **Stored key, different prices from a later run:** the observation is updated and the replaced values go to `price_observation_revisions` (ADR-0009).

## 5. Indexes

| Index | Table | Columns | Unique | Partial predicate | Purpose | Expected query | Phase 1 required | Basis |
|---|---|---|---|---|---|---|---|---|
| `uq_states_code` (from constraint) | `states` | `code` | yes | — | §20 UK | — | yes | REQUIREMENT FROM SSOT |
| `uq_states_name` (from constraint) | `states` | `name` | yes | — | prevents duplicate state rows | — | yes | ARCHITECTURAL DECISION |
| `uq_states_lgd_code` (from constraint) | `states` | `lgd_code` | yes | — | ADR-0014 (one row per LGD state) | — | yes | ARCHITECTURAL DECISION |
| `uq_districts_state_name` (from constraint) | `districts` | `state_id`, `name` | yes | — | natural key; also serves districts-by-state lookups | — | yes | ARCHITECTURAL DECISION |
| `uq_districts_lgd_code` (from constraint) | `districts` | `lgd_code` | yes | — | ADR-0014 (one row per LGD district) | — | yes | ARCHITECTURAL DECISION |
| `uq_localities_district_name` (from constraint) | `localities` | `district_id`, `name` | yes | — | natural key; serves localities-by-district lookups | — | yes | ARCHITECTURAL DECISION |
| `uq_localities_lgd_code` (from constraint) | `localities` | `lgd_code` | yes | — | ADR-0014 (one row per LGD sub-district) | — | yes | ARCHITECTURAL DECISION |
| `uq_district_source_aliases_source` (from constraint) | `district_source_aliases` | `source_state_name`, `source_district_name` | yes | — | one mapping per source spelling (ADR-0014) | — | yes | ARCHITECTURAL DECISION |
| `uq_commodities_canonical_name` (from constraint) | `commodities` | `canonical_name` | yes | — | §20 UK; exact-match resolution | — | yes | REQUIREMENT FROM SSOT |
| `uq_varieties_commodity_name` (from constraint) | `varieties` | `commodity_id`, `canonical_name` | yes | — | natural key; exact-match resolution | — | yes | ARCHITECTURAL DECISION |
| `uq_varieties_id_commodity` (from constraint) | `varieties` | `id`, `commodity_id` | yes | — | target of composite FKs that force a variety to belong to the referencing row's commodity | — | yes | ARCHITECTURAL DECISION |
| `uq_grades_commodity_name` (from constraint) | `grades` | `commodity_id`, `canonical_name` | yes | — | natural key; exact-match resolution | — | yes | ARCHITECTURAL DECISION |
| `uq_grades_id_commodity` (from constraint) | `grades` | `id`, `commodity_id` | yes | — | target of composite FKs (grade belongs to the row's commodity) | — | yes | ARCHITECTURAL DECISION |
| `uq_commodity_source_aliases_source` (from constraint) | `commodity_source_aliases` | `source_commodity_name` | yes | — | alias lookup by source spelling during ingestion (ADR-0051) | `SELECT commodity_id FROM commodity_source_aliases WHERE source_commodity_name = $1` | yes | ARCHITECTURAL DECISION |
| `ix_commodity_source_aliases_commodity_id` | `commodity_source_aliases` | `commodity_id` | no | — | FK support for commodity changes | — | yes | ARCHITECTURAL DECISION |
| `uq_source_datasets_resource_id` (from constraint) | `source_datasets` | `resource_id` | yes | — | §20 UK | — | yes | REQUIREMENT FROM SSOT |
| `uq_source_datasets_dataset_type` (from constraint) | `source_datasets` | `dataset_type` | yes | — | one resource per dataset role (ADR-0006) | — | yes | ARCHITECTURAL DECISION |
| `uq_markets_canonical_slug` (from constraint) | `markets` | `canonical_slug` | yes | — | §20 UK | — | yes | REQUIREMENT FROM SSOT |
| `ix_markets_district_id` | `markets` | `district_id` | no | — | Market-by-district lookup; current observations by geography (join to observations); FK support for district changes. | `SELECT id, canonical_name FROM markets WHERE district_id = $1 AND is_active` | yes | ARCHITECTURAL DECISION |
| `uq_market_source_aliases_source` (from constraint) | `market_source_aliases` | `source_state_name`, `source_district_name`, `source_market_name` | yes | — | one mapping per source spelling within its source state and district | — | yes | ARCHITECTURAL DECISION |
| `uq_price_observations_business_key` (from constraint) | `price_observations` | `business_key` | yes | — | §27, §31 RULE 6, FR-04 | — | yes | REQUIREMENT FROM SSOT |
| `uq_price_observations_identity` (from constraint) | `price_observations` | `market_id`, `commodity_id`, `report_date`, `variety_id`, `grade_id`, `source_dataset_id` NULLS NOT DISTINCT | yes | — | null-safe identity enforced from the resolved IDs; independent of hashing correctness; leading columns serve latest-price and trend queries | — | yes | ARCHITECTURAL DECISION |
| `ix_price_observations_ingestion_run_id` | `price_observations` | `ingestion_run_id` | no | — | Alert evaluation against the observations a run just wrote (§15, §22, §34); per-run audit; FK support. | `SELECT ... FROM price_observations WHERE ingestion_run_id = $1` | yes | ARCHITECTURAL DECISION |
| `uq_price_observation_revisions_observation_run` (from constraint) | `price_observation_revisions` | `price_observation_id`, `ingestion_run_id` | yes | — | at most one revision per observation per run (ADR-0009) | — | yes | ARCHITECTURAL DECISION |
| `ix_price_observation_revisions_run` | `price_observation_revisions` | `ingestion_run_id` | no | — | Verify `records_updated` per run against database rows (§48 admin health, ADR-0012). | `SELECT count(*) FROM price_observation_revisions WHERE ingestion_run_id = $1` | yes | ARCHITECTURAL DECISION |
| `uq_ingestion_runs_one_running` | `ingestion_runs` | `source_dataset_id` | yes | `status = 'RUNNING'` | at most one RUNNING run per dataset (ADR-0011) | — | yes | ARCHITECTURAL DECISION |
| `ix_ingestion_runs_dataset_started` | `ingestion_runs` | `source_dataset_id`, `started_at` | no | — | Run history and "last sync" per dataset for the admin dashboard (FR-18, §46 "last 5 ingestion runs"). | `SELECT ... FROM ingestion_runs WHERE source_dataset_id = $1 ORDER BY started_at DESC LIMIT 5` | yes | ARCHITECTURAL DECISION |
| `ix_ingestion_errors_run` | `ingestion_errors` | `ingestion_run_id` | no | — | Errors of one run for the admin view (FR-18); cascade path from `ingestion_runs`. | `SELECT ... FROM ingestion_errors WHERE ingestion_run_id = $1 ORDER BY created_at` | yes | ARCHITECTURAL DECISION |
| `ix_ingestion_errors_created_at` | `ingestion_errors` | `created_at` | no | — | Daily purge of rows older than 180 days (ADR-0029). | `DELETE FROM ingestion_errors WHERE created_at < now() - interval '180 days'` | yes | ARCHITECTURAL DECISION |
| `uq_ingestion_source_settings_dataset` (from constraint) | `ingestion_source_settings` | `source_dataset_id` | yes | — | one setting row per dataset | — | yes | ARCHITECTURAL DECISION |
| `uq_ingestion_report_dates_dataset_date` (from constraint) | `ingestion_report_dates` | `source_dataset_id`, `report_date` | yes | — | one ledger row per dataset and report date (ADR-0054); also serves the coverage range read | `SELECT ... FROM ingestion_report_dates WHERE source_dataset_id = $1 AND report_date BETWEEN $2 AND $3` | yes | ARCHITECTURAL DECISION |
| `ix_ingestion_report_dates_attempt_run` | `ingestion_report_dates` | `last_attempt_run_id` | no | — | Progress of a running run: the dates it has attempted (ADR-0054 item 6). | `SELECT count(*) FROM ingestion_report_dates WHERE last_attempt_run_id = $1` | yes | ARCHITECTURAL DECISION |
| `uq_users_phone_number` (from constraint) | `users` | `phone_number` | yes | — | §20 UK (NULLs remain distinct by default) | — | yes | REQUIREMENT FROM SSOT |
| `uq_users_email` (from constraint) | `users` | `email` | yes | — | §20 UK (NULLs remain distinct by default) | — | yes | REQUIREMENT FROM SSOT |
| `uq_users_email_verification_token_hash` (from constraint) | `users` | `email_verification_token_hash` | yes | — | lookup of the presented verification token (ADR-0048) | `SELECT id FROM users WHERE email_verification_token_hash = $1 AND email_verification_expires_at > now()` | yes | ARCHITECTURAL DECISION |
| `uq_users_password_reset_token_hash` (from constraint) | `users` | `password_reset_token_hash` | yes | — | lookup of the presented reset token (ADR-0048) | `SELECT id FROM users WHERE password_reset_token_hash = $1 AND password_reset_expires_at > now()` | yes | ARCHITECTURAL DECISION |
| `uq_farmer_profiles_user` (from constraint) | `farmer_profiles` | `user_id` | yes | — | §20 FK UK | — | yes | REQUIREMENT FROM SSOT |
| `uq_refresh_tokens_token_hash` (from constraint) | `refresh_tokens` | `token_hash` | yes | — | lookup of the presented token | — | yes | ARCHITECTURAL DECISION |
| `ix_refresh_tokens_family` | `refresh_tokens` | `family_id` | no | — | Revoke a whole token family on reuse detection or logout (ADR-0016). | `UPDATE refresh_tokens SET revoked_at = now() WHERE family_id = $1 AND revoked_at IS NULL` | yes | ARCHITECTURAL DECISION |
| `uq_transport_assumptions_single_active` | `transport_assumptions` | (is_active) | yes | `is_active` | at most one active row, so "load active" (§16) is deterministic | — | yes | ARCHITECTURAL DECISION |
| `uq_recommendation_weights_single_active` | `recommendation_weights` | (is_active) | yes | `is_active` | at most one active row (§16 deterministic engine) | — | yes | ARCHITECTURAL DECISION |
| `ix_selling_searches_farmer_profile` | `selling_searches` | `farmer_profile_id` | no | — | A farmer's latest recommendation run, needed when a Phase 2 listing links to it (§47 Q23). Not needed by any Phase 1 query. | `SELECT r.id FROM selling_searches s JOIN recommendation_runs r ON r.selling_search_id = s.id WHERE s.farmer_profile_id = $1 ORDER BY r.created_at DESC LIMIT 1` | no | ARCHITECTURAL DECISION |
| `uq_selling_options_run_market` (from constraint) | `selling_options` | `recommendation_run_id`, `market_id` | yes | — | one option per candidate market (§16 "one per candidate") | — | yes | ARCHITECTURAL DECISION |
| `uq_selling_options_run_rank` (from constraint) | `selling_options` | `recommendation_run_id`, `rank` | yes | — | §16 "Assign rank 1..N" | — | yes | ARCHITECTURAL DECISION |
| `ix_price_alerts_farmer_profile` | `price_alerts` | `farmer_profile_id` | no | — | A farmer's own alerts (`GET /api/v1/alerts`, F1.19). | `SELECT ... FROM price_alerts WHERE farmer_profile_id = $1` | yes | ARCHITECTURAL DECISION |
| `ix_price_alerts_active_commodity` | `price_alerts` | `commodity_id` | no | `status = 'ACTIVE'` | Post-run evaluation of ACTIVE alerts for the commodities a run ingested (§22). | `SELECT ... FROM price_alerts WHERE status = 'ACTIVE' AND commodity_id = ANY($1)` | yes | ARCHITECTURAL DECISION |
| `uq_alert_events_alert_run` (from constraint) | `alert_events` | `price_alert_id`, `ingestion_run_id` | yes | — | at most one event per alert per run; idempotent re-evaluation (ADR-0019) | — | yes | ARCHITECTURAL DECISION |
| `ix_alert_events_alert_triggered` | `alert_events` | `price_alert_id`, `triggered_at` | no | — | Event history of one alert, newest first; cascade path from `price_alerts`. | `SELECT ... FROM alert_events WHERE price_alert_id = $1 ORDER BY triggered_at DESC` | yes | ARCHITECTURAL DECISION |
| `ix_admin_audit_events_created_at` | `admin_audit_events` | `created_at` | no | — | Audit view, newest first (ADR-0020). | `SELECT ... FROM admin_audit_events ORDER BY created_at DESC LIMIT $1` | yes | ARCHITECTURAL DECISION |
| `ix_admin_audit_events_target` | `admin_audit_events` | `target_type`, `target_id` | no | — | History of one object (for example a market's merges and coordinate changes). | `SELECT ... FROM admin_audit_events WHERE target_type = $1 AND target_id = $2 ORDER BY created_at` | yes | ARCHITECTURAL DECISION |
| `uq_forecasts_point` (from constraint) | `forecasts` | `market_id`, `commodity_id`, `forecast_horizon_days`, `generated_at`, `forecast_date` | yes | — | one point per forecast date per generation; serves "latest forecast" lookups | — | no (optional ML) | ARCHITECTURAL DECISION |

**Composite indexes and what their leading columns serve:**

- `uq_price_observations_identity` (`market_id`, `commodity_id`, `report_date`, `variety_id`, `grade_id`, `source_dataset_id`): equality on market and commodity, then range or backward scan on `report_date`. Serves the latest price, the trend window, and per-variety or per-dataset filters checked inside the index (VERIFIED TECHNICAL FACT, PostgreSQL 16 §11.3, §11.4).
- `ix_ingestion_runs_dataset_started` (`source_dataset_id`, `started_at`): latest runs per dataset by backward scan.
- `ix_alert_events_alert_triggered` (`price_alert_id`, `triggered_at`): event history per alert, newest first.
- `uq_market_source_aliases_source` (`source_state_name`, `source_district_name`, `source_market_name`): exact alias resolution.
- `uq_districts_state_name`, `uq_localities_district_name`, `uq_varieties_commodity_name`, `uq_grades_commodity_name`: children of a parent, in name order.
- `uq_selling_options_run_market`, `uq_selling_options_run_rank`: the options of one run.

## 6. CHECK constraints

CHECK constraints may reference only the row being checked, and PostgreSQL assumes they are immutable (VERIFIED TECHNICAL FACT, PostgreSQL 16 §5.4.1). That is why no CHECK uses `CURRENT_DATE` or looks at another row; those rules stay in code (section 9).

### states

| CHECK constraint | Basis | Rationale |
|---|---|---|
| `ck_states_name_normalized` | ARCHITECTURAL DECISION | §15 "Trim all string fields" |
| `ck_states_code_format` | ARCHITECTURAL DECISION | non-empty code without whitespace |
| `ck_states_lgd_code_positive` | ARCHITECTURAL DECISION | LGD codes are positive integers |

```sql
-- Illustrative constraint fragments for states (documentation only; not a migration)
CONSTRAINT ck_states_name_normalized CHECK (name = btrim(regexp_replace(name, '\s+', ' ', 'g')) AND name <> '')
CONSTRAINT ck_states_code_format CHECK (code <> '' AND code !~ '\s')
CONSTRAINT ck_states_lgd_code_positive CHECK (lgd_code > 0)
```


### districts

| CHECK constraint | Basis | Rationale |
|---|---|---|
| `ck_districts_name_normalized` | ARCHITECTURAL DECISION | §15 trim rule |
| `ck_districts_centroid_range` | ARCHITECTURAL DECISION | valid coordinate ranges |
| `ck_districts_centroid_source` | ARCHITECTURAL DECISION | provenance must be stated |
| `ck_districts_lgd_code_positive` | ARCHITECTURAL DECISION | LGD codes are positive integers |

```sql
-- Illustrative constraint fragments for districts (documentation only; not a migration)
CONSTRAINT ck_districts_name_normalized CHECK (name = btrim(regexp_replace(name, '\s+', ' ', 'g')) AND name <> '')
CONSTRAINT ck_districts_centroid_range CHECK (centroid_lat BETWEEN -90 AND 90 AND centroid_lon BETWEEN -180 AND 180)
CONSTRAINT ck_districts_centroid_source CHECK (centroid_source <> '')
CONSTRAINT ck_districts_lgd_code_positive CHECK (lgd_code > 0)
```


### localities

| CHECK constraint | Basis | Rationale |
|---|---|---|
| `ck_localities_name_normalized` | ARCHITECTURAL DECISION | §15 trim rule |
| `ck_localities_coordinates_complete` | ARCHITECTURAL DECISION | coordinates travel with their provenance |
| `ck_localities_coordinate_range` | ARCHITECTURAL DECISION | valid coordinate ranges |
| `ck_localities_lgd_code_positive` | ARCHITECTURAL DECISION | LGD codes are positive integers |

```sql
-- Illustrative constraint fragments for localities (documentation only; not a migration)
CONSTRAINT ck_localities_name_normalized CHECK (name = btrim(regexp_replace(name, '\s+', ' ', 'g')) AND name <> '')
CONSTRAINT ck_localities_coordinates_complete CHECK ((lat IS NULL AND lon IS NULL AND coordinate_source IS NULL) OR (lat IS NOT NULL AND lon IS NOT NULL AND coordinate_source IS NOT NULL))
CONSTRAINT ck_localities_coordinate_range CHECK ((lat IS NULL OR lat BETWEEN -90 AND 90) AND (lon IS NULL OR lon BETWEEN -180 AND 180))
CONSTRAINT ck_localities_lgd_code_positive CHECK (lgd_code > 0)
```


### district_source_aliases

| CHECK constraint | Basis | Rationale |
|---|---|---|
| `ck_district_source_aliases_state_normalized` | ARCHITECTURAL DECISION | normalized key column |
| `ck_district_source_aliases_district_normalized` | ARCHITECTURAL DECISION | normalized key column |
| `ck_district_source_aliases_evidence` | ARCHITECTURAL DECISION | ADR-0014 |

```sql
-- Illustrative constraint fragments for district_source_aliases (documentation only; not a migration)
CONSTRAINT ck_district_source_aliases_state_normalized CHECK (source_state_name = btrim(regexp_replace(source_state_name, '\s+', ' ', 'g')) AND source_state_name <> '')
CONSTRAINT ck_district_source_aliases_district_normalized CHECK (source_district_name = btrim(regexp_replace(source_district_name, '\s+', ' ', 'g')) AND source_district_name <> '')
CONSTRAINT ck_district_source_aliases_evidence CHECK (evidence_note <> '')
```


### commodities

| CHECK constraint | Basis | Rationale |
|---|---|---|
| `ck_commodities_name_normalized` | ARCHITECTURAL DECISION | §15 trim rule |
| `ck_commodities_unit_with_source` | ARCHITECTURAL DECISION | a unit is recorded only with its evidence (ADR-0008) |

```sql
-- Illustrative constraint fragments for commodities (documentation only; not a migration)
CONSTRAINT ck_commodities_name_normalized CHECK (canonical_name = btrim(regexp_replace(canonical_name, '\s+', ' ', 'g')) AND canonical_name <> '')
CONSTRAINT ck_commodities_unit_with_source CHECK ((price_unit IS NULL AND price_unit_source IS NULL) OR (price_unit <> '' AND price_unit_source <> ''))
```


### varieties

| CHECK constraint | Basis | Rationale |
|---|---|---|
| `ck_varieties_name_normalized` | ARCHITECTURAL DECISION | §15 trim rule |

```sql
-- Illustrative constraint fragments for varieties (documentation only; not a migration)
CONSTRAINT ck_varieties_name_normalized CHECK (canonical_name = btrim(regexp_replace(canonical_name, '\s+', ' ', 'g')) AND canonical_name <> '')
```


### grades

| CHECK constraint | Basis | Rationale |
|---|---|---|
| `ck_grades_name_normalized` | ARCHITECTURAL DECISION | §15 trim rule |

```sql
-- Illustrative constraint fragments for grades (documentation only; not a migration)
CONSTRAINT ck_grades_name_normalized CHECK (canonical_name = btrim(regexp_replace(canonical_name, '\s+', ' ', 'g')) AND canonical_name <> '')
```


### commodity_source_aliases

| CHECK constraint | Basis | Rationale |
|---|---|---|
| `ck_commodity_source_aliases_source_normalized` | ARCHITECTURAL DECISION | normalized key column |
| `ck_commodity_source_aliases_evidence` | ARCHITECTURAL DECISION | ADR-0051 (no alias without evidence) |

```sql
-- Illustrative constraint fragments for commodity_source_aliases (documentation only; not a migration)
CONSTRAINT ck_commodity_source_aliases_source_normalized CHECK (source_commodity_name = btrim(regexp_replace(source_commodity_name, '\s+', ' ', 'g')) AND source_commodity_name <> '')
CONSTRAINT ck_commodity_source_aliases_evidence CHECK (evidence_note <> '')
```

Secondary indexes: `ix_commodity_source_aliases_commodity_id` (see [Indexes](#5-indexes)).


### source_datasets

| CHECK constraint | Basis | Rationale |
|---|---|---|
| `ck_source_datasets_resource_id_format` | ARCHITECTURAL DECISION | non-empty identifier without whitespace |
| `ck_source_datasets_type` | ARCHITECTURAL DECISION | §45 and ADR-0006 |

```sql
-- Illustrative constraint fragments for source_datasets (documentation only; not a migration)
CONSTRAINT ck_source_datasets_resource_id_format CHECK (resource_id <> '' AND resource_id !~ '\s')
CONSTRAINT ck_source_datasets_type CHECK (dataset_type IN ('CURRENT_PRICE', 'HISTORICAL_PRICE'))
```


### markets

| CHECK constraint | Basis | Rationale |
|---|---|---|
| `ck_markets_name_normalized` | ARCHITECTURAL DECISION | §15 trim rule |
| `ck_markets_slug_format` | ARCHITECTURAL DECISION | §47 Q4 (spaces become hyphens) |
| `ck_markets_coordinates_complete` | ARCHITECTURAL DECISION | coordinates travel with their provenance |
| `ck_markets_coordinate_range` | ARCHITECTURAL DECISION | valid coordinate ranges |
| `ck_markets_approximate_needs_coordinates` | ARCHITECTURAL DECISION | the flag describes existing coordinates |
| `ck_markets_review_status` | ARCHITECTURAL DECISION | ADR-0014 |
| `ck_markets_merge_target` | ARCHITECTURAL DECISION | a merge always names another market (ADR-0014) |
| `ck_markets_merged_inactive` | ARCHITECTURAL DECISION | a merged market is never a candidate (ADR-0014) |

```sql
-- Illustrative constraint fragments for markets (documentation only; not a migration)
CONSTRAINT ck_markets_name_normalized CHECK (canonical_name = btrim(regexp_replace(canonical_name, '\s+', ' ', 'g')) AND canonical_name <> '')
CONSTRAINT ck_markets_slug_format CHECK (canonical_slug <> '' AND canonical_slug !~ '\s')
CONSTRAINT ck_markets_coordinates_complete CHECK ((lat IS NULL AND lon IS NULL AND coordinate_source IS NULL) OR (lat IS NOT NULL AND lon IS NOT NULL AND coordinate_source IS NOT NULL))
CONSTRAINT ck_markets_coordinate_range CHECK ((lat IS NULL OR lat BETWEEN -90 AND 90) AND (lon IS NULL OR lon BETWEEN -180 AND 180))
CONSTRAINT ck_markets_approximate_needs_coordinates CHECK (NOT is_coordinate_approximate OR lat IS NOT NULL)
CONSTRAINT ck_markets_review_status CHECK (review_status IN ('PROVISIONAL', 'VERIFIED', 'MERGED'))
CONSTRAINT ck_markets_merge_target CHECK ((review_status = 'MERGED') = (merged_into_market_id IS NOT NULL) AND merged_into_market_id IS DISTINCT FROM id)
CONSTRAINT ck_markets_merged_inactive CHECK (review_status <> 'MERGED' OR NOT is_active)
```

Secondary indexes: `ix_markets_district_id` (see [Indexes](#5-indexes)).

### market_source_aliases

| CHECK constraint | Basis | Rationale |
|---|---|---|
| `ck_market_source_aliases_state_normalized` | ARCHITECTURAL DECISION | normalized key column |
| `ck_market_source_aliases_district_normalized` | ARCHITECTURAL DECISION | normalized key column |
| `ck_market_source_aliases_market_normalized` | ARCHITECTURAL DECISION | normalized value must derive from the stored raw value |
| `ck_market_source_aliases_evidence` | ARCHITECTURAL DECISION | ADR-0014 |

```sql
-- Illustrative constraint fragments for market_source_aliases (documentation only; not a migration)
CONSTRAINT ck_market_source_aliases_state_normalized CHECK (source_state_name = btrim(regexp_replace(source_state_name, '\s+', ' ', 'g')) AND source_state_name <> '')
CONSTRAINT ck_market_source_aliases_district_normalized CHECK (source_district_name = btrim(regexp_replace(source_district_name, '\s+', ' ', 'g')) AND source_district_name <> '')
CONSTRAINT ck_market_source_aliases_market_normalized CHECK (source_market_name = btrim(regexp_replace(source_market_name_raw, '\s+', ' ', 'g')) AND source_market_name <> '')
CONSTRAINT ck_market_source_aliases_evidence CHECK (evidence_note <> '')
```


### price_observations

| CHECK constraint | Basis | Rationale |
|---|---|---|
| `ck_price_observations_modal_positive` | REQUIREMENT FROM SSOT | §14 "price > 0", §15 "modal_price > 0" |
| `ck_price_observations_min_positive` | ARCHITECTURAL DECISION | §14 "price > 0" applied to every present price |
| `ck_price_observations_max_positive` | ARCHITECTURAL DECISION | §14 "price > 0" applied to every present price |
| `ck_price_observations_min_le_modal` | ARCHITECTURAL DECISION | holds in 505 of 505 captured records; §37 rejects inconsistent records; ingestion validates first |
| `ck_price_observations_modal_le_max` | ARCHITECTURAL DECISION | as above |
| `ck_price_observations_business_key_format` | ARCHITECTURAL DECISION | SHA-256 lowercase hex |
| `ck_price_observations_price_unit_present` | REQUIREMENT FROM SSOT | §31 RULE 3 (unit stored on every observation) |
| `ck_price_observations_quintal_floor` | ARCHITECTURAL DECISION | ADR-0008 rule V7 (defense in depth; ingestion rejects first) |
| `ck_price_observations_source_commodity_name` | ARCHITECTURAL DECISION | normalized when present (ADR-0051) |

```sql
-- Illustrative constraint fragments for price_observations (documentation only; not a migration)
CONSTRAINT ck_price_observations_modal_positive CHECK (modal_price > 0)
CONSTRAINT ck_price_observations_min_positive CHECK (min_price IS NULL OR min_price > 0)
CONSTRAINT ck_price_observations_max_positive CHECK (max_price IS NULL OR max_price > 0)
CONSTRAINT ck_price_observations_min_le_modal CHECK (min_price IS NULL OR min_price <= modal_price)
CONSTRAINT ck_price_observations_modal_le_max CHECK (max_price IS NULL OR modal_price <= max_price)
CONSTRAINT ck_price_observations_business_key_format CHECK (business_key ~ '^[0-9a-f]{64}$')
CONSTRAINT ck_price_observations_price_unit_present CHECK (price_unit <> '')
CONSTRAINT ck_price_observations_quintal_floor CHECK (price_unit <> 'Rs./Quintal' OR modal_price >= 100)
CONSTRAINT ck_price_observations_source_commodity_name CHECK (source_commodity_name IS NULL OR (source_commodity_name = btrim(regexp_replace(source_commodity_name, '\s+', ' ', 'g')) AND source_commodity_name <> ''))
```

Secondary indexes: `ix_price_observations_ingestion_run_id` (see [Indexes](#5-indexes)).

### price_observation_revisions

| CHECK constraint | Basis | Rationale |
|---|---|---|
| `ck_price_observation_revisions_prices` | ARCHITECTURAL DECISION | replaced values satisfied the observation constraints |
| `ck_price_observation_revisions_time_order` | ARCHITECTURAL DECISION | valid timeline |

```sql
-- Illustrative constraint fragments for price_observation_revisions (documentation only; not a migration)
CONSTRAINT ck_price_observation_revisions_prices CHECK (previous_modal_price > 0 AND (previous_min_price IS NULL OR (previous_min_price > 0 AND previous_min_price <= previous_modal_price)) AND (previous_max_price IS NULL OR previous_modal_price <= previous_max_price))
CONSTRAINT ck_price_observation_revisions_time_order CHECK (revised_at >= previous_ingested_at)
```

Secondary indexes: `ix_price_observation_revisions_run` (see [Indexes](#5-indexes)).

### ingestion_runs

| CHECK constraint | Basis | Rationale |
|---|---|---|
| `ck_ingestion_runs_triggered_by` | REQUIREMENT FROM SSOT | §15 |
| `ck_ingestion_runs_status` | REQUIREMENT FROM SSOT | §15, §22, §29 |
| `ck_ingestion_runs_finished_when_terminal` | ARCHITECTURAL DECISION | a run is finished exactly when it has a terminal status |
| `ck_ingestion_runs_time_order` | ARCHITECTURAL DECISION | valid timing |
| `ck_ingestion_runs_counters_nonnegative` | ARCHITECTURAL DECISION | counters cannot be negative |
| `ck_ingestion_runs_accounting` | ARCHITECTURAL DECISION | accounting invariant (ADR-0012) |
| `ck_ingestion_runs_scope_has_no_credentials` | ARCHITECTURAL DECISION | §28, §47 Q21 (key never stored); defense in depth |

```sql
-- Illustrative constraint fragments for ingestion_runs (documentation only; not a migration)
CONSTRAINT ck_ingestion_runs_triggered_by CHECK (triggered_by IN ('SCHEDULER', 'MANUAL', 'STARTUP'))
CONSTRAINT ck_ingestion_runs_status CHECK (status IN ('RUNNING', 'SUCCESS', 'PARTIAL_FAILURE', 'FAILED'))
CONSTRAINT ck_ingestion_runs_finished_when_terminal CHECK ((status = 'RUNNING') = (finished_at IS NULL))
CONSTRAINT ck_ingestion_runs_time_order CHECK (finished_at IS NULL OR finished_at >= started_at)
CONSTRAINT ck_ingestion_runs_counters_nonnegative CHECK (pages_fetched >= 0 AND pages_failed >= 0 AND records_fetched >= 0 AND records_inserted >= 0 AND records_updated >= 0 AND duplicates_skipped >= 0 AND validation_errors_count >= 0 AND records_out_of_scope >= 0 AND records_conflicted >= 0 AND (source_total_records IS NULL OR source_total_records >= 0))
CONSTRAINT ck_ingestion_runs_accounting CHECK (status NOT IN ('SUCCESS', 'PARTIAL_FAILURE') OR records_fetched = records_inserted + records_updated + duplicates_skipped + validation_errors_count + records_conflicted + records_out_of_scope)
CONSTRAINT ck_ingestion_runs_scope_has_no_credentials CHECK (NOT (request_scope ?| ARRAY['api-key', 'api_key', 'apikey']))
```

Secondary indexes: `ix_ingestion_runs_dataset_started` (see [Indexes](#5-indexes)).

### ingestion_errors

| CHECK constraint | Basis | Rationale |
|---|---|---|
| `ck_ingestion_errors_type` | ARCHITECTURAL DECISION | §15, §22 (VALIDATION, API_ERROR) extended by ADR-0009 and ADR-0011 |
| `ck_ingestion_errors_code_format` | ARCHITECTURAL DECISION | UPPER_SNAKE codes (ADR-0011) |
| `ck_ingestion_errors_page_offset` | ARCHITECTURAL DECISION | valid offset |

```sql
-- Illustrative constraint fragments for ingestion_errors (documentation only; not a migration)
CONSTRAINT ck_ingestion_errors_type CHECK (error_type IN ('VALIDATION', 'API_ERROR', 'CONFLICT', 'SCHEMA_ERROR', 'SYSTEM'))
CONSTRAINT ck_ingestion_errors_code_format CHECK (error_code ~ '^[A-Z][A-Z0-9_]*$')
CONSTRAINT ck_ingestion_errors_page_offset CHECK (page_offset IS NULL OR page_offset >= 0)
```

Secondary indexes: `ix_ingestion_errors_run`, `ix_ingestion_errors_created_at` (see [Indexes](#5-indexes)).

### ingestion_report_dates

| CHECK constraint | Basis | Rationale |
|---|---|---|
| `ck_ingestion_report_dates_outcome` | ARCHITECTURAL DECISION | `FETCHED` or `FAILED` (ADR-0054 item 1) |
| `ck_ingestion_report_dates_success_pair` | ARCHITECTURAL DECISION | a successful fetch has both its run and its time |
| `ck_ingestion_report_dates_fetched_is_success` | ARCHITECTURAL DECISION | a `FETCHED` last attempt is the last successful fetch |
| `ck_ingestion_report_dates_failures` | ARCHITECTURAL DECISION | failures are counted exactly while the last outcome is `FAILED` |
| `ck_ingestion_report_dates_records` | ARCHITECTURAL DECISION | non-negative record count |

Secondary indexes: `ix_ingestion_report_dates_attempt_run` (see [Indexes](#5-indexes)).

### ingestion_source_settings

| CHECK constraint | Basis | Rationale |
|---|---|---|
| `ck_ingestion_source_settings_reason` | ARCHITECTURAL DECISION | a pause always states its reason (ADR-0020) |

```sql
-- Illustrative constraint fragments for ingestion_source_settings (documentation only; not a migration)
CONSTRAINT ck_ingestion_source_settings_reason CHECK (is_paused = (paused_reason IS NOT NULL AND paused_reason <> ''))
```


### users

| CHECK constraint | Basis | Rationale |
|---|---|---|
| `ck_users_role` | REQUIREMENT FROM SSOT | §28, FR-20 |
| `ck_users_role_identifier` | ARCHITECTURAL DECISION | FARMER needs a phone and an email, ADMIN an email (ADR-0016, amended by ADR-0047) |
| `ck_users_phone_format` | ARCHITECTURAL DECISION | E.164 Indian mobile numbers (ADR-0016) |
| `ck_users_email_format` | ARCHITECTURAL DECISION | lower-case, minimally well-formed email (ADR-0016) |
| `ck_users_email_verified` | ARCHITECTURAL DECISION | only an existing email can be verified (ADR-0048) |
| `ck_users_email_verification_token` | ARCHITECTURAL DECISION | hash and expiry together; SHA-256 hex; only while the email is unverified (ADR-0048) |
| `ck_users_password_reset_token` | ARCHITECTURAL DECISION | hash and expiry together; SHA-256 hex; only for a verified email (ADR-0048) |

```sql
-- Illustrative constraint fragments for users (documentation only; not a migration)
CONSTRAINT ck_users_role CHECK (role IN ('FARMER', 'BUYER', 'ADMIN'))
CONSTRAINT ck_users_role_identifier CHECK ((phone_number IS NOT NULL OR email IS NOT NULL) AND (role <> 'FARMER' OR (phone_number IS NOT NULL AND email IS NOT NULL)) AND (role <> 'ADMIN' OR email IS NOT NULL))
CONSTRAINT ck_users_phone_format CHECK (phone_number IS NULL OR phone_number ~ '^\+91[6-9][0-9]{9}$')
CONSTRAINT ck_users_email_format CHECK (email IS NULL OR (email = lower(email) AND email ~ '^[^@\s]+@[^@\s]+$'))
CONSTRAINT ck_users_email_verified CHECK (email_verified_at IS NULL OR email IS NOT NULL)
CONSTRAINT ck_users_email_verification_token CHECK ((email_verification_token_hash IS NULL) = (email_verification_expires_at IS NULL) AND (email_verification_token_hash IS NULL OR (email_verification_token_hash ~ '^[0-9a-f]{64}$' AND email_verified_at IS NULL)))
CONSTRAINT ck_users_password_reset_token CHECK ((password_reset_token_hash IS NULL) = (password_reset_expires_at IS NULL) AND (password_reset_token_hash IS NULL OR (password_reset_token_hash ~ '^[0-9a-f]{64}$' AND email_verified_at IS NOT NULL)))
```


### farmer_profiles

| CHECK constraint | Basis | Rationale |
|---|---|---|
| `ck_farmer_profiles_locale` | REQUIREMENT FROM SSOT | F1.2, §43 |

```sql
-- Illustrative constraint fragments for farmer_profiles (documentation only; not a migration)
CONSTRAINT ck_farmer_profiles_locale CHECK (preferred_locale IS NULL OR preferred_locale IN ('mr', 'hi', 'en'))
```


### refresh_tokens

| CHECK constraint | Basis | Rationale |
|---|---|---|
| `ck_refresh_tokens_hash_format` | ARCHITECTURAL DECISION | SHA-256 lowercase hex |
| `ck_refresh_tokens_times` | ARCHITECTURAL DECISION | valid token timeline (ADR-0016) |

```sql
-- Illustrative constraint fragments for refresh_tokens (documentation only; not a migration)
CONSTRAINT ck_refresh_tokens_hash_format CHECK (token_hash ~ '^[0-9a-f]{64}$')
CONSTRAINT ck_refresh_tokens_times CHECK (expires_at > created_at AND (revoked_at IS NULL OR revoked_at >= created_at))
```

Secondary indexes: `ix_refresh_tokens_family` (see [Indexes](#5-indexes)).

### transport_assumptions

| CHECK constraint | Basis | Rationale |
|---|---|---|
| `ck_transport_assumptions_values` | ARCHITECTURAL DECISION | non-negative money; capacity is a divisor in §32 `trips` |

```sql
-- Illustrative constraint fragments for transport_assumptions (documentation only; not a migration)
CONSTRAINT ck_transport_assumptions_values CHECK (cost_per_km >= 0 AND minimum_trip_charge >= 0 AND vehicle_capacity_kg > 0)
```


### recommendation_weights

| CHECK constraint | Basis | Rationale |
|---|---|---|
| `ck_recommendation_weights_nonnegative` | ARCHITECTURAL DECISION | weights are magnitudes in the §16 formula (penalty is subtracted there) |
| `ck_recommendation_weights_max` | ARCHITECTURAL DECISION | admin bounds (ADR-0020) |
| `ck_recommendation_weights_freshness_order` | ARCHITECTURAL DECISION | ordered thresholds required by the §16 classification |

```sql
-- Illustrative constraint fragments for recommendation_weights (documentation only; not a migration)
CONSTRAINT ck_recommendation_weights_nonnegative CHECK (w_net_realization >= 0 AND w_price >= 0 AND w_trend >= 0 AND w_freshness >= 0 AND w_transport_penalty >= 0)
CONSTRAINT ck_recommendation_weights_max CHECK (w_net_realization <= 1 AND w_price <= 1 AND w_trend <= 1 AND w_freshness <= 1 AND w_transport_penalty <= 1)
CONSTRAINT ck_recommendation_weights_freshness_order CHECK (freshness_fresh_days >= 0 AND freshness_fresh_days <= freshness_recent_days AND freshness_recent_days <= freshness_stale_days)
```


### selling_searches

| CHECK constraint | Basis | Rationale |
|---|---|---|
| `ck_selling_searches_quantity` | ARCHITECTURAL DECISION | a comparison needs a positive quantity |
| `ck_selling_searches_unit` | REQUIREMENT FROM SSOT | §16, §25, §32 |
| `ck_selling_searches_distance` | ARCHITECTURAL DECISION | positive radius |
| `ck_selling_searches_origin_range` | ARCHITECTURAL DECISION | valid coordinate ranges |

```sql
-- Illustrative constraint fragments for selling_searches (documentation only; not a migration)
CONSTRAINT ck_selling_searches_quantity CHECK (quantity > 0)
CONSTRAINT ck_selling_searches_unit CHECK (quantity_unit IN ('KG', 'QUINTAL'))
CONSTRAINT ck_selling_searches_distance CHECK (max_distance_km > 0)
CONSTRAINT ck_selling_searches_origin_range CHECK (origin_lat BETWEEN -90 AND 90 AND origin_lon BETWEEN -180 AND 180)
```

Secondary indexes: `ix_selling_searches_farmer_profile` (see [Indexes](#5-indexes)).

### recommendation_runs

| CHECK constraint | Basis | Rationale |
|---|---|---|
| `ck_recommendation_runs_winners_together` | ARCHITECTURAL DECISION | both winners exist exactly when an option exists |
| `ck_recommendation_runs_disagree_consistent` | ARCHITECTURAL DECISION | §16 "rankingsDisagree = (priceWinner ≠ netWinner)"; protects Experiment 1 |

```sql
-- Illustrative constraint fragments for recommendation_runs (documentation only; not a migration)
CONSTRAINT ck_recommendation_runs_winners_together CHECK ((price_only_winner_market_id IS NULL) = (net_winner_market_id IS NULL))
CONSTRAINT ck_recommendation_runs_disagree_consistent CHECK (rankings_disagree = (price_only_winner_market_id IS DISTINCT FROM net_winner_market_id))
```


### selling_options

| CHECK constraint | Basis | Rationale |
|---|---|---|
| `ck_selling_options_rank` | REQUIREMENT FROM SSOT | §16 ranks 1..N |
| `ck_selling_options_amounts` | ARCHITECTURAL DECISION | non-negative distances and amounts (net realization may be negative) |
| `ck_selling_options_trend` | REQUIREMENT FROM SSOT | §16, §33; ADR-0025 |
| `ck_selling_options_freshness` | REQUIREMENT FROM SSOT | §16, F1.16 |
| `ck_selling_options_reason_codes` | REQUIREMENT FROM SSOT | §16 fixed vocabulary (8 codes) |

```sql
-- Illustrative constraint fragments for selling_options (documentation only; not a migration)
CONSTRAINT ck_selling_options_rank CHECK (rank >= 1)
CONSTRAINT ck_selling_options_amounts CHECK (distance_km >= 0 AND estimated_transport_cost >= 0 AND estimated_gross_value >= 0)
CONSTRAINT ck_selling_options_trend CHECK (trend_direction IN ('RISING', 'STABLE', 'FALLING', 'INSUFFICIENT_DATA'))
CONSTRAINT ck_selling_options_freshness CHECK (freshness_status IN ('FRESH', 'RECENT', 'STALE', 'UNAVAILABLE'))
CONSTRAINT ck_selling_options_reason_codes CHECK (reason_codes <@ ARRAY['PRICE_ADVANTAGE', 'LOWER_TRANSPORT', 'POSITIVE_TREND', 'STABLE_TREND', 'RECENT_DATA', 'SUITABLE_DISTANCE', 'NEGATIVE_TREND', 'STALE_DATA']::text[])
```


### price_alerts

| CHECK constraint | Basis | Rationale |
|---|---|---|
| `ck_price_alerts_type` | REQUIREMENT FROM SSOT | §34 |
| `ck_price_alerts_status` | REQUIREMENT FROM SSOT | §34 |
| `ck_price_alerts_threshold` | ARCHITECTURAL DECISION | prices are > 0 (§14); percentage thresholds are magnitudes (§34 uses −thresholdValue for falls) |

```sql
-- Illustrative constraint fragments for price_alerts (documentation only; not a migration)
CONSTRAINT ck_price_alerts_type CHECK (alert_type IN ('PRICE_ABOVE', 'PRICE_BELOW', 'PERCENTAGE_RISE', 'PERCENTAGE_FALL'))
CONSTRAINT ck_price_alerts_status CHECK (status IN ('ACTIVE', 'PAUSED'))
CONSTRAINT ck_price_alerts_threshold CHECK (threshold_value > 0)
```

Secondary indexes: `ix_price_alerts_farmer_profile`, `ix_price_alerts_active_commodity` (see [Indexes](#5-indexes)).

### alert_events

No CHECK constraints.

Secondary indexes: `ix_alert_events_alert_triggered` (see [Indexes](#5-indexes)).

### admin_audit_events

| CHECK constraint | Basis | Rationale |
|---|---|---|
| `ck_admin_audit_events_action` | ARCHITECTURAL DECISION | closed vocabulary (ADR-0020); extended only by migration. ADR-0041 adds `ACCOUNT_DEACTIVATED` and `ACCOUNT_REACTIVATED` |
| `ck_admin_audit_events_target_type` | ARCHITECTURAL DECISION | every action names its target kind |
| `ck_admin_audit_events_no_credentials` | ARCHITECTURAL DECISION | secrets never stored (§28, CLAUDE.md §K); defense in depth |

```sql
-- Illustrative constraint fragments for admin_audit_events (documentation only; not a migration)
CONSTRAINT ck_admin_audit_events_action CHECK (action IN ('INGESTION_TRIGGERED', 'INGESTION_PAUSED', 'INGESTION_RESUMED', 'TRANSPORT_ASSUMPTIONS_UPDATED', 'RECOMMENDATION_WEIGHTS_UPDATED', 'MARKET_VERIFIED', 'MARKET_MERGED', 'MARKET_DEACTIVATED', 'MARKET_REACTIVATED', 'MARKET_COORDINATES_SET', 'COMMODITY_ADDED', 'COMMODITY_UNIT_VERIFIED', 'COMMODITY_NAMES_UPDATED', 'DISTRICT_ALIAS_ADDED', 'ADMIN_BOOTSTRAPPED', 'ACCOUNT_ERASED', 'ACCOUNT_DEACTIVATED', 'ACCOUNT_REACTIVATED'))
CONSTRAINT ck_admin_audit_events_target_type CHECK (target_type <> '')
CONSTRAINT ck_admin_audit_events_no_credentials CHECK (NOT (details ?| ARRAY['password', 'password_hash', 'api-key', 'api_key', 'apikey', 'secret', 'token', 'refresh_token']))
```

Secondary indexes: `ix_admin_audit_events_created_at`, `ix_admin_audit_events_target` (see [Indexes](#5-indexes)).

### forecasts

| CHECK constraint | Basis | Rationale |
|---|---|---|
| `ck_forecasts_horizon` | REQUIREMENT FROM SSOT | §35 |
| `ck_forecasts_after_training` | ARCHITECTURAL DECISION | §35 chronological validation; §47 Q14 no leakage |
| `ck_forecasts_bounds` | ARCHITECTURAL DECISION | bounds enclose the prediction |
| `ck_forecasts_errors_nonnegative` | ARCHITECTURAL DECISION | error metrics are non-negative |

```sql
-- Illustrative constraint fragments for forecasts (documentation only; not a migration)
CONSTRAINT ck_forecasts_horizon CHECK (forecast_horizon_days IN (3, 7))
CONSTRAINT ck_forecasts_after_training CHECK (forecast_date > training_data_through)
CONSTRAINT ck_forecasts_bounds CHECK ((lower_bound IS NULL OR lower_bound <= predicted_price) AND (upper_bound IS NULL OR predicted_price <= upper_bound))
CONSTRAINT ck_forecasts_errors_nonnegative CHECK ((mae IS NULL OR mae >= 0) AND (rmse IS NULL OR rmse >= 0))
```


## 7. Query patterns and the indexes that serve them

| # | Query pattern (Task 7 list first) | Expected query | Index used | Phase 1 required |
|---|---|---|---|---|
| Q1 | Latest price by market + commodity (§16 freshest observation; F1.7; §34 latest modal price) | `SELECT … FROM price_observations WHERE market_id = $1 AND commodity_id = $2 AND report_date >= $3 ORDER BY report_date DESC LIMIT 1` (dataset precedence, variety and grade rules per ADR-0010) | `uq_price_observations_identity` | yes |
| Q2 | Historical trend by market + commodity + date range (§33; §21 steps 13, 30) | `SELECT report_date, modal_price … WHERE market_id = $1 AND commodity_id = $2 AND report_date BETWEEN $3 AND $4 ORDER BY report_date` | `uq_price_observations_identity` | yes |
| Q3 | Current observations by geography (district, state) | markets of the district (`ix_markets_district_id`; districts of a state via `uq_districts_state_name`), then Q1 per market | `ix_markets_district_id`, `uq_districts_state_name`, `uq_price_observations_identity` | yes |
| Q4 | Comparison candidate retrieval (§16 haversine within radius, active markets) | `SELECT id, lat, lon FROM markets WHERE is_active AND review_status = 'VERIFIED' AND lat IS NOT NULL`, distance filter in the transport module, then Q1 per candidate | none for `markets` (sequential scan of a small table, by design); Q1 index per candidate | yes |
| Q5 | Alerts against latest observations (§15, §22, §34) | observations inserted (`WHERE ingestion_run_id = $1`) or revised (`price_observation_revisions WHERE ingestion_run_id = $1`) by the finished run, ACTIVE alerts for their commodities, then Q1/Q2 for latest price and 7-day change (ADR-0019) | `ix_price_observations_ingestion_run_id`, `ix_price_observation_revisions_run`, `ix_price_alerts_active_commodity`, `uq_price_observations_identity` | yes (alerts are a Should feature) |
| Q6 | Ingestion / business-key lookup (§14, §15, §22) | `INSERT … ON CONFLICT (business_key) …`; alias resolution `WHERE source_state_name = $1 AND source_district_name = $2 AND source_market_name = $3`; exact commodity, variety, grade lookup | `uq_price_observations_business_key`, `uq_market_source_aliases_source`, `uq_commodities_canonical_name`, `uq_varieties_commodity_name`, `uq_grades_commodity_name`, `uq_source_datasets_resource_id` | yes |
| Q7 | Reference-data lookup (§30 geography and commodity groups) | states; districts of a state; localities of a district; commodities; varieties and grades of a commodity; market by slug | `uq_states_code`, `uq_states_name`, `uq_districts_state_name`, `uq_localities_district_name`, `uq_commodities_canonical_name`, `uq_varieties_commodity_name`, `uq_grades_commodity_name`, `uq_markets_canonical_slug` | yes |
| Q8 | Market-by-district lookup | `SELECT … FROM markets WHERE district_id = $1` | `ix_markets_district_id` | yes |
| Q9 | Admin run history and last sync (FR-18, §46) | `… FROM ingestion_runs WHERE source_dataset_id = $1 ORDER BY started_at DESC LIMIT 5` | `ix_ingestion_runs_dataset_started` | yes |
| Q10 | Errors of a run (FR-18) | `… FROM ingestion_errors WHERE ingestion_run_id = $1` | `ix_ingestion_errors_run` | yes |
| Q10a | Counter verification (§48 admin health, ADR-0012) | inserted rows `WHERE ingestion_run_id = $1` on observations; revisions `WHERE ingestion_run_id = $1` | `ix_price_observations_ingestion_run_id`, `ix_price_observation_revisions_run` | yes |
| Q11 | A farmer's alerts and alert history (F1.19) | `… FROM price_alerts WHERE farmer_profile_id = $1`; `… FROM alert_events WHERE price_alert_id = $1 ORDER BY triggered_at DESC` | `ix_price_alerts_farmer_profile`, `ix_alert_events_alert_triggered` | yes (Should feature) |
| Q12 | Login and token refresh (§10, §28); email verification and password recovery (ADR-0048) | user by phone or email; token by hash; family revocation on reuse (ADR-0016); user by verification or reset token hash | `uq_users_phone_number`, `uq_users_email`, `uq_refresh_tokens_token_hash`, `ix_refresh_tokens_family`, `uq_users_email_verification_token_hash`, `uq_users_password_reset_token_hash` | yes |
| Q13 | Active configuration (§16) | `… FROM transport_assumptions WHERE is_active`; same for weights | tiny tables; the partial unique indexes guarantee one row | yes |
| Q14 | A farmer's latest recommendation run (§47 Q23, Phase 2 linkage) | see `ix_selling_searches_farmer_profile` | `ix_selling_searches_farmer_profile` | no (Phase 2 need) |
| Q14a | Admin audit view and object history (ADR-0020) | `… FROM admin_audit_events ORDER BY created_at DESC`; `… WHERE target_type = $1 AND target_id = $2` | `ix_admin_audit_events_created_at`, `ix_admin_audit_events_target` | yes |
| Q14b | Daily purges (ADR-0029, ADR-0048) | `DELETE FROM ingestion_errors WHERE created_at < …`; alert events and refresh tokens by age; expired email-verification and password-reset columns of `users` set to NULL | `ix_ingestion_errors_created_at`; alert events, tokens and accounts are small (sequential scan) | yes |
| Q15 | Latest forecast for market + commodity (optional, §35) | `… FROM forecasts WHERE market_id = $1 AND commodity_id = $2 AND forecast_horizon_days = $3 ORDER BY generated_at DESC` | `uq_forecasts_point` | no (optional ML) |

**Stale-markets figure (§46 "3 markets STALE").** The latest `report_date` per market is a grouped scan of `uq_price_observations_identity`. At the estimated volume (about 10⁵ rows per year) this is acceptable for an admin page; no summary table is added. Revisit only if measurements show otherwise.

## 8. Indexes considered and not created

| Candidate | Why not |
|---|---|
| `price_observations (market_id, report_date DESC)` (evidence suggestion) | Redundant: the leading columns of `uq_price_observations_identity` plus a backward scan serve it; single-direction `DESC` indexes add nothing (VERIFIED TECHNICAL FACT, PostgreSQL 16 `CREATE INDEX` notes) |
| `price_observations (market_id, commodity_id, report_date DESC)` (evidence suggestion) | Same columns as the identity index prefix |
| `price_observations (commodity_id, report_date)` (evidence suggestion) | No Phase 1 query reads one commodity across all markets by date; comparison and alerts go market by market. Add if Task 8 defines a statewide commodity listing |
| Indexes on `price_observations.variety_id`, `grade_id`, `source_dataset_id` | No query pattern; referenced reference rows are never deleted, so FK checks on delete are rare administrative events |
| Indexes on `recommendation_runs` and `selling_options` foreign keys beyond the unique constraints | No Phase 1 query; decision records are retained, not purged (ADR-0029) |
| `markets (lat, lon)` bounding-box index; any spatial index | The table is small; PostGIS is out of scope |
| `lower(email)` unique index on `users` | Not needed: emails are stored in lower case and a CHECK enforces it (ADR-0016) |
| `refresh_tokens (user_id)`, `refresh_tokens (expires_at)` | No Phase 1 query needs them; the daily purge of expired tokens scans a small table (ADR-0029) |
| GIN index on `selling_options.reason_codes` | No query filters by reason code; Experiment 1 scans whole tables offline |

## 9. Rules kept in ingestion validation

These are enforced in code before a row reaches PostgreSQL, because the source can legitimately send them, or because a CHECK cannot express them ([DATABASE_ARCHITECTURE.md §17](DATABASE_ARCHITECTURE.md#17-integrity-rules)):

- date parsing (`dd/MM/yyyy`, strict) and future-dated records (ADR-0007);
- unit verification, the unit floor (V7, also a CHECK) and the relative outlier rule (V8), and price rounding to 2 decimals (ADR-0008);
- required source fields and JSON types; schema drift fails loudly (§37; evidence §19.7);
- unknown commodities and districts (rejected; a legacy commodity spelling with DMI evidence resolves through `commodity_source_aliases`, ADR-0051), new markets (registered PROVISIONAL), new varieties and grades (registered) (ADR-0006, ADR-0014);
- within-delivery duplicates and same-key conflicts (ADR-0009);
- out-of-scope records after exact matching (`records_out_of_scope`);
- cross-row rules: winners belong to the run's options; farmer profiles belong to FARMER accounts.

## 10. Illustrative constraint fragments

Primary keys, unique constraints, foreign keys and indexes, table by table, for precision. CHECK constraints are in section 6. Documentation only; not executable as written.

```sql
-- states (owner: geography)
CONSTRAINT pk_states PRIMARY KEY (id)
CONSTRAINT uq_states_code UNIQUE (code)
CONSTRAINT uq_states_name UNIQUE (name)
CONSTRAINT uq_states_lgd_code UNIQUE (lgd_code)

-- districts (owner: geography)
CONSTRAINT pk_districts PRIMARY KEY (id)
CONSTRAINT uq_districts_state_name UNIQUE (state_id, name)
CONSTRAINT uq_districts_lgd_code UNIQUE (lgd_code)
CONSTRAINT fk_districts_state FOREIGN KEY (state_id) REFERENCES states (id) ON DELETE RESTRICT

-- localities (owner: geography)
CONSTRAINT pk_localities PRIMARY KEY (id)
CONSTRAINT uq_localities_district_name UNIQUE (district_id, name)
CONSTRAINT uq_localities_lgd_code UNIQUE (lgd_code)
CONSTRAINT fk_localities_district FOREIGN KEY (district_id) REFERENCES districts (id) ON DELETE RESTRICT

-- district_source_aliases (owner: geography)
CONSTRAINT pk_district_source_aliases PRIMARY KEY (id)
CONSTRAINT uq_district_source_aliases_source UNIQUE (source_state_name, source_district_name)
CONSTRAINT fk_district_source_aliases_district FOREIGN KEY (district_id) REFERENCES districts (id) ON DELETE RESTRICT

-- commodities (owner: commodity)
CONSTRAINT pk_commodities PRIMARY KEY (id)
CONSTRAINT uq_commodities_canonical_name UNIQUE (canonical_name)

-- varieties (owner: commodity)
CONSTRAINT pk_varieties PRIMARY KEY (id)
CONSTRAINT uq_varieties_commodity_name UNIQUE (commodity_id, canonical_name)
CONSTRAINT uq_varieties_id_commodity UNIQUE (id, commodity_id)
CONSTRAINT fk_varieties_commodity FOREIGN KEY (commodity_id) REFERENCES commodities (id) ON DELETE RESTRICT

-- grades (owner: commodity)
CONSTRAINT pk_grades PRIMARY KEY (id)
CONSTRAINT uq_grades_commodity_name UNIQUE (commodity_id, canonical_name)
CONSTRAINT uq_grades_id_commodity UNIQUE (id, commodity_id)
CONSTRAINT fk_grades_commodity FOREIGN KEY (commodity_id) REFERENCES commodities (id) ON DELETE RESTRICT

-- source_datasets (owner: price)
CONSTRAINT pk_source_datasets PRIMARY KEY (id)
CONSTRAINT uq_source_datasets_resource_id UNIQUE (resource_id)
CONSTRAINT uq_source_datasets_dataset_type UNIQUE (dataset_type)

-- markets (owner: price)
CONSTRAINT pk_markets PRIMARY KEY (id)
CONSTRAINT uq_markets_canonical_slug UNIQUE (canonical_slug)
CONSTRAINT fk_markets_district FOREIGN KEY (district_id) REFERENCES districts (id) ON DELETE RESTRICT
CONSTRAINT fk_markets_merged_into FOREIGN KEY (merged_into_market_id) REFERENCES markets (id) ON DELETE RESTRICT
CREATE INDEX ix_markets_district_id ON markets (district_id);

-- market_source_aliases (owner: price)
CONSTRAINT pk_market_source_aliases PRIMARY KEY (id)
CONSTRAINT uq_market_source_aliases_source UNIQUE (source_state_name, source_district_name, source_market_name)
CONSTRAINT fk_market_source_aliases_market FOREIGN KEY (market_id) REFERENCES markets (id) ON DELETE RESTRICT

-- price_observations (owner: price)
CONSTRAINT pk_price_observations PRIMARY KEY (id)
CONSTRAINT uq_price_observations_business_key UNIQUE (business_key)
CONSTRAINT uq_price_observations_identity UNIQUE NULLS NOT DISTINCT (market_id, commodity_id, report_date, variety_id, grade_id, source_dataset_id)
CONSTRAINT fk_price_observations_market FOREIGN KEY (market_id) REFERENCES markets (id) ON DELETE RESTRICT
CONSTRAINT fk_price_observations_commodity FOREIGN KEY (commodity_id) REFERENCES commodities (id) ON DELETE RESTRICT
CONSTRAINT fk_price_observations_variety FOREIGN KEY (variety_id, commodity_id) REFERENCES varieties (id, commodity_id) ON DELETE RESTRICT
CONSTRAINT fk_price_observations_grade FOREIGN KEY (grade_id, commodity_id) REFERENCES grades (id, commodity_id) ON DELETE RESTRICT
CONSTRAINT fk_price_observations_source_dataset FOREIGN KEY (source_dataset_id) REFERENCES source_datasets (id) ON DELETE RESTRICT
CONSTRAINT fk_price_observations_ingestion_run FOREIGN KEY (ingestion_run_id) REFERENCES ingestion_runs (id) ON DELETE RESTRICT
CREATE INDEX ix_price_observations_ingestion_run_id ON price_observations (ingestion_run_id);

-- price_observation_revisions (owner: price)
CONSTRAINT pk_price_observation_revisions PRIMARY KEY (id)
CONSTRAINT uq_price_observation_revisions_observation_run UNIQUE (price_observation_id, ingestion_run_id)
CONSTRAINT fk_price_observation_revisions_observation FOREIGN KEY (price_observation_id) REFERENCES price_observations (id) ON DELETE RESTRICT
CONSTRAINT fk_price_observation_revisions_run FOREIGN KEY (ingestion_run_id) REFERENCES ingestion_runs (id) ON DELETE RESTRICT
CREATE INDEX ix_price_observation_revisions_run ON price_observation_revisions (ingestion_run_id);

-- ingestion_runs (owner: ingestion)
CONSTRAINT pk_ingestion_runs PRIMARY KEY (id)
CREATE UNIQUE INDEX uq_ingestion_runs_one_running ON ingestion_runs (source_dataset_id) WHERE status = 'RUNNING';
CONSTRAINT fk_ingestion_runs_source_dataset FOREIGN KEY (source_dataset_id) REFERENCES source_datasets (id) ON DELETE RESTRICT
CREATE INDEX ix_ingestion_runs_dataset_started ON ingestion_runs (source_dataset_id, started_at);

-- ingestion_errors (owner: ingestion)
CONSTRAINT pk_ingestion_errors PRIMARY KEY (id)
CONSTRAINT fk_ingestion_errors_run FOREIGN KEY (ingestion_run_id) REFERENCES ingestion_runs (id) ON DELETE CASCADE
CREATE INDEX ix_ingestion_errors_run ON ingestion_errors (ingestion_run_id);
CREATE INDEX ix_ingestion_errors_created_at ON ingestion_errors (created_at);

-- ingestion_source_settings (owner: ingestion)
CONSTRAINT pk_ingestion_source_settings PRIMARY KEY (id)
CONSTRAINT uq_ingestion_source_settings_dataset UNIQUE (source_dataset_id)
CONSTRAINT fk_ingestion_source_settings_dataset FOREIGN KEY (source_dataset_id) REFERENCES source_datasets (id) ON DELETE RESTRICT

-- users (owner: auth)
CONSTRAINT pk_users PRIMARY KEY (id)
CONSTRAINT uq_users_phone_number UNIQUE (phone_number)
CONSTRAINT uq_users_email UNIQUE (email)
CONSTRAINT uq_users_email_verification_token_hash UNIQUE (email_verification_token_hash)
CONSTRAINT uq_users_password_reset_token_hash UNIQUE (password_reset_token_hash)

-- farmer_profiles (owner: auth)
CONSTRAINT pk_farmer_profiles PRIMARY KEY (id)
CONSTRAINT uq_farmer_profiles_user UNIQUE (user_id)
CONSTRAINT fk_farmer_profiles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
CONSTRAINT fk_farmer_profiles_default_district FOREIGN KEY (default_district_id) REFERENCES districts (id) ON DELETE RESTRICT

-- refresh_tokens (owner: auth)
CONSTRAINT pk_refresh_tokens PRIMARY KEY (id)
CONSTRAINT uq_refresh_tokens_token_hash UNIQUE (token_hash)
CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
CREATE INDEX ix_refresh_tokens_family ON refresh_tokens (family_id);

-- transport_assumptions (owner: transport)
CONSTRAINT pk_transport_assumptions PRIMARY KEY (id)
CREATE UNIQUE INDEX uq_transport_assumptions_single_active ON transport_assumptions ((is_active)) WHERE is_active;

-- recommendation_weights (owner: recommendation)
CONSTRAINT pk_recommendation_weights PRIMARY KEY (id)
CREATE UNIQUE INDEX uq_recommendation_weights_single_active ON recommendation_weights ((is_active)) WHERE is_active;

-- selling_searches (owner: recommendation)
CONSTRAINT pk_selling_searches PRIMARY KEY (id)
CONSTRAINT fk_selling_searches_farmer_profile FOREIGN KEY (farmer_profile_id) REFERENCES farmer_profiles (id) ON DELETE SET NULL
CONSTRAINT fk_selling_searches_commodity FOREIGN KEY (commodity_id) REFERENCES commodities (id) ON DELETE RESTRICT
CONSTRAINT fk_selling_searches_variety FOREIGN KEY (variety_id, commodity_id) REFERENCES varieties (id, commodity_id) ON DELETE RESTRICT
CONSTRAINT fk_selling_searches_grade FOREIGN KEY (grade_id, commodity_id) REFERENCES grades (id, commodity_id) ON DELETE RESTRICT
CREATE INDEX ix_selling_searches_farmer_profile ON selling_searches (farmer_profile_id);

-- recommendation_runs (owner: recommendation)
CONSTRAINT pk_recommendation_runs PRIMARY KEY (id)
CONSTRAINT fk_recommendation_runs_search FOREIGN KEY (selling_search_id) REFERENCES selling_searches (id) ON DELETE CASCADE
CONSTRAINT fk_recommendation_runs_weights FOREIGN KEY (weights_id) REFERENCES recommendation_weights (id) ON DELETE RESTRICT
CONSTRAINT fk_recommendation_runs_transport FOREIGN KEY (transport_id) REFERENCES transport_assumptions (id) ON DELETE RESTRICT
CONSTRAINT fk_recommendation_runs_price_winner FOREIGN KEY (price_only_winner_market_id) REFERENCES markets (id) ON DELETE RESTRICT
CONSTRAINT fk_recommendation_runs_net_winner FOREIGN KEY (net_winner_market_id) REFERENCES markets (id) ON DELETE RESTRICT

-- selling_options (owner: recommendation)
CONSTRAINT pk_selling_options PRIMARY KEY (id)
CONSTRAINT uq_selling_options_run_market UNIQUE (recommendation_run_id, market_id)
CONSTRAINT uq_selling_options_run_rank UNIQUE (recommendation_run_id, rank)
CONSTRAINT fk_selling_options_run FOREIGN KEY (recommendation_run_id) REFERENCES recommendation_runs (id) ON DELETE CASCADE
CONSTRAINT fk_selling_options_market FOREIGN KEY (market_id) REFERENCES markets (id) ON DELETE RESTRICT
CONSTRAINT fk_selling_options_observation FOREIGN KEY (price_observation_id) REFERENCES price_observations (id) ON DELETE RESTRICT

-- price_alerts (owner: alert)
CONSTRAINT pk_price_alerts PRIMARY KEY (id)
CONSTRAINT fk_price_alerts_farmer_profile FOREIGN KEY (farmer_profile_id) REFERENCES farmer_profiles (id) ON DELETE CASCADE
CONSTRAINT fk_price_alerts_commodity FOREIGN KEY (commodity_id) REFERENCES commodities (id) ON DELETE RESTRICT
CONSTRAINT fk_price_alerts_market FOREIGN KEY (market_id) REFERENCES markets (id) ON DELETE RESTRICT
CREATE INDEX ix_price_alerts_farmer_profile ON price_alerts (farmer_profile_id);
CREATE INDEX ix_price_alerts_active_commodity ON price_alerts (commodity_id) WHERE status = 'ACTIVE';

-- alert_events (owner: alert)
CONSTRAINT pk_alert_events PRIMARY KEY (id)
CONSTRAINT uq_alert_events_alert_run UNIQUE (price_alert_id, ingestion_run_id)
CONSTRAINT fk_alert_events_alert FOREIGN KEY (price_alert_id) REFERENCES price_alerts (id) ON DELETE CASCADE
CONSTRAINT fk_alert_events_run FOREIGN KEY (ingestion_run_id) REFERENCES ingestion_runs (id) ON DELETE RESTRICT
CONSTRAINT fk_alert_events_market FOREIGN KEY (market_id) REFERENCES markets (id) ON DELETE RESTRICT
CONSTRAINT fk_alert_events_observation FOREIGN KEY (price_observation_id) REFERENCES price_observations (id) ON DELETE RESTRICT
CREATE INDEX ix_alert_events_alert_triggered ON alert_events (price_alert_id, triggered_at);

-- admin_audit_events (owner: admin)
CONSTRAINT pk_admin_audit_events PRIMARY KEY (id)
CONSTRAINT fk_admin_audit_events_actor FOREIGN KEY (actor_user_id) REFERENCES users (id) ON DELETE RESTRICT
CREATE INDEX ix_admin_audit_events_created_at ON admin_audit_events (created_at);
CREATE INDEX ix_admin_audit_events_target ON admin_audit_events (target_type, target_id);

-- forecasts (owner: price)
CONSTRAINT pk_forecasts PRIMARY KEY (id)
CONSTRAINT uq_forecasts_point UNIQUE (market_id, commodity_id, forecast_horizon_days, generated_at, forecast_date)
CONSTRAINT fk_forecasts_market FOREIGN KEY (market_id) REFERENCES markets (id) ON DELETE RESTRICT
CONSTRAINT fk_forecasts_commodity FOREIGN KEY (commodity_id) REFERENCES commodities (id) ON DELETE RESTRICT
```
