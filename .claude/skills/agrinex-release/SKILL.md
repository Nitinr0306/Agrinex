---
name: agrinex-release
description: AgriNex release and phase-gate readiness. Covers contract compatibility, migration checks, test gates, security sign-off, environment readiness, rollback readiness, data-source and provenance readiness, the Phase 1 freeze checklist, and the human approvals every release needs. Use this skill whenever a task prepares, reviews or decides a release, a staging or public deployment, a demo build, the Phase 1 freeze, a contract version change, or asks whether AgriNex is ready to ship, even if the user only says "can we deploy now?".
---

# AgriNex release

## Purpose

Decide readiness from evidence, never from optimism. A release, deployment or phase gate goes ahead only when every applicable gate has evidence and every required human approval is recorded.

This skill points to the authoritative documents. If it disagrees with them or with a binding decision, they win: correct this file.

## When to use

- Before a staging or public deployment, or a demo build.
- Before the Phase 1 freeze.
- Before merging a contract version change.
- Whenever someone asks "are we ready?".

## Source hierarchy

This follows CLAUDE.md §B:

1. The master specification:
   - §10 phase gate and freeze checklist;
   - §36 experiments, §37 risks and fallbacks;
   - §41 contract change rule, §42 development order;
   - §45 deployment, §48 acceptance criteria.
2. Binding decision-log entries, including the departures table (for example D-11 for the contract, D-12 for demo fixtures).
3. `docs/architecture/DEPLOYMENT_ARCHITECTURE.md`, `TESTING_ARCHITECTURE.md`, `SECURITY_ARCHITECTURE.md`; `docs/api/API_CONVENTIONS.md` §2; `docs/api/CONTRACT_VALIDATION.md`; `docs/security/TASK_8_5_SECURITY_REVIEW.md`.
4. Current official tool documentation, fetched with Context7, when a gate depends on tool behavior.

## Required reading (READ SSOT FIRST)

1. `CLAUDE.md` §A (HARD RULES), §L (done means tested) and §K, then `docs/00_SSOT_INDEX.md` and `docs/REQUIRES_DECISION.md`. Open items that block deployment, such as RD-037 for hosting, block the release.
2. Spec §10 and §48 in full.
3. `docs/architecture/DECISION_LOG.md`: the index, the departures, and ADR-0042 (security outcomes and gates).
4. The skills whose evidence the gates consume: agrinex-qa, agrinex-api-contract, agrinex-database, agrinex-cloud.

## Non-negotiable release gates (all must have evidence)

1. **Human approvals.**
   - Contract changes have two consuming-role approvals (spec §41).
   - Security-sensitive changes have human review (CLAUDE.md rule 16; ADR-0042).
   - Production screens have recorded design approvals (ADR-0027, SSOT area 5).
   - The Phase 1 freeze is declared by the project owner.

   No release without them.
2. **Contract compatibility.**
   - The file versions and `info.version` are incremented, and each change is classified as additive or breaking (`API_CONVENTIONS.md` §2; spec §41).
   - Mocks are updated in the same change.
   - Contract tests pass for every operation, including errors (ADR-0022 item 1).
   - The drift check of a generated document against `contracts/openapi.yaml` is clean (D-11).
3. **Migrations.**
   - Flyway migrations run automatically on startup (spec §10).
   - They are forward-only, and no applied migration was edited.
   - They were tested against a copy of existing data.
4. **Tests.**
   - Every §48 Phase 1 criterion has run evidence at its threshold.
   - The §10 freeze checklist items are checked with evidence.
   - The Definition of Done in `TESTING_ARCHITECTURE.md` is met.
   - OPTIONAL_ML operations are excluded from Phase 1 gates.
5. **Security.**
   - The ADR-0042 outcomes are implemented: account resolution on token use, the `__Secure-agrinex_refresh` cookie, the logging prohibition, centroid enforcement, and no HTTP route for operator procedures.
   - The final CSP is verified (SR-11).
   - TLS is verified and HSTS enabled. Actuator is not public.
   - No published or demo credentials exist, and the bootstrap password is removed (ADR-0028).
   - No secret is in the repository, images or logs.
6. **Environment readiness.**
   - Hosting is decided (RD-037 resolved).
   - `.env` is complete against `infra/.env.example`, with names checked and values never printed.
   - Health checks are green: actuator readiness, `/api/health`, `scripts/health_check.sh` (spec §43, created at infrastructure scaffolding).
   - Only ports 80 and 443 are public.
7. **Rollback readiness.**
   - Images are pinned to the baseline tags, so the previous release can be redeployed.
   - The daily `pg_dump` backups (7 days) exist (`DEPLOYMENT_ARCHITECTURE.md` §4.8). A test restore is the evidence of readiness: a verification practice of this skill, not an SSOT requirement.
   - Migrations are forward-only, so rolling back across a schema change means restoring a backup. Plan for it.
   - The local fallback for demos exists (spec §37).
8. **Data-source and provenance readiness.**
   - data.gov.in is the configured source; the admin status shows the API key as CONFIGURED (ADR-0005, ADR-0020).
   - The historical bootstrap holds at least 60 days (§10), and ingestion completeness is at least 95 % per run (§48).
   - Outage behavior was verified with the key withheld.
   - Freshness and attribution are on every price screen.
   - No synthetic or non-government data is in the deployment. Demo data is an exact export of real observations, loaded only with the demo profile (ADR-0028; D-12).
9. **Phase 1 freeze** (spec §10, HARD RULE 1).
   - The sequence is: internal testing, staging deployment, the evaluation experiment (Experiment 1), then the freeze.
   - Experiment 2 needs 14 consecutive days of ingestion on the public deployment (§36).
   - Phase 2 starts only after the freeze. ML starts only after the deterministic engine works end to end (HARD RULE 2).

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

- Assembling the gate checklist with evidence links.
- Identifying blockers.
- Preparing the release record for human approval.
- Verifying that departures and accepted risks are documented.

## Forbidden responsibilities

- Declaring readiness without evidence.
- Waiving a gate or approving on a human's behalf.
- Changing requirements, thresholds or decisions to pass a gate.
- Deploying with demo credentials or synthetic data.
- Starting Phase 2 before the freeze.

## Dependencies on other skills

- agrinex-qa: test evidence.
- agrinex-api-contract: compatibility.
- agrinex-database: migrations.
- agrinex-cloud: environment and rollback.
- agrinex-architecture: decision status.
- agrinex-design and agrinex-frontend: design approvals and the CSP gate.

## Validation expectations

Each gate is PASS (with evidence), FAIL (with the reason) or NOT APPLICABLE (with the reason). A single FAIL in a required gate means NOT READY.

## Escalation rules

- **Missing approval or evidence:** NOT READY. State exactly what is missing.
- **A gate conflicts with a decision:** report it. Never resolve it by editing the decision.
- **A security finding during release:** stop, and get a human review.

## Output expectations

Produce a gate table (gate, status, evidence, owner), then the list of blockers. Finish with READY or NOT READY and the exact reason. Never write "ready" while any required gate lacks evidence.
