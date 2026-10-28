---
name: agrinex-qa
description: AgriNex quality assurance. Covers contract validation, unit, integration and API tests, hand-computed fixtures for transport, net realization and scoring, provenance and freshness tests, security regression, ML-unavailable behavior, edge cases, negative and mutation testing, and acceptance against spec §48 and the §10 Phase 1 freeze checklist. Use this skill whenever a task writes, plans, runs or reviews tests, validates mocks or the contract, checks acceptance criteria, prepares evaluation experiments, or claims that any AgriNex work is complete.
---

# AgriNex QA

## Purpose

Prove, with tests that were actually run, that AgriNex behaves as the SSOT and the decisions require. Report results faithfully, with no false claims of completeness.

This skill points to the authoritative documents. If it disagrees with them or with a binding decision, they win: correct this file.

## When to use

- Planning, writing or reviewing tests at any level.
- Validating the contract or the mocks.
- Checking a task against its acceptance criteria.
- Preparing Experiments 1–3.
- Any time someone is about to say "done".

## Source hierarchy

This follows CLAUDE.md §B:

1. The master specification: §10 freeze checklist, §27, §36 experiments, §44 mocks and fixtures, §48 acceptance criteria.
2. Binding decision-log entries.
3. `docs/architecture/TESTING_ARCHITECTURE.md`, `docs/api/CONTRACT_VALIDATION.md`, `contracts/`, and the database documents.
4. Current official documentation for the test tools chosen at scaffolding (ADR-0034), fetched with Context7.

## Required reading (READ SSOT FIRST)

1. `CLAUDE.md` §L, then `docs/00_SSOT_INDEX.md` area 6, and `docs/REQUIRES_DECISION.md`.
2. `docs/architecture/TESTING_ARCHITECTURE.md`: test levels, test data, environments, Definition of Done.
3. `docs/api/CONTRACT_VALIDATION.md`: checks A–E and D1–D17, the mutation tests, and the required contract tests.
4. `docs/architecture/RECOMMENDATION_ENGINE.md` and `docs/api/API_CONVENTIONS.md` §4.1, for the formulas and rounding behind the fixtures.
5. `docs/api/PROVENANCE_AND_FRESHNESS.md` and `docs/architecture/RESILIENCE_ARCHITECTURE.md`, for the states and the outage behavior.
6. Decision-log entries:
   - ADR-0010, ADR-0012, ADR-0022 to ADR-0026;
   - ADR-0028 (synthetic fixtures never in deployment), ADR-0030;
   - ADR-0036 (Earth radius), ADR-0039, ADR-0040, ADR-0042 (security outcomes);
   - ADR-0045 (recommendation labels), ADR-0047 and ADR-0048 (accounts and recovery; review AR-01 to AR-12 in `docs/security/TASK_9_FINAL_ACCOUNT_RECOVERY_SECURITY_REVIEW.md`).

## Non-negotiable AgriNex rules

- **Tests are part of the task.** Every implementation task names its tests up front (rule 12). Work is complete only when they ran and passed, failures were investigated, and the acceptance criteria are met (CLAUDE.md §L).
- **Never** weaken, skip or delete a test to make a run pass.
- **Deterministic recommendation verification.** Use hand-computed fixtures (spec §27, §48), with the ADR-0036 radius and the §4.1 rounding points, for:
  - transport cost;
  - gross and net realization;
  - score ordering and tie-breaks;
  - `rankingsDisagree`;
  - all 8 reason codes;
  - What-If client and server parity.
- **Contract tests** cover every operation, including error responses, with decimal-place checks (ADR-0022 item 1). Every mock is validated against its declared operation and status.
  - `getMarketForecast` (OPTIONAL_ML) stays out of the Phase 1 suites.
- **Provenance and freshness:**
  - badge accuracy of 100 % against `reportDate` (§48);
  - `sourceStatus` on every price-serving response;
  - outage behavior with the government API key withheld: data is still served and the banner shows (§48);
  - no fabricated, zero-filled or carried-forward prices.
- **ML unavailable:** the forecast endpoint answers 200 with `available: false`, never a 5xx, and comparisons are unaffected (§29, §35; ADR-0030).
- **Security regression** (ADR-0016, ADR-0017, ADR-0042):
  - 401 for missing, invalid or expired tokens and for inactive or removed accounts, including on compare and simulate;
  - the `__Secure-agrinex_refresh` cookie with its attributes, and the `Origin` check on refresh and logout;
  - non-revealing login, with the same 401 and the 429 behavior;
  - rate limits;
  - no request or response bodies, secrets or personal data in logs;
  - non-centroid origins rejected with 400;
  - no HTTP route for erasure, deactivation or reactivation;
  - no secrets or internal ids in any response;
  - accounts and recovery (ADR-0047, ADR-0048): login by phone and by email, same password; the reset request answers the same 202 with comparable timing for every account state; single-use, expiring, replaceable tokens stored only as hashes; all sessions revoked on reset and no auto sign-in; the new rate limits; no email, token or link in logs;
  - recommendation labels (ADR-0045): "Recommended" only on rank 1, "Highest estimated net realization" only on the highest estimates, no false "more money" claim.
- **Edge cases:**
  - no market in range, and markets in range but no recent data;
  - stale-only data, and insufficient trend data;
  - an unsupported or unverified unit;
  - an estimate beyond the stored precision (422);
  - the simulator bounds;
  - pagination past the end.
- **Negative and mutation testing.** Where a checker or validator exists, prove it catches deliberately broken inputs, as `CONTRACT_VALIDATION.md` did for the contract.
- **Synthetic data stays synthetic.** Test fixtures are synthetic and labelled as such, and never enter a deployment database (ADR-0028). Integration inputs use the recorded evidence captures (`TESTING_ARCHITECTURE.md` §4.4).
- **Acceptance thresholds** (§48):
  - comparison p95 under 1 s for 10 users (k6);
  - ingestion completeness of at least 95 %;
  - 0 duplicates on a repeat run;
  - no untranslated farmer strings;
  - no horizontal scroll at 375px.

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

- Writing and running tests, fixtures, validators and mutation suites.
- Contract and mock validation.
- Acceptance and freeze-checklist evidence.
- Experiment scripts as the spec defines them (§36).

## Forbidden responsibilities

- Declaring work complete without run evidence.
- Changing requirements or thresholds to fit results.
- Using live data.gov.in responses as fixed test expectations.
- Presenting synthetic data as real.
- Choosing test tools without the ADR-0034 justification.

## Dependencies on other skills

Every other skill. Most often:

- agrinex-api-contract: what to validate.
- agrinex-backend and agrinex-frontend: the code under test.
- agrinex-database: duplicates and completeness.
- agrinex-release: the gates that consume QA evidence.

## Validation expectations

QA validates the others. Its own outputs are checked by:

- rerunning the suites;
- mutation tests for checkers;
- tracing each test to a spec criterion, decision or contract operation.

## Escalation rules

- **Failing tests:** report them with output. Never suppress.
- **An ambiguous expected value:** REQUIRES_DECISION. Never pick a convenient number.
- **A security regression:** stop and raise it for human review.

## Output expectations

Report:
- the tests added and run, with counts of passed, failed and skipped, and the failure output;
- the coverage of the §48 criteria and the §10 checklist items;
- the mutation-test results where relevant;
- open defects and items.

Never write "complete" without that evidence.
