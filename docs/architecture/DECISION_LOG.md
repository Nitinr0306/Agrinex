# AgriNex — Architecture Decision Log

This is the single home for approved project decisions. In this repository an ADR ([`CLAUDE.md`](../../CLAUDE.md) rule 14) is an entry in this file.

## Current state

**Architecture lock: 2026-10-03.** On that date the project owner explicitly delegated the remaining technical, architectural, database, ingestion, infrastructure, security-boundary and operational decisions to the lead architect (ADR-0001), and approved the recommended technology baseline (ADR-0002). This log records every decision taken in that lock, ADR-0001 to ADR-0034.

**API contract (Task 8): 2026-10-04.** Writing the contract found three items that the lock did not settle. They are recorded as PROPOSED entries, ADR-0035 to ADR-0037, with RD-038 to RD-040. The Task 8 reviews found three more, RD-041 to RD-043, registered in `docs/REQUIRES_DECISION.md` with their options. The delegation in ADR-0001 does not cover any of them, so they await explicit human approval. Individual endpoint shapes live in `contracts/`, not in this log.

**Decision review (Task 8.5): 2026-10-04.** `docs/decision-review/TASK_8_5_HUMAN_DECISION_PACKET.md` sets out options and recommendations for RD-038 to RD-043, plus the new RD-044. No entry changed status. ADR-0035 to ADR-0037 remain PROPOSED.

**Decision lock (Task 8.6): 2026-10-04.** The Human Project Owner approved:

- ADR-0035 and ADR-0036;
- ADR-0037, redrafted to option C;
- the new entries ADR-0038 to ADR-0041.

Together these resolve RD-038 to RD-044. The Human Project Owner also signed off the Task 8.5 security review (ADR-0042). No item from the Task 8 contract work remains open.

**Task 9 final decision lock: 2026-10-04.** The Human Project Owner decided the six Task 9 questions H1 to H6 (`docs/decision-review/TASK_9_DESIGN_DIRECTION_PACKET.md` §8):

- ADR-0043 (APPROVED): the UI/UX direction Hisaab with four borrowings, the screen approval, and two recorded delegations;
- ADR-0044 (APPROVED): the role file 02 design decisions PDD-01 to PDD-14;
- ADR-0045 (DECIDED under ADR-0043): the recommendation wording, resolving RD-045;
- ADR-0046 (APPROVED): Claude as owner of the Marathi and Hindi interface copy, resolving RD-046;
- ADR-0047 (APPROVED): email at registration, login by email or phone with one password, and Phase 1 password recovery, resolving RD-047;
- ADR-0048 (DECIDED under ADR-0043): the email-verification and password-reset mechanism and email delivery through Resend.

Record of the lock, research and readiness check: `docs/decision-review/TASK_9_FINAL_DECISION_LOCK.md`.

The master specification (`docs/01_AGRINEX_MASTER_PRODUCT_PHASES.md`) approves the architecture and technology it states, for example in §11–§13 and §27–§29. Those choices are not re-entered here as historical decisions. This log records decisions made from 2026-10-02 onward.

## What belongs here

- **New architectural decisions** not already settled by the master specification or an earlier binding entry. These affect at least one of:
  - module or service boundaries;
  - system boundaries;
  - API contract conventions (individual endpoint shapes live in `contracts/`);
  - database ownership;
  - external integrations;
  - security architecture;
  - infrastructure architecture.
- **Technology baseline approvals**, and any departure from a technology or version the master specification states (CLAUDE.md §J).
- **Resolutions of `docs/REQUIRES_DECISION.md` items**, including clarifications of gaps or inconsistencies in the master specification. This gives every decision one home.

**Not here:** changes to product requirements, scope, phases, or acceptance criteria. Those need an explicitly authorized update of the master specification (CLAUDE.md §E).

## Statuses

| Status | Meaning | Binding? |
|---|---|---|
| PROPOSED | Drafted for review, not yet approved | No. Do not implement against it |
| APPROVED | Explicitly approved by a human. The Approval field records who, when, and where | Yes |
| DECIDED | Decided under an explicit, recorded human delegation, within that delegation's scope (rule 3): ADR-0001 for the 2026-10-03 lock; ADR-0043 item 4 for the two choices delegated in the Task 9 final lock. The Approval field cites the delegating entry. Wherever CLAUDE.md refers to APPROVED entries (§B item 2, §G), a DECIDED entry counts as one | Yes, until a human supersedes it with a later entry |
| DEFERRED | Deliberately postponed. The entry names the trigger that reopens it and what stays blocked until then. Must also appear in `docs/REQUIRES_DECISION.md` | No. The deferred part must not be implemented |
| SUPERSEDED | Replaced by a later binding entry. The Status field reads "SUPERSEDED by ADR-NNNN" | No |
| REJECTED | Considered and declined. Kept for history | No |
| REQUIRES_DECISION | Needed, but cannot be decided because information or human input is missing. Must also appear in `docs/REQUIRES_DECISION.md` | No |

## Rules

1. **Append-only.** Never delete an entry, and never change the substance of an APPROVED or DECIDED entry. To change a decision, write a new entry that supersedes it and update the old entry's status.
2. **IDs** are sequential (`ADR-0001`, `ADR-0002`, …) and never reused.
3. **Only humans approve.** Agents may draft PROPOSED entries. An entry becomes APPROVED only when a human has explicitly approved it, and the Approval field records the approver, the date, and where approval was given (PR, issue, or conversation). The one exception is a decision taken under an explicit, recorded human delegation: it carries status DECIDED and cites the delegating entry. The delegation recorded in ADR-0001 covers only the decisions of the 2026-10-03 architecture lock (ADR-0002 to ADR-0034). It does not carry over to later sessions or agents.
4. **Evidence is required.** Cite master-specification sections, project documents, or current official documentation with versions. Label anything else ASSUMPTION.
5. **Departures from the master specification** must:
   - quote the specification text they depart from;
   - state explicitly that they are a departure;
   - be listed in the departures table below.

   The specification itself is not edited unless the user separately authorizes that.
6. **Follow through.** When an entry becomes binding, update the affected documents (SSOT index, architecture and database documents, contracts, role files). Also move any related `docs/REQUIRES_DECISION.md` item to Resolved. Do both in the same change or the one immediately after.
7. **Precedence inside the architecture and database documents.** Where an architecture document (`docs/architecture/`) or database document (`docs/database/`) disagrees with a binding entry here, the entry governs and the document must be corrected.

## Decision index

| ID | Title | Status | Resolves | Date |
|---|---|---|---|---|
| ADR-0001 | Delegated decision authority and the 2026-10-03 architecture lock | APPROVED | — | 2026-10-03 |
| ADR-0002 | Technology baseline approval and version departures | APPROVED | RD-001 | 2026-10-03 |
| ADR-0003 | Backend build tool: Maven 3.10.x with the Maven Wrapper | DECIDED | RD-013 | 2026-10-03 |
| ADR-0004 | Supported browser floor for farmer-facing screens | DECIDED | RD-014 | 2026-10-03 |
| ADR-0005 | Government data source, evidence hierarchy and outage behavior | DECIDED | — | 2026-10-03 |
| ADR-0006 | Phase 1 coverage and reference vocabularies | DECIDED | RD-003, RD-031 | 2026-10-03 |
| ADR-0007 | Source date formats and the reference time zone | DECIDED | RD-004, RD-036 | 2026-10-03 |
| ADR-0008 | Price unit, numeric representation and unit-consistency validation | DECIDED | RD-027 | 2026-10-03 |
| ADR-0009 | Observation identity, duplicates, conflicting records and revisions | DECIDED | RD-005, RD-028 | 2026-10-03 |
| ADR-0010 | Observation selection for comparison, trends and alerts | DECIDED | RD-015 | 2026-10-03 |
| ADR-0011 | Ingestion run orchestration | DECIDED | RD-016 | 2026-10-03 |
| ADR-0012 | Ingestion completeness metric and audit counters | DECIDED | RD-029 | 2026-10-03 |
| ADR-0013 | Module data ownership and cross-module access | DECIDED (item 3 amended by ADR-0039) | RD-017 | 2026-10-03 |
| ADR-0014 | Geographic reference data, coordinates, markets and aliases | DECIDED | RD-018, RD-030, RD-032 | 2026-10-03 |
| ADR-0015 | Localized reference names | DECIDED | RD-033 | 2026-10-03 |
| ADR-0016 | User accounts, passwords and token handling | DECIDED (items 1 and 3 amended by ADR-0047) | RD-034 | 2026-10-03 |
| ADR-0017 | Public routing, origin layout, CORS, browser tokens and rate limits | DECIDED (item 6 extended by ADR-0048) | RD-021 | 2026-10-03 |
| ADR-0018 | Redis responsibilities and failure behavior | DECIDED | RD-019 | 2026-10-03 |
| ADR-0019 | Alert evaluation and delivery | DECIDED | RD-020 | 2026-10-03 |
| ADR-0020 | Admin control boundary, secret handling and admin audit | DECIDED | — | 2026-10-03 |
| ADR-0021 | Deployment topology | DECIDED | RD-022 | 2026-10-03 |
| ADR-0022 | API contract strategy and conventions for Task 8 | DECIDED | RD-023 | 2026-10-03 |
| ADR-0023 | Comparison endpoint field names and no-data semantics | DECIDED | RD-006 | 2026-10-03 |
| ADR-0024 | Composite scoring rules and default weights | DECIDED | RD-007 | 2026-10-03 |
| ADR-0025 | Trend direction and trend windows | DECIDED | RD-008 | 2026-10-03 |
| ADR-0026 | What-If simulator calculation and ranking | DECIDED | RD-009 | 2026-10-03 |
| ADR-0027 | Design source of record and design approval | DECIDED | RD-010 | 2026-10-03 |
| ADR-0028 | Demo credentials and demo data | DECIDED | RD-011, RD-026 | 2026-10-03 |
| ADR-0029 | Data retention and deletion | DECIDED | RD-035 | 2026-10-03 |
| ADR-0030 | Backend–ML integration (optional track) | DECIDED | RD-024 | 2026-10-03 |
| ADR-0031 | Phase 2 component placement | DECIDED | RD-025 (b), (c), (d) mechanism | 2026-10-03 |
| ADR-0032 | Phase 2 state model, OTP provider and listing TTL | DEFERRED | RD-012, RD-025 (a), (d) value | 2026-10-03 |
| ADR-0033 | Credentials in the MCP configuration | DECIDED | RD-002 (decision; execution pending user action) | 2026-10-03 |
| ADR-0034 | Tooling deferred to the scaffolding of each area | DEFERRED | — | 2026-10-03 |
| ADR-0035 | Read-only ports for cross-module data in public responses | APPROVED | RD-038 | 2026-10-04 |
| ADR-0036 | Earth radius for haversine distances | APPROVED | RD-039 | 2026-10-04 |
| ADR-0037 | Account erasure as an in-application operator procedure | APPROVED | RD-040 | 2026-10-04 |
| ADR-0038 | Transport-cost label shows the applied rate | APPROVED | RD-041 | 2026-10-04 |
| ADR-0039 | Server-side district-centroid origins and the recommendation → geography dependency | APPROVED | RD-042 | 2026-10-04 |
| ADR-0040 | Comparison estimates beyond the stored precision | APPROVED | RD-043 | 2026-10-04 |
| ADR-0041 | Account deactivation and reactivation as operator procedures | APPROVED | RD-044 | 2026-10-04 |
| ADR-0042 | Task 8.5 security review sign-off | APPROVED | — | 2026-10-04 |
| ADR-0043 | Approved UI/UX direction, screen approval and recorded delegations (H1, H6) | APPROVED | — | 2026-10-04 |
| ADR-0044 | Approval of the role file 02 design decisions PDD-01 to PDD-14 (H2) | APPROVED | — | 2026-10-04 |
| ADR-0045 | Recommendation and winner wording | DECIDED (under ADR-0043) | RD-045 | 2026-10-04 |
| ADR-0046 | Marathi and Hindi interface copy: ownership and rules (H4) | APPROVED | RD-046 | 2026-10-04 |
| ADR-0047 | Account email, login identifiers and Phase 1 password recovery (H5) | APPROVED | RD-047 | 2026-10-04 |
| ADR-0048 | Email verification, password-reset mechanism and email delivery | DECIDED (under ADR-0043) | — (mechanism of ADR-0047) | 2026-10-04 |
| ADR-0049 | Frontend tooling chosen at scaffolding (Task 11) | DECIDED (under ADR-0050) | — (frontend part of ADR-0034) | 2026-10-06 |
| ADR-0050 | Delegated decision authority for the Phase 1 pre-deployment closure (Task 12) | APPROVED | — | 2026-10-15 |
| ADR-0051 | Commodity source aliases for legacy DMI spellings | DECIDED (under ADR-0050) | RD-050 | 2026-10-15 |
| ADR-0052 | Task 12 closure decisions D2 to D12 | DECIDED (under ADR-0050) | RD-048, RD-049 | 2026-10-15 |
| ADR-0053 | Offline evaluation of the forecasting baselines before deployment | PROPOSED | RD-051 | 2026-10-18 |
| ADR-0054 | Historical maintenance: report-date ledger, gap backfill and reconciliation | APPROVED | — | 2026-10-18 |
| ADR-0055 | Public deployment: Oracle Cloud Always Free VM, domain and production data | APPROVED | RD-037 | 2026-10-18 |

## Departures from the master specification

Each row quotes the specification text that a binding entry departs from. The specification is not edited (CLAUDE.md §E).

| # | Specification text (quoted) | Location | Departure | Entry |
|---|---|---|---|---|
| D-01 | "Java 17" | Header line 12; §12 | Java 25 (LTS) | ADR-0002 |
| D-02 | "Spring Boot 3.3.x" | Header line 12; §12; §43 | Spring Boot 4.1.x | ADR-0002 |
| D-03 | "Next.js 14" | Header line 12; §12; §43 | Next.js 16.3.x | ADR-0002 |
| D-04 | "Python 3.11 (ML, optional)" | Header line 12; §12 | Python 3.14 (optional track) | ADR-0002 |
| D-05 | `nginx:1.25` | §45 services table | `nginx:1.30.5` (stable line) | ADR-0002 |
| D-06 | "Parse date (dd-MM-yyyy or MM/dd/yyyy)" | §15 PROCESS step 5 | Record dates are parsed only as `dd/MM/yyyy`, the format every captured record uses. The historical request filter uses `dd-MM-yyyy` | ADR-0007 |
| D-07 | "DO UPDATE SET ingested_at=NOW() (if same price, still update timestamp)" | §15 UPSERT node | An identical re-delivery writes nothing and is counted in `duplicates_skipped`. Only changed prices are written, and each change is recorded as a revision | ADR-0009 |
| D-08 | Current dataset: "Latest prices, comparison, alerts, daily sync"; historical dataset: "Trends, ML training data, historical bootstrap" | §1 dataset table | Calculations select from both datasets. For the same market, commodity, variety, grade and date, the current dataset takes precedence | ADR-0010 |
| D-09 | Redis: "Rate limiting / Session cache / Alert queue" | §12 container diagram | Redis holds rate-limit counters and a reference-data cache only. There is no session cache and no alert queue | ADR-0018 |
| D-10 | Nginx: "TLS termination / CORS headers" | §12 container diagram | Nginx emits no CORS headers. Frontend and API share one origin, and the backend keeps its restricted CORS configuration (§28) as defense in depth | ADR-0017 |
| D-11 | "OpenAPI spec generated and exported to `contracts/openapi.yaml`" | §10 freeze checklist | `contracts/openapi.yaml` is hand-authored and authoritative. Any generated document is a build artifact used only to detect drift | ADR-0022 |
| D-12 | "Demo fixture data loads on first boot" | §10 freeze checklist | Reference data loads on first boot everywhere. Demo fixtures and test accounts load on first boot only in environments that enable the demo profile, never in the public deployment | ADR-0021, ADR-0028 |
| D-13 | `newTransport = max(300, dist × newRate)` | §17 client-side node | Client-side recalculation uses the full §32 formula, with the trip factor and the configured minimum charge | ADR-0026 |
| D-14 | Transport cost must always show: "**Estimated** — based on ₹8/km assumption. Actual costs will vary."; "Transport cost: ESTIMATED based on ₹8/km assumption"; "Estimated transport cost based on ₹8/km assumption" | §32 Mandatory UI Labels (line 1458); §49 (lines 2128, 2138) | The label shows the rate the estimate actually used (`appliedRatePerKm`): "Estimated — based on ₹{appliedRatePerKm}/km assumption. Actual costs will vary." At the default rate the text is identical to the specification | ADR-0038 |
| D-15 | "Quick phone registration\n(lightweight)" | §25 activity diagram (line 1266) | Registration also requires an email address, verified afterwards by an emailed link; it is still a quick registration with no SMS or OTP | ADR-0047 |
| D-16 | WinnerCalloutBanner: "Highest Price: Market B\nHighest Net: Market A"; "Highest Net Realization: APMC Nashik ← Recommended" | §25 (line 1254); §46 (line 1956) | The composite rank 1 is labelled "Recommended". "Highest estimated net realization" labels only options whose estimate is the highest, and the banner states the trade-off in rupees without claiming that rank 1 leaves more money when its estimate is not higher | ADR-0045 |

Pinning `postgres:16` to 16.15 and `redis:7-alpine` to 7.4.11 keeps the specification's major versions. These are refinements, not departures (ADR-0002).

## Entry template

Copy this block, fill in every field, and add the entry to the decision index.

```markdown
### ADR-NNNN — <short title>

- **Decision ID:** ADR-NNNN
- **Date:** YYYY-MM-DD
- **Status:** PROPOSED | APPROVED | DECIDED | DEFERRED | SUPERSEDED by ADR-NNNN | REJECTED | REQUIRES_DECISION
- **Title:** <short title>
- **Context:** The problem or open question, the constraints, and the related REQUIRES_DECISION item (if any). Separate VERIFIED facts from ASSUMPTIONS.
- **Decision:** Exactly what is decided, and its scope. If it departs from the master specification, quote the text it departs from.
- **Rationale:** Why this option is the best for AgriNex, judged on correctness, security, reliability, maintainability, simplicity and cost.
- **Alternatives Considered:** Each realistic option with its main trade-offs, including keeping the status quo where relevant.
- **Consequences:** Effects on modules, contracts, schema, tests, operations, and security; follow-up work; what becomes harder.
- **Source / Evidence:** Master-specification sections, project documents, current official documentation (library and version), measurements.
- **Approval:** Approver name/role, date, and where approval was given. "Pending" while PROPOSED. For DECIDED entries: "Decided under ADR-0001".
```

## Decisions

### ADR-0001 — Delegated decision authority and the 2026-10-03 architecture lock

- **Decision ID:** ADR-0001
- **Date:** 2026-10-03
- **Status:** APPROVED
- **Title:** Delegated decision authority and the 2026-10-03 architecture lock
- **Context:**
  - VERIFIED: in the Task 7 final instruction (conversation, 2026-10-03) the project owner wrote: "The human has explicitly authorized you to make the remaining technical, architectural, database, ingestion, infrastructure, security-boundary, and operational decisions." The same instruction says "DO NOT ASK THE HUMAN TO CHOOSE BETWEEN TECHNICAL OPTIONS", "YOU DECIDE WHATEVER IS BEST FOR AGRINEX" and "The human approves the recommended technology baseline."
  - The instruction keeps the master specification as the single source of truth. It forbids inventing requirements, data or API behavior, and it forbids implementation.
  - Before this entry, governance allowed agents only to draft PROPOSED entries (CLAUDE.md §G, rule 3 of this log). Thirty-six REQUIRES_DECISION items were open, most of them BLOCKING.
- **Decision:**
  1. The delegation is recorded here as a human approval act. Decisions taken under it carry status DECIDED and are binding (see Statuses).
  2. **Scope.** The delegation covers the technical, architectural, database, ingestion, infrastructure, security-boundary and operational decisions of this lock: ADR-0002 to ADR-0034. It does not cover:
     - changes to product requirements, scope, phases or acceptance criteria (CLAUDE.md §E);
     - legal conclusions;
     - procurement of paid accounts or services;
     - actions that CLAUDE.md §F reserves to explicit user instruction (changing `.mcp.json`, Git, CI or deployment configuration). ADR-0033 lists the resulting user actions.
  3. The delegation is not standing authority. Later sessions and agents draft PROPOSED entries again unless a human records a new delegation.
  4. Two statuses are added to this log: DECIDED and DEFERRED.
  5. The architecture documents (`docs/architecture/`) and database documents (`docs/database/`) become authoritative for their areas, as amended by the binding entries here (rule 7).
- **Rationale:**
  - The delegation is explicit and written. Recording it makes every later decision traceable to a human act.
  - A separate DECIDED status shows exactly which decisions a human approved directly (ADR-0001, ADR-0002) and which were taken under delegation, so a reviewer can audit and supersede the latter.
- **Alternatives Considered:**
  - Keep every decision PROPOSED until a human approves each one. This contradicts the instruction and leaves Phase 1 blocked.
  - Mark delegated decisions APPROVED. This would misstate that a human reviewed each decision.
- **Consequences:**
  - Items RD-001 to RD-036 are resolved, decided or deferred by ADR-0002 to ADR-0034, and `docs/REQUIRES_DECISION.md` is restructured to match. RD-002 is decided (ADR-0033) but stays open until the user acts. RD-012 and part of RD-025 are deferred (ADR-0032). RD-037 is new.
  - For CLAUDE.md §B item 2 ("entries with status APPROVED") and §G ("explicit human approval before implementation"), a DECIDED entry has the force of an APPROVED entry, because the approving human act is this entry.
  - Product parameters decided under the delegation, such as the default scoring weights (ADR-0024), are runtime configuration. The product owner can change them through the admin configuration without code changes.
  - A human may supersede any DECIDED entry with a new entry.
- **Source / Evidence:** Task 7 final instruction (conversation, 2026-10-03); CLAUDE.md §B, §D, §E, §F, §G, rule 14; this log's rules 1–3.
- **Approval:** Project owner, 2026-10-03, conversation ("AGRINEX — TASK 7 FINAL DECISION & ARCHITECTURE LOCK").

### ADR-0002 — Technology baseline approval and version departures

- **Decision ID:** ADR-0002
- **Date:** 2026-10-03
- **Status:** APPROVED
- **Title:** Technology baseline approval and version departures
- **Context:**
  - RD-001. `docs/architecture/CURRENT_TECHNOLOGY_BASELINE.md` (verified 2026-10-02) recommended a baseline. It is pending approval, with two open sub-decisions: RD-013 (build tool) and RD-014 (browser floor).
  - The project owner approved the recommended baseline on 2026-10-03.
- **Decision:**
  - The baseline in `CURRENT_TECHNOLOGY_BASELINE.md` §8 is the approved implementation baseline, completed by:
    - ADR-0003: Maven 3.10.x;
    - ADR-0004: the browser floor is accepted, and Tailwind CSS 4.3.x stays.
  - Re-verification on 2026-10-03 against official registries changed nothing except one JDK patch release:

    | Component | Verified on 2026-10-03 |
    |---|---|
    | Spring Boot | 4.1.1 is the latest GA. 4.2.0 exists only as milestones |
    | Next.js | 16.3.8 (npm `latest`) |
    | React | 19.3.0 |
    | Node.js | 24.21.0, LTS "Krypton" |
    | Maven | 3.10.0 |
    | Container images | `postgres:16.15`, `redis:7.4.11-alpine` and `nginx:1.30.5` exist |
    | JDK image | `eclipse-temurin` 25.0.4.1_1, a patch release within the Java 25 line, allowed by the baseline's patch rule (§8) |
    | TypeScript | npm `latest` is 7.0.2. The baseline stays on 6.0.x for the reasons in its §5.3 |

  - Departures from the specification's stated versions, listed as D-01 to D-05 in the departures table:
    - Java 17 → 25;
    - Spring Boot 3.3.x → 4.1.x;
    - Next.js 14 → 16.3.x;
    - Python 3.11 → 3.14 (optional track);
    - `nginx:1.25` → `nginx:1.30.5`.
  - PostgreSQL 16 and Redis 7 keep the specification's major versions, pinned to 16.15 and 7.4.11.
  - The approval covers the Redis 7.4 line as recommended. The licensing note in baseline §6 is acknowledged. This log draws no legal conclusion.
- **Rationale:**
  - The versions the specification states are outside support: Spring Boot 3.3.x OSS support ended 2025-06-30, Next.js 14 is unsupported, and nginx 1.25 is not listed.
  - The recommended set is supported beyond 2027 and was checked as a compatible whole (baseline §4).
- **Alternatives Considered:**
  - Keep the specification's versions: unsupported.
  - Java 21: supported, but Spring recommends JDK 25+ for Framework 7.
  - Spring Boot 4.0.x: OSS support ends 2026-12-31, inside the project horizon.
- **Consequences:**
  - `CURRENT_TECHNOLOGY_BASELINE.md` becomes AUTHORITATIVE (SSOT index area 8).
  - CLAUDE.md §J's precondition for scaffolding (an approved baseline) is met. Each scaffolding task still verifies current documentation (CLAUDE.md rules 5–6).
  - Tools the baseline does not name stay deferred (ADR-0034), except where another entry decides them: the JWT support in ADR-0016 and the government HTTP client in ADR-0011.
  - Upgrading the workstation (Java 25, Node 24) is a separate human action.
- **Source / Evidence:**
  - `CURRENT_TECHNOLOGY_BASELINE.md` §3–§8.
  - Registry checks on 2026-10-03:
    - Maven Central metadata for `org.springframework.boot:spring-boot` and `org.apache.maven:apache-maven`;
    - npm dist-tags for `next`, `react` and `typescript`;
    - `nodejs.org/dist/index.json`;
    - Docker Hub tags for `postgres`, `redis`, `nginx` and `eclipse-temurin`.
- **Approval:** Project owner, 2026-10-03, conversation ("The human approves the recommended technology baseline.").

### ADR-0003 — Backend build tool: Maven 3.10.x with the Maven Wrapper

- **Decision ID:** ADR-0003
- **Date:** 2026-10-03
- **Status:** DECIDED
- **Title:** Backend build tool: Maven 3.10.x with the Maven Wrapper
- **Context:** RD-013. Spring Boot 4.1.1 supports Maven 3.6.3+ and Gradle 8.14+/9.x. Java 25 needs Gradle 9.1.0+. Maven 4 is release-candidate only and "NOT safe for production use" (baseline §3, §7).
- **Decision:**
  - The backend builds with Maven 3.10.x, pinned through the Maven Wrapper (`mvnw`) committed with the backend.
  - Dependency versions come from Spring Boot's dependency management (baseline rule 5), and the Spring Boot Maven plugin builds the executable artifact.
  - Maven 4 is not used.
- **Rationale:**
  - Maven's declarative build is the conventional default in Spring Boot's own documentation, and it has the smallest configuration surface for a student team.
  - It adds no version constraint with Java 25.
  - The installed Maven 3.9.11 already exceeds Boot's minimum, and the wrapper pins one team-wide version.
- **Alternatives Considered:** Gradle 9.x offers faster incremental builds and a programmable DSL. It adds a Java 25 minimum (9.1.0) and more build logic to maintain, and no AgriNex requirement needs its flexibility.
- **Consequences:** `backend/pom.xml` plus the wrapper. The backend Dockerfile and CI call `./mvnw`. The baseline's build-tool row now reads Maven 3.10.x.
- **Source / Evidence:** `CURRENT_TECHNOLOGY_BASELINE.md` §3, §4, §7; Maven download page (3.10.0, "recommended version for all users"); Maven Central metadata (2026-10-03).
- **Approval:** Decided under ADR-0001.

### ADR-0004 — Supported browser floor for farmer-facing screens

- **Decision ID:** ADR-0004
- **Date:** 2026-10-03
- **Status:** DECIDED
- **Title:** Supported browser floor for farmer-facing screens
- **Context:**
  - RD-014. Next.js 16 supports Chrome/Edge/Firefox 111+ and Safari 16.4+, and Tailwind CSS 4 needs Firefox 128+.
  - Persona 2 uses an "Older Android 4.7"" device (§6). The 4.7" is a screen size; no OS or browser version is defined.
