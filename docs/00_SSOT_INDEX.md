# AgriNex — SSOT Index

This index answers one question: **where is the authoritative information for this area?** It is for navigation only and does not restate requirements. [`CLAUDE.md` §B](../CLAUDE.md) defines precedence between sources.

**Last reviewed:** 2026-10-18, after the deployment preparation of ADR-0054 and ADR-0055 (previous reviews: 2026-10-18, Phase 1 local readiness verification; 2026-10-16, Task 12 Phase 1 closure).

> **Approved UI/UX direction: HISAAB** (Task 9 Concept A, the honest ledger), with four approved borrowings: the step-by-step mobile input and the answer strip from Concept B, and the trade-off sentence and the shared-scale transport visualization from Concept C (ADR-0043). Design source of record: role file 02, `docs/02_AgriNex_UIUX_Phase1_Specification.md` (AUTHORITATIVE). Approval record: area 5 below.

- The control layer was set up on 2026-10-02.
- On 2026-10-03 the architecture was formalized (Task 6), the database architecture specified (Task 7), and the remaining decisions recorded in the decision log as ADR-0001 to ADR-0034, under the project owner's delegation (ADR-0001).
- On 2026-10-04 the API contract was written (Task 8): `contracts/` (OpenAPI 3.1 and JSON Schemas), `docs/api/` and the synthetic mocks in `frontend/__mocks__/api/`. Six new items were registered: RD-038 to RD-040, with PROPOSED entries ADR-0035 to ADR-0037, and RD-041 to RD-043 from the Task 8 reviews.
- Also on 2026-10-04 (Task 8.5):
  - a human decision packet and a security review were prepared (REFERENCE; see "Decision and security review documents" below);
  - `ML_ARCHITECTURE.md` §4.8–§4.9 and one deployment-diagram label were aligned with ADR-0030;
  - RD-044 was registered.

  No decision was approved.
- Also on 2026-10-04 (Task 8.6):
  - the Human Project Owner approved ADR-0035 to ADR-0041, resolving RD-038 to RD-044, and signed off the security review (ADR-0042);
  - the contract, the architecture, database and security documents, and the mocks were updated to match;
  - the two stale governance texts were corrected (market renames in `DATA_DICTIONARY.md`, CLAUDE.md §M);
  - the nine project skills were populated (`.claude/skills/agrinex-*/SKILL.md`).
- Also on 2026-10-04 (Task 9, UI/UX design exploration):
  - role file 02 was created as a draft (PENDING APPROVAL; visual direction not selected);
  - three visual directions were explored as Stitch and Figma working drafts on synthetic data, and a design direction packet was prepared (REFERENCE; see "Decision and security review documents" below);
  - RD-045 to RD-047 were registered.

  No design, screen or decision was approved in that step.
- Also on 2026-10-04 (Task 9 final decision lock; record in `docs/decision-review/TASK_9_FINAL_DECISION_LOCK.md`):
  - the Human Project Owner decided H1 to H6: the Hisaab direction with four borrowings and the screen approval (ADR-0043), the design decisions PDD-01 to PDD-14 (ADR-0044), Claude as owner of the Marathi and Hindi copy (ADR-0046), and email at registration, login by email or phone, and Phase 1 password recovery (ADR-0047);
  - under two delegations recorded in ADR-0043, the recommendation wording (ADR-0045) and the email-verification and password-reset mechanism with Resend delivery (ADR-0048) were decided;
  - RD-045 to RD-047 were resolved; role file 02 became AUTHORITATIVE;
  - contract 1.0.0 was revised before its first merge (four auth operations, two error codes, email fields) and its mocks updated; the database, architecture, security and API documents were updated to match;
  - a security review of the recovery mechanism was recorded (`docs/security/TASK_9_FINAL_ACCOUNT_RECOVERY_SECURITY_REVIEW.md`).
- Since Task 10 the repository also contains the backend (`backend/`, with Flyway migrations V1–V9) and the optional ML track (`ml/`). Since Task 11 (2026-10-06) it contains the Phase 1 frontend (`frontend/`; how to run it: `frontend/README.md`). Code is not an authoritative document: the documents listed below remain the source of truth. The frontend tooling entry ADR-0049 was PROPOSED until the Task 12 closure, which decided it.
- Task 12 (2026-10-13 to 2026-10-16) loaded real Maharashtra data, added migrations V10–V12, completed full QA and wrote the local guide (`docs/LOCAL_DEVELOPMENT_GUIDE.md`). In its closure:
  - the Human Project Owner delegated the twelve open decisions to Claude (ADR-0050);
  - they were decided as ADR-0051 and ADR-0052;
  - RD-048, RD-049 and RD-050 were resolved;
  - the security review was signed off as an AI review.

  The state of Phase 1 is recorded in `docs/decision-review/TASK_12_PHASE1_CLOSURE_REPORT.md`: pre-deployment work complete; not deployed and not frozen.
- 2026-10-18, Phase 1 local readiness verification. The current state is in `docs/decision-review/PHASE1_LOCAL_READINESS_REPORT.md`: READY WITH EXPLICIT LIMITATIONS for local use; not deployed; not frozen.
  - Added the Experiment 1 query `scripts/run_evaluation.sql` (tested) and the read-only measurement `scripts/data_readiness.sql`.
  - Executed ADR-0033 item 3: the root `.gitignore` excludes `.mcp.json`.
  - Proposed ADR-0053 (RD-051) for an offline ML evaluation; the owner's approval is pending.
