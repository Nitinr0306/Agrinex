# AgriNex — Project Operating Rules

This file governs how every Claude Code session, subagent, skill, and contributor works in this repository. It is governance, not product truth. Product requirements live in the master specification, and this file never overrides it on a product matter. In this file, "spec" means the master specification, `docs/01_AGRINEX_MASTER_PRODUCT_PHASES.md`.

**Read first:**

- [`docs/00_SSOT_INDEX.md`](docs/00_SSOT_INDEX.md): where the authoritative information for each area lives.
- [`docs/REQUIRES_DECISION.md`](docs/REQUIRES_DECISION.md): open decisions.
- [`docs/architecture/DECISION_LOG.md`](docs/architecture/DECISION_LOG.md): approved decisions.
- [`docs/README.md`](docs/README.md): how the documentation is organized.

## Core rules

These 16 rules are carried over unchanged from the original CLAUDE.md. Sections A–O expand on them.

1. Read the SSOT before making project decisions.
2. Never invent requirements.
3. Never invent API contracts.
4. Never invent database fields.
5. Never assume library APIs.
6. Verify current library/framework documentation.
7. If information is missing: REQUIRES_DECISION
8. If documents conflict: STOP and report conflict.
9. Do not modify unrelated files.
10. Do not perform large uncontrolled rewrites.
11. Work in small independently verifiable tasks.
12. Every implementation requires tests.
13. Do not mark a task complete until acceptance criteria pass.
14. Do not change approved architecture without an ADR.
15. Do not introduce a dependency without justification.
16. Security-sensitive decisions require explicit review.

In this repository, an ADR (rule 14) is an entry in `docs/architecture/DECISION_LOG.md`.

## A. Project context

- **AgriNex — Farmer Selling Intelligence & Market Linkage Platform** (SIH26132) is a farmer-centric decision-support web platform. It turns government mandi price data (data.gov.in / DMI) into transport-aware, explainable market recommendations (spec §1, §5).
- **Phase 1, "Where should I consider selling?"**, must stand alone as a complete, deployed, academically defensible product (spec §1, §7).
- **Phase 2, "Can AgriNex help me act on that decision?"**, adds buyer linkage (listing → bid → transaction). It extends Phase 1 rather than being a separate product (spec §8).
- **HARD RULE 1:** Phase 2 development does not begin before Phase 1 is deployed, tested, and formally frozen.
- **HARD RULE 2:** ML integration does not begin before the deterministic decision engine works end-to-end (spec §10). ML is optional (spec P9, §35).
- The out-of-scope list in spec §39 is enforced.
- The only correct product name is AgriNex. "MandiWise" and "KrishiSetu" are retired names (spec header).

This section orients you. It does not replace the spec.

## B. Source-of-truth hierarchy

Precedence, highest first:

1. **Master specification**, `docs/01_AGRINEX_MASTER_PRODUCT_PHASES.md`. This is the product-level single source of truth. It covers requirements, scope, phases, and business rules. It also covers the architecture and technology it states, and the acceptance criteria. It wins over the role-specific files 02–04 (spec header, line 17).
2. **Approved decisions**: entries with status APPROVED in `docs/architecture/DECISION_LOG.md`. They settle gaps the spec leaves open and record approved architecture or technology departures (G, J). They cannot change product requirements, scope, phases, or acceptance criteria.
3. **Subordinate project documents**, once created and listed as authoritative in the SSOT index:
   - role-specific files 02–04 (spec §43);
   - shared contracts in `contracts/` (spec §41);
   - database migrations;
   - the technology baseline;
   - the architecture documents in `docs/architecture/` (see `docs/architecture/ARCHITECTURE_INDEX.md`).

   Each governs only its own area and must stay consistent with 1 and 2.
4. **Current official technical documentation**, fetched via Context7 (`.claude/rules/context7.md`). It is authoritative for how a library, framework, or tool behaves at a given version, but is never a source of AgriNex requirements.

Not authoritative:

- conversation history, memory, or training data;
- agent and skill definitions in `.claude/`;
- tool logs;
- retired prototypes;
- any document the SSOT index does not list as authoritative.

If two authoritative sources conflict and this order does not settle it, STOP and report the conflict (rule 8).

| Area | Governs |
|---|---|
| Product | Requirements, scope, phases, business rules, acceptance criteria |
| Architecture | System and module boundaries, integrations, cross-cutting technical decisions |
| Database / data | Entities, schema, migrations, data ownership, ingestion and normalization rules, reference data |
| API contracts | Endpoints, methods, request/response schemas, validation, status and error semantics, authentication requirements |
| UX / design | Screens and states, design system, copy and labelling, localization, accessibility |
| QA / testing | Test strategy, acceptance verification, evaluation experiments |
| Operations / deployment | Services, environments, configuration, health checks, deployment |
| Technology baseline | Approved, verified implementation versions and their compatibility |

`docs/00_SSOT_INDEX.md` gives the authoritative document and current status for each area.

## C. Requirement integrity

Never invent any of the following, and never import them from "common practice". Each must trace to an authoritative source (B). Otherwise, raise REQUIRES_DECISION (D).

