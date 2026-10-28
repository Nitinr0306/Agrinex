---
name: agrinex-api-contract
description: AgriNex REST contract work on contracts/openapi.yaml (OpenAPI 3.1, /api/v1) and the JSON Schemas in contracts/. Covers request and response fields, error semantics (RFC 9457 ApiErrorResponse), provenance and freshness, security schemes and cookies, pagination, ML optionality, the deterministic decision basis, the synthetic mocks in frontend/__mocks__/api, and contract change control. Use this skill whenever a task reads, changes, validates or consumes an endpoint, field, error code, header, cookie, schema or mock, or when frontend, backend or QA work needs to know exactly what the API accepts or returns, even if the user only names a screen or a feature.
---

# AgriNex API contract

## Purpose

Keep one authoritative, hand-authored contract that the frontend, backend and QA all build against. Change it only through the Shared Contract Change Rule, and never let it expose database internals or invent behavior.

This skill points to `contracts/` and `docs/api/`. If it disagrees with them or with a binding decision, they win: correct this file.

## When to use

- Reading or proposing any operation, field, enumeration value, error code, header or cookie.
- Writing or reviewing mocks, contract tests, or the contract documentation.
- Answering "what does the API return for X?" for frontend, design, backend or QA work.
- Any contract version or compatibility question.

## Source hierarchy

This follows CLAUDE.md §B:

1. The master specification: §16, §17, §25, §28–§35, §41, §43, §44, §49.
2. Binding decision-log entries.
3. `contracts/openapi.yaml` and the JSON Schemas it references. These are AUTHORITATIVE. `docs/api/` explains them and is corrected wherever it differs.
4. Current official documentation, fetched with Context7, for OpenAPI 3.1, JSON Schema 2020-12 and RFC behavior.

Mocks are REFERENCE material: they must match the contract exactly, but they are never a source of requirements.

## Required reading (READ SSOT FIRST)

1. `CLAUDE.md` §H, then `docs/00_SSOT_INDEX.md` area 4 and `docs/REQUIRES_DECISION.md`.
2. `docs/api/API_INDEX.md`, the hub: files, endpoint inventory, decisions, assumptions.
3. `docs/api/API_CONVENTIONS.md`:
   - versioning and compatibility;
   - formats and precision, and calculation rounding (§4.1);
   - pagination and ordering;
   - the error catalogue;
   - security, headers, and the contract-design choices.
4. `contracts/openapi.yaml`, for the operations in scope, plus `common.schema.json`, `price-observation.schema.json`, `recommendation.schema.json`, `error-response.schema.json` and, for the optional ML track only, `forecast-output.schema.json`.
5. `docs/api/PROVENANCE_AND_FRESHNESS.md`, `docs/api/ML_CONTRACT_BOUNDARY.md`, `docs/api/FRONTEND_CONSUMPTION.md` and `docs/api/CONTRACT_VALIDATION.md`.
6. Decision-log entries:
   - core contract: ADR-0022 (strategy), ADR-0023 to ADR-0026 (comparison, scoring, trends, What-If);
   - data and security: ADR-0008, ADR-0010, ADR-0016, ADR-0017, ADR-0020, ADR-0030;
   - Task 8 decisions: ADR-0035 to ADR-0042;
   - Task 9 final decisions: ADR-0045 (wording; no contract change), ADR-0047 and ADR-0048 (email, login identifiers, `requestPasswordReset`, `confirmPasswordReset`, `confirmEmailVerification`, `resendEmailVerification`, two error codes; contract 1.0.0 revised before its first merge, 53 operations).

## Non-negotiable AgriNex rules

- **Authority.** `contracts/openapi.yaml` is hand-authored and authoritative. A document generated from code only detects drift and never overwrites it (ADR-0022; departure D-11).
- **Change control** (spec §41):
  - a PR with the reason, the consuming roles tagged, the file versions incremented, the mocks updated in the same change, and approval from two consuming roles;
  - a change to a referenced schema file counts as a change to `openapi.yaml`;
  - compatibility follows `API_CONVENTIONS.md` §2.
- **Shape:**
  - OpenAPI 3.1 under `/api/v1`;
  - closed request and response schemas; clients follow the tolerant-reader rule; enumerations are extensible only where marked `x-agrinex-extensible`;
  - every number declares `x-agrinex-max-decimal-places`;
  - instants are UTC; dates are IST calendar dates.