- 2026-10-18, deployment preparation. The owner approved ADR-0054 (historical maintenance: report-date ledger, daily gap check, weekly reconciliation) and ADR-0055 (Oracle Cloud Always Free VM, `agrinex.managio.me`, curated production data; resolves RD-037).
  - Added migration V13, the admin operation `getHistoricalCoverage` (contract 1.1.0), the `prod` profile with its start-up guard, the images, the root and dev compose files, the Nginx edge and the operator scripts.
  - The runbook is `docs/operations/DEPLOYMENT_GUIDE.md`; its section 12 records what has been deployed and verified.
  - Pre-deployment security fixes: backend profiles `local`, `prod` and `test` (originally `application-local.yml` and `application-prod.yml`; since 2026-10-28 profile-activated documents of the single `application.yml`) with startup checks before the context; the ADMIN role enforced in the security filter chain for every admin operation; the edge keeps page-level Referrer-Policy.
  - Wrote the test report (`docs/qa/PHASE1_LOCAL_TEST_REPORT.md`) and the data and ML readiness report (`docs/data/PHASE1_DATA_AND_ML_READINESS_REPORT.md`).

## How to use this index

1. Find the area you are about to change.
2. Read the authoritative documents listed, including the cited sections in full.
3. Work through the "Before modifying" checklist.
4. If a needed document is NOT CREATED YET, the only authorities are the listed master-specification sections and the binding entries (APPROVED or DECIDED) in `docs/architecture/DECISION_LOG.md`.
   - Anything those sources do not settle is REQUIRES_DECISION.
   - Never fill the gap by invention, and never cite a document that does not exist.

| Status | Meaning |
|---|---|
| AUTHORITATIVE | Exists in the repository and governs its area |
| NOT CREATED YET | Named by the master specification or needed by governance, but does not exist yet, so it cannot be cited |
| PENDING APPROVAL | Exists as a recommendation, but is not authoritative until a human approves it and the approval is recorded in the decision log |
| NON-AUTHORITATIVE | Exists, but is reference or tooling material only |
| REFERENCE | Exists and must conform to the authoritative documents of its area (for example synthetic mocks), but is never a source of requirements |
| GENERATED | Produced by a tool from code or from another document (for example an OpenAPI document generated by the backend for drift detection). Never authoritative, and never written over an authoritative file |

Section references (§) are to the master specification, `docs/01_AGRINEX_MASTER_PRODUCT_PHASES.md`. Planned file names are quoted exactly as the specification gives them.

## At a glance

| # | Area | Authoritative now | Planned dedicated documents |
|---|---|---|---|
| 1 | Product | Master specification | — |
| 2 | Architecture | Master specification, decision log (binding ADR-0001 to ADR-0048), 13 architecture documents in `docs/architecture/` (index: `ARCHITECTURE_INDEX.md`) | — |
| 3 | Database / data | Master specification sections; decision log; 7 database architecture documents in `docs/database/` (index: `DATABASE_INDEX.md`); verified government-data evidence and the official exports in `docs/data/raw/` (as evidence only) | Role file 04, migrations, fixtures: NOT CREATED YET. The data contract `contracts/price-observation.schema.json` exists (area 4) |
| 4 | API contracts | `contracts/openapi.yaml` and the five JSON Schemas in `contracts/` (contract 1.0.0, Task 8, revised before its first merge in Task 8.6 and the Task 9 final lock); the contract documentation in `docs/api/` (index: `API_INDEX.md`); master specification sections; decision log (ADR-0022, ADR-0023 and the entries cited in `docs/api/`) | Role file 04 (API part): NOT CREATED YET |
| 5 | UX / design | Master specification sections; decision log (design source of record ADR-0027, browser floor ADR-0004, approved direction ADR-0043); **role file 02** (APPROVED 2026-10-04: **Hisaab** with B and C borrowings); the approved screen designs recorded in area 5 | Role file 03: NOT CREATED YET |
| 6 | QA / testing | Master specification sections | Test fixtures, evaluation script: NOT CREATED YET. The spec names no QA plan |
| 7 | Operations / deployment | Master specification sections; decision log (ADR-0017, ADR-0018, ADR-0020, ADR-0021, ADR-0054, ADR-0055); `DEPLOYMENT_ARCHITECTURE.md` | Images, compose files, Nginx edge and scripts: CREATED 2026-10-18; runbook `docs/operations/DEPLOYMENT_GUIDE.md` |
| 8 | Current technology baseline | `docs/architecture/CURRENT_TECHNOLOGY_BASELINE.md` (approved, ADR-0002) | — |

---

## 1. PRODUCT

**Governs:**

- product requirements: features F1.x/F2.x, functional requirements FR-xx, non-functional requirements;
- scope and out-of-scope;
- phases and phase gates;
- business rules and personas;
- acceptance criteria and the Definition of Done;
- claims discipline.

**Authoritative document:** `docs/01_AGRINEX_MASTER_PRODUCT_PHASES.md`, the whole document. Frequently needed sections:

- §5 principles;
- §7 Phase 1 features and §8 Phase 2 features;
- §10 phase gate and freeze checklist;
- §26 functional requirements and §27 non-functional requirements;
- §39 out of scope;
- §48 acceptance criteria;
- §49 claims discipline.

**Status:** AUTHORITATIVE. Version 2.0, September 2026. The Document Owner is the Team Lead / Product Manager (spec header). The file is protected (CLAUDE.md §E).

> **Path note:** spec §43 lists this file as `docs/01_AgriNex_Master_Product_Phases.md`. The repository path above is the authoritative one. Git and Linux treat paths as case-sensitive, so always use the exact path above, even though Windows will also resolve the mixed-case form.

**Before modifying this area:**

- Agents do not change product requirements. A product change needs an explicitly authorized update of the master specification, done as a separate task.
- Check `docs/REQUIRES_DECISION.md` for open items affecting the requirement.
- Check the out-of-scope list (§39) and HARD RULES 1–2 (§10) before starting feature work.

---

## 2. ARCHITECTURE

