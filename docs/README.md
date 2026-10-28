# AgriNex Documentation

This is the landing page for the AgriNex documentation system. It explains how the documentation is organized and how to navigate it. It does not restate product requirements, which live in the master specification.

## Purpose

The documentation system lets every contributor, human or AI agent:

- find the authoritative source for any project area;
- tell verified requirements apart from assumptions;
- see which decisions are approved and which are still open;
- avoid inventing requirements, business rules, contracts, schema, or architecture;
- keep product, architecture, data, API, design, QA, and operations work aligned.

## Start here

| Order | Document | Use it for |
|---|---|---|
| 1 | [`../CLAUDE.md`](../CLAUDE.md) | Operating rules for every session, subagent, and contributor |
| 2 | [`00_SSOT_INDEX.md`](00_SSOT_INDEX.md) | Where the authoritative information for each area lives, and its status |
| 3 | [`01_AGRINEX_MASTER_PRODUCT_PHASES.md`](01_AGRINEX_MASTER_PRODUCT_PHASES.md) | The master specification, the product-level single source of truth |
| 4 | [`REQUIRES_DECISION.md`](REQUIRES_DECISION.md) | Open decisions (four remain), and resolved ones with pointers. Check before starting work in an area |
| 5 | [`architecture/DECISION_LOG.md`](architecture/DECISION_LOG.md) | Binding decisions (ADR-0001 to ADR-0048) and departures from the spec |

## Master specification

The master specification is `docs/01_AGRINEX_MASTER_PRODUCT_PHASES.md`, titled *AgriNex — Master Product & Phase Specification*. It is version 2.0 (September 2026), owned by the Team Lead / Product Manager.

- **Scope.** It is the product-level single source of truth. It covers requirements, scope, phases, business rules, the architecture and technology it states, and acceptance criteria.
- **Protected.** Contributors and agents do not edit it unless the user explicitly authorizes the change as a separate task (CLAUDE.md §E).
- **Not silently fixed.** Its ambiguities and internal inconsistencies are recorded in `REQUIRES_DECISION.md`, and resolved in the decision log, instead of being corrected in place. Each departure from its text is quoted in the decision log's departures table.

## Source-of-truth hierarchy

CLAUDE.md §B is the controlling statement. In summary, from highest precedence to lowest:

1. The master specification.
2. Binding entries in the decision log: APPROVED, or DECIDED under a recorded human delegation (ADR-0001).
3. Subordinate project documents, once created and listed as authoritative in the SSOT index:
   - role files 02–04;
   - shared contracts in `contracts/`;
   - database migrations;
   - the technology baseline;
   - the architecture documents in `architecture/`.
4. Current official technical documentation, for library and framework behavior only.

Everything else is non-authoritative. That includes chat history, memory, agent and skill definitions, and tool logs.

## Documentation categories

| Category | Governs | Current authority |
|---|---|---|
| Product | Requirements, scope, phases, business rules, acceptance criteria | Master specification |
| Architecture | System and module boundaries, integrations, cross-cutting decisions | Master specification, the decision log, and the architecture documents in `architecture/`, starting at [`architecture/ARCHITECTURE_INDEX.md`](architecture/ARCHITECTURE_INDEX.md) (AUTHORITATIVE) |
| Database / data | Schema, migrations, ingestion rules, reference data | Master specification sections, the decision log, and the database documents in `database/`, starting at [`database/DATABASE_INDEX.md`](database/DATABASE_INDEX.md) (AUTHORITATIVE). Migrations are NOT CREATED YET |
| API contracts | Endpoints, schemas, validation, status semantics, authentication | Master specification sections and the decision log (ADR-0022 conventions). `contracts/` is NOT CREATED YET (Task 8) |
| UX / design | Screens, states, design system, copy, localization, accessibility | Master specification sections and the decision log (ADR-0027). Role files 02 and 03 are NOT CREATED YET |
| QA / testing | Test strategy, acceptance verification, evaluation | Master specification sections and `architecture/TESTING_ARCHITECTURE.md` |
| Operations / deployment | Services, configuration, health checks, deployment | Master specification sections, the decision log and `architecture/DEPLOYMENT_ARCHITECTURE.md`. `infra/` is NOT CREATED YET |
| Technology baseline | Approved, verified implementation versions | `architecture/CURRENT_TECHNOLOGY_BASELINE.md` (approved, ADR-0002) |

[`00_SSOT_INDEX.md`](00_SSOT_INDEX.md) has the details: section references, planned file names, and checklists to work through before making changes.

## Decision log

[`architecture/DECISION_LOG.md`](architecture/DECISION_LOG.md) is the single home for binding decisions. It records:

- architecture decisions (CLAUDE.md rule 14: no change to approved architecture without an ADR);
- technology-baseline approvals, and any departure from a version the specification states;
- approved resolutions of `REQUIRES_DECISION.md` items.

Agents may draft PROPOSED entries, but only a human approves them. ADR-0001 records the one exception: the project owner's explicit delegation for the decision lock of 2026-10-03, under which ADR-0002 to ADR-0034 were recorded. That delegation does not carry over to later work.

## Unresolved decision register

