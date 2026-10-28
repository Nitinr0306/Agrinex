# AgriNex — Skill Inputs After Task 8.5

**Status:** REFERENCE. Input for the skill-population task, prepared on 2026-10-04 (Task 8.5).

**Superseded in part (Task 8.6, 2026-10-04):** RD-038 to RD-044 are resolved (ADR-0035 to ADR-0042), and the skills were populated from this input with the resolved state. The skills and the decision log govern; the open-item tables below are historical.

**Use:**

- Skills and agent definitions are tooling. They cannot override CLAUDE.md or the SSOT (CLAUDE.md §B, §N).
- Each skill should **point to** the authoritative documents below rather than copy their content, so that it does not drift when a decision changes.
- Each skill should state the open items that limit it, and must check `docs/REQUIRES_DECISION.md` before acting (CLAUDE.md §D).

**Repository state observed:**

- **Skill folders:**
  - eight empty folders exist for the skills below;
  - `.claude/skills/agrinex-release/` also exists, empty, but is not in the requested set;
  - `.claude/skills/context7-mcp/SKILL.md` exists and governs library-documentation lookups for every skill.
- **Agent definitions:** `.claude/agents/*.md` are empty, except an outline in `database-engineer.md`.

## 1. Project state every skill must assume (as of 2026-10-04)

| Fact | Source |
|---|---|
| Phase 1 only. Phase 2 waits for the Phase 1 freeze (HARD RULE 1). ML waits for an end-to-end deterministic engine (HARD RULE 2) | CLAUDE.md §A; spec §10 |
| No application code, migrations, infrastructure files or role files 02–04 exist yet | SSOT index areas 3–7 |
| Binding decisions: ADR-0001 to ADR-0034. PROPOSED, not binding: ADR-0035 to ADR-0037 | `docs/architecture/DECISION_LOG.md` |
| API contract 1.0.0: 49 operations, 5 JSON Schemas, 72 synthetic mocks. Validated in Task 8, but the rule-16 security review and the spec §41 consumer review are pending | `docs/api/API_INDEX.md`; `docs/api/CONTRACT_VALIDATION.md`; `docs/security/TASK_8_5_SECURITY_REVIEW.md` |
| Approved technology baseline. Examples: Java 25, Spring Boot 4.1.1, Maven 3.10.x, Next.js 16.3.8, React 19.3.0, Node.js 24, PostgreSQL 16, Redis 7.4 | `docs/architecture/CURRENT_TECHNOLOGY_BASELINE.md` (ADR-0002, ADR-0003) |
| Government source: data.gov.in only, two resource IDs. A 403 or maintenance response is temporary unavailability, never a reason to change the source | ADR-0005; `docs/data/AGRINEX_VERIFIED_GOVERNMENT_API_DATA_REFERENCE.md` |
| ML integration is database-mediated (ADR-0030). `ML_ARCHITECTURE.md` §4.8–§4.9 were aligned on 2026-10-04 | ADR-0030; `docs/architecture/ML_ARCHITECTURE.md` |
| Open items: RD-002, RD-012, RD-025, RD-037, RD-038 to RD-044 | `docs/REQUIRES_DECISION.md`; `docs/decision-review/TASK_8_5_HUMAN_DECISION_PACKET.md` |

**Open items by skill** (a skill must not implement against an undecided outcome):

| Item | Skills it limits |
|---|---|
| RD-002 (`.mcp.json` credentials, user action) | cloud, all (no commit or push of `.mcp.json`) |
| RD-037 (hosting procurement) | cloud |
| RD-038 (module paths; PROPOSED ADR-0035) | architecture, backend |
| RD-039 (Earth radius; PROPOSED ADR-0036) | backend, qa |
| RD-040 (erasure; PROPOSED ADR-0037) | backend, architecture, api-contract, design (only if an admin screen is chosen) |
| RD-041 (transport-label wording) | design, frontend |
| RD-042 (origin enforcement) | backend, architecture, api-contract |
| RD-043 (estimate precision) | backend, database, api-contract, design, frontend |
| RD-044 (account deactivation) | backend, database, api-contract |
| RD-012, RD-025 (Phase 2) | all: nothing Phase 2 |