- **Errors** are RFC 9457 `ApiErrorResponse` with `code`, `correlationId` and `errors[]`, and one status, type and title per code.
  - 400 is for data-independent validation, with one documented exception: a comparison origin that is not a stored district centroid (ADR-0039).
  - 404 is for path resources.
  - 422 is for data-dependent problems, including `ESTIMATE_OUT_OF_RANGE` on compare (ADR-0040).
- **Provenance and freshness.**
  - Every price is a `PriceQuote` with full provenance and attribution. Every price-serving response carries `sourceStatus` (ADR-0022 item 9).
  - Prices are never converted, filled, carried forward or interpolated (ADR-0005, ADR-0008).
- **Deterministic baseline.** `decisionBasis.mlContributed` is always false. Rankings come with reason codes, estimates, trend and freshness, never composite scores.
- **ML.** Forecasts appear only in the optional `getMarketForecast` (`x-agrinex-phase: OPTIONAL_ML`), which answers `available: false` when no current forecast exists (ADR-0030). No confidence percentage exists anywhere.
- **Security** (ADR-0016, ADR-0017, ADR-0042):
  - roles per operation;
  - a 15-minute JWT bearer token;
  - the refresh cookie `__Secure-agrinex_refresh` (`HttpOnly; Secure; SameSite=Strict; Path=/api/v1/auth`), with an `Origin` check on refresh and logout;
  - every operation that evaluates a token resolves the account, and an inactive or removed account answers 401;
  - the rate limits of ADR-0017, extended by ADR-0048 (per-identifier and per-account limits on the recovery and verification operations);
  - the password-reset request always answers 202 and never reveals an account; email addresses and tokens are never returned (ADR-0048).

  Security-sensitive changes need human review.
- **Origins.** `compareSellingOptions` accepts only stored district centroids as `originLat` and `originLon` (ADR-0039).
- **Transport label.** The label shows `appliedRatePerKm` (ADR-0038).
- **No HTTP operation** for account erasure, deactivation or reactivation (ADR-0037, ADR-0041). Their audit actions appear in `AdminAuditAction` only.
- **No database exposure:** no business keys, hashes, run ids, profile ids or secrets in any schema.
- **Phase scope.** No Phase 2 paths until after the Phase 1 freeze and RD-012 / RD-025 (ADR-0032).
- **Mocks** are synthetic, carry the `synthetic: true` envelope, match the contract exactly, and describe one coherent data set.

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

- Contract changes that trace to the spec or a binding decision, through spec §41.
- Keeping `docs/api/`, mocks and contract tests consistent with the contract.
- Answering consumer questions with the exact operation, schema and section.
- Registering REQUIRES_DECISION when a needed field, code or rule has no source.

## Forbidden responsibilities

- Inventing endpoints, fields, codes, limits, roles or validation rules.
- Generating the contract from code to overwrite it.
- Exposing internal identifiers or secrets.
- Mixing forecasts into comparisons.
- Adding Phase 2 operations, or HTTP endpoints for operator procedures.
- Merging security-sensitive changes without human review.

## Dependencies on other skills

- agrinex-architecture: module ownership (`x-agrinex-module`).
- agrinex-database: column precisions and closed vocabularies.
- agrinex-backend: implements the contract.
- agrinex-frontend and agrinex-design: consume it.
- agrinex-qa: validates it.
- agrinex-release: compatibility gates.

## Validation expectations

- Validate `openapi.yaml` against the OpenAPI 3.1 schema, and every JSON Schema against the 2020-12 meta-schema.
- Resolve every `$ref`.
- Rerun the semantic rules D1 to D17 in `docs/api/CONTRACT_VALIDATION.md`.
- Validate every mock against its declared operation and status, including headers, decimal places and mock-set coherence.
- Use mutation tests to show that the checks catch broken copies.
- Record each run in `CONTRACT_VALIDATION.md` §4.
- The contract-validation library itself is chosen at scaffolding (ADR-0034).

## Escalation rules

- **A consumer needs a field or behavior with no source:** REQUIRES_DECISION, not a guess.
- **The spec flows disagree with each other** (spec §16, §21, §29, §30): CONFLICT. Report it, then follow an existing decision or register a new item.
- **Auth, cookies, rate limits, account status, personal data:** human security review.

## Output expectations

Report:
- the operations and schemas changed, with the contract file versions;
- the mocks added or changed;
- the validation run results (checks and mutations);
- compatibility classification (additive or breaking);
- the decisions cited;
- open items.