- Requirements, features, or scope.
- Business rules, formulas, thresholds, or default values.
- Roles or permissions. The spec defines FARMER, BUYER, and ADMIN and their capabilities (spec §28).
- Workflows or state machines.
- API contracts (H), or database entities and fields (I).
- ML behavior (spec §35).
- Infrastructure requirements.
- Data presented as real. Never fabricate, interpolate, or synthesize price data (spec P10, §31 RULE 5). Reference data such as market coordinates must come from an identified source.

A plausible default is still an invention unless a source establishes it. Product claims, UI copy, and code comments follow the spec's naming and claims rules (§31 RULE 4, §49).

## D. Uncertainty handling

Classify the evidence behind every project-level statement and decision:

| Label | Meaning | Action |
|---|---|---|
| VERIFIED | Directly supported by the spec, an authoritative project document, or current official technical documentation | Cite it: file and section, or library ID and version |
| ASSUMPTION | A temporary interpretation needed to proceed, not established by a source | Label it, explain why it is unavoidable, keep it reversible, and list it in your output |
| REQUIRES_DECISION | Information needed to proceed safely is not established by an authoritative source | Stop the affected work. Register the item in `docs/REQUIRES_DECISION.md`, or report it to the user |
| CONFLICT | Two authoritative sources establish incompatible information | STOP the affected reasoning. Report source A, source B, the exact conflict, the affected area, and why it blocks. Do not pick a winner unless B settles it |

Never silently turn:

- an assumption into a requirement;
- a likely choice into an approved decision;
- common industry practice into an AgriNex requirement;
- a historical version into a current one;
- an implementation preference into an architecture decision.

These labels classify evidence. Claims in product documentation, UI copy, code comments, and presentations use the spec's own claim labels instead (spec §49): VERIFIED (verified by API test), PLANNED, ESTIMATED, and OPTIONAL.

Before starting work in an area, check `docs/REQUIRES_DECISION.md`. Work that a BLOCKING item names must not start.

## E. Master specification protection

- Do not modify `docs/01_AGRINEX_MASTER_PRODUCT_PHASES.md` unless the user explicitly authorizes that change as a separate task. Do not rewrite, normalize, reformat, restructure, correct, or "improve" it.
- Do not silently reconcile its internal contradictions. Preserve the ambiguity and register a REQUIRES_DECISION item. Known items are in `docs/REQUIRES_DECISION.md`.
- When a role-specific file (02–04) conflicts with the spec, correct the role file, not the spec (spec header, line 17).

## F. Change control

- Inspect before editing. Read the target file and understand what depends on it before changing it.
- Make the smallest change that satisfies the task. No uncontrolled rewrites, opportunistic refactors, or formatting-only churn.
- Change only files within the task's scope. If a necessary change falls outside it, stop and ask.
- Preserve existing working behavior unless the task explicitly changes it.
- Keep each change independently reviewable and verifiable.
- Read content before deleting or overwriting it.
- These actions need explicit user instruction: deleting files, rewriting Git history, and changing Git, MCP (`.mcp.json`), Claude settings, CI, or deployment configuration.

## G. Architecture control

- Approved architecture is what the spec states (notably §11–§13, §27, §28, §29, §39), plus APPROVED decision-log entries. Check these invariants first:
  - The backend is a modular monolith. Microservices and Kubernetes are out of scope (spec §12, §39).
  - Each backend module owns its repositories. No module accesses another module's database tables directly (spec §27).
  - Government API calls happen server-side only, in the ingestion module (spec §31 RULE 1).
  - ML failure must not affect comparison, recommendation, transport, or alerts (spec §35).
- A new architectural decision is one not already settled by the spec or an approved decision. It affects at least one of:
  - module or service boundaries;
  - system boundaries;
  - API contract conventions;
  - database ownership;
  - external integrations;
  - security architecture;
  - infrastructure architecture.
- A new architectural decision needs a decision-log entry and explicit human approval before implementation (rule 14).
- Agents may draft PROPOSED entries, but only a human approves them. Do not implement against a PROPOSED entry.

## H. API contract control

- Work contract-first (spec P5). Shared contracts belong in `contracts/` (spec §41, §43). The spec designates `contracts/openapi.yaml` as the REST contract between backend, frontend, and QA. The SSOT index shows whether each contract exists yet.
- Frontend and backend use the same approved contract. Neither side may independently invent any of these:
  - endpoint paths or HTTP methods;
  - request or response schemas;
  - validation rules;
  - status or error semantics;
  - authentication requirements.
- Contract changes follow the spec's Shared Contract Change Rule (§41).
- Mocks must match the contract exactly. A mock with wrong field names is a bug (spec §44).
- Where no approved contract exists yet, the only sources are the spec (§28, §30, and the flows in §16, §17, §21, and §23) and APPROVED decision-log entries. Those flows do not define complete schemas and disagree in places (see `docs/REQUIRES_DECISION.md`). Raise REQUIRES_DECISION rather than filling the gaps.

## I. Database control