**Governs:** system context, containers, backend module boundaries, and external integrations. It also governs cross-cutting technical decisions: security architecture, resilience, and infrastructure architecture.

**Authoritative documents:**

- Master specification:
  - §11 system context, §12 containers, §13 components and modules;
  - §14–§17 data, ingestion, recommendation, and simulator flows;
  - §27 non-functional requirements, including module boundaries;
  - §28 security and §29 resilience;
  - §30 API module interaction;
  - §38 future scope (outside the current scope) and §39 out of scope.
- `docs/architecture/DECISION_LOG.md`: binding entries (APPROVED or DECIDED); ADR-0001 to ADR-0034.
- The 13 architecture documents below.

**Status:**

- Master specification: AUTHORITATIVE.
- Decision log: AUTHORITATIVE. Created 2026-10-02. Holds ADR-0001 to ADR-0034, recorded on 2026-10-03, ADR-0035 to ADR-0042, approved by the Human Project Owner on 2026-10-04, and the departures table (D-01 to D-14).
- Architecture documents: AUTHORITATIVE. They were created on 2026-10-03 by the architecture formalization (Task 6), approved in the decision lock the same day (`ARCHITECTURE_INDEX.md` §14), and are listed below.

**Architecture documents (formalization of the master specification):**

The documents formalize the spec's architecture. Statements traced to the spec carry the spec's authority. The documents' own interpretations were approved in the decision lock (ADR-0001). They do not change the authority of the master specification, and where a document and a binding decision-log entry differ, the entry governs.

| Document | Purpose | Main spec sections | Relationship | Status |
|---|---|---|---|---|
| `docs/architecture/ARCHITECTURE_INDEX.md` | Hub: hierarchy, document map, decisions, assumptions, conflicts, prerequisites, approval checklist, reconciliation, audit | All architecture sections | Entry point for the set | AUTHORITATIVE |
| `docs/architecture/SYSTEM_CONTEXT.md` | C4 level 1: people, system, external systems | §1, §6, §11, §24, §31, §39 | Root of the C4 hierarchy | AUTHORITATIVE |
| `docs/architecture/CONTAINER_ARCHITECTURE.md` | C4 level 2: the six containers | §12, §45 | Decomposes SYSTEM_CONTEXT | AUTHORITATIVE |
| `docs/architecture/COMPONENT_ARCHITECTURE.md` | C4 level 3: modules, cross-cutting elements, frontend and ML parts | §12, §13, §27, §28, §30, §43 | Decomposes CONTAINER | AUTHORITATIVE |
| `docs/architecture/DATA_FLOW.md` | The 17 critical flows, with failure and fallback behavior | §14–§17, §21–§25, §33–§36 | Built on COMPONENT | AUTHORITATIVE |
| `docs/architecture/INGESTION_ARCHITECTURE.md` | Government-data ingestion | §1, §14, §15, §22, §31, §37 | Details DATA_FLOW F-02 to F-04 | AUTHORITATIVE |
| `docs/architecture/RECOMMENDATION_ENGINE.md` | Deterministic engine and What-If | §5, §16, §17, §21, §32, §33 | Details DATA_FLOW F-05 to F-08, F-12 | AUTHORITATIVE |
| `docs/architecture/SECURITY_ARCHITECTURE.md` | Trust boundaries, authentication, RBAC, secrets, edge | §27, §28, §29 | Cross-cutting | AUTHORITATIVE |
| `docs/architecture/RESILIENCE_ARCHITECTURE.md` | Failure boundaries and degraded behavior | §27, §29, §35, §37 | Cross-cutting | AUTHORITATIVE |
| `docs/architecture/DEPLOYMENT_ARCHITECTURE.md` | Services, environments, configuration, TLS, health | §7, §10, §28, §43, §45 | Cross-cutting. Versions come from area 8 | AUTHORITATIVE |
| `docs/architecture/FRONTEND_ARCHITECTURE.md` | Frontend boundary, interaction, required states | §12, §13, §25, §27, §30, §43, §44, §48 | Refines COMPONENT for the frontend | AUTHORITATIVE |
| `docs/architecture/ML_ARCHITECTURE.md` | The optional forecasting boundary | §12, §14, §35, §36, §41, §43, §45 | Optional track | AUTHORITATIVE |
| `docs/architecture/TESTING_ARCHITECTURE.md` | Test levels, acceptance mapping, test data, environments | §10, §27, §36, §40, §48 | Verifies all of the above | AUTHORITATIVE |

**Before modifying this area:**

- Read the relevant specification sections, every binding decision-log entry for the area, and the area's architecture document.
- Every architecture area is approved. The remaining prerequisites are human actions, listed in `ARCHITECTURE_INDEX.md` §13.
- Decide whether the change is a new architectural decision (CLAUDE.md §G). If it is, draft a PROPOSED log entry and get explicit human approval before implementation. The delegation in ADR-0001 covered only the 2026-10-03 decision lock.
- Check `docs/REQUIRES_DECISION.md` for open items in the area: RD-002, RD-012, RD-025 and RD-037 remain open. The Task 8 items are resolved: read-only ports (ADR-0035), operator procedures (ADR-0037, ADR-0041) and the recommendation → geography dependency (ADR-0039).

---

## 3. DATABASE / DATA

**Governs:**

- entities, schema, constraints, and migrations;
- data ownership by module;
- ingestion, normalization, and deduplication rules;
- reference and seed data;
- the government data-source policy.

**Authoritative now (master specification):**

- §1 verified data sources and resource IDs;
- §14–§15 data flow and ingestion pipeline;
- §20 ER diagram, a domain overview rather than a complete schema;
- §22 ingestion sequence;
- §27 data-integrity requirement;
- §31 data source policy (RULES 1–7);
- §32 transport parameters;
- §33 trend computation;
- §34 alert engine.

