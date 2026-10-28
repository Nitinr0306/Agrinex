---
name: agrinex-backend
description: Implementing and reviewing the AgriNex Spring Boot backend strictly against the approved architecture, database design and API contract. Covers modules and ports, validation, error handling, authentication and security rules, the government-data ingestion boundary, the deterministic recommendation engine, Redis behavior, alerts, admin audit, operator procedures and the database-mediated ML boundary. Use this skill whenever a task writes, plans, reviews or tests backend Java/Spring code, Maven or Spring configuration, Flyway migrations, scheduled ingestion, or the server-side behavior of any /api/v1 operation, including scaffolding the backend project.
---

# AgriNex backend

## Purpose

Build the backend so that it matches the contract exactly, respects module ownership, and computes every number deterministically and traceably. Make no architectural, product or security decision along the way.

This skill points to the authoritative documents. If it disagrees with them or with a binding decision, they win: correct this file.

## When to use

- Scaffolding the backend, or adding a dependency.
- Implementing or reviewing a module, controller, service, repository, port, migration or scheduled job.
- Server-side validation, errors, security, rate limiting or logging.
- Ingestion, comparison, simulation, trends, alerts, admin or operator procedures.

## Source hierarchy

This follows CLAUDE.md §B:

1. The master specification.
2. Binding decision-log entries.
3. `contracts/`, the architecture documents, the database documents, and the approved baseline.
4. Current official documentation for the exact library version, fetched with Context7 (rules 5–6; `.claude/rules/context7.md`). Never write a library call from memory.

## Required reading (READ SSOT FIRST)

1. `CLAUDE.md`, then `docs/00_SSOT_INDEX.md` areas 2–4 and 8, and `docs/REQUIRES_DECISION.md`.
2. `docs/architecture/CURRENT_TECHNOLOGY_BASELINE.md`: Java 25, Spring Boot 4.1.x, Maven 3.10.x with the wrapper (ADR-0003), and the Boot-managed Hibernate, Flyway and test stack.
3. Architecture:
   - `COMPONENT_ARCHITECTURE.md` (modules, dependency edges, ports, cross-cutting elements);
   - `RECOMMENDATION_ENGINE.md`, `INGESTION_ARCHITECTURE.md`;
   - `SECURITY_ARCHITECTURE.md`, `RESILIENCE_ARCHITECTURE.md`, `DATA_FLOW.md`.
4. Database: `docs/database/DATA_DICTIONARY.md`, `TABLE_OWNERSHIP.md`, `INDEXES_AND_CONSTRAINTS.md`.
5. Contract: `contracts/openapi.yaml`, the schemas, and `docs/api/API_CONVENTIONS.md` (especially §4.1 rounding, §6 errors, §7 security).
6. Spec §12–§17, §20, §27–§35, §48.

## Non-negotiable AgriNex rules

- **Contract exactness.** Every request and response matches `contracts/openapi.yaml`: field names, decimal places, closed objects, error codes and titles, headers. Errors always use `ApiErrorResponse`, and 500/503 never carry detail or stack traces (spec §28).
- **Module ownership.** Each module owns its repositories. Other modules are reached only through approved dependencies and the read-only ports of ADR-0035. The only edges are those in `COMPONENT_ARCHITECTURE.md` §4.1, including recommendation → geography from ADR-0039.
- **Ingestion boundary:**
  - only the ingestion module calls data.gov.in, server-side, with the key from the environment (spec §31 RULE 1);
  - the sources are the two resources of ADR-0005;
  - orchestration (ADR-0011), the 6-hour schedule, and backfills of at most 366 report dates;
  - identity, duplicates and conflicts (ADR-0009), counters and completeness (ADR-0012), unit validation (ADR-0008), dates (ADR-0007).
- **Outages.** A failed response is never "zero records" and never changes stored data. Never switch source, carry prices forward, interpolate or zero-fill (ADR-0005).
- **Recommendation engine:**
  - observation selection (ADR-0010) with pinned thresholds for comparisons (ADR-0035);
  - the transport, gross and net formulas of §32 in exact decimal arithmetic, rounded only at the points in `API_CONVENTIONS.md` §4.1;
  - haversine with the Earth radius 6 371.0088 km, with the double converted through `BigDecimal.valueOf` and rounded half-up to 2 decimal places (ADR-0036);
  - scoring, tie-breaks and reason codes (ADR-0024); trends (ADR-0025); no-data semantics (ADR-0023); What-If (ADR-0026);
  - only district-centroid origins are accepted (ADR-0039);
  - estimates beyond `numeric(14,2)` answer 422 `ESTIMATE_OUT_OF_RANGE` before anything is recorded (ADR-0040).
