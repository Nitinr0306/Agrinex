# AgriNex — Data Dictionary

**Status:** AUTHORITATIVE. Database architecture from Task 7 (2026-10-03), approved in the decision lock of the same day (ADR-0001, ADR-0013; [DECISION_LOG.md](../architecture/DECISION_LOG.md)). Where this document and a binding decision-log entry differ, the entry governs. Design rationale: [DATABASE_ARCHITECTURE.md](DATABASE_ARCHITECTURE.md). Diagrams: [ERD.md](ERD.md), generated from the same table model as this dictionary; a later change to either must be made in both. Nothing here is implemented.

Coverage: 28 tables (27 Phase 1, 1 optional), 235 columns, of which 89 are ARCHITECTURAL DECISION columns not named by the spec. Revised on 2026-10-04 by ADR-0047 and ADR-0048 (an email for every FARMER; email-verification and password-reset columns on `users`), and on 2026-10-15 by ADR-0051 (`commodity_source_aliases` and `price_observations.source_commodity_name`).

## 1. Conventions

### 1.1 Basis of each column

| Basis | Meaning |
|---|---|
| REQUIREMENT FROM SSOT | The spec names the column: §20 for most columns; §15 (`ingestion_runs.triggered_by`) and §35 (`forecasts`) for the others. Precision, nullability or default may still be an ARCHITECTURAL DECISION; the source column says so |
| ARCHITECTURAL DECISION | The spec does not name the column. It was introduced by this document set for the stated reason and approved in the decision lock (ADR-0013 and the entry cited in the source column) |
| ADR-NNNN | Appears in the source column when a value, vocabulary or meaning was settled by a decision-log entry ([DECISION_LOG.md](../architecture/DECISION_LOG.md)) |

### 1.2 Sensitivity classification (ARCHITECTURAL DECISION)

The spec requires PII minimization and secret protection (§28, §47 Q21) but defines no classification scheme. This dictionary uses four classes:

| Class | Meaning | Handling |
|---|---|---|
| PUBLIC | Government-published data or public reference data | No restriction beyond integrity |
| INTERNAL | Operational, configuration or audit data about AgriNex itself | Exposed only through the owning module's API to the roles §28 allows (for example admin health, ADMIN only) |
| PERSONAL | Identifies or describes a registered person, or links a location to one | Minimized (§28: no address, district granularity only); returned only to the owner or as §28 allows; never logged in full |
| CREDENTIAL | Secret-derived values (password hash, token hash) | Never returned by any API, never logged, never copied into documentation; security review required (CLAUDE.md rule 16) |

### 1.3 Types, defaults and units