**Evidence of observed government data:**

| Document | Governs | Status |
|---|---|---|
| `docs/data/AGRINEX_VERIFIED_GOVERNMENT_API_DATA_REFERENCE.md` | Observed response shapes and captured values of the two data.gov.in resources (seven unique captures, Appendix A) | AUTHORITATIVE as evidence of observed payloads only, as designated by the project owner in the Task 7 instruction (2026-10-03). It is not a source of requirements; its implementation suggestions (for example `bigint` keys or a `PARTIAL` run status) are non-binding. Anything it does not demonstrate is UNVERIFIED |
| `docs/data/raw/Commodity-wise_Market-wise_Daily_Weighted_Avg_Report_*.csv` (5 files), `docs/data/raw/Market_Wise_Price_Arrival_*.csv` | Official Agmarknet 2.0 (DMI) report exports for 01-Oct-2026, supplied by the project owner: units, commodity names, grades, market spellings | AUTHORITATIVE as official corroborating evidence only (ADR-0005). Never an ingestion source. Profiled in `docs/database/DATABASE_ARCHITECTURE.md` §10.9 |
| `docs/data/raw/Agriculture_price_dataset.csv` | Third-party price dataset of unknown provenance (737,392 rows, 2023-06-06 to 2025-06-11), supplied by the project owner | NON-AUTHORITATIVE. Validation evidence only, used offline to test validation rules. Never loaded, shown, or used for ML (ADR-0005) |

**Database architecture documents (AUTHORITATIVE):**

- Created on 2026-10-03 by the database architecture specification (Task 7), and updated and approved in the decision lock the same day (checklist at the end of `docs/database/DATABASE_INDEX.md`; ADR-0001, ADR-0013).
- Statements traced to the spec carry the spec's authority, and binding decision-log entries govern where they differ.
- No migration or database exists.

| Document | Purpose | Status |
|---|---|---|
| `docs/database/DATABASE_INDEX.md` | Hub: document and table maps, ownership, dependencies, evidence, prerequisites, decisions, conflicts, audit, approval checklist | AUTHORITATIVE |
| `docs/database/DATABASE_ARCHITECTURE.md` | Scope, phase boundaries, domains, reference-data and observation strategy, ingestion, recommendation, alert, auth and ML persistence, integrity, indexing, lifecycle, decisions; CSV evidence profile (§10.9) | AUTHORITATIVE |
| `docs/database/ERD.md` | Mermaid ER diagrams, relationships, cardinalities, keys | AUTHORITATIVE |
| `docs/database/DATA_DICTIONARY.md` | Every table and column with basis, source and sensitivity (28 tables) | AUTHORITATIVE |
| `docs/database/TABLE_OWNERSHIP.md` | Owner module of every table and the allowed access paths (resolution of RD-017, ADR-0013) | AUTHORITATIVE |
| `docs/database/INDEXES_AND_CONSTRAINTS.md` | Keys, unique and CHECK constraints, indexes, query patterns | AUTHORITATIVE |
| `docs/database/SEED_AND_REFERENCE_DATA.md` | Maharashtra reference data, the official commodity master and units, initial configuration | AUTHORITATIVE |

**Planned:**

| Planned item (as named in the specification) | Spec reference | Status |
|---|---|---|
| `docs/04_AgriNex_Backend_Phase1_API_Database.md` (role file 04) | §43 | NOT CREATED YET |
| Flyway migrations in `backend/src/main/resources/db/migration/` ("V1–V6 (Phase 1 schema)", §42) | §12, §43 | NOT CREATED YET |

The data contract `contracts/price-observation.schema.json` (Data Engineering produces; Backend, ML, QA consume; §41) exists since Task 8 and is listed in area 4.
| `fixtures/seed_data.sql`, `fixtures/demo_price_observations.sql`, `fixtures/ml_observations.csv` | §43, §44 | NOT CREATED YET |

**Related architecture documents (AUTHORITATIVE):**

- `docs/architecture/DATA_FLOW.md`;
- `docs/architecture/INGESTION_ARCHITECTURE.md`;
- `docs/architecture/COMPONENT_ARCHITECTURE.md`, which lists the tables each module owns (ADR-0013; detail in `docs/database/TABLE_OWNERSHIP.md`).

**Before modifying this area:**

- Read §20 and the relevant sections above, the database architecture documents, and the evidence file. Once migrations exist, read them too.
- Apply CLAUDE.md §I. Do not invent entities, columns, or constraints.
- The database design is approved, so migrations may be written from it (`docs/database/DATABASE_INDEX.md` §7 lists the human prerequisites for seeding). Each migration states its owning module and its tests.
- The database decisions are recorded in the decision log (map: `DATABASE_INDEX.md` §8). The main ones are:
  - ADR-0006 to ADR-0009 and ADR-0011 to ADR-0012: ingestion;
  - ADR-0010: observation selection;
  - ADR-0013: ownership;
  - ADR-0014: geography and markets;
  - ADR-0015: localized names;
  - ADR-0016: accounts;
  - ADR-0024: weights seed;
  - ADR-0028: demo data;
  - ADR-0029: retention.

---

## 4. API CONTRACTS

**Governs:**

- endpoint paths and HTTP methods;
- request and response schemas;
- validation rules;
- status and error semantics;
- authentication and authorization per endpoint;
- API mocks.

**Authoritative documents (contract 1.0.0, created by Task 8 on 2026-10-04):**

