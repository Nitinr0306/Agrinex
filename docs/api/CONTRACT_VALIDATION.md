# AgriNex — Contract Validation

**Status:** AUTHORITATIVE, part of the API contract area (SSOT index area 4). It records how contract 1.0.0 was validated in Task 8 (2026-10-04) and rerun after the Task 9 final decisions (§2.1), and what the contract tests must check once the code exists (ADR-0022 item 1).

## 1. Method

**Constraint.** The task forbade installing dependencies only to validate the contract. The contract-validation library itself is deferred to scaffolding (ADR-0034). Validation therefore used only what was already present:

- Python 3.13 with PyYAML 6.0.3;
- official reference material downloaded for the check;
- a purpose-written JSON Schema 2020-12 validator.

The validator and the scripts live in the Task 8 session workspace, not in the repository. They can be added to the repository by a later tooling task if the team wants them.

| Element | Detail |
|---|---|
| JSON Schema validator | Purpose-written, implementing the 2020-12 core, applicator, unevaluated, validation, meta-data, format-annotation and content vocabularies, including `$dynamicRef` and annotation-based `unevaluatedProperties` |
| Validator self-test | The official JSON-Schema-Test-Suite, `tests/draft2020-12`, downloaded 2026-10-04: **1,247 of 1,247 applicable cases pass**. 18 cases were not applicable: 13 need remote reference servers (`localhost:1234`), and 5 use ECMA-262 `\p{…}` escapes that Python's `re` lacks, which the contract does not use |
| Official meta-schemas | OpenAPI 3.1 schema iterations `2025-09-15` and `2022-10-07`, the OAS 3.1 dialect and vocabulary (`spec.openapis.org`), and the JSON Schema 2020-12 meta-schema and vocabularies (`json-schema.org`) |
| Contract checker | Sections A to E below, run over `contracts/` and `frontend/__mocks__/api/` |
| Mutation tests | Deliberately broken copies of the contract and the mocks. Each must make a named check fail |

## 2. Results (Task 8 final run, 2026-10-04; the Task 9 final rerun is §2.1)

**106 checks, 0 failures**, and **68 of 68 mutations detected**, after the Task 8 reviews and the Task 8.6 decisions (ADR-0035 to ADR-0042) were applied (§4).

| Section | Check | Result |
|---|---|---|
| A | `openapi.yaml` valid against the OpenAPI 3.1 schema 2025-09-15 | PASS |
| A | Schema Objects valid against the OAS 3.1 dialect (schema-base wrapper) | PASS |
| A | Valid against OpenAPI 3.1 schema-base 2022-10-07 | PASS |
| A | `openapi` 3.1.x; semantic `info.version` | PASS |
| B | Each of the 5 `*.schema.json` files valid against the JSON Schema 2020-12 meta-schema, with `x-agrinex-contract-version` | PASS (5 of 5) |
| C | Every `$ref` resolves | PASS (778 references) |
| C | No unused components or `$defs` (4 deliberate exports named) | PASS |
| D1 | Unique `operationId`; tags, summary, description, module (spec §30), phase, trace and explicit `security` on every operation | PASS |
| D2 | Every operation has a `default` error and a 2xx; `X-Correlation-Id` on every response; errors are `application/problem+json` with `ApiErrorResponse`; 401 carries `WWW-Authenticate`; 429 carries `Retry-After` | PASS |
| D3 | Path parameters match the path templates | PASS |
| D4 | Security and roles per path group (spec §28; ADR-0017; ADR-0022 item 7) | PASS |
| D5 | No Phase 2 path; only the forecast operation is optional ML | PASS |
| D6 | `page`/`size` exactly on operations that return `PageMetadata` + `items` | PASS |
| D7 | Every schema with `modalPrice` requires the ADR-0022 item 9 provenance (except the ML input point) | PASS |
| D8 | Every response that serves price data carries `sourceStatus`, and only those | PASS |
| D9 | No secret or internal field (business key, hashes, API keys, run ids, profile ids); `password` only write-only | PASS |
| D10 | No `nullable`, no Schema Object `example`, only documented `x-agrinex-*` extensions | PASS |
| D11 | Every object schema closed (or marked free-form) | PASS |
| D12 | Every required property defined | PASS |
| D13 | String enumerations UPPER_SNAKE | PASS |
| D14 | Error catalogue: one status, `type` and title per code; responses only allow codes of their status | PASS |
| D15 | Admin writes map one-to-one to the closed audit vocabulary. `ADMIN_BOOTSTRAPPED` (startup), `ACCOUNT_ERASED`, `ACCOUNT_DEACTIVATED` and `ACCOUNT_REACTIVATED` (operator procedures) have no HTTP operation, and no operation may write them (ADR-0028, ADR-0037, ADR-0041) | PASS |
| D16 | Every number schema declares its decimal precision | PASS |
| D17 | Inventory: 49 operations (admin 28, alert 6, auth 4, commodity 3, geography 3, price 3, recommendation 2) | PASS |
| E | Each of the 75 mocks: schema of the declared operation and status, with format and decimal-place assertions; required headers, including the `__Secure-agrinex_refresh` cookie pattern; content type; request body valid (400 mocks: invalid exactly in the named fields, by the schema or by the centroid rule of ADR-0039); query parameters | PASS (75 of 75) |
| E | Mock arithmetic and consistency: estimates by the §32 formula and the rounding rules; ranks; winners and tie-breaks; `rankingsDisagree`; reason-code triggers (ADR-0024); freshness against `reportDate`; dataset and resource pairs; trend windows and the 7-day statistic; INSUFFICIENT_DATA for a report date before the recent window; request echo; simulation echo of `recommendationId` and overrides; `USER_OVERRIDE` exactly with a rate override; a quantity override in KG | PASS |
| E | Required cases present: successful price, no data, stale data, current dataset never synced, recommendation, What-If (radius and quantity overrides), ML available, ML unavailable, validation error, unauthorized (bearer-only and optional-bearer operations, and an inactive account), non-centroid origin, estimate out of range, rate limited, trend; plus a success mock for every operation | PASS |
| E | Cross-file coherence: distances recomputed from the synthetic coordinates (haversine with the ADR-0036 radius; approximate markets at their district centroid); every accepted comparison origin a district centroid (ADR-0039); the estimate-out-of-range mock really exceeding `numeric(14,2)` at the known prices (ADR-0040); every market within a comparison's radius listed as an option or as excluded; the no-market-in-range case free of nearer markets; error-page totals equal to the run's `errorCount`; comparison trends equal to the trend mock; forecast training date not after the stored data | PASS |