**Rules common to every skill** (from CLAUDE.md; listed once here):

- the 16 core rules;
- source precedence (§B) and the evidence labels (§D);
- spec protection (§E) and change control (§F);
- Context7 before any library call (rules 5–6; `.claude/rules/context7.md`);
- no secrets anywhere (§K);
- no fabricated, interpolated or synthesized price data (§C; spec P10, §31 RULE 5);
- the out-of-scope list (spec §39);
- the product name AgriNex only.

---

## 2. agrinex-api-contract

**Authoritative documents:**

- `contracts/openapi.yaml`, `common.schema.json`, `price-observation.schema.json`, `recommendation.schema.json`, `error-response.schema.json`, `forecast-output.schema.json`.
- `docs/api/API_INDEX.md`, `API_CONVENTIONS.md`, `PROVENANCE_AND_FRESHNESS.md`, `ML_CONTRACT_BOUNDARY.md`, `FRONTEND_CONSUMPTION.md`, `CONTRACT_VALIDATION.md`.
- `frontend/__mocks__/api/README.md` (REFERENCE).
- Spec §16, §17, §25, §28–§35, §41, §43, §44, §49.

**Non-negotiable rules:**

- **Authority.** The hand-authored `openapi.yaml` is authoritative. A generated document never overwrites it (ADR-0022; departure D-11).
- **Change control** (spec §41): a PR with the reason, the consuming roles tagged, the file versions incremented, the mocks updated in the same change, and two approvals. A change to a referenced schema file counts as a change to `openapi.yaml`.
- **Compatibility** follows the table in `API_CONVENTIONS.md` §2. Schemas are closed; clients follow the tolerant-reader rule; enumerations are extensible only where marked.
- **Errors** are RFC 9457 with `code`, `correlationId` and `errors[]`, and one status, type and title per code. 400 is for data-independent validation, 404 for path resources, 422 for data-dependent problems.
- **Numbers and time:** every number declares `x-agrinex-max-decimal-places`; instants are UTC; dates are IST calendar dates; rounding happens only at the points in `API_CONVENTIONS.md` §4.1.
- **Prices:** every price carries the `PriceQuote` provenance, and every price-serving response carries `sourceStatus`. Calculations use `Rs./Quintal` only, with no conversion (ADR-0008).
- **ML:** `decisionBasis.mlContributed` is always false. Forecasts appear only in `getMarketForecast` (OPTIONAL_ML).
- **Scope and exposure:** no Phase 2 paths; no internal keys, hashes, run ids or secrets in any schema.
- **Mocks** are synthetic, use the envelope, are validated, and stay one coherent data set.

**Relevant ADRs:** 0005, 0006, 0008, 0010, 0012, 0014, 0015, 0016, 0017, 0019, 0020, 0022, 0023, 0024, 0025, 0026, 0028, 0029, 0030, 0032. PROPOSED: 0035, 0036, 0037.

**API and schema references:**

- Prices and sources: `PriceQuote`, `SourceSyncStatus`, `DailyPriceSeries`.
- Comparison: `MarketComparisonResponse`, `CompareRequest`, `SimulateRequest`, `SellingOptionResponse`, `ExcludedMarket`, `DecisionBasis`, `Scenario`, `TransportAssumptionsApplied`.
- Errors: `ApiErrorResponse`, `ErrorCode`, `FieldErrorCode`.
- Trends and forecasts: `TrendResponse`, `ForecastResponse`, `ForecastSeries`, `ForecastPointResponse`, `ForecastOutput`.
- Shared: `PageMetadata`, `AdminAuditAction`.

**Forbidden assumptions:**