| Document | Content | Producer → consumers (§41) | Status |
|---|---|---|---|
| `contracts/openapi.yaml` | Hand-authored OpenAPI 3.1 contract: 53 Phase 1 operations under `/api/v1` | Backend → Frontend, QA | AUTHORITATIVE (ADR-0022; departure D-11) |
| `contracts/common.schema.json` | Shared primitives and reference objects | Backend → all | AUTHORITATIVE |
| `contracts/price-observation.schema.json` | Price observation with provenance and freshness, source sync status, ML input series | Data Engineering → Backend, ML, QA | AUTHORITATIVE |
| `contracts/recommendation.schema.json` | Comparison and simulation requests and `MarketComparisonResponse` | Backend → Frontend, QA | AUTHORITATIVE |
| `contracts/error-response.schema.json` | `ApiErrorResponse` (RFC 9457) and the error-code catalogue | Backend → Frontend, QA | AUTHORITATIVE |
| `contracts/forecast-output.schema.json` | Optional ML output written to `forecasts` | ML → Backend | AUTHORITATIVE (optional ML track) |
| `docs/api/API_INDEX.md` | Hub: files, endpoint inventory, ownership, scope, open items | — | AUTHORITATIVE (explains the contract files, which govern where they differ) |
| `docs/api/API_CONVENTIONS.md` | Versioning, formats, precision, pagination, errors, security, headers | — | AUTHORITATIVE (as above) |
| `docs/api/PROVENANCE_AND_FRESHNESS.md` | Provenance, units, freshness, outage and no-data semantics | — | AUTHORITATIVE (as above) |
| `docs/api/ML_CONTRACT_BOUNDARY.md` | ML boundary and deterministic fallback | — | AUTHORITATIVE (as above) |
| `docs/api/FRONTEND_CONSUMPTION.md` | Screen-to-operation states, client What-If, auth flow, mock mode | — | AUTHORITATIVE (as above) |
| `docs/api/CONTRACT_VALIDATION.md` | Validation method and results; contract-test expectations | — | AUTHORITATIVE (as above) |

**Reference and generated material:**

| Item | Status |
|---|---|
| `frontend/__mocks__/api/*.json` (83 synthetic mocks, index `frontend/__mocks__/api/README.md`) | REFERENCE: synthetic data that must match the contract exactly (§41, §44). Never shown as real (P10). Served in the browser by MSW in the frontend's mock mode; validated against the contract by `frontend/lib/api/contract.test.ts` |
| An OpenAPI document generated from backend code | GENERATED: drift detection only, never written over `contracts/openapi.yaml` (ADR-0022). None exists yet |

**Also authoritative for this area (master specification):** §30 API path groups, owning modules and response types; §41 contract matrix and Shared Contract Change Rule; §28 role capabilities and auth controls; §29 failure behavior; the flows in §16, §17, §21 and §23.

**Decision-log entries that govern the contract:** ADR-0022 (conventions), ADR-0023 (comparison), ADR-0016 and ADR-0017 (authentication, tokens, edge), ADR-0019 (alerts), ADR-0020 (admin boundary), ADR-0010, ADR-0024, ADR-0025 and ADR-0026 (calculation rules), ADR-0030 (optional forecast), ADR-0047 and ADR-0048 (account email, login identifiers, email verification and password recovery). `docs/api/API_INDEX.md` §3 traces every operation.

**Planned:**

| Planned item (as named in the specification) | Spec reference | Status |
|---|---|---|
| `docs/04_AgriNex_Backend_Phase1_API_Database.md` (role file 04, API part) | §43 | NOT CREATED YET. It must stay consistent with `contracts/` |

**Related documents:**

- `docs/architecture/COMPONENT_ARCHITECTURE.md` (API groups per module);
- `docs/architecture/SECURITY_ARCHITECTURE.md` (authentication and roles);
- `docs/architecture/FRONTEND_ARCHITECTURE.md`;
- `docs/database/` (entities, vocabularies and constraints behind the payloads).

**Before modifying this area:**

- Work contract-first (spec P5). Frontend and backend use the same contract (CLAUDE.md §H).
- Follow the Shared Contract Change Rule (§41): increment the version in each changed contract file, update the mocks in the same change, and get approval from at least two consuming roles. Compatibility rules: `docs/api/API_CONVENTIONS.md` §2.
- `contracts/openapi.yaml` is hand-authored and authoritative. Generated documents only detect drift (ADR-0022, departure D-11).
- No open item blocks the contract. RD-038 to RD-044 are resolved (ADR-0035 to ADR-0042), and the security-sensitive contract is signed off (ADR-0042). Phase 2 endpoints wait for RD-012 and RD-025 (ADR-0032).

---

## 5. UX / DESIGN

This area covers screen design and the frontend implementation specification.

**Governs:** screens and their states, design system and tokens, interaction flows, copy and labelling, localization, accessibility, and responsive behavior.

**Authoritative now (master specification):**

- §6 personas;
- §7 features: for example F1.2 language, F1.16 freshness indicators, F1.17 source attribution, F1.21 mobile-first, F1.22 accessibility;
- §25 farmer decision journey;
- §27 performance, accessibility, and localization targets;
- §31 RULE 4 and RULE 7;
- §32 mandatory UI labels;
- §33 trend presentation;
- §48 language and responsiveness criteria;
- §49 claims, labels, and the standard disclaimer.

**Approved UI/UX direction (ADR-0043):** **HISAAB** (Task 9 Concept A, the honest ledger) is the approved base direction, with four approved borrowings rendered in Hisaab's visual language:

1. from Concept B, the step-by-step, one-question-at-a-time comparison input on mobile;
2. from Concept B, the answer strip at the top of the results;
3. from Concept C, the trade-off sentence in the winner callout (wording ADR-0045);
4. from Concept C, the shared-scale transport visualization.

Concepts B and C remain exploration reference (`docs/decision-review/TASK_9_DESIGN_DIRECTION_PACKET.md`), not an approved direction.

**Authoritative documents:**