**Mutation tests** (68 of 68 detected). Representative examples:

- **Structure:** a misspelled Operation field, a parameter without `in`, a non-string `info.version`, a broken `$ref`, an invalid schema keyword value.
- **Semantics:**
  - a duplicate `operationId`, a missing default response, a missing `X-Correlation-Id`, an undeclared path parameter;
  - an admin operation made anonymous, a Phase 2 path;
  - provenance or `sourceStatus` removed, an exposed business key, `nullable`, an open request schema, an undefined required property, a lower-case enumeration value, a wrong error status, a missing audit action, a number without precision.
- **Mocks:**
  - wrong net realization, ranks out of order, inconsistent winners, a missing reason code;
  - an extra property, a missing attribution, a wrong freshness, ML contribution claimed, a 3-decimal price;
  - a resource id mismatched to its dataset, a simulation ranked by score;
  - missing `Retry-After` or `WWW-Authenticate`, a forecast "unavailable" carrying predictions, an error code with the wrong status;
  - a validation error without field errors, a 400 mock with a valid request, an inconsistent trend statistic, an unordered series;
  - an unknown request field, a mock not marked synthetic, an invalid UUID, an invalid date;
  - `USER_OVERRIDE` without the override, a simulation echoing another `recommendationId`, a quantity override echoed in QUINTAL, a trend defined although the recent window is empty;
  - an approximate market away from its district centroid, an in-range market missing from a comparison, a trend differing between files, an error-page total differing from the run, a forecast trained past the stored data, a required scenario file removed, an operation without a success mock;
  - the old refresh-cookie name, an HTTP erasure operation, a deactivation action missing from the vocabulary, a comparison accepted from a non-centroid origin, a centroid origin in the non-centroid 400 mock, an estimate error for a quantity that fits, an estimate error without field errors, and an estimate error on simulate.

**Limitations, stated honestly:**

- The validator is purpose-written. It passes the official suite, but it is not one of the libraries the team will choose at scaffolding (ADR-0034).
- `format` was asserted only for `date`, `date-time`, `uuid`, `uri-reference` and `email`.
- Patterns were evaluated with Python `re`; the contract's patterns use only syntax common to ECMA-262 and Python.
- No code exists, so no request or response from a real server was validated.
- The mock distances use the Earth radius approved in ADR-0036.
- [ASSUMPTION] Contract 1.0.0 had not been merged or shared with consumers when the Task 8.6 decisions changed it, so the changes are part of 1.0.0 rather than a new version. The spec §41 increment rule applies from the first merged version.

### 2.1 Task 9 final rerun (2026-10-04, ADR-0045 to ADR-0048)

The Task 9 final decision lock changed the contract (ADR-0047, ADR-0048): `registerFarmer` requires `email`; `AuthenticatedUser` gains `emailVerified`; `login`'s `identifier` names the mobile number or the email of the same account; four operations are added (`requestPasswordReset`, `confirmPasswordReset`, `confirmEmailVerification`, `resendEmailVerification`); `ErrorCode` gains `PASSWORD_RESET_TOKEN_INVALID` and `EMAIL_VERIFICATION_TOKEN_INVALID` (422). Three mocks were updated and eight added.