- **Decision:**
  - The supported floor is Chrome/Edge 111+, Safari 16.4+ and Firefox 128+. Tailwind CSS 4.3.x stays.
  - Devices on Android 7.0 or later receive Chrome at or above that floor:
    - Chrome 119 was the last release for Android 7;
    - Chrome 138 was the last release for Android 8 and 9;
    - Chrome 139 requires Android 10.
  - Devices limited to Android 6.0 or older are not supported.
- **Rationale:**
  - Next.js 16 is the only supported Next.js line. Supporting pre-2016 Android browsers would need polyfills and Tailwind CSS 3.4, whose last release was 2025-12-10. That costs maintenance and security for a small, shrinking group of devices.
  - Every Android version from 7.0 onward meets the floor.
- **Alternatives Considered:** Require older browsers. This means Next.js browser targeting, polyfills and Tailwind 3.4, which reopens the baseline.
- **Consequences:**
  - Mobile QA uses Chrome on Android at the 375px viewport (§48).
  - The UI/UX specification (role file 02) states the floor.
- **Source / Evidence:**
  - Next.js installation documentation ("Supported browsers") and the Tailwind CSS compatibility page (baseline §3).
  - Chromium PSAs on chromium-dev:
    - "Android Nougat is deprecated in the Chromium code base": M119 is the last release for Android 7; M120 requires Android 8;
    - "Android 8 Oreo and 9 Pie are deprecated in the Chromium code base": "Chrome version 138 is the last release that will support Android Oreo and Android Pie", and version 139 sets the minimum to Android 10.
- **Approval:** Decided under ADR-0001.

### ADR-0005 — Government data source, evidence hierarchy and outage behavior

- **Decision ID:** ADR-0005
- **Date:** 2026-10-03
- **Status:** DECIDED
- **Title:** Government data source, evidence hierarchy and outage behavior
- **Context:**
  - The specification names two data.gov.in resources (§1, §14):
    - current daily prices, `9ef84268-d588-465a-a308-a864a43d0070`;
    - historical variety-wise prices, `35985678-0d79-46b4-9ed6-6f13308a1d24`.
  - On 2026-10-03 `www.data.gov.in` answered HTTP 403, and the owner reports the portal as under maintenance.
  - The owner supplied seven CSV files in `docs/data/raw/`.
  - VERIFIED, from the data.gov.in state catalog page (`karnataka.data.gov.in`, fetched 2026-10-03):
    - the current-price catalog is published by the "Directorate of Marketing and Inspection (DMI)";
    - its information is generated through the AGMARKNET portal (`agmarknet.gov.in`);
    - it was last updated 2026-09-25.
  - VERIFIED (observed 2026-10-03): Agmarknet 2.0's Daily Price and Arrival Report and National Agriculture Market Atlas pages request a captcha before serving data. No documented API or terms for automated access were found.
- **Decision:**
  1. **Ingestion source.** The two data.gov.in resources above are the only sources AgriNex ingests prices from. Maintenance is temporary unavailability, not abandonment. Replacing or adding a source needs a new entry.
  2. **Corroborating official evidence.** These are official evidence for units, vocabularies and naming, never an ingestion source:
     - the Agmarknet 2.0 portal and its report exports: the five "Commodity-wise, Market-wise Daily Report (Weighted Average)" files and the "Marketwise Price & Arrival Report";
     - the Agmarknet 2.0 commodity, district and market masters.

     AgriNex never scrapes Agmarknet, and its captcha is never bypassed.
  3. **Non-government evidence.** `Agriculture_price_dataset.csv` has unknown provenance: no publisher, no unit field, inconsistent state spellings, and dates from 2023-06-06 to 2025-06-11. It is validation evidence only. It may be used offline to test validation rules. It is never loaded into `price_observations`, never shown to users, and never used as ML training data.
  4. **Accepting a response.** Only an HTTP 200 JSON body with the expected structure (a records array and a total) is accepted. Any other response is a page failure, logged with its HTTP status or error code:
     - HTTP 403 or 5xx;
     - timeouts;
     - an HTML maintenance page;
     - non-JSON content;
     - a missing records array.

     A failure is never read as "zero records", and it never deletes, zeroes or modifies stored data.
  5. **During an outage.**
     - Stored observations keep being served with their real report dates and freshness labels.
     - The §29 message is shown while the latest completed current-dataset run is FAILED.
     - There is no automatic switch to another source, no carry-forward of a price to a new date, no interpolation, and no relabelling of old data as current (§31 RULES 4–5).
  6. **Recovery.**
     - The next scheduled run fetches again.
     - An admin may trigger a run.
     - Dates missed during an outage are backfilled from the historical resource, which supports per-date filters (ADR-0011).
- **Rationale:**
  - Keeps the specification's data-source policy (§31 RULE 1, §39) and the owner's integrity rules.
  - The official Agmarknet material strengthens verification without creating an unverified integration.
- **Alternatives Considered:**
  - Ingest from Agmarknet 2.0 during the outage: an unofficial integration behind a captcha, with no API terms. Rejected.
  - Load the Agriculture dataset as history: unknown provenance, so it would present unofficial data as government data. Rejected.
- **Consequences:**
  - The ingestion parser treats non-JSON and unexpected structures as SCHEMA_ERROR page failures (ADR-0011).
  - Admin health shows per-dataset source status.
  - The §48 government-API failure test applies to every failure class above.
- **Source / Evidence:**
  - Spec §1, §14, §29, §31, §37 ("Government API outage … Show stored data with STALE banner"; "schema change … Fail ingestion loudly; expose in admin"), §39.
  - The `karnataka.data.gov.in` catalog page and an observation of `agmarknet.gov.in`, both 2026-10-03.
  - CSV profile: 737,392 rows, 29 state spellings, dates 2023-06-06 to 2025-06-11.
- **Approval:** Decided under ADR-0001.

### ADR-0006 — Phase 1 coverage and reference vocabularies

- **Decision ID:** ADR-0006
- **Date:** 2026-10-03
- **Status:** DECIDED
- **Title:** Phase 1 coverage and reference vocabularies
- **Context:**
  - RD-003 and RD-031.
  - The Task 7 instruction sets the Phase 1 database geography to Maharashtra statewide with broad commodity coverage: "The authoritative government source determines what commodities are available."
  - §14 validates "commodity in known list". It sets no known-list check for varieties or grades.
  - VERIFIED: the Agmarknet 2.0 commodity master (DMI, public filter data retrieved 2026-10-03) lists 605 commodities in 16 commodity groups.
  - All 52 Maharashtra commodity values in the verified data.gov.in captures match master names exactly. So do all 11 Maharashtra commodity headings in the official report exports, once the report-only "(MSP: …)" annotation is removed.
- **Decision:**
  1. **Coverage.**
     - Phase 1 ingests all commodities for the source state value `Maharashtra` from both resources.
     - Records for any other state are counted in `records_out_of_scope` and not stored.
     - Coverage is deployment configuration. Adding a state needs a new entry and that state's reference data (state, districts with centroids, district aliases), but no code change (§27, §47 Q20).
  2. **Commodity known list.**
     - The known list is the `commodities` table, seeded from the official Agmarknet 2.0 commodity master.
     - Names are stored normalized: leading and trailing whitespace trimmed, and internal runs of whitespace collapsed to one space. Two master names carry a trailing space.
     - A record whose commodity is not in the list is rejected as VALIDATION / `UNKNOWN_COMMODITY`, with its raw payload kept.
     - An admin may add a commodity only with an official source reference (ADR-0020).
  3. **Varieties and grades.**
     - Registered automatically, per commodity, on first appearance, using the exact normalized source value.
     - Never merged, renamed or case-folded. Values such as `Other`, `FAQ` and `Local` are kept literally.
  4. **`commodities.category`.** The official Agmarknet commodity-group name of the commodity (16 groups, for example `Vegetables`, `Cereals`, `Pulses`). NULL only when the master gives none.
  5. **`states.code`.** The ISO 3166-2 subdivision code: `IN-MH` for Maharashtra. The LGD state code is stored separately (ADR-0014).
  6. **`source_datasets.dataset_type`.** `CURRENT_PRICE` (§45) for the current resource and `HISTORICAL_PRICE` for the historical resource. The manual ingestion trigger accepts both values.
- **Rationale:**
  - The master is the publisher's own commodity universe, and the exact name matches show that the data.gov.in feed uses it.
  - Seeding from it honors §14's known-list check without rejecting legitimate commodities. A hand-picked list would invent scope; open auto-registration would make the check meaningless.
- **Alternatives Considered:**
  - Seed only the 52 observed values. Seasonal commodities (cotton, soybean) would be rejected.
  - Register unknown commodities automatically. This defeats the §14 check.
  - Use LGD numeric state codes as `states.code`. These are kept as a separate column instead.
- **Consequences:**
  - The seed records the master's retrieval date.
  - The farmer commodity selector lists commodities that have recent observations in coverage, not all 605.
  - Unknown commodities appear on the admin dashboard.
  - The dataset-type values become part of the trigger contract (Task 8).
- **Source / Evidence:**
  - Spec §14 (line 649), §15, §20 (lines 856, 886, 918), §27, §38, §45 (line 1925), §47 Q20.
  - Agmarknet 2.0 filter data, retrieved 2026-10-03: 605 commodities, 16 groups, 2,154 varieties, 18 grades.
  - Evidence file Appendix A (52 Maharashtra commodity values).
  - Report exports in `docs/data/raw/`.
  - ISO 3166-2:IN (IN-MH).
- **Approval:** Decided under ADR-0001.

### ADR-0007 — Source date formats and the reference time zone

- **Decision ID:** ADR-0007
- **Date:** 2026-10-03
- **Status:** DECIDED
- **Title:** Source date formats and the reference time zone
- **Context:**
  - RD-004 and RD-036.
  - §15 says "Parse date (dd-MM-yyyy or MM/dd/yyyy)". Every captured record in both resources is dated `dd/MM/yyyy` (evidence §19, Appendix A), and the historical metadata declares the `Arrival_Date` filter as `dd-MM-yyyy`.
  - Accepting both day-first and month-first dates makes every date with a day of 12 or less ambiguous. In the Agriculture CSV, 297,181 of 737,392 rows are ambiguous in that way.
- **Decision:**
  1. **Record dates** (`arrival_date` / `Arrival_Date`) are parsed strictly as `dd/MM/yyyy` in both resources. Any other format is rejected as VALIDATION / `DATE_FORMAT_INVALID`. This is departure D-06.
  2. **The historical request filter** is sent as `dd-MM-yyyy`, as the resource metadata declares.
  3. **Reference time zone.** `Asia/Kolkata` (IST) defines "today" and every calendar window: freshness classification, trend windows, alert windows and future-date validation.
     - The application computes "today" explicitly in IST and never relies on the server's default time zone.
     - `report_date` is stored as a time-zone-free calendar date.
     - Instants (`ingested_at`, `created_at` and similar) are stored as `timestamptz`.
  4. **Future dates.** A record with a `report_date` after today (IST) is rejected as VALIDATION / `DATE_IN_FUTURE`.
- **Rationale:**
  - The parser follows observed source behavior, which the evidence file verifies.
  - IST matches the Indian market calendar. Without it, freshness would shift by a day between 00:00 and 05:30 IST on a UTC server, breaking §48's 100% freshness accuracy.
- **Alternatives Considered:**
  - Accept the §15 formats: they match no captured record and introduce ambiguity.
  - Use UTC dates: wrong for five and a half hours every day.
- **Consequences:**
  - Unit tests cover boundary cases: dates 01–12, month ends, and IST midnight.
  - Every date-window rule in ADR-0010, ADR-0019 and ADR-0025 uses IST.
- **Source / Evidence:**
  - Spec §1 (line 98: "dated 01/08/2026 through 13/09/2026"), §14 (line 647), §15 (line 689), §16 (line 737), §33, §48 (freshness accuracy).
  - Evidence §19.1–§19.2, §19.7, Appendix A.
  - CSV profile of the date strings.
- **Approval:** Decided under ADR-0001.

### ADR-0008 — Price unit, numeric representation and unit-consistency validation

- **Decision ID:** ADR-0008
- **Date:** 2026-10-03
- **Status:** DECIDED
- **Title:** Price unit, numeric representation and unit-consistency validation
- **Context:**
  - RD-027. §31 RULE 3: the unit "must be verified … and stored on every price_observation. No silent unit assumptions." §37: "Data inconsistency (units, names) … Reject record, log validation error."
  - The data.gov.in payloads have no unit field (evidence §0.8). The owner's instruction forbids inventing a JSON unit field and forbids silent conversion.
  - VERIFIED evidence of the unit:
    - **Official report exports** (Agmarknet 2.0, DMI, report date 01-Oct-2026):
      - all 2,732 market rows state "Unit of Price" = `Rs./Quintal` and "Unit of Arrivals" = `Metric Tonnes`;
      - they cover 41 commodity headings in 25 states, including 295 Maharashtra rows across 11 commodities;
      - the Marketwise report header reads "Price (Rs./Quintal)".
    - **Agmarknet 2.0 Market-wise dashboard** (retrieved 2026-10-03): the column group "Price (Rs./Quintal)" covers the 27 commodities in its filter list, 25 of which had data that day, including Onion, Potato and Tomato in the Vegetables group.
    - **Per-row unit column.** The official report carries "Unit of Price" on each row, and the 605-commodity master includes livestock, eggs, flowers and wood. So the unit is not proven uniform for every commodity.
  - VERIFIED anomalies:
    - the captures show per-kg-like magnitudes: `Onion Green` 4/12/20 at `APMC Pune`, and `Methi(Leaves)` 10/13/15 at `Pune(Moshi)` (evidence Appendix A);
    - the Agriculture CSV has 1,498 rows with 0 < modal < 100, mostly Potato and Onion in per-kg ranges;
    - none of the 2,732 official rows has a modal below 600.
  - VERIFIED relative spread: in the official exports, 0 of 2,606 rows in the 72 commodity-and-state groups with five or more rows lie outside 0.1×–10× of the group median modal. In the Agriculture CSV, 241 of 710,842 rows do. Examples are a decimal shift (`Akola` onion 15,000 vs a median of 1,150) and per-kg values (`Khed(Chakan)` potato 15 vs a median of 1,500).
- **Decision:**
  1. **The unit is a per-commodity, evidence-backed attribute.**
     - `commodities.price_unit` holds the unit in the source's own notation (`Rs./Quintal`).
     - `commodities.price_unit_source` records the official evidence: the Agmarknet report export or dashboard, with its date.
     - The two columns are set together or not at all.
     - The seed sets the unit from the evidence above. Every other commodity is verified by exporting the official Daily Price and Arrival Report for its commodity group during seed preparation. A commodity without such evidence stays unverified.
  2. **Ingestion stores an observation only when its commodity has a verified unit.**
     - `price_observations.price_unit` is copied from the commodity at load time, so the stored row keeps its unit.
     - A record whose commodity has no verified unit is rejected as VALIDATION / `UNIT_UNVERIFIED`.
     - No JSON unit field is assumed or invented.
  3. **No conversion, ever.** AgriNex never multiplies or divides a source price. The farmer's quantity is converted to quintals per §32; source prices are not.
  4. **Numeric representation.**
     - Source price text is parsed as an exact decimal, never through binary floating point.
     - It is rounded half-up to two decimal places, so `3963.4099999999999` becomes 3963.41, and stored as `numeric(14,2)`.
     - Monetary calculations use exact decimal arithmetic and round half-up to two decimal places.
  5. **Validation rules.** Each failure rejects the record (error type VALIDATION) with the code shown; the raw payload is kept in `ingestion_errors`. The rules are applied in this order before load:

     | Rule | Rejected when | Error code |
     |---|---|---|
     | V1 | State, district, market, commodity, arrival date or modal price is missing or blank | `REQUIRED_FIELD_MISSING` |
     | V2 | The date is not `dd/MM/yyyy`, or lies in the future (ADR-0007) | `DATE_FORMAT_INVALID`, `DATE_IN_FUTURE` |
     | V3 | The commodity is unknown (ADR-0006) or has no verified unit | `UNKNOWN_COMMODITY`, `UNIT_UNVERIFIED` |
     | V4 | The district is unresolvable (ADR-0014) | `UNKNOWN_DISTRICT` |
     | V5 | A present price is not a decimal number, or a present price is ≤ 0. An absent min or max price is stored as NULL; a zero is never read as "not reported" | `PRICE_NOT_NUMERIC`, `PRICE_NON_POSITIVE` |
     | V6 | min > modal, or modal > max | `PRICE_RANGE_INVALID` |
     | V7 | The unit is `Rs./Quintal` and modal < 100.00 (below ₹1/kg) | `PRICE_BELOW_UNIT_FLOOR` |
     | V8 | Within one delivery unit (ADR-0011), for each commodity and report date with at least five records that passed V1–V7, the modal is more than 10× or less than 0.1× the group's median modal | `PRICE_OUTLIER` |

- **Rationale:**
  - Rules 1–3 meet RULE 3 with official evidence, and they never label a commodity whose unit was not shown, such as livestock or eggs.
  - The validation rules turn §37's "reject record" into deterministic checks fitted to the evidence:
    - V7 catches per-kg entries in small groups;
    - V8 catches decimal shifts and per-kg entries wherever five or more peers exist, and no official row falls outside its bounds.
  - Rejection with the raw payload kept is never silent.
- **Alternatives Considered:**
  - One dataset-level unit (`Rs./Quintal` for every commodity). Simpler, but it would silently label commodities with no unit evidence.
  - Convert suspected per-kg rows by multiplying by 100. This is forbidden silent conversion.
  - Keep suspect rows with a quality flag. This needs a flag on every price query, and a missed flag would mislead farmers.
- **Consequences:**
  - The schema gains `commodities.price_unit` and `commodities.price_unit_source`, and `ingestion_errors` gains `error_code`.
  - Comparison and alerts accept only commodities whose unit is `Rs./Quintal`, because §32's gross-value formula is per quintal. Other units are rejected with 422 `UNSUPPORTED_PRICE_UNIT` (ADR-0023).
  - Every price in an API response carries its unit (ADR-0022).
  - Seed preparation must export the official reports for the remaining commodity groups.
- **Source / Evidence:**
  - Spec §1 (line 100), §10 (line 458), §14, §15 step 7, §31 RULE 3, §32, §37.
  - Evidence §0.8, Appendix A (C-314-P0, C-19-MH-ONION, H-07SEP-2026, C-647-P0).
  - `docs/data/raw/` exports.
  - Agmarknet 2.0 dashboard, 2026-10-03.
  - Profiles of the files in `docs/data/raw/`, measured 2026-10-03. The results are recorded in `docs/database/DATABASE_ARCHITECTURE.md` (the CSV evidence section).
- **Approval:** Decided under ADR-0001.

### ADR-0009 — Observation identity, duplicates, conflicting records and revisions

- **Decision ID:** ADR-0009
- **Date:** 2026-10-03
- **Status:** DECIDED
- **Title:** Observation identity, duplicates, conflicting records and revisions
- **Context:**
  - RD-005 and RD-028. The specification describes both update and skip for the same case:
    - §14 "ON CONFLICT (business_key) DO UPDATE if newer ingestion_run";
    - §15 "DO UPDATE SET ingested_at=NOW() (if same price, still update timestamp)" and, right after it, "Increment duplicates_skipped / Skip insert".
  - VERIFIED:
    - H-07SEP-2026 contains four same-key pairs with different prices (evidence Appendix A.2);
    - H-01AUG-2026-P0 contains the same observation under two market spellings (A.3);
    - PostgreSQL rejects an `INSERT … ON CONFLICT DO UPDATE` that would affect the same row twice in one statement.
- **Decision:**
  1. **Identity.** An observation's identity is source dataset + market + commodity + variety + grade + report date, after resolution:
     - the market is resolved through its alias (ADR-0014);
     - variety and grade are NULL when the source value is absent.

     `business_key` is the lowercase hex SHA-256 of the canonical identity built from the resolved IDs, as designed in Task 7. The null-safe identity unique constraint enforces the same identity independently.
  2. **Within one delivery unit (ADR-0011), before load:**
     - Records with the same identity and identical min, modal and max prices produce one candidate. Each extra copy, including alias twins, counts in `duplicates_skipped`.
     - Records with the same identity but different prices produce no stored observation. Each variant is logged as error type CONFLICT, code `SAME_KEY_DIFFERENT_PRICES`, with its raw payload, and counted in `records_conflicted`. A later delivery that carries one consistent value is stored normally.
  3. **Against stored observations:**
     - **Identical prices:** nothing is written, and the record counts in `duplicates_skipped`. This is departure D-07.
     - **Different prices:** the newer run wins (§14). A `price_observation_revisions` row first records the previous min, modal and max prices and the previous `ingested_at`, together with the revising run. The observation's prices and `ingested_at` are then updated, and the record counts in `records_updated`.
     - `price_unit` never changes on a revision.
  4. **Column meaning.**
     - `price_observations.ingestion_run_id` is the run that inserted the row. It never changes.
     - `ingested_at` is when the current price values were written, by the insert or by the latest revision.
  5. **Batch upserts** receive only deduplicated, conflict-free candidates, so no statement touches a row twice.
- **Rationale:**
  - No source value is silently overwritten (every change leaves a revision), and no arbitrary choice is made between conflicting records.
  - Re-ingesting unchanged data writes nothing, which meets §48's "0 new duplicate rows" without rewriting thousands of rows every six hours.
  - Every counter except `duplicates_skipped` can be checked against database rows (ADR-0012).
- **Alternatives Considered:**
  - Keep the first or last conflicting record: page order is not guaranteed, so the result is not deterministic.
  - Average conflicting prices: a synthetic price, which is forbidden (P10).
  - Extend the identity: the source offers no distinguishing field.
  - Never update stored rows: this ignores source revisions (§49 says prices may be "revised").
- **Consequences:**
  - New table `price_observation_revisions`, owned by the price module.
  - New error type CONFLICT and new counter `ingestion_runs.records_conflicted`.
  - Tests cover each path: duplicate, alias twin, conflict, revision and unchanged re-delivery.
- **Source / Evidence:**
  - Spec §14 (line 651), §15 (lines 691–697), §22 (line 1120), §27, §31 RULE 6, §37, §47 Q8, §48, §49.
  - Evidence Appendix A.2–A.3.
  - PostgreSQL 16 `INSERT` documentation (cardinality violation).
- **Approval:** Decided under ADR-0001.

### ADR-0010 — Observation selection for comparison, trends and alerts

- **Decision ID:** ADR-0010
- **Date:** 2026-10-03
- **Status:** DECIDED
- **Title:** Observation selection for comparison, trends and alerts
- **Context:**
  - RD-015 (CONFLICT C-01).
  - §1 assigns roles to the two datasets, but the selection queries in §16 and §33 filter by neither dataset nor variety or grade.
  - VERIFIED: one market, commodity and date carries several varieties, for example `APMC Shevgaon` Onion on 07/09/2026 with `2nd Sort`, `1st Sort` and `Other`.
- **Decision:**
  1. **Candidates come from both datasets.** For the same market, commodity, variety, grade and report date, the current-dataset row takes precedence and the historical row is ignored. Identities present in only one dataset are kept. This is departure D-08.
  2. **Window.** A market's observation date is the latest `report_date` that satisfies all of:
     - it is no later than today (IST);
     - it is no earlier than today − `freshness_stale_days`;
     - it has at least one candidate after the variety and grade filter.
  3. **Variety and grade.**
     - When the farmer selects a variety, a grade or both, only exact matches qualify.
     - A market with no exact match in the window is excluded with `NO_RECENT_PRICE_DATA`. No other variety is substituted.
  4. **Representative observation** when no variety or grade is selected. Sort the candidates on the chosen date by:
     1. modal price, ascending;
     2. variety name, ascending (NULL first);
     3. grade name, ascending (NULL first).

     Then take the element at position ⌊(n−1)/2⌋, the lower median. It is always a real observation.
  5. **Disclosure.** Every selected price is returned with its observation ID, variety, grade, report date, source dataset and unit (ADR-0022).
  6. **No synthesis.** Prices are never averaged or blended across varieties, grades, markets or datasets to produce a displayed price. Averages appear only inside the §33 trend statistic, which is labelled as such.
  7. **Daily series** for trends (ADR-0025), percentage alerts (ADR-0019) and optional ML input (ADR-0030): one value per date, chosen by rules 1, 3 and 4 applied to that date.
  8. **Alerts** use the same selection for `latestModalPrice` (ADR-0019).
- **Rationale:**
  - The selection is deterministic, which §27 and §48 testing need, and it never fabricates a price.
  - It keeps comparison working when only historical rows exist (for example after bootstrap or during a current-resource outage), and it shows provenance instead of hiding it.
  - The lower median avoids the optimism of choosing the highest variety price.
- **Alternatives Considered:**
  - Strict dataset roles (comparison from current only, trends from historical only): comparisons would fail after bootstrap, and trends would miss the latest dates.
  - Highest modal: overstates realizable prices.
  - Arrival-weighted average: the API carries no arrivals, and the result would be synthetic.
  - Substitute another variety: silent substitution.
- **Consequences:** Comparison, trend and alert implementations share one selection component in the price module. Unit tests cover precedence, the lower median for even counts, every tie-break and an exact-filter miss.
- **Source / Evidence:**
  - Spec §1 (lines 91–92), F1.4, F1.7, §16 (lines 715, 722, 737), §33 (lines 1469–1480), §34.
  - Evidence Appendix A (C-19-MH-ONION, H-07SEP-2026).
  - `docs/database/DATA_DICTIONARY.md` (`selling_searches.variety_id`, `grade_id`).
- **Approval:** Decided under ADR-0001.

### ADR-0011 — Ingestion run orchestration

- **Decision ID:** ADR-0011
- **Date:** 2026-10-03
- **Status:** DECIDED
- **Title:** Ingestion run orchestration
- **Context:**
  - RD-016. The specification gives:
    - a 6-hour cadence (§10, §15);
    - `triggeredBy=SCHEDULER|MANUAL|STARTUP` (§15);
    - a bootstrap of at least 60 days (§10);
    - "Exponential backoff, max 3 retries" (§22);
    - partial progress kept (§29);
    - "Resume from last successful offset" (§37);
    - `scripts/bootstrap_historical.sh` (§43).
  - It leaves the historical cadence, bootstrap trigger, overlap handling, resume state and status rules open.