| Document | Spec reference | Status |
|---|---|---|
| `docs/02_AgriNex_UIUX_Phase1_Specification.md` (role file 02; owner: UI/UX) | §43, §44 | AUTHORITATIVE. Created 2026-10-04 (Task 9); approved 2026-10-04 by the Human Project Owner (ADR-0043, ADR-0044). Design source of record (ADR-0027): screens S01–S16 and A01–A05, states E01–E31, copy (EN, MR, HI; ADR-0046), tokens, components, design decisions PDD-01 to PDD-14 |
| `docs/03_AgriNex_Frontend_Phase1_Implementation.md` (role file 03) | §43 | NOT CREATED YET |

**Approval record (ADR-0027 item 2).** Approver: the Human Project Owner, 2026-10-04 (ADR-0043), recorded at the owner's instruction. Design references are Stitch screen IDs; project A `16473179131950483450` unless stated. Each draft is approved for layout and visual language only; role file 02 governs content, and tool-invented content listed in the packet §6.1 is excluded.

| Screen | Design reference | Approval |
|---|---|---|
| Role file 02 as a whole, with the Hisaab tokens (§14.2 column A) | `docs/02_AgriNex_UIUX_Phase1_Specification.md` | APPROVED |
| S01/S02 first launch and home (mobile) | `73a8d7641a654d0687f9304e4d2ba972` | APPROVED |
| S02 landing (desktop) | `e942607371fe4455ba2e52b32ef26218` | APPROVED |
| S03 input, mobile steps | Flow from Concept B (project `6060147904238362683`: `e375344003e24a8e9e3657e05d8df95a`, `29028be0d7b647318f2608b31e2e3f23`, `4095f70c979449ca9024935ae9c72c83`, `691450c3f42b46698434a84f26ae2505`); visual language from the A form `1434e413f6b74e37908ab65fe5fabdba` | APPROVED (role file 02 §1.1) |
| S03 + S04 comparison sheet (desktop) | `08275aed05844078a623ac91e62b2bcd` | APPROVED |
| S04 results (mobile) | `5ce101cb69ac4a14bbd0c6515e9dc9fb`; answer-strip pattern B `94962ca061fb44868a5da3b24f2c2ec6`; trade-off and transport-scale pattern C (project `7462455097525525857`) `1d477ed7e7474e1ba3b6686aaf69c813` | APPROVED (patterns in Hisaab style; wording ADR-0045) |
| S05 why recommended | `1654e3dc58054495b04ae01e5f0c0000` | APPROVED |
| S07 trend | `5f93505d36694dcd95885c380ee0a019` | APPROVED |
| S09 What-If (mobile) | `fa95cea48dfe400a851dafd3bf8ede76` | APPROVED (labels ADR-0045 item 6) |
| S10 registration | `44544e48277d4165ba8574716011e0a9` | APPROVED for layout only (content: role file 02 §8 S10) |
| S11 price alerts | `89779b622d8849988b914f3975263895` | APPROVED |
| E02 outage and stale | `aa19f90c43324750a291f345297b175f` | APPROVED |
| E05 no market in range | `42fc34d22b3443e3b1b359e6d32382fd` | APPROVED |
| A01 admin data health (desktop) | `3217f9a370184c48b4957721a6419962` | APPROVED for layout only |
| States E01–E31 | Figma file `YGFBNHyWCwyFaszM4205Q6`, board 09 (Hisaab tokens), and role file 02 §9 | APPROVED |
| S06, S08, S12, S14, S15, S16, A02–A05, desktop S07 and S09, the Marathi results | Role file 02 only (no rendered draft) | APPROVED as specified |

The Task 9 Stitch projects and the Figma file are working drafts, not authoritative documents; the approvals above refer to specific screens in them.

**Related architecture documents (AUTHORITATIVE):**

- `docs/architecture/FRONTEND_ARCHITECTURE.md`;
- `docs/architecture/RESILIENCE_ARCHITECTURE.md` (user-visible states and messages).

**Before modifying this area:**

- Confirm that an approved design source exists for the screen (CLAUDE.md §M). The design source of record is role file 02, with approvals recorded in this area (ADR-0027, ADR-0043). Do not change the approved direction without a new decision-log entry approved by a human (ADR-0043 item 5).
- The browser floor is Chrome/Edge 111+, Safari 16.4+, Firefox 128+ (ADR-0004).
- Check localization (Marathi, Hindi, English), the 375px viewport, the WCAG 2.1 AA target for core flows, and the mandatory labels and disclaimers.
- The transport label shows the applied rate (ADR-0038, departure D-14). The error states for a non-centroid origin (ADR-0039) and an estimate out of range (ADR-0040) need copy. Erasure, deactivation and reactivation have no screens, because they are operator procedures (ADR-0037, ADR-0041). Recommendation labels follow ADR-0045, and Marathi and Hindi copy ADR-0046 (owner: Claude). The project skill for this area is `.claude/skills/agrinex-design/SKILL.md`.

---

## 6. QA / TESTING

**Governs:** test strategy and levels, acceptance verification, test data, evaluation experiments, and the Definition of Done.

**Authoritative now (master specification):**

- §10 Phase 1 freeze checklist;
- §27 testability requirement;
- §36 evaluation and research methodology (Experiments 1–2 are required);
- §40 RACI for unit, E2E, and contract tests;
- §48 acceptance criteria and Definition of Done.

**Planned:**

| Planned item | Spec reference | Status |
|---|---|---|
| `fixtures/test_accounts.sql` (one account per role) | §43, §44 | NOT CREATED YET. Demo-profile environments only (ADR-0028) |
| `scripts/run_evaluation.sql` (Experiment 1 query) | §43 | CREATED 2026-10-18; tested by `EvaluationScriptIntegrationTest` (hand-computed). Figures need real use after deployment (ADR-0052 item 9) |
| Dedicated QA / test plan document | No file named in the specification | NOT CREATED YET. Results of the Phase 1 local verification: `docs/qa/PHASE1_LOCAL_TEST_REPORT.md` (REFERENCE, 2026-10-18) |

