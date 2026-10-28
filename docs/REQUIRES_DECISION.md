# AgriNex — Unresolved Decisions (REQUIRES_DECISION)

This register lists genuine unresolved decisions only. Each item marks a point where either:

- no authoritative source establishes what is needed to proceed safely; or
- authoritative statements disagree with each other.

**Basis:** inspections of the repository and the full master specification (`docs/01_AGRINEX_MASTER_PRODUCT_PHASES.md`, version 2.0). Unless stated otherwise, § and line references are to that file.

| Items | Source | Date |
|---|---|---|
| RD-001–RD-012 | Control-layer inspection | 2026-10-02 |
| RD-013–RD-014 | Technology-baseline research | 2026-10-02 |
| RD-015–RD-026 | Architecture formalization (Task 6; see `docs/architecture/ARCHITECTURE_INDEX.md`) | 2026-10-03 |
| RD-027–RD-036 | Database architecture (Task 7; see `docs/database/DATABASE_INDEX.md`) | 2026-10-03 |
| RD-037 | Task 7 final decision lock (see `docs/architecture/DECISION_LOG.md`) | 2026-10-03 |
| RD-038–RD-043 | API contract design and its reviews (Task 8; see `docs/api/API_INDEX.md`) | 2026-10-04 |
| RD-044 | Decision closure and security review preparation (Task 8.5; see `docs/decision-review/TASK_8_5_HUMAN_DECISION_PACKET.md`) | 2026-10-04 |
| RD-045–RD-047 | UI/UX design exploration (Task 9; see `docs/02_AgriNex_UIUX_Phase1_Specification.md` §18 and `docs/decision-review/TASK_9_DESIGN_DIRECTION_PACKET.md`) | 2026-10-04 |
| RD-048–RD-049 | Backend engineering (Task 10) | 2026-10-05 |
| RD-050 | Real-data completion (Task 12) | 2026-10-15 |
| RD-051 | Phase 1 local readiness (ML readiness) | 2026-10-18 |

## Rules for this register

**Include only:**

- (a) inconsistencies within or between authoritative sources;
- (b) gaps where the specification defines a behavior but omits something needed to implement it, so that implementing it would mean inventing a rule, value, contract, or schema;
- (c) repository conditions that block safe progress;
- (d) decisions that governance ([`CLAUDE.md`](../CLAUDE.md)) requires before work can start, including security reviews.

**Exclude:**

- questions the master specification already answers;
- detail that belongs in a role file or contract not yet written (raise it then, if it is still open);
- speculative future needs.

**Blocking status:**

- **BLOCKING**: the work named in the item must not start until the item is resolved.
- **NON-BLOCKING**: no Phase 1 work is blocked.
- Each item names exactly what it blocks. None of the current items blocks documentation or governance work.

**Resolution:**

