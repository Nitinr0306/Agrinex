---
name: agrinex-database
description: AgriNex PostgreSQL schema reasoning. Covers table ownership by backend module, price-observation identity and provenance, numeric precision and CHECK constraints, Flyway migration discipline, reference data and seed sources, retention, personal data and the audit vocabulary. Use this skill whenever a task involves tables, columns, migrations, indexes, constraints, seeds, fixtures, the data dictionary, how ingestion stores observations, or any question about what data AgriNex stores and who may read or write it, even when it is phrased as a backend or API question.
---

# AgriNex database

## Purpose

Keep the AgriNex schema exactly as designed and approved, owned module by module, with every stored price traceable to its government source. Change the schema only through authorized, versioned migrations.

This skill points to the authoritative database documents and does not replace them. If it disagrees with them or with a binding decision, they win: correct this file.

## When to use

- Reading or proposing tables, columns, types, constraints, indexes or foreign keys.
- Writing or reviewing Flyway migrations, seeds or fixtures.
- Questions about observation identity, duplicates, revisions, conflicts, provenance or freshness storage.
- Retention, erasure, personal data, and the admin audit vocabulary.
- Checking that an API field or backend query matches a real column with the right precision.

## Source hierarchy

This follows CLAUDE.md §B:

1. The master specification. §20 is an overview, not a complete schema.
2. Binding decision-log entries.
3. The authoritative database documents in `docs/database/`, and migrations once they exist.
4. Current official PostgreSQL and Flyway documentation, fetched with Context7, for the baseline versions.

## Required reading (READ SSOT FIRST)

1. `CLAUDE.md`: §C and §I. Then `docs/00_SSOT_INDEX.md` area 3 and `docs/REQUIRES_DECISION.md`.
2. `docs/database/DATABASE_INDEX.md`, then:
   - `DATABASE_ARCHITECTURE.md`, `DATA_DICTIONARY.md`, `ERD.md`;
   - `INDEXES_AND_CONSTRAINTS.md`, `TABLE_OWNERSHIP.md`, `SEED_AND_REFERENCE_DATA.md`.
3. Spec §14, §15, §20, §31–§35.
4. Decision-log entries:
   - ingestion and observations: ADR-0005 to ADR-0012;
   - geography, markets and names: ADR-0013 to ADR-0015;
   - alerts and admin: ADR-0019, ADR-0020;
   - demo data, retention and ML: ADR-0028, ADR-0029, ADR-0030;
   - Task 8 decisions: ADR-0035, ADR-0039, ADR-0040, ADR-0041;
   - Task 9 final decisions: ADR-0047 (email required for FARMER) and ADR-0048 (five `users` columns for email verification and password reset, two unique constraints, three CHECKs; no new table).
5. `docs/data/AGRINEX_VERIFIED_GOVERNMENT_API_DATA_REFERENCE.md`, as evidence of source formats only.

## Non-negotiable AgriNex rules

- **One owner per table** (`TABLE_OWNERSHIP.md` §2). The prohibited-access list (§5) is absolute:
  - other modules use the owner's interface, or a read-only port (ADR-0035);
  - no cross-module repository, query or join.
- **Migrations:**
  - The schema changes only through versioned Flyway migrations, and only when a task authorizes them.
  - Never edit an applied migration; add a new one.
  - Flyway and PostgreSQL versions follow `CURRENT_TECHNOLOGY_BASELINE.md`.
- **No invention.** Never invent tables, columns, types, constraints, indexes or defaults (CLAUDE.md rule 4). The approved design is `DATA_DICTIONARY.md` and `INDEXES_AND_CONSTRAINTS.md`.
- **Observation identity** (spec §31 RULE 6; ADR-0009):
  - `business_key` is unique at database level;
  - an identical re-delivery is a skipped duplicate;
  - a changed price is an update with a revision row;
  - records sharing a key but differing in price are quarantined as conflicts, never stored.
- **Provenance and freshness** (ADR-0010, ADR-0022 item 9):
  - every observation keeps its dataset, resource id, ingestion run and `ingested_at`;
  - freshness is derived from the report date and the thresholds, never stored as fact.
- **Prices:**
  - exact decimals, as `numeric(14,2)`;
  - each commodity has one verified unit with evidence, and prices are never converted (ADR-0008).
