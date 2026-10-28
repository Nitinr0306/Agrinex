# AgriNex — Task 9 Final Decision Lock

**Status:** REFERENCE (decisions 2026-10-04; record completed 2026-10-05). It records how the Human Project Owner's decisions H1 to H6 were locked and reconciled. It is never a source of requirements: the decisions live in `docs/architecture/DECISION_LOG.md` (ADR-0043 to ADR-0048) and their effects in the authoritative documents listed in §7.

**Instruction:** conversation of 2026-10-04 ("TASK 9 FINAL — HUMAN DECISION LOCK + CONTRACT/UX CONSISTENCY + FINAL READINESS GATE"). No Task 10 work, no code, no scaffolding, no migration, no deployment.

## 1. Decisions

| # | Owner's decision | Recorded in | Status |
|---|---|---|---|
| H1 | Concept A, Hisaab, with four borrowings: B's step-by-step mobile input and answer strip; C's trade-off sentence and shared-scale transport visualization | ADR-0043 items 1–2; role file 02 §1.1 | APPROVED |
| H2 | The Hisaab design and the existing PDDs are accepted; make them implementation-stable | ADR-0044; role file 02 §17 | APPROVED (PDD-14 reworded) |
| H3 | Final wording chosen by Claude after research | ADR-0045 (delegation ADR-0043 item 4(a)); departure D-16 | DECIDED |
| H4 | Claude owns the Marathi and Hindi copy | ADR-0046; role file 02 §10 | APPROVED |
| H5 | Email at registration; login by email or phone with one password; Phase 1 password recovery through the verified email; no paid SMS or OTP; mechanism chosen by Claude | ADR-0047 (product; departure D-15), ADR-0048 (mechanism; delegation ADR-0043 item 4(b)) | APPROVED / DECIDED |
| H6 | Screen approval consistent with H1; specify the screens that were never rendered | ADR-0043 item 3; SSOT index area 5; role file 02 §19 | APPROVED |

RD-045, RD-046 and RD-047 are resolved. The master specification is not edited: the two places where its text differs are departures D-15 and D-16 (decision-log rule 5), and every other effect fills a gap the specification leaves open.

## 2. H3 research (2026-10-04)

**The problem.** Rank 1 is the composite-score winner (§16; ADR-0023 item 2; ADR-0024 weights 0.55 net, 0.15 price, 0.10 trend, 0.10 freshness, 0.10 transport penalty), but the specification's banner calls it "Highest Net" (§25 line 1254) and "Highest Net Realization … Recommended" (§46 line 1956). §46's narrative itself notes that the highest-price market can "still win on net". So the label can be false.

| Source (fetched 2026-10-04) | What it says | How it is applied |
|---|---|---|
| Google Travel Help, answer 7664728 (Google Flights) | "These flights are ranked based on the best trade-off between price and convenience, including factors such as duration, number of stops…"; "The 'Cheapest' tab prioritizes low prices" | A composite ranking keeps its own label and names its factors; a single-metric label ("Highest price", "Highest estimated net realization") is used only for that metric |
| Consumer Protection (E-Commerce) Rules, 2020, rule 5(3)(f) (consumerprotection.in) | "an explanation of the main parameters which, individually or collectively, are most significant in determining the ranking … and the relative importance of those main parameters through an easily and publicly available description drafted in plain and intelligible language" | The basis line names the parameters in plain language, net realization first. AgriNex Phase 1 is not a marketplace, so the rule does not bind it; it is a domestic benchmark |
| 2026 amendment of those Rules (G.S.R. 789(E), notified 2026-09-09, in force 2027-01-01), as summarized by LexOrbis | parameters "must be stated in descending order of importance" | The basis line lists the parameters in descending weight. Secondary source: a law-firm summary, not the gazette text |
| Microsoft HAX Toolkit, guideline 11 | "Enable the user to access an explanation of why the AI system behaved as it did"; "the mere presence of an explanation has been shown to increase user trust. This may cause over-reliance" | "Why the highest price is not ranked first" and "Why this market?" explain; the wording stays hedged ("may", "about", "estimated") to avoid over-trust |
| Google PAIR, People + AI Guidebook, Explainability + Trust | "the user shouldn't trust the system completely … the user should know when to trust the system's predictions and when to apply their own judgement"; "data sources have to be part of your explanations" | The basis, the report dates and the attribution stay beside the recommendation |
| Nielsen Norman Group, UX Guidelines for Recommended Content | "Clearly State the Source of Data"; "avoid adding vague descriptions" | The basis is specific ("estimated net realization, price, trend, data freshness and transport"), not "smart" or "best" |
| Spec §49 | Forbids "Guaranteed best market", "You will earn ₹X"; requires "Estimated net realization" | No superlative or outcome claim on rank 1 |

