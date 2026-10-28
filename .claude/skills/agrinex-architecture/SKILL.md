---
name: agrinex-architecture
description: AgriNex architecture governance. Covers module boundaries and dependency rules, C4 consistency (system context, containers, components, data flows), decision-log (ADR) discipline, the Phase 1 / Phase 2 boundary, and the optional database-mediated ML track. Use this skill whenever a task touches backend module structure, inter-module dependencies or ports, new integrations, containers or deployment shape, ADRs or REQUIRES_DECISION items, ML integration, or any change that could add a service, dependency edge or infrastructure component to AgriNex, even if the user never says "architecture".
---

# AgriNex architecture

## Purpose

Keep every AgriNex change inside the approved architecture, and make sure any genuinely new architectural decision goes through the decision log and human approval before anyone builds against it.

This skill is tooling. It points to the authoritative documents and never replaces them. When this file and the SSOT or a binding decision disagree, the SSOT or decision wins: correct this file and do not change the requirement.

## When to use

- Designing or reviewing module structure, ports, or dependencies between backend modules.
- Any proposal to add a service, container, queue, cache role, external integration or infrastructure component.
- Writing, revising or checking an ADR, a departure from the spec, or a REQUIRES_DECISION item.
- Keeping the C4 views consistent: system context, containers, components, and data flows.
- Anything about the optional ML track or the Phase 1 / Phase 2 boundary.

## Source hierarchy

This follows CLAUDE.md §B:

1. The master specification, `docs/01_AGRINEX_MASTER_PRODUCT_PHASES.md`.
2. Binding decision-log entries (APPROVED or DECIDED) in `docs/architecture/DECISION_LOG.md`.
3. The authoritative documents listed in `docs/00_SSOT_INDEX.md`: the architecture documents, the database documents and `contracts/`.
4. Current official technical documentation, fetched with Context7 (`.claude/rules/context7.md`).

Conversation history, memory, skills, agent definitions and tool output are not authoritative.

## Required reading (READ SSOT FIRST)

1. `CLAUDE.md`: core rules, §A, §B, §D, §G, §N.
2. `docs/00_SSOT_INDEX.md`, area 2. Check `docs/REQUIRES_DECISION.md` for open items before starting.
3. `docs/architecture/ARCHITECTURE_INDEX.md`, then the document for the area:
   - `SYSTEM_CONTEXT.md`, `CONTAINER_ARCHITECTURE.md`, `COMPONENT_ARCHITECTURE.md`, `DATA_FLOW.md`;
   - `INGESTION_ARCHITECTURE.md`, `RECOMMENDATION_ENGINE.md`, `SECURITY_ARCHITECTURE.md`, `RESILIENCE_ARCHITECTURE.md`;
   - `ML_ARCHITECTURE.md`, `FRONTEND_ARCHITECTURE.md`, `DEPLOYMENT_ARCHITECTURE.md`, `TESTING_ARCHITECTURE.md`, `CURRENT_TECHNOLOGY_BASELINE.md`.
4. `docs/architecture/DECISION_LOG.md`: the rules, the index, the departures table, and the entries for the area. Key entries:
   - ADR-0013 (ownership and dependencies; item 3 is amended by ADR-0039);
   - ADR-0035 (read-only ports);
   - ADR-0030 (ML);
   - ADR-0031 and ADR-0032 (Phase 2).
5. `docs/database/TABLE_OWNERSHIP.md`, §4 (dependencies) and §5 (prohibited access).
6. Spec §5, §9–§14, §27, §35, §39, §45.

## Non-negotiable AgriNex rules

- **Modular monolith.** There is one Spring Boot backend container with the modules in `COMPONENT_ARCHITECTURE.md` §4.1:
  - auth, geography, commodity, price, recommendation, alert, admin, ingestion, transport.

  Microservices, Kubernetes, message brokers and extra containers are out of scope (spec §12, §39). Do not drift into them for "scalability".
- **Dependency edges** are only those in `COMPONENT_ARCHITECTURE.md` §4.1 and `TABLE_OWNERSHIP.md` §4: ADR-0013 item 3, plus recommendation → geography from ADR-0039.
  - The graph stays acyclic.
  - Geography, commodity and transport are leaves, and no module depends on admin.
  - Any new edge is a new architectural decision.
- **Cross-module data** flows through read-only ports over existing edges (ADR-0035):
  - `SourceSyncStatusPort` and `FreshnessPolicyPort` are declared by price;
  - `CommodityAvailabilityPort` is declared by commodity;
  - comparisons pass their own pinned freshness thresholds.