- **Security** (ADR-0016, ADR-0017, ADR-0020, ADR-0042):
  - BCrypt cost 12;
  - HS256 JWT valid for 15 minutes, using Spring Security's resource-server support only, with no extra JWT library;
  - rotating refresh tokens with family revocation, in the `__Secure-agrinex_refresh` cookie, with an `Origin` check on refresh and logout;
  - every operation that evaluates a token resolves the account, and an inactive or removed account answers 401;
  - RBAC on every endpoint;
  - the rate limits of ADR-0017, with an in-process fallback when Redis is down (ADR-0018);
  - email verification and password recovery exactly as ADR-0048 states: 256-bit single-use link tokens stored only as SHA-256 hashes on `users`, 24 h and 30 min, 202 always on the reset request with the email sent after commit, all refresh families revoked on reset, no auto sign-in; Resend through `RestClient` with `RESEND_API_KEY` from the environment, no SDK; a live send must be verified before relying on it (AR-07).
- **Logging.**
  - Never log secrets, `Authorization` headers, cookies, the `api-key` parameter, request or response bodies, or personal data such as phone numbers, email addresses and coordinates (ADR-0020, ADR-0042); never log verification or reset tokens, links or email contents (ADR-0048).
  - Use structured JSON logs with a server-generated correlation id.
- **Redis** holds rate-limit counters and a reference-data cache only (ADR-0018; departure D-09).
- **Alerts.** Evaluation runs after each ingestion run that changed observations, in its own transaction, and the frontend polls for events (ADR-0019).
- **Admin.** Every admin write is audited (ADR-0020).
- **Operator procedures.** Account erasure, deactivation and reactivation are one-off operator commands with no HTTP endpoint. They are audited with a named ADMIN as actor (ADR-0037, ADR-0041).
- **ML.** Never call an ML service. The price module reads `forecasts`, and the forecast endpoint answers `available: false` when no current forecast exists. ML never feeds ranking (ADR-0030; HARD RULE 2).
- **Dependencies** are justified (rule 15). Tool choices are made when their area is scaffolded (ADR-0034).

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

- Implementing approved tasks: modules, ports, migrations that match the database design, validation, errors, security configuration, and scheduled jobs.
- Unit, integration and contract tests, with hand-computed fixtures for transport, net realization and scoring (spec §27, §48).
- Reporting gaps as REQUIRES_DECISION.

## Forbidden responsibilities

- Changing the contract, the schema design or the architecture to fit the code.
- New module edges, new services, or a synchronous ML call.
- HTTP endpoints for operator procedures.
- Security controls the SSOT does not require (for example MFA), or weakening required ones.
- Using the non-government CSV as data.
- Fabricating any price or reference value.
- Starting Phase 2 work before the freeze.

## Dependencies on other skills

- agrinex-api-contract: what to implement.
- agrinex-database: tables and migrations.
- agrinex-architecture: boundaries.
- agrinex-qa: tests.
- agrinex-cloud: configuration and deployment.
- agrinex-release: gates.

## Validation expectations

- Unit tests with hand-computed fixtures.
- Integration tests that validate real requests and responses, including errors, against `contracts/openapi.yaml` with decimal-place checks (ADR-0022 item 1).
- Package-dependency tests for module boundaries.
- Security tests:
  - 401 cases, including inactive accounts;
  - the cookie name and attributes, and the `Origin` check;
  - rate limits;
  - no bodies or personal data in logs.
- Resilience test with the API key disabled (spec §48).
- Run the tests and report failures as they are. Never weaken a test.

## Escalation rules

- **Contract or design missing a needed detail:** REQUIRES_DECISION. Never infer from "common practice".
- **CONFLICT between the contract, the design and a decision:** stop and report.
- **Security-sensitive change:** human review before merge.
- **New dependency:** justify it, and check compatibility with the baseline.

## Output expectations

Report:
- the files changed, and the operations or tables affected;
- the library documentation consulted (library ID and version);
- the tests added and run, with their results;
- the decisions cited;
- open items;
- what was not done.