- **Decision:**
  1. **Schedule.**
     - Every 6 hours at 00:00, 06:00, 12:00 and 18:00 IST, as two separate runs:
       - first, a CURRENT_PRICE run;
       - then a HISTORICAL_PRICE run over the trailing window of report dates today − 3 to today (IST).
     - Paused datasets are skipped (ADR-0020).
  2. **Startup.** At application start, a STARTUP CURRENT_PRICE run begins if no CURRENT_PRICE run ended SUCCESS or PARTIAL_FAILURE in the previous 6 hours.
  3. **Manual trigger** (ADR-0020):
     - `datasetType` is CURRENT_PRICE or HISTORICAL_PRICE;
     - for HISTORICAL_PRICE, an optional `fromDate`/`toDate` range of at most 366 days, defaulting to the trailing window;
     - the bootstrap (at least 60 days, §10) and outage backfills use this trigger;
     - `scripts/bootstrap_historical.sh` is a thin wrapper that calls it with credentials supplied at run time, never stored.
  4. **Overlap.**
     - At most one RUNNING run per source dataset, enforced by a partial unique index.
     - A scheduler tick for a dataset that is running is skipped and logged.
     - A manual trigger answers 409 CONFLICT.
     - Runs of different datasets may overlap.
  5. **Interrupted runs.** At startup, any run still RUNNING is set to FAILED with error type SYSTEM, code `RUN_INTERRUPTED`.
  6. **Fetching.**
     - The government API is called server-side with Spring Framework's `RestClient` (spring-web, part of the approved baseline; no new dependency).
     - The API key is sent as the source requires and never logged; logged URLs have the key redacted.
     - Pagination uses `offset` and `limit` (§31 RULE 2). The page size is configuration, defaulting to 500 (§15). The offset advances by the number of records actually returned. Fetching stops when the reported total is reached or a page returns no records.
     - Each page is retried up to 3 times with exponential backoff (§22).
     - **Delivery unit.** For CURRENT_PRICE, a failed page ends fetching for the run. For HISTORICAL_PRICE, each report date is its own delivery unit, and a failed date does not stop later dates.
  7. **Processing.**
     - Each delivery unit is fetched first. It is then validated (ADR-0008) and deduplicated and conflict-checked (ADR-0009).
     - It is loaded in one transaction per delivery unit, and units already committed are never rolled back (§29).
     - There is no persisted resume state. Every run starts at offset 0, and idempotent loading makes re-fetching safe. "Resume from last successful offset" (§37) is met inside a run, where a retry resumes at the failed offset.
  8. **Run status:**
     - **SUCCESS:** every page of every delivery unit was fetched. Record-level rejections do not affect status.
     - **PARTIAL_FAILURE:** at least one unit loaded, and at least one page or unit failed after retries.
     - **FAILED:** nothing was fetched (source unavailable, authentication failure or invalid structure), a load transaction failed, or the run was interrupted. This reconciles §15's "Mark run status=FAILED" with §22's retries and §29's partial progress.
  9. **Error types:** VALIDATION, API_ERROR, CONFLICT, SCHEMA_ERROR, SYSTEM.
     - Each `ingestion_errors` row also carries an UPPER_SNAKE `error_code`, for example `HTTP_STATUS`, `TIMEOUT`, `NON_JSON_RESPONSE`, `UNEXPECTED_STRUCTURE`, `LOAD_FAILED`, the ADR-0008 codes and `RUN_INTERRUPTED`.
     - HISTORICAL failures record the requested report date.
  10. **After commit.** Every run that inserted or updated at least one observation invokes alert evaluation (ADR-0019).
- **Rationale:**
  - One schedule is simple, and the trailing window picks up late historical publications and revisions at small cost.
  - Per-unit two-phase processing is needed so that whole-unit checks (conflicts, the V8 outlier rule) see every record.
  - Stateless re-fetching avoids offsets that drift as the source changes.
- **Alternatives Considered:**
  - A separate daily historical schedule: a second cadence for little gain.
  - Persisted resume offsets: fragile with a changing source, and unnecessary given idempotent loading.
  - Allowing concurrent runs of one dataset: they would race on the same identities.
- **Consequences:**
  - Schema: a partial unique index on RUNNING runs, `ingestion_errors.error_code` and `request_report_date`, and the error-type vocabulary.
  - Admin trigger contract (Task 8): the dataset-type values, the date range and the 409 response.
  - Tests cover each status rule and the retry exhaustion paths.
- **Source / Evidence:** Spec §10 (lines 457, 462), §14 (line 647), §15 (lines 681–702), §22 (lines 1113–1131), §29 (line 1371), §31 RULE 2, §37 (line 1639), §43 (line 1853), §45 (line 1925), FR-02; evidence §19 (pagination fields `total`, `count`, `limit`, `offset`).
- **Approval:** Decided under ADR-0001.

### ADR-0012 — Ingestion completeness metric and audit counters

- **Decision ID:** ADR-0012
- **Date:** 2026-10-03
- **Status:** DECIDED
- **Title:** Ingestion completeness metric and audit counters
- **Context:**
  - RD-029. §48: "Count records fetched vs count in price_observations", at least 95% per run.
  - §48 also requires admin health counts to equal database counts exactly, and a repeated run to add 0 rows.
  - Correct deduplication collapses records, so a literal "rows inserted / records fetched" ratio fails on correct behavior.
- **Decision:**
  1. **Accounting invariant.** For runs ending SUCCESS or PARTIAL_FAILURE:

     `records_fetched = records_inserted + records_updated + duplicates_skipped + validation_errors_count + records_conflicted + records_out_of_scope`

     It is enforced by a CHECK constraint.
  2. **Completeness** = (`records_inserted` + `records_updated` + `duplicates_skipped`) ÷ (`records_fetched` − `records_out_of_scope`) × 100. It is not applicable when the denominator is 0.
     - It is the share of in-scope records whose values are stored in `price_observations` after the run.
     - Validation rejections and conflicts reduce it; legitimate collapses do not.
     - The §48 threshold is at least 95% per run.
  3. **Row-backed counters** are checked against the database:
     - `records_inserted` = count of `price_observations` with that `ingestion_run_id`;
     - `records_updated` = count of `price_observation_revisions` with that run;
     - `validation_errors_count` and `records_conflicted` = counts of that run's `ingestion_errors` of types VALIDATION and CONFLICT.

     `duplicates_skipped` and `records_out_of_scope` follow from the invariant.
  4. **Repeated-run test** (§48): a second run over unchanged source data gives `records_inserted` = 0 and `records_updated` = 0, so completeness is 100%.
  5. **Experiment 2 rates** are computed over in-scope records:
     - duplicate rate = `duplicates_skipped` ÷ in-scope records;
     - validation-error rate = `validation_errors_count` ÷ in-scope records;
     - sync success rate = SUCCESS runs ÷ completed scheduled runs, with PARTIAL_FAILURE reported separately.
- **Rationale:** The metric measures what §48 intends (in-scope data reaching the store), stays exact for the admin health test, and is not lowered by correct deduplication.
- **Alternatives Considered:** Inserted rows only: fails on alias twins and on repeated runs. Table totals: not run-specific.
- **Consequences:** New counter `records_conflicted` and the invariant CHECK. The admin health response exposes all counters and the completeness percentage (Task 8).
- **Source / Evidence:** Spec §20 `INGESTION_RUNS`, §36 Experiment 2 (lines 1605–1612), §48 (lines 2097–2098, 2104); evidence Appendix A.3 (alias twins).
- **Approval:** Decided under ADR-0001.

### ADR-0013 — Module data ownership and cross-module access

- **Decision ID:** ADR-0013
- **Date:** 2026-10-03
- **Status:** DECIDED. Item 3 amended by ADR-0039 (2026-10-04), which adds the recommendation → geography dependency
- **Title:** Module data ownership and cross-module access
- **Context:**
  - RD-017 (CONFLICT C-02). §27: "No cross-module direct DB access; each module owns its repositories."
  - `docs/database/TABLE_OWNERSHIP.md` proposed one owner per table and interface-only access.