**Result:** ADR-0045 — "Recommended" for rank 1 with its basis; "Highest price" and "Highest estimated net realization" only where true; a trade-off sentence whose wording depends on the sign of the estimated difference; "Highest estimated net (what-if)" in What-If.

## 3. H2 — review of PDD-01 to PDD-14

| PDD | Checked against | Result |
|---|---|---|
| 01 Alert polling (start, focus, every 5 min) | ADR-0019 (interval set by role file 02), spec §15 (6-hourly runs) | Consistent |
| 02 Two farmer destinations + account | No public market-list operation (contract); Hisaab top app bar | Consistent |
| 03 Locality optional | `CompareRequest` has no locality; ADR-0014 | Consistent; also step 1 of the mobile input |
| 04 Attribution verbatim in all locales | §31 RULE 7; ADR-0046 | Consistent |
| 05 Fallback names as-is with `lang="en"` | ADR-0015 | Consistent |
| 06 Latin digits, Indian grouping | §6 personas | Consistent |
| 07 Estimates to whole rupees with "≈" | ADR-0022 item 10 (2 dp in the API) | Consistent |
| 08 Absolute dates, 24-hour IST | ADR-0007 | Consistent |
| 09 Trend points: date and dataset | ADR-0010, ADR-0025 | Consistent |
| 10 Out-of-range quantity in What-If | ADR-0023, `FRONTEND_CONSUMPTION.md` §3 | Consistent |
| 11 What-If treatment after "use these numbers" | §17 J, ADR-0026 | Consistent |
| 12 Freshness copy without day counts | Configurable thresholds (ADR-0020, ADR-0024) | Consistent |
| 13 Busy button, skeleton after 300 ms | §27 compare target < 1 s | Consistent |
| 14 Winner callout in rupees | ADR-0045 | Reworded: "wording per ADR-0045" (it had said "pending RD-045") |

## 4. H5 — what existed and what was added

| Item | Before the lock | After (ADR-0047, ADR-0048) |
|---|---|---|
| Email on the account | `users.email` in the spec's domain model (§20 UK) and the data dictionary; required for ADMIN only | Required for FARMER too (CHECK amended); never returned by the API |
| Login | One `identifier`, "phone or email, normalized" (ADR-0016 item 2) | Unchanged mechanism; a FARMER may use either identifier of the same account with the same password |
| Registration fields | Phone, password, optional name and district | Plus email; nothing else added or removed |
| Recovery | None (RD-047) | Emailed single-use links: verification 24 h, reset 30 min; 256-bit tokens, hashes on `users`; 202 always on request; all sessions revoked on reset; no auto sign-in |
| Contract | 49 operations | 53: `requestPasswordReset`, `confirmPasswordReset`, `confirmEmailVerification`, `resendEmailVerification`; `PASSWORD_RESET_TOKEN_INVALID`, `EMAIL_VERIFICATION_TOKEN_INVALID`; `RegisterRequest.email`; `AuthenticatedUser.emailVerified`. Still 1.0.0: never merged (precedent in `CONTRACT_VALIDATION.md`) |
| Database | `users` 7 columns | 12 columns; 2 unique constraints and 3 CHECKs added; no new table, foreign key or migration (none exists yet) |
| Rate limits | ADR-0017 | Extended: 10/min per IP for each new operation; 3 reset requests per identifier per hour; 3 resends per account per hour |
| Email delivery | None; alerts in-app only (ADR-0019) | Resend HTTPS API through `RestClient`, account emails only; `RESEND_API_KEY`; alerts unchanged |
| Security review | Task 8.5 (SR-00 to SR-16) | `docs/security/TASK_9_FINAL_ACCOUNT_RECOVERY_SECURITY_REVIEW.md` (AR-01 to AR-12) |