- Endpoints, fields, codes, limits or roles that no source defines.
- The outcome of RD-040 to RD-044: an erasure endpoint, an origin-validation rule, an overflow code, the label wording, deactivation operations.
- A unit field from the source (none exists), or any unit conversion.
- Confidence percentages, or model metadata the model did not produce.
- That "alphabetical" means a locale collation: the contract uses code-point order (`API_CONVENTIONS.md` §5).

**Dependencies:**

- architecture (module ownership: `x-agrinex-module`);
- database (precisions and closed vocabularies);
- backend (implements);
- frontend and qa (consume);
- design (states and copy);
- cloud (headers, rate-limit configuration).

---

## 3. agrinex-architecture

**Authoritative documents:**

- Spec §5, §9–§14, §27, §35, §39, §45.
- All of `docs/architecture/`, entered through `ARCHITECTURE_INDEX.md`.
- `docs/architecture/DECISION_LOG.md`: rules 1–7, the index, and the departures D-01 to D-13.
- `docs/REQUIRES_DECISION.md`.
- `docs/database/TABLE_OWNERSHIP.md`.

**Non-negotiable rules:**

- **System shape:** a modular monolith; each module owns its repositories (spec §12, §27).
- **Dependencies:** only the edges approved in ADR-0013 item 3. The graph stays acyclic, and geography, commodity and transport stay leaves.
- **Integrations:** the government API is called only by the ingestion module, server-side (spec §31 RULE 1). ML meets the backend only through the database (ADR-0030).
- **New decisions:** a new architectural decision becomes a PROPOSED decision-log entry and needs explicit human approval (CLAUDE.md §G). The substance of an APPROVED or DECIDED entry is never edited (rule 1).
- **Precedence:** when an architecture document disagrees with a binding entry, the entry governs and the document is corrected (rule 7).
- **Departures** from the spec quote the spec text and are listed in the departures table (rule 5).

**Relevant ADRs:** 0001, 0002, 0005, 0010, 0011, 0013, 0014, 0017, 0018, 0020, 0021, 0030, 0031, 0032. PROPOSED: 0035, 0036, 0037.

**API and schema references:**

- `x-agrinex-module` on every operation (spec §30 module mapping);
- `docs/api/ML_CONTRACT_BOUNDARY.md`.

**Forbidden assumptions:**

- Microservices or Kubernetes.
- New module edges, such as those pending under RD-038 and RD-042.
- Synchronous calls to ML.
- Replacing or supplementing data.gov.in.
- Treating a PROPOSED entry as binding, or approving a decision.

**Dependencies:** database, api-contract, backend, cloud.

---

## 4. agrinex-backend

**Authoritative documents:**

- Spec §12–§17, §20, §27–§35, §48.
- `CURRENT_TECHNOLOGY_BASELINE.md`.
- Architecture: `COMPONENT_ARCHITECTURE.md`, `RECOMMENDATION_ENGINE.md`, `INGESTION_ARCHITECTURE.md`, `SECURITY_ARCHITECTURE.md`, `RESILIENCE_ARCHITECTURE.md`, `DATA_FLOW.md`.
- Database: `DATA_DICTIONARY.md`, `TABLE_OWNERSHIP.md`, `INDEXES_AND_CONSTRAINTS.md`.
- `contracts/` and `docs/api/`.
- The government-data evidence file, for source formats.

**Non-negotiable rules:**

- **Library calls:** check current documentation through Context7 before writing any library call (rules 5–6). New dependencies are justified when their area is scaffolded (ADR-0034; rule 15).
- **Arithmetic:**
  - exact decimal arithmetic, rounded only at the named points;
  - the haversine double is converted through its shortest decimal form (`BigDecimal.valueOf`) and then rounded half-up to 2 decimal places (`API_CONVENTIONS.md` §4.1).
- **Comparison engine:**
  - observation selection (ADR-0010);
  - scoring and tie-breaks (ADR-0024);
  - trend windows (ADR-0025);
  - What-If (ADR-0026);
  - no-data semantics (ADR-0023).