- **Method.** The Task 8 checker lives in the Task 8 session workspace, not in the repository, so it could not be rerun. A reduced checker was written for this rerun, using only Python 3.13 and PyYAML 6.0.3 (no dependency installed). It implements every JSON Schema keyword the contract uses (`$ref`, `type`, `properties`, `required`, `additionalProperties`, annotation-based `unevaluatedProperties`, `allOf`, `anyOf`, `if`/`then`/`else`, `not`, `enum`, `const`, `pattern`, length and range bounds, `items` and array bounds, `uniqueItems`, the formats `date`, `date-time`, `uuid`, `email` and `uri-reference`, and `x-agrinex-max-decimal-places`). It reruns A (OpenAPI 3.1, semantic version), B (schema-file versions), C (every `$ref` resolves), D1, D2, D4 (roles imply Bearer, 401 and 403), a rate-limit rule (a rate-limited operation declares 429), D9, D11, D14 and the D17 inventory, and E for every mock (operation, method and path, status, required headers and their patterns, content type, body schema, request body schema, Bearer header on Bearer-only operations, a success mock for every operation).
- **Result:** **2,218 checks, 0 failures**; inventory **53 operations** (admin 28, alert 6, auth 8, commodity 3, geography 3, price 3, recommendation 2); **83 mocks**. **14 of 14** targeted mutations of the changed parts were detected (for example: registration without `email`, a user without `emailVerified`, an unknown request field, a malformed token, a wrong status or title for the new codes, a verification error carrying the reset code, a missing `Authorization`, a missing cookie-clearing header, a body on a 202, a non-write-only `newPassword`, a role-restricted operation without 403, a catalogue entry removed).
- **Not rerun:** the validator self-test against the official JSON-Schema-Test-Suite, the official OpenAPI meta-schema check, and the cross-file arithmetic and coherence rules of section E (none of the changed files carries prices or estimates). Two documented exceptions were configured, as in Task 8: shared fragments closed by `unevaluatedProperties: false` in the schemas that compose them (`AdminMarketFields`), and the non-centroid 400 mock, which is schema-valid and invalid only by the ADR-0039 centroid rule.
- [ASSUMPTION, same as above] Contract 1.0.0 still has not been merged or shared with consumers (the repository has no commit), so these changes are part of 1.0.0, including the new required request property, which would otherwise be a breaking change (API_CONVENTIONS.md §2). The spec §41 increment rule applies from the first merged version.

## 3. Contract tests required at scaffolding

[ARCHITECTURAL DECISION, ADR-0022 item 1; TESTING_ARCHITECTURE §4.2.] The library is chosen and justified at scaffolding (ADR-0034). It must support OpenAPI 3.1 and JSON Schema 2020-12, including `unevaluatedProperties`, `if`/`then` and relative external `$ref`s between the contract files.

| Test | Owner (§40) | Rule |
|---|---|---|
| Backend integration tests | Backend, QA | Validate real requests and responses of every operation, including error responses, against `contracts/openapi.yaml`, with decimal-place checks (`x-agrinex-max-decimal-places`) |
| Frontend mock tests | Frontend, QA | Validate every file in `frontend/__mocks__/api/` against its declared operation and status (spec §44 "must match exactly") |
| Lint | QA | Rerun the semantic rules D1 to D16 on every contract change |
| Drift check | Backend | A document generated from code (GENERATED) is compared with the contract and never written over it (ADR-0022; departure D-11) |
| Change control | All | Every change to `contracts/` updates the file versions and the mocks in the same change, with the §41 approvals |
| Optional ML | Backend, ML | `getMarketForecast` (`x-agrinex-phase: OPTIONAL_ML`) is left out of the Phase 1 suites and the Phase 1 freeze, and is tested only once the ML track starts (ADR-0030; HARD RULE 2) |

## 4. Rerun log

| Date | Event | Result |
|---|---|---|
| 2026-10-04 | Initial validation of contract 1.0.0 | 75 checks passed; 46 of 46 mutations detected |
| 2026-10-04 | Security, QA, ML/frontend and data/database reviews applied: erasure operation removed (RD-040); spec names restored (`observations`, the §35 forecast fields, `…Response` schema names); slug pattern aligned with ADR-0014; mock data set made coherent; checker extended with cross-file coherence, override echo and the trend-window rule | 103 checks passed; 60 of 60 mutations detected; validator self-test rerun: 1,247 of 1,247 applicable cases |
| 2026-10-04 | Task 8.6 decisions applied (ADR-0035 to ADR-0042): `__Secure-agrinex_refresh` with a header pattern; account resolution on token use; centroid-only origins; `ESTIMATE_OUT_OF_RANGE` on compare; two operator-procedure audit actions; three new mocks; checker and mutation tests extended | 106 checks passed; 68 of 68 mutations detected |
| 2026-10-04 | Task 9 final decisions applied (ADR-0047, ADR-0048): email at registration, `emailVerified`, email-or-phone login, email verification and password recovery operations, two error codes; 3 mocks updated, 8 added (§2.1) | Reduced checker: 2,218 checks passed; 14 of 14 targeted mutations detected; 53 operations; 83 mocks |
| 2026-10-18 | Contract 1.1.0: `getHistoricalCoverage` and its schemas added (ADR-0054); mock `admin-ingestion-historical-coverage.200.json` added | Frontend contract tests: 196 of 196 (every operation has a success mock; every mock validates); backend `ContractCoverageIntegrationTest`: implemented operations equal the contract's 54 |