**Discrepancy recorded:** the owner's instruction calls Resend "the existing Resend email infrastructure already used by the project", but no project document mentioned Resend before ADR-0048. ADR-0048 records it as the provider on the strength of the owner's instruction, verified against Resend's documentation; a live send is an implementation gate (AR-07).

**Validation:** reduced contract checker, 2,218 checks, 0 failures, 14 of 14 targeted mutations detected, 53 operations, 83 mocks (`CONTRACT_VALIDATION.md` §2.1). Database counts recomputed from the dictionary: 229 columns, 83 architectural-decision columns, 47 indexes.

## 5. UX consistency audit of the approved design

Checked: role file 02 against the specification, the binding entries, the contract and mocks, and the architecture documents.

| Area | Result | Evidence |
|---|---|---|
| Desktop | Consistent | Role file 02 §1.1, §13, §19: the comparison sheet at ≥ 1024 px; screens without a draft specified |
| Mobile | Consistent | §1.1: ledger cards, step-by-step input below 1024 px |
| Major Phase 1 journeys | Consistent | §5: J1–J6 |
| Authentication, registration, login, password recovery | Consistent with the contract | S10 fields = `RegisterRequest`; sign-in = `LoginRequest.identifier`; S14 = `PasswordResetRequest`; S15 = `PasswordResetConfirmRequest`; S16 = `EmailVerificationConfirmRequest`; S12 = `resendEmailVerification` + `emailVerified` |
| Language flows | Consistent | S01, §10 (glossary, register), §11; ADR-0046 |
| Comparison, recommendation, explanation | Consistent | S04, S05 with ADR-0045; never a score; API order kept |
| Trends | Consistent | S07; INSUFFICIENT_DATA message §33 |
| What-If | Consistent | S09; "Highest estimated net (what-if)"; break-even worded as estimates |
| Alerts | Consistent | S11; in-app only (ADR-0019) |
| Admin | Consistent | A01–A05; no secrets, no operator procedures |
| Stale, outage, no data, no suitable markets, errors | Consistent | §9 E01–E31 with spec texts verbatim where the spec gives them |
| Freshness and provenance | Consistent | §6, §7 |
| Transport-rate display | Consistent | §7 transport label (ADR-0038) |
| "Estimated net realization" wording | Consistent | §7, §10 glossary |
| Deterministic recommendation; ML | Consistent | Labels never re-rank; S08 optional and separate (ADR-0030) |
| Hypothetical values | Consistent | §6; What-If treatment also on the transport bar |
| Accessibility | Consistent | §12: approved Hisaab pairs ≥ 4.5:1; controls and bar ≥ 3:1 |
| Responsive behaviour | Consistent | §13 |

**Corrected during the audit:** stale ADR ranges in `docs/README.md`, `docs/database/DATABASE_INDEX.md` and `docs/architecture/ARCHITECTURE_INDEX.md` (which also named departures "D-01 to D-13" although D-14 existed); the decision log's definition of DECIDED, aligned with its rule 3; five Marathi and Hindi drafts (role file 02 §10.3).