- **Ingestion:**
  - orchestration (ADR-0011);
  - identity, duplicates and conflicts (ADR-0009);
  - counters (ADR-0012);
  - IST and date formats (ADR-0007);
  - unit validation (ADR-0008).
- **Security and operations:**
  - auth (ADR-0016);
  - cookie, `Origin` check and rate limits (ADR-0017);
  - Redis fallback (ADR-0018);
  - alerts (ADR-0019);
  - admin boundary and audit (ADR-0020);
  - retention purges (ADR-0029).
- **Boundaries and testing:**
  - module boundaries (`TABLE_OWNERSHIP.md` §5);
  - every implementation has tests;
  - §48 calculations use hand-computed fixtures.

**Relevant ADRs:** 0003, 0005 to 0026, 0028, 0029, 0030, 0034. PROPOSED: 0035, 0036, 0037.

**API and schema references:**

- every operation, with `x-agrinex-module`, `x-agrinex-audit-action` and `x-agrinex-rate-limit`;
- the error catalogue;
- the rounding rules.

**Forbidden assumptions:**

- Implementing work blocked by an open item before a human decides it:
  - the RD-038 paths;
  - the final RD-039 constant and its fixtures;
  - RD-040 erasure;
  - RD-042 origin validation;
  - RD-043 overflow handling;
  - RD-044 deactivation.
- Calling data.gov.in outside the ingestion module.
- Reading another module's tables.
- Serving forecasts from an ML service.
- Adding a JWT library (ADR-0016).
- Logging secrets or request bodies.
- Carrying prices forward or filling gaps.
- Using the non-government CSV for anything except validation tests.

**Dependencies:** architecture, database, api-contract, qa, cloud.

---

## 5. agrinex-cloud

**Authoritative documents:**

- Spec §28 (TLS, headers), §42 items 27–28, §45.
- `DEPLOYMENT_ARCHITECTURE.md`, `CONTAINER_ARCHITECTURE.md`, `SECURITY_ARCHITECTURE.md`, `RESILIENCE_ARCHITECTURE.md`.
- `CURRENT_TECHNOLOGY_BASELINE.md`: Docker Engine 29.x, Compose v5.x, and the images listed there.

**Non-negotiable rules:**

- **Topology:** one cloud VM running Docker Compose with Nginx and certbot (ADR-0021).
- **Edge:**
  - one public HTTPS origin, with only ports 80 and 443 published;
  - `/actuator/` answers 404;
  - HTTP redirects to HTTPS, except the ACME path;
  - security headers, and HSTS once TLS is verified (ADR-0017).
- **Secrets:** only in the root `.env` file (mode 600, never committed). `infra/.env.example` lists names only (ADR-0020).
- **Credentials:** no published credentials. The bootstrap admin password is removed after first start (ADR-0028).
- **Redis:** password-protected, with no persistence (ADR-0018).
- **Rate limits:** the limit values are deployment configuration (ADR-0017).
- **ML:** `agrinex-ml` is an optional batch job with no port (ADR-0030), and deploying without it must not break core flows.

**Relevant ADRs:** 0002, 0017, 0018, 0020, 0021, 0028, 0030, 0033. Open: RD-037, RD-002.

**API and schema references:**