- Agents never resolve an item by picking an option themselves, unless a human has recorded an explicit delegation in the decision log. ADR-0001 records the delegation used for the 2026-10-03 decision lock.
- Resolution needs an explicit human decision, or a decision taken under a recorded human delegation, recorded in `docs/architecture/DECISION_LOG.md` or in an explicitly authorized update of the master specification.
- Once resolved, move the item to [Resolved](#resolved) with a pointer to where the decision is recorded.
- Items are never deleted, and IDs are never reused.

## Summary

**Decision lock (2026-10-03).** Under the delegation recorded in ADR-0001, the open items were resolved or deferred in `docs/architecture/DECISION_LOG.md`.

**API contract (Task 8, 2026-10-04).** The contract work found six new items. RD-038 to RD-040 each have a PROPOSED decision-log entry awaiting human approval: ADR-0035 to ADR-0037. RD-041 to RD-043 came from the Task 8 reviews and await a human choice between the options listed. The delegation in ADR-0001 does not cover any of them.

**Decision review (Task 8.5, 2026-10-04).** RD-038 to RD-043 were analyzed in `docs/decision-review/TASK_8_5_HUMAN_DECISION_PACKET.md`, and the security items in `docs/security/TASK_8_5_SECURITY_REVIEW.md`. The review found one new item, RD-044. No item was approved: each still needs a human decision.

**Decision lock (Task 8.6, 2026-10-04).** The Human Project Owner approved a resolution for each of RD-038 to RD-044 (ADR-0035 to ADR-0041) and signed off the security review (ADR-0042). These items are now under Resolved.

After Task 8.6, four items remained open:

- RD-002: decided, awaiting user action;
- RD-012: deferred to Phase 2;
- RD-025: partly decided, the rest deferred to Phase 2;
- RD-037: hosting procurement.

None of them blocks the API contract or UI/UX design work. RD-037 blocks only the public deployment, and RD-002 any commit or push of `.mcp.json`.

**UI/UX design exploration (Task 9, 2026-10-04).** Designing the Phase 1 screens found three new items, RD-045 to RD-047. None blocks the design exploration itself; each blocks approving specific copy in role file 02 (`docs/02_AgriNex_UIUX_Phase1_Specification.md`, then a draft pending approval). Seven items were then open.

**Task 9 final decision lock (2026-10-04).** The Human Project Owner decided H1 to H6. RD-045 is resolved by ADR-0045 (decided under the delegation recorded in ADR-0043), RD-046 by ADR-0046 and RD-047 by ADR-0047 with the mechanism in ADR-0048. No new item was found. Four items remain open: RD-002 (awaiting user action), RD-012 and RD-025 (deferred to Phase 2) and RD-037 (hosting procurement). None blocks Task 10's setup and infrastructure work; RD-037 blocks only the public deployment and, through the sending domain, production email (ADR-0048), and RD-002 any commit or push of `.mcp.json`.

**Backend engineering (Task 10, 2026-10-05).** Implementing ingestion and account email found two new items, RD-048 and RD-049. Neither blocks local development or tests. RD-048 blocks relying on historical ingestion until a live request settles the filter format; RD-049 blocks production account email, which RD-037 also blocks. Six items are open.

**Task 12 closure (2026-10-15).** Real-data completion found RD-050. Under the delegation recorded in ADR-0050, RD-050 is resolved by ADR-0051, and RD-048 and RD-049 by ADR-0052. Four items remain open: RD-002 (awaiting user action), RD-012 and RD-025 (deferred to Phase 2) and RD-037 (hosting procurement, which blocks the public deployment of Task 13 and production email). None blocks local use of Phase 1.

**Phase 1 local readiness (2026-10-18).** Measuring ML readiness found RD-051 (ADR-0053 PROPOSED), which needs the owner's decision. Five items are open; none blocks local use of Phase 1.

**Deployment decisions (2026-10-18).** The Human Project Owner resolved RD-037 (ADR-0055: Oracle Cloud Always Free VM, `agrinex.managio.me`, curated production data) and approved the historical maintenance of ADR-0054. Four items are open: RD-002 (awaiting user action), RD-012 and RD-025 (deferred to Phase 2) and RD-051 (owner's decision on ADR-0053). None blocks the public deployment; RD-002 still blocks any commit or push of `.mcp.json`.

### Open

| ID | Decision needed | Area | Status | Blocks |
|---|---|---|---|---|
| RD-002 | Handling of inline credentials in `.mcp.json` | Security, repository | BLOCKING (decided by ADR-0033; awaiting user action) | Committing `.mcp.json`; any push to a remote |
| RD-012 | Phase 2 listing, bid, and transaction state model | Phase 2 | NON-BLOCKING (DEFERRED by ADR-0032) | Phase 2 development (after Phase 1 Freeze) |
| RD-025 | Phase 2 OTP provider and listing TTL value (the rest is decided by ADR-0031) | Phase 2 | NON-BLOCKING (DEFERRED by ADR-0032) | Phase 2 development (after Phase 1 Freeze) |
| RD-051 | Offline evaluation of the forecasting baselines on real data before deployment (ADR-0053 PROPOSED) | ML (optional) | NON-BLOCKING for Phase 1; BLOCKING for running ML on real data | Experiment 3 evidence |

### Resolved

| ID | Decision | Recorded in | Date |
|---|---|---|---|
| RD-001 | Technology implementation baseline | ADR-0002 | 2026-10-03 |
| RD-003 | Phase 1 ingestion coverage and the commodity "known list" | ADR-0006 | 2026-10-03 |
| RD-004 | Date formats of the government datasets | ADR-0007 | 2026-10-03 |
| RD-005 | Ingestion behavior on a `business_key` conflict | ADR-0009 | 2026-10-03 |
| RD-006 | Comparison endpoint contract inconsistencies | ADR-0023 | 2026-10-03 |
| RD-007 | Composite scoring definition gaps | ADR-0024 | 2026-10-03 |
| RD-008 | Trend direction definition (FR-14 vs §33) | ADR-0025 | 2026-10-03 |
| RD-009 | What-If simulator calculation rules | ADR-0026 | 2026-10-03 |
| RD-010 | Design source of record and design approval | ADR-0027 | 2026-10-03 |
| RD-011 | Fixed demo admin credentials on a publicly accessible deployment | ADR-0028 | 2026-10-03 |
| RD-013 | Backend build tool: Maven or Gradle | ADR-0003 | 2026-10-03 |
| RD-014 | Minimum supported browsers for farmer-facing screens | ADR-0004 | 2026-10-03 |
| RD-015 | Price-observation selection for comparison, trends, and alerts | ADR-0010 | 2026-10-03 |
| RD-016 | Ingestion run orchestration | ADR-0011 | 2026-10-03 |
| RD-017 | Module data ownership and cross-module access paths | ADR-0013 | 2026-10-03 |
| RD-018 | Geographic reference data source and unknown markets | ADR-0014 | 2026-10-03 |
| RD-019 | Redis responsibilities and failure behavior | ADR-0018 | 2026-10-03 |
| RD-020 | Alert and notification delivery | ADR-0019 | 2026-10-03 |
| RD-021 | Public request routing, origin layout, and browser token handling | ADR-0017 | 2026-10-03 |
| RD-022 | Deployment topology | ADR-0021 | 2026-10-03 |
| RD-023 | Source of truth for `contracts/openapi.yaml` | ADR-0022 | 2026-10-03 |
| RD-024 | Backend–ML integration and forecast exposure | ADR-0030 | 2026-10-03 |
| RD-026 | Demo price data vs the no-fabrication rule | ADR-0028 | 2026-10-03 |
| RD-027 | Price unit and numeric representation of government prices | ADR-0008 | 2026-10-03 |
| RD-028 | Source records that share an observation key but differ in price | ADR-0009 | 2026-10-03 |
| RD-029 | Ingestion completeness metric when the source repeats observations | ADR-0012 | 2026-10-03 |
| RD-030 | Scope of market slug uniqueness | ADR-0014 | 2026-10-03 |
| RD-031 | Reference values the specification names but does not define | ADR-0006 | 2026-10-03 |
| RD-032 | Locality reference data | ADR-0014 | 2026-10-03 |
| RD-033 | Source of Marathi and Hindi reference names | ADR-0015 | 2026-10-03 |
| RD-034 | User account identifiers and registration fields | ADR-0016 | 2026-10-03 |
| RD-035 | Data retention and deletion policy | ADR-0029 | 2026-10-03 |
| RD-036 | Reference time zone for date-based rules | ADR-0007 | 2026-10-03 |
| RD-037 | Hosting procurement: cloud VM, region, size and domain name | ADR-0055 | 2026-10-18 |
| RD-038 | Module path for cross-module data in public responses | ADR-0035 | 2026-10-04 |
| RD-039 | Earth radius for haversine distances | ADR-0036 | 2026-10-04 |
| RD-040 | Account-erasure mechanism | ADR-0037 | 2026-10-04 |
| RD-041 | Transport-label wording when the rate is not ₹8/km | ADR-0038 | 2026-10-04 |
| RD-042 | Server-side enforcement of district-centroid origins | ADR-0039 | 2026-10-04 |
| RD-043 | Estimates that exceed the stored precision | ADR-0040 | 2026-10-04 |
| RD-044 | Account deactivation mechanism and audit | ADR-0041 | 2026-10-04 |
| RD-045 | Winner-callout wording: "Highest Net Realization" vs the composite-score rank 1 | ADR-0045 | 2026-10-04 |
| RD-046 | Source and review of Marathi and Hindi interface copy | ADR-0046 | 2026-10-04 |
| RD-047 | Password recovery path for farmers in Phase 1 | ADR-0047, ADR-0048 | 2026-10-04 |
| RD-048 | Date format of the historical `Arrival_Date` request filter | ADR-0052 item 5 | 2026-10-15 |
| RD-049 | Account email copy | ADR-0052 item 7 | 2026-10-15 |
| RD-050 | Source commodity spellings that are not Agmarknet 2.0 master names | ADR-0051 | 2026-10-15 |

---

## Open items

### RD-002 — Inline credentials in `.mcp.json`

- **ID:** RD-002
- **Question:** How should credentials for the project's MCP servers be supplied, and may `.mcp.json` be committed to version control?
- **Why It Matters:**
  - `.mcp.json` holds inline credential values for the `stitch`, `context7`, and `github` servers.
  - The file is untracked and the repository has no root `.gitignore`. Routinely adding and committing the whole working tree would therefore put these credentials into Git history, and a push would expose them.
  - The specification's secret rules cover the government API key and `.env` files (§27, §47 Q21). They do not cover tool credentials.
  - Agents may not change `.mcp.json` without explicit instruction (CLAUDE.md §F, §K).
- **Affected Area:** Security; repository hygiene; developer tooling (MCP configuration).
- **Source / Evidence:**
  - `.mcp.json`, inspected 2026-10-02. The values are deliberately not reproduced here.
  - `git status` shows `.mcp.json` as untracked.
  - There is no `.gitignore` at the repository root.
  - The repository has no commits, so the values are not in Git history.
- **Blocking Status:** BLOCKING: any commit that would include `.mcp.json`, and any push to a remote. Requires explicit human security review (CLAUDE.md rule 16).
- **Status update (2026-10-03, decision lock):** decided by ADR-0033. The item stays open as a repository condition (register rule (c)) until the user completes three actions:
  1. replace the inline values with `${…}` environment references;
  2. add a root `.gitignore` that excludes `.mcp.json` until it is converted;
  3. rotate any credential whose exposure is suspected.

  Agents may not perform these steps (CLAUDE.md §F, §K). Until then, no commit may include `.mcp.json`, and no push may happen.
- **Status update (2026-10-18):**
  - **Action 2 is done**, on the owner's explicit instruction ("Ensure `.env` files and credential-bearing `.mcp.json` files cannot be accidentally committed"). The root `.gitignore` now excludes `.mcp.json`, `.env`, `.env.*` (keeping the `.env.example` templates) and `.playwright-mcp/`.
  - A dry-run `git add -A` stages 783 files, and none of them is `.mcp.json`, `infra/.env` or `local-reference-data/`. None of them contains any of the 9 secret values of `infra/.env` and `.mcp.json`, or a common token pattern.
  - Actions 1 (convert to `${…}` references) and 3 (rotate exposed credentials) remain the owner's. `git add -f` can still override an ignore rule, so the item stays open.

### RD-012 — Phase 2 listing, bid, and transaction state model

- **ID:** RD-012
- **Question:** What is the authoritative Phase 2 state model? Specifically:
  - which states exist;
  - which transitions are allowed;
  - which actor triggers each transition;
  - which entity (listing, bid, or transaction) holds each state.
- **Why It Matters:**
  - FR-21, FR-22, and FR-24 require server-enforced transitions with a full audit trail, and §48 tests illegal transitions.
  - The specification's statements disagree. The transition rules cannot be implemented or tested without choosing between them.
- **Affected Area:** Phase 2 listing, bid, and transaction modules; Phase 2 schema; contracts; tests.
- **Source / Evidence:**
  - (a) The §18 transition table lists `REJECTED` as a terminal state, but the §18 diagram has no such state. F2.8 says "12 states": the diagram has 12, the table 13.
  - (b) The table allows three transitions the diagram omits: DRAFT→CANCELLED, PUBLISHED→EXPIRED, and BID_RECEIVED→EXPIRED.
  - (c) The diagram and the table name different actors:

    | Transition | Diagram actor | Table actor |
    |---|---|---|
    | PUBLISHED → CANCELLED | Farmer | System |
    | BIDDING_OPEN → BID_RECEIVED (the farmer views bids) | Farmer | System |
    | BID_ACCEPTED → DISPUTED | Either party | Farmer |

  - (d) §18 models listing and transaction states as one machine. §23 instead keeps three separate states: `produce_listings.status`, `bids.status` (PENDING, ACCEPTED, REJECTED), and `transactions.currentState`. It creates the transaction at BID_ACCEPTED.
  - (e) The concurrent-acceptance scenario names different actors:
    - "two farmers" (§18 note);
    - "another buyer" (§29);
    - "two buyers" (§47 Q19).

    Yet §28 lets only the listing-owner farmer, or an admin, accept bids.
- **Blocking Status:** NON-BLOCKING for Phase 1. Must be resolved before Phase 2 development starts. HARD RULE 1 (§10) places that after the Phase 1 Freeze.
- **Status update (2026-10-03, decision lock):** DEFERRED by ADR-0032 until Phase 2 starts, after the Phase 1 freeze (HARD RULE 1). It is a product decision about states, transitions and actors. Still NON-BLOCKING for Phase 1 and Task 8.

### RD-025 — Phase 2 architecture gaps

- **ID:** RD-025
- **Question:**
  - (a) Which provider delivers the OTP for buyer phone verification? The system context has no such external system, and §39 excludes unverified external API dependencies.
  - (b) Where and how are buyer verification documents stored for the "Backend file service"?
  - (c) Which module owns buyer verification and the file service?
  - (d) What mechanism and TTL drive system-initiated transitions such as listing expiry?
- **Why It Matters:**
  - Phase 2 requires these capabilities, but the spec's context, container, and module views do not place them.
  - Implementing them would add an external integration and storage infrastructure without an approved decision.
- **Affected Area:** Phase 2 architecture (system context, containers, modules, security, deployment); RD-012 (state model).
- **Source / Evidence:**
  - F2.1: "Buyer registration with phone verification" (line 381). §19: "OTP verification" (line 832).
  - §11: no OTP or SMS external system (lines 513–526). §39 (line 1683).
  - §28: "Backend file service" (line 1348). F2.12 (line 392).
  - §12: the module list has no verification module (line 540). §23: `buyer_verifications` (line 1159).
  - §18: "Listing expires (TTL)" and "System" actor transitions (lines 794, 812).
- **Blocking Status:** NON-BLOCKING for Phase 1. Must be resolved before Phase 2 development starts (HARD RULE 1).
- **Status update (2026-10-03, decision lock):** partly decided by ADR-0031:
  - (b) documents are stored in PostgreSQL with access control;
  - (c) buyer verification belongs to the auth module;
  - (d) system transitions run as a scheduled job in the listing module.

  Still open, DEFERRED by ADR-0032 until Phase 2 starts: (a) the OTP provider, and (d) the listing TTL value. NON-BLOCKING for Phase 1 and Task 8.

### RD-051 — Evaluating the forecasting baselines on real data before deployment

- **ID:** RD-051
- **Question:** May the existing forecasting baselines be evaluated offline, read-only, on the local real data before AgriNex is deployed and Phase 1 is frozen?
- **Why It Matters:**
  - Spec §35 criterion 1 (at least 90 observations per market–commodity pair) already holds for 1,301 VERIFIED pairs, 1,179 of them with data in every period of §35's split.
  - Criterion 2 ("complete **and deployed**") and criterion 3 ("after the Phase 1 freeze") are not met, and `docs/api/ML_CONTRACT_BOUNDARY.md` §8 forbids running the job before all criteria hold.
  - Without an evaluation there is no evidence on whether forecasting is worth building (§36 Experiment 3).
- **Affected Area:** Optional ML track; Experiment 3. No Phase 1 flow.
- **Source / Evidence:**
  - spec §10 HARD RULE 2, §35, §36;
  - ADR-0030; `ML_CONTRACT_BOUNDARY.md` §8; `ml/forecasting/README.md`;
  - `docs/data/PHASE1_DATA_AND_ML_READINESS_REPORT.md`.
- **Proposed resolution:** ADR-0053 (PROPOSED): one offline, read-only evaluation of the two baselines, without `--write`, with nothing exposed and the track not started.
- **Blocking Status:** NON-BLOCKING for Phase 1, which does not depend on ML (§10, P9). BLOCKING for any run of the forecasting job on real data until the owner decides ADR-0053, or the §35 criteria are met.

---

## Resolved

Resolved items keep their original text below the resolution lines, for history. Their "Blocking Status" and "Status update" lines describe the situation before resolution, and no longer block anything. IDs are never reused.

### RD-001 — Technology implementation baseline

- **Resolution (2026-10-03):** RESOLVED by ADR-0002 in `docs/architecture/DECISION_LOG.md`. The baseline in `docs/architecture/CURRENT_TECHNOLOGY_BASELINE.md` is approved, with Maven (ADR-0003) and the accepted browser floor (ADR-0004). It was re-verified against official registries on 2026-10-03.
- **Resolved by:** the project owner, in the conversation of 2026-10-03 ("The human approves the recommended technology baseline.").

- **ID:** RD-001
- **Question:** Which versions of the languages, frameworks, runtimes, database, cache, proxy, and container images are the approved implementation baseline? Are any departures from the versions the master specification states approved?
- **Why It Matters:**
  - Scaffolding, dependency manifests, Dockerfiles, and Compose image tags all pin versions.
  - Governance treats the specification's stated versions as a historical baseline. They must be verified for currency and compatibility before use (CLAUDE.md §J).
  - Departing from a stated version without an approved decision would be an unapproved architecture change.
  - A separate technology-baseline task will research the options. The decision itself still requires human approval.
- **Affected Area:** Technology baseline; architecture; operations/deployment; all application scaffolding.
- **Source / Evidence:**
  - The specification states its versions in three places:
    - header line 12: "Stack: Java 17 · Spring Boot 3.3.x · Next.js 14 · TypeScript · PostgreSQL 16 · Redis 7 · Python 3.11 (ML, optional) · Docker";
    - §12 container labels;
    - §45 service images (`postgres:16`, `redis:7-alpine`, `nginx:1.25`).
  - CLAUDE.md §J.
  - `docs/00_SSOT_INDEX.md` area 8: no baseline document exists.
  - No version research has been done yet, deliberately, because it is outside the scope of the control-layer task.
- **Blocking Status:** BLOCKING: application scaffolding, dependency installation, and container image selection.
- **Status update (2026-10-02):** the technology-baseline task produced a recommendation in `docs/architecture/CURRENT_TECHNOLOGY_BASELINE.md`, marked RECOMMENDED — PENDING HUMAN APPROVAL.
  - The technology families stay as the master specification establishes them.
  - The open parts are the exact version baseline, the build tool (RD-013), and the browser floor (RD-014).
  - This item stays open until a human approves the baseline and the approval is recorded in the decision log.

### RD-003 — Phase 1 ingestion coverage and the commodity "known list"

- **Resolution (2026-10-03):** RESOLVED by ADR-0006 in `docs/architecture/DECISION_LOG.md`. Maharashtra statewide, all commodities. The commodity known list is seeded from the official Agmarknet 2.0 commodity master (605 names, which match all 52 captured Maharashtra values). Varieties and grades register automatically. Unknown commodities are rejected as `UNKNOWN_COMMODITY`.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-003
- **Question:** Which states and which commodities are in Phase 1 ingestion scope? What is the authoritative "known list" that ingestion validates commodities against?
- **Why It Matters:**
  - Ingestion filters by state and commodity, and rejects records whose commodity is not in a known list. The specification defines neither the filter values nor the list.
  - Coverage affects data volume, reference-data seeding, validation-error rates, the admin health metrics, and Experiment 2 (§36).
  - The specification implies Maharashtra but never states Phase 1 coverage, and it gives no commodity list.
- **Affected Area:** Database / data (ingestion, normalization, reference data); commodity and geography modules; QA and evaluation.
- **Source / Evidence:**
  - Filters and validation:
    - §15 `&filters=state,commodity` (line 683);
    - §14 validation "commodity in known list" (line 649);
    - §42 item 7, reference-data seed: "states, districts, commodities, known markets".
  - Coverage as data configuration: §38 and §47 Q20. Q20 asks how to scale "to other states beyond Maharashtra".
  - Where Maharashtra appears: the header (issuing body), §1 (verified API behavior), §6 (personas), and §46 (demo).
  - Commodities named in the specification:
    - onion and tomato (§6, persona 1);
    - cotton and soybean (§6, persona 2).
- **Blocking Status:** BLOCKING: ingestion filter configuration, commodity validation, and reference-data seeding (§42 items 5–7).
- **Status update (2026-10-03, Task 7):**
  - The project owner's Task 7 instruction sets the Phase 1 database geography to **Maharashtra statewide** and says the commodity model must not be limited to onion, tomato, cotton and soybean. This answers the state part in substance, but it is not yet recorded in the decision log, so the item stays open.
  - The commodity known list remains open. The verified captures contain 52 distinct Maharashtra commodity values (`docs/database/SEED_AND_REFERENCE_DATA.md` §4). Live verification of the complete list was unavailable on 2026-10-03 (the data.gov.in pages returned HTTP 403), so the complete universe is UNVERIFIED. Cotton and soybean do not appear in any capture.
  - §14 sets no known-list check for varieties or grades. Whether ingestion registers new commodity, variety and grade values automatically belongs to this item.

### RD-004 — Date formats of the government datasets

- **Resolution (2026-10-03):** RESOLVED by ADR-0007 in `docs/architecture/DECISION_LOG.md`. Record dates are parsed strictly as `dd/MM/yyyy`, and the historical filter uses `dd-MM-yyyy`. Future-dated records are rejected (departure D-06).
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-004
- **Question:** Which date formats does each government dataset actually return and accept in filters? Which formats may the ingestion parser accept?
- **Why It Matters:**
  - The specification's format statements disagree.
  - If the parser accepts both day-first and month-first slash formats, any date whose day is 12 or lower becomes ambiguous. For example, `01/08/2026` could be read as 8 January instead of 1 August.
  - A misread date would silently corrupt `report_date`, and with it the business key, freshness classification, trends, and alerts.
  - The answer must come from verified API responses, not assumption.
- **Affected Area:** Database / data (ingestion transform and validation, `price_observations.report_date`, `business_key`); freshness; trends; alerts.
- **Source / Evidence:**
  - §15 step 5: "Parse date (dd-MM-yyyy or MM/dd/yyyy)" (line 689).
  - §14 historical extract: "Arrival_Date=dd-MM-yyyy" (line 647).
  - §1 reports the returned records as "dated 01/08/2026 through 13/09/2026" (line 98):
    - this is day-first with slashes, because 13/09 cannot be month-first;
    - it matches neither §15 format, if this notation reflects the API's own format.
- **Blocking Status:** BLOCKING: implementing ingestion date parsing (§42 items 5–6).
- **Status update (2026-10-03, Task 7):**
  - The verified government-data evidence shows every record in both datasets as `dd/MM/yyyy` (values `01/08/2026`, `07/09/2026`, `13/09/2026`, `14/09/2026`, with JSON-escaped slashes). The historical metadata declares the `Arrival_Date` filter format `dd-MM-yyyy`; the current metadata exposes no date filter (evidence §19.1–§19.2, §19.7, Appendix A).
  - This supports a strict `dd/MM/yyyy` record parser and `dd-MM-yyyy` for historical filters, and contradicts §15's "MM/dd/yyyy" (CONFLICT DB-C-1 in `docs/database/DATABASE_INDEX.md` §9).
  - The decision itself remains open, together with the treatment of future-dated records, which the spec does not address.

### RD-005 — Ingestion behavior on a `business_key` conflict

- **Resolution (2026-10-03):** RESOLVED by ADR-0009 in `docs/architecture/DECISION_LOG.md`. An identical re-delivery writes nothing and counts in `duplicates_skipped` (departure D-07). Changed prices update the row (the newer run wins) and leave a `price_observation_revisions` row.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-005
- **Question:** When an incoming record's `business_key` already exists, what happens? Either:
  - ingestion updates the stored observation and counts it under `records_updated` (if so, which fields: prices, `ingestion_run_id`, `ingested_at`?); or
  - ingestion skips it and counts it under `duplicates_skipped`.
- **Why It Matters:**
  - The specification describes both behaviors for the same case.
  - The choice decides:
    - whether source revisions to a price are reflected;
    - what `ingestion_run_id` and `ingested_at` mean on existing rows;
    - how the audit counters are computed.
  - Those counters feed the admin health API, which must match database counts exactly (§48), and the duplicate-rate metric of Experiment 2 (§36).
- **Affected Area:** Database / data (ingestion load step, `price_observations`, `ingestion_runs` counters); admin module; evaluation.
- **Source / Evidence:**
  - Conflicting descriptions of the same step:
    - §14 load: "ON CONFLICT (business_key) DO UPDATE if newer ingestion_run" (line 651).
    - §15 upsert: "DO UPDATE SET ingested_at=NOW() (if same price, still update timestamp)" and "Increment records_inserted or records_updated" (line 691).
    - §15 duplicate check, right after the upsert: "business_key already exists? … Yes → Increment duplicates_skipped / Skip insert" (lines 695–696).
    - §22: "UPSERT price_observations ON CONFLICT (business_key) DO UPDATE" (line 1120).
  - Related schema and policy:
    - §20 `INGESTION_RUNS` has both `records_updated` and `duplicates_skipped`.
    - The §49 disclaimer says prices may be "revised".
- **Blocking Status:** BLOCKING: implementing the ingestion load and audit steps (§42 items 5–6).
- **Status update (2026-10-03, Task 7):**
  - Conflicting records can also arrive together in one response, not only against stored rows: H-07SEP-2026 contains four same-key pairs with different prices. That case is registered separately as RD-028.
  - PostgreSQL rejects an `INSERT … ON CONFLICT DO UPDATE` that would affect the same row twice in one statement (a cardinality violation; PostgreSQL 16 `INSERT` documentation, fetched 2026-10-03). Batch upserts must therefore deduplicate within each batch, whatever this item decides.
  - The schema keeps both §20 counters (`records_updated`, `duplicates_skipped`) and leaves the meaning of `ingestion_run_id` and `ingested_at` on existing rows open (`docs/database/DATABASE_ARCHITECTURE.md` §10.7, §11).

### RD-006 — Comparison endpoint contract inconsistencies

- **Resolution (2026-10-03):** RESOLVED by ADR-0023 in `docs/architecture/DECISION_LOG.md`. The fields are `quantityUnit` and `netRealizationWinnerId`. 422 `NO_MARKETS_IN_RANGE` when no market is in range. 200 with empty `options`, `noDataReason: NO_RECENT_PRICE_DATA` and `excludedMarkets[]` when markets lack recent data.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-006
- **Question:** For `POST /api/v1/selling-options/compare`, what are:
  - (a) the canonical request field name for the quantity unit;
  - (b) the canonical response field name for the net-realization winner;
  - (c) the response when markets in range lack recent price data? Specifically: are excluded markets returned in `options`, and if so, how are they marked? What status and code are returned when no eligible option remains?
- **Why It Matters:**
  - This endpoint is the Phase 1 hero flow (F1.8–F1.14).
  - A frontend built from §21 and a backend built from §16 would disagree on field names. A mock with wrong field names is a bug (§44).
  - The no-data case drives the frontend empty states (§25, §29).
  - Contract-first development (P5) needs one answer before the OpenAPI skeleton defines this endpoint.
- **Affected Area:** API contracts (`contracts/openapi.yaml`, `contracts/recommendation.schema.json`, mocks); recommendation module; frontend comparison screen; QA contract tests.
- **Source / Evidence:**
  - (a) Two names for the quantity unit:
    - §16 input lists `quantityUnit` (line 715);
    - §21 step 4 sends `unit:KG` (line 1064).
  - (b) Two names for the net-realization winner:
    - §16 response lists `netRealizationWinnerId` (line 749);
    - §21 step 24 lists `netWinnerId` (line 1085).
  - (c) Different treatment of the no-data case:
    - §16 excludes markets with no recent observation, with reason `NO_RECENT_PRICE_DATA`, and routes them into "Collect all eligible candidates" (lines 724, 743–744).
    - §16 returns 422 `INSUFFICIENT_DATA` / `NO_MARKETS_IN_RANGE` only when no markets are in range (line 719).
    - §29 says "Return empty options array with appropriate code" when there is no recent data, without naming the code (line 1372).
- **Blocking Status:** BLOCKING: defining the comparison endpoint in `contracts/openapi.yaml`, its mocks, and its implementation (§42 items 2, 15).

### RD-007 — Composite scoring definition gaps

- **Resolution (2026-10-03):** RESOLVED by ADR-0024 in `docs/architecture/DECISION_LOG.md`. Default weights 0.55/0.15/0.10/0.10/0.10. Trend score 0.5 for INSUFFICIENT_DATA. Min–max `normalized_transport`, with constant values when max = min. Deterministic tie-breaks and reason-code thresholds.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-007
- **Question:** What are the approved values and rules for the parts of the composite score that the specification leaves undefined?
  - (a) Default values for `w_trend`, `w_freshness`, and `w_transport_penalty`. Also confirm `w_net` = 0.55 and `w_price` = 0.15, which appear only in an example.
  - (b) The `trendScore` for `INSUFFICIENT_DATA`.
  - (c) The definition of `normalized_transport`.
  - (d) Min–max normalization when the maximum equals the minimum, for example with a single eligible market or identical values.
- **Why It Matters:**
  - FR-10 requires ranking by composite score, with net realization as the dominant factor.
  - Scoring must be covered by deterministic unit tests (§27).
  - Without these values, an implementer would have to invent business rules.
  - In case (d), the formula would otherwise divide by zero.
- **Affected Area:** Recommendation module (scoring, ranking, `rankingsDisagree`); `recommendation_weights` seed data; admin weights configuration; unit-test fixtures; Experiment 1 (§36).
- **Source / Evidence:**
  - §16 score node (line 739):
    - defines `normalized_net` and `normalized_price`;
    - defines `trendScore` (RISING 1.0, STABLE 0.5, FALLING 0.0) and `freshnessScore`;
    - uses `normalized_transport` without defining it.
  - §20 `RECOMMENDATION_WEIGHTS` lists no defaults.
  - §21 step 6 shows `weights {w_net:0.55, w_price:0.15, ...}` (line 1066).
  - §33 defines `INSUFFICIENT_DATA` (line 1480).
  - FR-10 (line 1288).
  - By contrast, the specification does state defaults for freshness days (§16) and for transport (§32).
- **Blocking Status:** BLOCKING: implementing composite scoring and ranking, and seeding `recommendation_weights` (§42 items 13–15).

### RD-008 — Trend direction definition (FR-14 vs §33)

- **Resolution (2026-10-03):** RESOLVED by ADR-0025 in `docs/architecture/DECISION_LOG.md`. One §33 `trendDirection`, with `INSUFFICIENT_DATA` as a valid value and contiguous IST windows. The 30-day toggle changes only the chart window.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-008
- **Question:**
  - What does FR-14's "30-day trend direction" mean? §33 defines a single `trendDirection`: the last-7-day average compared with the 8–14-day baseline.
  - Is `INSUFFICIENT_DATA` (§33) one of the trend direction values? FR-14 lists only RISING, STABLE, and FALLING.
- **Why It Matters:**
  - The answer determines what the trend engine computes and what `TrendResponse` returns.
  - Implementing a separate 30-day direction without a definition would invent a business rule. Omitting it would leave FR-14 unmet.
- **Affected Area:** Price module (trend computation); `TrendResponse` contract; frontend trend chart and badge; tests.
- **Source / Evidence:**
  - FR-14 (line 1292).
  - The §33 algorithm (lines 1471–1480).
  - §21 steps 29–32: a `days=30` request returns `trendDirection` together with `changePercent7Day` (lines 1090–1093).
  - F1.15: 7-day and 30-day charts (line 338).
- **Blocking Status:** BLOCKING: finalizing `TrendResponse` and declaring FR-14 met. Implementing the §33 algorithm as written (§42 item 10) is not blocked.

### RD-009 — What-If simulator calculation rules

- **Resolution (2026-10-03):** RESOLVED by ADR-0026 in `docs/architecture/DECISION_LOG.md`. The full §32 formula is used client-side (departure D-13). The simulator re-ranks by net realization, and server mode is used only when the radius exceeds the original one.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-009
- **Question:**
  - (a) Must the What-If recalculation apply the full §32 transport model, including the configured `minimumTripCharge` and the multi-trip factor `ceil(quantityInKg / vehicleCapacityKg)`?
  - (b) Must it re-rank by the §16 composite score, or by net realization only?
  - (c) How does client-side mode handle a maximum distance above the original comparison radius?
  - (d) When must the server-side simulate endpoint be used instead?
- **Why It Matters:**
  - The simulator's quantity slider reaches 5,000 kg, but the default vehicle capacity is 1,000 kg.
  - The §17 client-side formula has no trip factor and hard-codes ₹300. For multi-trip quantities, or when an admin changes the minimum charge, it would contradict the comparison endpoint for the same inputs.
  - Hand-computed test fixtures (§48) need a single formula.
  - The answer may also determine which transport parameters the comparison response must carry.
- **Affected Area:** Frontend What-If simulator; recommendation module (`POST /api/v1/selling-options/simulate`); comparison and simulate contracts; unit and E2E tests.
- **Source / Evidence:**
  - §17 (lines 758–763):
    - the client-side formula `newTransport = max(300, dist × newRate)`;
    - "Re-sort by newNet", applied over "pre-fetched" markets;
    - sliders: quantity 100–5000 kg, rate ₹3–₹20/km, distance 25–300 km;
    - "Server-side (if complex)", with no stated trigger.
  - §32:
    - the formula with `trips` and a default `vehicleCapacityKg` of 1,000 kg (lines 1436–1455);
    - the What-If override names only `ratePerKm` and `maxDistanceKm` (line 1463), while FR-15 and §17 also include quantity.
  - FR-07 (line 1285).
  - §16 ranks by composite score (line 745).
  - §10: "recalculates client-side without full API re-call" (line 483).
  - §42 items 16 and 23.
- **Blocking Status:** BLOCKING: implementing the What-If simulator, frontend and backend, and fixing the comparison and simulate contract fields it depends on (§42 items 16, 23).

### RD-010 — Design source of record and design approval

- **Resolution (2026-10-03):** RESOLVED by ADR-0027 in `docs/architecture/DECISION_LOG.md`. Role file 02 is the design source of record. The Document Owner records design approvals in SSOT index area 5.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-010
- **Question:**
  - What is the design source of record for production screens: the UI/UX specification (role file 02), Stitch designs, or both? If both, which wins when they differ?
  - How is a screen design's approval recorded?
- **Why It Matters:**
  - CLAUDE.md §M forbids building production screens from invented designs and requires an approved design source.
  - The specification assigns screen design to the UI/UX role and plans a UI/UX specification file. It never mentions Stitch or a design-approval process.
  - The repository configures a Stitch MCP server, so a design tool is evidently intended. Its standing relative to the specification is undefined.
- **Affected Area:** UX / design; frontend; QA (visual and acceptance checks).
- **Source / Evidence:**
  - From the specification:
    - §40 RACI: UI/UX is responsible for "Design system & tokens" and "Screen design — all states".
    - §43 and §44 name `02_AgriNex_UIUX_Phase1_Specification.md`, which does not exist yet.
    - A search of the specification finds no mention of Stitch or of design approval.
  - From the repository:
    - `.mcp.json` configures a `stitch` server.
    - `.claude/agents/design-engineer.md` is empty.
    - `.claude/skills/agrinex-design/` is empty.
  - From Stitch: on 2026-10-02, the connected account had no AgriNex project.
- **Blocking Status:** BLOCKING: production frontend screen implementation (§42 items 17–24 and 26). The exception is a specific screen whose design source the user explicitly confirms.

### RD-011 — Fixed demo admin credentials on a publicly accessible deployment

- **Resolution (2026-10-03):** RESOLVED by ADR-0028 in `docs/architecture/DECISION_LOG.md`. No fixed or published credentials on any public deployment. The first admin is bootstrapped from the deployment environment. Test accounts exist only in demo-profile environments.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-011
- **Question:**
  - May the publicly accessible deployment use the fixed admin email and password shown in the specification's demo narrative, or any other published, fixed credentials?
  - If not, how are admin and test-account credentials provisioned for deployed environments?
- **Why It Matters:**
  - Phase 1 must be deployed to a cloud VM and publicly accessible.
  - The specification plans a test-accounts fixture with one account per role.
  - The ADMIN role can change recommendation weights and transport assumptions, trigger ingestion, and review buyer verification. Published fixed credentials on a public deployment would let anyone do the same.
  - This is a security decision that needs explicit review (CLAUDE.md rule 16).
- **Affected Area:** Security (authentication, secrets); operations/deployment; fixtures (`fixtures/test_accounts.sql`); demo.
- **Source / Evidence:**
  - The §46 demo step "Switch to admin view" names an admin email and password (line 1990; not repeated here).
  - §7: "Deployed to a cloud VM and publicly accessible" (line 314).
  - §43 and §44: `fixtures/test_accounts.sql`, "One account per role for testing".
  - ADMIN capabilities: §28, and the admin endpoints in §30 (`PUT /api/v1/admin/config/*`, `POST /api/v1/admin/ingestion/trigger`).
- **Blocking Status:** BLOCKING: any publicly accessible deployment, and any non-local use of seeded accounts. Local development is not blocked.

### RD-013 — Backend build tool: Maven or Gradle

- **Resolution (2026-10-03):** RESOLVED by ADR-0003 in `docs/architecture/DECISION_LOG.md`. Maven 3.10.x with the Maven Wrapper.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-013
- **Question:** Should the backend use Maven or Gradle as its build tool?
- **Why It Matters:**
  - A build tool is needed before backend scaffolding, the backend Dockerfile, and CI can be created.
  - Both tools are fully compatible with the recommended baseline (Spring Boot 4.1.1, Java 25), so no technical evidence decides between them. Choosing one without a decision would invent a project choice.
- **Affected Area:** Technology baseline; backend build; CI; Docker.
- **Source / Evidence:**
  - Found during the technology-baseline research on 2026-10-02 (`docs/architecture/CURRENT_TECHNOLOGY_BASELINE.md` §7).
  - The master specification never mentions Maven or Gradle. Its `src/main/java` layout (§43) fits both.
  - The repository contains no build files.
  - Spring Boot 4.1.1 supports Maven 3.6.3+ and Gradle 8.14+ or 9.x. Java 25 requires Gradle 9.1.0+.
  - Maven 4 is release-candidate only.
- **Blocking Status:** BLOCKING: backend scaffolding, the backend Dockerfile, and CI.

### RD-014 — Minimum supported browsers for farmer-facing screens

- **Resolution (2026-10-03):** RESOLVED by ADR-0004 in `docs/architecture/DECISION_LOG.md`. The Next.js 16 / Tailwind 4 floor is accepted. Android 7.0+ devices receive Chrome 119 or later, which meets the floor.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-014
- **Question:** Is the browser floor of the supported frontend stack acceptable for AgriNex's farmer users? That floor is Chrome/Edge 111+, Safari 16.4+, and Firefox 128+. If not, which older browsers must be supported?
- **Why It Matters:**
  - Next.js 16 is the only supported Next.js major line after about 2026-10-21. It supports Chrome/Edge/Firefox 111+ and Safari 16.4+.
  - Tailwind CSS v4 requires Firefox 128+.
  - The specification describes a farmer persona using an "Older Android 4.7"" device on intermittent 3G, but it defines no minimum OS or browser.
  - Supporting older browsers would require browser targeting and polyfills, and probably Tailwind CSS v3.4 instead of v4.
- **Affected Area:** UX / design; frontend; technology baseline.
- **Source / Evidence:**
  - Master specification §6 (Persona 2) and F1.21 (§7).
  - Next.js installation docs ("Supported browsers") and the Next.js 16 upgrade guide.
  - The Tailwind CSS compatibility page.
  - `docs/architecture/CURRENT_TECHNOLOGY_BASELINE.md` §4 and §7.
- **Blocking Status:** BLOCKING: confirming the Tailwind CSS major version at frontend scaffolding. Backend work is not blocked.

### RD-015 — Price-observation selection for comparison, trends, and alerts

- **Resolution (2026-10-03):** RESOLVED by ADR-0010 in `docs/architecture/DECISION_LOG.md`. Selection uses both datasets, with current-dataset precedence for the same identity (departure D-08). The variety and grade filter is exact. Otherwise the lower-median representative observation is used, never a synthesized price.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-015
- **Question:** Which stored price observations does each calculation use?
  - (a) Does comparison (and alert evaluation) use only current-dataset observations, and do trends use only historical-dataset observations, as the spec's dataset roles state? Or are both datasets mixed?
  - (b) Several observations can exist for the same market, commodity, and date: different varieties or grades, or one row from each dataset. Which one is used?
  - (c) Does a farmer-selected variety or grade (F1.4) filter the observations?
- **Why It Matters:**
  - The business key includes `source_dataset_id`, so same-day rows from both datasets coexist.
  - The spec's selection queries filter by neither dataset nor variety/grade. They could pick an arbitrary row (`LIMIT 1`) or double-count in averages, which makes ranking, trends, and alerts non-deterministic.
  - Deterministic unit tests (§27, §48) need a single rule.
  - This is CONFLICT C-01 in `docs/architecture/ARCHITECTURE_INDEX.md`.
- **Affected Area:** Recommendation module (freshest-observation selection); price module (trends); alert evaluation; ML input; Task 7 (query and index design, whether searches store variety/grade); Task 8 (compare input semantics).
- **Source / Evidence:**
  - Dataset roles:
    - §1: current dataset "Latest prices, comparison, alerts, daily sync"; historical dataset "Trends, ML training data, historical bootstrap" (lines 91–92).
    - F1.7: "Latest reported mandi prices (current dataset)" (line 330).
    - §33: "Data source: … historical variety-wise prices" (line 1469).
  - Queries:
    - §16 freshest-observation query filters only on market, commodity, and date (line 722), although the input accepts `varietyId?` and `gradeId?` (line 715).
    - §33 step 1 has no dataset filter (line 1472).
    - §34 alert conditions use the "latestModalPrice" (lines 1496–1499).
  - Schema: the §14 business key includes `source_dataset_id` (line 650). §20 `SELLING_SEARCHES` has no variety or grade fields (lines 973–982). F1.4 offers "Commodity + variety/grade selection" (line 327).
- **Blocking Status:** BLOCKING: implementing comparison (§42 item 15), trend computation (item 10), and alert evaluation (item 24). It also affects Task 7 and Task 8 design.
- **Status update (2026-10-03, Task 7):**
  - The database design keeps every distinction this item needs, so the schema does not presume an answer. Every observation carries `source_dataset_id`, `variety_id` and `grade_id`; `selling_searches` records a farmer's variety and grade selection, or NULL when none is made (`docs/database/DATA_DICTIONARY.md`).
  - The evidence confirms the multiplicity: one market, commodity and date carries several varieties (for example `APMC Shevgaon` Onion on 07/09/2026: `2nd Sort`, `1st Sort`, `Other`).
  - Task 7's schema no longer depends on this item. Comparison, trend and alert queries still do.

### RD-016 — Ingestion run orchestration

- **Resolution (2026-10-03):** RESOLVED by ADR-0011 in `docs/architecture/DECISION_LOG.md`. A 6-hourly CURRENT run, then a HISTORICAL run over a trailing window. The bootstrap uses the manual trigger with a date range. One RUNNING run per dataset. No persisted resume state. Explicit status rules.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-016
- **Question:**
  - (a) After the bootstrap, is the historical dataset ingested on the 6-hour schedule, or only during the bootstrap?
  - (b) How is the historical bootstrap triggered: startup, manual trigger with a dataset type, or `scripts/bootstrap_historical.sh`?
  - (c) What happens if a run is triggered while another is RUNNING?
  - (d) Where does the "resume from last successful offset" state live, and is a resumed run a new `ingestion_run`?
  - (e) After retries, when does an API error produce FAILED and when PARTIAL_FAILURE?
- **Why It Matters:**
  - These rules decide how fresh the trend data is, how runs are audited and counted, and how Experiment 2's sync success rate is computed.
  - Without them, an implementer would invent scheduling and status rules.
- **Affected Area:** Ingestion module; `ingestion_runs` semantics; admin health; Experiment 2; deployment scripts.
- **Source / Evidence:**
  - §10: "bootstrapped with ≥60 days of data" and "Ingestion scheduler running on 6-hour cadence" (lines 457, 462).
  - §15 and §22: the scheduled run shows only the current resource (lines 683, 1113–1116). `triggeredBy=SCHEDULER|MANUAL|STARTUP` (line 681).
  - §15: API error → "Mark run status=FAILED" (line 687), while FINALIZE sets "SUCCESS|PARTIAL_FAILURE" (line 702).
  - §22: "Exponential backoff, max 3 retries" (line 1125).
  - §29: partial ingestion keeps successful pages (line 1371).
  - §37: "Resume from last successful offset" (line 1639).
  - §43: `scripts/bootstrap_historical.sh` (line 1853).
  - §45: trigger body `{"datasetType":"CURRENT_PRICE"}` (line 1925).
  - FR-02 (line 1280).
- **Blocking Status:** BLOCKING: implementing ingestion scheduling, triggers, and run-status handling (§42 items 5–6), and defining the Experiment 2 metrics.
- **Status update (2026-10-03, Task 7):**
  - The schema records each run's request scope, the total the source reports, pages fetched and failed, and a page offset on errors (`docs/database/DATA_DICTIONARY.md`, `ingestion_runs` and `ingestion_errors`).
  - It stores no resume state. If (d) chooses resume state held in the database, one column is added.
  - It does not enforce a single `RUNNING` run per dataset, because that would decide (c).

### RD-017 — Module data ownership and cross-module access paths

- **Resolution (2026-10-03):** RESOLVED by ADR-0013 in `docs/architecture/DECISION_LOG.md`. The `docs/database/TABLE_OWNERSHIP.md` proposal is adopted, with owners for the four new tables and an approved, acyclic set of module dependencies.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-017
- **Question:** Which backend module owns each table, and how do the cross-module flows reach data they do not own?
  - (a) Ingestion writing `price_observations`.
  - (b) Alert evaluation after each run (`price_alerts`, `alert_events`).
  - (c) The admin module updating transport assumptions and recommendation weights, which the recommendation and transport modules read.
- **Why It Matters:**
  - §27 forbids cross-module direct database access, yet the ingestion sequence touches tables that §13 and §34 place behind other modules.
  - Table ownership is an architecture decision (database ownership, CLAUDE.md §G), and Task 7 needs it.
  - This is CONFLICT C-02 in `docs/architecture/ARCHITECTURE_INDEX.md`.
- **Affected Area:** Component architecture; Task 7 (table and migration ownership); ingestion, alert, admin, recommendation, transport, and price modules.
- **Source / Evidence:**
  - §27: "No cross-module direct DB access; each module owns its repositories" (line 1325). §47 Q11 (line 2044).
  - §13 dependencies: ingestion → price, admin → ingestion, recommendation → transport and price (lines 619–622).
  - §15 and §22: `IngestionService` upserts `price_observations`, selects `price_alerts`, and inserts `alert_events` (lines 691, 704, 1120, 1130–1131).
  - §34: `AlertEvaluationService` (line 1501).
  - §30: `PUT /api/v1/admin/config/*` (line 1396).
  - §32: "Admin-Managed in `transport_assumptions` table" (line 1449).
  - §16: the recommendation flow loads weights and assumptions (line 715).
- **Blocking Status:** BLOCKING: Task 7 (database architecture), and implementing the ingestion load, alert evaluation, and admin configuration (§42 items 5, 24, 25).
- **Status update (2026-10-03, Task 7):**
  - Task 7 drafted a proposed resolution in `docs/database/TABLE_OWNERSHIP.md`:
    - one owner module per table;
    - writes to `price_observations` only through the price module;
    - post-run alert evaluation invoked through the alert module;
    - configuration tables owned by the transport and recommendation modules and changed through their interfaces;
    - `source_datasets` owned by the price module.
  - The proposal needs six new module dependencies and one conditional dependency (`TABLE_OWNERSHIP.md` §4).
  - It is PENDING APPROVAL. This item stays open until a human approves the proposal and the approval is recorded in the decision log.

### RD-018 — Geographic reference data source and unknown markets

- **Resolution (2026-10-03):** RESOLVED by ADR-0014 in `docs/architecture/DECISION_LOG.md`. LGD for names and codes. District centroids from Survey of India boundaries. Market coordinates default to the district centroid (flagged approximate). Unknown markets register as PROVISIONAL, and admins verify or merge them on evidence.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-018
- **Question:**
  - (a) What is the authoritative source for district centroid coordinates and market coordinates?
  - (b) When does the "derived from district centroid" approximation apply to market coordinates?
  - (c) What does ingestion do with a record whose market is not in the reference data?
- **Why It Matters:**
  - Every distance and transport calculation depends on these coordinates.
  - Neither government dataset is documented as providing coordinates, and the spec names no other source.
  - External APIs are out of scope unless verified (§39). Reference data must come from an identified source (CLAUDE.md §C).
  - Rejecting unknown markets could also threaten the ≥95% ingestion-completeness criterion.
- **Affected Area:** Database / data (reference data, `markets`, `districts`); transport and recommendation modules; ingestion; system context (external data sources).
- **Source / Evidence:**
  - FR-06: distance from the farmer's district centroid (line 1284).
  - §16: haversine on `market.lat/lon` and "is_approximate = market.lat derived from district centroid" (lines 717, 727).
  - §20: `DISTRICTS` centroid fields and `MARKETS` lat/lon (lines 858–880).
  - §42 item 7: "Reference data seed (states, districts, commodities, known markets)" (line 1755). §43: `seed_data.sql` (line 1847).
  - §14: validation checks "commodity in known list" but not markets (line 649).
  - §38: routing APIs are future scope (line 1659). §39: no unverified external API dependency (line 1683).
  - §48: ingestion completeness ≥95% (line 2097).
- **Blocking Status:** BLOCKING: reference-data seeding (§42 item 7), the transport engine's distance input (item 11), and ingestion handling of unknown markets (item 5).
- **Status update (2026-10-03, Task 7):**
  - Neither government dataset contains coordinate fields (evidence §19.3), so coordinates must come from another identified source.
  - The schema holds the fields without choosing a source: `districts.centroid_lat`, `centroid_lon` and `centroid_source` (mandatory); `markets.lat`, `lon`, `coordinate_source` and `is_coordinate_approximate` (nullable, with provenance required when coordinates are present).
  - Markets resolve through `market_source_aliases`, keyed by source state, district and market. The key reflects the evidence: trailing whitespace, alias spellings such as `Manmad APMC` / `APMC Manmad`, and `Sirsa APMC` in two states.
  - For (c): rejecting unknown markets, with replay from `ingestion_errors`, needs no schema change; registering provisional markets would need a review-status column. How alias mappings are maintained (seed file or an admin function; the spec defines no API) also belongs to (c).

### RD-019 — Redis responsibilities and failure behavior

- **Resolution (2026-10-03):** RESOLVED by ADR-0018 in `docs/architecture/DECISION_LOG.md`. Redis holds rate-limit counters and a reference-data cache only (departure D-09). An in-process limiter and a cache bypass take over when it is down. Redis is excluded from readiness.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-019
- **Question:**
  - (a) What does the Redis "session cache" hold, given that access tokens are stateless JWTs and refresh tokens are stored in PostgreSQL?
  - (b) What, if anything, is the Redis "cache" used for? For example, the "< 300ms (with caching)" GET endpoints.
  - (c) When Redis is unavailable, does the per-IP rate limiter on the comparison endpoint fail open or closed, and how do health and readiness react?
- **Why It Matters:**
  - Redis sits on the hero-flow request path through rate limiting, yet the spec defines no Redis failure behavior.
  - Fail-open vs fail-closed is a security and availability trade-off that needs explicit review (CLAUDE.md rule 16).
  - This is CONFLICT C-04 in `docs/architecture/ARCHITECTURE_INDEX.md`.
- **Affected Area:** Container architecture; security (rate limiting); resilience; deployment health checks.
- **Source / Evidence:**
  - §12: Redis "Rate limiting / Session cache / Alert queue" (line 545). §45: "Rate limiting, cache" (line 1892).
  - §27: "< 300ms (with caching)" (line 1312) and health "with DB + Redis sub-checks" (line 1324).
  - §28: stateless JWT, refresh tokens in the DB, Redis-backed rate limiting (lines 1336–1337, 1343).
  - §29: the failure table has no Redis row (lines 1368–1376).
- **Blocking Status:** BLOCKING: implementing rate limiting and any Redis-backed caching, and configuring health and readiness. Requires explicit security review.

### RD-020 — Alert and notification delivery

- **Resolution (2026-10-03):** RESOLVED by ADR-0019 in `docs/architecture/DECISION_LOG.md`. Evaluation after each run that changed observations, at most one event per alert per run, in-app polling, and an admin dashboard banner for ingestion failures.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-020
- **Question:**
  - (a) Are in-app alert events polled by the frontend or pushed to it?
  - (b) What is the Redis "Alert queue"?
  - (c) Which API exposes alert events, and how are viewed or dismissed events represented?
  - (d) Through which channel, and to whom, does the "Send admin alert" step on an ingestion API failure go?
- **Why It Matters:**
  - The spec leaves the delivery mechanism explicitly open.
  - §30 lists only `/api/v1/alerts` (returning `AlertResponse`) and no endpoint for alert events.
  - Push delivery would add a capability the spec does not describe.
  - The admin-alert step has no channel.
- **Affected Area:** Alert module; frontend alert UI; Task 8 (alert APIs); ingestion failure handling; Redis responsibilities.
- **Source / Evidence:**
  - §34: "In-app notifications only (alert_events table → polled or pushed to frontend)" (line 1503). Dismissal leaves alerts ACTIVE (line 1508).
  - §12: Redis "Alert queue" (line 545).
  - §14: "Alert Events … In-app notifications" (line 672).
  - §30: the alert row (line 1393).
  - §15: "Send admin alert" (line 687).
- **Blocking Status:** BLOCKING: the alert API and UI (§42 item 24; Task 8, Task 9) and the ingestion-failure notification.

### RD-021 — Public request routing, origin layout, and browser token handling

- **Resolution (2026-10-03):** RESOLVED by ADR-0017 in `docs/architecture/DECISION_LOG.md`. A single origin behind Nginx. The access token stays in memory, and the refresh cookie is `HttpOnly; Secure; SameSite=Strict; Path=/api/v1/auth`. Actuator is not public. Forwarded client IPs come only from trusted proxies. Rate limits are defined.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-021
- **Question:**
  - (a) How does public traffic reach the frontend container and the backend, and which paths does Nginx route where? `/api/v1/*` goes to the backend, but the frontend's own `/api/health` must not.
  - (b) Do the frontend and the API share one public origin, and therefore is CORS needed, and where: Nginx, the backend, or both?
  - (c) What scope and protection attributes does the httpOnly refresh cookie have?
  - (d) Where does the browser keep the 15-minute access token?
  - (e) Does Next.js server-side code call the backend, and over which path?
  - (f) How does the real client IP reach the backend for per-IP rate limiting behind Nginx?
  - (g) Is `/actuator/health` exposed publicly?
- **Why It Matters:**
  - These choices set the trust boundary between the browser, the edge, and the backend.
  - They are security-sensitive: token handling, CORS, and rate limiting (CLAUDE.md rule 16).
  - Without them, `nginx.conf`, the authentication flow, and the frontend API client cannot be written without invention.
- **Affected Area:** Security architecture; deployment (`infra/nginx.conf`); auth module; frontend API client; rate limiting.
- **Source / Evidence:**
  - §12: Nginx "TLS termination / CORS headers" and the frontend calling `/api/v1/*` with JWT Bearer through Nginx (lines 562, 565).
  - §28: CORS in backend `WebConfig`, per-IP rate limiting, and secure headers in Nginx (lines 1342–1344).
  - §27: token lifetimes (line 1318).
  - §29: refresh "using httpOnly cookie" (line 1375).
  - §45: local access directly on `localhost:3000` and `localhost:8080`, and the frontend `/api/health` (lines 1907, 1914, 1918).
  - §10: "Health check endpoints respond at `/actuator/health`" (line 498).
- **Blocking Status:** BLOCKING: `infra/nginx.conf`, auth implementation (§42 item 8), the frontend API client (item 17), and CORS configuration. Requires explicit security review.

### RD-022 — Deployment topology

- **Resolution (2026-10-03):** RESOLVED by ADR-0021 in `docs/architecture/DECISION_LOG.md`. Root compose for the full stack, a dev override, and an infra-only compose. One env contract. certbot on the host. Reference seed through Flyway, demo data only with the demo profile, and a named volume with daily dumps. Provider and domain procurement is RD-037.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-022
- **Question:**
  - (a) What are the roles of `infra/docker-compose.yml`, `infra/docker-compose.dev.yml`, and the root `docker-compose.yml` ("Production compose file")? What is the relationship between `infra/.env.example` and the root `.env.example`?
  - (b) Which environments exist? Local, staging, and the public cloud VM: do staging and production share a host? Which environment runs Experiment 2's 14 consecutive days of ingestion?
  - (c) Where does certbot run, and how are certificates renewed?
  - (d) Is seed and demo data loaded automatically on first boot, or by the manual `psql` command?
  - (e) How does PostgreSQL data persist across container re-creation?
- **Why It Matters:**
  - Infrastructure scaffolding (Task 10) and deployment need one definite layout.
  - The spec's statements overlap or differ: three compose files, two env templates, and automatic vs manual seeding.
  - This is CONFLICT C-05 in `docs/architecture/ARCHITECTURE_INDEX.md`.
- **Affected Area:** Deployment architecture; `infra/`; scripts; fixtures; testing environments.
- **Source / Evidence:**
  - §43: `infra/` compose files, `nginx.conf`, `.env.example`, root `docker-compose.yml` and `.env.example` (lines 1841–1844, 1857–1858).
  - §41: `infra/.env.example` is the contract file (line 1732).
  - §45: `cp infra/.env.example .env`, `docker compose up -d`, manual `psql` seeding (lines 1901–1906). The services table has no certbot service (lines 1887–1894).
  - §10: "starts all services from `.env.example`" and "Demo fixture data loads on first boot" (lines 496, 499).
  - §28: TLS "HTTPS only in staging and production | Nginx + certbot" (line 1347).
  - §7 (line 314) and §42 item 28 (line 1786): cloud VM. §10: staging deployment (line 434).
  - §36: Experiment 2 (line 1607). FR-16 (line 1294).
- **Blocking Status:** BLOCKING: infrastructure scaffolding (§42 item 3) and the staging and cloud deployments (items 27–28).

### RD-023 — Source of truth for `contracts/openapi.yaml`

- **Resolution (2026-10-03):** RESOLVED by ADR-0022 in `docs/architecture/DECISION_LOG.md`. `contracts/openapi.yaml` is hand-authored in OpenAPI 3.1 and authoritative, and drift is detected by contract tests (departure D-11). The conventions for Task 8 are recorded.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-023
- **Question:** Once implementation exists, is `contracts/openapi.yaml` a hand-authored contract that code must conform to, or a specification generated from code and exported over the file? How is drift between the agreed contract and the implementation detected?
- **Why It Matters:**
  - Contract-first development, the Shared Contract Change Rule, and mock conformance all depend on knowing which artifact is authoritative.
  - A generated export could silently overwrite the agreed contract.
  - This is CONFLICT C-06 in `docs/architecture/ARCHITECTURE_INDEX.md`.
- **Affected Area:** API contracts (Task 8); backend (generation tooling); frontend mocks; QA contract tests; CI.
- **Source / Evidence:**
  - P5: "Every interface defined in writing before implementation" (line 244).
  - §42 item 2: "OpenAPI skeleton" in the setup weeks (line 1748).
  - §41: the backend produces `openapi.yaml`, frontend review, the change rule with version increments (lines 1725, 1734–1739).
  - §44: mocks must match production contracts exactly (line 1877).
  - §10: "OpenAPI spec generated and exported to `contracts/openapi.yaml`" (line 475).
- **Blocking Status:** BLOCKING: Task 8 (contract authoring approach) and setting up contract tests.

### RD-024 — Backend–ML integration and forecast exposure

- **Resolution (2026-10-03):** RESOLVED by ADR-0030 in `docs/architecture/DECISION_LOG.md`. ML reads a daily-series view and writes `forecasts` through a restricted database role. The backend reads forecasts through the price module and never calls ML synchronously.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-024
- **Question:**
  - (a) Does the backend read forecasts from the PostgreSQL `forecasts` table, or by calling the ML service?
  - (b) Does ML read its input directly from PostgreSQL, or does the backend supply it?
  - (c) Which API exposes `ForecastResponse` to the frontend? §30 has none.
  - (d) How does the ML container access the database (credentials, ownership of the `forecasts` table)?
- **Why It Matters:**
  - The answer determines whether a runtime dependency on the ML service exists, how ML failure is contained (`available: false`, no 5xx), and whether `agrinex-ml` must be reachable.
  - This is CONFLICT C-03 in `docs/architecture/ARCHITECTURE_INDEX.md`.
- **Affected Area:** ML architecture; container and deployment architecture; resilience; Task 7 (`forecasts`); Task 8 (forecast endpoint).
- **Source / Evidence:**
  - §12: ML "Reads from PG, writes forecasts back"; ML → PG; dashed backend → ML "reads forecasts" (lines 549, 572–573).
  - §14: dashed PG → ML → PG → backend (lines 668–670).
  - §35: input "(from backend/DB)", output "(written to `forecasts` table)", failure behavior (lines 1543–1548, 1552, 1564).
  - §43: `ml/api` "FastAPI server (if serving forecasts)" (line 1823).
  - §29: `ForecastResponse` (line 1373).
  - §30: no forecast endpoint (lines 1384–1402).
  - §41: forecast-output contract (line 1728).
- **Blocking Status:** NON-BLOCKING for Phase 1. Must be resolved before ML integration, which HARD RULE 2 and the §35 criteria place after the deterministic engine is complete and deployed.
- **Status update (2026-10-03, Task 7):**
  - An optional `forecasts` table is defined in `docs/database/DATA_DICTIONARY.md`: the §35 output fields, one row per forecast point, and no reference from any core table.
  - Its owner is proposed as the price module, but that remains part of (d).
  - The §35 output has no variety, although the input has `varietyId`. The gap is noted here and under RD-015.

### RD-026 — Demo price data vs the no-fabrication rule

- **Resolution (2026-10-03):** RESOLVED by ADR-0028 in `docs/architecture/DECISION_LOG.md`. Demo data is real observations only. "Adjust" means choosing real scenarios, never altering data.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-026
- **Question:** When the spec says to "adjust the demo data" so that rankings disagree, what may be adjusted? Is it limited to choosing real observations and demo scenarios (location, quantity, distance setting), or may price or market data be altered?
- **Why It Matters:**
  - The spec forbids inventing or synthesizing price data. Its demo instructions ask for data adjustments without saying which.
  - Altered prices shown as government data would breach P10 and the claims discipline (§49).
  - This is CONFLICT C-07 in `docs/architecture/ARCHITECTURE_INDEX.md`.
  - Unit-test fixtures are not affected: they are test inputs, not data presented as real.
- **Affected Area:** Product integrity; `fixtures/demo_price_observations.sql`; demo preparation; QA.
- **Source / Evidence:**
  - P10: "Never invent or synthesize price data" (line 249). §31 RULE 5 (lines 1420–1421).
  - §21 note: "Adjust data for demo to show disagreement — e.g. shorter distance for A" (line 1082).
  - §46: "Adjust the demo data to ensure rankings disagree for impact" (line 1958).
  - §43: `demo_price_observations.sql` "Demo-ready price snapshot" (line 1848).
  - §37: "fixture data pre-loaded" (line 1647).
- **Blocking Status:** BLOCKING: creating demo fixtures and preparing the demo only. Other work is not blocked.

### RD-027 — Price unit and numeric representation of government prices

- **Resolution (2026-10-03):** RESOLVED by ADR-0008 in `docs/architecture/DECISION_LOG.md`. The unit is verified per commodity from official Agmarknet evidence (`Rs./Quintal`) and copied onto every observation. No conversion. Exact decimal parsing with half-up rounding to 2 dp. Validation rules V1–V8 reject unit-inconsistent records.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-027
- **Question:**
  - (a) What is the verified unit of `Min_Price`/`min_price`, `Max_Price`/`max_price` and `Modal_Price`/`modal_price` in each dataset, and which official documentation verifies it?
  - (b) Is the unit the same for every commodity and market? If it can vary (for example by commodity), where does ingestion get the unit of each row?
  - (c) What value is written to `price_observations.price_unit`, and what happens while the unit cannot be verified?
  - (d) How are source values with more than two decimal places stored: rounded to two decimals (by which rule), kept at full precision, or rejected?
- **Why It Matters:**
  - The spec forbids silent unit assumptions and requires verification before any monetary calculation. Gross value, net realization, comparisons and price-threshold alerts all depend on the unit.
  - The payload has no unit field, and official verification was unavailable on 2026-10-03.
  - The spec's examples assume rupees per quintal, but the captures show the same commodity on the same day about 100-fold apart between markets: `Onion Green` 4/12/20 (min/modal/max) at `APMC Pune` and 400/550/700 at `APMC Mumbai`; `Methi(Leaves)` 10/13/15 at `Pune(Moshi)` and 2000/4000/6000 at `APMC Hingna`. A single per-quintal unit cannot be assumed.
  - Raw price literals carry floating-point artifacts (`3963.4099999999999`, `4144.0699999999997`, `3210.1100000000001`). The rounding rule decides the stored values.
- **Affected Area:** Database / data (`price_observations.price_unit`, price precision); ingestion normalization; recommendation (FR-08, FR-09); alerts; trends; optional ML; UI price labels.
- **Source / Evidence:**
  - §1: "Price fields need unit verification before any monetary calculation" (line 100). §31 RULE 3 (lines 1414–1415). FR-05 (line 1283). §10: "units verified" (line 458).
  - Per-quintal framing: §2 P1 (line 120), §21 step 12 (line 1072), §32 (line 1444), §36 Experiment 3 "MAE (₹/quintal)" (line 1618).
  - Evidence §0.8 ("does not by itself establish … the exact monetary unit"); Appendix A captures C-314-P0 and C-19-MH-ONION (magnitudes), H-07SEP-2026 and C-647-P0 (decimal literals).
  - Online check 2026-10-03: the data.gov.in resource and catalog pages returned HTTP 403 (LIVE EXTERNAL VERIFICATION UNAVAILABLE).
  - `docs/database/DATABASE_ARCHITECTURE.md` §10.5; CONFLICT DB-C-5 (`docs/database/DATABASE_INDEX.md` §9).
- **Blocking Status:** BLOCKING: writing `price_unit` values during ingestion; any monetary calculation (gross value, net realization) and price-threshold alert evaluation on real data; demo fixtures that show prices. The schema structure is not blocked.

### RD-028 — Source records that share an observation key but differ in price

- **Resolution (2026-10-03):** RESOLVED by ADR-0009 in `docs/architecture/DECISION_LOG.md`. Same-key records with different prices in one delivery are quarantined: none stored, every variant logged as CONFLICT, and counted in `records_conflicted`.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-028
- **Question:** When one response or run contains two or more records with the same source dataset, market, commodity, variety, grade and date but different prices, what does ingestion store, and how are the records counted and logged? Options include keeping the first, keeping the last, rejecting all of them, or extending the identity (which would change the §14 business key).
- **Why It Matters:**
  - The §14 business key cannot hold both records, so one must be dropped or both rejected. Choosing by source order is not deterministic, because page order is not guaranteed.
  - PostgreSQL raises a cardinality violation when one `INSERT … ON CONFLICT DO UPDATE` statement would affect the same row twice, so a batched upsert of such a page fails entirely.
  - RD-005 covers a conflict with an already stored row, not conflicting records arriving together.
  - The rule changes the audit counters, the admin health figures and Experiment 2's duplicate rate.
- **Affected Area:** Database / data (ingestion load and deduplication, `price_observations`, `ingestion_runs` counters, `ingestion_errors`, possibly a new `error_type`); evaluation.
- **Source / Evidence:**
  - §14 business key and load (lines 650–651); §15 (lines 689–697); §27 (line 1327); §31 RULE 6 (line 1424); §47 Q8 (line 2035).
  - Evidence Appendix A.2 (H-07SEP-2026, all Onion / Unhali / Local / 07/09/2026): `APMC Manmad` 1000/4200/4545 and 1330/4400/4720; `APMC Yeola` 801/4500/5000 and 801/4350/4850; `Pimpalgaon Baswant(Saykheda)` 2000/4101/4611 and 200/4150/4611; `APMC Nampur` 700/4600/5205 and 715/4600/5250.
  - PostgreSQL 16 `INSERT` documentation (fetched 2026-10-03).
  - CONFLICT DB-C-2 (`docs/database/DATABASE_INDEX.md` §9). Related: RD-005.
- **Blocking Status:** BLOCKING: implementing the ingestion load step (§42 items 5–6).

### RD-029 — Ingestion completeness metric when the source repeats observations

- **Resolution (2026-10-03):** RESOLVED by ADR-0012 in `docs/architecture/DECISION_LOG.md`. Completeness = (inserted + updated + duplicates) ÷ (fetched − out of scope). An accounting invariant is enforced by CHECK, and every row-backed counter can be checked against database rows.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-029
- **Question:** How is §48's "Ingestion completeness" ("Count records fetched vs count in price_observations", at least 95% per run) computed?
  - Which counters form the numerator and the denominator?
  - How are legitimately collapsed records counted: exact re-deliveries, alias twins, same-key conflicts (RD-028)?
  - How are records outside the run's scope counted?
  - Does "count in price_observations" mean the rows the run inserted, the rows it inserted or updated, or the table total?
- **Why It Matters:**
  - In H-01AUG-2026-P0, a 100-record page, 47 candidate groups of records are the same observation under two market spellings. With verified alias mappings, correct deduplication stores far fewer rows than were fetched, so the criterion as worded could fail on correct behavior.
  - The repeated-ingestion test (§48: 0 new rows on the second run) conflicts with any reading based on rows inserted per run.
  - Admin health must match database counts exactly (§48), and Experiment 2 reports duplicate and validation-error rates (§36).
- **Affected Area:** QA acceptance; admin health API; `ingestion_runs` counters; Experiment 2.
- **Source / Evidence:**
  - §48 (lines 2097–2098, 2104); §36 Experiment 2 (lines 1605–1612); §20 `INGESTION_RUNS` counters.
  - Evidence Appendix A.3 (H-01AUG-2026-P0) and A.7 (C-19-MH-ONION: three `Onion Green` rows in an onion candidate set).
  - `docs/database/DATABASE_ARCHITECTURE.md` §11.4; CONFLICT DB-C-3.
- **Blocking Status:** BLOCKING: defining the §48 completeness test, the admin health completeness figure, and the Experiment 2 metrics.

### RD-030 — Scope of market slug uniqueness

- **Resolution (2026-10-03):** RESOLVED by ADR-0014 in `docs/architecture/DECISION_LOG.md`. Slugs stay globally unique, and collisions are disambiguated by district slug, then state code.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-030
- **Question:** Is `markets.canonical_slug` unique across all markets, as §20's UK implies, with canonical names disambiguated where two markets share a name? Or is it unique only within a district? Which naming convention disambiguates same-named markets?
- **Why It Matters:**
  - §47 Q4 derives the slug from the name (lowercase, spaces to hyphens).
  - The evidence shows the same market name in two states: `Sirsa APMC` in Haryana (district Sirsa) and in Uttar Pradesh (district Prayagraj). A global unique slug forces distinct canonical names for such markets.
  - No collision appears among the Maharashtra captures, but the database must support more states without schema changes (§27, §38, §47 Q20).
- **Affected Area:** Database / data (`uq_markets_canonical_slug`, market curation); any API that exposes slugs (Task 8).
- **Source / Evidence:**
  - §20 `MARKETS.canonical_slug` UK (line 875); §47 Q4 (line 2023); §27 (line 1316); §47 Q20 (line 2071).
  - Evidence Appendix A.6 (C-647-P0), the two `Sirsa APMC` records.
  - CONFLICT DB-C-4.
- **Blocking Status:** NON-BLOCKING for Phase 1 Maharashtra. It becomes blocking if a Maharashtra collision is found during market curation, or before a second state is added.

### RD-031 — Reference values the specification names but does not define

- **Resolution (2026-10-03):** RESOLVED by ADR-0006 in `docs/architecture/DECISION_LOG.md`. `states.code` is ISO 3166-2 (`IN-MH`). Dataset types are `CURRENT_PRICE` and `HISTORICAL_PRICE`. The category is the official Agmarknet commodity group.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-031
- **Question:**
  - (a) Which code scheme does `states.code` use?
  - (b) What is the `dataset_type` of the historical dataset? §45 gives only `CURRENT_PRICE`. Is the same value accepted by the manual ingestion trigger?
  - (c) What is the vocabulary of `commodities.category`, and who assigns categories?
- **Why It Matters:**
  - §20 defines these columns but gives no values, so seeding them would invent values.
  - `dataset_type` is part of the trigger request (§45) and therefore of the API contract.
- **Affected Area:** Database / data (reference seed, `source_datasets`); API contracts (trigger request, Task 8); commodity API.
- **Source / Evidence:**
  - §20 `STATES.code` UK (line 856), `SOURCE_DATASETS.dataset_type` (line 918), `COMMODITIES.category` (line 886).
  - §45 trigger body `{"datasetType":"CURRENT_PRICE"}` (line 1925); §42 item 7 (line 1755).
  - `docs/database/SEED_AND_REFERENCE_DATA.md` §3.1, §4.3, §7.
- **Blocking Status:** BLOCKING: seeding `states.code`, the historical `source_datasets` row and commodity categories; defining the trigger request values in Task 8.

### RD-032 — Locality reference data

- **Resolution (2026-10-03):** RESOLVED by ADR-0014 in `docs/architecture/DECISION_LOG.md`. Localities are LGD sub-districts (talukas) without coordinates. The origin is always the district centroid.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-032
- **Question:**
  - (a) What is the authoritative source of the localities offered in the location wizard (F1.3)?
  - (b) What is a locality: taluka, village, or something else?
  - (c) Do locality coordinates play any role, given that FR-06 and §21 use the district centroid as the origin?
- **Why It Matters:**
  - F1.3 and §25 require a Locality step, and §20 `LOCALITIES` has coordinates. No source or granularity is defined, so seeding would invent data.
  - Whether locality coordinates are used affects the meaning of the origin and the PII minimization rule (§28).
- **Affected Area:** Database / data (geography reference data); location wizard; geography API (`LocalityResponse`); distance origin.
- **Source / Evidence:**
  - F1.3 (line 326); §25 (line 1244); §20 `LOCALITIES` (lines 865–871).
  - FR-06 (line 1284); §21 step 3 (line 1063); §28 PII minimization (line 1345); §30 `LocalityResponse` (line 1387).
  - §42 item 7 lists "states, districts, commodities, known markets" but no localities (line 1755).
- **Blocking Status:** BLOCKING: seeding localities and implementing the wizard's locality step (§42 items 7, 18).

### RD-033 — Source of Marathi and Hindi reference names

- **Resolution (2026-10-03):** RESOLVED by ADR-0015 in `docs/architecture/DECISION_LOG.md`. Curated, provenance-recorded translations for states and commodities, with an English fallback and no machine translation. Other official names are shown as published.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-033
- **Question:**
  - Who provides `name_hi` and `name_mr` for states and commodities, and how are they reviewed?
  - Must every commodity shown to farmers have both before it is displayed, and what is shown otherwise?
  - Do district, market, variety and grade names, which §20 gives only once, need translations on farmer screens?
- **Why It Matters:**
  - Farmer-facing screens must be available in Marathi, Hindi and English (F1.2, §27), and §48 tests the language switch.
  - Translations are reference data and need an identified source (CLAUDE.md §C).
  - The commodity list is data-driven, so new commodities can arrive without localized names.
- **Affected Area:** Database / data (reference data); commodity and geography APIs; localization; UX.
- **Source / Evidence:**
  - §20 `STATES` (lines 854–855) and `COMMODITIES` (lines 884–885) `name_hi`, `name_mr`.
  - F1.2 (line 325); §27 localization (line 1321); §48 language criterion (line 2106); CLAUDE.md §C.
- **Blocking Status:** BLOCKING: seeding `name_hi` and `name_mr`; signing off the §48 language criterion for data-driven labels.

### RD-034 — User account identifiers and registration fields

- **Resolution (2026-10-03):** RESOLVED by ADR-0016 in `docs/architecture/DECISION_LOG.md`. FARMER logs in by E.164 phone, ADMIN by lower-case email, with one `identifier` login field. Password length limits are set per role. Refresh-token rotation with reuse detection. Admins own alerts through a farmer profile created on demand.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-034
- **Question:**
  - (a) Which identifiers does each role need? For example, phone for farmers (§25 quick phone registration), email for admins (§46 shows an email login), both for buyers?
  - (b) What is the login identifier?
  - (c) What formats and normalization apply: phone number format, email case-insensitivity?
  - (d) Does quick phone registration set a password (`password_hash` is in §20), or use another factor?
  - (e) May ADMIN users own price alerts? §28 lets ADMIN create alerts, but `price_alerts.farmer_profile_id` is required (§20) and admins have no farmer profile.
- **Why It Matters:**
  - These answers fix the nullability and uniqueness semantics of `users` and the auth API contract.
  - Authentication is security-sensitive and needs explicit human review (CLAUDE.md rule 16).
  - The test-accounts fixture depends on them.
- **Affected Area:** Security; database / data (`users`, `farmer_profiles`, `price_alerts`); auth module; Task 8 auth endpoints; fixtures.
- **Source / Evidence:**
  - §20 `USERS` (lines 939–946), `FARMER_PROFILES` (lines 947–953), `PRICE_ALERTS.farmer_profile_id` (line 1009).
  - §25 quick phone registration (line 1266); §28 role capabilities (lines 1350–1362, "Create price alert" line 1355); §46 admin login (line 1990; credentials not repeated); §10 JWT auth (line 465); §43 `test_accounts.sql` (line 1849).
  - CONFLICT DB-C-8 (`docs/database/DATABASE_INDEX.md` §9).
- **Blocking Status:** BLOCKING: implementing registration and login (§42 item 8), the auth contract (Task 8), and the test-accounts fixture. Requires explicit security review.

### RD-035 — Data retention and deletion policy

- **Resolution (2026-10-03):** RESOLVED by ADR-0029 in `docs/architecture/DECISION_LOG.md`. Retention periods per table, account erasure through CASCADE and SET NULL, and daily purge jobs.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-035
- **Question:**
  - How long are these kept: price observations, ingestion runs and errors (including raw payloads), selling searches, recommendation runs and options, alert events, expired refresh tokens and optional forecasts?
  - May accounts and farmer profiles be hard-deleted? If so, what happens to their searches and alerts: deleted, anonymized, or kept?
- **Why It Matters:**
  - The spec defines deletion only for alerts (§34) and account deactivation (`is_active`, §20). It sets no retention periods.
  - Searches and profiles hold personal data (§28). Experiment 1 needs historical recommendation runs (§36). Raw payloads grow without bound.
  - The schema's `ON DELETE RESTRICT` choices are placeholders until this is decided.
- **Affected Area:** Database / data (lifecycle of every table, foreign-key delete actions, purge jobs); security and privacy.
- **Source / Evidence:**
  - §34 alert lifecycle (lines 1505–1510); §20 `USERS.is_active` (line 945); §28 PII minimization (line 1345); §36 Experiment 1 (line 1595); §10 historical bootstrap "≥60 days" (line 457).
  - `docs/database/DATABASE_ARCHITECTURE.md` §19.
- **Blocking Status:** NON-BLOCKING: Phase 1 implements no purge or account deletion, and the schema uses `RESTRICT` and deactivation until this is decided. It becomes blocking for any purge job or account-deletion feature.

### RD-036 — Reference time zone for date-based rules

- **Resolution (2026-10-03):** RESOLVED by ADR-0007 in `docs/architecture/DECISION_LOG.md`. `Asia/Kolkata` (IST) defines "today" and every date window.
- **Resolved by:** the lead architect, under the delegation the project owner recorded in ADR-0001 (2026-10-03).

- **ID:** RD-036
- **Question:** In which time zone is "today" computed for freshness classification (FRESH, RECENT, STALE, UNAVAILABLE), trend windows (last 7, 14 and 30 days) and alert windows? Is `report_date` treated as an Indian calendar date?
- **Why It Matters:**
  - `report_date` is a calendar date from Indian markets, and servers may run in UTC. Between 00:00 and 05:30 IST the UTC date is still the previous day, so a freshness status computed in UTC shifts by one day for those hours.
  - §48 requires 100% freshness-badge accuracy against `report_date`, and deterministic tests need one rule.
- **Affected Area:** Price module (freshness, trends); recommendation (freshness score); alert evaluation; UI badges; database session time-zone configuration.
- **Source / Evidence:**
  - §16 freshness node: "report_date >= today - fresh_days" (line 737); §33 windows (lines 1471–1480); §48 "Freshness accuracy" (line 2099).
  - Evidence §0.7 and §20.5: application freshness is derived from "now - arrival_date / project policy".
- **Blocking Status:** BLOCKING: implementing freshness classification, trend windows and alert evaluation windows (§42 items 10, 12–15, 24), and the §48 freshness test.

### RD-037 — Hosting procurement: cloud VM, region, size and domain name

- **Resolution (2026-10-18):** RESOLVED by ADR-0055 in `docs/architecture/DECISION_LOG.md`. One Oracle Cloud Always Free VM (VM.Standard.A1.Flex, 2 OCPU, 12 GB, Ubuntu 24.04, home region India West (Mumbai) or India South (Hyderabad)), the domain `agrinex.managio.me`, and a curated copy of the local database as the production starting data. Zero cost; the free tier's limitations are accepted (ADR-0055 item 4).
- **Resolved by:** the Human Project Owner, 2026-10-18 (hosting "Oracle Always Free VM", address "agrinex.managio.me", production data "Curated copy").
- **ID:** RD-037
- **Question:** Which cloud provider, region and VM size host the public deployment, and which domain name does it use?
- **Why It Matters:**
  - ADR-0021 decides the topology: one cloud VM, Nginx with certbot, and Compose.
  - Running it needs a paid account, a VM and a domain. TLS certificates need the domain (§28).
  - Procurement and budget are human decisions, outside the delegation in ADR-0001.
- **Affected Area:** Operations / deployment; TLS; Experiment 2 (14 days of ingestion on the public deployment).
- **Source / Evidence:** §7 ("Deployed to a cloud VM and publicly accessible", line 314); §28 TLS (line 1347); §42 items 27–28; ADR-0021.
- **Status update (2026-10-04, Task 9 final):** ADR-0048 sends account emails (verification, password reset) through Resend from a sending domain verified on the project domain, so production email also waits for the domain. Local development and tests are not affected (non-delivering email adapter).
- **Blocking Status:** BLOCKING: the staging/public deployment only (§42 items 27–28). Not blocking for Task 8, scaffolding or local development.

### RD-038 — Module path for cross-module data in public responses

- **Resolution (2026-10-04):** RESOLVED by ADR-0035 in `docs/architecture/DECISION_LOG.md`. Option 1: read-only module ports. Comparisons pass their pinned freshness thresholds to price.
- **Resolved by:** the Human Project Owner, in the conversation of 2026-10-04 ("TASK 8.6 — FINAL DECISION LOCK + SECURITY SIGN-OFF + SKILL POPULATION + PROJECT READINESS").

- **ID:** RD-038
- **Question:** How do the price, recommendation, alert and commodity modules obtain data that binding entries require them to return, when another module owns it and ADR-0013 approves no dependency path for it?
  - (a) **Source synchronization status.** ADR-0022 item 9 requires `lastSuccessfulSyncAt` and whether the latest current-dataset run failed in every response that serves price data. The data lives in `ingestion_runs` (ingestion module). The serving modules are price (market detail, trends), recommendation (compare, simulate) and alert (events).
  - (b) **Freshness thresholds.** `freshnessStatus` on every price (ADR-0022 item 9) and the stale window of ADR-0010 come from a `recommendation_weights` version (recommendation module; `docs/database/DATABASE_ARCHITECTURE.md` §10.6). ADR-0010 item 2 uses `freshness_stale_days` inside the selection, so the thresholds decide which observation is selected, not only its label. A comparison records its version in `recommendation_runs.weights_id`, and a simulation reuses it (ADR-0026 item 3). Price and alert serve prices directly, with the active version.
  - (c) **Commodity availability.** The farmer commodity selector lists commodities with recent observations in coverage (ADR-0006 consequences). `GET /api/v1/commodities` belongs to the commodity module (spec §30), a leaf that cannot read observations or the stale window.
- **Why It Matters:**
  - Price → ingestion and price → recommendation would close cycles with the approved ingestion → price and recommendation → price. Alert → ingestion and commodity → price would also close cycles, with the approved ingestion → alert and price → commodity (ADR-0013 item 3). Alert → recommendation and recommendation → ingestion would be acyclic but are not approved (`docs/database/TABLE_OWNERSHIP.md` §4–§5: price "never reads ingestion tables").
  - Comparisons must classify freshness with their own weights version. If price fetched the active thresholds itself, an admin update during a request would make `selling_options.freshness_status` and the stale window disagree with `recommendation_runs.weights_id`, breaking the Experiment 1 audit trail (Task 8 data review).
  - Choosing a path is an architecture decision ("No other module-to-module dependency exists. Adding one needs a new decision", `docs/architecture/COMPONENT_ARCHITECTURE.md` §4.1; CLAUDE.md §G).
  - The delegation in ADR-0001 does not extend to this session.
- **Affected Area:** Architecture (module boundaries); backend price, recommendation, alert, commodity and ingestion modules. The contract shape (`contracts/openapi.yaml`) is not affected: the fields and their meaning are fixed.
- **Source / Evidence:**
  - ADR-0006 (consequences), ADR-0010, ADR-0013 item 3, ADR-0022 item 9.
  - `docs/database/TABLE_OWNERSHIP.md` §4, §5; `docs/database/DATABASE_ARCHITECTURE.md` §10.6; `docs/architecture/COMPONENT_ARCHITECTURE.md` §4.1.
  - `contracts/openapi.yaml`: `SourceSyncStatus`, `freshnessStatus`, `listCommodities`.
- **Options:**
  - read-only ports declared by the consuming module and implemented by the owner over existing edges (recommended in PROPOSED ADR-0035);
  - a push model with state in the price module;
  - new direct dependencies where no cycle results (only recommendation → ingestion and alert → recommendation qualify, so the price, alert-status and commodity cases stay unsolved);
  - moving ownership (contradicts spec §20 for the thresholds).
- **Blocking Status:**
  - NON-BLOCKING for the API contract (Task 8).
  - BLOCKING for implementing (a) `sourceStatus` in price, recommendation and alert responses, (b) freshness classification and stale-window selection in the price and alert modules, and (c) the availability filter of `listCommodities`, until a human approves ADR-0035 or another resolution.
  - Requires explicit human approval (ADR-0001 delegation not applicable).

### RD-039 — Earth radius for haversine distances

- **Resolution (2026-10-04):** RESOLVED by ADR-0036 in `docs/architecture/DECISION_LOG.md`. Option 1: Earth radius 6 371.0088 km, with shortest-decimal conversion and half-up rounding to 2 decimal places.
- **Resolved by:** the Human Project Owner, in the conversation of 2026-10-04 ("TASK 8.6 — FINAL DECISION LOCK + SECURITY SIGN-OFF + SKILL POPULATION + PROJECT READINESS").

- **ID:** RD-039
- **Question:** Which Earth radius does the transport module's haversine distance use?
- **Why It Matters:**
  - The spec requires haversine distance (FR-06, §16, §21, §32) but no authoritative source names the radius.
  - `distanceKm` drives the transport cost, the radius filter, the `SUITABLE_DISTANCE` reason code (< 50 km) and ranking ties.
  - §48 requires the hand-computed fixtures to match 100 %, so server and fixtures must use the same constant.
- **Affected Area:** Recommendation and transport modules; QA fixtures. The contract is not affected: only the server computes distances.
- **Source / Evidence:**
  - Spec FR-06, §16, §21, §32, §47 Q6, §48; `docs/architecture/RECOMMENDATION_ENGINE.md` §4.4.
  - `docs/database/DATABASE_ARCHITECTURE.md` A-DB-02 (WGS84 coordinates).
  - No radius appears in any project document (searched 2026-10-04).
- **Options:**
  - WGS 84 / GRS 80 mean radius 6 371.0088 km (recommended in PROPOSED ADR-0036);
  - 6 371 km;
  - the equatorial radius 6 378.137 km;
  - an ellipsoidal distance (departs from "haversine").
- **Blocking Status:**
  - NON-BLOCKING for the API contract (Task 8).
  - BLOCKING for finalizing the transport distance function and its §48 fixtures, until a human approves ADR-0036 or another value.

### RD-040 — Account-erasure mechanism

- **Resolution (2026-10-04):** RESOLVED by ADR-0037 in `docs/architecture/DECISION_LOG.md`. Option C: an in-application operator procedure without HTTP. The admin module erases through auth, audited as `ACCOUNT_ERASED` with a named ADMIN actor. No erasure endpoint exists.
- **Resolved by:** the Human Project Owner, in the conversation of 2026-10-04 ("TASK 8.6 — FINAL DECISION LOCK + SECURITY SIGN-OFF + SKILL POPULATION + PROJECT READINESS").

- **ID:** RD-040
- **Question:** Is the audited account-erasure procedure of ADR-0029 an authenticated admin API operation, or an operations-runbook procedure outside the API? If it is an API operation, which inputs and safeguards does it need?
- **Why It Matters:**
  - ADR-0029 item 2 decides that erasure is "an audited admin procedure", "documented in the operations runbook", and that no self-service endpoint is added.
  - ADR-0020 item 1 lists the audited admin endpoints and does not include erasure.
  - The closed audit vocabulary includes `ACCOUNT_ERASED`, and `docs/database/TABLE_OWNERSHIP.md` §4 approves admin → auth for "the audited account-erasure procedure". Both readings are possible.
  - Deleting accounts through HTTP is security-sensitive (CLAUDE.md rule 16): an ADMIN access token held in browser memory could be misused to erase farmers.
  - Phone numbers are unverified in Phase 1 (no OTP, ADR-0016), so erasure is also the remedy when someone registers another person's number.
  - The Task 8 security review flagged that putting the operation in the contract would decide the question. Contract 1.0.0 therefore leaves it out.
- **Affected Area:** Security; admin and auth modules; the contract (an optional additive operation); the operations runbook.
- **Source / Evidence:** ADR-0016, ADR-0020 item 1, ADR-0029 item 2; `docs/database/DATA_DICTIONARY.md` (`admin_audit_events.action`); `docs/database/TABLE_OWNERSHIP.md` §4; Task 8 security review (2026-10-04).
- **Options:**
  - (A) An admin operation, as drafted in PROPOSED ADR-0037: `POST /api/v1/admin/account-erasures` with a mobile number, a request reference and a reason; the number masked in the audit details; sessions ended at once.
  - (B) A runbook procedure run by an operator with database access, writing the `ACCOUNT_ERASED` audit row in the same transaction.
  - (C) An in-application operator procedure without HTTP: a one-off command mode of the backend run by an operator with server access. The admin module erases through auth's interface and writes `ACCOUNT_ERASED` with a named ADMIN as actor. Added in Task 8.5. The Task 8.5 decision packet recommends C, which revises the Task 8 draft. ADR-0037 would be redrafted to the chosen option before approval.
  - Decide together with RD-044 (deactivation).
- **Blocking Status:**
  - NON-BLOCKING for the API contract and for every Phase 1 flow.
  - BLOCKING for implementing account erasure, by either path, until a human decides.
  - Option A adds the operation in a minor contract version (spec §41).

### RD-041 — Transport-label wording when the rate is not ₹8/km

- **Resolution (2026-10-04):** RESOLVED by ADR-0038 in `docs/architecture/DECISION_LOG.md`. Option A: the label shows `appliedRatePerKm`. The departure from the literal "₹8/km" wording is authorized and recorded as D-14.
- **Resolved by:** the Human Project Owner, in the conversation of 2026-10-04 ("TASK 8.6 — FINAL DECISION LOCK + SECURITY SIGN-OFF + SKILL POPULATION + PROJECT READINESS").

- **ID:** RD-041
- **Question:** The mandatory transport label quotes a fixed rate, but the rate behind an estimate can differ. Should the label show the literal "₹8/km", the rate the estimate actually used, or both?
- **Why It Matters:**
  - §32 (line 1458) says the transport cost "must always show" the text "**Estimated** — based on ₹8/km assumption. Actual costs will vary." §49 (lines 2128, 2138) repeats "based on ₹8/km assumption".
  - The rate is not fixed. ADMIN updates the transport assumptions as new versions (§32 parameters; ADR-0020 item 1), and the What-If lets the farmer change the rate (§17; ADR-0026).
  - After such a change, the literal label would misstate the basis of the estimate it labels (spec P2 explainability; §49 claims discipline).
  - Changing the wording departs from the spec text, which only a human can authorize (CLAUDE.md §E).
- **Affected Area:** UX copy and the frontend (role file 02; Task 9). The contract is not affected: every comparison and simulation returns `transportAssumptions.appliedRatePerKm` and `ratePerKmSource`.
- **Source / Evidence:** Spec §17, §32 (line 1458), §49 (lines 2128, 2138); ADR-0020 item 1; ADR-0026; `contracts/recommendation.schema.json` (`TransportAssumptionsApplied`).
- **Options:**
  - (A) Show the applied rate: "Estimated — based on ₹<appliedRatePerKm>/km assumption. Actual costs will vary." It is identical to the spec text while the rate is 8 (recommended).
  - (B) Keep the literal text and never change the rate from 8. This makes ADR-0020's rate updates and the What-If rate slider unusable.
  - (C) Keep the literal text and add a second line with the applied rate.
- **Blocking Status:**
  - NON-BLOCKING for the API contract (Task 8).
  - BLOCKING for finalizing the transport-label copy in role file 02 and Task 9 screens, until a human decides.

### RD-042 — Server-side enforcement of district-centroid origins

- **Resolution (2026-10-04):** RESOLVED by ADR-0039 in `docs/architecture/DECISION_LOG.md`. Option A: the server accepts only stored district centroids (400 `VALIDATION_FAILED`, `INVALID_VALUE` otherwise), through the new recommendation → geography dependency. The request shape is unchanged.
- **Resolved by:** the Human Project Owner, in the conversation of 2026-10-04 ("TASK 8.6 — FINAL DECISION LOCK + SECURITY SIGN-OFF + SKILL POPULATION + PROJECT READINESS").

- **ID:** RD-042
- **Question:** Must the backend verify that a comparison's `originLat` and `originLon` are a district centroid, and if so, by which module path?
- **Why It Matters:**
  - §28 (line 1345) assigns "No home address collected; district centroid only" to "Frontend + Backend". ADR-0023 item 1 sends the origin as the selected district's centroid.
  - The contract accepts any in-range coordinates with up to 6 decimal places. The server stores them in `selling_searches.origin_lat` and `origin_lon`, which are PERSONAL data (`docs/database/DATA_DICTIONARY.md`) kept under ADR-0029, and `simulate` returns them to any caller that holds the `recommendationId`.
  - A modified client could therefore make the system store a precise home location.
  - The recommendation module has no approved dependency on geography (ADR-0013 item 3). Geography is a leaf, so a recommendation → geography edge would be acyclic, but adding one is an architecture decision (CLAUDE.md §G).
  - Per-user data protection is security-sensitive (CLAUDE.md rule 16).
- **Affected Area:** Security and privacy; the recommendation and geography modules; possibly the compare request (option B).
- **Source / Evidence:** Spec §28 (line 1345), FR-06, §47 Q6; ADR-0013 item 3; ADR-0023 item 1; ADR-0029; `docs/database/DATA_DICTIONARY.md` (`selling_searches.origin_lat`, `origin_lon`); Task 8 data review (2026-10-04).
- **Options:**
  - (A) Server check: the origin must equal a stored district centroid at 6 decimal places, otherwise 400 VALIDATION_FAILED. Needs a new recommendation → geography edge (acyclic) or a read-only port (recommended, with the edge).
  - (B) Send `originDistrictId` and let the server look up the centroid. This changes ADR-0023 item 1 and the request shape.
  - (C) Accept the residual risk: only the frontend enforces centroids, and the backend stores what it receives. Record the acceptance in the security review.
- **Blocking Status:**
  - NON-BLOCKING for the API contract 1.0.0 shape under options A and C. Option B would revise the request before release.
  - BLOCKING for implementing compare's origin validation and for the security sign-off of the comparison flow, until a human decides.

### RD-043 — Estimates that exceed the stored precision

- **Resolution (2026-10-04):** RESOLVED by ADR-0040 in `docs/architecture/DECISION_LOG.md`. Option C: estimates beyond `numeric(14,2)` answer 422 `ESTIMATE_OUT_OF_RANGE` and nothing is recorded. No quantity cap, no column change.
- **Resolved by:** the Human Project Owner, in the conversation of 2026-10-04 ("TASK 8.6 — FINAL DECISION LOCK + SECURITY SIGN-OFF + SKILL POPULATION + PROJECT READINESS").

- **ID:** RD-043
- **Question:** How is an extreme but schema-valid quantity handled when its estimates do not fit the `selling_options` columns?
- **Why It Matters:**
  - ADR-0023 item 1 bounds the compare `quantity` only by > 0, "with no further limits". The contract therefore accepts up to 999 999 999.999 in either unit (`selling_searches.quantity` is numeric(12,3)).
  - `selling_options.estimated_gross_value`, `estimated_transport_cost` and `estimated_net_realization` are numeric(14,2), at most 999 999 999 999.99 (`docs/database/DATA_DICTIONARY.md`).
  - 999 999 999.999 QUINTAL at ₹3 000 per quintal gives about 3.0 × 10¹² rupees. Persisting it fails and surfaces as a 500, and no 4xx code describes the case.
  - Contract fuzzers send the declared maximum first.
- **Affected Area:** Recommendation module; the first Flyway migration of `selling_options`; the error catalogue.
- **Source / Evidence:** ADR-0023 items 1 and 4; `docs/database/DATA_DICTIONARY.md` (`selling_searches.quantity`, `selling_options.estimated_*`); `contracts/recommendation.schema.json` (`CompareRequest.quantity`); Task 8 data review (2026-10-04).
- **Options:**
  - (A) Cap `quantity` so every estimate fits. This amends ADR-0023 item 1 and changes the request schema.
  - (B) Widen the three estimate columns before the first migration, so every schema-valid request fits. A database-design change; no contract change. Recommended in Task 8, before the precision analysis.
  - (C) Keep both and answer such requests with a new data-dependent 422 code. A contract addition. Recommended by the Task 8.5 decision packet: covering every column-bounded input would need about 28-digit columns, and browsers lose paise precision above about 9 × 10¹³ rupees, while C keeps ADR-0023 and the column design unchanged.
- **Blocking Status:**
  - NON-BLOCKING for the API contract (Task 8).
  - BLOCKING for the first migration of `selling_options` and for the compare implementation's handling of extreme quantities, until a human decides.

### RD-044 — Account deactivation mechanism and audit

- **Resolution (2026-10-04):** RESOLVED by ADR-0041 in `docs/architecture/DECISION_LOG.md`. Option C: deactivation and reactivation are in-application operator procedures without HTTP, audited as `ACCOUNT_DEACTIVATED` and `ACCOUNT_REACTIVATED`.
- **Resolved by:** the Human Project Owner, in the conversation of 2026-10-04 ("TASK 8.6 — FINAL DECISION LOCK + SECURITY SIGN-OFF + SKILL POPULATION + PROJECT READINESS").

- **ID:** RD-044
- **Question:** Who deactivates and reactivates a user account (`users.is_active`), by which mechanism, and how is the change audited?
- **Why It Matters:**
  - §20 defines `users.is_active`, and ADR-0029 item 2 says admins "are deactivated, never deleted". No entry defines how deactivation happens.
  - The contract already enforces the flag: login answers 401, refresh refuses the token and revokes its family, and alert and admin operations answer 401.
  - The closed audit vocabulary (ADR-0020 item 6; `ck_admin_audit_events_action`) has no deactivation action. Today a compromised or departing admin account could be disabled only by unaudited manual SQL.
  - Account changes are security-sensitive (CLAUDE.md rule 16).
- **Affected Area:** Security; auth and admin modules; the audit vocabulary (database CHECK and the contract's `AdminAuditAction`); the operations runbook.
- **Source / Evidence:** Spec §20; ADR-0016, ADR-0020 item 6, ADR-0029 item 2; `docs/database/DATA_DICTIONARY.md` (`users.is_active`, `admin_audit_events.action`); `docs/database/INDEXES_AND_CONSTRAINTS.md`; Task 8.5 security review, finding SR-05.
- **Options:** the same family as RD-040.
  - (A) Admin HTTP operations.
  - (B) A database runbook.
  - (C) An in-application operator procedure.

  Options A and C need new audit actions (for example `ACCOUNT_DEACTIVATED` and `ACCOUNT_REACTIVATED`). The Task 8.5 decision packet recommends deciding this together with RD-040 and using the same mechanism.
- **Blocking Status:**
  - NON-BLOCKING for the API contract and for Task 9, unless option A adds admin screens.
  - BLOCKING for implementing account deactivation and reactivation, until a human decides.

### RD-045 — Winner-callout wording: "Highest Net Realization" vs the composite-score rank 1

- **Resolution (2026-10-04):** RESOLVED by ADR-0045 in `docs/architecture/DECISION_LOG.md`, decided under the delegation the Human Project Owner recorded in ADR-0043 item 4(a). Option A, completed: the composite rank 1 is labelled "Recommended"; "Highest estimated net realization" labels only options whose estimate is the highest; the winner banner states the trade-off without claiming that rank 1 leaves more money when it does not (departure D-16).
- **Resolved by:** the Human Project Owner, in the conversation of 2026-10-04 ("TASK 9 FINAL — HUMAN DECISION LOCK + CONTRACT/UX CONSISTENCY + FINAL READINESS GATE").

- **ID:** RD-045
- **Question:** What may the WinnerCalloutBanner call the rank-1 market, given that rank 1 is decided by the composite score and is not guaranteed to have the highest estimated net realization?
- **Why It Matters:**
  - The spec's banner copy names rank 1 by net realization: "Highest Net: Market A" (§25, line 1254) and "Highest Net Realization: APMC Nashik ← Recommended" (§46, line 1956).
  - But §16 defines it as "net_winner (rank 1 by composite score)" (line 745), confirmed by ADR-0023 item 2, and FR-10 makes net realization the *dominant*, not the only, factor (line 1288). Under the ADR-0024 weights (0.55 net, 0.15 price, 0.10 trend, 0.10 freshness, 0.10 transport penalty), a market whose estimated net is slightly lower but whose trend is RISING and data FRESH can outrank a market with the highest net whose trend is FALLING and data STALE.
  - In that case the spec's label would be false on screen, which breaks §49 claims discipline and P3. The same assumption appears in Experiment 1's `netDifference` (§36, line 1598), which can then be negative.
  - Task 9 drafts therefore use a label that is true in every case ("Recommended … about ₹188 more after estimated transport", with the rupee difference computed from the response).
- **Options:**
  - (A) Always label rank 1 "Recommended" (never "Highest net realization") and show its estimated net and the difference.
  - (B) Use the spec's label only when rank 1 also has the highest estimated net realization; otherwise "Recommended".
  - (C) Change the ranking so rank 1 always has the highest net (a product change to §16, outside design scope).
- **Affected Area:** Screen S04 and its copy (role file 02 §8, §10); frontend label tests; interpretation of Experiment 1 (§36).
- **Source / Evidence:** §16 (line 745); §25 (line 1254); §36 (lines 1596–1598); §46 (line 1956); §49; FR-10 (line 1288); ADR-0023 item 2; ADR-0024 items 1 and 5.
- **Blocking Status:** NON-BLOCKING for the Task 9 design exploration. BLOCKING for approving the winner-callout copy and screen S04 in role file 02.

### RD-046 — Source and review of Marathi and Hindi interface copy

- **Resolution (2026-10-04):** RESOLVED by ADR-0046 in `docs/architecture/DECISION_LOG.md`. Claude, the AI assistant maintaining this repository, owns the authoring and review of Marathi and Hindi interface copy, under the rules recorded there and in role file 02 §10.
- **Resolved by:** the Human Project Owner, in the conversation of 2026-10-04 ("TASK 9 FINAL — HUMAN DECISION LOCK + CONTRACT/UX CONSISTENCY + FINAL READINESS GATE").

- **ID:** RD-046
- **Question:** Who produces and reviews the Marathi and Hindi interface text (labels, messages, mandatory labels and the standard disclaimer), and how is the review recorded?
- **Why It Matters:**
  - Every farmer-facing screen must be in Marathi, Hindi and English, with 0 untranslated hardcoded strings (§27, line 1321; §48, line 2106; F1.2).
  - ADR-0015 item 4 says interface text is fully localized in the frontend but names no source or review. Its item 1 sets a review rule — "a team translation reviewed by a named native speaker" — for reference names only (Decision Log, line 832).
  - The mandatory labels and the disclaimer carry the product's claims discipline (§32, §49); a wrong translation could misstate an estimate as a fact or a guarantee.
  - Task 9 drafted Marathi and Hindi strings for design evaluation (role file 02 §10); they are unreviewed and must not ship. Only three Marathi reason phrases come from the spec (§46, line 1963).
- **Options:**
  - (A) Extend ADR-0015 item 1's rule to interface text: a team translation per language, reviewed by a named native speaker, recorded in role file 02.
  - (B) A professional translation with the same recorded review.
  - (C) Ship English only until reviewed (fails §48).
- **Affected Area:** Role file 02 §10 copy deck; frontend localization files; §48 acceptance.
- **Source / Evidence:** F1.2 (line 325); §27 (line 1321); §46 (line 1963); §48 (line 2106); §49; ADR-0015 items 1 and 4.
- **Blocking Status:** NON-BLOCKING for the Task 9 design exploration. BLOCKING for approving role file 02 §10 and signing off the §48 language criterion.

### RD-047 — Password recovery path for farmers in Phase 1

- **Resolution (2026-10-04):** RESOLVED by ADR-0047 (product decision) and ADR-0048 (mechanism) in `docs/architecture/DECISION_LOG.md`. Option C through email: registration includes an email address; login by mobile number or email with one password; Phase 1 password recovery by a single-use emailed link to the verified email; no SMS or OTP.
- **Resolved by:** the Human Project Owner, in the conversation of 2026-10-04 ("TASK 9 FINAL — HUMAN DECISION LOCK + CONTRACT/UX CONSISTENCY + FINAL READINESS GATE").

- **ID:** RD-047
- **Question:** What can a farmer do after forgetting their password, and what may the sign-in screen say about it?
- **Why It Matters:**
  - Alerts require a login, created by "Quick phone registration (lightweight)" (§25, line 1266); the alert-focused persona (Sunita, §6) has basic digital literacy, so forgotten passwords are likely.
  - Contract 1.0.0 has no recovery operation, and Phase 1 has no SMS or OTP (§39; ADR-0016 item 3, Decision Log line 869).
  - The only way to free a registered number is the operator erasure procedure (ADR-0037), which also deletes the farmer's alerts and needs a request channel that no document defines.
  - Without a decision the sign-in screen either dead-ends the farmer or invents a support path; Task 9 drafts therefore show no "forgot password" control.
- **Options:**
  - (A) No self-service recovery in Phase 1; the sign-in screen explains how to ask the team to remove the account and register again (alerts are lost). Needs a defined contact channel and a runbook entry.
  - (B) No recovery and no copy; record it as a known Phase 1 limitation (in the style of §47 Q22).
  - (C) Add a recovery feature — needs OTP (outside Phase 1, §39) or another verified channel: a product change.
- **Affected Area:** Screen S10 copy (role file 02 §8); operations runbook (not created yet); viva limitations (§47).
- **Source / Evidence:** §6; §25 (line 1266); §39; ADR-0016 item 3; ADR-0037; `contracts/openapi.yaml` (auth operations: register, login, refresh, logout only).
- **Blocking Status:** NON-BLOCKING for the Task 9 design exploration. BLOCKING for approving the copy of sign-in screen S10 in role file 02.

### RD-048 — Date format of the historical `Arrival_Date` request filter

- **Resolution (2026-10-15):** RESOLVED by ADR-0052 item 5 in `docs/architecture/DECISION_LOG.md`. ADR-0007 item 2 (`dd-MM-yyyy`) is confirmed by the live comparison, by 156 report dates each returning their reported total, by the 366-date backfill and by run `d417d32f` (204 dates, 536 pages, 0 failed). No change was needed.
- **Resolved by:** Claude under the Human Project Owner's delegation recorded in ADR-0050 (conversation of 2026-10-15, "AgriNex — Final Phase 1 Closure and Owner Decision Resolution").

- **ID:** RD-048
- **Question:** Does the historical resource's `filters[Arrival_Date]` filter take `dd-MM-yyyy` (ADR-0007 item 2) or `dd/MM/yyyy` (the verified request)?
- **Why It Matters:**
  - ADR-0007 item 2 sends the filter as `dd-MM-yyyy`, because the resource metadata declares that format. The backend implements the decision (`SourceSchema.DATE_FILTER_FORMAT`).
  - The verified evidence shows a successful request with `filters[Arrival_Date]=13/09/2026`, which returned `total=17`. ADR-0007's context cites the metadata but not this request.
  - If the API does not match `dd-MM-yyyy`, historical runs could return no records and still end SUCCESS, so the history behind trends (ADR-0025) would silently stay empty.
  - Choosing a format without a live request would be an invention.
- **Affected Area:** Ingestion (HISTORICAL_PRICE delivery units); trends; Experiment 2.
- **Source / Evidence:**
  - `docs/architecture/DECISION_LOG.md` ADR-0007 (context and item 2).
  - `docs/data/AGRINEX_VERIFIED_GOVERNMENT_API_DATA_REFERENCE.md` §2.3 (request pattern) and §2.4 (actual successful test, 13/09/2026).
  - Task 10 could not test it: no `DATA_GOV_API_KEY` was configured, and no request was sent to data.gov.in.
- **Blocking Status:** NON-BLOCKING for local development and tests. BLOCKING for relying on historical ingestion (trends, Experiment 2) until one live request per format is compared, with the API key, and the result is recorded in the decision log.
- **Evidence (Task 12, 2026-10-13):** the live comparison was made with the project key. `filters[Arrival_Date]=07-09-2026` (ADR-0007 item 2, what the backend sends) and `07/09/2026` both returned the same 71 Onion records for Maharashtra; `2026-09-07` and `7/9/2026` did too. ADR-0007 item 2 is therefore confirmed and no change is needed. Details: `docs/data/AGRINEX_VERIFIED_GOVERNMENT_API_DATA_REFERENCE.md` §22.2. Recording the result in the decision log and closing this item are for the Human Project Owner.
- **Further evidence (Task 12, 2026-10-15):** 156 historical report dates requested with `dd-MM-yyyy` each returned exactly their reported `total`, and the backend's 366-date backfill in `dd-MM-yyyy` fetched 466,346 records with no failed page. Recommendation and the remaining approval: `docs/decision-review/TASK_12_OWNER_DECISION_PACKET.md` D5.

### RD-049 — Account email copy

- **Resolution (2026-10-15):** RESOLVED by ADR-0052 item 7 in `docs/architecture/DECISION_LOG.md`. Each account email is one plain-text message in English, Marathi and Hindi, with the link once; the wording is in `AuthEmails.java` and role file 02 §10.3 A-14. Live through Resend: 23 of 23 checks. No native speaker has reviewed the Marathi and Hindi text.
- **Resolved by:** Claude under the Human Project Owner's delegation recorded in ADR-0050 (conversation of 2026-10-15, "AgriNex — Final Phase 1 Closure and Owner Decision Resolution").

- **ID:** RD-049
- **Question:** What are the approved subject and body texts, and languages, of the three account emails: email verification, password reset and password changed?
- **Why It Matters:**
  - ADR-0048 decides how account emails are sent and which links they carry. Role file 02 defines the screens around them (S14, E28) but not the email texts.
  - The backend needs a text to send. Task 10 uses minimal English texts that state only the link, its single use and lifetime, and what to do if the request was not yours (`backend/src/main/java/com/agrinex/auth/email/AuthEmails.java`). They are an ASSUMPTION, not approved copy.
  - Copy, its Marathi and Hindi versions and their review are UI/UX decisions (spec §40; ADR-0046).
- **Affected Area:** UI/UX copy; auth module email adapter; localization.
- **Source / Evidence:** ADR-0047, ADR-0048; `docs/02_AgriNex_UIUX_Phase1_Specification.md` (S14, E28: on-screen copy only).
- **Blocking Status:** NON-BLOCKING for local development and tests, which use the non-delivering email adapter. BLOCKING for production account email, which RD-037 also blocks through the sending domain.

### RD-050 — Source commodity spellings that are not Agmarknet 2.0 master names

- **Resolution (2026-10-15):** RESOLVED by ADR-0051 in `docs/architecture/DECISION_LOG.md`. A `commodity_source_aliases` table (migration V12) maps the 20 legacy spellings, each on its DMI `Commodity_Code` evidence, and keeps the source spelling on the stored row. The re-run backfill stored 8,978 of the 11,337 records; 276 are quarantined conflicts and 2,083 were identical to an observation already stored under the master spelling.
- **Resolved by:** Claude under the Human Project Owner's delegation recorded in ADR-0050 (conversation of 2026-10-15, "AgriNex — Final Phase 1 Closure and Owner Decision Resolution").

- **ID:** RD-050
- **Question:** May ingestion resolve a source commodity spelling that is not a master name to a master commodity and, if so, on what evidence and through which mechanism?
- **Why It Matters:**
  - ADR-0006 item 2 resolves a record's commodity only by exact match with `commodities.canonical_name`. The schema has no commodity alias table, so other spellings are rejected as `UNKNOWN_COMMODITY`.
  - In the historical resource for Maharashtra, 20 commodity spellings that are not master names occur up to 2026-05-06. The full backfill of 2025-10-15 to 2026-10-15 on the local database (366 report dates, 466,346 records) rejected 11,337 records for them (2.4 %). The largest are `Arhar(Tur/Red Gram)(Whole)` (9,299 records), `Arhar (Tur/Red Gram)(Whole)` (402), `Black Gram (Urd Beans)(Whole)` (323), `Green Gram (Moong)(Whole)` (285) and `Paddy(Dhan)(Common)` (149).
  - Each of them carries the same official `Commodity_Code` as exactly one master name, for example code 49 for `Arhar(Tur/Red Gram)(Whole)` and `Red gram/Arhar/Tur(whole)`, code 2 for `Paddy(Dhan)(Common)` and `Paddy(Common)`, and code 14 for `Sunflower` and `Sunflower/Sunflower Seed`.
  - Mapping them needs either a new table (as `district_source_aliases` does for districts) or matching on `Commodity_Code`, which only the historical resource carries. Both change the database design and the ingestion rules, so they need a decision (CLAUDE.md §G, §I). Adding the old spellings as separate commodities would split one commodity's series in two.
- **Affected Area:** Database (commodity module), ingestion (rule V3), historical backfill.
- **Source / Evidence:** ADR-0006 item 2; Task 12 harvest of both resources (Maharashtra, 156 report dates); `docs/data/AGRINEX_VERIFIED_GOVERNMENT_API_DATA_REFERENCE.md` §23.
- **Investigation (2026-10-15):** all 20 spellings map, through their `Commodity_Code`, to exactly one master name, and every target already has the verified unit `Rs./Quintal`. 13 differ only in spacing; 7 are renames. (Correction, 2026-10-15: 12 differ only in spacing, 1 in spacing and a suffix, as the packet's table shows.) The full mapping table, the options and the recommendation (a `commodity_source_aliases` table with evidence notes, then a re-run of the backfill to 2026-05-06) are in `docs/decision-review/TASK_12_OWNER_DECISION_PACKET.md` D1.
- **Blocking Status:** NON-BLOCKING. Until it is decided, these records are rejected as `UNKNOWN_COMMODITY` with their raw payload kept and are visible to admins. No record dated after 2026-05-06 is affected, so current comparisons and trends are not.