- Sources are:
  - spec §20, which is a domain overview, not a complete schema;
  - spec §14–§15 and §31–§34;
  - the planned backend API/database document and migrations (see the SSOT index).
- Before any schema change, check:
  - the existing schema and migrations;
  - the approved data model;
  - the migration required;
  - compatibility with existing data;
  - affected API behavior and contracts;
  - test impact.
- Do not invent entities, tables, columns, types, constraints, or indexes (rule 4).
- Change the schema only through versioned migrations (Flyway; spec §12, §43). Do not edit a migration that may already have been applied. Add a new one instead.
- The spec's data-integrity rules are requirements. One example is the database-level unique `business_key` (spec §31 RULE 6).

## J. Technology version control

- The technologies the spec names are part of the approved architecture. The version numbers it states are a historical baseline, not automatically the current recommended versions. They appear in the header "Stack" line, §12, and §45.
- Before scaffolding, adding a dependency, or changing a version:
  1. Verify current official documentation (Context7).
  2. Verify compatibility across the stack.
  3. Document the recommended baseline.
- A baseline that departs from a version the spec states needs a decision-log entry and explicit human approval. The spec itself is not edited (E).
- Producing the technology baseline is a separate task. Do not scaffold applications or install dependencies until an approved baseline exists. Its status is in SSOT index area 8 (see also RD-001).
- Justify every new dependency (rule 15): why it is needed, why this one, and whether it is compatible with the approved baseline.
- Never write library calls from memory. Check the documentation for the version in use (rules 5–6).

## K. Security control

- Security-sensitive changes need explicit human review before they are merged or applied (rule 16). These areas are security-sensitive:
  - authentication;
  - authorization and RBAC;
  - secrets and credentials;
  - token handling;
  - tenant or per-user data isolation;
  - access control;
  - external integrations;
  - security configuration: CORS, headers, TLS, rate limiting.
- The spec's security controls (§27, §28) are requirements. Do not weaken or replace them without an approved decision.
- Secrets never go in any of these (spec §27, §28, §47 Q21):
  - frontend code or `NEXT_PUBLIC_*` variables;
  - version control;
  - logs, error messages, or API responses.
- Never copy a secret value into documentation, commit messages, or chat output. This includes credentials in tool configuration such as `.mcp.json`.
- If you find an exposed credential, stop and tell the user. Do not attempt rotation or history rewriting yourself.

## L. Testing and verification

- Every implementation task names its tests up front: unit, integration, contract, or end-to-end as appropriate. Core calculations (transport, net realization, scoring) need deterministic unit tests with hand-computed fixtures (spec §27, §48).
- A task is complete only when all of these hold:
  - its acceptance criteria are addressed, including relevant items in spec §48 and the spec §10 freeze checklist;
  - the relevant tests have been run, not just written;
  - failures have been investigated, not skipped or suppressed;
  - any required verification has been performed.
- Report results faithfully. State failures, skipped steps, and unverified claims as such. Writing code is not the same as completing a task.
- Do not weaken, skip, or delete tests to make a run pass.

## M. Design and frontend control

- Screen design belongs to the UI/UX role (spec §40). The spec's UX requirements include:
  - personas (§6);
  - mobile-first responsiveness, accessibility, and localization (F1.2, F1.21, F1.22; §27; §48);
  - mandatory labels and disclaimers (§31, §32, §49).
- Do not build production screens from invented designs. Each screen needs an approved design source: the UI/UX specification (role file 02), a design recorded as approved in the SSOT index (for example, a Stitch screen), or both.
- ADR-0027, which resolved RD-010, defines the design source of record: role file 02, with each screen's approval recorded by the Document Owner in SSOT index area 5. Do not implement a production screen whose design is not approved in that way.
- Label prototypes as prototypes.
- The frontend uses only the approved API contract or contract-matching mocks (H).

## N. Subagent control

- Give each subagent a narrowly scoped brief. Where relevant, it includes:
  - the objective;
  - inputs: the documents and sections to read;
  - allowed files and forbidden files;
  - dependencies;
  - acceptance criteria and required tests;
  - the expected output.
- Subagents follow this file. They must not make silent product or architecture decisions. Anything unresolved comes back labelled REQUIRES_DECISION, ASSUMPTION, or CONFLICT.
- The delegating session verifies subagent output (reads the diff, reruns the tests) before accepting it. A subagent's claim is not evidence.
- Agent definitions (`.claude/agents/`) and skills (`.claude/skills/`) are tooling. They cannot override this file or the SSOT.

## O. Documentation workflow

Before changing a major project area:

1. Locate the relevant SSOT document through `docs/00_SSOT_INDEX.md`.
2. Read it, including the relevant sections in full.
3. Verify the constraints that apply.
4. Identify conflicts, and STOP if there are any.
5. Identify unresolved decisions. Check and update `docs/REQUIRES_DECISION.md`.
6. Make the smallest valid change.
7. Verify the result.

When you create, rename, or retire an authoritative document, update `docs/00_SSOT_INDEX.md` in the same change. When a decision is approved, record it in `docs/architecture/DECISION_LOG.md` and move the matching REQUIRES_DECISION item to Resolved.