- the internal health checks (`/actuator` inside the Docker network; the frontend's `/api/health`);
- the routing table in ADR-0017 item 1.

**Forbidden assumptions:**

- Kubernetes or microservices.
- Publishing the database, Redis or backend ports.
- Committing `.mcp.json` or `.env`.
- Choosing a provider, region or domain (RD-037 is a human decision).
- Copying any secret into documentation.

**Dependencies:** architecture, backend, frontend, qa. Security sign-off (SR-10, SR-13).

---

## 6. agrinex-database

**Authoritative documents:**

- Spec §14, §15, §20, §31–§35.
- `docs/database/`: `DATABASE_INDEX.md`, `DATABASE_ARCHITECTURE.md`, `DATA_DICTIONARY.md`, `ERD.md`, `INDEXES_AND_CONSTRAINTS.md`, `TABLE_OWNERSHIP.md`, `SEED_AND_REFERENCE_DATA.md`.
- The government-data evidence file and `docs/data/raw/` (evidence only).

**Non-negotiable rules:**

- **Migrations:** the schema changes only through versioned Flyway migrations, and an applied migration is never edited.
- **Identity:** `business_key` is unique (spec §31 RULE 6). Primary keys are `uuid` with `gen_random_uuid()`.
- **Types and vocabularies:** column precisions are exact as specified, and closed vocabularies are enforced by CHECK constraints.
- **Ownership:** module ownership and the prohibited-access list apply (`TABLE_OWNERSHIP.md` §4–§5).
- **Data protection:** personal-data classes as recorded, and retention purges (ADR-0029). `forecasts` is owned by price, and the ML role is restricted (ADR-0030).
- **Seeds and demo data:** seeds come only from identified sources. Demo data is an exact export of real observations (ADR-0028).

**Relevant ADRs:** 0005 to 0015, 0019, 0020, 0024, 0026, 0028, 0029, 0030.

**API and schema references:**

- the contract precisions mirror the column types (`API_CONVENTIONS.md` §4);
- `AdminAuditAction` mirrors `ck_admin_audit_events_action`.

**Forbidden assumptions:**

- Inventing tables, columns or indexes, or defining Phase 2 tables (deferred).
- Seeding from `Agriculture_price_dataset.csv`.
- Widening the estimate columns before RD-043 is decided.
- Extending the audit vocabulary before RD-044 is decided.
- Correcting the `markets.canonical_name` note without approval: the note says admins may rename markets, but no decision provides a rename (cross-document finding in the Task 8.5 report).

**Dependencies:** architecture, backend, api-contract, qa.

---

## 7. agrinex-design

**Authoritative documents:**

- Spec §6, §10 (freeze checklist), §16, §17, §21 (hero flow), §25, §29, §31–§34, §37, §46, §49; F1.2, F1.21, F1.22.
- `FRONTEND_ARCHITECTURE.md`.
- `docs/api/FRONTEND_CONSUMPTION.md`: the §2 screen-state matrix and the mandatory-content table.
- `docs/api/PROVENANCE_AND_FRESHNESS.md` §5 (states and messages).
- Role file 02, once created (ADR-0027).

**Non-negotiable rules:**

- **Design authority** (ADR-0027):
  - role file 02 is the design source of record; Stitch and other tools produce drafts;
  - an approval is recorded in SSOT index area 5 by the Document Owner;
  - production screens are built only from approved designs, and prototypes are labelled.
- **Audience:** mobile-first; Marathi, Hindi and English; accessibility.
- **Labels and claims:**
  - the mandatory labels and disclaimers;
  - "Latest reported price", never "live" or "real-time";
  - the §49 claim labels.
- **States:** every state is designed. That includes:
  - loading, empty and no data;
  - stale data and the outage banner;
  - no market in range;
  - insufficient trend data;
  - forecast unavailable;
  - each error class.

**Relevant ADRs:** 0004, 0015, 0016 (registration fields), 0017 (transparent refresh), 0019 (alert events), 0023 (no-data states), 0024 (reason codes), 0025 (trend), 0026 (What-If), 0027, 0028 (demo).

**API and schema references:**

- the 8 `ReasonCode` values; `FreshnessStatus`; `TrendDirection`; `noDataReason`; `sourceStatus`; `transportAssumptions`;
- the error codes, for copy mapping;
- `ForecastResponse.available`.

**Forbidden assumptions:**

- Features or screens not in the spec, or Phase 2 screens.
- Forecasts shown inside comparisons, or confidence percentages.
- Fixing the transport-label wording before RD-041 is decided.
- Erasure or deactivation admin screens before RD-040 and RD-044 are decided.
- The copy "FRESH — reported today" (spec §46) without approval: FRESH also covers yesterday under the default threshold (§16).
- Showing synthetic mock names as real.

**Dependencies:** frontend, api-contract, qa. Human approval of designs.

---

## 8. agrinex-frontend

**Authoritative documents:**

- `FRONTEND_ARCHITECTURE.md`.
- `docs/api/FRONTEND_CONSUMPTION.md`.
- `docs/api/API_CONVENTIONS.md`: §4 numbers, §6 errors, §7 security, §8 headers.
- `contracts/` and `frontend/__mocks__/api/` with its README.
- `CURRENT_TECHNOLOGY_BASELINE.md`: Next.js 16.3.8, React 19.3.0, Node.js 24, Tailwind CSS 4.3.x, Recharts 3.10.x, MSW 2.15.x.
- Role files 02 and 03, once created.

**Non-negotiable rules:**

- **Data source:** only the approved contract, or mocks that match it.
- **Tokens and sessions:**
  - the access token lives in memory only, and refresh calls are serialized;
  - on 401, refresh once and retry;
  - after `REFRESH_TOKEN_INVALID`, retry operations that allow anonymous access without the token.
- **Configuration:** no secret in `NEXT_PUBLIC_*`. Server-side rendering does not call the backend (ADR-0017 item 4).
- **What-If** (`FRONTEND_CONSUMPTION.md` §3):
  - exact decimal arithmetic;
  - re-evaluate only `PRICE_ADVANTAGE` and `LOWER_TRANSPORT`;
  - send only the values the user changed;
  - call simulate on slider release;
  - use the server response as the new base.
- **Robustness:** handle unknown enumeration values; fall back to `name` when a localized name is missing.
- **Screens:** production screens only from approved designs. Mock mode is labelled as a prototype.

**Relevant ADRs:** 0004, 0015, 0016, 0017, 0022, 0023, 0024, 0026, 0027, 0034.

**API and schema references:**

- `AuthResponse`, `ApiErrorResponse`, `MarketComparisonResponse`, `TrendResponse`, `MarketDetailResponse`, the alert schemas, `ForecastResponse`;
- the mock envelope.

**Forbidden assumptions:**

- Computing composite scores.
- Showing forecasts outside the optional section.
- Keeping tokens in Web Storage.
- Inventing fields.
- Showing `detail` to farmers.
- Hard-coding "₹8/km" before RD-041 is decided.

**Dependencies:** design (approved screens), api-contract, qa.

---

## 9. agrinex-qa

**Authoritative documents:**

- Spec §10 (freeze checklist), §36 (experiments), §44 (fixtures and mocks), §48 (acceptance criteria).
- `TESTING_ARCHITECTURE.md`.
- `docs/api/CONTRACT_VALIDATION.md`.
- `contracts/` and the mocks.
- `RECOMMENDATION_ENGINE.md` and `DATA_FLOW.md`.

**Non-negotiable rules:**

- **Every implementation has named tests** (rule 12).
- **Calculations:** transport, net realization and scoring use hand-computed fixtures (§27, §48).
- **Contract tests** cover every operation, including errors, with decimal-place checks (ADR-0022 item 1). Every mock is validated against its declared operation and status.
- **Scope:** OPTIONAL_ML operations stay out of the Phase 1 suites.
- **Experiments:** Experiments 1–3 follow §36.
- **Integrity:**
  - tests are never weakened or skipped, and failures are reported as they are;
  - synthetic fixtures never enter a deployment database (ADR-0028);
  - integration inputs are the recorded evidence captures (`TESTING_ARCHITECTURE.md` §4.4).

**Relevant ADRs:** 0012, 0022, 0023, 0024, 0025, 0026, 0028, 0030, 0034. Open: RD-039 (distance fixtures).

**API and schema references:**

- the validation checks A to E and D1 to D17 in `CONTRACT_VALIDATION.md`;
- the error catalogue;
- the mock envelope.

**Forbidden assumptions:**

- Finalizing distance fixtures before RD-039 is decided.
- Presenting synthetic data as real.
- Choosing test tools without the ADR-0034 justification.

**Dependencies:** every other skill.