- **Quantities and estimates:**
  - Quantities are `numeric(12,3)`.
  - Comparison estimates are `numeric(14,2)`. An estimate that would not fit is rejected before anything is stored (422 `ESTIMATE_OUT_OF_RANGE`; ADR-0040). Never widen the columns or invent a cap for it.
- **Keys and time:**
  - primary keys are `uuid` with `gen_random_uuid()`, and timestamps are `timestamptz`;
  - date rules use the Asia/Kolkata calendar (ADR-0007).
- **Closed vocabularies** are CHECK constraints. Extending one needs a decision and a migration. The admin audit vocabulary includes `ACCOUNT_ERASED`, `ACCOUNT_DEACTIVATED` and `ACCOUNT_REACTIVATED`, written only by operator procedures (ADR-0037, ADR-0041).
- **Personal data:**
  - `selling_searches.origin_lat` and `origin_lon` hold only stored district centroids (ADR-0039) and are PERSONAL;
  - retention and erasure cascades follow ADR-0029;
  - admins are deactivated, never deleted.
- **ML tables.** `forecasts` is owned by price. The optional ML role may only SELECT the daily-series view and INSERT/DELETE `forecasts` (ADR-0030).
- **Seeds and demo data:**
  - seeds come only from identified official sources (`SEED_AND_REFERENCE_DATA.md`);
  - the non-government `Agriculture_price_dataset.csv` never seeds anything (ADR-0005 item 3);
  - demo data is an exact export of real ingested observations (ADR-0028);
  - synthetic fixtures never enter a deployment database.
- **Never fabricate data:** no fabricated, zero-filled, carried-forward or interpolated prices, and no invented reference data such as coordinates without an identified source.
- **No market rename.** No approved operation renames a market (ADR-0014, ADR-0020).
- **Phase 2 tables** are DEFERRED, with columns unspecified. Do not design them before the Phase 1 freeze.

## Common rules (apply to every AgriNex skill)

- Read the SSOT first.
- Do not invent:
  - requirements;
  - database fields;
  - API contracts;
  - external data.
- Do not bypass module ownership or silently override an ADR.
- Do not modify unrelated files.
- Security-sensitive changes need human review (CLAUDE.md rule 16).
- Every implementation task needs tests.
- Never mark work complete until its acceptance criteria pass.

## Allowed responsibilities

- Schema reasoning, and impact analysis of a proposed change (CLAUDE.md §I checklist).
- Writing migrations, seeds and fixtures, when a task authorizes it and the design supports it.
- Keeping `DATA_DICTIONARY.md`, `INDEXES_AND_CONSTRAINTS.md`, `ERD.md` and `TABLE_OWNERSHIP.md` consistent with binding decisions.
- Checking that API precisions mirror column types (`docs/api/API_CONVENTIONS.md` §4).

## Forbidden responsibilities

- Schema changes without an approved decision or design.
- Editing applied migrations.
- Cross-module table access.
- Loading non-government or synthetic price data into a deployment.
- Deciding retention, erasure or vocabulary changes on your own.
- Designing Phase 2 tables.

## Dependencies on other skills

- agrinex-architecture: ownership and dependencies.
- agrinex-backend: repositories and migrations in code.
- agrinex-api-contract: field precisions and enumerations.
- agrinex-qa: duplicate, completeness and freshness tests.
- agrinex-release: migration gates.

## Validation expectations

- Each migration is checked against `DATA_DICTIONARY.md` and `INDEXES_AND_CONSTRAINTS.md`: names, types, nullability, defaults, constraints.
- Duplicate prevention is tested: running ingestion twice adds 0 new rows (spec §48).
- Completeness is at least 95 % per run (spec §48; ADR-0012).
- Erasure cascades and ownership boundaries are tested.
- After a change to a closed vocabulary, the contract's matching enumeration is updated too (for example `AdminAuditAction`).

## Escalation rules

- **Field or table not in the approved design:** REQUIRES_DECISION, not invention.
- **CONFLICT between the spec, a decision and a database document:** stop and report. A binding decision governs over database documents (log rule 7).
- **Personal-data, retention or erasure changes:** security-sensitive, so they need human review.

## Output expectations

Report:
- the files changed;
- the tables and columns affected, each with its design reference;
- the migration version, if any;
- the ADRs cited;
- the tests run with their results;
- open items, with evidence labels.

State what was not done.