**Implementation notes (not decisions):** demo-profile test accounts (ADR-0028 item 3) need a synthetic email on every FARMER account, because the amended CHECK requires one; the bootstrap ADMIN's email is stored as verified.

## 6. Task 10 readiness gate

| Criterion | Result |
|---|---|
| H1–H6 locked | Yes: ADR-0043 to ADR-0048 |
| Hisaab recorded as the approved direction | Yes: SSOT index (top and area 5), ADR-0043, role file 02 |
| B and C borrowings recorded | Yes: ADR-0043 item 2; role file 02 §1.1 |
| Authentication decisions consistent | Yes: contract, mocks, database, security, frontend and role file aligned; checker passes |
| Email at registration recorded | Yes: ADR-0047 item 1; `RegisterRequest.email` |
| Login by email or phone recorded | Yes: ADR-0047 item 3; `LoginRequest.identifier` |
| Phase 1 password recovery recorded | Yes: ADR-0047 item 4; ADR-0048 |
| No paid SMS provider | Yes: ADR-0047 item 4 |
| H5 contract and schema implications reconciled | Yes: §4 above |
| Design documentation internally consistent | Yes: §5 above; stale-statement sweep clean |
| No genuine blocking decision | Yes. Open items do not block Task 10: RD-002 (user action; blocks committing `.mcp.json` and pushing), RD-012 and RD-025 (Phase 2), RD-037 (public deployment and production email only) |
| No Task 10 implementation started | Yes: no code, scaffold, migration or deployment exists |

**Result: READY FOR TASK 10.** Task 10 follows the enforced development order (spec §42): setup and infrastructure, database, data foundation and ingestion, backend core, recommendation engine, frontend core, quality and deployment. ML stays optional and after the deterministic path (HARD RULE 2).

## 7. Documents changed

- **Decisions:** `docs/architecture/DECISION_LOG.md` (ADR-0043 to ADR-0048; D-15, D-16; status notes on ADR-0016 and ADR-0017; DECIDED definition); `docs/REQUIRES_DECISION.md`.
- **Design:** `docs/02_AgriNex_UIUX_Phase1_Specification.md`; `docs/decision-review/TASK_9_DESIGN_DIRECTION_PACKET.md` (outcome note).
- **Contract:** `contracts/openapi.yaml`, `contracts/error-response.schema.json`, `frontend/__mocks__/api/` (3 updated, 8 added, README); `docs/api/API_INDEX.md`, `API_CONVENTIONS.md`, `FRONTEND_CONSUMPTION.md`, `CONTRACT_VALIDATION.md`.
- **Database:** `docs/database/DATA_DICTIONARY.md`, `INDEXES_AND_CONSTRAINTS.md`, `ERD.md`, `DATABASE_ARCHITECTURE.md`, `DATABASE_INDEX.md`.
- **Architecture:** `docs/architecture/SECURITY_ARCHITECTURE.md`, `SYSTEM_CONTEXT.md`, `CONTAINER_ARCHITECTURE.md`, `COMPONENT_ARCHITECTURE.md`, `DATA_FLOW.md`, `DEPLOYMENT_ARCHITECTURE.md`, `RESILIENCE_ARCHITECTURE.md`, `TESTING_ARCHITECTURE.md`, `FRONTEND_ARCHITECTURE.md`, `CURRENT_TECHNOLOGY_BASELINE.md`, `ARCHITECTURE_INDEX.md`.
- **Security:** `docs/security/TASK_9_FINAL_ACCOUNT_RECOVERY_SECURITY_REVIEW.md` (new).
- **Navigation:** `docs/00_SSOT_INDEX.md`, `docs/README.md`.
- **Tooling:** the project skills `agrinex-design`, `agrinex-frontend`, `agrinex-api-contract`, `agrinex-qa`, `agrinex-backend`, `agrinex-database` (pointers to the new decisions).
- **Not changed:** the master specification, `.mcp.json`, Git history, CI or deployment configuration.