- **No module reads another module's tables** (spec §27; `TABLE_OWNERSHIP.md` §5).
- **Government data:**
  - data.gov.in, through the two resources in ADR-0005, is the only price source. Only the ingestion module calls it, server-side (spec §31 RULE 1).
  - A 403, maintenance page or outage is temporary unavailability. It never authorizes another source, fabrication, zero-filling, carrying prices forward or interpolation (ADR-0005).
- **ML is optional** (spec P9, §35; HARD RULE 2; ADR-0030):
  - the backend never calls an ML service;
  - the ML job reads a daily-series view and writes `forecasts` through a restricted database role;
  - the forecast endpoint answers `available: false` when no current forecast exists;
  - forecasts never feed comparison, scoring, ranking, transport or alerts;
  - the deterministic engine must work end to end without ML.
- **Phase boundary.** Phase 2 work starts only after the Phase 1 freeze (HARD RULE 1, spec §10). ADR-0031 and ADR-0032 record placement and deferral only.
- **Operator procedures.** Account erasure, deactivation and reactivation are in-application operator procedures with no HTTP endpoint (ADR-0037, ADR-0041).
- **Decision-log discipline:**
  - A new architectural decision becomes a PROPOSED entry; only a human approves it (CLAUDE.md §G; log rule 3).
  - Never change the substance of an APPROVED or DECIDED entry. Supersede it with a new one (rule 1).
  - Departures from the spec quote the spec text and go into the departures table (rule 5).
  - When an entry becomes binding, the affected documents follow in the same change (rule 6).
  - Where an architecture or database document disagrees with a binding entry, the entry wins and the document is corrected (rule 7).
- **C4 consistency.** A change at one level is checked against the others and against `DATA_FLOW.md` and `DEPLOYMENT_ARCHITECTURE.md`. Diagrams, tables and text must tell the same story.
- **Technology.** Families and versions follow the approved baseline (ADR-0002, `CURRENT_TECHNOLOGY_BASELINE.md`). A version or technology change needs a decision (CLAUDE.md §J).

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

- Architectural reasoning, impact analysis, and consistency checks across the C4 views.
- Drafting PROPOSED decision-log entries with context, options, evidence and consequences.
- Registering REQUIRES_DECISION items.
- Correcting architecture documents to match binding entries, when the task authorizes that change.
- Advising the other skills on boundaries and dependencies.

## Forbidden responsibilities

- Approving decisions, or treating a PROPOSED entry as binding.
- Editing the master specification (CLAUDE.md §E).
- Implementing application code, migrations, contracts or infrastructure under this skill. Hand those to the matching skill after the decision exists.
- Adding modules, edges, containers, brokers or external services without an approved entry.
- Changing ML into a runtime dependency, or Phase 2 into Phase 1 scope.

## Dependencies on other skills

- agrinex-database: table ownership and schema impact.
- agrinex-api-contract: `x-agrinex-module` ownership of every operation.
- agrinex-backend: implementation of modules and ports.
- agrinex-cloud: containers and topology.
- agrinex-release: phase gates.

## Validation expectations

- After any change, cross-check:
  - the edges in `COMPONENT_ARCHITECTURE.md` §4.1 against `TABLE_OWNERSHIP.md` §4 and ADR-0013/ADR-0039;
  - the container list against `CONTAINER_ARCHITECTURE.md` and `DEPLOYMENT_ARCHITECTURE.md`;
  - the ML statements against ADR-0030.
- Search for stale wording such as "PROPOSED" on approved entries, open-item IDs that are already resolved, or an "ML service call".
- Update the decision index, `docs/REQUIRES_DECISION.md` and `docs/00_SSOT_INDEX.md` when entries or authoritative documents change (CLAUDE.md §O).

## Escalation rules

- **CONFLICT between authoritative sources:** stop and report source A, source B, the exact conflict, the affected area and why it blocks (CLAUDE.md §D).
- **Missing information:** register or report REQUIRES_DECISION. Never fill the gap with "common practice".
- **New architecture:** draft a PROPOSED entry and wait for human approval.
- **Security-sensitive change:** human review before merge.
- **Change outside the task scope:** stop and ask.

## Output expectations

Report:
- the files changed;
- the spec sections and ADRs relied on, with evidence labels (VERIFIED, ASSUMPTION, REQUIRES_DECISION, CONFLICT);
- any decision drafted, and its status;
- the consistency checks run and their results;
- open items.

State plainly what was not done.