**Related architecture document (AUTHORITATIVE):** `docs/architecture/TESTING_ARCHITECTURE.md`.

**Before modifying this area:**

- Identify the §48 criteria and §10 checklist items the change affects.
- Name the tests before implementing (CLAUDE.md §L).
- Base contract tests on `contracts/` (ADR-0022 item 1); the expectations are in `docs/api/CONTRACT_VALIDATION.md`.
- Never weaken tests to make them pass.

---

## 7. OPERATIONS / DEPLOYMENT

**Governs:**

- services and containers;
- environments and configuration;
- runtime secret handling;
- reverse proxy and TLS;
- health checks and deployment;
- CI/CD and branch strategy.

**Authoritative now (master specification):**

- §45 deployment overview: services, ports, one-command start, health checks;
- §10 deployment freeze checklist;
- §12 container architecture;
- §27 portability and observability;
- §28 TLS, secure headers, and secret handling;
- §29 failure handling;
- §42 development order and branch strategy (`main`, `develop`, `feature/*`).

**Planned:**

| Planned item (as named in the specification) | Spec reference | Status |
|---|---|---|
| `infra/docker-compose.yml` (backing services only: PostgreSQL and Redis, ADR-0021) | §43 | CREATED (Task 10) |
| `infra/docker-compose.dev.yml`; the Nginx edge `infra/nginx/templates/default.conf.template` (the spec's `infra/nginx.conf`, as a template of the official image) and its local variant `infra/nginx/dev/` | §43 | CREATED 2026-10-18 (verified on a local rehearsal) |
| `infra/.env.example` (the environment contract file; owner: DevOps) | §41, §43, §45 | CREATED (Task 10) |
| Root `docker-compose.yml` ("Production compose file"), `backend/Dockerfile`, `frontend/Dockerfile`, and the root `.env.example` (a pointer to `infra/.env.example`) | §43 | CREATED 2026-10-18 (root `.env.example` earlier) |
| `scripts/health_check.sh`, `scripts/backup_db.sh`, `infra/deploy/setup_host.sh`, `infra/deploy/certbot-deploy-hook.sh`, `infra/deploy/build_source_bundle.py` (allow-listed source bundle with manifest), `infra/deploy/create_server_env.sh` and the production template `infra/deploy/production.env.example` | §43 (health check); ADR-0020 item 4, ADR-0021 (backups, TLS, secrets) | CREATED 2026-10-18 |
| `scripts/bootstrap_historical.sh` | §43 | NOT CREATED: the daily gap check of ADR-0054 fills missing report dates within 366 days, and the admin trigger covers any other range (ADR-0011 item 3) |
| CI/CD pipeline configuration | §40 (DevOps responsible); no file named | NOT CREATED YET |
| Dedicated operations / runbook document | None named | Local: `docs/LOCAL_DEVELOPMENT_GUIDE.md` (Task 12, 2026-10-15). Deployment: `docs/operations/DEPLOYMENT_GUIDE.md` (REFERENCE, 2026-10-18) |

**Related architecture documents (AUTHORITATIVE):**

- `docs/architecture/DEPLOYMENT_ARCHITECTURE.md`;
- `docs/architecture/SECURITY_ARCHITECTURE.md`;
- `docs/architecture/RESILIENCE_ARCHITECTURE.md`.

**Before modifying this area:**

- The technology baseline is approved (area 8, ADR-0002).
- Changes touching secrets, TLS, CORS, headers, or rate limiting need security review (CLAUDE.md §K).
- Never put secrets in version control. Do not commit `.mcp.json` until RD-002's user actions are done (ADR-0033). No fixed credentials on a public deployment (ADR-0028).
- The decisions are recorded: ADR-0017 (routing, origin, CORS, rate limits), ADR-0018 (Redis), ADR-0020 (admin boundary and secrets) and ADR-0021 (topology). Hosting procurement is RD-037.

---

## 8. CURRENT TECHNOLOGY BASELINE

**Governs:** the approved, verified implementation versions of languages, frameworks, runtimes, databases, container images, and key libraries, and their compatibility with each other.

**Authoritative document:** `docs/architecture/CURRENT_TECHNOLOGY_BASELINE.md`.

- Verified 2026-10-02 and re-verified 2026-10-03.
- Approved by the project owner on 2026-10-03 (ADR-0002), with the build tool (Maven 3.10.x, ADR-0003) and the browser floor (ADR-0004).
- The departures from the specification's stated versions are listed in the decision log (D-01 to D-05).

**Reference only, not a current baseline:** the versions stated in the master specification (header "Stack" line, §12, §45 image tags). They identify the approved technologies, but their version numbers must be verified before use.

**Current technical documentation:** fetched through Context7 (`.claude/rules/context7.md`, `.claude/skills/context7-mcp/SKILL.md`). It is authoritative for library and framework behavior at a stated version, never for AgriNex requirements.

**Before modifying this area:**

- Scaffolding may pin the approved versions, using a newer patch within the same line when one exists.
- A new dependency or a version change outside the baseline needs a decision-log entry with current official evidence (CLAUDE.md §J, rule 15). Tools deferred to scaffolding are listed in ADR-0034.

---

## Governance documents

| Path | Purpose | Status |
|---|---|---|
| `CLAUDE.md` | Operating rules for all sessions, subagents, and contributors | AUTHORITATIVE (process) |
| `docs/README.md` | Documentation landing page | AUTHORITATIVE (navigation) |
| `README.md` | Repository overview: how to set up, configure, test, build and deploy the code, with diagrams of the implementation (2026-10-28) | REFERENCE (never a source of requirements; summarizes the code and the documents it links) |
| `docs/00_SSOT_INDEX.md` | This index | AUTHORITATIVE (navigation) |
| `docs/architecture/DECISION_LOG.md` | Binding decisions (ADRs) and departures from the spec | AUTHORITATIVE; binding ADR-0001 to ADR-0052 (ADR-0049 to ADR-0052 decided under the Task 12 delegation, ADR-0050) |
| `docs/REQUIRES_DECISION.md` | Register of open decisions | AUTHORITATIVE |

## Decision and security review documents

These prepare human decisions. They are never sources of requirements, and nothing in them is approved until the decision log records it.

| Path | Purpose | Status |
|---|---|---|
| `docs/decision-review/TASK_8_5_HUMAN_DECISION_PACKET.md` | Options, evidence and recommendations for RD-038 to RD-044 | REFERENCE (2026-10-04); historical: decided by ADR-0035 to ADR-0041 |
| `docs/security/TASK_8_5_SECURITY_REVIEW.md` | The CLAUDE.md rule 16 review of the auth contract and related items, with the sign-off table | REFERENCE (2026-10-04); signed off by the Human Project Owner (ADR-0042) |
| `docs/decision-review/SKILL_INPUTS_AFTER_TASK_8_5.md` | What each project skill must read and obey; input for the skill-population task | REFERENCE (2026-10-04); partly superseded by the skills |
| `docs/decision-review/SKILL_VALIDATION_REPORT.md` | Validation of the nine project skills against the SSOT, the decisions and the contract | REFERENCE (2026-10-04) |
| `docs/decision-review/TASK_9_DESIGN_DIRECTION_PACKET.md` | The three Task 9 design directions: comparison, recommendation, the decisions H1 to H6, and the index of Stitch and Figma working drafts | REFERENCE (2026-10-04); historical: decided by ADR-0043 to ADR-0048 (Hisaab approved; B and C are reference) |
| `docs/decision-review/TASK_9_FINAL_DECISION_LOCK.md` | The Task 9 final decision lock: H1 to H6 and their records, the H3 research, the PDD review, the H5 reconciliation, the UX consistency audit and the Task 10 readiness gate | REFERENCE (2026-10-04/05) |
| `docs/security/TASK_9_FINAL_ACCOUNT_RECOVERY_SECURITY_REVIEW.md` | Security analysis of the email-verification and password-recovery mechanism (findings AR-01 to AR-12) | REFERENCE (2026-10-04); mechanism decided under ADR-0048; implementation needs rule 16 review before merge |
| `docs/security/TASK_12_SECURITY_REVIEW.md` | Rule 16 review of the Task 12 changes: the CORS rejection format, optional-access session handling, secrets, live account email and database outage, with the sign-off table and the closure re-review (§7) | REFERENCE (2026-10-15); signed off 2026-10-15 as an AI review under ADR-0050; key rotation pending (owner) |
| `docs/decision-review/TASK_12_OWNER_DECISION_PACKET.md` | Decisions D1–D12 left by Task 12 (RD-050 aliases, unit evidence, sign-offs, ADR-0049, RD-048, copy, the 3G profile, load-test records, legacy markets, key rotation), with evidence, recommendations and the dispositions | REFERENCE (2026-10-15); decided 2026-10-15 under ADR-0050 (ADR-0051, ADR-0052) |
| `docs/decision-review/TASK_12_PHASE1_CLOSURE_REPORT.md` | Phase 1 state at the end of Task 12: freeze checklist and §48 status, the twelve decisions, results, limitations, owner actions, guide check | REFERENCE (2026-10-16) |
| `docs/decision-review/PHASE1_LOCAL_READINESS_REPORT.md` | Phase 1 status without deployment: features F1.1–F1.25 with evidence, remaining blockers, approvals needed, limitations | REFERENCE (2026-10-18) |
| `docs/qa/PHASE1_LOCAL_TEST_REPORT.md` | Results of the Phase 1 local verification: suites, journeys, sweep, accessibility, rechecks, probes, live ingestion | REFERENCE (2026-10-18) |
| `docs/data/PHASE1_DATA_AND_ML_READINESS_REPORT.md` | Data integrity, units, geography and taluka coverage, ingestion quality, ML coverage against spec §35, Experiment 1 instrumentation | REFERENCE (2026-10-18) |
| `docs/operations/DEPLOYMENT_GUIDE.md` | Runbook of the public deployment (ADR-0055): host, owner actions, first deployment, configuration, schedules, backup and recovery, maintenance, and the deployment record | REFERENCE (2026-10-18) |

## Non-authoritative material in the repository

| Path | What it is |
|---|---|
| `.claude/rules/context7.md`, `.claude/skills/context7-mcp/SKILL.md` | Instructions for fetching current technical documentation. Process guidance only |
| `.claude/agents/*.md` (8 files) | Subagent definitions. All 8 files were empty on 2026-10-02 |
| `.claude/skills/agrinex-*/SKILL.md` (9 skills) | Project skills populated on 2026-10-04 (Task 8.6): architecture, database, api-contract, backend, frontend, design, qa, cloud, release. Tooling that points to the authoritative documents; never a source of requirements (CLAUDE.md §N). Validation: `docs/decision-review/SKILL_VALIDATION_REPORT.md` |
| `.claude/settings.json`, `.claude/settings.local.json`, `.mcp.json` | Tool configuration, not documentation. For `.mcp.json`, see RD-002 |
| `.playwright-mcp/` | Browser-tool output: a smoke test against a public demo site (2026-10-02), and two page snapshots of `agmarknet.gov.in` saved during the decision-lock research (2026-10-03). Not evidence; the findings are recorded in the decision log |
| `.idea/`, `Agrinex.iml` | IDE project files |