- **Keys:** every primary key is `uuid` (§20), default `gen_random_uuid()` (PostgreSQL core function, VERIFIED TECHNICAL FACT, PostgreSQL 16 §9.14).
- **Money and prices:** `numeric(p,s)`, mapped to Java `BigDecimal`. No floating-point column exists. Government prices (`price_observations`, `price_observation_revisions`, `forecasts`, `price_alerts.threshold_value` for price types) are in the unit stored on each observation, copied from the commodity's evidence-verified unit. Every unit verified so far is `Rs./Quintal` (ADR-0008). Prices are parsed as exact decimals and rounded half-up to 2 dp; they are never converted. Transport parameters and estimates are in rupees, as §32 states (₹8/km, ₹300).
- **Dates:** `date` for source observation dates (`report_date`), never text. `timestamptz` for system times.
- **Controlled values** are `varchar` with CHECK lists rather than PostgreSQL enum types, so a vocabulary change is an ordinary migration (ARCHITECTURAL DECISION). The lists are in [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md).
- **Text normalization:** canonical and normalized text columns hold whitespace-normalized values: runs of whitespace collapsed to one space, ends trimmed, case and punctuation unchanged. CHECK constraints enforce this ([DATABASE_ARCHITECTURE.md §9.2](DATABASE_ARCHITECTURE.md#92-normalization-rule-architectural-decision)).
- **Defaults** shown as `—` mean none: the writer must supply a value.

## 2. Table index

| Table | Group | Owning module | Phase | Status | Columns |
|---|---|---|---|---|---|
| [`states`](#states) | Geography reference | geography | Phase 1 | REQUIRED | 6 |
| [`districts`](#districts) | Geography reference | geography | Phase 1 | REQUIRED | 7 |
| [`localities`](#localities) | Geography reference | geography | Phase 1 | REQUIRED | 7 |
| [`district_source_aliases`](#district_source_aliases) | Geography reference | geography | Phase 1 | REQUIRED | 6 |
| [`commodities`](#commodities) | Commodity reference | commodity | Phase 1 | REQUIRED | 8 |
| [`varieties`](#varieties) | Commodity reference | commodity | Phase 1 | REQUIRED | 4 |
| [`grades`](#grades) | Commodity reference | commodity | Phase 1 | REQUIRED | 4 |
| [`commodity_source_aliases`](#commodity_source_aliases) | Commodity reference | commodity | Phase 1 | REQUIRED | 5 |
| [`source_datasets`](#source_datasets) | Government source and observations | price | Phase 1 | REQUIRED | 4 |
| [`markets`](#markets) | Government source and observations | price | Phase 1 | REQUIRED | 12 |
| [`market_source_aliases`](#market_source_aliases) | Government source and observations | price | Phase 1 | REQUIRED | 8 |
| [`price_observations`](#price_observations) | Government source and observations | price | Phase 1 | REQUIRED | 15 |
| [`price_observation_revisions`](#price_observation_revisions) | Government source and observations | price | Phase 1 | REQUIRED | 8 |
| [`ingestion_runs`](#ingestion_runs) | Ingestion audit | ingestion | Phase 1 | REQUIRED | 17 |
| [`ingestion_errors`](#ingestion_errors) | Ingestion audit | ingestion | Phase 1 | REQUIRED | 9 |
| [`ingestion_source_settings`](#ingestion_source_settings) | Ingestion audit | ingestion | Phase 1 | REQUIRED | 5 |
| [`ingestion_report_dates`](#ingestion_report_dates) | Ingestion audit | ingestion | Phase 1 | REQUIRED | 10 |
| [`users`](#users) | Identity and access | auth | Phase 1 | REQUIRED | 12 |
| [`farmer_profiles`](#farmer_profiles) | Identity and access | auth | Phase 1 | REQUIRED | 5 |
| [`refresh_tokens`](#refresh_tokens) | Identity and access | auth | Phase 1 | REQUIRED | 7 |
| [`transport_assumptions`](#transport_assumptions) | Recommendation configuration | transport | Phase 1 | REQUIRED | 6 |
| [`recommendation_weights`](#recommendation_weights) | Recommendation configuration | recommendation | Phase 1 | REQUIRED | 11 |
| [`selling_searches`](#selling_searches) | Recommendation results | recommendation | Phase 1 | REQUIRED | 10 |
| [`recommendation_runs`](#recommendation_runs) | Recommendation results | recommendation | Phase 1 | REQUIRED | 8 |
| [`selling_options`](#selling_options) | Recommendation results | recommendation | Phase 1 | REQUIRED | 13 |
| [`price_alerts`](#price_alerts) | Alerts | alert | Phase 1 | REQUIRED | 8 |
| [`alert_events`](#alert_events) | Alerts | alert | Phase 1 | REQUIRED | 8 |
| [`admin_audit_events`](#admin_audit_events) | Administration | admin | Phase 1 | REQUIRED | 8 |
| [`forecasts`](#forecasts) | Optional ML | price | Optional (ML) | OPTIONAL | 14 |

## 3. Tables

### states

| Field | Value |
|---|---|
| Purpose | States selectable in the location wizard (State → District → Locality). Phase 1 holds Maharashtra; more states are added as rows, not schema changes. |
| Owning module | geography |
| Phase | Phase 1 |
| Status | REQUIRED |
| Table group | Geography reference |
| Source / requirement | §20 STATES; F1.3; §30 StateResponse; §27 (additional states without code changes); §47 Q20; Task 7 Maharashtra scope directive |
| Natural key | `code` (spec UK); `name` (ARCHITECTURAL DECISION) |
| Record class | Reference data (curated) |
| Deletion behavior | Never deleted while referenced (`ON DELETE RESTRICT` from `districts`). No deactivation flag exists in §20. Retained indefinitely (ADR-0029). |

| Column | Type | Nullable | Default | PK | FK | Unique | Description | Basis | Source / requirement | Sensitivity |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | uuid | NO | `gen_random_uuid()` | YES | — | — | Surrogate primary key. | REQUIREMENT FROM SSOT | §20 (uuid PK); default value generation: ARCHITECTURAL DECISION | PUBLIC |
| `name` | varchar(100) | NO | — | — | — | YES | State name, whitespace-normalized. Phase 1 value: `Maharashtra` (spelling as in §1 and in every evidence record). | REQUIREMENT FROM SSOT | §20; uniqueness: ARCHITECTURAL DECISION | PUBLIC |
| `name_hi` | varchar(100) | YES | — | — | — | — | Hindi name, curated with recorded provenance; NULL means the English `name` is shown (ADR-0015). | REQUIREMENT FROM SSOT | §20; value source ADR-0015 | PUBLIC |
| `name_mr` | varchar(100) | YES | — | — | — | — | Marathi name, curated with recorded provenance; NULL means the English `name` is shown (ADR-0015). | REQUIREMENT FROM SSOT | §20; value source ADR-0015 | PUBLIC |
| `code` | varchar(20) | NO | — | — | — | YES | ISO 3166-2 subdivision code; Maharashtra is `IN-MH` (ADR-0006). | REQUIREMENT FROM SSOT | §20 (UK); scheme ADR-0006 (ISO 3166-2) | PUBLIC |
| `lgd_code` | integer | NO | — | — | — | YES | Local Government Directory (LGD) state code. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0014 (LGD is the authority for names and codes) | PUBLIC |

**Foreign keys**

- None.

**Unique constraints**

- `uq_states_code`: `code` (REQUIREMENT FROM SSOT).
- `uq_states_name`: `name` (ARCHITECTURAL DECISION).
- `uq_states_lgd_code`: `lgd_code` (ARCHITECTURAL DECISION).

CHECK constraints and indexes: [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md#states).

### districts

| Field | Value |
|---|---|
| Purpose | Districts of each state, each with a centroid used as the farmer origin for distance calculation (FR-06). Phase 1: the 36 Maharashtra districts listed by LGD, with centroids computed from Survey of India district boundaries (ADR-0014). |
| Owning module | geography |
| Phase | Phase 1 |
| Status | REQUIRED |
| Table group | Geography reference |
| Source / requirement | §20 DISTRICTS; F1.3; FR-06; §21 step 3; §28 (district centroid only); §47 Q6 |
| Natural key | (`state_id`, `name`) — ARCHITECTURAL DECISION |
| Record class | Reference data (curated) |
| Deletion behavior | Never deleted while referenced (`RESTRICT` from `localities`, `markets`, `farmer_profiles`, `district_source_aliases`). |

| Column | Type | Nullable | Default | PK | FK | Unique | Description | Basis | Source / requirement | Sensitivity |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | uuid | NO | `gen_random_uuid()` | YES | — | — | Surrogate primary key. | REQUIREMENT FROM SSOT | §20; default: ARCHITECTURAL DECISION | PUBLIC |
| `state_id` | uuid | NO | — | — | `states.id` | composite: uq_districts_state_name | Owning state. | REQUIREMENT FROM SSOT | §20 | PUBLIC |
| `name` | varchar(150) | NO | — | — | — | composite: uq_districts_state_name | District name as published by LGD (for example `Ahilyanagar`, `Chhatrapati Sambhajinagar`, `Dharashiv`). Feed spellings such as `Amarawati` or `Osmanabad` resolve through `district_source_aliases`. | REQUIREMENT FROM SSOT | §20; list source LGD (ADR-0014) | PUBLIC |
| `lgd_code` | integer | NO | — | — | — | YES | LGD district code (for example Ahilyanagar 466). | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0014 | PUBLIC |
| `centroid_lat` | numeric(9,6) | NO | — | — | — | — | Centroid latitude, WGS84 decimal degrees, computed from the Survey of India district boundary (ADR-0014). | REQUIREMENT FROM SSOT | §20 (decimal); precision: ARCHITECTURAL DECISION; source ADR-0014 | PUBLIC |
| `centroid_lon` | numeric(9,6) | NO | — | — | — | — | Centroid longitude (as `centroid_lat`). | REQUIREMENT FROM SSOT | §20; precision: ARCHITECTURAL DECISION; source ADR-0014 | PUBLIC |
| `centroid_source` | varchar(300) | NO | — | — | — | — | Provenance of the centroid: dataset, version and method (geometric centroid, or a point on the surface when the centroid falls outside the polygon). | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: coordinate provenance (Task 7 directive); content ADR-0014 | PUBLIC |

**Foreign keys**

- `fk_districts_state`: (`state_id`) → `states` (`id`), ON DELETE RESTRICT.

**Unique constraints**

- `uq_districts_state_name`: `state_id`, `name` (ARCHITECTURAL DECISION).
- `uq_districts_lgd_code`: `lgd_code` (ARCHITECTURAL DECISION).

CHECK constraints and indexes: [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md#districts).

### localities

| Field | Value |
|---|---|
| Purpose | Localities offered in the third wizard step (F1.3): LGD sub-districts (talukas; 358 in Maharashtra). They carry no coordinates in Phase 1; the farmer origin stays the district centroid (FR-06, ADR-0014). |
| Owning module | geography |
| Phase | Phase 1 |
| Status | REQUIRED |
| Table group | Geography reference |
| Source / requirement | §20 LOCALITIES; F1.3; §25; §30 LocalityResponse |
| Natural key | (`district_id`, `name`) — ARCHITECTURAL DECISION |
| Record class | Reference data (curated) |
| Deletion behavior | No dependants in Phase 1; reference data retained indefinitely (ADR-0029). |

| Column | Type | Nullable | Default | PK | FK | Unique | Description | Basis | Source / requirement | Sensitivity |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | uuid | NO | `gen_random_uuid()` | YES | — | — | Surrogate primary key. | REQUIREMENT FROM SSOT | §20; default: ARCHITECTURAL DECISION | PUBLIC |
| `district_id` | uuid | NO | — | — | `districts.id` | composite: uq_localities_district_name | Owning district. | REQUIREMENT FROM SSOT | §20 | PUBLIC |
| `name` | varchar(150) | NO | — | — | — | composite: uq_localities_district_name | Sub-district (taluka) name as published by LGD. | REQUIREMENT FROM SSOT | §20; source and granularity: LGD sub-districts (ADR-0014) | PUBLIC |
| `lgd_code` | integer | NO | — | — | — | YES | LGD sub-district code. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0014 | PUBLIC |
| `lat` | numeric(9,6) | YES | — | — | — | — | Latitude. NULL in Phase 1: locality coordinates are neither seeded nor used (ADR-0014). | REQUIREMENT FROM SSOT | §20 (decimal); nullability: ARCHITECTURAL DECISION; unused in Phase 1 (ADR-0014) | PUBLIC |
| `lon` | numeric(9,6) | YES | — | — | — | — | Longitude. NULL in Phase 1 (ADR-0014). | REQUIREMENT FROM SSOT | §20; nullability: ARCHITECTURAL DECISION; unused in Phase 1 (ADR-0014) | PUBLIC |
| `coordinate_source` | varchar(300) | YES | — | — | — | — | Provenance of `lat`/`lon`; present exactly when coordinates are present. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: coordinate provenance | PUBLIC |

**Foreign keys**

- `fk_localities_district`: (`district_id`) → `districts` (`id`), ON DELETE RESTRICT.

**Unique constraints**

- `uq_localities_district_name`: `district_id`, `name` (ARCHITECTURAL DECISION).
- `uq_localities_lgd_code`: `lgd_code` (ARCHITECTURAL DECISION).

CHECK constraints and indexes: [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md#localities).

### district_source_aliases

| Field | Value |
|---|---|
| Purpose | Maps each source (state, district) spelling to one LGD district, with mandatory evidence, so that renamed districts (`Osmanabad` / `Dharashiv`), feed spellings (`Amarawati`) and non-district entries resolve deterministically (ADR-0014). |
| Owning module | geography |
| Phase | Phase 1 |
| Status | REQUIRED |
| Table group | Geography reference |
| Source / requirement | ADR-0014; Agmarknet 2.0 Maharashtra district master (38 entries, retrieved 2026-10-03); evidence §19.3 (district spellings) |
| Natural key | (`source_state_name`, `source_district_name`) |
| Record class | Reference data (seeded with evidence; added by admins with an evidence note) |
| Deletion behavior | Never deleted while in use; `RESTRICT` to `districts`. Retained indefinitely (ADR-0029). |

| Column | Type | Nullable | Default | PK | FK | Unique | Description | Basis | Source / requirement | Sensitivity |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | uuid | NO | `gen_random_uuid()` | YES | — | — | Surrogate primary key. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0014 | PUBLIC |
| `district_id` | uuid | NO | — | — | `districts.id` | — | LGD district this spelling denotes. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0014 | PUBLIC |
| `source_state_name` | varchar(100) | NO | — | — | — | composite: uq_district_source_aliases_source | Normalized source `State` / `state` value. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0014; evidence §19.3 | PUBLIC |
| `source_district_name` | varchar(150) | NO | — | — | — | composite: uq_district_source_aliases_source | Normalized source `District` / `district` value (for example `Amarawati`, `Osmanabad`). | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0014; evidence §19.3 | PUBLIC |
| `evidence_note` | varchar(500) | NO | — | — | — | — | Official reference for the mapping (for example the government notification of a rename). | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0014 (no alias without evidence) | INTERNAL |
| `created_at` | timestamptz | NO | `now()` | — | — | — | When the mapping was recorded. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: mapping audit | INTERNAL |

**Foreign keys**

- `fk_district_source_aliases_district`: (`district_id`) → `districts` (`id`), ON DELETE RESTRICT.

**Unique constraints**

- `uq_district_source_aliases_source`: `source_state_name`, `source_district_name` (ARCHITECTURAL DECISION).

CHECK constraints and indexes: [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md#district_source_aliases).

### commodities

| Field | Value |
|---|---|
| Purpose | The commodity known list (§14): seeded from the official Agmarknet 2.0 commodity master (605 names, 16 groups), each with its evidence-verified price unit (ADR-0006, ADR-0008). Data-driven: a new commodity is a new row. Distinct source values stay distinct (for example `Onion` and `Onion Green`). |
| Owning module | commodity |
| Phase | Phase 1 |
| Status | REQUIRED |
| Table group | Commodity reference |
| Source / requirement | §20 COMMODITIES; F1.4; §14 (normalize commodity, "commodity in known list"); §27 (additional commodities without code changes); §47 Q4; evidence §0.10, §20.3 |
| Natural key | `canonical_name` (spec UK) |
| Record class | Reference data (seeded from the official master; added only by an admin with an official source reference). Ingestion never creates commodities: unknown values are rejected as `UNKNOWN_COMMODITY` (ADR-0006). |
| Deletion behavior | Never deleted while referenced (`RESTRICT`). |

| Column | Type | Nullable | Default | PK | FK | Unique | Description | Basis | Source / requirement | Sensitivity |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | uuid | NO | `gen_random_uuid()` | YES | — | — | Surrogate primary key. | REQUIREMENT FROM SSOT | §20; default: ARCHITECTURAL DECISION | PUBLIC |
| `canonical_name` | varchar(200) | NO | — | — | — | YES | Whitespace-normalized source commodity value, matched exactly (case-sensitive, no fuzzy matching). | REQUIREMENT FROM SSOT | §20 (UK); naming policy: ARCHITECTURAL DECISION (evidence §20.3 exactness) | PUBLIC |
| `name_hi` | varchar(200) | YES | — | — | — | — | Hindi display name, curated with recorded provenance; NULL falls back to `canonical_name` (ADR-0015). | REQUIREMENT FROM SSOT | §20; value source ADR-0015 | PUBLIC |
| `name_mr` | varchar(200) | YES | — | — | — | — | Marathi display name, curated with recorded provenance; NULL falls back to `canonical_name` (ADR-0015). | REQUIREMENT FROM SSOT | §20; value source ADR-0015 | PUBLIC |
| `category` | varchar(100) | YES | — | — | — | — | Official Agmarknet commodity-group name (16 groups, e.g. `Vegetables`, `Cereals`, `Pulses`) (ADR-0006). | REQUIREMENT FROM SSOT | §20; vocabulary ADR-0006 | PUBLIC |
| `price_unit` | varchar(50) | YES | — | — | — | — | Verified unit of the source prices for this commodity, in the source notation (e.g. `Rs./Quintal`). NULL = unverified: ingestion rejects its records as `UNIT_UNVERIFIED` (ADR-0008). | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0008 (§31 RULE 3) | PUBLIC |
| `price_unit_source` | varchar(300) | YES | — | — | — | — | Official evidence for `price_unit`: Agmarknet report export or dashboard, with its date. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0008 | PUBLIC |
| `created_at` | timestamptz | NO | `now()` | — | — | — | When the commodity was added. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: reference-data audit (ADR-0020) | INTERNAL |

**Foreign keys**

- None.

**Unique constraints**

- `uq_commodities_canonical_name`: `canonical_name` (REQUIREMENT FROM SSOT).

CHECK constraints and indexes: [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md#commodities).

### varieties

| Field | Value |
|---|---|
| Purpose | Varieties per commodity as reported by the source (for example Onion: `Unhali`, `Red`, `White`, `Other`). Never collapsed into the commodity. |
| Owning module | commodity |
| Phase | Phase 1 |
| Status | REQUIRED |
| Table group | Commodity reference |
| Source / requirement | §20 VARIETIES; F1.4; §14 (canonicalize variety); evidence (one market, commodity and date can carry several varieties) |
| Natural key | (`commodity_id`, `canonical_name`) — ARCHITECTURAL DECISION |
| Record class | Reference data (registered automatically by ingestion on first appearance, per commodity, with the exact normalized source value; never merged or renamed, ADR-0006) |
| Deletion behavior | Never deleted while referenced (`RESTRICT`). |

| Column | Type | Nullable | Default | PK | FK | Unique | Description | Basis | Source / requirement | Sensitivity |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | uuid | NO | `gen_random_uuid()` | YES | — | composite: uq_varieties_id_commodity | Surrogate primary key. | REQUIREMENT FROM SSOT | §20; default: ARCHITECTURAL DECISION | PUBLIC |
| `commodity_id` | uuid | NO | — | — | `commodities.id` | composite: uq_varieties_commodity_name, uq_varieties_id_commodity | Commodity this variety belongs to. | REQUIREMENT FROM SSOT | §20 | PUBLIC |
| `canonical_name` | varchar(200) | NO | — | — | — | composite: uq_varieties_commodity_name | Whitespace-normalized source variety value. `Other` and `Local` are real source values, stored as they are. | REQUIREMENT FROM SSOT | §20; naming policy: ARCHITECTURAL DECISION | PUBLIC |
| `created_at` | timestamptz | NO | `now()` | — | — | — | When the variety was first registered. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: registration audit (ADR-0006) | INTERNAL |

**Foreign keys**

- `fk_varieties_commodity`: (`commodity_id`) → `commodities` (`id`), ON DELETE RESTRICT.

**Unique constraints**

- `uq_varieties_commodity_name`: `commodity_id`, `canonical_name` (ARCHITECTURAL DECISION).
- `uq_varieties_id_commodity`: `id`, `commodity_id` (ARCHITECTURAL DECISION).

CHECK constraints and indexes: [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md#varieties).

### grades

| Field | Value |
|---|---|
| Purpose | Grades per commodity as reported by the source (Maharashtra evidence: `FAQ`, `Local`, `Non-FAQ`). |
| Owning module | commodity |
| Phase | Phase 1 |
| Status | REQUIRED |
| Table group | Commodity reference |
| Source / requirement | §20 GRADES (per commodity); F1.4; §14 (canonicalize grade); evidence |
| Natural key | (`commodity_id`, `canonical_name`) — ARCHITECTURAL DECISION |
| Record class | Reference data (registered automatically by ingestion on first appearance, per commodity, with the exact normalized source value; never merged or renamed, ADR-0006) |
| Deletion behavior | Never deleted while referenced (`RESTRICT`). |

| Column | Type | Nullable | Default | PK | FK | Unique | Description | Basis | Source / requirement | Sensitivity |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | uuid | NO | `gen_random_uuid()` | YES | — | composite: uq_grades_id_commodity | Surrogate primary key. | REQUIREMENT FROM SSOT | §20; default: ARCHITECTURAL DECISION | PUBLIC |
| `commodity_id` | uuid | NO | — | — | `commodities.id` | composite: uq_grades_commodity_name, uq_grades_id_commodity | Commodity this grade belongs to (§20 models grades per commodity). | REQUIREMENT FROM SSOT | §20 | PUBLIC |
| `canonical_name` | varchar(200) | NO | — | — | — | composite: uq_grades_commodity_name | Whitespace-normalized source grade value. | REQUIREMENT FROM SSOT | §20; naming policy: ARCHITECTURAL DECISION | PUBLIC |
| `created_at` | timestamptz | NO | `now()` | — | — | — | When the grade was first registered. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: registration audit (ADR-0006) | INTERNAL |

**Foreign keys**

- `fk_grades_commodity`: (`commodity_id`) → `commodities` (`id`), ON DELETE RESTRICT.

**Unique constraints**

- `uq_grades_commodity_name`: `commodity_id`, `canonical_name` (ARCHITECTURAL DECISION).
- `uq_grades_id_commodity`: `id`, `commodity_id` (ARCHITECTURAL DECISION).

CHECK constraints and indexes: [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md#grades).

### commodity_source_aliases

| Field | Value |
|---|---|
| Purpose | Maps a legacy source spelling of a commodity that is not a master name to the one master commodity it denotes, on DMI evidence, so that ingestion stores those records instead of rejecting them as `UNKNOWN_COMMODITY` (ADR-0051, RD-050). Never used for case folding, fuzzy matching or merging distinct commodities. |
| Owning module | commodity |
| Phase | Phase 1 |
| Status | REQUIRED |
| Table group | Commodity reference |
| Source / requirement | ADR-0051; the DMI `Commodity_Code` that each legacy spelling carries in the data.gov.in historical resource (Task 12 harvest, `docs/decision-review/TASK_12_OWNER_DECISION_PACKET.md` D1) |
| Natural key | `source_commodity_name` |
| Record class | Reference data (seeded by migration V12 with an evidence note per row) |
| Deletion behavior | Never deleted while in use; `RESTRICT` to `commodities`. Retained indefinitely (ADR-0029). |

| Column | Type | Nullable | Default | PK | FK | Unique | Description | Basis | Source / requirement | Sensitivity |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | uuid | NO | `gen_random_uuid()` | YES | — | — | Surrogate primary key. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0051 | PUBLIC |
| `commodity_id` | uuid | NO | — | — | `commodities.id` | — | Master commodity this spelling denotes. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0051 | PUBLIC |
| `source_commodity_name` | varchar(200) | NO | — | — | — | YES | Whitespace-normalized source `Commodity` value (for example `Arhar(Tur/Red Gram)(Whole)`). Never equal to a master name: the exact master match always wins. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0051 | PUBLIC |
| `evidence_note` | varchar(500) | NO | — | — | — | — | The evidence for the mapping: the shared DMI `Commodity_Code` and where it was observed. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0051 (no alias without evidence) | INTERNAL |
| `created_at` | timestamptz | NO | `now()` | — | — | — | When the mapping was recorded. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: mapping audit | INTERNAL |

**Foreign keys**

- `fk_commodity_source_aliases_commodity`: (`commodity_id`) → `commodities` (`id`), ON DELETE RESTRICT.

**Unique constraints**

- `uq_commodity_source_aliases_source`: `source_commodity_name` (ARCHITECTURAL DECISION).

CHECK constraints and indexes: [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md#commodity_source_aliases).

### source_datasets

| Field | Value |
|---|---|
| Purpose | Registry of the two government resources. Every observation and every ingestion run points to one row, so the source of a price is always traceable. |
| Owning module | price |
| Phase | Phase 1 |
| Status | REQUIRED |
| Table group | Government source and observations |
| Source / requirement | §20 SOURCE_DATASETS; §1 (resource IDs and roles); §22 ("Load source_datasets"); §31 RULE 7; §45 (`datasetType`) |
| Natural key | `resource_id` (spec UK) |
| Record class | Reference data (two seeded rows in Phase 1) |
| Deletion behavior | Never deleted while referenced (`RESTRICT`). |

| Column | Type | Nullable | Default | PK | FK | Unique | Description | Basis | Source / requirement | Sensitivity |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | uuid | NO | `gen_random_uuid()` | YES | — | — | Surrogate primary key. | REQUIREMENT FROM SSOT | §20; default: ARCHITECTURAL DECISION | PUBLIC |
| `resource_id` | varchar(64) | NO | — | — | — | YES | data.gov.in resource ID, e.g. `9ef84268-d588-465a-a308-a864a43d0070` (current) and `35985678-0d79-46b4-9ed6-6f13308a1d24` (historical). | REQUIREMENT FROM SSOT | §20 (UK); values §1 and evidence §19 | PUBLIC |
| `title` | varchar(300) | NO | — | — | — | — | Official resource title as returned in the response envelope. | REQUIREMENT FROM SSOT | §20; values evidence §19.1–19.2 | PUBLIC |
| `dataset_type` | varchar(30) | NO | — | — | — | YES | Dataset role: `CURRENT_PRICE` (§45) or `HISTORICAL_PRICE` (ADR-0006). One resource per role. | REQUIREMENT FROM SSOT | §20; vocabulary §45 and ADR-0006 | PUBLIC |

**Foreign keys**

- None.

**Unique constraints**

- `uq_source_datasets_resource_id`: `resource_id` (REQUIREMENT FROM SSOT).
- `uq_source_datasets_dataset_type`: `dataset_type` (ARCHITECTURAL DECISION).

CHECK constraints and indexes: [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md#source_datasets).

### markets

| Field | Value |
|---|---|
| Purpose | Canonical Maharashtra markets (any number), with coordinates for nearby-market discovery and their provenance. Source spelling variants live in `market_source_aliases`, never as extra market rows merged silently. A spelling seen for the first time registers a PROVISIONAL market that reaches farmers only after admin verification (ADR-0014). |
| Owning module | price |
| Phase | Phase 1 |
| Status | REQUIRED |
| Table group | Government source and observations |
| Source / requirement | §20 MARKETS; §16 (haversine on market lat/lon, `is_active`, "is_approximate = market.lat derived from district centroid"); §30 (`/markets/*` → price module); §47 Q4 |
| Natural key | `canonical_slug` (spec UK; global, with deterministic disambiguation, ADR-0014) |
| Record class | Reference data: registered PROVISIONAL by ingestion, then curated by admins (VERIFIED or MERGED); deactivated with `is_active`, never deleted (ADR-0014) |
| Deletion behavior | Never deleted while referenced (`RESTRICT`). Withdrawn from comparison with `is_active = false` (§16). A merged market keeps its row (status MERGED, `merged_into_market_id` set, inactive). |

| Column | Type | Nullable | Default | PK | FK | Unique | Description | Basis | Source / requirement | Sensitivity |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | uuid | NO | `gen_random_uuid()` | YES | — | — | Surrogate primary key. | REQUIREMENT FROM SSOT | §20; default: ARCHITECTURAL DECISION | PUBLIC |
| `canonical_name` | varchar(200) | NO | — | — | — | — | Canonical market name, whitespace-normalized. For an automatically registered market, the normalized first source spelling. No approved operation renames a market: curation verifies, merges, deactivates, reactivates and sets coordinates (ADR-0014, ADR-0020) | REQUIREMENT FROM SSOT | §20 | PUBLIC |
| `canonical_slug` | varchar(220) | NO | — | — | — | YES | Lowercase canonical name with each run of non-alphanumeric characters replaced by one hyphen; on collision `-<district slug>`, then `-<state code>` is appended (ADR-0014). | REQUIREMENT FROM SSOT | §20 (UK, global); derivation §47 Q4 refined by ADR-0014 | PUBLIC |
| `district_id` | uuid | NO | — | — | `districts.id` | — | District hosting the market. | REQUIREMENT FROM SSOT | §20 | PUBLIC |
| `lat` | numeric(9,6) | YES | — | — | — | — | Market latitude. Set to the district centroid at registration (approximate); exact values only from an identified source (ADR-0014). A market without coordinates cannot be a distance candidate. | REQUIREMENT FROM SSOT | §20 (decimal); nullability and precision: ARCHITECTURAL DECISION; source ADR-0014 | PUBLIC |
| `lon` | numeric(9,6) | YES | — | — | — | — | Market longitude (as `lat`). | REQUIREMENT FROM SSOT | §20; nullability and precision: ARCHITECTURAL DECISION; source ADR-0014 | PUBLIC |
| `is_coordinate_approximate` | boolean | NO | `false` | — | — | — | TRUE when `lat`/`lon` were derived from the district centroid, so distances must be presented as approximate. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION derived from §16 ("is_approximate = market.lat derived from district centroid"); applies to district-centroid coordinates (ADR-0014) | PUBLIC |
| `coordinate_source` | varchar(300) | YES | — | — | — | — | Provenance of `lat`/`lon`; present exactly when coordinates are present. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: coordinate provenance; content ADR-0014 | PUBLIC |
| `is_active` | boolean | NO | `true` | — | — | — | Only active markets are comparison candidates. | REQUIREMENT FROM SSOT | §20, §16; default value: ARCHITECTURAL DECISION | PUBLIC |
| `review_status` | varchar(12) | NO | `'PROVISIONAL'` | — | — | — | `PROVISIONAL` (registered from the feed, not shown to farmers), `VERIFIED` (comparison and alert candidate) or `MERGED` (alias of another market). | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0014 | PUBLIC |
| `merged_into_market_id` | uuid | YES | — | — | `markets.id` | — | Target market of a merge; set exactly when `review_status` is `MERGED`. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0014 (evidence-backed merges only) | PUBLIC |
| `created_at` | timestamptz | NO | `now()` | — | — | — | When the market was registered. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: registration audit (ADR-0014) | INTERNAL |

**Foreign keys**

- `fk_markets_district`: (`district_id`) → `districts` (`id`), ON DELETE RESTRICT.
- `fk_markets_merged_into`: (`merged_into_market_id`) → `markets` (`id`), ON DELETE RESTRICT.

**Unique constraints**

- `uq_markets_canonical_slug`: `canonical_slug` (REQUIREMENT FROM SSOT).

CHECK constraints and indexes: [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md#markets).

### market_source_aliases

| Field | Value |
|---|---|
| Purpose | The "canonical mapping table" of §47 Q4: maps each source (state, district, market) spelling to one canonical market. Keeps the raw source spelling for audit. Mappings come from the seed (with evidence), from automatic PROVISIONAL registration (one alias per new spelling) or from an evidence-backed admin merge; never from fuzzy matching (ADR-0014). |
| Owning module | price |
| Phase | Phase 1 |
| Status | REQUIRED |
| Table group | Government source and observations |
| Source / requirement | §47 Q4 ("we maintain a canonical mapping table"); FR-03; §14–§15 (normalize market name); evidence §19.7 (trailing whitespace), Appendix A (alias twins such as `APMC Manmad` / `Manmad APMC`; `Sirsa APMC` in two states) |
| Natural key | (`source_state_name`, `source_district_name`, `source_market_name`) |
| Record class | Reference data (seeded, registered automatically or curated) |
| Deletion behavior | Deleting a mapping does not touch observations; `RESTRICT` to `markets`. Re-mapping procedure: DATABASE_ARCHITECTURE.md section 9.4. |

| Column | Type | Nullable | Default | PK | FK | Unique | Description | Basis | Source / requirement | Sensitivity |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | uuid | NO | `gen_random_uuid()` | YES | — | — | Surrogate primary key. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION (table: §47 Q4) | PUBLIC |
| `market_id` | uuid | NO | — | — | `markets.id` | — | Canonical market this spelling denotes. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION (§47 Q4 mapping) | PUBLIC |
| `source_state_name` | varchar(100) | NO | — | — | — | composite: uq_market_source_aliases_source | Normalized source `State` / `state` value. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION; evidence §19.3 | PUBLIC |
| `source_district_name` | varchar(150) | NO | — | — | — | composite: uq_market_source_aliases_source | Normalized source `District` / `district` value (source spelling, e.g. `Amarawati`). | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION; evidence §19.3 | PUBLIC |
| `source_market_name` | varchar(200) | NO | — | — | — | composite: uq_market_source_aliases_source | Normalized source `Market` / `market` value: whitespace runs collapsed to one space and trimmed; case and punctuation unchanged. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION; evidence §19.7 | PUBLIC |
| `source_market_name_raw` | varchar(200) | NO | — | — | — | — | The market string exactly as first received, including leading, trailing or doubled spaces. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: preserve source values for audit (Task 7 canonicalization rule) | PUBLIC |
| `evidence_note` | varchar(500) | NO | — | — | — | — | Why the mapping exists: seed evidence reference, `auto-registered by run <id>`, or the admin merge justification. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0014 (no alias without evidence) | INTERNAL |
| `created_at` | timestamptz | NO | `now()` | — | — | — | When the mapping was recorded. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: mapping audit | INTERNAL |

**Foreign keys**

- `fk_market_source_aliases_market`: (`market_id`) → `markets` (`id`), ON DELETE RESTRICT.

**Unique constraints**

- `uq_market_source_aliases_source`: `source_state_name`, `source_district_name`, `source_market_name` (ARCHITECTURAL DECISION).

CHECK constraints and indexes: [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md#market_source_aliases).

### price_observations

| Field | Value |
|---|---|
| Purpose | One row per distinct source observation: dataset + market + commodity + variety + grade + report date, with min, max and modal prices exactly as reported (2 decimals) and the unit recorded on the row. |
| Owning module | price |
| Phase | Phase 1 |
| Status | REQUIRED |
| Table group | Government source and observations |
| Source / requirement | §20 PRICE_OBSERVATIONS; §14–§15; §22; §27 (business key unique at DB level); §31 RULES 3, 5, 6, 7; FR-04, FR-05, FR-13; §47 Q8; evidence §19–§20 |
| Natural key | `business_key` (spec UK) and the null-safe composite identity (ARCHITECTURAL DECISION) |
| Record class | Historical fact; prices change only through a recorded source revision (ADR-0009) |
| Deletion behavior | Never deleted by the application; retained indefinitely (ADR-0029). `RESTRICT` from `selling_options`, `price_observation_revisions` and `alert_events`. |

| Column | Type | Nullable | Default | PK | FK | Unique | Description | Basis | Source / requirement | Sensitivity |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | uuid | NO | `gen_random_uuid()` | YES | — | — | Surrogate primary key. | REQUIREMENT FROM SSOT | §20; default: ARCHITECTURAL DECISION | PUBLIC |
| `market_id` | uuid | NO | — | — | `markets.id` | composite: uq_price_observations_identity | Canonical market resolved through `market_source_aliases`. | REQUIREMENT FROM SSOT | §20 | PUBLIC |
| `commodity_id` | uuid | NO | — | — | `commodities.id` | composite: uq_price_observations_identity | Commodity resolved by exact match of the master name (`Onion` ≠ `Onion Green`), or of a `commodity_source_aliases` spelling (ADR-0051). | REQUIREMENT FROM SSOT | §20; evidence §20.3; aliases ADR-0051 | PUBLIC |
| `variety_id` | uuid | YES | — | — | `varieties.id (composite with commodity_id)` | composite: uq_price_observations_identity | Source variety. NULL only when the source omits it (never observed; §14 does not make variety a required field). | REQUIREMENT FROM SSOT | §20; nullability: ARCHITECTURAL DECISION (§14 required fields) | PUBLIC |
| `grade_id` | uuid | YES | — | — | `grades.id (composite with commodity_id)` | composite: uq_price_observations_identity | Source grade. NULL only when the source omits it. | REQUIREMENT FROM SSOT | §20; nullability: ARCHITECTURAL DECISION (§14 required fields) | PUBLIC |
| `report_date` | date | NO | — | — | — | composite: uq_price_observations_identity | Date the source observation refers to: `Arrival_Date` (historical) / `arrival_date` (current), parsed from the observed `dd/MM/yyyy` text. Never overwritten. | REQUIREMENT FROM SSOT | §20; parsing ADR-0007 (strict `dd/MM/yyyy`); evidence §19.7 | PUBLIC |
| `modal_price` | numeric(14,2) | NO | — | — | — | — | Modal price as reported: parsed as an exact decimal, rounded half-up to 2 dp, never converted (ADR-0008). | REQUIREMENT FROM SSOT | §20 (decimal); precision: ARCHITECTURAL DECISION; unit and rounding ADR-0008 | PUBLIC |
| `min_price` | numeric(14,2) | YES | — | — | — | — | Minimum price as reported; NULL when the source omits it (a zero is rejected, never read as missing, ADR-0008). | REQUIREMENT FROM SSOT | §20; nullability: ARCHITECTURAL DECISION (§14 requires only "price"); representation ADR-0008 | PUBLIC |
| `max_price` | numeric(14,2) | YES | — | — | — | — | Maximum price as reported; NULL when the source omits it (ADR-0008). | REQUIREMENT FROM SSOT | §20; nullability: ARCHITECTURAL DECISION; representation ADR-0008 | PUBLIC |
| `price_unit` | varchar(50) | NO | — | — | — | — | Unit of the three prices, copied at load time from the commodity's verified unit (`commodities.price_unit`), in the source notation, e.g. `Rs./Quintal`. Never converted (ADR-0008). | REQUIREMENT FROM SSOT | §20; §31 RULE 3; FR-05; value ADR-0008 | PUBLIC |
| `source_commodity_name` | varchar(200) | YES | — | — | — | — | The source's commodity spelling when the record was resolved through `commodity_source_aliases`; NULL when the source used the master name. Provenance only: not part of the identity or the business key. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0051 (preserve the original value) | PUBLIC |
| `business_key` | varchar(64) | NO | — | — | — | YES | Lowercase hex SHA-256 of the observation identity; the `ON CONFLICT` arbiter. | REQUIREMENT FROM SSOT | §20 (UK); §14; §47 Q8; encoding and inputs: ARCHITECTURAL DECISION | PUBLIC |
| `source_dataset_id` | uuid | NO | — | — | `source_datasets.id` | composite: uq_price_observations_identity | Dataset that supplied the row. Current and historical rows for the same market, commodity and date coexist. | REQUIREMENT FROM SSOT | §20; §14 business key | PUBLIC |
| `ingestion_run_id` | uuid | NO | — | — | `ingestion_runs.id` | — | Run that inserted the row; never changes. Revisions record their own run (ADR-0009). | REQUIREMENT FROM SSOT | §20; semantics ADR-0009 | INTERNAL |
| `ingested_at` | timestamptz | NO | `now()` | — | — | — | When the current price values were written: at insert or at the latest revision (ADR-0009). Not when the market reported them. | REQUIREMENT FROM SSOT | §20; semantics ADR-0009 | INTERNAL |

**Foreign keys**

- `fk_price_observations_market`: (`market_id`) → `markets` (`id`), ON DELETE RESTRICT.
- `fk_price_observations_commodity`: (`commodity_id`) → `commodities` (`id`), ON DELETE RESTRICT.
- `fk_price_observations_variety`: (`variety_id`, `commodity_id`) → `varieties` (`id`, `commodity_id`), ON DELETE RESTRICT.
- `fk_price_observations_grade`: (`grade_id`, `commodity_id`) → `grades` (`id`, `commodity_id`), ON DELETE RESTRICT.
- `fk_price_observations_source_dataset`: (`source_dataset_id`) → `source_datasets` (`id`), ON DELETE RESTRICT.
- `fk_price_observations_ingestion_run`: (`ingestion_run_id`) → `ingestion_runs` (`id`), ON DELETE RESTRICT.

**Unique constraints**

- `uq_price_observations_business_key`: `business_key` (REQUIREMENT FROM SSOT).
- `uq_price_observations_identity`: `market_id`, `commodity_id`, `report_date`, `variety_id`, `grade_id`, `source_dataset_id`, NULLS NOT DISTINCT (ARCHITECTURAL DECISION).

CHECK constraints and indexes: [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md#price_observations).

### price_observation_revisions

| Field | Value |
|---|---|
| Purpose | Audit trail of source revisions: one row each time a later run delivers different prices for a stored observation, holding the values that were replaced (ADR-0009). Nothing is overwritten without a trace. |
| Owning module | price |
| Phase | Phase 1 |
| Status | REQUIRED |
| Table group | Government source and observations |
| Source / requirement | ADR-0009; §14 ("DO UPDATE if newer ingestion_run"); §49 (prices may be "revised"); owner instruction "NEVER silently overwrite conflicting source observations" |
| Natural key | (`price_observation_id`, `ingestion_run_id`) |
| Record class | Audit data (append-only) |
| Deletion behavior | Never deleted by the application; retained indefinitely (ADR-0029). `RESTRICT` to `price_observations` and `ingestion_runs`. |

| Column | Type | Nullable | Default | PK | FK | Unique | Description | Basis | Source / requirement | Sensitivity |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | uuid | NO | `gen_random_uuid()` | YES | — | — | Surrogate primary key. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0009 | INTERNAL |
| `price_observation_id` | uuid | NO | — | — | `price_observations.id` | composite: uq_price_observation_revisions_observation_run | Observation whose prices were revised. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0009 | INTERNAL |
| `ingestion_run_id` | uuid | NO | — | — | `ingestion_runs.id` | composite: uq_price_observation_revisions_observation_run | Run that delivered the new values (counted in its `records_updated`). | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0009, ADR-0012 | INTERNAL |
| `previous_modal_price` | numeric(14,2) | NO | — | — | — | — | Modal price before the revision. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0009 | PUBLIC |
| `previous_min_price` | numeric(14,2) | YES | — | — | — | — | Minimum price before the revision. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0009 | PUBLIC |
| `previous_max_price` | numeric(14,2) | YES | — | — | — | — | Maximum price before the revision. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0009 | PUBLIC |
| `previous_ingested_at` | timestamptz | NO | — | — | — | — | When the replaced values had been written. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0009 | INTERNAL |
| `revised_at` | timestamptz | NO | `now()` | — | — | — | When the revision was applied (equals the observation's new `ingested_at`). | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0009 | INTERNAL |

**Foreign keys**

- `fk_price_observation_revisions_observation`: (`price_observation_id`) → `price_observations` (`id`), ON DELETE RESTRICT.
- `fk_price_observation_revisions_run`: (`ingestion_run_id`) → `ingestion_runs` (`id`), ON DELETE RESTRICT.

**Unique constraints**

- `uq_price_observation_revisions_observation_run`: `price_observation_id`, `ingestion_run_id` (ARCHITECTURAL DECISION).

CHECK constraints and indexes: [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md#price_observation_revisions).

### ingestion_runs

| Field | Value |
|---|---|
| Purpose | One audit row per ingestion run: dataset, trigger, request scope, status, timing, page counts and record counters (FR-17). |
| Owning module | ingestion |
| Phase | Phase 1 |
| Status | REQUIRED |
| Table group | Ingestion audit |
| Source / requirement | §20 INGESTION_RUNS; §14 AU1; §15 (`RUNNING`, `triggeredBy`, FINALIZE counts); §22; §29 ("X records inserted, Y pages failed"); FR-17, FR-18; §36 Experiment 2; §48 (admin health exact match); evidence §0.5, §19.6 |
| Natural key | none (event row) |
| Record class | Audit data (append-only; counters updated while RUNNING) |
| Deletion behavior | Never deleted by the application; retained indefinitely (ADR-0029). `RESTRICT` from `price_observations`, `price_observation_revisions` and `alert_events`. |

| Column | Type | Nullable | Default | PK | FK | Unique | Description | Basis | Source / requirement | Sensitivity |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | uuid | NO | `gen_random_uuid()` | YES | — | — | Surrogate primary key. | REQUIREMENT FROM SSOT | §20; default: ARCHITECTURAL DECISION | INTERNAL |
| `source_dataset_id` | uuid | NO | — | — | `source_datasets.id` | partial: uq_ingestion_runs_one_running | Dataset the run ingested. | REQUIREMENT FROM SSOT | §20 | INTERNAL |
| `triggered_by` | varchar(20) | NO | — | — | — | — | `SCHEDULER`, `MANUAL` or `STARTUP`. | REQUIREMENT FROM SSOT | §15 (`triggeredBy`; not in §20) | INTERNAL |
| `status` | varchar(20) | NO | — | — | — | — | `RUNNING`, `SUCCESS`, `PARTIAL_FAILURE` or `FAILED`. | REQUIREMENT FROM SSOT | §20; values §15, §22, §29; status rule ADR-0011 | INTERNAL |
| `started_at` | timestamptz | NO | `now()` | — | — | — | Run start. | REQUIREMENT FROM SSOT | §20 | INTERNAL |
| `finished_at` | timestamptz | YES | — | — | — | — | Run end; NULL while RUNNING. Duration (§15 `duration_seconds`) is derived as `finished_at - started_at`, not stored. | REQUIREMENT FROM SSOT | §20 | INTERNAL |
| `request_scope` | jsonb | NO | — | — | — | — | Request parameters of the run (filters such as state, commodity, arrival date; page size). Never contains credentials. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: run scope (evidence §0.11, Appendix B; Task 7 ingestion model) | INTERNAL |
| `source_total_records` | integer | YES | — | — | — | — | The `total` the source reported for the scope (e.g. 103 while page 0 returned 100). | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: pagination audit (evidence §0.5, §19.6, §21) | INTERNAL |
| `pages_fetched` | integer | NO | `0` | — | — | — | Pages retrieved successfully. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: pagination audit (§31 RULE 2) | INTERNAL |
| `pages_failed` | integer | NO | `0` | — | — | — | Pages that failed after retries (the "Y pages failed" figure). | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION supporting §29 | INTERNAL |
| `records_fetched` | integer | NO | `0` | — | — | — | Records received from the source. | REQUIREMENT FROM SSOT | §20 | INTERNAL |
| `records_inserted` | integer | NO | `0` | — | — | — | Records that created a new observation. | REQUIREMENT FROM SSOT | §20 | INTERNAL |
| `records_updated` | integer | NO | `0` | — | — | — | Records that changed the prices of an existing observation; each leaves one `price_observation_revisions` row (ADR-0009). | REQUIREMENT FROM SSOT | §20; semantics ADR-0009 | INTERNAL |
| `duplicates_skipped` | integer | NO | `0` | — | — | — | Records whose values were already stored or duplicated within the delivery (identical re-deliveries, alias twins); nothing is written (ADR-0009). | REQUIREMENT FROM SSOT | §20; semantics ADR-0009 | INTERNAL |
| `validation_errors_count` | integer | NO | `0` | — | — | — | Records rejected by validation (one `ingestion_errors` row each). | REQUIREMENT FROM SSOT | §20; §15 | INTERNAL |
| `records_out_of_scope` | integer | NO | `0` | — | — | — | Records returned by the source but outside the run scope after exact matching (for example another state, or `Onion Green` in an `Onion` scope). Not errors. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: counter reconciliation (evidence §0.11, §20.3); counting rule ADR-0012 | INTERNAL |
| `records_conflicted` | integer | NO | `0` | — | — | — | Records quarantined because another record in the same delivery had the same identity but different prices; one CONFLICT error row each (ADR-0009). | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0009, ADR-0012 | INTERNAL |

**Foreign keys**

- `fk_ingestion_runs_source_dataset`: (`source_dataset_id`) → `source_datasets` (`id`), ON DELETE RESTRICT.

**Unique constraints**

- `uq_ingestion_runs_one_running`: `source_dataset_id`, partial WHERE `status = 'RUNNING'` (ARCHITECTURAL DECISION).

CHECK constraints and indexes: [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md#ingestion_runs).

### ingestion_errors

| Field | Value |
|---|---|
| Purpose | One row per rejected record or API failure, with the raw payload so that nothing is silently lost and records can be replayed. |
| Owning module | ingestion |
| Phase | Phase 1 |
| Status | REQUIRED |
| Table group | Ingestion audit |
| Source / requirement | §20 INGESTION_ERRORS; §14 AU1; §15 (VALIDATION, raw_payload); §22 (API_ERROR); §29 (failed pages); §37 (schema change: raw_payload, fail loudly); FR-18 |
| Natural key | none (event row) |
| Record class | Audit data (append-only) |
| Deletion behavior | `ON DELETE CASCADE` from `ingestion_runs` (errors are part of their run). Rows older than 180 days are purged daily (ADR-0029). |

| Column | Type | Nullable | Default | PK | FK | Unique | Description | Basis | Source / requirement | Sensitivity |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | uuid | NO | `gen_random_uuid()` | YES | — | — | Surrogate primary key. | REQUIREMENT FROM SSOT | §20; default: ARCHITECTURAL DECISION | INTERNAL |
| `ingestion_run_id` | uuid | NO | — | — | `ingestion_runs.id` | — | Run the error belongs to. | REQUIREMENT FROM SSOT | §20 | INTERNAL |
| `error_type` | varchar(30) | NO | — | — | — | — | `VALIDATION`, `API_ERROR`, `CONFLICT`, `SCHEMA_ERROR` or `SYSTEM` (ADR-0009, ADR-0011). | REQUIREMENT FROM SSOT | §20; values §15, §22, extended by ADR-0009 and ADR-0011 | INTERNAL |
| `error_code` | varchar(50) | NO | — | — | — | — | Machine-readable UPPER_SNAKE code, e.g. `UNKNOWN_COMMODITY`, `UNIT_UNVERIFIED`, `PRICE_OUTLIER`, `SAME_KEY_DIFFERENT_PRICES`, `HTTP_STATUS`, `NON_JSON_RESPONSE`, `RUN_INTERRUPTED` (ADR-0008, ADR-0011). | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0008, ADR-0011 | INTERNAL |
| `error_message` | text | NO | — | — | — | — | Field and reason (validation) or HTTP outcome (API error). Never contains the API key. | REQUIREMENT FROM SSOT | §20; §15 ("field+reason"); §47 Q21 | INTERNAL |
| `raw_payload` | jsonb | YES | — | — | — | — | The rejected source record, or the error response body. Never the request URL or credentials. | REQUIREMENT FROM SSOT | §20; §15; §37 | PUBLIC |
| `page_offset` | integer | YES | — | — | — | — | Offset of the page concerned, for API errors and page-level failures. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: pagination audit (no persisted resume state, ADR-0011) | INTERNAL |
| `request_report_date` | date | YES | — | — | — | — | Report date requested when a HISTORICAL delivery unit failed; NULL otherwise (ADR-0011). | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0011 (backfill of failed dates) | INTERNAL |
| `created_at` | timestamptz | NO | `now()` | — | — | — | When the error was recorded. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: error timeline for the admin view (FR-18) | INTERNAL |

**Foreign keys**

- `fk_ingestion_errors_run`: (`ingestion_run_id`) → `ingestion_runs` (`id`), ON DELETE CASCADE.

**Unique constraints**

- None beyond the primary key.

CHECK constraints and indexes: [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md#ingestion_errors).

### ingestion_report_dates

| Field | Value |
|---|---|
| Purpose | The report-date ledger of historical ingestion (ADR-0054 item 1): per dataset and report date, the last attempt, the last successful fetch and the consecutive failed attempts. The status of each date (SETTLED, PENDING, UNSETTLED, FAILED, MISSING) is computed from it and never stored. |
| Owning module | ingestion |
| Phase | Phase 1 |
| Status | REQUIRED |
| Table group | Ingestion audit |
| Source / requirement | §10 (bootstrap, 6-hour cadence), §15, §29, §37; ADR-0011, ADR-0054 |
| Natural key | (`source_dataset_id`, `report_date`) |
| Record class | Operational state (one row per date, updated after each attempt) |
| Deletion behavior | Never purged (one row per report date; ADR-0029 keeps the ingestion audit indefinitely). |

| Column | Type | Nullable | Default | PK | FK | Unique | Description | Basis | Source / requirement | Sensitivity |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | uuid | NO | `gen_random_uuid()` | YES | — | — | Surrogate primary key. | ARCHITECTURAL DECISION | ADR-0054 | INTERNAL |
| `source_dataset_id` | uuid | NO | — | — | `source_datasets.id` | with `report_date` | Dataset of the date; only HISTORICAL_PRICE writes rows. | ARCHITECTURAL DECISION | ADR-0054 item 1 | INTERNAL |
| `report_date` | date | NO | — | — | — | with `source_dataset_id` | The report date (Asia/Kolkata calendar, ADR-0007). | ARCHITECTURAL DECISION | ADR-0054 item 1 | PUBLIC |
| `last_attempt_run_id` | uuid | NO | — | — | `ingestion_runs.id` | — | Run of the last attempt. | ARCHITECTURAL DECISION | ADR-0054 item 1 | INTERNAL |
| `last_attempt_at` | timestamptz | NO | — | — | — | — | When the last attempt's delivery unit ended (the run start for rows seeded by V13). | ARCHITECTURAL DECISION | ADR-0054 item 1 | INTERNAL |
| `last_outcome` | varchar(20) | NO | — | — | — | — | `FETCHED` (every page fetched and the unit loaded) or `FAILED`. | ARCHITECTURAL DECISION | ADR-0054 item 1 | INTERNAL |
| `last_success_run_id` | uuid | YES | — | — | `ingestion_runs.id` | — | Run of the last successful fetch; NULL when never fetched. | ARCHITECTURAL DECISION | ADR-0054 item 1 | INTERNAL |
| `last_success_at` | timestamptz | YES | — | — | — | — | When the last successful fetch ended; decides SETTLED against the settle age. | ARCHITECTURAL DECISION | ADR-0054 item 2 | INTERNAL |
| `success_records_fetched` | integer | YES | — | — | — | — | Records the last successful fetch received; NULL for seeded rows. | ARCHITECTURAL DECISION | ADR-0054 item 1 | INTERNAL |
| `consecutive_failures` | integer | NO | `0` | — | — | — | Failed attempts since the last successful fetch; greater than 0 exactly when the last outcome is `FAILED`. | ARCHITECTURAL DECISION | ADR-0054 item 3 | INTERNAL |

**Foreign keys**

- `fk_ingestion_report_dates_source_dataset`: (`source_dataset_id`) → `source_datasets` (`id`), ON DELETE RESTRICT.
- `fk_ingestion_report_dates_attempt_run`: (`last_attempt_run_id`) → `ingestion_runs` (`id`), ON DELETE RESTRICT.
- `fk_ingestion_report_dates_success_run`: (`last_success_run_id`) → `ingestion_runs` (`id`), ON DELETE RESTRICT.

**Unique constraints**

- `uq_ingestion_report_dates_dataset_date`: (`source_dataset_id`, `report_date`).

CHECK constraints and indexes: [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md#ingestion_report_dates).

### ingestion_source_settings

| Field | Value |
|---|---|
| Purpose | Runtime pause switch per source dataset, so an admin can stop scheduled runs during known source maintenance; manual triggers still work (ADR-0020). Holds no secrets and no schedule. |
| Owning module | ingestion |
| Phase | Phase 1 |
| Status | REQUIRED |
| Table group | Ingestion audit |
| Source / requirement | ADR-0020; ADR-0011 (scheduler skips paused datasets) |
| Natural key | `source_dataset_id` |
| Record class | Mutable configuration (one row per dataset; every change audited in `admin_audit_events`) |
| Deletion behavior | Never deleted; `RESTRICT` to `source_datasets`. |

| Column | Type | Nullable | Default | PK | FK | Unique | Description | Basis | Source / requirement | Sensitivity |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | uuid | NO | `gen_random_uuid()` | YES | — | — | Surrogate primary key. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0020 | INTERNAL |
| `source_dataset_id` | uuid | NO | — | — | `source_datasets.id` | YES | Dataset the setting applies to. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0020 | INTERNAL |
| `is_paused` | boolean | NO | `false` | — | — | — | TRUE stops scheduled runs of this dataset. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0020 | INTERNAL |
| `paused_reason` | varchar(500) | YES | — | — | — | — | Why the dataset is paused; present exactly while paused. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0020 | INTERNAL |
| `updated_at` | timestamptz | NO | `now()` | — | — | — | Last change. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0020 | INTERNAL |

**Foreign keys**

- `fk_ingestion_source_settings_dataset`: (`source_dataset_id`) → `source_datasets` (`id`), ON DELETE RESTRICT.

**Unique constraints**

- `uq_ingestion_source_settings_dataset`: `source_dataset_id` (ARCHITECTURAL DECISION).

CHECK constraints and indexes: [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md#ingestion_source_settings).

### users

| Field | Value |
|---|---|
| Purpose | Accounts for registered farmers, admins and (Phase 2) buyers. The §20 fields, `created_at`, and the email-verification and password-reset state (ADR-0048); no identity-provider or OAuth tables. Every FARMER and ADMIN account has an email (ADR-0047). |
| Owning module | auth |
| Phase | Phase 1 |
| Status | REQUIRED |
| Table group | Identity and access |
| Source / requirement | §20 USERS; §10 (register, login, refresh, logout); §25 (quick phone registration); §28 (BCrypt, roles); FR-20; ADR-0047 and ADR-0048 (email at registration, email verification, password recovery) |
| Natural key | `phone_number`, `email` (spec UKs) |
| Record class | User-owned data; deactivated with `is_active` |
| Deletion behavior | Deactivation via `is_active` (§20). Erasure (ADR-0029): deleting a user cascades to `farmer_profiles` (and through it to alerts and events) and to `refresh_tokens`. ADMIN users are deactivated, never deleted (`RESTRICT` from `admin_audit_events`). The email-verification and password-reset columns are cleared when the token is used or replaced, and expired values are cleared by the auth module's daily purge (ADR-0048). |

| Column | Type | Nullable | Default | PK | FK | Unique | Description | Basis | Source / requirement | Sensitivity |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | uuid | NO | `gen_random_uuid()` | YES | — | — | Surrogate primary key. | REQUIREMENT FROM SSOT | §20; default: ARCHITECTURAL DECISION | PERSONAL |
| `phone_number` | varchar(20) | YES | — | — | — | YES | Indian mobile number in E.164 form (`+91` and 10 digits starting 6–9); required for FARMER (ADR-0016). | REQUIREMENT FROM SSOT | §20 (UK); nullability and format ADR-0016 | PERSONAL |
| `email` | varchar(254) | YES | — | — | — | YES | Email address, stored in lower case; required for FARMER and ADMIN (ADR-0016, ADR-0047). A second login identifier of the same account and the only password-recovery channel. Nullable only for Phase 2 roles, whose rules are not designed. | REQUIREMENT FROM SSOT | §20 (UK); nullability and case ADR-0016, ADR-0047 | PERSONAL |
| `password_hash` | varchar(100) | NO | — | — | — | — | BCrypt hash, cost 12. The password itself is never stored. Passwords: FARMER 8–64, ADMIN 15–64 characters, at most 72 UTF-8 bytes (ADR-0016). | REQUIREMENT FROM SSOT | §20; §28; ADR-0016 | CREDENTIAL |
| `role` | varchar(10) | NO | — | — | — | — | `FARMER`, `BUYER` or `ADMIN` (one role per account). | REQUIREMENT FROM SSOT | §20; §28 | INTERNAL |
| `is_active` | boolean | NO | `true` | — | — | — | Account status. | REQUIREMENT FROM SSOT | §20; default value: ARCHITECTURAL DECISION | INTERNAL |
| `created_at` | timestamptz | NO | `now()` | — | — | — | When the account was created. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: account audit (ADR-0016) | INTERNAL |
| `email_verified_at` | timestamptz | YES | — | — | — | — | When the account's email was verified; NULL while unverified. A provisioned ADMIN's email is recorded as verified at creation. | ARCHITECTURAL DECISION | ADR-0047, ADR-0048 | INTERNAL |
| `email_verification_token_hash` | varchar(64) | YES | — | — | — | YES | Lowercase hex SHA-256 of the account's one active email-verification token. The token itself is never stored. NULL when no link is outstanding. | ARCHITECTURAL DECISION | ADR-0048 | CREDENTIAL |
| `email_verification_expires_at` | timestamptz | YES | — | — | — | — | Issue time + 24 hours; present exactly when the hash is. | ARCHITECTURAL DECISION | ADR-0048 | INTERNAL |
| `password_reset_token_hash` | varchar(64) | YES | — | — | — | YES | Lowercase hex SHA-256 of the account's one active password-reset token, issued only to a verified email. The token itself is never stored. | ARCHITECTURAL DECISION | ADR-0048 | CREDENTIAL |
| `password_reset_expires_at` | timestamptz | YES | — | — | — | — | Issue time + 30 minutes; present exactly when the hash is. | ARCHITECTURAL DECISION | ADR-0048 | INTERNAL |

**Foreign keys**

- None.

**Unique constraints**

- `uq_users_phone_number`: `phone_number` (REQUIREMENT FROM SSOT).
- `uq_users_email`: `email` (REQUIREMENT FROM SSOT).
- `uq_users_email_verification_token_hash`: `email_verification_token_hash` (ARCHITECTURAL DECISION, ADR-0048).
- `uq_users_password_reset_token_hash`: `password_reset_token_hash` (ARCHITECTURAL DECISION, ADR-0048).

CHECK constraints and indexes: [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md#users).

### farmer_profiles

| Field | Value |
|---|---|
| Purpose | Farmer-specific settings for a registered account (display name, default district, preferred language). Owner of searches and alerts. A FARMER gets a profile at registration; an ADMIN gets one on first need, to own alerts (ADR-0016). |
| Owning module | auth |
| Phase | Phase 1 |
| Status | REQUIRED |
| Table group | Identity and access |
| Source / requirement | §20 FARMER_PROFILES; F1.2; §25 |
| Natural key | `user_id` (spec FK UK) |
| Record class | User-owned data |
| Deletion behavior | Deleted with its user (`ON DELETE CASCADE` from `users`, ADR-0029). Deleting a profile cascades to `price_alerts` and sets `selling_searches.farmer_profile_id` to NULL. |

| Column | Type | Nullable | Default | PK | FK | Unique | Description | Basis | Source / requirement | Sensitivity |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | uuid | NO | `gen_random_uuid()` | YES | — | — | Surrogate primary key. | REQUIREMENT FROM SSOT | §20; default: ARCHITECTURAL DECISION | PERSONAL |
| `user_id` | uuid | NO | — | — | `users.id` | YES | Account this profile belongs to (at most one profile per account). | REQUIREMENT FROM SSOT | §20 (FK UK) | PERSONAL |
| `display_name` | varchar(100) | YES | — | — | — | — | Name shown to the farmer. | REQUIREMENT FROM SSOT | §20; nullability: ARCHITECTURAL DECISION | PERSONAL |
| `default_district_id` | uuid | YES | — | — | `districts.id` | — | Default district for the wizard (district granularity only, §28). | REQUIREMENT FROM SSOT | §20 | PERSONAL |
| `preferred_locale` | varchar(5) | YES | — | — | — | — | `mr`, `hi` or `en`. | REQUIREMENT FROM SSOT | §20; values F1.2 and §43 (`public/locales/mr\|hi\|en`) | PERSONAL |

**Foreign keys**

- `fk_farmer_profiles_user`: (`user_id`) → `users` (`id`), ON DELETE CASCADE.
- `fk_farmer_profiles_default_district`: (`default_district_id`) → `districts` (`id`), ON DELETE RESTRICT.

**Unique constraints**

- `uq_farmer_profiles_user`: `user_id` (REQUIREMENT FROM SSOT).

CHECK constraints and indexes: [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md#farmer_profiles).

### refresh_tokens

| Field | Value |
|---|---|
| Purpose | Hashed refresh tokens (7-day TTL), rotated on every refresh with reuse detection by token family (ADR-0016; RFC 9700 §4.14.2). Rotation and logout revoke rows; revoked rows stay until expiry so that reuse is detected. Security-sensitive: explicit review required (CLAUDE.md rule 16). |
| Owning module | auth |
| Phase | Phase 1 |
| Status | REQUIRED |
| Table group | Identity and access |
| Source / requirement | §28 ("SHA-256 hashed, stored in DB, 7-day TTL", "Backend refresh_tokens table"); §27 (rotation); §10 (refresh, logout); §29 |
| Natural key | `token_hash` |
| Record class | Security/session data |
| Deletion behavior | Revoked (`revoked_at`), not deleted, on rotation and logout; rows are deleted one day after expiry (ADR-0029); `ON DELETE CASCADE` from `users`. |

| Column | Type | Nullable | Default | PK | FK | Unique | Description | Basis | Source / requirement | Sensitivity |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | uuid | NO | `gen_random_uuid()` | YES | — | — | Surrogate primary key. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION (table named in §28; columns not specified) | INTERNAL |
| `user_id` | uuid | NO | — | — | `users.id` | — | Account the token was issued to. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION derived from §28 | PERSONAL |
| `token_hash` | varchar(64) | NO | — | — | — | YES | Lowercase hex SHA-256 of the refresh token. The token itself is never stored. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION derived from §28 ("SHA-256 hashed") | CREDENTIAL |
| `expires_at` | timestamptz | NO | — | — | — | — | Issue time + 7 days. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION derived from §28 ("7-day TTL") | INTERNAL |
| `family_id` | uuid | NO | — | — | — | — | Token family: every token issued from one login shares it; presenting a revoked token revokes the whole family (ADR-0016). | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0016 (RFC 9700 §4.14.2) | INTERNAL |
| `created_at` | timestamptz | NO | `now()` | — | — | — | Issue time. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0016 | INTERNAL |
| `revoked_at` | timestamptz | YES | — | — | — | — | When the token was rotated, logged out or revoked with its family; NULL while usable. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0016 | INTERNAL |

**Foreign keys**

- `fk_refresh_tokens_user`: (`user_id`) → `users` (`id`), ON DELETE CASCADE.

**Unique constraints**

- `uq_refresh_tokens_token_hash`: `token_hash` (ARCHITECTURAL DECISION).

CHECK constraints and indexes: [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md#refresh_tokens).

### transport_assumptions

| Field | Value |
|---|---|
| Purpose | Admin-managed transport parameters (§32). Versioned by insertion: a change inserts a new row and deactivates the previous one, so every recommendation run keeps pointing at the exact values it used. |
| Owning module | transport |
| Phase | Phase 1 |
| Status | REQUIRED |
| Table group | Recommendation configuration |
| Source / requirement | §20 TRANSPORT_ASSUMPTIONS; §16 (load active); §32 (parameters and defaults); UC19; §30 `PUT /api/v1/admin/config/*` |
| Natural key | none; at most one active row |
| Record class | Mutable configuration (versioned) |
| Deletion behavior | Rows referenced by `recommendation_runs` are never updated or deleted (`RESTRICT`). |

| Column | Type | Nullable | Default | PK | FK | Unique | Description | Basis | Source / requirement | Sensitivity |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | uuid | NO | `gen_random_uuid()` | YES | — | — | Surrogate primary key. | REQUIREMENT FROM SSOT | §20; default: ARCHITECTURAL DECISION | INTERNAL |
| `cost_per_km` | numeric(10,2) | NO | — | — | — | — | `ratePerKm`, rupees per km per trip (default ₹8.00). | REQUIREMENT FROM SSOT | §20; §32 | INTERNAL |
| `minimum_trip_charge` | numeric(12,2) | NO | — | — | — | — | `minimumTripCharge`, rupees per trip (default ₹300). | REQUIREMENT FROM SSOT | §20; §32 | INTERNAL |
| `vehicle_capacity_kg` | numeric(10,2) | NO | — | — | — | — | `vehicleCapacityKg` (default 1,000 kg). | REQUIREMENT FROM SSOT | §20; §32 | INTERNAL |
| `is_active` | boolean | NO | `false` | — | — | — | The row used by new comparisons. | REQUIREMENT FROM SSOT | §20; §16; default value: ARCHITECTURAL DECISION | INTERNAL |
| `created_at` | timestamptz | NO | `now()` | — | — | — | When this version was created. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: configuration history ("which assumptions contributed") | INTERNAL |

**Foreign keys**

- None.

**Unique constraints**

- `uq_transport_assumptions_single_active`: (is_active), partial WHERE `is_active` (ARCHITECTURAL DECISION).

CHECK constraints and indexes: [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md#transport_assumptions).

### recommendation_weights

| Field | Value |
|---|---|
| Purpose | Admin-configurable scoring weights and freshness thresholds, versioned like `transport_assumptions`. Stored separately; no scoring formula lives in the database. |
| Owning module | recommendation |
| Phase | Phase 1 |
| Status | REQUIRED |
| Table group | Recommendation configuration |
| Source / requirement | §20 RECOMMENDATION_WEIGHTS; §16 (load active; freshness defaults 1/4/10); UC20; §30 |
| Natural key | none; at most one active row |
| Record class | Mutable configuration (versioned) |
| Deletion behavior | Rows referenced by `recommendation_runs` are never updated or deleted (`RESTRICT`). |

| Column | Type | Nullable | Default | PK | FK | Unique | Description | Basis | Source / requirement | Sensitivity |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | uuid | NO | `gen_random_uuid()` | YES | — | — | Surrogate primary key. | REQUIREMENT FROM SSOT | §20; default: ARCHITECTURAL DECISION | INTERNAL |
| `w_net_realization` | numeric(6,4) | NO | — | — | — | — | Weight of normalized net realization (default 0.55, ADR-0024). | REQUIREMENT FROM SSOT | §20; default §21 step 6, ADR-0024 | INTERNAL |
| `w_price` | numeric(6,4) | NO | — | — | — | — | Weight of normalized price (default 0.15, ADR-0024). | REQUIREMENT FROM SSOT | §20; default §21 step 6, ADR-0024 | INTERNAL |
| `w_trend` | numeric(6,4) | NO | — | — | — | — | Weight of the trend score (default 0.10, ADR-0024). | REQUIREMENT FROM SSOT | §20; default ADR-0024 | INTERNAL |
| `w_freshness` | numeric(6,4) | NO | — | — | — | — | Weight of the freshness score (default 0.10, ADR-0024). | REQUIREMENT FROM SSOT | §20; default ADR-0024 | INTERNAL |
| `w_transport_penalty` | numeric(6,4) | NO | — | — | — | — | Weight of the transport penalty (default 0.10, ADR-0024). | REQUIREMENT FROM SSOT | §20; default ADR-0024 | INTERNAL |
| `freshness_fresh_days` | integer | NO | — | — | — | — | FRESH threshold in days (default 1). | REQUIREMENT FROM SSOT | §20; §16 | INTERNAL |
| `freshness_recent_days` | integer | NO | — | — | — | — | RECENT threshold in days (default 4). | REQUIREMENT FROM SSOT | §20; §16 | INTERNAL |
| `freshness_stale_days` | integer | NO | — | — | — | — | STALE threshold in days (default 10); older is UNAVAILABLE. | REQUIREMENT FROM SSOT | §20; §16 | INTERNAL |
| `is_active` | boolean | NO | `false` | — | — | — | The row used by new comparisons. | REQUIREMENT FROM SSOT | §20; §16; default value: ARCHITECTURAL DECISION | INTERNAL |
| `created_at` | timestamptz | NO | `now()` | — | — | — | When this version was created. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: configuration history | INTERNAL |

**Foreign keys**

- None.

**Unique constraints**

- `uq_recommendation_weights_single_active`: (is_active), partial WHERE `is_active` (ARCHITECTURAL DECISION).

CHECK constraints and indexes: [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md#recommendation_weights).

### selling_searches

| Field | Value |
|---|---|
| Purpose | The farmer input of one comparison (§16 PERSIST). Anonymous comparisons are allowed, so the profile reference is optional. Origin is the district centroid, never an address. |
| Owning module | recommendation |
| Phase | Phase 1 |
| Status | REQUIRED |
| Table group | Recommendation results |
| Source / requirement | §20 SELLING_SEARCHES; §16 (input, PERSIST); §21; F1.1, F1.3–F1.6; §28 (anonymous OK; PII minimization) |
| Natural key | none (event row) |
| Record class | Derived data (decision record); user-related when linked to a profile |
| Deletion behavior | Retained indefinitely for Experiment 1; on account erasure `farmer_profile_id` is set to NULL (ADR-0029). |

| Column | Type | Nullable | Default | PK | FK | Unique | Description | Basis | Source / requirement | Sensitivity |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | uuid | NO | `gen_random_uuid()` | YES | — | — | Surrogate primary key. | REQUIREMENT FROM SSOT | §20; default: ARCHITECTURAL DECISION | INTERNAL |
| `farmer_profile_id` | uuid | YES | — | — | `farmer_profiles.id` | — | NULL for anonymous comparisons, for callers without a farmer profile, and after account erasure (ADR-0029). | REQUIREMENT FROM SSOT | §20; nullability: ARCHITECTURAL DECISION (F1.1, §28 "anonymous ok") | PERSONAL |
| `commodity_id` | uuid | NO | — | — | `commodities.id` | — | Selected commodity. | REQUIREMENT FROM SSOT | §20 | INTERNAL |
| `variety_id` | uuid | YES | — | — | `varieties.id (composite with commodity_id)` | — | Selected variety; NULL when none was selected, in which case the lower-median representative observation is used (ADR-0010). | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: persists §16 input `varietyId?` (F1.4); semantics ADR-0010 | INTERNAL |
| `grade_id` | uuid | YES | — | — | `grades.id (composite with commodity_id)` | — | Selected grade; NULL when none was selected (ADR-0010). | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: persists §16 input `gradeId?` (F1.4); semantics ADR-0010 | INTERNAL |
| `quantity` | numeric(12,3) | NO | — | — | — | — | Quantity in `quantity_unit`. | REQUIREMENT FROM SSOT | §20; §16 | INTERNAL |
| `quantity_unit` | varchar(10) | NO | — | — | — | — | `KG` or `QUINTAL`. | REQUIREMENT FROM SSOT | §20; values §16, §25, §32 | INTERNAL |
| `origin_lat` | numeric(9,6) | NO | — | — | — | — | Origin latitude: the selected district centroid. The backend stores only a value equal to a stored district centroid (ADR-0039). | REQUIREMENT FROM SSOT | §20; FR-06, §21 step 3 | PERSONAL |
| `origin_lon` | numeric(9,6) | NO | — | — | — | — | Origin longitude: the selected district centroid, validated with `origin_lat` (ADR-0039). | REQUIREMENT FROM SSOT | §20; FR-06 | PERSONAL |
| `max_distance_km` | integer | NO | — | — | — | — | Search radius actually applied (API default 150). | REQUIREMENT FROM SSOT | §20; §16 | INTERNAL |

**Foreign keys**

- `fk_selling_searches_farmer_profile`: (`farmer_profile_id`) → `farmer_profiles` (`id`), ON DELETE SET NULL.
- `fk_selling_searches_commodity`: (`commodity_id`) → `commodities` (`id`), ON DELETE RESTRICT.
- `fk_selling_searches_variety`: (`variety_id`, `commodity_id`) → `varieties` (`id`, `commodity_id`), ON DELETE RESTRICT.
- `fk_selling_searches_grade`: (`grade_id`, `commodity_id`) → `grades` (`id`, `commodity_id`), ON DELETE RESTRICT.

**Unique constraints**

- None beyond the primary key.

CHECK constraints and indexes: [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md#selling_searches).

### recommendation_runs

| Field | Value |
|---|---|
| Purpose | One persisted comparison result: which weights and transport assumptions were used, the price-only winner, the net winner and whether they disagree. Basis of Experiment 1. |
| Owning module | recommendation |
| Phase | Phase 1 |
| Status | REQUIRED |
| Table group | Recommendation results |
| Source / requirement | §20 RECOMMENDATION_RUNS; §16 (RANK, PERSIST); §17 ("last recommendation_run"); §36 Experiment 1; §47 Q23 |
| Natural key | none (event row) |
| Record class | Derived data (decision record) |
| Deletion behavior | `ON DELETE CASCADE` from `selling_searches` (part of the search); retained indefinitely (ADR-0029). |

| Column | Type | Nullable | Default | PK | FK | Unique | Description | Basis | Source / requirement | Sensitivity |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | uuid | NO | `gen_random_uuid()` | YES | — | — | Surrogate primary key; the `recommendationId` returned to clients (§16). | REQUIREMENT FROM SSOT | §20; default: ARCHITECTURAL DECISION | INTERNAL |
| `selling_search_id` | uuid | NO | — | — | `selling_searches.id` | — | Input that produced the run. | REQUIREMENT FROM SSOT | §20 | INTERNAL |
| `weights_id` | uuid | NO | — | — | `recommendation_weights.id` | — | Weights version used. | REQUIREMENT FROM SSOT | §20 | INTERNAL |
| `transport_id` | uuid | NO | — | — | `transport_assumptions.id` | — | Transport assumptions version used. | REQUIREMENT FROM SSOT | §20 | INTERNAL |
| `price_only_winner_market_id` | uuid | YES | — | — | `markets.id` | — | Market with the highest modal price; NULL only if no option was eligible. | REQUIREMENT FROM SSOT | §20; nullability: ARCHITECTURAL DECISION (no-data case, ADR-0023) | INTERNAL |
| `net_winner_market_id` | uuid | YES | — | — | `markets.id` | — | Rank-1 market by composite score; NULL only if no option was eligible. | REQUIREMENT FROM SSOT | §20; nullability: ARCHITECTURAL DECISION (no-data case, ADR-0023) | INTERNAL |
| `rankings_disagree` | boolean | NO | — | — | — | — | TRUE when the two winners differ (§16). | REQUIREMENT FROM SSOT | §20; §16 | INTERNAL |
| `created_at` | timestamptz | NO | `now()` | — | — | — | When the run was produced; identifies the "last" run (§17, §47 Q23). | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION derived from §17 and §47 Q23 | INTERNAL |

**Foreign keys**

- `fk_recommendation_runs_search`: (`selling_search_id`) → `selling_searches` (`id`), ON DELETE CASCADE.
- `fk_recommendation_runs_weights`: (`weights_id`) → `recommendation_weights` (`id`), ON DELETE RESTRICT.
- `fk_recommendation_runs_transport`: (`transport_id`) → `transport_assumptions` (`id`), ON DELETE RESTRICT.
- `fk_recommendation_runs_price_winner`: (`price_only_winner_market_id`) → `markets` (`id`), ON DELETE RESTRICT.
- `fk_recommendation_runs_net_winner`: (`net_winner_market_id`) → `markets` (`id`), ON DELETE RESTRICT.

**Unique constraints**

- None beyond the primary key.

CHECK constraints and indexes: [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md#recommendation_runs).

### selling_options

| Field | Value |
|---|---|
| Purpose | One ranked candidate market per run, with the observation used, distance, estimated transport cost, gross value, net realization, trend, freshness, score and reason codes. Prices are referenced, not copied; the gross value preserves the price used. |
| Owning module | recommendation |
| Phase | Phase 1 |
| Status | REQUIRED |
| Table group | Recommendation results |
| Source / requirement | §20 SELLING_OPTIONS; §16 (calculations, reasons, rank, PERSIST); §32; FR-07–FR-13; §36 |
| Natural key | (`recommendation_run_id`, `market_id`) and (`recommendation_run_id`, `rank`) — ARCHITECTURAL DECISION |
| Record class | Derived data (decision record) |
| Deletion behavior | `ON DELETE CASCADE` from `recommendation_runs`; `RESTRICT` to `price_observations` and `markets`. |

| Column | Type | Nullable | Default | PK | FK | Unique | Description | Basis | Source / requirement | Sensitivity |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | uuid | NO | `gen_random_uuid()` | YES | — | — | Surrogate primary key. | REQUIREMENT FROM SSOT | §20; default: ARCHITECTURAL DECISION | INTERNAL |
| `recommendation_run_id` | uuid | NO | — | — | `recommendation_runs.id` | composite: uq_selling_options_run_market, uq_selling_options_run_rank | Run the option belongs to. | REQUIREMENT FROM SSOT | §20 | INTERNAL |
| `market_id` | uuid | NO | — | — | `markets.id` | composite: uq_selling_options_run_market | Candidate market. | REQUIREMENT FROM SSOT | §20 | INTERNAL |
| `price_observation_id` | uuid | NO | — | — | `price_observations.id` | — | Observation the option was computed from (ADR-0010). NOT NULL because only eligible candidates are persisted; excluded markets are returned but never persisted (ADR-0023). | REQUIREMENT FROM SSOT | §20; NOT NULL: ARCHITECTURAL DECISION (ADR-0023) | INTERNAL |
| `distance_km` | numeric(8,2) | NO | — | — | — | — | Haversine distance from the origin (approximate). | REQUIREMENT FROM SSOT | §20; §16; §47 Q6 | INTERNAL |
| `estimated_transport_cost` | numeric(14,2) | NO | — | — | — | — | `max(minimumTripCharge, distanceKm × ratePerKm) × trips` (ESTIMATED). | REQUIREMENT FROM SSOT | §20; §32; FR-07 | INTERNAL |
| `estimated_gross_value` | numeric(14,2) | NO | — | — | — | — | `normalizedModalPrice × quantityInQuintal` (ESTIMATED). | REQUIREMENT FROM SSOT | §20; §32; FR-08; unit `Rs./Quintal` only (ADR-0008) | INTERNAL |
| `estimated_net_realization` | numeric(14,2) | NO | — | — | — | — | Gross value − transport cost (ESTIMATED); can be negative. | REQUIREMENT FROM SSOT | §20; §32; FR-09 | INTERNAL |
| `trend_direction` | varchar(20) | NO | — | — | — | — | `RISING`, `STABLE`, `FALLING` or `INSUFFICIENT_DATA`. | REQUIREMENT FROM SSOT | §20; values §16, §33; INSUFFICIENT_DATA is a direction (ADR-0025) | INTERNAL |
| `freshness_status` | varchar(20) | NO | — | — | — | — | Freshness at run time: `FRESH`, `RECENT`, `STALE` or `UNAVAILABLE`. | REQUIREMENT FROM SSOT | §20; §16; F1.16 | INTERNAL |
| `rank` | integer | NO | — | — | — | composite: uq_selling_options_run_rank | Rank 1..N within the run. | REQUIREMENT FROM SSOT | §20; §16 | INTERNAL |
| `score` | numeric(10,6) | NO | — | — | — | — | Composite score (can be negative because of the transport penalty). | REQUIREMENT FROM SSOT | §20; §16; completed by ADR-0024 | INTERNAL |
| `reason_codes` | text[] | NO | `'{}'` | — | — | — | Fixed-vocabulary reason codes (§16). | REQUIREMENT FROM SSOT | §20; §16; P2 | INTERNAL |

**Foreign keys**

- `fk_selling_options_run`: (`recommendation_run_id`) → `recommendation_runs` (`id`), ON DELETE CASCADE.
- `fk_selling_options_market`: (`market_id`) → `markets` (`id`), ON DELETE RESTRICT.
- `fk_selling_options_observation`: (`price_observation_id`) → `price_observations` (`id`), ON DELETE RESTRICT.

**Unique constraints**

- `uq_selling_options_run_market`: `recommendation_run_id`, `market_id` (ARCHITECTURAL DECISION).
- `uq_selling_options_run_rank`: `recommendation_run_id`, `rank` (ARCHITECTURAL DECISION).

CHECK constraints and indexes: [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md#selling_options).

### price_alerts

| Field | Value |
|---|---|
| Purpose | Farmer price alerts (threshold or percentage movement) for a commodity and an optional market. Lifecycle ACTIVE / PAUSED; deletion is a real delete. |
| Owning module | alert |
| Phase | Phase 1 |
| Status | REQUIRED |
| Table group | Alerts |
| Source / requirement | §20 PRICE_ALERTS; §34 (types, lifecycle); §25 (marketId optional); F1.19; FR-19; §28 |
| Natural key | none |
| Record class | User-owned data |
| Deletion behavior | Farmer delete = `DELETE` that cascades to `alert_events` (§34). Deleted with the owning profile (`ON DELETE CASCADE`, ADR-0029). `RESTRICT` to `commodities`, `markets`. |

| Column | Type | Nullable | Default | PK | FK | Unique | Description | Basis | Source / requirement | Sensitivity |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | uuid | NO | `gen_random_uuid()` | YES | — | — | Surrogate primary key. | REQUIREMENT FROM SSOT | §20; default: ARCHITECTURAL DECISION | INTERNAL |
| `farmer_profile_id` | uuid | NO | — | — | `farmer_profiles.id` | — | Owner of the alert. | REQUIREMENT FROM SSOT | §20; ADMIN-owned alerts use a profile created on demand (ADR-0016) | PERSONAL |
| `alert_type` | varchar(30) | NO | — | — | — | — | `PRICE_ABOVE`, `PRICE_BELOW`, `PERCENTAGE_RISE` or `PERCENTAGE_FALL`. | REQUIREMENT FROM SSOT | §20; §34 | INTERNAL |
| `commodity_id` | uuid | NO | — | — | `commodities.id` | — | Monitored commodity. | REQUIREMENT FROM SSOT | §20 | INTERNAL |
| `market_id` | uuid | YES | — | — | `markets.id` | — | Optional market scope. | REQUIREMENT FROM SSOT | §20; optional per §25, §34 | INTERNAL |
| `threshold_value` | numeric(14,4) | NO | — | — | — | — | Price threshold (price types) or percentage magnitude (percentage types). | REQUIREMENT FROM SSOT | §20; §34; price thresholds in `Rs./Quintal` (ADR-0008) | INTERNAL |
| `status` | varchar(10) | NO | `'ACTIVE'` | — | — | — | `ACTIVE` or `PAUSED`. | REQUIREMENT FROM SSOT | §20; §34 ("Farmer creates alert → status=ACTIVE") | INTERNAL |
| `last_triggered_at` | timestamptz | YES | — | — | — | — | Time of the latest alert event. | REQUIREMENT FROM SSOT | §20; §34 | INTERNAL |

**Foreign keys**

- `fk_price_alerts_farmer_profile`: (`farmer_profile_id`) → `farmer_profiles` (`id`), ON DELETE CASCADE.
- `fk_price_alerts_commodity`: (`commodity_id`) → `commodities` (`id`), ON DELETE RESTRICT.
- `fk_price_alerts_market`: (`market_id`) → `markets` (`id`), ON DELETE RESTRICT.

**Unique constraints**

- None beyond the primary key.

CHECK constraints and indexes: [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md#price_alerts).

### alert_events

| Field | Value |
|---|---|
| Purpose | History of alert triggers, delivered in-app by polling (ADR-0019): at most one event per alert per ingestion run, with the market and observation that met the condition, and a read marker. |
| Owning module | alert |
| Phase | Phase 1 |
| Status | REQUIRED |
| Table group | Alerts |
| Source / requirement | §20 ALERT_EVENTS; §14; §15; §22; §34 |
| Natural key | none (event row) |
| Record class | Derived data (event history) |
| Deletion behavior | `ON DELETE CASCADE` from `price_alerts` (§34); rows older than 180 days are purged daily (ADR-0029). |

| Column | Type | Nullable | Default | PK | FK | Unique | Description | Basis | Source / requirement | Sensitivity |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | uuid | NO | `gen_random_uuid()` | YES | — | — | Surrogate primary key. | REQUIREMENT FROM SSOT | §20; default: ARCHITECTURAL DECISION | INTERNAL |
| `price_alert_id` | uuid | NO | — | — | `price_alerts.id` | composite: uq_alert_events_alert_run | Alert that fired. | REQUIREMENT FROM SSOT | §20 | INTERNAL |
| `triggered_at` | timestamptz | NO | `now()` | — | — | — | Trigger time. | REQUIREMENT FROM SSOT | §20 | INTERNAL |
| `trigger_value` | numeric(14,4) | NO | — | — | — | — | Value that met the condition (latest modal price, or 7-day % change, which can be negative). | REQUIREMENT FROM SSOT | §20; §34 | INTERNAL |
| `ingestion_run_id` | uuid | NO | — | — | `ingestion_runs.id` | composite: uq_alert_events_alert_run | Run whose new or revised observations triggered the event. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0019 (one event per alert per run) | INTERNAL |
| `market_id` | uuid | NO | — | — | `markets.id` | — | Market that met the condition (for alerts without a market: the most extreme qualifying market). | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0019 | INTERNAL |
| `price_observation_id` | uuid | NO | — | — | `price_observations.id` | — | Latest selected observation behind `trigger_value` (ADR-0010). | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0019 (provenance) | INTERNAL |
| `read_at` | timestamptz | YES | — | — | — | — | When the farmer viewed or dismissed the event; the alert stays ACTIVE (§34). | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0019 | INTERNAL |

**Foreign keys**

- `fk_alert_events_alert`: (`price_alert_id`) → `price_alerts` (`id`), ON DELETE CASCADE.
- `fk_alert_events_run`: (`ingestion_run_id`) → `ingestion_runs` (`id`), ON DELETE RESTRICT.
- `fk_alert_events_market`: (`market_id`) → `markets` (`id`), ON DELETE RESTRICT.
- `fk_alert_events_observation`: (`price_observation_id`) → `price_observations` (`id`), ON DELETE RESTRICT.

**Unique constraints**

- `uq_alert_events_alert_run`: `price_alert_id`, `ingestion_run_id` (ARCHITECTURAL DECISION).

CHECK constraints and indexes: [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md#alert_events).

### admin_audit_events

| Field | Value |
|---|---|
| Purpose | Append-only audit of every admin action: actor, action, target, masked before/after details, correlation ID and time (ADR-0020). Never holds secrets. |
| Owning module | admin |
| Phase | Phase 1 |
| Status | REQUIRED |
| Table group | Administration |
| Source / requirement | ADR-0020; ADR-0028 (admin bootstrap); ADR-0029 (account erasure); §27 (correlation ID per request); CLAUDE.md §K |
| Natural key | none (event row) |
| Record class | Audit data (append-only) |
| Deletion behavior | Never deleted by the application; retained indefinitely (ADR-0029). `RESTRICT` to `users` (admins are deactivated, never deleted). |

| Column | Type | Nullable | Default | PK | FK | Unique | Description | Basis | Source / requirement | Sensitivity |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | uuid | NO | `gen_random_uuid()` | YES | — | — | Surrogate primary key. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0020 | INTERNAL |
| `actor_user_id` | uuid | NO | — | — | `users.id` | — | ADMIN who performed the action (for `ADMIN_BOOTSTRAPPED`, the created admin). | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0020 | INTERNAL |
| `action` | varchar(50) | NO | — | — | — | — | Closed vocabulary: `INGESTION_TRIGGERED`, `INGESTION_PAUSED`, `INGESTION_RESUMED`, `TRANSPORT_ASSUMPTIONS_UPDATED`, `RECOMMENDATION_WEIGHTS_UPDATED`, `MARKET_VERIFIED`, `MARKET_MERGED`, `MARKET_DEACTIVATED`, `MARKET_REACTIVATED`, `MARKET_COORDINATES_SET`, `COMMODITY_ADDED`, `COMMODITY_UNIT_VERIFIED`, `COMMODITY_NAMES_UPDATED`, `DISTRICT_ALIAS_ADDED`, `ADMIN_BOOTSTRAPPED`, `ACCOUNT_ERASED`, `ACCOUNT_DEACTIVATED`, `ACCOUNT_REACTIVATED`. The last three are written only by in-application operator procedures, never by an HTTP operation (ADR-0037, ADR-0041). | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0020, ADR-0037, ADR-0041 | INTERNAL |
| `target_type` | varchar(50) | NO | — | — | — | — | Kind of object acted on (for example `market`, `commodity`, `transport_assumptions`, `source_dataset`, `user`). | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0020 | INTERNAL |
| `target_id` | varchar(100) | YES | — | — | — | — | Identifier of the target (for example the new configuration version ID). | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0020 | INTERNAL |
| `details` | jsonb | NO | `'{}'::jsonb` | — | — | — | Masked before/after values and justification (for example a merge evidence note or dropped conflicting values). Never credentials. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0014, ADR-0020 | INTERNAL |
| `correlation_id` | varchar(100) | YES | — | — | — | — | Request correlation ID (§27 structured logs). | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0020; §27 | INTERNAL |
| `created_at` | timestamptz | NO | `now()` | — | — | — | When the action happened. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION: ADR-0020 | INTERNAL |

**Foreign keys**

- `fk_admin_audit_events_actor`: (`actor_user_id`) → `users` (`id`), ON DELETE RESTRICT.

**Unique constraints**

- None beyond the primary key.

CHECK constraints and indexes: [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md#admin_audit_events).

### forecasts

| Field | Value |
|---|---|
| Purpose | Optional ML output (§35): one row per forecast point. Never read by comparison, recommendation, transport or alerts. No row means "forecast unavailable". |
| Owning module | price |
| Phase | Optional (ML) |
| Status | OPTIONAL |
| Table group | Optional ML |
| Source / requirement | §35 (output contract "written to forecasts table"); §12; §14; §29; §41 `contracts/forecast-output.schema.json`; §47 Q14 |
| Natural key | (`market_id`, `commodity_id`, `forecast_horizon_days`, `generated_at`, `forecast_date`) — ARCHITECTURAL DECISION |
| Record class | Derived data (optional) |
| Deletion behavior | Rows are deleted 90 days after their `forecast_date` (ADR-0029). |

| Column | Type | Nullable | Default | PK | FK | Unique | Description | Basis | Source / requirement | Sensitivity |
|---|---|---|---|---|---|---|---|---|---|---|
| `id` | uuid | NO | `gen_random_uuid()` | YES | — | — | Surrogate primary key. | ARCHITECTURAL DECISION | ARCHITECTURAL DECISION (table named in §35) | INTERNAL |
| `market_id` | uuid | NO | — | — | `markets.id` | composite: uq_forecasts_point | Forecast market. | REQUIREMENT FROM SSOT | §35 `marketId` | INTERNAL |
| `commodity_id` | uuid | NO | — | — | `commodities.id` | composite: uq_forecasts_point | Forecast commodity. §35 output has no variety (input has `varietyId`). | REQUIREMENT FROM SSOT | §35 `commodityId`; built on the representative daily series (ADR-0010, ADR-0030) | INTERNAL |
| `forecast_horizon_days` | integer | NO | — | — | — | composite: uq_forecasts_point | 3 or 7. | REQUIREMENT FROM SSOT | §35 `forecastHorizonDays` ("3 or 7 days ahead") | INTERNAL |
| `forecast_date` | date | NO | — | — | — | composite: uq_forecasts_point | Date the predicted price is for. | REQUIREMENT FROM SSOT | §35 `forecast[].date` | INTERNAL |
| `predicted_price` | numeric(14,2) | NO | — | — | — | — | Predicted modal price, in the unit of the input series (`Rs./Quintal`, ADR-0008). | REQUIREMENT FROM SSOT | §35 `predictedPrice` | INTERNAL |
| `lower_bound` | numeric(14,2) | YES | — | — | — | — | Lower prediction bound, if the model provides one. | REQUIREMENT FROM SSOT | §35 `lowerBound`; nullability: ARCHITECTURAL DECISION | INTERNAL |
| `upper_bound` | numeric(14,2) | YES | — | — | — | — | Upper prediction bound, if the model provides one. | REQUIREMENT FROM SSOT | §35 `upperBound`; nullability: ARCHITECTURAL DECISION | INTERNAL |
| `model_name` | varchar(100) | NO | — | — | — | — | Model, e.g. a §35 baseline. | REQUIREMENT FROM SSOT | §35 `modelName` | INTERNAL |
| `model_version` | varchar(50) | NO | — | — | — | — | Model version. | REQUIREMENT FROM SSOT | §35 `modelVersion` | INTERNAL |
| `mae` | numeric(14,4) | YES | — | — | — | — | Validation MAE. | REQUIREMENT FROM SSOT | §35 `mae` | INTERNAL |
| `rmse` | numeric(14,4) | YES | — | — | — | — | Validation RMSE. | REQUIREMENT FROM SSOT | §35 `rmse` | INTERNAL |
| `training_data_through` | date | NO | — | — | — | — | Last observation date used for training. | REQUIREMENT FROM SSOT | §35 `trainingDataThrough` | INTERNAL |
| `generated_at` | timestamptz | NO | — | — | — | composite: uq_forecasts_point | When the forecast was produced. | REQUIREMENT FROM SSOT | §35 `generatedAt` | INTERNAL |

**Foreign keys**

- `fk_forecasts_market`: (`market_id`) → `markets` (`id`), ON DELETE RESTRICT.
- `fk_forecasts_commodity`: (`commodity_id`) → `commodities` (`id`), ON DELETE RESTRICT.

**Unique constraints**

- `uq_forecasts_point`: `market_id`, `commodity_id`, `forecast_horizon_days`, `generated_at`, `forecast_date` (ARCHITECTURAL DECISION).

CHECK constraints and indexes: [INDEXES_AND_CONSTRAINTS.md](INDEXES_AND_CONSTRAINTS.md#forecasts).

## 4. Phase 2 / deferred tables

**PHASE 2 / DEFERRED.** The spec names these tables (or, for ratings, only the capability). Their columns are not specified, so this dictionary lists none, and [ERD.md §3](ERD.md#3-phase-2--deferred-extension-view) shows them without attributes. Phase 1 tables never reference them.

| Table | Owning module | Phase | Purpose | Source | Columns |
|---|---|---|---|---|---|
| `produce_listings` | listing | PHASE 2 / DEFERRED | Farmer produce listings, optionally linked to a Phase 1 recommendation run. | §23 (`INSERT produce_listings`, `recommendationRunId`, `commodityId`), F2.3, F2.4, §47 Q23 | Not specified. Designed after the Phase 1 Freeze (HARD RULE 1; placements in ADR-0031; RD-012 and the rest of RD-025 deferred by ADR-0032) |
| `bids` | bid | PHASE 2 / DEFERRED | Bids by verified buyers on listings. | §23 (`INSERT bid`, statuses PENDING / ACCEPTED / REJECTED), F2.5–F2.7 | Not specified. Designed after the Phase 1 Freeze (HARD RULE 1; placements in ADR-0031; RD-012 and the rest of RD-025 deferred by ADR-0032) |
| `transactions` | transaction | PHASE 2 / DEFERRED | Transaction created from an accepted bid; server-enforced state machine (state model deferred: RD-012, ADR-0032). | §18, §23 (`INSERT transactions`, `currentState`), F2.8, F2.9 | Not specified. Designed after the Phase 1 Freeze (HARD RULE 1; placements in ADR-0031; RD-012 and the rest of RD-025 deferred by ADR-0032) |
| `transaction_state_history` | transaction | PHASE 2 / DEFERRED | Audit trail of every transaction state transition (actor, timestamp, reason). | §23, F2.11, FR-24 | Not specified. Designed after the Phase 1 Freeze (HARD RULE 1; placements in ADR-0031; RD-012 and the rest of RD-025 deferred by ADR-0032) |
| `buyer_verifications` | auth | PHASE 2 / DEFERRED | Buyer verification state (§19); documents stored in PostgreSQL with access control (ADR-0031). | §19, §23 (`buyer_verifications WHERE status=VERIFIED`), F2.2, F2.12; ADR-0031 | Not specified. Designed after the Phase 1 Freeze (HARD RULE 1; placements in ADR-0031; RD-012 and the rest of RD-025 deferred by ADR-0032) |
| `rating_storage_unnamed` | rating | PHASE 2 / DEFERRED | Post-completion ratings in both directions. Table name not given by the spec. | §23 (`POST /api/v1/ratings/{transactionId}`), F2.10, FR-25; the spec names no table | Not specified. Designed after the Phase 1 Freeze (HARD RULE 1; placements in ADR-0031; RD-012 and the rest of RD-025 deferred by ADR-0032) |

## 5. Columns deliberately not added

Each item below was considered and left out, because no requirement or decision needs it.

| Considered | Decision | Reason |
|---|---|---|
| Account `updated_at`, `last_login_at` | Not added | No requirement. `users.created_at` was added for account audit (ADR-0016) |
| A separate table for email-verification and password-reset tokens | Not added | Each account has at most one active link per purpose, and a new link replaces the old one (ADR-0048). Nullable hash and expiry columns on `users` express exactly that, with no new table, foreign key or cascade |
| An email snapshot on verification tokens; an email-change operation | Not added | Phase 1 has no operation that changes an email (ADR-0047), so a verification link always refers to the account's current email |
| Deleting refresh-token rows on rotation | Not done | Rotation and logout revoke rows (`revoked_at`) so that a reused token is detected and its family revoked (ADR-0016) |
| Raw source strings on every observation | Not added | The source spelling of markets is kept once per spelling in `market_source_aliases`; rejected records keep their full raw payload in `ingestion_errors`. Copying text into every observation would duplicate data |
| `Commodity_Code` | Not added | Historical-only, only `23` observed, uniqueness unverified, and no Phase 1 use ([DATABASE_ARCHITECTURE.md §9.5](DATABASE_ARCHITECTURE.md#95-commodities-varieties-and-grades)) |
| Stored freshness status on observations | Not added | Derived daily from `report_date` and the active thresholds; a stored value would go stale. Decision records keep a snapshot in `selling_options.freshness_status` |
| `ingestion_runs.duration_seconds` (§15) | Derived, not stored | Equals `finished_at - started_at` |
| `ingestion_runs.error_count` (evidence suggestion) | Derived, not stored | Count of the run's `ingestion_errors` rows |
| Resume offset for interrupted runs | Not added | Runs restart at offset 0 and loading is idempotent (ADR-0011) |
| Similarity or fuzzy-match columns for market names | Not added | Aliases are mapped only on evidence (ADR-0014) |
| Delivery queues for alerts (push, email) | Not added | Delivery is in-app polling only (ADR-0019) |
| Dataset-level price unit on `source_datasets` | Not added | The unit is verified per commodity (ADR-0008) |
| Quality flag on observations | Not added | Unit-inconsistent records are rejected with their raw payload kept, never stored with a flag (ADR-0008) |
| Origin district or locality on searches | Not added | The origin coordinates are stored; PII minimization (§28) argues against more |
| Localized names for districts, markets, varieties, grades | Not added | §20 gives localized names only for states and commodities; official names are shown as published (ADR-0015) |
| Market type (APMC, private yard) | Not added | No requirement; the evidence contains both kinds of market and the spec does not distinguish them |
| Commodity "pinned" or "most common" flag (§25) | Not added | A presentation concern with no defined rule; Task 8 or 9 decides whether it is computed or configured |
| Per-module database schemas or roles | Not added | Ownership is enforced by the module rule in code (§27). The only extra role is the optional ML job's restricted role (ADR-0030) |