- **Decision:**
  1. **The `TABLE_OWNERSHIP.md` proposal is adopted:**
     - exactly one owner per table;
     - only the owner's code reads or writes the table;
     - cross-module foreign keys are allowed as integrity constraints;
     - no joins or shared repositories across ownership boundaries;
     - results are composed in code;
     - Flyway, seeds and offline evaluation are tooling, under the limits in its section 7.
  2. **Owners of the tables added by this lock:**
     - `price_observation_revisions`: price;
     - `district_source_aliases`: geography;
     - `ingestion_source_settings`: ingestion;
     - `admin_audit_events`: admin.

     `forecasts` belongs to price (ADR-0030).
  3. **Approved Phase 1 module dependencies.** "A → B" means A calls B's interface.
     - The four in §13 stay: recommendation → transport, recommendation → price, ingestion → price, admin → ingestion.
     - New:
       - ingestion → commodity, ingestion → geography, ingestion → alert (post-run evaluation, invoked in its own transaction after the run commits);
       - alert → price, alert → commodity, alert → auth (the caller's farmer profile);
       - recommendation → commodity, recommendation → auth;
       - price → geography (district data for market registration and approximate coordinates), price → commodity (variety and grade names for disclosure);
       - admin → transport, admin → recommendation, admin → price, admin → commodity, admin → geography, admin → auth;
       - auth → geography (the farmer's default district).
     - The graph is acyclic. Geography, commodity and transport are leaves, and no module depends on admin.
  4. **Failure isolation.** A failure in post-run alert evaluation is logged as an `ingestion_errors` row (type SYSTEM, code `ALERT_EVALUATION_FAILED`). It does not change the run's status.
- **Rationale:** Keeps §27 literally, makes every cross-module path explicit and testable, and needs no shared tables or cycles.
- **Alternatives Considered:**
  - An event bus between modules: an extra mechanism, where direct interface calls give the same isolation in one process.
  - Ingestion writing alert tables directly: violates §27.
- **Consequences:**
  - `TABLE_OWNERSHIP.md` and `COMPONENT_ARCHITECTURE.md` record these dependencies.
  - Architecture tests (for example, package-dependency rules) can enforce them.
- **Source / Evidence:** Spec §13 (lines 619–622), §15, §22, §27 (line 1325), §30, §34, §47 Q11; `docs/database/TABLE_OWNERSHIP.md`.
- **Approval:** Decided under ADR-0001.

### ADR-0014 — Geographic reference data, coordinates, markets and aliases

- **Decision ID:** ADR-0014
- **Date:** 2026-10-03
- **Status:** DECIDED
- **Title:** Geographic reference data, coordinates, markets and aliases
- **Context:**
  - RD-018, RD-030 and RD-032. Neither government dataset carries coordinates (evidence §19.3). §16 explicitly allows market coordinates "derived from district centroid".
  - VERIFIED, at the level of search-indexed content: the Local Government Directory (LGD) and the official Maharashtra village directory (`mahavillages.mahabhumi.gov.in`) list 36 districts and 358 talukas. Examples of current names and LGD codes: Ahilyanagar 466, Chhatrapati Sambhajinagar 469, Dharashiv 488. The directory page itself refused the connection on 2026-10-03, so the seed task re-verifies every name and code against LGD.
  - VERIFIED: the Agmarknet 2.0 Maharashtra district master has 38 entries:
    - both `Osmanabad` and `Dharashiv`;
    - the renamed `Ahilyanagar` and `Chattrapati Sambhajinagar`;
    - the non-district entries `Bandra(E)` and `Murum`.
  - VERIFIED: the Agmarknet 2.0 Maharashtra market master has 429 markets, and 337 of the names have edge or double spaces. The captures show alias spellings such as `Pune(Moshi)` and `Pune(Moshi) APMC`.
  - VERIFIED: the Survey of India (SoI) Administrative Boundary Database publishes official state, district and sub-district boundaries. Registration is required.
- **Decision:**
  1. **States, districts and localities.**
     - LGD is the authority for names and codes. `states`, `districts` and `localities` store the LGD code (`lgd_code`), and names follow LGD.
     - A locality is an LGD sub-district (taluka). Localities carry no coordinates in Phase 1.
     - The wizard's locality step (F1.3) does not change the origin. Distance is always measured from the district centroid (FR-06), so no finer location is collected (§28).
  2. **District centroids.**
     - Computed once, during seed preparation, from SoI district boundary polygons, in WGS84.
     - The method is the geometric centroid. A point on the surface is used instead when the centroid falls outside the polygon.
     - `centroid_source` records the dataset, version and method.
     - If SoI data cannot be obtained, district seeding is blocked. No unofficial or estimated coordinates are used.
  3. **District source aliases.**
     - The new `district_source_aliases` table, owned by geography, maps each source state and district spelling to an LGD district, with a mandatory evidence note.
     - It is seeded from the Agmarknet district master, with an official reference for each mapping (for example the government notification of a rename).
     - A source district that does not resolve rejects the record as VALIDATION / `UNKNOWN_DISTRICT`.
  4. **Markets.**
     - **Registration.** A market spelling seen for the first time is registered automatically as a PROVISIONAL market, with:
       - its resolved district;
       - a name normalized by trimming and collapsing whitespace;
       - an alias row (raw spelling plus the evidence note "auto-registered by run …");
       - approximate coordinates equal to its district centroid (`is_coordinate_approximate = true`, with `coordinate_source` naming the centroid).
     - **What PROVISIONAL means.** Observations of PROVISIONAL markets are stored and kept. Those markets are excluded from comparison and alerts until an admin verifies them.
     - **Visibility.** Public market endpoints (market detail, trends) serve only VERIFIED markets. A PROVISIONAL or MERGED market answers 404 there, and is visible only through the ADMIN curation endpoints.
     - **Admin curation** (ADR-0020):
       - **VERIFY:** the market becomes a comparison candidate.
       - **MERGE:** declare the market the same as another market, with an evidence note.
         - Aliases are moved to the target, and observations are re-pointed under ADR-0009 identity rules. An identical copy is dropped; a conflicting copy keeps the target's value and the dropped values are recorded in the audit event.
         - The merged market keeps status MERGED, with a pointer to its target, and is inactive.
       - **SET COORDINATES:** exact coordinates are allowed only with an identified source, for example the DMI National Agriculture Market Atlas on Agmarknet 2.0, and they clear the approximate flag.
     - **Never automatic.** There is no fuzzy matching, automatic merging or geocoding service.
  5. **Comparison candidates** are VERIFIED, active markets with coordinates. Approximate distances are flagged in responses and in the UI (§16 `is_approximate`).
  6. **Slugs.** `canonical_slug` stays globally unique (§20 UK).
     - The slug is the lowercase canonical name, with every run of non-alphanumeric characters replaced by one hyphen.
     - On collision, append `-` plus the district slug, then, if needed, `-` plus the lowercase state code.
- **Rationale:**
  - Only official sources are used (LGD, SoI, DMI), and §16's centroid approximation keeps distances honest.
  - Automatic PROVISIONAL registration retains data without letting unreviewed markets reach farmers.
  - Merges happen only on evidence, which meets "never silently merge aliases".
- **Alternatives Considered:**
  - A geocoding API: an external dependency the specification excludes (§39), with licence limits on storing results.
  - Reject unknown markets: loses data and the completeness metric.
  - Fuzzy-match aliases: silent merging.
  - OpenStreetMap coordinates: not official. Not needed while §16's approximation is allowed.
- **Consequences:**
  - Schema:
    - `lgd_code` columns on states, districts and localities;
    - new table `district_source_aliases`;
    - `markets.review_status`, `merged_into_market_id` and `created_at`;
    - `market_source_aliases.evidence_note`.
  - Someone on the team must register on the SoI portal to obtain the boundaries.
- **Source / Evidence:**
  - Spec FR-06 (line 1284), F1.3, §16 (lines 717, 727), §20 (lines 858–880), §28 (line 1345), §39, §42 item 7.
  - Evidence §19.3 and Appendix A.6 (`Sirsa APMC` in two states).
  - LGD and the Maharashtra village directory (36 districts, 358 talukas).
  - SoI Administrative Boundary Database (`onlinemaps.surveyofindia.gov.in`).
  - Agmarknet 2.0 masters, retrieved 2026-10-03.
- **Approval:** Decided under ADR-0001.

### ADR-0015 — Localized reference names

- **Decision ID:** ADR-0015
- **Date:** 2026-10-03
- **Status:** DECIDED
- **Title:** Localized reference names
- **Context:**
  - RD-033. Farmer screens must be available in Marathi, Hindi and English (F1.2, §27).
  - §20 gives `name_hi` and `name_mr` only for states and commodities. Translations are reference data and need an identified source (CLAUDE.md §C).
- **Decision:**
  1. `name_hi` and `name_mr` for states and commodities are curated reference data. Each value comes either from an official publication or from a team translation reviewed by a named native speaker, and the seed records which.
  2. A missing translation falls back to the English source name. Display is never blocked, and there is no runtime machine translation.
  3. District, market, variety and grade names are shown as officially published. §20 defines no localized columns for them, and no source of reviewed translations exists for more than 400 markets and more than 2,000 varieties.
  4. Interface text (labels, messages, disclaimers) is fully localized in the frontend. §48's "0 untranslated hardcoded strings" applies to that text.
- **Rationale:** Meets F1.2 for everything AgriNex authors, without inventing translations of official names.
- **Alternatives Considered:**
  - Machine translation: unreviewed reference data.
  - Adding localized columns for every entity: no source exists for the values.
- **Consequences:**
  - Reference-data responses return `name` plus the nullable `nameHi` and `nameMr` (ADR-0022).
  - Role file 02 specifies how a fallback name is shown.
- **Source / Evidence:** Spec F1.2 (line 325), §20 (lines 854–855, 884–885), §27 (line 1321), §48 (line 2106); CLAUDE.md §C.
- **Approval:** Decided under ADR-0001.

### ADR-0016 — User accounts, passwords and token handling

- **Decision ID:** ADR-0016
- **Date:** 2026-10-03
- **Status:** DECIDED (items 1 and 3 amended by ADR-0047)
- **Title:** User accounts, passwords and token handling
- **Context:**
  - RD-034 (CONFLICT DB-C-8).
  - §28: BCrypt with cost ≥ 12, HS256 JWT with a 15-minute expiry, SHA-256-hashed refresh tokens with a 7-day TTL, and RBAC with FARMER, BUYER and ADMIN.
  - §27: "7-day refresh token rotation". §25: quick phone registration.
  - VERIFIED:
    - RFC 9700 §4.14.2 requires rotation or sender-constraining for public clients, and describes reuse detection that revokes the active token;
    - Spring Security's BCrypt encoder rejects passwords longer than 72 bytes after the CVE-2025-22228 fix;
    - NIST SP 800-63B-4 sets a 15-character minimum for single-factor passwords and recommends allowing at least 64;
    - Spring Security 7 decodes HS256 tokens with `NimbusJwtDecoder.withSecretKey(…)` and `macAlgorithm(…)`.
- **Decision:**
  1. **Identifiers.**
     - FARMER: an Indian mobile number in E.164 form (`+91` followed by 10 digits starting 6–9).
     - ADMIN: an email address stored in lower case.
     - Both are unique. A CHECK requires a phone for FARMER and an email for ADMIN.
     - BUYER identifiers follow the Phase 2 decisions.
  2. **Login** takes one `identifier` (phone or email, normalized) and a password.
  3. **Registration and passwords.**
     - Quick phone registration (§25) creates a FARMER from a phone number, a password, and an optional display name and district. There is no SMS or OTP in Phase 1 (§39).
     - ADMIN accounts are provisioned, never self-registered (ADR-0028).
     - Password length: FARMER 8–64 characters, ADMIN 15–64 characters, and never more than 72 bytes of UTF-8.
     - No composition rules.
     - Hashing: BCrypt with cost 12.
  4. **Access token.**
     - JWT HS256 with a 15-minute expiry and claims `sub` (user ID), `role`, `iat`, `exp` and `iss` = `agrinex`.
     - Signed with a secret from the environment of at least 256 random bits.
     - Issued and validated with Spring Security's OAuth2 resource-server JWT support (Nimbus, Boot-managed). No additional JWT library.
  5. **Refresh tokens.**
     - An opaque 256-bit random value. Only its SHA-256 hash is stored.
     - Each token expires 7 days after it is issued.
     - Every refresh rotates the token: the old one is revoked and a new one is issued in the same family (`refresh_tokens.family_id`, `created_at`, `revoked_at`).
     - Presenting a revoked token is treated as reuse and revokes the whole family (RFC 9700 §4.14.2). Logout revokes the family.
     - The frontend serializes refresh calls, so concurrent tabs do not trigger reuse detection.
  6. **ADMIN-owned alerts** (§28 lets ADMIN create alerts). A FARMER gets a `farmer_profiles` row at registration. An ADMIN gets one when first needed. `price_alerts` keeps its §20 foreign key to `farmer_profiles`.
  7. **Login throttling:** see ADR-0017.
- **Rationale:**
  - Follows §27 and §28 with current guidance, and adds no dependency.
  - A shorter farmer minimum is a deliberate usability choice for low-literacy users, offset by throttling and BCrypt. Admin accounts, which can change system configuration, follow NIST's 15-character minimum.
- **Alternatives Considered:**
  - A 15-character minimum for everyone: hard for the farmer persona.
  - OTP login: needs an SMS provider (§39).
  - A JWT library such as jjwt: an extra dependency with no gain.
  - Long-lived refresh tokens without rotation: violates §27.
- **Consequences:**
  - Schema:
    - `users`: CHECKs on identifier presence and format, plus `created_at`;
    - `refresh_tokens`: `family_id`, `created_at` and `revoked_at`;
    - ON DELETE CASCADE from users to profiles and tokens (ADR-0029).
  - The auth contract (Task 8) uses `identifier` + `password`.
  - Security review applies before merge (CLAUDE.md rule 16).
- **Source / Evidence:**
  - Spec §20 (lines 939–953, 1009), §25 (line 1266), §27 (line 1318), §28 (lines 1336–1362).
  - RFC 9700 §4.14.2.
  - NIST SP 800-63B-4.
  - Spring Security 7.0 reference ("Trusting a Single Symmetric Key"; `SecretKeyJwtDecoderBuilder.macAlgorithm`).
  - CVE-2025-22228 advisories.
- **Approval:** Decided under ADR-0001.

### ADR-0017 — Public routing, origin layout, CORS, browser tokens and rate limits

- **Decision ID:** ADR-0017
- **Date:** 2026-10-03
- **Status:** DECIDED (item 6 extended by ADR-0048)
- **Title:** Public routing, origin layout, CORS, browser tokens and rate limits
- **Context:**
  - RD-021. The specification places TLS and security headers in Nginx and CORS in the backend `WebConfig` (§12, §28). It also requires a refresh "using httpOnly cookie" (§29), per-IP rate limiting of 60 requests per minute on compare (§28), and the frontend's own `/api/health` (§45).
  - VERIFIED: Spring Boot 4.1 supports `server.forward-headers-strategy=native` with Tomcat's RemoteIpValve, whose trusted proxies are set by `server.tomcat.remoteip.internal-proxies`.
- **Decision:**
  1. **One public HTTPS origin, served by Nginx:**

     | Path | Destination |
     |---|---|
     | `/api/v1/` | Backend (port 8080, internal) |
     | Every other path, including the frontend's `/api/health` | Frontend (port 3000, internal) |
     | `/actuator/` | Answers 404 at Nginx; health is checked only inside the Docker network |
     | Plain HTTP | Redirects to HTTPS, except the ACME challenge path (ADR-0021) |

  2. **CORS.** Browser traffic is same-origin, so Nginx emits no CORS headers (departure D-10). The backend's CORS configuration allows only the configured public origin, plus `http://localhost:3000` in the development profile (§28).
  3. **Browser tokens.**
     - The access token lives only in memory, never in Web Storage.
     - The refresh token is a cookie with the attributes `HttpOnly`, `Secure`, `SameSite=Strict`, `Path=/api/v1/auth` and a 7-day lifetime.
     - The cookie-authenticated endpoints (refresh, logout) also require an `Origin` header equal to the public origin.
     - On a 401, the client refreshes once and retries (§29).
  4. **Server-side rendering.** Next.js server-side code does not call the backend in Phase 1. Every API call starts in the browser and passes through Nginx.
  5. **Client IP.**
     - Nginx sets `X-Forwarded-For` and `X-Forwarded-Proto`.
     - The backend uses the native forward-headers strategy, trusting only the Docker network's proxy addresses.
     - Rate limits and logs use the resulting client IP.
  6. **Rate limits.** Fixed one-minute windows per client IP, using Redis counters (ADR-0018). Limit values are deployment configuration. Exceeding a limit answers 429 `RATE_LIMITED` with a `Retry-After` header.

     | Endpoint | Limit |
     |---|---|
     | `POST /api/v1/selling-options/compare` and `/simulate` (shared bucket) | 60 per minute (§28) |
     | `POST /api/v1/auth/login` | 10 per minute |
     | `POST /api/v1/auth/register` | 10 per minute |
     | `POST /api/v1/auth/refresh` | 10 per minute |
     | Failed logins for one identifier | 10 per 15 minutes |

  7. **Security headers at Nginx** (§28):
     - `X-Content-Type-Options: nosniff`;
     - `X-Frame-Options: DENY`;
     - a `Content-Security-Policy` built on `default-src 'self'` and `frame-ancestors 'none'`, finalized with the frontend implementation;
     - `Referrer-Policy: strict-origin-when-cross-origin`;
     - `Strict-Transport-Security` once TLS is verified.

     Error responses never include stack traces (§28).
- **Rationale:**
  - One origin removes cross-origin cookie and CORS complexity.
  - In-memory access tokens limit what a cross-site-scripting bug can steal, and the path-scoped strict cookie never accompanies ordinary API calls.
  - Trusting only the internal proxy keeps per-IP limits accurate.
- **Alternatives Considered:**
  - A separate API subdomain: needs CORS and cross-site cookies.
  - Tokens in `localStorage`: exposed to scripts.
  - Cookie-authenticated API calls: need CSRF tokens on every endpoint.
  - Server-side backend calls from Next.js: needs token forwarding, and hides client IPs.
- **Consequences:** `infra/nginx.conf`, the backend web and security configuration, and the frontend API client follow this entry. Security review is required before merge (rule 16).
- **Source / Evidence:**
  - Spec §10 (line 498), §12 (lines 562, 565), §27 (line 1318), §28 (lines 1342–1344), §29 (line 1375), §45 (lines 1907, 1914, 1918).
  - Spring Boot 4.1 how-to "Running Behind a Front-end Proxy Server".
- **Approval:** Decided under ADR-0001.

### ADR-0018 — Redis responsibilities and failure behavior

- **Decision ID:** ADR-0018
- **Date:** 2026-10-03
- **Status:** DECIDED
- **Title:** Redis responsibilities and failure behavior
- **Context:**
  - RD-019 (CONFLICT C-04). §12 lists Redis for "Rate limiting / Session cache / Alert queue", and §45 for "Rate limiting, cache".
  - Access tokens are stateless and refresh tokens live in PostgreSQL (§28). §29 defines no Redis failure behavior.
  - VERIFIED: Spring Boot 4.1 health groups can include or exclude individual indicators.
- **Decision:**
  1. **Redis holds:**
     - **Rate-limit counters** (ADR-0017): fixed-window `INCR` with expiry, through Spring Data Redis and Lettuce (baseline; no new dependency).
     - **A cache of public reference reads** (states, districts, localities, commodities, varieties, grades), with a one-hour TTL and explicit eviction by the owning module on every write. Writes include admin curation and the automatic registration of varieties and grades.
  2. **Redis does not hold** sessions, an alert queue, price observations or comparison results. This is departure D-09.
  3. **Configuration.**
     - No persistence: RDB and AOF are disabled.
     - A `maxmemory` limit with `allkeys-lru` eviction.
     - Password-protected, and reachable only on the internal Docker network.
  4. **When Redis is unavailable:**
     - rate limiting switches to an in-process limiter with the same limits;
     - cache reads and writes are bypassed and go to PostgreSQL;
     - both events log a warning.

     Redis appears as a component of `/actuator/health` (§27) and on the admin dashboard. The readiness group includes the database and excludes Redis, so a Redis outage never takes the backend out of service.
- **Rationale:**
  - Keeps Redis to the duties that need it.
  - A Redis failure degrades service instead of breaking it, so farmer endpoints keep working (§27 reliability).
  - The in-process fallback neither opens the hero flow to abuse nor shuts it down.
- **Alternatives Considered:**
  - Fail-open: unlimited abuse during the outage.
  - Fail-closed: the hero flow goes down with Redis.
  - Bucket4j: an extra dependency for a fixed window.
  - Caching comparison results: they could be stale after ingestion.
- **Consequences:** Health-group configuration, a test of the Redis-down path, and Redis is removed from the alert design.
- **Source / Evidence:** Spec §12 (line 545), §27 (lines 1312, 1324), §28 (lines 1336–1343), §29, §45 (line 1892); Spring Boot 4.1 actuator documentation (health groups); baseline §3 (Lettuce 7.5.2).
- **Approval:** Decided under ADR-0001.

### ADR-0019 — Alert evaluation and delivery

- **Decision ID:** ADR-0019
- **Date:** 2026-10-03
- **Status:** DECIDED
- **Title:** Alert evaluation and delivery
- **Context:**
  - RD-020. §34 defines:
    - four alert types;
    - evaluation "after every ingestion run … against newly ingested observations";
    - the lifecycle, in which dismissal leaves the alert ACTIVE and it "will trigger again next ingestion if still true";
    - delivery "polled or pushed".
  - §15 says "Send admin alert" without naming a channel.
- **Decision:**
  1. **When.** After each run that inserted or updated at least one observation (ADR-0011, ADR-0013), the alert module evaluates every ACTIVE alert whose commodity, and market when set, has at least one observation inserted or updated by that run in a VERIFIED, active market.
  2. **Conditions** follow §34, with prices from ADR-0010 and changes from ADR-0025:

     | Alert type | Triggers when |
     |---|---|
     | `PRICE_ABOVE` | modal ≥ threshold |
     | `PRICE_BELOW` | modal ≤ threshold |
     | `PERCENTAGE_RISE` | `changePercent7Day` ≥ threshold |
     | `PERCENTAGE_FALL` | `changePercent7Day` ≤ −threshold |

     A trend of `INSUFFICIENT_DATA` never triggers.
  3. **Alerts without a market** are evaluated over the qualifying markets. The event records the market with the most extreme qualifying value: the highest price for ABOVE, the lowest for BELOW, the largest rise or the largest fall. Ties go to the alphabetically first market name.
  4. **Event storage.**
     - `alert_events` gains `ingestion_run_id`, `market_id`, `price_observation_id` and `read_at`.
     - A unique constraint on (`price_alert_id`, `ingestion_run_id`) allows at most one event per alert per run, so re-evaluation is idempotent.
     - `price_alerts.last_triggered_at` is updated (§34).
  5. **Delivery.**
     - In-app only, by polling an alert-events API. There is no push channel (WebSocket, server-sent events) and no email in Phase 1.
     - Viewing or dismissing sets `read_at`. The alert stays ACTIVE (§34).
  6. **Failures** are logged (ADR-0013 item 4). There is no retry queue, and the next run evaluates again.
  7. **Admin alert on ingestion failure** (§15):
     - a banner on the admin dashboard, driven by the ingestion health API (latest status per dataset and the count of consecutive failures);
     - a structured ERROR log event.

     There is no external channel.
- **Rationale:**
  - Polling needs no new infrastructure and matches the specification's "polled" option.
  - One event per alert per run, raised only by new or revised data, prevents event floods while keeping §34's re-trigger rule.
- **Alternatives Considered:**
  - Push over WebSocket or server-sent events: new infrastructure and connection state.
  - A Redis alert queue: unnecessary for in-process evaluation.
  - Email: an external dependency, and optional in §34.
- **Consequences:**
  - Schema changes to `alert_events`.
  - The alert API (Task 8) exposes alerts and paginated events with a read action.
  - The frontend polls when the page gains focus and while it is open, at an interval set in the frontend specification.
- **Source / Evidence:** Spec §12 (line 545), §14 (line 672), §15 (lines 687, 704), §28, §30 (line 1393), §34 (lines 1496–1510).
- **Approval:** Decided under ADR-0001.

### ADR-0020 — Admin control boundary, secret handling and admin audit

- **Decision ID:** ADR-0020
- **Date:** 2026-10-03
- **Status:** DECIDED
- **Title:** Admin control boundary, secret handling and admin audit
- **Context:**
  - The specification gives ADMIN ingestion health, the ingestion trigger and the configuration of weights and transport assumptions (§28, §30, §32).
  - Secrets must never reach the frontend, version control, logs, error messages or API responses (§27, §28, §47 Q21; CLAUDE.md §K).
  - The owner instructed: "DO NOT assume that every secret or infrastructure setting belongs in an admin dashboard."
- **Decision:**
  1. **ADMIN can change**, through authenticated endpoints that are each audited:

     | Capability | Detail |
     |---|---|
     | Trigger ingestion | ADR-0011 |
     | Pause and resume scheduled ingestion per dataset | New table `ingestion_source_settings`. Manual triggers still work while paused |
     | Update transport assumptions | §32 parameters; each update inserts a new version, and values must be > 0 |
     | Update recommendation weights and freshness days | Each update inserts a new version; weights must be between 0 and 1, and fresh ≤ recent ≤ stale days |
     | Curate markets | Verify, merge, deactivate and set coordinates with a source (ADR-0014) |
     | Add commodities | Each with an official source reference, plus its verified unit and evidence (ADR-0006, ADR-0008) |
     | Add district aliases | With an evidence note |
     | Edit `name_hi` and `name_mr` | With a provenance note (ADR-0015) |

  2. **ADMIN can only view:**
     - ingestion runs, counters, completeness and errors, including raw-payload excerpts;
     - per-dataset source status;
     - database and Redis health;
     - configuration versions and the audit log;
     - the configured schedule, coverage states and resource IDs;
     - whether the government API key is configured: shown as CONFIGURED or NOT CONFIGURED, never the value.
  3. **Nobody changes these at runtime.** They live only in the deployment environment, and changing them means a redeploy:
     - secrets: `DATA_GOV_API_KEY`, the JWT secret, and the database and Redis credentials;
     - source settings: resource IDs and base URL;
     - the schedule (§15 fixes 6 hours), coverage states, page size and retry policy;
     - edge settings: rate limits, CORS origin and TLS;
     - the time zone.

     A change to coverage or schedule also needs a decision entry. No API reads or writes a secret.
  4. **Secret storage.**
     - Environment variables come from a root `.env` file on the server, readable only by the deploying user (mode 600) and never committed.
     - `infra/.env.example` lists the variable names, never their values (ADR-0021).
  5. **Masking.**
     - Logs never contain secrets, `Authorization` headers or cookies.
     - Government API URLs are logged with `api-key` redacted.
     - The database rejects credential keys in `ingestion_runs.request_scope` and in admin audit details.
  6. **Admin audit.** The new append-only table `admin_audit_events` (owned by admin) records:
     - the actor;
     - the action, from a closed vocabulary;
     - the target type and ID;
     - masked before and after details, as JSON;
     - the correlation ID and time.

     Configuration tables stay versioned, and the audit row references the new version.
  7. **Admin accounts** are provisioned (ADR-0028) and never self-registered. Admin endpoints live under `/api/v1/admin/`.
- **Rationale:**
  - Admins get the operational control Phase 1 needs (data curation, source pauses during known maintenance) and full visibility.
  - Credentials and infrastructure stay out of reach of any API, and every change is attributable.
- **Alternatives Considered:**
  - Secrets editable in the dashboard: one compromised admin account would expose credentials.
  - Schedules editable in the dashboard: risks silently diverging from §15's 6-hour rule.
- **Consequences:**
  - New tables `ingestion_source_settings` and `admin_audit_events`.
  - Task 8 defines the admin endpoints, within this boundary.
  - Security review is required (rule 16).
- **Source / Evidence:** Spec §27, §28, §30 (lines 1396–1397), §32 (line 1449), §45, §47 Q21; CLAUDE.md §K; owner instruction (2026-10-03).
- **Approval:** Decided under ADR-0001.

### ADR-0021 — Deployment topology

- **Decision ID:** ADR-0021
- **Date:** 2026-10-03
- **Status:** DECIDED
- **Title:** Deployment topology
- **Context:**
  - RD-022 (CONFLICT C-05). The specification names:
    - three compose files (`infra/docker-compose.yml`, `infra/docker-compose.dev.yml` and the root "Production compose file");
    - two env templates;
    - a "staging deployment" (§10) and a public cloud VM (§7, §42 item 28);
    - TLS via "Nginx + certbot";
    - "Demo fixture data loads on first boot" (§10) alongside a manual `psql` seed command (§45).
  - VERIFIED: Docker Compose merges files given with `-f` in order: "Subsequent files may merge, override, or add to their predecessors."
- **Decision:**
  1. **Environments.**
     - Local development.
     - One public deployment on a single cloud VM. It is "staging" until the Phase 1 freeze and production afterwards: one host, one deployment.
     - Experiment 2's 14 days of ingestion run on the public deployment.
     - The VM provider, region, size and domain are procurement inputs (RD-037).
  2. **Compose files.**

     | File | Role |
     |---|---|
     | Root `docker-compose.yml` | The full production-shaped stack: Nginx, frontend, backend, PostgreSQL and Redis, plus the optional ML service under a Compose profile. No database or Redis ports are published, and Nginx alone publishes 80 and 443 |
     | `infra/docker-compose.dev.yml` | The local override, applied with `docker compose -f docker-compose.yml -f infra/docker-compose.dev.yml up`. It publishes 3000 and 8080 (§45), plus 5432 and 6379, and enables the demo profile. Its relative paths are written relative to the repository root, because merged files resolve paths from the first file |
     | `infra/docker-compose.yml` | Backing services only (PostgreSQL and Redis), for developers who run the backend and frontend from an IDE |

  3. **Environment files.**
     - `infra/.env.example` is the single environment contract (§41).
     - The root `.env.example` contains only comments that point to it. §45's `cp infra/.env.example .env` creates the root `.env` that Compose reads.
  4. **TLS.**
     - certbot runs on the host and uses the HTTP-01 webroot method, through a challenge directory that Nginx serves.
     - Certificates are mounted read-only into the Nginx container.
     - Renewal uses the certbot package's scheduled renewal, with a deploy hook that reloads Nginx.
  5. **Seeding.**
     - Reference data and the initial configuration versions are Flyway migrations, applied automatically at backend start.
     - Demo fixtures and test accounts load on first boot only in environments that enable the demo profile, from an extra Flyway location. They never load on the public deployment. This is departure D-12, and see ADR-0028.
     - Real price data on the public deployment comes from ingestion and the historical bootstrap.
  6. **Persistence.**
     - PostgreSQL uses a named volume.
     - A daily `pg_dump` from the host keeps 7 days of backups.
     - Redis is ephemeral (ADR-0018).
  7. **Health checks.**

     | Service | Check |
     |---|---|
     | Backend | `/actuator/health/readiness`, internal only |
     | Frontend | `/api/health` |
     | PostgreSQL | `pg_isready` |
     | Redis | `PING` |

     Nginx starts after the services it fronts are healthy.
- **Rationale:**
  - Gives each file the specification names a distinct, non-overlapping role.
  - One canonical stack definition means local development runs the same topology as production.
  - A single deployment fits the budget, and §28's HTTPS-only rule holds throughout.
- **Alternatives Considered:**
  - Separate staging and production hosts: doubles cost.
  - certbot in a container: one more service and shared-volume coordination.
  - Auto-loading demo data everywhere: conflicts with ADR-0028.
- **Consequences:**
  - Infrastructure scaffolding (§42 item 3) follows this layout.
  - The VM, domain and SoI registration are human actions.
- **Source / Evidence:**
  - Spec §7 (line 314), §10 (lines 434, 496, 499), §28 (line 1347), §36, §41 (line 1732), §42, §43 (lines 1841–1858), §45 (lines 1887–1925).
  - Docker documentation "Merge Compose files".
  - certbot documentation (webroot plugin and renewal).
- **Approval:** Decided under ADR-0001.

### ADR-0022 — API contract strategy and conventions for Task 8

- **Decision ID:** ADR-0022
- **Date:** 2026-10-03
- **Status:** DECIDED
- **Title:** API contract strategy and conventions for Task 8
- **Context:**
  - RD-023 (CONFLICT C-06). P5 requires contract-first development. §41 names `contracts/openapi.yaml` and the JSON-schema contracts. §10 says the spec is "generated and exported".
  - VERIFIED:
    - OpenAPI Specification 3.2.1 is the latest release, and 3.1.2 is the latest 3.1 release;
    - springdoc-openapi 3.1.1 is current, states compatibility with Spring Boot 4, and defaults to OpenAPI 3.1. Its compatibility matrix lists Spring Boot 4.0.x explicitly.
- **Decision:**
  1. **The contract.**
     - `contracts/openapi.yaml` is hand-authored and authoritative, written in OpenAPI 3.1. Code conforms to it.
     - Any generated document is a build artifact used only to detect drift, and is never written over the contract. This is departure D-11.
     - Drift is detected by automated contract tests:
       - backend integration tests validate real requests and responses against the document;
       - frontend tests validate every mock against it (§44).

       The validator library is chosen at scaffolding (ADR-0034).
  2. **Versioning.**
     - The path carries the major version: `/api/v1`.
     - `info.version` follows semantic versioning: additive changes raise the minor version, breaking changes the major version.
     - Changes follow §41's Shared Contract Change Rule, and mocks change in the same change.
  3. **Module boundaries.**
     - Each path group and its schemas belong to the module named in §30.
     - Response schema names follow §29 and §30, for example `MarketComparisonResponse`.
     - Shared components hold the error model, pagination and provenance.
  4. **Error model.**
     - The error body is the schema §10 names `ApiErrorResponse`, shaped as RFC 9457 Problem Details (`application/problem+json`) with the extension members:
       - `code`: a stable UPPER_SNAKE identifier, for example `VALIDATION_FAILED`, `NO_MARKETS_IN_RANGE`, `UNSUPPORTED_PRICE_UNIT`, `RATE_LIMITED`, `CONFLICT`;
       - `correlationId`;
       - `errors[]`: field errors, each with field, code and message.
     - `contracts/error-response.schema.json` defines the model.
     - Stack traces are never included (§28).
     - 400 is used for syntactic or validation errors, and 422 for valid requests the data cannot satisfy.
  5. **Pagination.**
     - List endpoints use `page` (zero-based) and `size` (default 20, maximum 100). They return `PaginatedResponse<T>` with `items`, `page`, `size`, `totalItems` and `totalPages` (§30).
     - Reference lists (states, districts, commodities, varieties) are not paginated.
  6. **Filtering** uses explicit, named query parameters per endpoint. There is no generic query language.
  7. **Authentication.**
     - JWT Bearer, with OpenAPI `security` declared per operation.
     - Anonymous access: compare, simulate, geography, commodities, markets and trends (§28 "anonymous ok").
     - FARMER or ADMIN: alerts. ADMIN: `/api/v1/admin/`.
     - The refresh cookie applies only under `/api/v1/auth` (ADR-0017).
  8. **Validation.** Bean Validation mirrors the OpenAPI constraints (required fields, ranges, enums) (§28).
  9. **Source and freshness metadata.** Every object that carries a price includes:
     - `observationId`, `reportDate` and `freshnessStatus`;
     - `sourceDataset` (`CURRENT_PRICE` or `HISTORICAL_PRICE`);
     - `variety` and `grade`;
     - `priceUnit`;
     - the RULE 7 attribution text.

     Responses that serve price data also carry the latest synchronization status: `lastSuccessfulSyncAt` and whether the latest current-dataset run failed. These drive the §29 banner.
  10. **Price-unit semantics.**
      - Source prices are returned with their `priceUnit` (`Rs./Quintal`).
      - Computed amounts (transport cost, gross value, net realization) are totals in INR.
      - Every amount is a JSON number rounded half-up to two decimal places.
      - Requests carry `quantity` with `quantityUnit` (`KG` or `QUINTAL`).
  11. **Formats.**
      - Dates use `yyyy-MM-dd`. Instants use RFC 3339 with an offset.
      - IDs are UUID strings.
      - Reference names are returned as `name`, `nameHi` and `nameMr` (ADR-0015).
      - Error `code` values are the frontend's keys for localized messages.
- **Rationale:**
  - A hand-authored contract keeps P5 and §41's review process; a generated export could silently replace the agreed contract.
  - OpenAPI 3.1 aligns with JSON Schema 2020-12, so the `*.schema.json` contracts of §41 can be referenced directly. It is also the version current Spring tooling emits.
  - RFC 9457 is the standard error format and is supported by Spring Framework.
- **Alternatives Considered:**
  - A code-first generated contract: the frontend would depend on backend timing, and it breaks P5.
  - OpenAPI 3.2: tooling support for it is not established.
  - A custom error envelope: non-standard.
- **Consequences:**
  - Task 8 writes the contract within these rules.
  - The §10 checklist item is satisfied by the drift check (D-11).
- **Source / Evidence:**
  - Spec P5 (line 244), §10 (line 474 "Error responses always use `ApiErrorResponse` format — no stack traces exposed"; line 475), §28, §29, §30, §41 (lines 1725–1739), §44 (line 1877).
  - The OpenAPI Specification versions page (`spec.openapis.org/oas/`).
  - springdoc documentation and FAQ (version 3.1.1, default `openapi_3_1`).
  - RFC 9457.
- **Approval:** Decided under ADR-0001.

### ADR-0023 — Comparison endpoint field names and no-data semantics

- **Decision ID:** ADR-0023
- **Date:** 2026-10-03
- **Status:** DECIDED
- **Title:** Comparison endpoint field names and no-data semantics
- **Context:**
  - RD-006. The quantity unit is named `quantityUnit` in §16 but `unit` in §21.
  - The net-realization winner is named `netRealizationWinnerId` in §16 but `netWinnerId` in §21.
  - §16 returns 422 only when no markets are in range. §29 says "Return empty options array with appropriate code" when recent data is missing.
- **Decision:**
  1. **Request.**
     - The quantity unit field is `quantityUnit`, with the values `KG` and `QUINTAL` (§16).
     - The origin is the selected district's centroid, sent as `originLat` and `originLon` (§16, §21 step 3).
     - `maxDistanceKm` defaults to 150 (§16).
     - Bounds: `quantity` > 0 and `maxDistanceKm` > 0, with no further limits, because the specification states none for compare. The §17 slider ranges bound only the simulate request (ADR-0026).
  2. **Response winners.**
     - `netRealizationWinnerId` is the rank-1 market by composite score (§16 RANK: "Identify net_winner (rank 1 by composite score)").
     - `priceOnlyWinnerId` is the market with the highest modal price. Ties go to the higher net realization, then the shorter distance, then the alphabetically first market name.
     - `rankingsDisagree` is true exactly when the two IDs differ.
  3. **No data.**
     - **No market in range** (no VERIFIED, active market with coordinates within `maxDistanceKm`): 422 with `code` `NO_MARKETS_IN_RANGE` and title "Insufficient data" (§16).
     - **Markets in range, but none with a recent observation** (ADR-0010): 200 with `options: []`, `noDataReason: NO_RECENT_PRICE_DATA`, null winners, `rankingsDisagree: false`, and `excludedMarkets[]` (§29).
     - **Some markets excluded:** `options[]` holds the ranked eligible markets. `excludedMarkets[]` lists each excluded market with its ID, name, distance, approximate-distance flag and reason `NO_RECENT_PRICE_DATA`.
     - **Commodity unit is not `Rs./Quintal`:** 422 `UNSUPPORTED_PRICE_UNIT` (ADR-0008).
  4. **Persistence.**
     - Every successful comparison persists its `selling_search` and `recommendation_run`, including zero-option results.
     - `selling_options` are persisted for eligible markets only. Excluded markets are returned but never persisted.
  5. **Transparency.** The response carries the transport assumptions used (ADR-0026) and per-option provenance (ADR-0022).
- **Rationale:** §16 is the specification's detailed definition of the engine and its response, while §21 abbreviates names in a sequence diagram. The no-data rules apply §16 and §29 together without inventing new states.
- **Alternatives Considered:**
  - Use §21's names: they conflict with §16's response definition.
  - Return 422 for "no recent data": contradicts §29.
  - Persist excluded markets: `selling_options` requires an observation.
- **Consequences:** Task 8 defines `MarketComparisonResponse` with these fields. Mocks cover the three no-data cases.
- **Source / Evidence:** Spec §16 (lines 715–749), §21 (lines 1064, 1085), §29 (line 1372), §44.
- **Approval:** Decided under ADR-0001.

### ADR-0024 — Composite scoring rules and default weights

- **Decision ID:** ADR-0024
- **Date:** 2026-10-03
- **Status:** DECIDED
- **Title:** Composite scoring rules and default weights
- **Context:**
  - RD-007. §16 defines the score formula. It leaves undefined:
    - `normalized_transport`;
    - the trend score for INSUFFICIENT_DATA;
    - the max = min case;
    - three default weights.
  - §21 step 6 shows `w_net:0.55, w_price:0.15`. FR-10 makes net realization the dominant factor.
- **Decision:**
  1. **Default weights** (seed of the first `recommendation_weights` version):
     - `w_net_realization` 0.55 and `w_price` 0.15 (§21);
     - `w_trend` 0.10, `w_freshness` 0.10 and `w_transport_penalty` 0.10;
     - freshness days 1 / 4 / 10 (§16).

     All are admin-tunable (ADR-0020).
  2. `trendScore` for `INSUFFICIENT_DATA` is 0.5, the same as STABLE.
  3. `normalized_transport` = (transport − min transport) ÷ (max transport − min transport) over the eligible candidates, the same min–max form as net and price.
  4. **Max = min.** When a factor's maximum equals its minimum (including a single candidate), every candidate gets `normalized_net` = 1, `normalized_price` = 1 and `normalized_transport` = 0.
  5. **Ranking order:** score descending, then estimated net realization descending, then distance ascending, then the alphabetically first market name.
  6. **Reason codes.** "Top 25%" and "bottom 25%" (§16) are evaluated over the N eligible candidates:
     - `PRICE_ADVANTAGE`: the candidate's modal is at least the modal at position ⌈N/4⌉ in descending order.
     - `LOWER_TRANSPORT`: the candidate's transport cost is at most the cost at position ⌈N/4⌉ in ascending order.
     - The other codes use §16's stated conditions.
- **Rationale:**
  - Uses the specification's own example weights. The remaining 0.30 is split equally because no source favors one factor, and net realization stays dominant (FR-10).
  - A neutral trend score neither penalizes nor rewards markets with thin history.
  - The constant normalization removes the division by zero deterministically, without changing rankings.
- **Alternatives Considered:**
  - Trend score 0 for insufficient data: penalizes data-poor markets.
  - Re-normalizing weights when the trend is missing: more complex, with the same intent.
  - Dropping the transport penalty: §16 includes the term.
- **Consequences:**
  - Hand-computed unit fixtures cover every rule above, and all eight reason codes (§48).
  - Experiment 1 runs with these defaults.
  - The product owner may retune the weights at runtime without code changes.
- **Source / Evidence:** Spec §16 (line 739), §20 `RECOMMENDATION_WEIGHTS`, §21 (line 1066), §27, §33 (line 1480), §36, §48, FR-10 (line 1288).
- **Approval:** Decided under ADR-0001.

### ADR-0025 — Trend direction and trend windows

- **Decision ID:** ADR-0025
- **Date:** 2026-10-03
- **Status:** DECIDED
- **Title:** Trend direction and trend windows
- **Context:**
  - RD-008. FR-14 asks for a "30-day trend direction". §33 defines one `trendDirection`: the last-7-day average against the average from 8–14 days ago, with `INSUFFICIENT_DATA` below 4 observations in 14 days.
  - §21 shows a `days=30` request returning `trendDirection` together with `changePercent7Day`.
- **Decision:**
  1. **One trend direction per market and commodity, computed from the daily series** (ADR-0010) in IST:
     - The **recent window** is the 7 calendar days ending today (today − 6 to today). The **baseline window** is the 7 days before it (today − 13 to today − 7). This reads "8–14 days ago" counting today as day 1.
     - `changePercent7Day` = (recent average − baseline average) ÷ baseline average × 100. The averages are means of the daily values in each window.
     - The unrounded value is classified as RISING above +3.0, FALLING below −3.0, and STABLE otherwise.
     - `changePercent7Day` is displayed to one decimal place.
  2. **`INSUFFICIENT_DATA`** is a value of `trendDirection` (§33). It applies when fewer than 4 daily values exist in the last 14 days, or when either window is empty. `changePercent7Day` is then null.
  3. **FR-14's "30-day trend direction"** is this direction, shown with the 30-day chart (today − 29 to today).
     - The 7/30-day toggle (F1.15, §33) changes only the chart window. Direction and change are identical for both.
     - `TrendResponse` returns the daily series with provenance, `trendDirection`, `changePercent7Day` and the window bounds.
- **Rationale:** Follows the detailed §33 algorithm and §21's example, and adds no second, undefined 30-day statistic. The windows are contiguous, of equal length and deterministic.
- **Alternatives Considered:** A separate 30-day regression or direction: undefined in the specification.
- **Consequences:** Trend tests cover window edges, empty windows and the 4-value threshold.
- **Source / Evidence:** Spec F1.15 (line 338), §16 TREND node, §21 (lines 1090–1093), §33 (lines 1471–1480), FR-14 (line 1292).
- **Approval:** Decided under ADR-0001.

### ADR-0026 — What-If simulator calculation and ranking

- **Decision ID:** ADR-0026
- **Date:** 2026-10-03
- **Status:** DECIDED
- **Title:** What-If simulator calculation and ranking
- **Context:**
  - RD-009. The §17 client formula `max(300, dist × newRate)` has no trip factor and a fixed minimum charge. §32 is the documented, configurable formula ("Never Silent").
  - The quantity slider reaches 5,000 kg, while the default vehicle capacity is 1,000 kg.
- **Decision:**
  1. **Client-side recalculation applies §32 in full:**
     - trips = ⌈quantity in kg ÷ `vehicleCapacityKg`⌉;
     - per-trip cost = max(`minimumTripCharge`, distance × `ratePerKm`);
     - transport = per-trip cost × trips;
     - gross = modal × quantity in quintals;
     - net = gross − transport.

     The comparison response carries the assumptions it used (`ratePerKm`, `minimumTripCharge`, `vehicleCapacityKg` and the version ID). This is departure D-13.
  2. **Ranking.** The simulator ranks by estimated net realization (§17 "Re-sort by newNet"), breaking ties by shorter distance and then by market name. The screen labels the basis. Composite scores are not recomputed in the browser.
  3. **Distance.**
     - Lowering the maximum distance filters the pre-fetched options in the browser.
     - Raising it above the original comparison radius is the only trigger for server mode: `POST /api/v1/selling-options/simulate` with `recommendationId` and the overrides (`quantity`, `transportRatePerKm`, `maxDistanceKm`).
     - The server recomputes the comparison with the original run's weight and transport versions and the overrides, and returns a `MarketComparisonResponse`. Nothing is persisted (§32 "session-only").
  4. **Input bounds:** quantity 100–5,000 kg, rate ₹3–₹20 per km, distance 25–300 km (§17).
- **Rationale:** One formula on client and server keeps the What-If numbers equal to the comparison numbers for the same inputs, which hand-computed tests need (§48). Server mode is needed only when markets that were never fetched can enter.
- **Alternatives Considered:**
  - Keep §17's simplified formula: wrong for multi-trip quantities and for an admin-changed minimum charge.
  - Server mode for every change: contradicts §10's "without full API re-call".
- **Consequences:** The comparison contract includes the transport assumptions. Shared test fixtures cover client and server calculations.
- **Source / Evidence:** Spec §10 (line 483), §17 (lines 758–763), §32 (lines 1436–1463), FR-07, FR-15.
- **Approval:** Decided under ADR-0001.

### ADR-0027 — Design source of record and design approval

- **Decision ID:** ADR-0027
- **Date:** 2026-10-03
- **Status:** DECIDED
- **Title:** Design source of record and design approval
- **Context:**
  - RD-010. Screen design belongs to the UI/UX role (§40). Role file 02 is planned (§43, §44).
  - A Stitch MCP server is configured. The specification never mentions Stitch or a design-approval process.
- **Decision:**
  1. The design source of record for production screens is role file 02 (`docs/02_AgriNex_UIUX_Phase1_Specification.md`). It must conform to the master specification's UX requirements (§6, F1.2, F1.21, F1.22, §27, §31, §32, §49).
  2. Stitch screens, and those from any other tool, are working drafts. A screen design is approved when role file 02 references it and the Document Owner (Team Lead / Product Manager, spec header) records the approval in SSOT index area 5: screen, design reference, date and approver.
  3. **Precedence:** master specification, then role file 02, then tool designs.
  4. Production screens are built only from approved designs (CLAUDE.md §M). Prototypes are labelled as prototypes.
- **Rationale:** Gives one auditable source, keeps the specification on top, and lets the team use a design tool without making it authoritative.
- **Alternatives Considered:** Stitch as the source of record: it sits outside version control and has no review trail.
- **Consequences:** Frontend screen work waits for role file 02 and recorded approvals. Recording an approval is a human act.
- **Source / Evidence:** Spec §40, §43, §44; CLAUDE.md §M; `.mcp.json` server list (no values reproduced).
- **Approval:** Decided under ADR-0001.

### ADR-0028 — Demo credentials and demo data

- **Decision ID:** ADR-0028
- **Date:** 2026-10-03
- **Status:** DECIDED
- **Title:** Demo credentials and demo data
- **Context:**
  - RD-011 and RD-026. §46 names a fixed admin login, which is not repeated here, and the deployment is public (§7).
  - The specification asks to "adjust the demo data" (§21, §46) yet forbids fabricated prices (P10, §31 RULE 5).
- **Decision:**
  1. **No fixed or published credentials** exist in any publicly accessible deployment. The §46 credentials are never seeded, configured or documented.
  2. **First admin.** The backend creates the first ADMIN on startup from bootstrap credentials in the deployment environment, and only when no ADMIN exists.
     - The password must meet ADR-0016.
     - The operator removes the bootstrap password from the environment after first start.
     - The creation is audited.
  3. **Test accounts** (`fixtures/test_accounts.sql`) exist only in demo-profile environments. Their credentials are generated per environment, never committed and never published.
  4. **Public demo.** The presenter uses a real admin account whose credentials stay private. The farmer flow needs no login, because compare is anonymous (§28).
  5. **Demo data** is real government observations only. "Adjust the demo data" means choosing real scenarios where the rankings disagree: commodity, date, origin district, quantity and distance setting.
     - Prices, markets, dates and coordinates are never altered.
     - `fixtures/demo_price_observations.sql` is an exact export of ingested observations, provenance columns included.
     - Synthetic unit-test fixtures never enter a deployment database and are never shown as real.
- **Rationale:** Published credentials would let anyone reconfigure the system. Choosing real scenarios meets the demo goal without breaking P10.
- **Alternatives Considered:** Fixed demo credentials, possibly rotated after the demo: unsafe while the site is public.
- **Consequences:** Deployment documentation describes the bootstrap. The demo script uses real scenarios.
- **Source / Evidence:** Spec P10 (line 249), §7 (line 314), §21 (line 1082), §28, §31 RULE 5, §43 (lines 1848–1849), §44, §46 (lines 1958, 1990).
- **Approval:** Decided under ADR-0001.

### ADR-0029 — Data retention and deletion

- **Decision ID:** ADR-0029
- **Date:** 2026-10-03
- **Status:** DECIDED
- **Title:** Data retention and deletion
- **Context:**
  - RD-035. The specification defines deletion only for alerts (§34) and deactivation of users (§20).
  - VERIFIED: the Digital Personal Data Protection Act, 2023 requires erasure once the purpose is no longer served or consent is withdrawn, unless law requires retention (section 8(7)). It also grants a right to erasure (section 12). The DPDP Rules were notified on 2025-11-13. This entry is a design alignment, not a legal opinion.
- **Decision:**
  1. **Retention:**

     | Data | Retention |
     |---|---|
     | `price_observations`, `price_observation_revisions`, `ingestion_runs`, reference and configuration tables | Indefinite: core evidence for trends, ML and Experiment 2 |
     | `ingestion_errors` (raw payloads) | 180 days |
     | `alert_events` | 180 days |
     | `refresh_tokens` | Deleted one day after expiry. Revoked tokens are kept until expiry for reuse detection |
     | `forecasts` | Deleted 90 days after their forecast date |
     | `selling_searches`, `recommendation_runs`, `selling_options` | Indefinite, anonymized on account erasure (Experiment 1) |
     | `admin_audit_events` | Indefinite |

     Purges run as daily scheduled jobs in the owning modules.
  2. **Account erasure.**
     - Deleting a user cascades to the user's `farmer_profiles`, `price_alerts`, `alert_events` (§34 cascade) and `refresh_tokens`.
     - `selling_searches.farmer_profile_id` is set to NULL. Searches hold only district-centroid origins (§28).
     - Admins are deactivated, never deleted, so audit actors stay resolvable.
     - In Phase 1, erasure is an audited admin procedure. A self-service endpoint is not in the specification and is not added.
- **Rationale:** Keeps the data the product and its evaluation need, bounds operational data, and makes erasure possible without schema change.
- **Alternatives Considered:**
  - Keep everything: unbounded growth of raw payloads and personal data.
  - Soft delete only: does not meet erasure.
- **Consequences:**
  - Foreign keys: CASCADE from users to profiles and tokens, CASCADE from profiles to alerts, CASCADE from alerts to events, SET NULL on searches, RESTRICT for audit actors.
  - Erasure is documented in the operations runbook.
- **Source / Evidence:** Spec §10 (line 457), §20 (line 945), §28 (line 1345), §34 (lines 1505–1510), §36; DPDP Act 2023 sections 8(7) and 12; DPDP Rules 2025 (notified 2025-11-13).
- **Approval:** Decided under ADR-0001.

### ADR-0030 — Backend–ML integration (optional track)

- **Decision ID:** ADR-0030
- **Date:** 2026-10-03
- **Status:** DECIDED
- **Title:** Backend–ML integration (optional track)
- **Context:**
  - RD-024 (CONFLICT C-03). ML is optional and starts only after HARD RULE 2 is met (§10, §35).
  - §12 says ML "Reads from PG, writes forecasts back". §30 has no forecast endpoint. ML failure must not affect core features (§35).
- **Decision:**
  1. **Access.** The ML job connects as a dedicated database role, which may:
     - SELECT a read-only view of the daily series (ADR-0010 rule 7), defined by a migration owned by the price module;
     - INSERT and DELETE rows in `forecasts`;
     - nothing else.
  2. **Ownership.** `forecasts` is owned by the price module (ADR-0013).
  3. **Serving.** The backend reads forecasts through the price module. It never calls the ML service synchronously, and the FastAPI service in `ml/api` is not used to serve forecasts.
  4. **Exposure.** A price-module endpoint returns `ForecastResponse`. It returns `available: false`, never a server error, when no forecast with a `forecast_date` of today or later exists (§29). Task 8 defines the endpoint as optional.
  5. **Training threshold.** ML trains only on series with at least 90 observations (§37). Forecasts are per market and commodity, built on the representative daily series.
- **Rationale:** A database-only integration contains ML failure by construction: no runtime dependency, no network call on the request path.
- **Alternatives Considered:** A synchronous REST call to FastAPI: adds a runtime dependency and new failure modes.
- **Consequences:** When the track starts, it adds the database role, the view migration and the endpoint. Phase 1 is unaffected.
- **Source / Evidence:** Spec §12 (lines 549, 572–573), §14 (lines 668–670), §29 (line 1373), §30, §35 (lines 1543–1564), §37, §41 (line 1728), §43 (line 1823).
- **Approval:** Decided under ADR-0001.

### ADR-0031 — Phase 2 component placement

- **Decision ID:** ADR-0031
- **Date:** 2026-10-03
- **Status:** DECIDED
- **Title:** Phase 2 component placement
- **Context:** RD-025 (b), (c) and the mechanism part of (d). Phase 2 starts only after the Phase 1 freeze (HARD RULE 1).
- **Decision:**
  1. **Ownership.** Buyer verification (§23 `buyer_verifications`) and its documents belong to the auth module, because verification is an attribute of a BUYER identity.
  2. **Document storage.** Verification documents are stored in PostgreSQL, in an auth-owned table. They are readable only by the owning BUYER and by ADMIN (§28 "stored with access control"). No object-storage service is added. Size and type limits are set in the Phase 2 contract.
  3. **System-initiated transitions**, such as listing expiry, run as a scheduled job in the listing module, using the same scheduling mechanism as ingestion.
- **Rationale:** Uses existing modules and infrastructure, and adds no new service or external dependency for a single-VM deployment.
- **Alternatives Considered:** A separate verification module or object storage: new boundaries and infrastructure for small volumes.
- **Consequences:** Phase 2 design starts from these placements. Nothing changes in Phase 1.
- **Source / Evidence:** Spec §8 (F2.1, F2.12), §12 (line 540), §18 (lines 794, 812), §23 (line 1159), §28 (line 1348).
- **Approval:** Decided under ADR-0001.

### ADR-0032 — Phase 2 state model, OTP provider and listing TTL

- **Decision ID:** ADR-0032
- **Date:** 2026-10-03
- **Status:** DEFERRED
- **Title:** Phase 2 state model, OTP provider and listing TTL
- **Context:**
  - RD-012: the §18 diagram and table, and §23, disagree on states, transitions and actors.
  - RD-025 (a): there is no OTP or SMS provider, and §39 excludes unverified external dependencies.
  - RD-025 (d): the listing TTL value is a business rule.
- **Decision:** All three are deferred until Phase 2 starts, which is after the Phase 1 freeze (HARD RULE 1).
  - The state model is a product decision about actors and states.
  - The OTP provider involves procurement and an external integration.
  - The TTL is a business rule.

  They remain open in `docs/REQUIRES_DECISION.md` as NON-BLOCKING for Phase 1.
- **Rationale:** None of them affects Phase 1. Deciding them now would anticipate product input the Phase 2 kickoff must provide.
- **Alternatives Considered:** Deciding now: risks fixing product rules without the owner's Phase 2 input.
- **Consequences:** Phase 2 development stays blocked on these items. Phase 1 and Task 8 are not affected; Task 8 defines no Phase 2 endpoints.
- **Source / Evidence:** Spec §10 HARD RULE 1, §18, §19 (line 832), §23, §29, §39, §47 Q19.
- **Approval:** Deferred under ADR-0001.

### ADR-0033 — Credentials in the MCP configuration

- **Decision ID:** ADR-0033
- **Date:** 2026-10-03
- **Status:** DECIDED
- **Title:** Credentials in the MCP configuration
- **Context:**
  - RD-002. `.mcp.json` holds inline credential values for three servers. It is untracked, and there is no root `.gitignore`. The values are not reproduced.
  - Agents may not change `.mcp.json` or Git configuration without explicit instruction (CLAUDE.md §F, §K).
  - VERIFIED: Claude Code expands `${VAR}` and `${VAR:-default}` in `.mcp.json`, in the `command`, `args`, `env`, `url` and `headers` fields.
- **Decision:**
  1. `.mcp.json` is never committed with inline credentials.
  2. Every credential in it becomes a `${VAR}` reference. Each developer sets the values in their own environment.
  3. Until that conversion is done, `.mcp.json` must be excluded from every commit, through a root `.gitignore` entry.
  4. If any value may have been exposed beyond the local machine, its owner rotates it at the provider.
- **Rationale:** Environment expansion keeps a shareable configuration in Git without secrets, and it is supported by the tool (verified).
- **Alternatives Considered:** Never commit `.mcp.json`: each developer then maintains a private copy, and the configuration drifts.
- **Consequences:** Execution needs the user, because agents may not change `.mcp.json` or Git configuration:
  1. replace the inline values with `${…}` references and set the variables;
  2. create a root `.gitignore` that excludes `.env`, `.mcp.json` until it is converted, and local artifacts;
  3. rotate any credential whose exposure is suspected.

  RD-002 stays open as a repository condition until the user confirms these steps.
- **Source / Evidence:** CLAUDE.md §F, §K; Claude Code MCP documentation ("Environment variable expansion in `.mcp.json`"); repository inspection (`git status`, 2026-10-03).
- **Approval:** Decided under ADR-0001. Execution is pending user action.

### ADR-0034 — Tooling deferred to the scaffolding of each area

- **Decision ID:** ADR-0034
- **Date:** 2026-10-03
- **Status:** DEFERRED
- **Title:** Tooling deferred to the scaffolding of each area
- **Context:** The approved baseline (ADR-0002) does not name several tools, and the specification does not either (baseline §2).
- **Decision:** These are chosen when their area is scaffolded, each in its own entry that justifies the dependency under rule 15:
  - the frontend i18n library;
  - the frontend unit-test and end-to-end tools;
  - the CI system;
  - the OpenAPI contract-validation library (ADR-0022);
  - an OpenAPI generator for drift reports, which is optional;
  - the ESLint configuration.

  Already decided elsewhere:
  - the JWT support (ADR-0016);
  - the government HTTP client (ADR-0011);
  - the rate limiter, which uses Redis counters (ADR-0018).
- **Rationale:** Each tool depends on that area's code and on current versions at that time. None affects architecture or contract design.
- **Alternatives Considered:** Choosing them now, without the code that will use them.
- **Consequences:**
  - The deferred tooling blocks only the scaffolding of its own area.
  - Task 8 needs none of it: the contract is an OpenAPI 3.1 text file.
- **Source / Evidence:** `CURRENT_TECHNOLOGY_BASELINE.md` §2 ("Not established by the SSOT"); CLAUDE.md rule 15, §J.
- **Approval:** Deferred under ADR-0001.

### ADR-0035 — Read-only ports for cross-module data in public responses

- **Decision ID:** ADR-0035
- **Date:** 2026-10-04
- **Status:** APPROVED
- **Title:** Read-only ports for cross-module data in public responses
- **Context:**
  - RD-038, found while writing the API contract (Task 8, 2026-10-04). Binding entries require three pieces of data in responses whose owning module cannot read them under the approved dependency graph (ADR-0013; `docs/database/TABLE_OWNERSHIP.md` §4–§5):
    - (a) **Source synchronization status.** ADR-0022 item 9 requires `lastSuccessfulSyncAt` and whether the latest current-dataset run failed in every response that serves price data. The data lives in `ingestion_runs` (ingestion module). The serving modules are price (market detail, trends), recommendation (compare, simulate) and alert (events). There is no price → ingestion, recommendation → ingestion or alert → ingestion dependency. Price → ingestion would close a cycle with ingestion → price, and alert → ingestion one with ingestion → alert (ADR-0013 item 3; TABLE_OWNERSHIP §4 note; §5: price "never reads ingestion tables").
    - (b) **Freshness thresholds.** ADR-0022 item 9 requires `freshnessStatus` on every price, derived from a `recommendation_weights` version (`docs/database/DATABASE_ARCHITECTURE.md` §10.6). ADR-0010 item 2 uses the stale days inside the selection, so the thresholds decide which observation is selected. A comparison records its version in `recommendation_runs.weights_id`, and a simulation reuses it (ADR-0026 item 3). The price module serves market detail and trends directly, and alert serves events, both with the active version. Price → recommendation would close a cycle with recommendation → price; alert → recommendation is not approved.
    - (c) **Commodity availability.** ADR-0006 (consequences): the farmer commodity selector lists commodities with recent observations in coverage. `GET /api/v1/commodities` belongs to the commodity module (spec §30), a leaf that cannot read observations or the stale window.
  - VERIFIED: the contract (`contracts/openapi.yaml`) fixes the fields and their meaning; only the module path is open.
  - The delegation in ADR-0001 covered only the 2026-10-03 lock, so this entry needed explicit human approval (CLAUDE.md §G; this log's rule 3).
- **Decision:**
  1. The module that serves the data declares a small **read-only port** (an interface in its own package), and the owning module implements it. The compile-time dependency therefore runs from the owner to the consumer's port, inside edges ADR-0013 already approves, and no table is read across a boundary:
     | Data | Port declared by | Implemented by | Existing edge used |
     |---|---|---|---|
     | (a) Latest CURRENT_PRICE completed run: status and `finishedAt`; last SUCCESS or PARTIAL_FAILURE `finishedAt` | price (`SourceSyncStatusPort`) | ingestion | ingestion → price |
     | (b) Active freshness thresholds (fresh, recent, stale days), for market detail, trends, alerts and the commodity filter | price (`FreshnessPolicyPort`) | recommendation | recommendation → price |
     | (c) Commodities with at least one observation in the stale window at a VERIFIED, active market with coordinates | commodity (`CommodityAvailabilityPort`) | price | price → commodity |
  2. **Pinned thresholds in comparisons.** For a comparison or simulation, recommendation passes the thresholds of the run's weights version into price's selection call. Price uses them for the stale window and `freshnessStatus`, never the port, so `selling_options.freshness_status` always matches `recommendation_runs.weights_id`.
  3. Recommendation and alert obtain (a) through the price module's existing interface (recommendation → price, alert → price). Alert obtains (b) the same way. Neither needs a new dependency.
  4. Ports are read-only, synchronous, in-process calls. They do not cache across requests in Phase 1, except that the owning module may serve them from its own Redis reference cache (ADR-0018).
- **Rationale:**
  - It keeps ADR-0013's static graph acyclic and unchanged, keeps every table read inside its owner, and needs no new table, column or infrastructure.
  - It is the standard dependency-inversion pattern for a modular monolith, and package-dependency tests can still verify it.
- **Alternatives Considered:**
  - Push model: ingestion pushes run status, and recommendation pushes thresholds into price, on change and at startup. That puts state in the price module and needs ordering at startup or a new column (a migration).
  - New direct dependencies: recommendation → ingestion and alert → recommendation would be acyclic, but alert → ingestion (with ingestion → alert), commodity → price (with price → commodity), price → ingestion and price → recommendation would close cycles (ADR-0013 item 3). (a) for price and alert, (b) for price and (c) would stay unsolved.
  - Moving the freshness thresholds out of `recommendation_weights`: contradicts spec §20, which places them there.
  - Serving the selector list from the recommendation module: departs from spec §30, which maps the commodity selector to `/api/v1/commodities/*`.
- **Consequences:**
  - `COMPONENT_ARCHITECTURE.md` §4.1 and `TABLE_OWNERSHIP.md` §4 record the three ports as permitted runtime paths over existing edges. RD-038 is resolved.
  - The backend implements `sourceStatus`, freshness classification and the `listCommodities` availability filter through these ports only. Comparisons pass their pinned thresholds.
  - The contract shape is unaffected.
  - Package-dependency tests verify that no other path exists.
- **Source / Evidence:** ADR-0006 (consequences), ADR-0010, ADR-0013 item 3, ADR-0022 item 9, ADR-0026 item 3; `docs/database/TABLE_OWNERSHIP.md` §4, §5; `docs/database/DATABASE_ARCHITECTURE.md` §10.6; `docs/architecture/COMPONENT_ARCHITECTURE.md` §4.1; `contracts/openapi.yaml`.
- **Revision:** 2026-10-04, after the Task 8 data review: cycle analysis corrected, and comparisons pinned to their own thresholds (item 2).
- **Affected artifacts:** `docs/architecture/COMPONENT_ARCHITECTURE.md`; `docs/database/TABLE_OWNERSHIP.md`; `docs/api/PROVENANCE_AND_FRESHNESS.md` §4; `docs/api/API_INDEX.md`; `contracts/openapi.yaml` (`listCommodities` description); `docs/REQUIRES_DECISION.md` (RD-038).
- **Approval:** Human Project Owner, 2026-10-04, conversation ("TASK 8.6 — FINAL DECISION LOCK + SECURITY SIGN-OFF + SKILL POPULATION + PROJECT READINESS": "RD-038 → Option 1 … Approve ADR-0035 as revised.").

### ADR-0036 — Earth radius for haversine distances

- **Decision ID:** ADR-0036
- **Date:** 2026-10-04
- **Status:** APPROVED
- **Title:** Earth radius for haversine distances
- **Context:**
  - RD-039, found while writing the contract's calculation-precision rules (Task 8). The spec requires haversine distance (FR-06, §16, §21, §32), but no authoritative source names the Earth radius.
  - The radius affects `distanceKm`, the transport cost, the radius filter, the `SUITABLE_DISTANCE` reason (< 50 km), ranking ties, and the hand-computed fixtures that §48 requires to match 100 %.
  - Coordinates are WGS84 decimal degrees (`docs/database/DATABASE_ARCHITECTURE.md` A-DB-02).
  - The choice changes two-decimal results: between the two synthetic district centroids of the API mocks, 98.49 km at this radius becomes 98.60 km at the equatorial radius (Task 8 data review). Results near 50 km and near the search radius can change classification.
  - VERIFIED: the WGS 84 defining parameters a = 6 378 137 m and 1/f = 298.257 223 563 give b = 6 356 752.314 m and the mean radius R1 = (2a + b) / 3 = 6 371 008.771 m. The same value, 6 371 008.7714 m, is the GRS 80 mean radius.
- **Decision:** The haversine distance uses R = 6 371.0088 km, the WGS 84 / GRS 80 mean radius R1, computed in double precision. The double is converted to a decimal through its shortest round-trip representation, then rounded half-up to 2 decimal places before any further use (`docs/api/API_CONVENTIONS.md` §4.1). VERIFIED for the Java 25 baseline: `BigDecimal.valueOf(double)` uses `Double.toString(double)`, which selects the shortest decimal that rounds to the given double (OpenJDK 25 API documentation, via Context7).
- **Rationale:** It is the radius conventionally used with haversine on WGS84 coordinates, and it is fully determined by the official ellipsoid. Fixing it makes server results and QA fixtures reproducible.
- **Alternatives Considered:**
  - 6 371 km (rounded): equally defensible, but less traceable to the ellipsoid.
  - Equatorial radius 6 378.137 km: overstates distances at Indian latitudes.
  - Ellipsoidal (Vincenty) distance: more precise, but the spec names haversine.
- **Consequences:** The transport module and the §48 fixtures use this constant. RD-039 is resolved. The synthetic API mocks and the contract's coherence check already use it.
- **Source / Evidence:** Spec FR-06, §16, §21, §32, §47 Q6, §48; `RECOMMENDATION_ENGINE.md` §4.4; `DATABASE_ARCHITECTURE.md` A-DB-02; WGS 84 defining parameters (NGA); GRS 80 mean radius.
- **Affected artifacts:** `docs/architecture/RECOMMENDATION_ENGINE.md` §4.4; `docs/api/API_CONVENTIONS.md` §4.1; `docs/api/CONTRACT_VALIDATION.md`; `frontend/__mocks__/api/README.md`; `docs/REQUIRES_DECISION.md` (RD-039).
- **Approval:** Human Project Owner, 2026-10-04, conversation ("TASK 8.6 — FINAL DECISION LOCK + SECURITY SIGN-OFF + SKILL POPULATION + PROJECT READINESS": "RD-039 → Option 1 → Earth radius = 6,371.0088 km. → Approve ADR-0036.").

### ADR-0037 — Account erasure as an in-application operator procedure

- **Decision ID:** ADR-0037
- **Date:** 2026-10-04
- **Status:** APPROVED
- **Title:** Account erasure as an in-application operator procedure
- **Context:**
  - RD-040. ADR-0029 item 2 decides that Phase 1 erasure is "an audited admin procedure", with no self-service endpoint. Its consequences say "Erasure is documented in the operations runbook". It does not say how the procedure runs.
  - ADR-0020 items 1 and 7 list the audited admin endpoints. Erasure is not among them.
  - ADR-0013 item 3 approves admin → auth for "Admin bootstrap and the audited account-erasure procedure" (`docs/database/TABLE_OWNERSHIP.md` §4). The bootstrap (ADR-0028 item 2) is an in-application action that does not use HTTP.
  - `admin_audit_events.action` includes `ACCOUNT_ERASED`. `actor_user_id` is NOT NULL, so an ADMIN must be the recorded actor.
  - Security review finding SR-08: an HTTP erasure operation would create a remote, irreversible capability.
  - Drafted as option A on 2026-10-04 (Task 8). Redrafted to option C after the Task 8.5 decision packet and approved in Task 8.6.
- **Decision:**
  1. **No HTTP operation erases an account.** Phase 1 erasure is an in-application operator procedure of the backend, and the API contract contains no erasure operation.
  2. **Execution.**
     - An operator with server access runs the procedure as a one-off command of the backend application, on the deployment host (ADR-0021), with the same build and configuration.
     - The operations runbook documents it (ADR-0029 consequences).
  3. **Module boundary.** The admin module performs the erasure through the auth module's interface (admin → auth, ADR-0013). Every table is changed only by its owner.
  4. **Inputs:**
     - the FARMER's phone number (E.164, ADR-0016);
     - the identifier of the acting ADMIN, which must be an existing, active ADMIN account;
     - a request reference;
     - a reason.
  5. **Scope.** Only FARMER accounts are erased. ADMIN accounts are deactivated, never deleted (ADR-0029; ADR-0041).
  6. **Refusal.** The procedure changes nothing and writes no audit row when the number has no FARMER account, or the named actor is not an active ADMIN.
  7. **Effects.**
     - Deletion cascades as ADR-0029 states: profile, alerts, events and refresh tokens are deleted; searches have `farmer_profile_id` set to NULL.
     - Refresh fails at once. Any outstanding access token of the account answers 401 on every operation that evaluates it (ADR-0042, SR-04).
  8. **Audit.**
     - In the same transaction as the deletion, the procedure writes one `admin_audit_events` row with action `ACCOUNT_ERASED`, the named ADMIN as actor, and the erased user's id as target.
     - The details hold the request reference, the reason, and the phone number masked to its last two digits. No other personal data is recorded.
- **Rationale:**
  - It matches ADR-0029's "procedure" and runbook wording.
  - It uses the approved admin → auth path, like the non-HTTP bootstrap in the same approval row.
  - It needs no change to ADR-0020's endpoint list.
  - It adds no remote, irreversible deletion capability (CLAUDE.md rule 16).
- **Alternatives Considered:**
  - (A) An authenticated admin HTTP operation: easier to operate, but a stolen admin access token or a compromised admin account could erase farmers irreversibly. It would also need safeguards no source defines, and an extension of ADR-0020.
  - (B) A database runbook with manual SQL: bypasses the module interface, needs privileged database access for a routine request, and relies on a hand-written audit row.
- **Consequences:**
  - No contract change. The contract keeps no erasure operation, and `ACCOUNT_ERASED` is written only by this procedure.
  - `COMPONENT_ARCHITECTURE.md` records the operator procedures as an entry point of the backend. `DEPLOYMENT_ARCHITECTURE.md` notes where they run, and `SECURITY_ARCHITECTURE.md` records the boundary. The runbook, not created yet, gives the steps.
  - Tests cover the cascade, the refusal cases, and the masked audit row.
  - Finding SR-08 is resolved. A claimed phone number (SR-01) is freed by this procedure.
- **Source / Evidence:** ADR-0013 item 3, ADR-0016, ADR-0020, ADR-0021, ADR-0028 item 2, ADR-0029 item 2; `docs/database/DATA_DICTIONARY.md` (`admin_audit_events`); `docs/database/TABLE_OWNERSHIP.md` §4; `docs/decision-review/TASK_8_5_HUMAN_DECISION_PACKET.md` (RD-040); `docs/security/TASK_8_5_SECURITY_REVIEW.md` (SR-08); DPDP Act 2023 sections 8(7) and 12 (design alignment, not a legal opinion; ADR-0029).
- **Affected artifacts:** `docs/architecture/COMPONENT_ARCHITECTURE.md`; `docs/architecture/DEPLOYMENT_ARCHITECTURE.md`; `docs/architecture/SECURITY_ARCHITECTURE.md`; `docs/database/TABLE_OWNERSHIP.md` §4; `contracts/openapi.yaml` (`AdminAuditAction` description); `docs/REQUIRES_DECISION.md` (RD-040).
- **Approval:** Human Project Owner, 2026-10-04, conversation ("TASK 8.6 — FINAL DECISION LOCK + SECURITY SIGN-OFF + SKILL POPULATION + PROJECT READINESS": "RD-040 → Option C → In-application operator procedure without an HTTP erasure endpoint. … Redraft ADR-0037 to match Option C and approve it.").

### ADR-0038 — Transport-cost label shows the applied rate

- **Decision ID:** ADR-0038
- **Date:** 2026-10-04
- **Status:** APPROVED
- **Title:** Transport-cost label shows the applied rate
- **Context:**
  - RD-041. Spec §32 (line 1458) says transport cost "must always show" "**Estimated** — based on ₹8/km assumption. Actual costs will vary." Spec §49 (lines 2128, 2138) repeats "based on ₹8/km assumption".
  - The same §32 lists `ratePerKm` under "Configurable Parameters (Admin-Managed in `transport_assumptions` table)" and says "Farmer can override `ratePerKm` and `maxDistanceKm` in the simulator." §17 gives a ₹3–₹20/km slider.
  - ADR-0020 item 1 lets admins publish new transport-assumption versions, and ADR-0026 applies What-If overrides.
  - [CONFLICT] inside the specification: whenever the rate is not 8, the literal label would misstate the basis of the estimate it labels.
- **Decision:**
  1. The mandatory transport-cost label shows the rate the estimate actually used: "Estimated — based on ₹{appliedRatePerKm}/km assumption. Actual costs will vary."
     - `{appliedRatePerKm}` is `transportAssumptions.appliedRatePerKm` from the same response: the configured rate, or the simulation override.
     - In the client-side What-If it is the rate the browser applied.
  2. The §49 claim forms use the same rate, for example "Transport cost: ESTIMATED based on ₹{appliedRatePerKm}/km assumption".
  3. A fixed "₹8/km" is never displayed when the applied rate differs. At the default rate of 8, the text is identical to the specification.
  4. Localized versions keep the same structure and the same number. Number formatting per locale is a UI/UX matter (role file 02).
  5. **Departure from the specification:** the literal "₹8/km" wording quoted above is replaced by the applied rate. It is recorded as departure D-14 and authorized explicitly by the Human Project Owner. The specification is not edited (CLAUDE.md §E).
- **Rationale:** §32 itself makes the rate configurable and overridable. A fixed number would be false whenever the rate differs, which breaks §49's claims discipline and explainability (P2). The template keeps the specification's wording exactly at the default.
- **Alternatives Considered:**
  - (B) Keep the literal text and never let the rate differ from 8: makes admin rate updates and the What-If rate slider unusable, against §32, §17, ADR-0020 and ADR-0026.
  - (C) Keep the literal text and add a second line with the applied rate: two different rates on one screen, and a false first line.
- **Consequences:**
  - No API change: the contract already returns `appliedRatePerKm` and `ratePerKmSource`.
  - Role file 02 (Task 9) specifies the localized copy. `FRONTEND_CONSUMPTION.md` §2 and the `TransportAssumptionsApplied` description state the rule.
  - UI tests check the label against `appliedRatePerKm`.
- **Source / Evidence:** Spec §17, §32 (lines 1436–1463), §49 (lines 2128, 2138), P2; ADR-0020 item 1; ADR-0026; ADR-0027; `docs/decision-review/TASK_8_5_HUMAN_DECISION_PACKET.md` (RD-041).
- **Affected artifacts:** departures table (D-14); `docs/api/FRONTEND_CONSUMPTION.md`; `contracts/recommendation.schema.json` (`TransportAssumptionsApplied` description); role file 02 (when created); `docs/REQUIRES_DECISION.md` (RD-041).
- **Approval:** Human Project Owner, 2026-10-04, conversation ("TASK 8.6 — FINAL DECISION LOCK + SECURITY SIGN-OFF + SKILL POPULATION + PROJECT READINESS": "RD-041 → Option A … Explicitly authorize the departure from the literal "₹8/km" wording in the master specification").

### ADR-0039 — Server-side district-centroid origins and the recommendation → geography dependency

- **Decision ID:** ADR-0039
- **Date:** 2026-10-04
- **Status:** APPROVED
- **Title:** Server-side district-centroid origins and the recommendation → geography dependency
- **Context:**
  - RD-042. Spec §28: "PII minimization | No home address collected; district centroid only | Frontend + Backend". FR-06 measures distance from "farmer's district centroid".
  - ADR-0023 item 1 sends the selected district's centroid as `originLat` and `originLon`.
  - ADR-0029 item 2 keeps searches on erasure only because "Searches hold only district-centroid origins (§28)".
  - `selling_searches.origin_lat` and `origin_lon` are PERSONAL and kept indefinitely.
  - The contract accepted any in-range coordinates, and ADR-0013 item 3 approves no recommendation → geography dependency. Geography is a leaf. Security finding SR-09.
- **Decision:**
  1. **New approved dependency: recommendation → geography.** It is used only to read district centroids. It amends ADR-0013 item 3. The graph stays acyclic, and geography remains a leaf.
  2. **Validation rule.**
     - `compareSellingOptions` accepts an origin only when (`originLat`, `originLon`) equals, at 6 decimal places, the stored centroid of a district: the values `listDistricts` publishes.
     - Otherwise it answers 400 `VALIDATION_FAILED`, with `errors[]` naming `originLat` and `originLon` and field code `INVALID_VALUE`.
     - The check runs before anything is recorded, so a rejected request leaves no search, run or option.
  3. **Request shape unchanged:** `originLat` and `originLon` stay as ADR-0023 item 1 defines them.
  4. **Status choice.** This is a documented exception to the convention that 400 covers only checks that need no stored data. The set of valid origins is published reference data that the client copies from `listDistricts`, so a non-matching origin is an invalid field value, not a missing record.
  5. **Simulation** reuses the stored, already validated origin of its comparison.
- **Rationale:**
  - §28 assigns the control to the backend as well as the frontend.
  - ADR-0029's anonymization depends on it.
  - A direct edge keeps geography a leaf. A port implemented by geography would make geography depend on recommendation's package.
  - The existing request fields stay valid for the real frontend.
- **Alternatives Considered:**
  - Send `originDistrictId` instead: cleaner semantics, but it changes ADR-0023 item 1 and departs from the request fields in spec §16 and §21.
  - Accept the residual risk: precise coordinates from a buggy or modified client would be stored indefinitely and survive anonymization.
  - A read-only port implemented by geography: geography would lose its leaf property.
- **Consequences:**
  - `COMPONENT_ARCHITECTURE.md` (§4.1, §6) and `TABLE_OWNERSHIP.md` §4 add the dependency, and `SECURITY_ARCHITECTURE.md` records where the control is enforced.
  - The contract documents the rule on `compareSellingOptions` and `CompareRequest`. `API_CONVENTIONS.md` §6 records the 400 exception. A new mock shows the rejection.
  - Findings SR-09 and SR-02 are resolved.
  - Tests cover exact matches, a near-miss in the sixth decimal place, and coordinates of no district.
- **Source / Evidence:** Spec §16, §21 step 3, §28 (line 1345), FR-06, §47 Q6; ADR-0013 item 3, ADR-0014, ADR-0023 item 1, ADR-0029 item 2; `docs/database/DATA_DICTIONARY.md` (`selling_searches`, `districts`); `docs/database/TABLE_OWNERSHIP.md`; `docs/security/TASK_8_5_SECURITY_REVIEW.md` (SR-02, SR-09).
- **Affected artifacts:** ADR-0013 status note; `docs/architecture/COMPONENT_ARCHITECTURE.md`; `docs/architecture/RECOMMENDATION_ENGINE.md`; `docs/architecture/SECURITY_ARCHITECTURE.md`; `docs/database/TABLE_OWNERSHIP.md`; `docs/database/DATA_DICTIONARY.md` (`selling_searches` origin columns); `contracts/openapi.yaml`; `contracts/recommendation.schema.json`; `docs/api/API_CONVENTIONS.md`; `docs/api/FRONTEND_CONSUMPTION.md`; `frontend/__mocks__/api/`; `docs/REQUIRES_DECISION.md` (RD-042).
- **Approval:** Human Project Owner, 2026-10-04, conversation ("TASK 8.6 — FINAL DECISION LOCK + SECURITY SIGN-OFF + SKILL POPULATION + PROJECT READINESS": "RD-042 → Option A → Backend server-side enforcement of district-centroid origins through an approved recommendation → geography dependency.").

### ADR-0040 — Comparison estimates beyond the stored precision

- **Decision ID:** ADR-0040
- **Date:** 2026-10-04
- **Status:** APPROVED
- **Title:** Comparison estimates beyond the stored precision
- **Context:**
  - RD-043. ADR-0023 item 1 bounds the compare quantity only by > 0, "with no further limits".
  - `selling_options.estimated_gross_value`, `estimated_transport_cost` and `estimated_net_realization` are `numeric(14,2)`, at most 999 999 999 999.99 in absolute value.
  - A schema-valid extreme quantity times a plausible price exceeds that precision. Without a rule, the database would fail and the API would answer 500.
- **Decision:**
  1. Before recording a comparison, the server checks that every estimate of every option fits the stored precision: an absolute value of at most 999 999 999 999.99.
  2. If one does not, `compareSellingOptions` answers 422 with:
     - code `ESTIMATE_OUT_OF_RANGE`;
     - title "Estimate out of range";
     - type `/problems/estimate-out-of-range`;
     - `errors[]` naming `quantity` with field code `OUT_OF_RANGE`.
  3. Nothing is recorded: no search, run or option.
  4. No quantity cap is added (ADR-0023 item 1 unchanged), and no column is widened.
  5. `simulateSellingOptions` is unaffected, because its quantity is bounded (100–5 000 kg) and it records nothing.
- **Rationale:** Keeps ADR-0023 and the approved column design unchanged. Turns an undefined database-induced 500 into a documented error. The addition is additive, because `ErrorCode` is extensible.
- **Alternatives Considered:**
  - Cap the quantity: needs an invented product limit and amends ADR-0023.
  - Widen the columns: about 28-digit columns would be needed for every input combination, and browsers lose paise precision above about 9 × 10¹³ rupees anyway.
- **Consequences:**
  - `error-response.schema.json` gains the code with its status, type and title rule.
  - `openapi.yaml` gives `compareSellingOptions` its own 422 response allowing it.
  - `API_CONVENTIONS.md` §6.1 lists the code, and `FRONTEND_CONSUMPTION.md` lists the error state (copy in role file 02).
  - A mock covers the case. Tests cover the boundary value.
- **Source / Evidence:** ADR-0023 items 1 and 4; `docs/database/DATA_DICTIONARY.md` (`selling_searches.quantity` `numeric(12,3)`, `selling_options.estimated_*` `numeric(14,2)`); spec §28, §29; MDN `Number.MAX_SAFE_INTEGER` (JavaScript number precision); `docs/decision-review/TASK_8_5_HUMAN_DECISION_PACKET.md` (RD-043).
- **Affected artifacts:** `contracts/error-response.schema.json`; `contracts/openapi.yaml`; `docs/api/API_CONVENTIONS.md`; `docs/api/FRONTEND_CONSUMPTION.md`; `docs/api/CONTRACT_VALIDATION.md`; `frontend/__mocks__/api/`; `docs/REQUIRES_DECISION.md` (RD-043).
- **Approval:** Human Project Owner, 2026-10-04, conversation ("TASK 8.6 — FINAL DECISION LOCK + SECURITY SIGN-OFF + SKILL POPULATION + PROJECT READINESS": "RD-043 → Option C … Approve the corresponding new error code/title as part of the contract change.").

### ADR-0041 — Account deactivation and reactivation as operator procedures

- **Decision ID:** ADR-0041
- **Date:** 2026-10-04
- **Status:** APPROVED
- **Title:** Account deactivation and reactivation as operator procedures
- **Context:**
  - RD-044. Spec §20 defines `users.is_active`, and ADR-0029 item 2 says admins "are deactivated, never deleted". No entry defined how deactivation happens.
  - The closed audit vocabulary (ADR-0020 item 6; `ck_admin_audit_events_action`) had no deactivation action. Security finding SR-05.
- **Decision:**
  1. **Procedure type.** Deactivating and reactivating an account (FARMER or ADMIN) are in-application operator procedures of the same kind as erasure (ADR-0037). There is no HTTP endpoint.
  2. **Module boundary.** The admin module performs them through the auth module's interface (admin → auth, ADR-0013). The dependency's purpose is extended to cover them.
  3. **Inputs:**
     - the account's identifier (phone number for FARMER, email for ADMIN; ADR-0016);
     - the acting ADMIN, which must be an existing, active ADMIN account;
     - a reason.
  4. **Deactivation** sets `is_active` to false and revokes every refresh-token family of the account, in one transaction. **Reactivation** sets it to true and restores no token: the user signs in again.
  5. **Effects of an inactive account:**
     - login answers 401 `INVALID_CREDENTIALS`;
     - refresh answers 401 `REFRESH_TOKEN_INVALID`;
     - every operation that evaluates the access token answers 401 `UNAUTHORIZED` (ADR-0042, SR-04).
  6. **Refusal cases.** The procedure refuses, and changes nothing, when:
     - the account does not exist;
     - it is already in the requested state;
     - the actor is not an active ADMIN;
     - it would deactivate the last active ADMIN.

     The last case is needed because both procedures require an active ADMIN as actor, and the bootstrap of ADR-0028 runs only when no ADMIN exists.
  7. **Audit.** One `admin_audit_events` row per change, in the same transaction:
     - action `ACCOUNT_DEACTIVATED` or `ACCOUNT_REACTIVATED`;
     - the named ADMIN as actor and the user's id as target;
     - details limited to the account's role and the reason, with no phone number or email.
  8. **Vocabulary.** `ACCOUNT_DEACTIVATED` and `ACCOUNT_REACTIVATED` are added to the closed audit vocabulary: the database CHECK and the contract's `AdminAuditAction`.
- **Rationale:**
  - It closes the incident-response gap of SR-05 with an audited path and no remote capability.
  - It reuses the erasure procedure's boundary and actor semantics.
- **Alternatives Considered:**
  - Admin HTTP operations: a remote capability to lock out any account, plus new endpoints and screens.
  - Manual SQL: unaudited.
- **Consequences:**
  - `DATA_DICTIONARY.md` and `INDEXES_AND_CONSTRAINTS.md` list the two new actions, and `TABLE_OWNERSHIP.md` §4 extends the admin → auth purpose.
  - The contract enumeration gains the values without a new operation. The contract checker treats them as operator-procedure actions.
  - `SECURITY_ARCHITECTURE.md` records the procedure. The runbook gives the steps.
  - Finding SR-05 is resolved.
- **Source / Evidence:** Spec §20; ADR-0013 item 3, ADR-0016, ADR-0020 item 6, ADR-0028 item 2, ADR-0029 item 2, ADR-0037; `docs/security/TASK_8_5_SECURITY_REVIEW.md` (SR-05); `docs/decision-review/TASK_8_5_HUMAN_DECISION_PACKET.md` (RD-044).
- **Affected artifacts:** `docs/database/DATA_DICTIONARY.md`; `docs/database/INDEXES_AND_CONSTRAINTS.md`; `docs/database/TABLE_OWNERSHIP.md`; `contracts/openapi.yaml` (`AdminAuditAction`); `docs/architecture/SECURITY_ARCHITECTURE.md`; `docs/architecture/COMPONENT_ARCHITECTURE.md`; `docs/REQUIRES_DECISION.md` (RD-044).
- **Approval:** Human Project Owner, 2026-10-04, conversation ("TASK 8.6 — FINAL DECISION LOCK + SECURITY SIGN-OFF + SKILL POPULATION + PROJECT READINESS": "RD-044 → Option C → Account deactivation/reactivation through an in-application operator procedure without a public HTTP endpoint. → Extend the audit vocabulary").

### ADR-0042 — Task 8.5 security review sign-off

- **Decision ID:** ADR-0042
- **Date:** 2026-10-04
- **Status:** APPROVED
- **Title:** Task 8.5 security review sign-off
- **Context:** CLAUDE.md rule 16 requires human review of security-sensitive changes. `docs/security/TASK_8_5_SECURITY_REVIEW.md` lists findings SR-00 to SR-16 for API contract 1.0.0 and the related decisions. The Human Project Owner gave an outcome for each.
- **Decision:**
  1. **Outcomes:**

     | Finding | Outcome | Result |
     |---|---|---|
     | SR-00 | RESOLVED | The security-sensitive contract is approved with this review incorporated |
     | SR-01 | ACCEPTED | Registration 409 reveals a registered number, and numbers can be claimed. Residual Phase 1 risk, because OTP/SMS is outside Phase 1 (§39). Revisit when Phase 2 OTP exists. A claimed number is freed by erasure (ADR-0037) |
     | SR-02 | ACCEPTED | Accepted once ADR-0039 is in force: a run can only carry a district centroid |
     | SR-03 | ACCEPTED | The 15-minute stateless access token is accepted for Phase 1 |
     | SR-04 | RESOLVED | Rule 2 below |
     | SR-05 | RESOLVED | By ADR-0041 |
     | SR-06 | RESOLVED | Rule 3 below |
     | SR-07 | ACCEPTED | Informational. No absolute session lifetime in Phase 1 |
     | SR-08 | RESOLVED | By ADR-0037. No HTTP erasure operation |
     | SR-09 | RESOLVED | By ADR-0039. Precise client coordinates are never stored as comparison origins |
     | SR-10 | ACCEPTED | Password-only admin sign-in. No MFA is introduced, because the SSOT does not require it |
     | SR-11 | IMPLEMENTATION GATE | The final CSP is verified during frontend implementation, before release |
     | SR-12 | ACCEPTED | Targeted login lockout is accepted for Phase 1 |
     | SR-13 | NO CHANGE | No general per-IP limit is added. The limits of ADR-0017 stay. Tuning is deployment and observability work based on real traffic |
     | SR-14 | RESOLVED | Rule 4 below. Documentation only, no new product decision |
     | SR-15 | ACCEPTED | The existing non-revealing authentication semantics |
     | SR-16 | RESOLVED | Rule 5 below |
  2. **Account status on token use (SR-04).** Every operation that evaluates an `Authorization` header resolves the caller's account. That covers the alert and admin operations, and compare and simulate when a token is sent. An inactive or removed account answers 401 `UNAUTHORIZED` with `error="invalid_token"`, never undefined behavior or a 500. The client then refreshes, which is refused, and retries operations that allow anonymous access without the token.
  3. **Refresh cookie name (SR-06).** The refresh cookie is `__Secure-agrinex_refresh`.
     - Its attributes stay as ADR-0017 item 3 fixes them: `HttpOnly; Secure; SameSite=Strict; Path=/api/v1/auth`, and a 7-day lifetime.
     - The server reads only the full prefixed name.
     - `__Host-` is not used, because it requires `Path=/` (MDN `Set-Cookie`, cookie prefixes).
  4. **Rate-limited refresh (SR-14).** A 429 from refresh is not a sign-out. The client waits for `Retry-After` and retries once. A request over the limit is rejected before its token is processed, so the token stays valid.
  5. **Logging (SR-16).** Request and response bodies are never logged. Personal data, including phone numbers and coordinates, never appears in logs. This extends ADR-0020 item 5, which already excludes secrets, `Authorization` headers, cookies and the `api-key` parameter.
- **Rationale:** Each outcome follows the review's analysis. No speculative control is added: there is no MFA, no new rate limit and no absolute session lifetime. Accepted risks are recorded so that later phases revisit them.
- **Alternatives Considered:** The alternatives for each finding are in the security review §3.
- **Consequences:**
  - **Contract:** account-status semantics, the cookie name (with a header pattern), and the client refresh rule.
  - **Security architecture:** logging prohibition, cookie prefix, centroid enforcement, operator procedures and accepted risks.
  - **Review record:** the security review's §4 records the sign-off.
  - **Frontend implementation:** the CSP gate (SR-11) is a release gate.
- **Source / Evidence:** `docs/security/TASK_8_5_SECURITY_REVIEW.md`; ADR-0016, ADR-0017, ADR-0020, ADR-0028, ADR-0029, ADR-0037, ADR-0039, ADR-0041; MDN `Set-Cookie` (cookie prefixes); RFC 9700 §4.14.2.
- **Affected artifacts:** `contracts/openapi.yaml`; `docs/api/API_CONVENTIONS.md` §7, §9; `docs/api/FRONTEND_CONSUMPTION.md` §4; `frontend/__mocks__/api/` (cookies); `docs/architecture/SECURITY_ARCHITECTURE.md`; `docs/security/TASK_8_5_SECURITY_REVIEW.md` §4.
- **Approval:** Human Project Owner, 2026-10-04, conversation ("TASK 8.6 — FINAL DECISION LOCK + SECURITY SIGN-OFF + SKILL POPULATION + PROJECT READINESS": "SECURITY HUMAN SIGN-OFF", outcomes SR-00 to SR-16 as listed).

### ADR-0043 — Approved UI/UX direction, screen approval and recorded delegations (H1, H6)

- **Decision ID:** ADR-0043
- **Date:** 2026-10-04
- **Status:** APPROVED
- **Title:** Approved UI/UX direction, screen approval and recorded delegations (H1, H6)
- **Context:**
  - Task 9 explored three design directions on synthetic data and set out the decisions H1 to H6 for the Human Project Owner (`docs/decision-review/TASK_9_DESIGN_DIRECTION_PACKET.md` §8).
  - ADR-0027: role file 02 is the design source of record; a screen is approved when role file 02 references it and the approval is recorded in SSOT index area 5 (screen, design reference, date, approver). Tool drafts are working drafts.
  - VERIFIED: the Concept A drafts cover the Phase 1 farmer journeys on mobile and part of desktop; some requested drafts timed out, and the drafts carry tool-invented defects listed in the packet §6.1.
- **Decision:**
  1. **Direction.** Concept A, "Hisaab" (the honest ledger), is the approved base design direction of AgriNex. "Hisaab" is an internal design name; the product name shown to users is only AgriNex.
  2. **Approved borrowings**, each rendered in Hisaab's visual language and tokens, never as a second design language:
     - (a) from Concept B: the step-by-step, one-question-at-a-time comparison input on mobile;
     - (b) from Concept B: the answer strip at the top of the results;
     - (c) from Concept C: the trade-off sentence in the winner callout (wording: ADR-0045);
     - (d) from Concept C: the shared-scale transport visualization in each option.
  3. **Screen approval (H6).** Role file 02 is approved as the design source of record for every Phase 1 screen it specifies, including screens with no rendered draft (the account screens S10 and S14 to S16, desktop detail and What-If, the admin screens A02 to A05). The Concept A Stitch drafts and the borrowed-pattern drafts listed in SSOT index area 5 are approved as design references for layout and visual language only: the defects named in the packet §6.1 are excluded, and where a draft and role file 02 differ, role file 02 governs (ADR-0027 item 3). Concepts B and C stay exploration reference material, not an approved production direction.
  4. **Recorded delegations** (decision-log rule 3). In the same conversation the Human Project Owner delegated two choices to the agent maintaining this repository:
     - (a) the final recommendation wording (H3): "You are authorized to choose the final wording based on your research and the actual AgriNex logic. Do not ask me another H3 question." Exercised in ADR-0045.
     - (b) the password-recovery mechanism (H5): "CHOOSE THE BEST-PRACTICE SECURE APPROACH YOURSELF. … You decide the best mechanism based on security best practice and the existing AgriNex architecture." Exercised in ADR-0048.

     The delegation covers only these two choices in this lock. It does not carry over to later sessions or decisions.
  5. **Change control.** Refinements are allowed only for consistency, usability, responsiveness, accessibility, implementation readiness and correctness against approved product behavior. Changing the direction needs a new entry with explicit human approval.
- **Rationale:** The owner's choice follows the packet's comparison (Hisaab highest on verifiable reasoning and honest treatment of small differences; packet §4, §7). The borrowings remove Hisaab's main weaknesses: mobile input weight and an answer not stated in words.
- **Alternatives Considered:** Concept B alone (simplest, but its arithmetic was one tap away); Concept C alone (most distinctive, hardest to make accessible); other combinations (packet §7).
- **Consequences:**
  - Role file 02 becomes AUTHORITATIVE; its candidate values for Hisaab become the approved tokens, and the B and C values stay as reference.
  - SSOT index area 5 records the approval and answers "which UI/UX direction is approved?": Hisaab with the four borrowings.
  - The frontend design gate (CLAUDE.md §M; `FRONTEND_ARCHITECTURE.md` §6) is satisfied for the approved screens. Frontend implementation still follows the enforced development order (spec §42).
  - In the Task 9 Figma file, the "Theme A · Hisaab" collection holds the approved values; the B and C collections are reference.
- **Source / Evidence:** `docs/decision-review/TASK_9_DESIGN_DIRECTION_PACKET.md` (§2, §4, §6, §7, §8); `docs/02_AgriNex_UIUX_Phase1_Specification.md`; ADR-0027; spec §6, §40, §43, §44; `docs/decision-review/TASK_9_FINAL_DECISION_LOCK.md`.
- **Affected artifacts:** `docs/02_AgriNex_UIUX_Phase1_Specification.md`; `docs/00_SSOT_INDEX.md` (area 5); `docs/decision-review/TASK_9_DESIGN_DIRECTION_PACKET.md` (status); `.claude/skills/agrinex-design/SKILL.md`, `.claude/skills/agrinex-frontend/SKILL.md`.
- **Approval:** Human Project Owner, 2026-10-04, conversation ("TASK 9 FINAL — HUMAN DECISION LOCK + CONTRACT/UX CONSISTENCY + FINAL READINESS GATE": "H1 — FINAL DESIGN DIRECTION. APPROVED: Concept A — Hisaab. … The following two concept borrowings are ALSO approved …"; "H6 is APPROVED consistently with H1."; the delegations quoted in item 4).

### ADR-0044 — Approval of the role file 02 design decisions PDD-01 to PDD-14 (H2)

- **Decision ID:** ADR-0044
- **Date:** 2026-10-04
- **Status:** APPROVED
- **Title:** Approval of the role file 02 design decisions PDD-01 to PDD-14 (H2)
- **Context:** Role file 02 §17 listed fourteen proposed design decisions (PDD-01 to PDD-14) made in Task 9 to fill design gaps. Each was reviewed against the approved direction (ADR-0043), the specification, the binding entries, the contract and the frontend, security and database architecture (`docs/decision-review/TASK_9_FINAL_DECISION_LOCK.md` §3). Thirteen are consistent as written. PDD-14 named RD-045 as pending.
- **Decision:**
  1. PDD-01 to PDD-13 are approved and locked as written in role file 02 §17.
  2. PDD-14 is approved with its wording corrected to the outcome of RD-045: "Winner callout states the trade-off in rupees, with the arithmetic; wording per ADR-0045."
  3. The farmer-first principles of role file 02 §3, on which the PDDs rest, are adopted with the approved specification.
  4. The frontend implements these decisions as written. Reinterpreting or changing one needs a new decision-log entry.
- **Rationale:** The owner accepted the Hisaab design and the existing PDDs ("THE HISAAB DESIGN AND THE EXISTING PDD DECISIONS ARE ACCEPTED"). The review found no conflict that would need more than the PDD-14 wording correction.
- **Alternatives Considered:** Re-opening individual PDDs: not needed; none conflicts with a binding source.
- **Consequences:** Role file 02 §17 shows each PDD as APPROVED (ADR-0044). The labels "PROPOSED" for these decisions are removed.
- **Source / Evidence:** Role file 02 §3, §16, §17; ADR-0015, ADR-0019, ADR-0023, ADR-0025, ADR-0026; `docs/api/FRONTEND_CONSUMPTION.md` §3; `docs/decision-review/TASK_9_FINAL_DECISION_LOCK.md` §3.
- **Affected artifacts:** `docs/02_AgriNex_UIUX_Phase1_Specification.md` §17.
- **Approval:** Human Project Owner, 2026-10-04, conversation ("TASK 9 FINAL — HUMAN DECISION LOCK + CONTRACT/UX CONSISTENCY + FINAL READINESS GATE": "H2 — FINALIZE PDD-01 TO PDD-14 … THE HISAAB DESIGN AND THE EXISTING PDD DECISIONS ARE ACCEPTED.").

### ADR-0045 — Recommendation and winner wording

- **Decision ID:** ADR-0045
- **Date:** 2026-10-04
- **Status:** DECIDED
- **Title:** Recommendation and winner wording
- **Context:**
  - RD-045. Rank 1 is decided by the composite score (§16 "net_winner (rank 1 by composite score)"; ADR-0023 item 2; weights ADR-0024: 0.55 estimated net realization, 0.15 price, 0.10 trend, 0.10 freshness, 0.10 transport penalty). `netRealizationWinnerId` names that rank-1 market, and `rankingsDisagree` is true exactly when it differs from `priceOnlyWinnerId`. The rank-1 market therefore does not always have the highest estimated net realization; §46's own narrative notes a case where the highest-price market "still wins on net".
  - The specification's banner texts name rank 1 by net realization (§25 line 1254; §46 lines 1955–1956), which can be false on screen. §49 forbids "Guaranteed best market" and requires "Estimated net realization".
  - Research (2026-10-04; details in `docs/decision-review/TASK_9_FINAL_DECISION_LOCK.md` §2):
    - Google Flights separates a composite "Best" view, "ranked based on the best trade-off between price and convenience, including factors such as duration, number of stops…", from a single-metric "Cheapest" view (Google Travel Help, answer 7664728).
    - India's Consumer Protection (E-Commerce) Rules, 2020, rule 5(3)(f), require marketplaces to explain "the main parameters which, individually or collectively, are most significant in determining the ranking … and the relative importance of those main parameters … in plain and intelligible language". A 2026 amendment (G.S.R. 789(E), notified 2026-09-09, in force 2027-01-01, as summarized by LexOrbis) requires them in descending order of importance. AgriNex Phase 1 is not a marketplace, so the rule does not apply; it is used as a domestic benchmark for ranking transparency.
    - Microsoft HAX guideline 11: "Enable the user to access an explanation of why the AI system behaved as it did", with the warning that "the mere presence of an explanation has been shown to increase user trust. This may cause over-reliance".
    - Google PAIR, Explainability + Trust: "the user shouldn't trust the system completely … the user should know when to trust the system's predictions and when to apply their own judgement"; "data sources have to be part of your explanations".
    - Nielsen Norman Group, UX Guidelines for Recommended Content: "Clearly State the Source of Data"; "avoid adding vague descriptions".
- **Decision:**
  1. **Rank 1 of a comparison is labelled "Recommended"** (badge and answer strip). It is never called "Best", "Highest net", "most profitable" or "most money", and no label claims a guaranteed or actual outcome (§49).
  2. **Factual labels appear only where they are literally true**, computed from the response (`docs/api/FRONTEND_CONSUMPTION.md` §2.1):
     - "Highest price" on the option of `priceOnlyWinnerId` (an observed, reported price);
     - "Highest estimated net realization" on every option whose `estimatedNetRealization` equals the maximum over the options (all of them on a tie). It may sit on the recommended option too.
  3. **The ranking basis is always shown** with comparison results, its parameters in descending order of weight: "Ranked mainly by estimated net realization, also weighing price, trend, data freshness and transport."
  4. **Answer strip:** "Recommended: {market}", then "≈₹{net} estimated net realization — money left after estimated transport", then the distance, the report date and the freshness.
  5. **Winner callout** (only when `rankingsDisagree`), titled "Why the highest price is not ranked first", with the trade-off computed from the response (P = highest-price option, R = rank 1; `netDiff` = R's estimate − P's estimate):
     - when `netDiff` > 0: "{P} has the highest price (₹{price}/quintal), but after estimated transport {R} may leave about ₹{netDiff} more." and the arithmetic line "+₹{grossDiff} from the higher price · −₹{transportDiff} more estimated transport = ₹{netDiff} less at {P}";
     - when `netDiff` ≤ 0: "{P} has the highest price and about ₹{|netDiff|} more estimated net realization, but {R} is ranked first because the ranking also weighs price trend, data freshness and transport." (on equality: "the same estimated net realization"), followed by P's caution reasons when present, and the arithmetic line ending "= ₹{|netDiff|} more at {P}".

     The callout never claims that R leaves more money when its estimate is not higher.
  6. **What-If and simulations** rank by estimated net realization only (ADR-0026): rank 1 is labelled "Highest estimated net (what-if)", never "Recommended".
  7. **Estimates are worded as estimates** everywhere: "may leave", "about", "≈", "estimated". Reported prices are never prefixed with "≈".
  8. **Experiment 1 (§36)** keeps its method. Its report names the metric "estimated net difference between the recommended (rank-1) market and the highest-price market", reports negative values instead of dropping them, and keeps §36's honest framing.
  9. Localized wording follows ADR-0046. This is departure D-16.
- **Rationale:**
  - Truthful in every case: each label is either the system's own ranking ("Recommended", with its stated basis) or a fact computed from the same response.
  - It mirrors the established composite-versus-single-metric pattern (Google Flights), states the basis in plain language in descending order of importance (rule 5(3)(f) benchmark), explains why (HAX G11) and names the data sources (PAIR, NN/g) without inviting over-trust.
  - No contract or engine change is needed.
- **Alternatives Considered:**
  - RD-045 option B, the spec's label only when rank 1 also has the highest estimate: true, but the label would come and go between comparisons, and the farmer would still not see why rank 1 was chosen when it does not.
  - RD-045 option C, re-ranking by net realization only: a product and engine change to §16, outside H3's scope.
  - "Best", as travel sites use: a superlative claim that §49's claims discipline argues against.
- **Consequences:**
  - Role file 02 §7, §8 (S04, S05, S09) and §10 carry the wording; `docs/api/FRONTEND_CONSUMPTION.md` §2.1 the computation; `TESTING_ARCHITECTURE.md` the tests.
  - No contract change. `netRealizationWinnerId` keeps its name and meaning (ADR-0023 item 2); its description already states "rank 1 by composite score".
  - RD-045 is resolved.
- **Source / Evidence:** Spec §16 (line 745), §25 (line 1254), §36 (lines 1592–1600), §46 (lines 1955–1957), §49; FR-10; ADR-0023 item 2, ADR-0024, ADR-0026; `contracts/recommendation.schema.json` (`netRealizationWinnerId`, `priceOnlyWinnerId`, `rankingsDisagree`, `SellingEstimates`); support.google.com/travel/answer/7664728; consumerprotection.in, rule 5 of the Consumer Protection (E-Commerce) Rules, 2020; lexorbis.com summary of the 2026 amendment; microsoft.com/en-us/haxtoolkit, guideline 11; pair.withgoogle.com/chapter/explainability-trust; nngroup.com/articles/recommendation-guidelines (all fetched 2026-10-04).
- **Affected artifacts:** `docs/02_AgriNex_UIUX_Phase1_Specification.md`; `docs/api/FRONTEND_CONSUMPTION.md`; `docs/api/API_INDEX.md`; `docs/architecture/TESTING_ARCHITECTURE.md`; `docs/REQUIRES_DECISION.md` (RD-045).
- **Approval:** Decided under the delegation recorded in ADR-0043 item 4(a) (Human Project Owner, 2026-10-04).

### ADR-0046 — Marathi and Hindi interface copy: ownership and rules (H4)

- **Decision ID:** ADR-0046
- **Date:** 2026-10-04
- **Status:** APPROVED
- **Title:** Marathi and Hindi interface copy: ownership and rules (H4)
- **Context:** RD-046. Every farmer-facing screen must be in Marathi, Hindi and English with no untranslated hardcoded strings (§27, §48; F1.2). ADR-0015 covers localized reference names only. Task 9 drafted Marathi and Hindi strings in role file 02 §10, unreviewed.
- **Decision:**
  1. **Owner.** Claude, the AI assistant that maintains this repository, is responsible for authoring and reviewing all Marathi and Hindi interface copy. No separate human reviewer is required.
  2. **Rules for the copy:**
     - it preserves the meaning of the approved English, the actual product behavior, terminology and data semantics, and the approved UX decisions; it never changes product meaning;
     - the Marathi phrases the specification gives (§46: "कमी वाहतूक खर्च", "अलीकडील माहिती", "स्थिर भाव") are used verbatim;
     - the glossary in role file 02 §10 is binding, so one English term has one translation per language;
     - the mandatory labels and the standard disclaimer (§32, §49) are translated in full, never shortened or softened; estimates stay estimates;
     - a genuinely ambiguous translation is flagged in the ambiguity register of role file 02 §10, not silently resolved;
     - terminology is not invented: where no established term exists, the plain descriptive phrase is used and the case is flagged.
  3. **Scope and completion.** The attribution string stays verbatim English (PDD-04), and reference names keep ADR-0015's rule. The remaining interface strings are produced under these rules during frontend implementation, and every farmer-facing string must exist in all three languages before the §48 language sign-off.
- **Rationale:** The owner assigned the work to Claude explicitly. The rules make the copy auditable without a separate reviewer.
- **Alternatives Considered:** RD-046 options A (team translation with a named native-speaker reviewer), B (professional translation) and C (English only until reviewed, which fails §48).
- **Consequences:** Role file 02 §10 records the owner, the glossary, the ambiguity register and each string's status. RD-046 is resolved.
- **Source / Evidence:** Spec F1.2, §27 (line 1321), §46 (line 1963), §48 (line 2106), §49; ADR-0015; role file 02 §10, §11.
- **Affected artifacts:** `docs/02_AgriNex_UIUX_Phase1_Specification.md` §10, §11; `docs/REQUIRES_DECISION.md` (RD-046); `.claude/skills/agrinex-design/SKILL.md`, `.claude/skills/agrinex-frontend/SKILL.md`.
- **Approval:** Human Project Owner, 2026-10-04, conversation ("TASK 9 FINAL — HUMAN DECISION LOCK + CONTRACT/UX CONSISTENCY + FINAL READINESS GATE": "Claude is responsible for the Marathi and Hindi UX copy work. … Record Claude as the owner responsible for this copy work.").

### ADR-0047 — Account email, login identifiers and Phase 1 password recovery (H5)

- **Decision ID:** ADR-0047
- **Date:** 2026-10-04
- **Status:** APPROVED
- **Title:** Account email, login identifiers and Phase 1 password recovery (H5)
- **Context:**
  - RD-047: contract 1.0.0 had no recovery operation, and Phase 1 has no SMS or OTP (§39; ADR-0016 item 3).
  - VERIFIED: the specification's `USERS` entity already has `phone_number UK` and `email UK` (§20, lines 939–946). ADR-0016 made the email required for ADMIN only, and already defines login as one `identifier`, "phone or email, normalized" (ADR-0016 item 2). `docs/database/DATA_DICTIONARY.md` already has `users.email` varchar(254), unique, lower case.
- **Decision:**
  1. **Registration** of a FARMER requires a mobile number (E.164, ADR-0016), an **email address** and a password (8–64 characters). The optional display name and default district stay as they are. No other field is added, removed or replaced.
  2. **Email.** Stored in lower case and unique across accounts. It is required for FARMER and ADMIN accounts (this amends ADR-0016 item 1, whose CHECK required a phone for FARMER and an email for ADMIN). The API never returns it.
  3. **Login.** One `identifier` and the account's single password. A FARMER may use the mobile number or the email address of the same account; an ADMIN uses the email address. There is one account, one password and one password system per person; an identifier never creates or selects a different account.
  4. **Password recovery is part of Phase 1**, through the account's **verified** email. No SMS, no OTP and no paid SMS provider (for example Twilio, 2Factor or Fast2SMS) is introduced for it.
  5. **Email verification** is part of Phase 1, because recovery uses only a verified email.
  6. The mechanism, the operations and the email delivery are decided in ADR-0048.
  7. Alert delivery is unchanged: in-app only (ADR-0019). This entry adds no alert email.

  This amends ADR-0016 items 1 and 3, and is departure D-15.
- **Rationale:** The owner decided it. Email is already part of the specification's account model, and recovery removes the dead end RD-047 described for the alert persona (§6).
- **Alternatives Considered:** RD-047 option A (no self-service recovery; ask the team to erase the account), option B (a known limitation with no copy), option C with OTP (excluded by §39 and by the owner).
- **Consequences:**
  - Contract: `registerFarmer` requires `email` and may answer 409 for it; `login`'s `identifier` is documented as either identifier of the same account; `AuthenticatedUser.emailVerified`; the operations of ADR-0048.
  - Database: `ck_users_role_identifier` requires an email for FARMER; the verification and recovery columns of ADR-0048.
  - UX: role file 02 S10, S12 and the new S14 to S16.
  - RD-047 is resolved. The specification is not edited (decision-log rule 5).
- **Source / Evidence:** Spec §6, §20 (lines 939–946), §25 (line 1266), §28 (line 1335), §39; ADR-0016, ADR-0019, ADR-0028; `contracts/openapi.yaml` (auth operations); `docs/database/DATA_DICTIONARY.md` (`users`).
- **Affected artifacts:** `contracts/openapi.yaml`; `frontend/__mocks__/api/` (auth mocks); `docs/api/`; `docs/database/` (`users`); `docs/architecture/SECURITY_ARCHITECTURE.md`, `FRONTEND_ARCHITECTURE.md`; `docs/02_AgriNex_UIUX_Phase1_Specification.md`; `docs/REQUIRES_DECISION.md` (RD-047).
- **Approval:** Human Project Owner, 2026-10-04, conversation ("TASK 9 FINAL — HUMAN DECISION LOCK + CONTRACT/UX CONSISTENCY + FINAL READINESS GATE": "Phase 1 DOES include password recovery. … During registration, an email address must be included. … The user may log in using either: email address OR phone number together with the same account password. … recovery is performed through the user's verified email, NO paid SMS OTP provider is required").

### ADR-0048 — Email verification, password-reset mechanism and email delivery

- **Decision ID:** ADR-0048
- **Date:** 2026-10-04
- **Status:** DECIDED
- **Title:** Email verification, password-reset mechanism and email delivery
- **Context:**
  - ADR-0047 requires email verification and recovery through the verified email, and leaves the mechanism to the delegation recorded in ADR-0043 item 4(b).
  - Existing architecture: BCrypt cost 12; refresh tokens as 256-bit opaque values stored as SHA-256 hashes, rotated, revocable by family (ADR-0016); per-IP and per-identifier rate limits with an in-process fallback (ADR-0017, ADR-0018); non-revealing auth semantics (ADR-0042, SR-15); no personal data in logs (ADR-0042); Spring Framework's `RestClient` in the baseline (ADR-0011).
  - VERIFIED (OWASP Forgot Password Cheat Sheet, fetched 2026-10-04): "Return a consistent message for both existent and non-existent accounts"; "Ensure that responses return in a consistent amount of time"; tokens "Generated using a cryptographically secure random number generator", "Long enough to protect against brute-force attacks", "Stored in a secure manner", "Invalidated after they have been used"; "Don't rely on the Host header while creating the reset URLs"; "the reset password page adds the Referrer Policy tag with the no-referrer value"; "Don't automatically log the user in"; invalidate existing sessions; "Send the user an email informing them that their password has been reset".
  - VERIFIED (Resend API reference via Context7, 2026-10-04): `POST https://api.resend.com/emails` with `Authorization: Bearer <key>` and a JSON body (`from`, `to`, `subject`, `html`); an optional `Idempotency-Key` header (1–256 characters, kept 24 hours); sending to arbitrary recipients needs a verified domain (DKIM and SPF records), and the `resend.dev` test domain delivers only to the account's own address.
  - The project documents did not mention Resend before this entry. The owner's H5 instruction names it: "The delivery mechanism may use the existing Resend email infrastructure already used by the project." This entry records it as the project's email provider.
- **Decision:**
  1. **Mechanism: emailed single-use links**, for both email verification and password reset. No numeric code, no OTP, no SMS.
     - Token: 256 random bits from a cryptographically secure generator, encoded base64url without padding (43 characters).
     - Storage: only the lowercase hex SHA-256 hash, on `users`, with an expiry time. One active token per purpose and account; issuing a new one replaces the previous one; using it clears it.
     - Lifetime: verification links 24 hours, reset links 30 minutes.
  2. **Links** are built on the configured public origin, never on the request's `Host` header. The token travels in the URL fragment (`/verify-email#token=…`, `/reset-password#token=…`), so it never reaches a server log or a `Referer` header. The frontend removes it from the address bar at once and sends it in a request body; the reset page sends `Referrer-Policy: no-referrer`.
  3. **Operations** (contract 1.0.0, revised before its first merge):
     - `requestPasswordReset` (anonymous): `identifier` = mobile number or email. Always 202 with no body. A link is sent only when the identifier names an active account with a verified email, and only to that email.
     - `confirmPasswordReset` (anonymous): `token` + `newPassword`. 204: in one transaction the password hash is replaced (ADR-0016 rules and BCrypt cost 12) and the token spent; every refresh-token family of the account is revoked; the response clears the refresh cookie; a "password changed" notice is sent; the user is not signed in. Otherwise 422 `PASSWORD_RESET_TOKEN_INVALID`.
     - `confirmEmailVerification` (anonymous, so the link works on any device): 204 sets `email_verified_at`. Otherwise 422 `EMAIL_VERIFICATION_TOKEN_INVALID`.
     - `resendEmailVerification` (Bearer, FARMER or ADMIN): 202; sends a new link while the email is unverified.
     - `AuthenticatedUser.emailVerified` tells the client whether the email is verified.
  4. **Verification.** A verification link is sent after registration. A provisioned ADMIN's email (ADR-0028 bootstrap) is recorded as verified at creation, because the operator supplies it.
  5. **Enumeration resistance.** The reset request answers the same 202 for every well-formed identifier, and its email is sent after the response, off the request path, so timing reveals nothing. Its per-identifier limit counts unknown identifiers like known ones. The phone number and the email of one account are separate identifiers for every limit, so no response links them. Registration's 409 for a registered email is the same class of residual risk as SR-01 (ADR-0042), bounded by the register rate limit; it is recorded for the owner's awareness (security review AR-02).
  6. **Rate limits** (extends ADR-0017 item 6; values are deployment configuration): 10 requests per minute per client IP for each of the four operations; 3 password-reset requests per identifier per hour; 3 verification resends per account per hour. They fall back to the in-process limiter when Redis is down (ADR-0018).
  7. **One password system.** Reset applies the account's own password rule (FARMER 8–64, ADMIN 15–64 characters, at most 72 UTF-8 bytes; ADR-0016). ADMIN accounts recover the same way as FARMER accounts.
  8. **Email delivery.** The auth module sends the three emails (verification, reset, password changed) through Resend's HTTPS API with Spring Framework's `RestClient`: no SDK and no additional library. The API key `RESEND_API_KEY` lives only in the backend environment. The sender address is on a verified sending domain of the project domain (RD-037). Emails are sent after the transaction commits, with a bounded timeout and an idempotency key per message. A failure is logged as an ERROR event with the correlation id, without personal data; there is no queue and no retry loop, and the user can request a new link. Local development and tests use a non-delivering email adapter.
  9. **Verification gate** (spec §39 "no unverified external API dependency", §49 "VERIFIED (verified by API test)"): before the auth module relies on Resend, a live send must succeed from the deployment configuration. Until then the feature is PLANNED, not VERIFIED.
  10. **Logging.** Email addresses, tokens, links and email contents are never logged.
  11. **Data.** Five nullable columns on `users`: `email_verified_at`, `email_verification_token_hash`, `email_verification_expires_at`, `password_reset_token_hash`, `password_reset_expires_at`, with unique constraints on the two hashes and CHECKs (hash and expiry together; verification tokens only while unverified; reset tokens only for a verified email). No new table. Expired values are cleared by the auth module's daily purge; erasure deletes them with the account (ADR-0029, ADR-0037).
- **Rationale:**
  - Follows every OWASP recommendation above, and mirrors the existing refresh-token design (opaque 256-bit values, hashes only), so no new pattern is introduced.
  - A link is easier for the farmer persona than typing a code, and a 256-bit token cannot be brute-forced, unlike a 6-digit code.
  - `RestClient` and a Bearer key need no new dependency; the owner named Resend.
- **Alternatives Considered:**
  - A 6–8 digit code by email: needs extra brute-force controls and is harder to type on a small phone.
  - Passwordless "magic link" login: changes the authentication model; H5 requires the same password.
  - Security questions: discouraged by OWASP.
  - SMS OTP: excluded by H5 and §39.
  - SMTP through a mail library: a new dependency and SMTP credentials.
  - A separate token table: unnecessary for one active token per purpose.
- **Consequences:**
  - Contract: four operations, two error codes, `emailVerified`, rate-limit descriptions (`CONTRACT_VALIDATION.md` §2.1).
  - Database: the five columns, two unique constraints and three CHECKs (`DATA_DICTIONARY.md`, `INDEXES_AND_CONSTRAINTS.md`, `ERD.md`).
  - Architecture: Resend as an external system (`SYSTEM_CONTEXT.md`, `CONTAINER_ARCHITECTURE.md`), the auth module's email adapter, F-18, the email-provider failure row, the deployment prerequisites, `RESEND_API_KEY`, the tests.
  - Security-sensitive: the implementation needs explicit human review before merge (CLAUDE.md rule 16). Review record: `docs/security/TASK_9_FINAL_ACCOUNT_RECOVERY_SECURITY_REVIEW.md`.
  - Human actions before production use: a Resend account and API key, a verified sending domain (depends on RD-037), and the live send of item 9.
- **Source / Evidence:** OWASP Cheat Sheet Series, "Forgot Password Cheat Sheet" (fetched 2026-10-04); Resend API reference (`/websites/resend`, Context7, 2026-10-04): sending email, idempotency keys, domains, the `resend.dev` restriction; ADR-0011, ADR-0016, ADR-0017, ADR-0018, ADR-0019, ADR-0020, ADR-0028, ADR-0029, ADR-0037, ADR-0042; spec §28, §39, §49.
- **Affected artifacts:** `contracts/openapi.yaml`; `contracts/error-response.schema.json`; `frontend/__mocks__/api/`; `docs/api/API_INDEX.md`, `API_CONVENTIONS.md`, `FRONTEND_CONSUMPTION.md`, `CONTRACT_VALIDATION.md`; `docs/database/DATA_DICTIONARY.md`, `INDEXES_AND_CONSTRAINTS.md`, `ERD.md`, `DATABASE_ARCHITECTURE.md`; `docs/architecture/SECURITY_ARCHITECTURE.md`, `SYSTEM_CONTEXT.md`, `CONTAINER_ARCHITECTURE.md`, `COMPONENT_ARCHITECTURE.md`, `DATA_FLOW.md`, `DEPLOYMENT_ARCHITECTURE.md`, `RESILIENCE_ARCHITECTURE.md`, `TESTING_ARCHITECTURE.md`, `FRONTEND_ARCHITECTURE.md`, `CURRENT_TECHNOLOGY_BASELINE.md`; `docs/security/TASK_9_FINAL_ACCOUNT_RECOVERY_SECURITY_REVIEW.md`; `docs/02_AgriNex_UIUX_Phase1_Specification.md`.
- **Approval:** Decided under the delegation recorded in ADR-0043 item 4(b) (Human Project Owner, 2026-10-04).

### ADR-0049 — Frontend tooling chosen at scaffolding (Task 11)

- **Decision ID:** ADR-0049
- **Date:** 2026-10-06
- **Status:** DECIDED (under ADR-0050)
- **Title:** Frontend tooling chosen at scaffolding (Task 11)
- **Context:**
  - ADR-0034 defers the frontend i18n library, the frontend unit-test and end-to-end tools, the contract-validation library and the ESLint configuration to the scaffolding of the frontend, "each in its own entry that justifies the dependency under rule 15".
  - The frontend was scaffolded in Task 11 on the approved baseline (ADR-0002): Next.js 16.3.8, React 19.3.0, TypeScript 6.0.3, Tailwind CSS 4.3.3, Recharts 3.10.1, MSW 2.15.0, Node.js 24.
  - `docs/api/CONTRACT_VALIDATION.md` §3 (ADR-0022 item 1) requires frontend mock tests with a library that supports OpenAPI 3.1 and JSON Schema 2020-12, including `unevaluatedProperties`, `if`/`then` and relative external `$ref`s between the contract files.
  - The tooling below is already installed and used by Task 11, as ADR-0034 directs ("chosen when their area is scaffolded"). It stays PROPOSED until a human approves it. Every item except the i18n approach is a development dependency and can be replaced without changing product code.
- **Decision (proposed):**
  1. **i18n: no library.** Typed in-house catalogues in `frontend/public/locales/{en,mr,hi}.json` (the location spec §43 names), keys typed from `en.json`, `{name}` placeholders, `Intl.NumberFormat` and `Intl.DateTimeFormat` for numbers and dates (role file 02 §11, PDD-06 to PDD-08). The chosen language is kept in a first-party cookie, `agrinex_locale`, read by the root layout on the server; only the active catalogue is sent to the browser. No locale in the URL.
  2. **Unit and component tests:** Vitest 5.0.3 with jsdom 30.1.2, @testing-library/react 16.3.3 and @testing-library/dom 10.4.2 (development only).
  3. **Contract validation of the mocks:** Ajv 8.20.0 (its 2020-12 build), ajv-formats 3.0.1 and yaml 2.9.1 (development only), with a custom keyword for `x-agrinex-max-decimal-places`. Test: `frontend/lib/api/contract.test.ts`, with negative controls that must fail.
  4. **ESLint:** ESLint 9.39.5 with eslint-config-next 16.3.8 (core-web-vitals and TypeScript sets, flat config, as generated by create-next-app 16.3.8). Generated files are ignored: the MSW worker and the OpenAPI types.
  5. **OpenAPI types:** openapi-typescript 7.13.0, run on demand through `npx` (`npm run generate:api-types`) and not installed, because its TypeScript ^5 peer range conflicts with TypeScript 6. Its output, `frontend/lib/api/schema.d.ts`, is committed and regenerated whenever the contract changes.
  6. **react-is 19.3.0** as a runtime dependency: a peer dependency of Recharts 3.10.1 (`^16.8.0 || … || ^19.0.0`), pinned to the React version.
  7. **End-to-end tool** (added in Task 12, 2026-10-15): @playwright/test 1.64.1 (development only), run with `npm run e2e` against the real local stack, using the installed Google Chrome (`channel: "chrome"`) so no browser is downloaded. Journeys in `frontend/e2e/`; configuration `frontend/playwright.config.ts`. Rule 15: the browser journeys of the §10 FRONTEND checklist need a real browser driven against the running frontend and backend; jsdom cannot show layout, real navigation or the dev-server API forwarding. Playwright is the tool the Task 12 browser checks already used (through the Playwright MCP), supports Chrome, TypeScript and the Node.js 24 runtime of the baseline, and needs no further package.
- **Rationale:**
  - i18n: three fixed languages, no locale routing, and plural forms limited to "one/other" pairs that separate keys express. A library would add a dependency without a need the in-house catalogues leave unmet. Typed keys turn a missing key into a type error, and a test keeps the three catalogues identical in keys and placeholders.
  - Vitest runs TypeScript and ESM natively under the Next.js toolchain without a Babel setup; Testing Library tests components the way a user meets them.
  - Ajv is the most widely used JSON Schema validator for JavaScript and implements 2020-12 including `unevaluatedProperties`; registering each contract file under its own `$id` resolves the relative `$ref`s.
  - eslint-config-next is the Next.js default and includes the React Hooks rules, which found 13 real defects during Task 11.
- **Alternatives Considered:**
  - next-intl or i18next: capable, but a runtime dependency for a need the catalogues meet.
  - Jest: needs a transform setup for ESM and TypeScript.
  - Writing a JSON Schema validator for the tests: more code to verify than a mature library.
  - Installing openapi-typescript: blocked by its peer range.
- **Consequences:**
  - `CURRENT_TECHNOLOGY_BASELINE.md` §2 rows for these items can point here once this entry is approved.
  - `npm audit` (2026-10-06) reports no vulnerability in production dependencies and five high-severity findings in development dependencies, all through eslint-config-next (`braces` via `micromatch` and `fast-glob`); they do not ship to the browser.
  - If this entry is rejected, the affected tests and configuration are replaced. The i18n approach would need rework of `frontend/lib/i18n/`.
- **Source / Evidence:** ADR-0002, ADR-0022 item 1, ADR-0034; `docs/api/CONTRACT_VALIDATION.md` §3; `docs/architecture/FRONTEND_ARCHITECTURE.md` (tooling chosen at scaffolding); role file 02 §11; spec §43; the packages' npm registry entries at the pinned versions; Next.js 16.3.8 bundled documentation (`frontend/node_modules/next/dist/docs`).
- **Affected artifacts:** `frontend/package.json`, `frontend/package-lock.json`, `frontend/vitest.config.mts`, `frontend/eslint.config.mjs`, `frontend/lib/i18n/`, `frontend/public/locales/`, `frontend/lib/api/schema.d.ts`, `frontend/**/*.test.ts(x)`; for item 7, `frontend/playwright.config.ts` and `frontend/e2e/`.
- **Approval:** Drafted by Claude in Task 11. Decided under ADR-0050 on 2026-10-15 (ADR-0052 item 4), after a check that every version named here matches `frontend/package.json` (17 of 17) and that each tool is in use.

### ADR-0050 — Delegated decision authority for the Phase 1 pre-deployment closure (Task 12)

- **Decision ID:** ADR-0050
- **Date:** 2026-10-15
- **Status:** APPROVED
- **Title:** Delegated decision authority for the Phase 1 pre-deployment closure (Task 12)
- **Context:**
  - VERIFIED: in the instruction "AgriNex — Final Phase 1 Closure and Owner Decision Resolution" (conversation, 2026-10-15) the Human Project Owner wrote: "I authorize you to make these decisions on my behalf within the existing Phase 1 scope. Do not keep asking me to approve each individual decision." The decisions are D1–D12 of `docs/decision-review/TASK_12_OWNER_DECISION_PACKET.md`, including RD-048, RD-049, RD-050 and ADR-0049.
  - The same instruction keeps the approved architecture and requirements, forbids fabricating government data, units, coordinates or market identities, and excludes deployment (Task 13), cloud hosting and Phase 2.
- **Decision:**
  1. The delegation is recorded here as a human approval act, like ADR-0001. Decisions taken under it carry status DECIDED and have the force of APPROVED entries.
  2. **Scope:** D1–D12 of the Task 12 owner decision packet, the related items RD-048, RD-049, RD-050 and ADR-0049, and the rule 16 sign-off of the Task 12 security review (`docs/security/TASK_12_SECURITY_REVIEW.md`).
  3. **Not covered:**
     - changes to product requirements, scope, phases or acceptance criteria, and to the master specification;
     - actions that need the owner's external accounts, such as rotating the data.gov.in key;
     - deployment, hosting and Phase 2.
  4. The delegation is not standing authority. Later work drafts PROPOSED entries again unless a human records a new delegation.
- **Rationale:** The instruction is explicit and written. Recording it makes each Task 12 closure decision traceable to a human act, and the DECIDED status shows which decisions were taken under delegation, so a reviewer can audit or supersede them.
- **Alternatives Considered:** Keeping D1–D12 open until the owner approves each one, which the instruction rules out.
- **Consequences:** ADR-0051 and ADR-0052 record the decisions. RD-048, RD-049 and RD-050 move to Resolved. Security sign-offs made under this delegation are AI reviews, and are recorded as such.
- **Source / Evidence:** Conversation, 2026-10-15; CLAUDE.md §B, §D, §G, rule 16; ADR-0001 (precedent).
- **Approval:** Human Project Owner, 2026-10-15, conversation ("AgriNex — Final Phase 1 Closure and Owner Decision Resolution").

### ADR-0051 — Commodity source aliases for legacy DMI spellings

- **Decision ID:** ADR-0051
- **Date:** 2026-10-15
- **Status:** DECIDED (under ADR-0050)
- **Title:** Commodity source aliases for legacy DMI spellings
- **Context:**
  - RD-050. ADR-0006 item 2 resolves a record's commodity only by exact match with `commodities.canonical_name`.
  - VERIFIED (Task 12 harvest; `docs/decision-review/TASK_12_OWNER_DECISION_PACKET.md` D1):
    - until 2026-05-06 the data.gov.in historical resource used 20 commodity spellings that are not Agmarknet 2.0 master names;
    - the 2025-10-15 to 2026-10-15 backfill rejected 11,337 records for them as `UNKNOWN_COMMODITY` (2.4 %);
    - in that resource each spelling carries the same DMI `Commodity_Code` as exactly one master spelling (for example code 49 for `Arhar(Tur/Red Gram)(Whole)`, `Arhar (Tur/Red Gram)(Whole)` and `Red gram/Arhar/Tur(whole)`);
    - 12 spellings differ only in spacing, 1 in spacing and a suffix (`Elephant Yam (Suran)` → `Elephant Yam(Suran)/Amorphophallus`), and 7 are renames; every target has the verified unit `Rs./Quintal`. (The header comment of the applied migration V12 says "13 … only in spacing"; it counts the suffix case as spacing and is not edited, CLAUDE.md §I.)
  - The owner's conditions (2026-10-15): an alias mechanism only if demonstrably safe; DMI codes and one-to-one mappings; original values and history preserved; distinct commodities never merged; no invented alias.
- **Decision:**
  1. A new table `commodity_source_aliases`, owned by the commodity module, maps a source spelling to one master commodity, with a mandatory evidence note. Migration V12 seeds the 20 evidenced spellings. A spelling maps to exactly one commodity (unique key), and no alias equals a master name (the migration's guard and a test).
  2. Ingestion resolves a whitespace-normalized source name by exact match with the master name first, then by exact match in `commodity_source_aliases`. There is no case folding, no fuzzy matching and no runtime matching on `Commodity_Code`, which the current resource does not carry. Anything else stays `UNKNOWN_COMMODITY`.
  3. `price_observations.source_commodity_name` keeps the source spelling of a row resolved through an alias; it is NULL when the source used the master name. It is not part of the identity or the business key, so a record the source reported under both spellings becomes one observation under ADR-0009: an identical copy is a duplicate, a differing one is quarantined as a conflict.
  4. New aliases are added only by a migration with evidence. There is no admin endpoint, and the contract is unchanged.
  5. After V12 the historical backfill is re-run for 2025-10-15 to 2026-05-06.
- **Rationale:** One commodity keeps one series. The mapping rests on the government's own code, is exact and auditable, and keeps the original spelling on every affected row. It changes neither the contract nor any other module.
- **Alternatives Considered:**
  - Registering the legacy spellings as separate commodities: splits one commodity's history into two series.
  - Matching on `Commodity_Code` at ingestion: only the historical resource carries it, and Agmarknet codes differ between portal generations (DATABASE_INDEX.md evidence table).
  - An admin alias endpoint: a contract change, not needed for 20 fixed rows.
  - Status quo: 2.4 % of a year of historical records stays rejected.
- **Consequences:**
  - Schema: one table and one nullable column (V12; DATA_DICTIONARY, INDEXES_AND_CONSTRAINTS, TABLE_OWNERSHIP, ERD, DATABASE_INDEX, DATABASE_ARCHITECTURE updated). `CommodityApi.resolveSourceName`; `ReferenceLookups` uses it.
  - Tests: `ReferenceDataSeedIntegrationTest` (`legacyDmiSpellingsResolveOneToOneThroughEvidencedAliases`, and the ingestion case now stores `Arhar(Tur/Red Gram)(Whole)` with its source spelling); `DatabaseSchemaIntegrationTest` expects V1–V12.
  - Local database, 2026-10-15: run `d417d32f` (HISTORICAL_PRICE, 204 report dates) ended SUCCESS: 536 pages, 0 failed, 222,072 records fetched, 8,978 inserted (every one alias-resolved), 0 `UNKNOWN_COMMODITY`. Of the 11,337 records rejected before: 8,978 are stored; 276 are quarantined conflicts; 2,083 were identical to an observation of the same identity, because the source reported both spellings in parallel, mostly in April and May 2026.
  - Residual: a new legacy spelling found later is rejected until a migration adds it with evidence.
- **Source / Evidence:** RD-050; packet D1 and its mapping table; ADR-0006, ADR-0009; `docs/data/AGRINEX_VERIFIED_GOVERNMENT_API_DATA_REFERENCE.md` §23.
- **Approval:** Decided under ADR-0050 (Human Project Owner's delegation, 2026-10-15).

### ADR-0052 — Task 12 closure decisions D2 to D12

- **Decision ID:** ADR-0052
- **Date:** 2026-10-15
- **Status:** DECIDED (under ADR-0050)
- **Title:** Task 12 closure decisions D2 to D12
- **Context:** The remaining decisions of `docs/decision-review/TASK_12_OWNER_DECISION_PACKET.md`, with the owner's conditions for each (conversation, 2026-10-15). D1 is ADR-0051. Item numbers below match the packet's D numbers.
- **Decision:**
  2. **D2, units of 10 crops.** No official unit evidence was accessible: the report export needs a captcha, and the feed's prices are not evidence of a unit (ADR-0008). The ten crops stay unverified, and their records stay rejected as `UNIT_UNVERIFIED` with the raw payload visible to admins. To add one: export the official Agmarknet 2.0 report for a date with a row for the crop, store the file in `docs/data/raw/`, record the unit through *Admin → Commodities → Edit → Save verified unit* (audited) or a migration, then backfill the affected dates.
  3. **D3, security sign-off.** CORS rejection, optional-access session handling, and the database fail-fast timeout were re-examined, and their regressions and live probes re-run (`docs/security/TASK_12_SECURITY_REVIEW.md` §7). No new defect was found. The four items of §6 are signed off under ADR-0050. This is an AI review recorded under the owner's delegation, not a human review. S12-1 (never run the `dev` profile on a deployment) remains a condition of Task 13.
  4. **D4, ADR-0049.** DECIDED under ADR-0050; its status line is updated.
  5. **D5, RD-048.** Closed: ADR-0007 item 2 (`dd-MM-yyyy`) is confirmed. Evidence: the Task 12 live comparison; 156 report dates each returning their reported total; the 366-date backfill; and run `d417d32f` (204 dates, 536 pages, 0 failed).
  6. **D6, Marathi and Hindi copy.** Claude re-reviewed all 378 keys and ran automated checks. Four corrections were made (role file 02 §10.3 A-09 to A-12) and two decisions recorded (A-13, A-14). No native speaker has reviewed the copy. ADR-0046 does not require one; it stays a recommended future enhancement.
  7. **D7, account email language (RD-049).** Each of the three emails is one plain-text message in English, then Marathi, then Hindi. The subject follows the language chooser's "EN · MR · HI" pattern, and the link appears once. There is no per-user language, because the contract carries no language preference and adding one is a contract change outside this closure. The wording is in `AuthEmails.java` and role file 02 §10.3 A-14. The ADR-0048 mechanism is unchanged.
  8. **D8, stale note.** The note "These prices are old…" (role file 02 E03) appears only when every displayed option is STALE. In mixed results each stale option keeps its Stale badge and its `STALE_DATA` reason, and the answer strip shows the recommended price's own freshness badge.
  9. **D9, load-test records.** The 10,316 comparisons the Task 12 load tests recorded were deleted, with their 10,316 runs and 328,799 options. They were identified by all of: anonymous; one of the five fixed load-test bodies; no variety or grade; created inside the two load-test windows. They were first exported (`local-reference-data/backups/d9_load_test_rows_2026-10-15/`, CSV with SHA-256 sums) and are also in the full dump taken before the closure. The remaining 50 recorded comparisons are kept, because their origin (tests or manual use) cannot be confirmed row by row. Experiment 1 figures must come from real use after deployment, or exclude local test windows. Later load tests against a database used for Experiment 1 are removed the same way after measuring. ADR-0029 retention is unchanged for genuine comparisons.
  10. **D10, the "3G" profile of the LCP target (spec §27).** The target is judged with the Chrome DevTools "Fast 3G" preset (Puppeteer `PredefinedNetworkConditions['Fast 3G']`, which Chrome also lists as "Slow 4G"), 4× CPU slowdown, a 375 × 812 mobile viewport at DPR 3, a cold cache and the production build. Slow 3G is reported alongside it and is not met; see Consequences.
  11. **D11, legacy market spellings.** A PROVISIONAL market was merged into a VERIFIED one only when all of the following held (ADR-0014 item 4):
      - the same district;
      - names that differ only in form: case, punctuation, the word "APMC" and word order;
      - at least 3 observations with identical min, modal and max prices for the same commodity, variety, grade and report date in the same delivery (same dataset and ingestion run);
      - no differing overlapping observation.

      242 markets were merged through the admin API, each with its evidence note. The other 477 stay PROVISIONAL:
      - 6 pairs had a differing observation;
      - 10 had fewer than 3 shared observations;
      - 302 had no shared delivery;
      - 16 matched more than one VERIFIED market;
      - 143 matched none.
  12. **D12, data.gov.in key.** The key is treated as compromised. Rotating it needs the owner's data.gov.in account, so Claude did not rotate it. The exact steps are in `docs/security/TASK_12_SECURITY_REVIEW.md` §7. The key has never been printed, logged or committed.
- **Rationale:** Each item follows the owner's conditions: evidence before change, no fabricated data, residual limitations stated, and no claim of human or external action that did not happen.
- **Alternatives Considered:**
  - D7: per-user language (a contract change), or English only (fewer recipients can read it).
  - D8: the note whenever any option is stale (contradicts fresh prices beside it).
  - D9: keep the rows (they distort Experiment 1).
  - D10: Slow 3G as the acceptance profile (not met), or inlining the CSS (blocked by the CSP; see Consequences).
  - D11: merging by name alone (forbidden by ADR-0014).
- **Consequences:**
  - D7: live through Resend to a disposable inbox, 23 of 23 checks passed, including the trilingual rendering, the link once, resend replacing the token, single use, expiry, the end of every session after a reset, and no email for an unknown address. Tests: `AuthEmailsTest` (4).
  - D8: `stale-note.test.tsx` (4 cases).
  - D9: 50 searches, 50 runs and 1,711 options remain.
  - D10, measured 2026-10-15:
    - Fast 3G: home 1,567 ms and compare 1,624 ms (earlier runs 1,695 ms and 1,474 ms).
    - Slow 3G: home 5,045 to 6,656 ms and compare 5,230 ms. One render-blocking stylesheet costs about 2 s.
    - `experimental.inlineCss` was tried. Next.js 16.3.8 puts no CSP nonce on the inlined `<style>`, so the production CSP blocked it and the page rendered unstyled; it was reverted. Fixing Slow 3G needs a deployment-time measure (Task 13), such as compressed HTTP/2 delivery with preload from Nginx, or a security-reviewed hash-based `style-src`.
  - D11:
    - 34,531 identical copies were dropped, and no observation remains on a merged market;
    - 242 `MARKET_MERGED` audit events were written;
    - 286 of the 354 VERIFIED markets now have history back to 2025-10-15;
    - full dump taken before the merges: `local-reference-data/backups/agrinex_pre_d11_merges_2026-10-15.dump`.
  - D12: an external action by the owner remains.
- **Source / Evidence:**
  - the packet;
  - `docs/security/TASK_12_SECURITY_REVIEW.md`;
  - role file 02 §9 E03 and §10.3;
  - ADR-0007, ADR-0008, ADR-0014, ADR-0029, ADR-0046, ADR-0048, ADR-0049;
  - the Next.js 16.3.8 bundled documentation (`inlineCss.md`);
  - the Puppeteer documentation (`PredefinedNetworkConditions`, via Context7).
- **Approval:** Decided under ADR-0050 (Human Project Owner's delegation, 2026-10-15).

### ADR-0053 — Offline evaluation of the forecasting baselines before deployment

- **Decision ID:** ADR-0053
- **Date:** 2026-10-18
- **Status:** PROPOSED
- **Title:** Offline evaluation of the forecasting baselines before deployment
- **Context:**
  - RD-051. Spec §35 lets ML be built only when (1) the historical series has at least 90 observations per market–commodity pair, (2) the deterministic engine is complete **and deployed**, and (3) a team member has time after the Phase 1 freeze.
  - `docs/api/ML_CONTRACT_BOUNDARY.md` §8 and `ml/forecasting/README.md` say the job must not run against real data before those criteria are met, and that starting the track is a human decision. ADR-0030 governs the database boundary.
  - VERIFIED, local database, 2026-10-18 (`docs/data/PHASE1_DATA_AND_ML_READINESS_REPORT.md`):
    - criterion 1 holds for 1,301 of the 3,571 VERIFIED market–commodity pairs (103 commodities, 184 markets);
    - 1,179 of them also have data in each period of §35's chronological split (January–July 2026 training, August validation, September test).
  - HARD RULE 2 (deterministic engine end to end) is met locally. Criteria 2 and 3 are not: AgriNex is not deployed and Phase 1 is not frozen.
  - The forecasting code exists (Task 10): the two §35 baselines and a chronological, expanding-window evaluation that gives the model only the history up to each origin date (`evaluate`), tested on synthetic data with a leakage test.
- **Decision (proposed):**
  1. Permit **one offline evaluation, before deployment and the freeze**, of the two §35 baselines (naive last value, 7-day rolling mean) on the local database's daily-series view.
  2. **Scope:**
     - VERIFIED markets only, and pairs with at least 90 observations;
     - horizons 3 and 7 days;
     - §35's split: origins in August 2026 for validation and in September 2026 for test, each with all earlier history (expanding window);
     - metrics MAE, RMSE and directional accuracy.
  3. **Read only:**
     - the job runs without `--write`, so no `forecasts` row is written and `GET /markets/{id}/forecast` keeps answering `available: false`;
     - the connection only needs SELECT on `daily_price_series`.
  4. **Reporting:** the results are reported as "Experiment 3 (preliminary, offline, baselines only)", including poor results (§36).
  5. **Not covered:**
     - ARIMA, Prophet and LightGBM;
     - writing or serving forecasts;
     - any UI change;
     - starting the ML track.

     §35 criteria 2 and 3 still govern all of these.
- **Rationale:**
  - The data already meets criterion 1 for many pairs.
  - A read-only baseline evaluation measures whether forecasting is worth building at all, before the team spends post-freeze time on it.
  - It cannot affect any Phase 1 flow, because nothing is written or exposed.
- **Alternatives Considered:**
  - Wait for deployment and the freeze (status quo): no evidence until then.
  - Synthetic data only (already done): proves the code, not the usefulness of forecasting on mandi data.
  - Starting the full ML track: contradicts §35.
- **Consequences:**
  - A departure from spec §35 criteria 2–3 and from `ML_CONTRACT_BOUNDARY.md` §8, for evaluation only. It therefore needs the Human Project Owner's approval; Claude may not decide it.
  - If approved: run the existing CLI without `--write`, and add the results to the readiness report and to the test architecture's Experiment 3 row.
- **Source / Evidence:**
  - spec §10 HARD RULE 2, §35, §36 Experiment 3, §42 item 35;
  - ADR-0030;
  - `docs/api/ML_CONTRACT_BOUNDARY.md` §8;
  - `ml/forecasting/README.md`;
  - the readiness report named above.
- **Approval:** PENDING. Drafted by Claude on 2026-10-18 for the Human Project Owner.

### ADR-0054 — Historical maintenance: report-date ledger, gap backfill and reconciliation

- **Decision ID:** ADR-0054
- **Date:** 2026-10-18
- **Status:** APPROVED
- **Title:** Historical maintenance: report-date ledger, gap backfill and reconciliation
- **Context:**
  - ADR-0011 item 1 fetches the trailing window of report dates today − 3 to today every 6 hours, and item 7 keeps no persisted per-date state. A date older than the window is fetched again only by a manual trigger.
  - VERIFIED, local database, 2026-10-18 (read-only queries):
    - the historical resource publishes a report date's records over 1 to 3 days: 2026-10-13 received 1,278 records at age 2 and 358 more at age 3; 2026-10-14 received 1,086 and then 382 records at age 2; a re-fetch of 2026-10-14 at age 4 found nothing new;
    - no stored price has ever been revised (0 rows in `price_observation_revisions`), including a re-fetch of seven months of history about 20 hours after the first fetch;
    - after the backend stopped on 2026-10-16, the report dates 2026-10-16 and 2026-10-17 were not fetched after publication. A stop longer than the window leaves such dates missing, and nothing reports them.
  - The owner asked for automatic gap detection and backfill, including dates older than the window, a reprocessing window for late or corrected records, per-date progress, and recovery after interruptions. The owner approved this design on 2026-10-18.
- **Decision:**
  1. **Ledger.** New table `ingestion_report_dates`, owned by the ingestion module, with one row per source dataset and report date: the last attempt (run, time, outcome `FETCHED` or `FAILED`), the last successful fetch (run, time, records fetched) and the count of consecutive failed attempts. Only HISTORICAL_PRICE delivery units write it, after each unit. The migration seeds it from the existing runs and their page errors.
  2. **Date status**, computed from the ledger at the reference time zone (ADR-0007). The settle age is `historical-window-days-back` (3 days), the publication lag measured above:

     | Status | Meaning |
     |---|---|
     | `SETTLED` | A successful fetch happened when the date was at least the settle age old |
     | `PENDING` | Younger than the settle age; the trailing window still fetches it |
     | `UNSETTLED` | At least the settle age old, but every successful fetch happened earlier |
     | `FAILED` | The last attempt failed |
     | `MISSING` | Never attempted |

     Coverage spans the last 366 report dates up to today (the ADR-0011 maximum).
  3. **Daily gap check** at 03:00 Asia/Kolkata. Unless the HISTORICAL_PRICE dataset is paused or one of its runs is RUNNING, it starts one run over the newest incomplete dates (`MISSING`, `FAILED` or `UNSETTLED`): from the oldest incomplete date within the 31 report dates that end at the newest incomplete date, to that newest date. Complete dates inside that range are fetched again, which is idempotent (ADR-0009). The check repeats daily until no incomplete date remains; a date that keeps failing stays visible with its count of consecutive failures.
  4. **Weekly reconciliation** on Sunday at 04:00 Asia/Kolkata: one run over the 14 report dates today − 14 to today − 1, to pick up late publications and corrections. The 14 days are a margin over the measured 3 days (ASSUMPTION, to be revisited with Experiment 2's evidence).
  5. **Same run path.** Both use the HISTORICAL_PRICE run of ADR-0011 (items 4 to 10): one RUNNING run per dataset, per-date delivery units, retries, idempotent loading, revisions (ADR-0009) and counters (ADR-0012). They are `SCHEDULER` runs, and `request_scope` records their `purpose`: `TRAILING_WINDOW`, `GAP_BACKFILL`, `RECONCILIATION` or `MANUAL`. A paused dataset skips both; manual triggers are unchanged.
  6. **Admin view.** A read-only operation, `GET /api/v1/admin/ingestion/historical-coverage`, returns the counts per status, the incomplete dates, the progress of a running HISTORICAL run (report dates attempted out of those in scope), and the times of the next gap check and reconciliation.
  7. **Configuration.** The gap-check and reconciliation schedules, the 366-date coverage, the 31-date limit and the 14-date window are deployment configuration (ADR-0020 item 3).
- **Rationale:**
  - A per-date record is the only way to tell a date fetched with no records from a date never fetched, and to show progress.
  - Deriving the state from runs and errors would lose failed dates when errors are purged after 180 days (ADR-0029).
  - Bounded runs keep the request rate and run time close to the scheduled runs, and reuse every tested ingestion rule.
- **Alternatives Considered:**
  - Widening the 6-hourly window to 14 days: about four times the source requests every day, with no evidence of benefit.
  - Persisted page offsets: rejected in ADR-0011 item 7; per-date units already make re-fetching safe.
  - Deriving coverage from `ingestion_runs` and `ingestion_errors`: loses evidence after the 180-day error purge.
- **Consequences:**
  - Migration V13; the contract gains one admin operation (minor version); the admin data-health screen shows coverage and progress.
  - Tests cover the ledger outcomes, the status rules, the gap selection, both schedules, a revised price and recovery after an interrupted run.
  - ADR-0011 item 1 and item 7 are extended, not replaced.
- **Source / Evidence:** Spec §10 (bootstrap, 6-hour cadence), §15, §29, §31 RULE 6, §36 Experiment 2, §37; ADR-0005 item 6, ADR-0007, ADR-0009, ADR-0011, ADR-0012, ADR-0020, ADR-0029; the measurements above.
- **Approval:** APPROVED by the Human Project Owner on 2026-10-18 ("Approve full design").

### ADR-0055 — Public deployment: Oracle Cloud Always Free VM, domain and production data

- **Decision ID:** ADR-0055
- **Date:** 2026-10-18
- **Status:** APPROVED
- **Title:** Public deployment: Oracle Cloud Always Free VM, domain and production data
- **Context:**
  - RD-037 left the provider, region, size and domain to the owner. ADR-0021 fixes the topology: one cloud VM running the root Compose stack, Nginx with certbot (spec §7, §45, F1.25).
  - The owner's target is zero ongoing cost.
  - VERIFIED from the providers' official documentation on 2026-10-18:
    - Oracle Cloud Always Free: Ampere A1 capacity "equivalent to 2 OCPUs and 12 GB of memory", 200 GB of block volume and 10 TB of monthly outbound data. Always Free instances must be in the home region. Idle instances "may be reclaimed" when, over 7 days, 95th-percentile CPU, network and (for A1) memory utilization are all below 20%. Sign-up uses a card for identity verification, which is not charged unless the account is upgraded;
    - Render free web services spin down after 15 minutes without traffic, cannot run cron jobs, and free Postgres databases expire after 30 days, without backups;
    - Vercel Hobby is for non-commercial personal use, and its cron jobs run at most once a day;
    - Neon Free gives 1 GB of storage per project.
  - VERIFIED, local database, 2026-10-18: 496 MB in total, about 340 MB without the test comparisons. The 354 VERIFIED markets and the 242 merges were curated through the admin API (audited), not by migrations.
- **Decision:**
  1. **Host.** One Oracle Cloud Always Free VM: shape VM.Standard.A1.Flex (Arm64) with 2 OCPU and 12 GB, a 100 GB boot volume, Canonical Ubuntu 24.04, in the home region India West (Mumbai), or India South (Hyderabad) when Mumbai has no capacity. The account uses NitinR0306@gmail.com. The ADR-0021 topology is unchanged; the custom images are built on the VM.
  2. **Domain.** `agrinex.managio.me`, an A record set by the owner. The public origin is `https://agrinex.managio.me`, with a certbot certificate as ADR-0021 item 4 describes. Email stays on the verified managio.me sending domain (ADR-0048).
  3. **Production data.** The production database starts as a curated copy of the local database. This departs from ADR-0021 item 5, where real price data comes from ingestion and the historical bootstrap:
     - **copied:** every table except those below, including all observations with their run provenance, the reference data, the market curation and its audit events, the configuration versions and the ADMIN account;
     - **not copied:** FARMER accounts and their profiles, alerts and alert events; refresh tokens; and every recorded comparison (`selling_searches`, `recommendation_runs`, `selling_options`), which are test data, so that Experiment 1 starts with real use.
  4. **Accepted limitations of the free tier:**
     - no availability guarantee;
     - the idle-reclamation policy;
     - Ampere capacity that may be unavailable at times;
     - backups are the daily `pg_dump` of ADR-0021 item 6 on the same VM. Copies off the VM are an owner action.
- **Rationale:** The free VM runs the approved topology unchanged, including the in-process 6-hourly scheduler, with room for the data and its growth (about 0.3 GB a year). The curated copy keeps the curation that comparisons depend on, without test or personal data.
- **Alternatives Considered:**
  - Vercel, Render and Neon: departs from spec §7 and ADR-0021; the scheduler would stop while the service sleeps, there is no free cron at the needed frequency, and the storage limit is close.
  - A paid VM: not zero cost.
  - A fresh database with a historical bootstrap: every market would start PROVISIONAL, so comparisons would be empty until the curation was repeated.
- **Consequences:**
  - RD-037 is resolved.
  - The deployment documentation records the host, the address and the operator procedures.
  - The owner performs the sign-up, the VM creation, the DNS record and the key rotation.
- **Source / Evidence:**
  - Spec §7, §10, §28, §36, §45; ADR-0017, ADR-0020, ADR-0021, ADR-0028, ADR-0029, ADR-0048.
  - Oracle "Always Free Resources" and Free Tier FAQ; Render "Deploy for Free"; Vercel "Hobby Plan" and "Usage & Pricing for Cron Jobs"; Neon "Plans" (all 2026-10-18).
- **Approval:** APPROVED by the Human Project Owner on 2026-10-18 (hosting "Oracle Always Free VM", address "agrinex.managio.me", production data "Curated copy").