[`REQUIRES_DECISION.md`](REQUIRES_DECISION.md) lists only genuine open decisions. These are:

- inconsistencies in authoritative sources;
- gaps that would force someone to invent a rule or contract;
- repository conditions that block safe progress;
- decisions that governance requires before work can start.

Each item says what it blocks. A BLOCKING item stops the named work until a human resolves it. After the 2026-10-03 decision lock, four items remain open: RD-002, RD-012, RD-025 and RD-037. None of them blocks Task 8.

## How agents should navigate

1. Read `CLAUDE.md`. Claude Code loads it automatically.
2. Use `00_SSOT_INDEX.md` to find the authoritative document for the area.
3. Read the cited master-specification sections in full, not just their summaries.
4. Check `REQUIRES_DECISION.md` for open items in the area, and `architecture/DECISION_LOG.md` for approved decisions.
5. Classify what you rely on as VERIFIED, ASSUMPTION, REQUIRES_DECISION, or CONFLICT (CLAUDE.md §D).
6. When you need library or framework specifics, fetch current documentation through Context7 (`.claude/rules/context7.md`).
7. Make the smallest valid change, verify it, and update the documentation it affects.

## Project documentation vs current technical documentation

The two kinds of documentation answer different questions:

- **Project documentation** (master specification, approved decisions, contracts, role files) defines *what* AgriNex must do and *which* technologies it uses.
- **Current technical documentation** (official documentation fetched through Context7) defines *how* a library, framework, or tool behaves at a specific version: API syntax, configuration, and migration steps.

Neither replaces the other:

- Technical documentation never adds AgriNex requirements.
- Project documentation never justifies assuming how a library API works.

The versions the master specification states are a historical baseline. Current documentation may point to a different version or reveal an incompatibility. If so, treat it as a technology-baseline decision (CLAUDE.md §J; the baseline is ADR-0002), not a silent upgrade. When a decision depends on technical documentation, record which library version's documentation was consulted.

## Documentation update discipline

- Do not edit the master specification without explicit authorization as a separate task.
- When you create, rename, or retire an authoritative document, update `00_SSOT_INDEX.md` in the same change.
- Keep each fact in one place and link to it rather than copying it, because copies drift.
- Every statement about product behavior must trace to a master-specification section. Mark anything else as ASSUMPTION or REQUIRES_DECISION.
- Update documentation in the same change as the code or contract it describes.
- Date entries as `YYYY-MM-DD`.
- Do not delete records. Supersede decisions with new entries, and move resolved items to the Resolved section.
- Product claims and UI copy follow the specification's claims discipline (§49).
- Never put secrets or credential values in documentation.

## Directory layout

```text
docs/
├── README.md                             ← this page
├── 00_SSOT_INDEX.md                      ← where authoritative information lives
├── 01_AGRINEX_MASTER_PRODUCT_PHASES.md   ← master specification (protected)
├── REQUIRES_DECISION.md                  ← open and resolved decisions register
├── architecture/
│   ├── ARCHITECTURE_INDEX.md             ← architecture hub
│   ├── SYSTEM_CONTEXT.md                 ← C4 level 1
│   ├── CONTAINER_ARCHITECTURE.md         ← C4 level 2
│   ├── COMPONENT_ARCHITECTURE.md         ← C4 level 3
│   ├── DATA_FLOW.md                      ← critical data flows
│   ├── INGESTION_ARCHITECTURE.md         ← ingestion
│   ├── RECOMMENDATION_ENGINE.md          ← deterministic engine
│   ├── SECURITY_ARCHITECTURE.md          ← security
│   ├── RESILIENCE_ARCHITECTURE.md        ← failure behavior
│   ├── DEPLOYMENT_ARCHITECTURE.md        ← deployment
│   ├── FRONTEND_ARCHITECTURE.md          ← frontend
│   ├── ML_ARCHITECTURE.md                ← optional ML
│   ├── TESTING_ARCHITECTURE.md           ← verification
│   ├── DECISION_LOG.md                   ← binding decisions (ADRs) and departures
│   └── CURRENT_TECHNOLOGY_BASELINE.md    ← approved technology baseline
├── database/
│   ├── DATABASE_INDEX.md                 ← database hub
│   ├── DATABASE_ARCHITECTURE.md          ← strategy, rationale, CSV evidence profile
│   ├── ERD.md                            ← entity-relationship diagrams
│   ├── DATA_DICTIONARY.md                ← every table and column
│   ├── TABLE_OWNERSHIP.md                ← owner module per table
│   ├── INDEXES_AND_CONSTRAINTS.md        ← keys, constraints, indexes
│   └── SEED_AND_REFERENCE_DATA.md        ← reference data and initial configuration
└── data/
    ├── AGRINEX_VERIFIED_GOVERNMENT_API_DATA_REFERENCE.md  ← observed API payloads (evidence)
    └── raw/                              ← official Agmarknet exports (evidence) and a non-government CSV (validation only)
```

All documents under `architecture/` and `database/` are AUTHORITATIVE since the 2026-10-03 decision lock.

The role-specific files 02–04 named in specification §43 are NOT CREATED YET. See the index.
