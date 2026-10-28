# AgriNex — Task 8.5 Human Decision Packet (RD-038 to RD-043)

**Status:** REFERENCE, a decision aid prepared on 2026-10-04 (Task 8.5). **Nothing in this packet is approved.** A recommendation is a proposal. It becomes binding only when a human approves it and the decision is recorded in `docs/architecture/DECISION_LOG.md`, with the matching item moved to Resolved in `docs/REQUIRES_DECISION.md` (CLAUDE.md §O; decision-log rule 3).

**Outcome (2026-10-04, Task 8.6):** the Human Project Owner chose the recommended option for every item and approved the decisions recorded as ADR-0035 to ADR-0041. The security sign-off is ADR-0042. This packet is now a historical record; the decision log governs.

**Sources read:** CLAUDE.md; `docs/00_SSOT_INDEX.md`; the master specification ("spec"); `docs/architecture/*`; `docs/database/*`; `docs/api/*` and `contracts/*`; the decision log (ADR-0001 to ADR-0037); `docs/REQUIRES_DECISION.md`; the Task 8 report. The security analysis behind several items is in [`docs/security/TASK_8_5_SECURITY_REVIEW.md`](../security/TASK_8_5_SECURITY_REVIEW.md).

**Labels:** [SSOT REQUIREMENT] master specification · [ARCHITECTURAL DECISION] binding decision-log entry or approved architecture/database document · [VERIFIED TECHNICAL FACT] current official documentation or reproducible computation · [DATA EVIDENCE] the verified government-data evidence · [ASSUMPTION] reversible interpretation · [REQUIRES_DECISION] open item · [CONFLICT] incompatible statements in authoritative sources.

## Summary

| Item | Decision needed | Recommendation | Evidence supports it? | Blocks | Decide before Task 9? |
|---|---|---|---|---|---|
| RD-038 | Module paths for sync status, freshness thresholds and commodity availability | Option 1: read-only ports (PROPOSED ADR-0035 as revised) | Yes | Backend: `sourceStatus`, freshness classification in price and alert, `listCommodities` filter | No (backend only) |
| RD-039 | Earth radius for haversine | Option 1: 6 371.0088 km (PROPOSED ADR-0036) | Yes | Distance function and its §48 fixtures | No (backend and QA only) |
| RD-040 | Account-erasure mechanism | Option C: in-application operator procedure without HTTP (revises the Task 8 draft, which proposed A) | Partly: the evidence excludes B and favours C over A, but A against C is also a risk-appetite choice | Implementing erasure | Recommended: option A would add an admin screen |
| RD-041 | Wording of the mandatory transport label | Option A: show the applied rate | Yes, but departs from the literal spec text, so it needs explicit authorization | Final label copy | **Yes** (copy in role file 02) |
| RD-042 | Server-side enforcement of district-centroid origins | Option A: server check through a new recommendation → geography edge | Yes | Compare origin validation; security sign-off | Recommended: affects one error state |
| RD-043 | Estimates that exceed the stored precision | Option C: a new data-dependent 422 code (revises the Task 8 lean toward B) | Yes | First `selling_options` migration; compare error handling | Recommended: affects one error state |
| RD-044 (new, found in Task 8.5) | Account deactivation mechanism and audit | Decide together with RD-040, using the same mechanism; extend the audit vocabulary | Yes for the audit part; the mechanism follows RD-040 | Implementing deactivation; incident response | No, unless an admin screen is chosen |

**Impact matrix** (YES = the decision changes this area's artifacts or behavior):

| Item | Backend | Database | API | Frontend | UX | Security | Testing |
|---|---|---|---|---|---|---|---|
| RD-038 | YES | no | no (wording only) | no | no | no | YES |
| RD-039 | YES | no | no (wording only) | no | no | no | YES |
| RD-040 | YES (A, C) | no | YES (A only) | YES (A only) | YES (A only) | YES | YES |
| RD-041 | no | no | no (wording only) | YES | YES | no | YES |
| RD-042 | YES | no | YES (A: validation; B: request shape) | minor (A) / YES (B) | minor | YES | YES |
| RD-043 | YES | no (C) / YES (B) | YES (A, C) | YES | YES | no | YES |
| RD-044 | YES | YES (audit vocabulary) | YES (audit enum; operations if A) | YES (A only) | YES (A only) | YES | YES |

---

## RD-038 — Module paths for cross-module data in public responses

### QUESTION

How do the price, recommendation, alert and commodity modules obtain three pieces of data that binding entries require in their responses, when the approved dependency graph gives them no path?

- (a) The current-dataset sync status. It is owned by ingestion (`ingestion_runs`).
- (b) The freshness thresholds. They are owned by recommendation (`recommendation_weights`).
- (c) Commodity availability, meaning recent observations in coverage. That data is owned by price, but `GET /api/v1/commodities` belongs to the commodity module.

### CURRENT STATE

- The contract fixes the fields and their meaning. The path is open.
  - `sourceStatus` appears on all five price-serving operations.
  - `freshnessStatus` appears on every price.
  - `listCommodities` lists only commodities with recent observations in coverage.
- PROPOSED ADR-0035 (revised 2026-10-04 after the Task 8 data review) proposes:
  - read-only ports, declared by the consuming module and implemented by the owner over existing edges;
  - comparisons and simulations pass the thresholds of their own weights version into price's selection call.
- Task 8 assumes nothing about the path. Assumption T8-A2 interprets "recent observations in coverage" (ADR-0006).
- Blocking status: BLOCKING for the named backend work only. Not blocking for the contract or Task 9.

### EVIDENCE

- [ARCHITECTURAL DECISION] ADR-0022 item 9: price responses carry provenance, and every price-serving response carries `lastSuccessfulSyncAt` and whether the latest current-dataset run failed.
- [ARCHITECTURAL DECISION] ADR-0013 item 3: only the listed edges are approved. They include:
  - ingestion → price, ingestion → alert;
  - recommendation → price;
  - price → commodity;
  - alert → price.

  The graph is acyclic, and geography, commodity and transport are leaves.
- [ARCHITECTURAL DECISION] ADR-0010 item 2: the stale days bound the selection window, so the thresholds decide which observation is selected, not only its label.
- [ARCHITECTURAL DECISION] ADR-0026 item 3: a simulation uses the original run's weights version.
- [SSOT REQUIREMENT] Spec §27: each module owns its repositories, with no cross-module table access. §20 places the freshness days in `recommendation_weights`. §30 maps `/api/v1/commodities/*` to the commodity module.
- [ARCHITECTURAL DECISION] `docs/database/TABLE_OWNERSHIP.md` §5: price "never reads ingestion tables". `COMPONENT_ARCHITECTURE.md` §4.1: no other dependency without a new decision.
- [CONFLICT] This is a gap rather than two contradicting texts: binding entries require data that the approved graph cannot deliver. Each direct edge that would solve it closes a cycle:
  - price → ingestion, with ingestion → price;
  - price → recommendation, with recommendation → price;
  - alert → ingestion, with ingestion → alert;
  - commodity → price, with price → commodity.

### OPTIONS

1. **Read-only ports** (ADR-0035 as revised).
   - Price declares `SourceSyncStatusPort` (implemented by ingestion) and `FreshnessPolicyPort` (implemented by recommendation; the active version, for market detail, trends, alerts and the commodity filter).
   - Commodity declares `CommodityAvailabilityPort` (implemented by price).
   - Comparisons pass their pinned thresholds as parameters.
2. **Push model.** Ingestion and recommendation push state into price on change and at startup.
3. **Mixed.** Add the acyclic edges recommendation → ingestion and alert → recommendation, plus ports for the rest.
4. **Move ownership.** Thresholds leave `recommendation_weights`, and the commodity selector is served by another module.

### RECOMMENDATION

**Option 1.** Supported by the evidence:

- it keeps ADR-0013's static graph acyclic and unchanged;
- every table stays read only by its owner (spec §27);
- it needs no schema change;
- pinned thresholds keep `selling_options.freshness_status` consistent with `recommendation_runs.weights_id` (Experiment 1 audit trail).

### CONSEQUENCES

| Option | Technical | Product |
|---|---|---|
| 1 | Three small interfaces, plus a threshold parameter on price's selection call. Package-dependency tests stay valid | None visible |
| 2 | Price keeps copies of other modules' state, so values can be stale. Needs startup ordering and possibly a new column or table (a migration) | The outage banner could lag behind the real sync state |
| 3 | Amends ADR-0013 with two edges and still needs ports for price (a) and (b) and for commodity (c) | None |
| 4 | Contradicts spec §20 and §30, so it needs an authorized spec change | None |

### FILES AFFECTED (on approval of option 1)

- `docs/architecture/DECISION_LOG.md`: ADR-0035 status set to APPROVED with the approver.
- `docs/REQUIRES_DECISION.md`: RD-038 moved to Resolved.
- `docs/architecture/COMPONENT_ARCHITECTURE.md` §4.1 and §6, and `docs/database/TABLE_OWNERSHIP.md` §4: the ports recorded as permitted runtime paths.
- `docs/api/PROVENANCE_AND_FRESHNESS.md` §4, `docs/api/API_INDEX.md` §8, and the `listCommodities` description in `contracts/openapi.yaml`: references to the pending item replaced. No contract shape change and no mock change.
- `docs/00_SSOT_INDEX.md`: area 2 status line.

### HUMAN DECISION REQUIRED

Approve ADR-0035 as revised, or choose another option. The ADR-0001 delegation does not cover this item.

---

## RD-039 — Earth radius for haversine distances

### QUESTION

Which Earth radius does the transport module's haversine distance use?

### CURRENT STATE

- The contract fixes the rounding (`docs/api/API_CONVENTIONS.md` §4.1). The double result is converted to its shortest decimal form, then `distanceKm` is rounded half-up to 2 decimal places.
- The radius is open. PROPOSED ADR-0036 recommends 6 371.0088 km.
- The synthetic mocks and the Task 8 coherence check use the proposed value, and the mock README says so.

### EVIDENCE

- [SSOT REQUIREMENT] FR-06, §16, §21 and §32 require haversine. §47 Q6 labels distances "approximate". §48 requires hand-computed fixtures to match 100 %.
- [DATA EVIDENCE] None. The spec contains no pair of coordinates and distance from which a radius could be derived.
- [VERIFIED TECHNICAL FACT] The WGS 84 defining parameters (a = 6 378 137 m, 1/f = 298.257 223 563) give the mean radius R1 = (2a + b)/3 = 6 371 008.771 m. The value is the same as the GRS 80 mean radius.
- [VERIFIED TECHNICAL FACT] On the Java 25 baseline, `BigDecimal.valueOf(double)` uses `Double.toString`, the shortest round-trip decimal (OpenJDK 25 API documentation).
- Sensitivity: between the two synthetic district centroids of the mocks, 98.49 km at 6 371.0088 becomes 98.60 km at 6 378.137. The difference between 6 371.0088 and 6 371 is about 0.00014 %, so it changes a 2-decimal result only near a rounding boundary.
- [CONFLICT] None. This is a gap: no source names the radius.

### OPTIONS

1. **6 371.0088 km.** The WGS 84 / GRS 80 mean radius.
2. **6 371 km.** The rounded value.
3. **6 378.137 km.** The equatorial radius.
4. **An ellipsoidal distance** (for example Vincenty).

### RECOMMENDATION

**Option 1.** Supported by the evidence: it is fully traceable to the official ellipsoid and fixes server results and fixtures reproducibly. Option 2 is equally neutral for the product but less traceable.

### CONSEQUENCES

| Option | Technical | Product |
|---|---|---|
| 1 | Mocks and checker unchanged; fixtures use this constant | None |
| 2 | Mocks regenerated; 2-decimal changes only at rounding boundaries | None |
| 3 | Distances 0.11 % longer. Can flip the 50 km `SUITABLE_DISTANCE` reason and radius membership near the edges | Slightly overstated transport costs |
| 4 | Departs from "haversine" (FR-06), so it needs an authorized spec change; more complex | Slightly more accurate distances that are still labelled approximate |

### FILES AFFECTED

- `docs/architecture/DECISION_LOG.md` (ADR-0036 status) and `docs/REQUIRES_DECISION.md` (RD-039 resolved).
- `docs/architecture/RECOMMENDATION_ENGINE.md` §4.4: record the constant.
- `docs/api/API_CONVENTIONS.md` §4.1 and `docs/api/CONTRACT_VALIDATION.md`: replace the pending-item notes.
- Only if options 2–4 are chosen: the mock generator constant, the regenerated mocks and the coherence check.

### HUMAN DECISION REQUIRED

Approve ADR-0036, or choose another radius.

---

## RD-040 — Account-erasure mechanism

### QUESTION

ADR-0029 decides that Phase 1 erasure is "an audited admin procedure". Is it carried out as:

- an authenticated HTTP admin operation;
- an in-application procedure an operator runs without HTTP; or
- a database runbook?

### CURRENT STATE

- Contract 1.0.0 contains no erasure operation. It was removed in Task 8 so that the contract would not decide the question.
- PROPOSED ADR-0037 drafts option A.
- The database already supports erasure:
  - the foreign-key cascades and SET NULL on searches (ADR-0029);
  - the `ACCOUNT_ERASED` audit action;
  - the approved admin → auth edge.
- Nothing is implemented. Until a decision, no approved path can fulfil an erasure request.

### EVIDENCE

- [ARCHITECTURAL DECISION] ADR-0029 item 2: "In Phase 1, erasure is an audited admin procedure. A self-service endpoint is not in the specification and is not added." Its consequences add: "Erasure is documented in the operations runbook."
- [ARCHITECTURAL DECISION] ADR-0013 item 3, as recorded in `TABLE_OWNERSHIP.md` §4: admin → auth exists for "Admin bootstrap and the audited account-erasure procedure".
  - So the procedure is application code in the admin module, calling auth's interface.
  - The other use in the same row, the first-admin bootstrap (ADR-0028 item 2), is an in-application action that does not use HTTP.
- [ARCHITECTURAL DECISION] ADR-0020 items 1 and 7 list the audited admin endpoints. Erasure is not among them.
- [ARCHITECTURAL DECISION] `admin_audit_events.action` includes `ACCOUNT_ERASED`. `actor_user_id` is NOT NULL, so an ADMIN must be the recorded actor (`docs/database/DATA_DICTIONARY.md`).
- [ARCHITECTURAL DECISION] ADR-0016: phone numbers are not verified in Phase 1, so erasure is also the remedy when someone registers another person's number.
- [SSOT REQUIREMENT] §20 (`users.is_active`), §34 (alert cascade), §39 (no OTP); no erasure endpoint in §30.
- [CONFLICT] A soft one: an HTTP operation (option A) would extend ADR-0020's endpoint list, which a later entry (ADR-0029) does not do explicitly.
- Security analysis: finding SR-05 of the security review.

### OPTIONS

- **A. Admin HTTP operation**, as drafted in ADR-0037: `POST /api/v1/admin/account-erasures` with phone number, request reference and reason; 204; FARMER accounts only; audited with the number masked.
- **B. Database runbook.** An operator with database write access deletes the user, the foreign keys cascade, and the operator writes the audit row by hand.
- **C. In-application operator procedure without HTTP.**
  - An operator with server access runs a one-off command mode of the backend.
  - The admin module performs the erasure through auth's interface.
  - It writes `ACCOUNT_ERASED` with a named, active ADMIN as actor and masked details.
  - It is documented in the operations runbook.

### RECOMMENDATION

**Option C**, with moderate confidence. Evidence:

- It matches ADR-0029's "procedure" and runbook wording.
- It uses the approved admin → auth path, like the non-HTTP bootstrap in the same approval row.
- It needs no change to ADR-0020's endpoint list.
- It adds no remote, irreversible deletion capability (CLAUDE.md rule 16).

The evidence excludes B, which bypasses the module interface and relies on a hand-written audit row. A against C is partly a risk-appetite choice: A is easier to operate, C has a smaller attack surface. **This revises the Task 8 draft:** ADR-0037 proposes A, and it would be redrafted to the chosen option before approval.

### CONSEQUENCES

| Option | Technical | Product and security |
|---|---|---|
| A | New contract operation (minor version), mocks, an admin screen, and safeguards no source defines yet (low rate limit, confirmation, reference and reason, masked audit). Amends ADR-0020 | A stolen admin access token (valid up to 15 minutes) or a compromised admin account could erase farmers irreversibly |
| B | No code, but privileged database access for a routine request; the audit row is hand-written; the module boundary is bypassed | Error-prone; weakest audit |
| C | A new runtime entry point (an operator command mode) needs its own decision. No API or UI change. Needs server access, which operators already have (ADR-0021). The actor is named by the operator | No remote attack surface; Task 9 unaffected |

### FILES AFFECTED

- **All options:**
  - `docs/architecture/DECISION_LOG.md` (ADR-0037 redrafted to the chosen option, then approved);
  - `docs/REQUIRES_DECISION.md`;
  - `docs/architecture/SECURITY_ARCHITECTURE.md` and the operations runbook (not created yet).
- **A:** `contracts/openapi.yaml`, a new request schema, mocks, `docs/api/*`, role file 02 (an admin screen), and ADR-0020 (endpoint list).
- **C:** `docs/architecture/COMPONENT_ARCHITECTURE.md` (operator entry point) and `DEPLOYMENT_ARCHITECTURE.md` (how to run it).
- **B:** runbook only.

### HUMAN DECISION REQUIRED

Choose A, B or C, and give the security sign-off (rule 16). Decide RD-044 at the same time.

---

## RD-041 — Wording of the mandatory transport label

### QUESTION

The spec mandates the label "Estimated — based on ₹8/km assumption. Actual costs will vary." The rate behind an estimate can differ from 8. Should the label show the literal "₹8/km", the rate actually applied, or both?

### CURRENT STATE

- Every comparison and simulation returns `transportAssumptions.appliedRatePerKm` and `ratePerKmSource` (`CONFIGURED` or `USER_OVERRIDE`).
- `docs/api/FRONTEND_CONSUMPTION.md` §2 marks the wording as open.
- No screen or copy exists yet. Role file 02 is not created.

### EVIDENCE

- [SSOT REQUIREMENT] §32 (line 1458) says the transport cost "must always show" the text "**Estimated** — based on ₹8/km assumption. Actual costs will vary." §49 (lines 2128 and 2138) repeats "based on ₹8/km assumption".
- [SSOT REQUIREMENT] The same §32 lists `ratePerKm` under "Configurable Parameters (Admin-Managed in `transport_assumptions` table)" and says "Farmer can override `ratePerKm` and `maxDistanceKm` in the simulator." §17 gives a ₹3–₹20/km slider.
- [ARCHITECTURAL DECISION] ADR-0020 item 1: admins update transport assumptions as new versions. ADR-0026: What-If overrides. ADR-0027: copy belongs to role file 02.
- [CONFLICT] Inside the spec: §32's literal label against §32's own configurability and override. Both statements have the same authority, so precedence cannot settle it.

### OPTIONS

- **A.** Show the applied rate: "Estimated — based on ₹<appliedRatePerKm>/km assumption. Actual costs will vary." The text is identical to the spec at the default rate.
- **B.** Keep the literal text and never let the rate differ from 8.
- **C.** Keep the literal text and add a second line with the applied rate.

### RECOMMENDATION

**Option A.** Supported by the evidence:

- §32 itself makes the rate configurable and overridable, so a fixed "₹8/km" would be a false statement whenever the rate differs, against §49's claims discipline;
- A reproduces the spec wording exactly at the default.

Because A departs from the literal text, it needs explicit human authorization, recorded in the decision log as a departure or an authorized interpretation. The spec itself is not edited (CLAUDE.md §E).

### CONSEQUENCES

| Option | Technical | Product |
|---|---|---|
| A | A localized copy template with a rate placeholder; no API change | The label is always true |
| B | Admin rate updates and the What-If rate slider must be disabled or ignored | Contradicts §32, §17, ADR-0020 and ADR-0026 |
| C | Two strings to localize | Two different rates on one screen confuse low-literacy users (§6 personas), and the first line is false |

### FILES AFFECTED

- `docs/architecture/DECISION_LOG.md`: a decision entry, plus a departure-table row if A is treated as a departure.
- `docs/REQUIRES_DECISION.md`.
- Role file 02 (Task 9).
- `docs/api/FRONTEND_CONSUMPTION.md` §2.
- The `TransportAssumptionsApplied` description in `contracts/recommendation.schema.json` (wording only).
- Localization strings, later.

### HUMAN DECISION REQUIRED

Choose the wording rule. If you choose A, authorize the departure from the literal §32 and §49 text.

---

## RD-042 — Server-side enforcement of district-centroid origins

### QUESTION

Must the backend verify that a comparison's `originLat` and `originLon` are a district centroid? If so, by which module path?

### CURRENT STATE

- The contract accepts any in-range coordinates with up to 6 decimal places. The compare description names RD-042.
- The frontend is told to send the centroid from `listDistricts`.
- `simulate` returns the stored origin to any holder of the `recommendationId` (security finding SR-02).
- No backend check is designed.

### EVIDENCE

- [SSOT REQUIREMENT] §28: "PII minimization | No home address collected; district centroid only | Frontend + Backend".
- [SSOT REQUIREMENT] FR-06 ("between farmer's district centroid and each candidate market") and §47 Q6.
- [ARCHITECTURAL DECISION] ADR-0023 item 1: the origin is the selected district's centroid, sent as `originLat` and `originLon`.
- [ARCHITECTURAL DECISION] ADR-0029 item 2: on erasure, searches are kept and only unlinked, because "Searches hold only district-centroid origins (§28)". That premise holds only if centroids are enforced.
- [ARCHITECTURAL DECISION] `selling_searches.origin_lat` and `origin_lon` are PERSONAL and kept indefinitely (`DATA_DICTIONARY.md`; ADR-0029). `SECURITY_ARCHITECTURE.md` lists the control under "Frontend and backend".
- [ARCHITECTURAL DECISION] ADR-0013 item 3: there is no recommendation → geography edge, and geography is a leaf. `TABLE_OWNERSHIP.md` notes that such an edge was avoided when market ownership was decided.
- [CONFLICT] The approved premise is relied on by ADR-0029 and assigned to the backend by §28. Neither the contract nor any approved module path enforces it.

### OPTIONS

- **A.** Server check through a new direct recommendation → geography edge.
  - The origin must equal a stored district centroid at 6 decimal places.
  - Otherwise 400 `VALIDATION_FAILED`, with `INVALID_VALUE` on `originLat` and `originLon`.
  - The edge is acyclic, and geography stays a leaf.
- **A′.** The same check through a port. Rejected: geography would depend on recommendation's port package and lose its leaf property.
- **B.** Send `originDistrictId` and let the server look up the centroid. This changes ADR-0023 item 1 and departs from the request fields in spec §16 and §21.
- **C.** Accept the residual risk: only the frontend enforces centroids. Record the acceptance in the security review.

### RECOMMENDATION

**Option A.** Supported by the evidence:

- §28 assigns the control to the backend too;
- ADR-0029's anonymization depends on it;
- the existing contract fields stay unchanged.

The new edge needs its own decision-log entry (it amends ADR-0013's edge list) and the security sign-off.

### CONSEQUENCES

| Option | Technical | Product and security |
|---|---|---|
| A | One lookup per comparison; a new approved edge; contract text and one new 400 mock | No change for the real frontend, which sends `listDistricts` values. Precise locations can never be stored, and SR-02 drops to negligible |
| B | Request shape change, departure entry, wizard change | Cleaner semantics (no coordinate matching), but larger change |
| C | None | Precise coordinates sent by a buggy or modified client are stored indefinitely and survive anonymization. ADR-0029's premise becomes false |

### FILES AFFECTED

- **A:**
  - a new decision-log entry for the edge and the rule;
  - `COMPONENT_ARCHITECTURE.md` §4.1 and §6, `TABLE_OWNERSHIP.md` §4, and `SECURITY_ARCHITECTURE.md` §4.2;
  - `contracts/openapi.yaml` (compare description) and `contracts/recommendation.schema.json` (`originLat` and `originLon` descriptions);
  - `docs/api/API_CONVENTIONS.md`, a new 400 mock and the checker;
  - `docs/REQUIRES_DECISION.md`.
- **B:** additionally a spec departure entry, a request schema change, `FRONTEND_CONSUMPTION.md`, and an entry superseding ADR-0023 item 1.
- **C:** a risk acceptance in the security review, and `docs/REQUIRES_DECISION.md`.

### HUMAN DECISION REQUIRED

Choose A, B or C. Approve the new decision entry, and give the security sign-off.

---

## RD-043 — Estimates that exceed the stored precision

### QUESTION

How should a schema-valid but extreme compare quantity be handled when its estimates do not fit `numeric(14,2)`?

### CURRENT STATE

- The contract accepts quantity > 0 up to 999 999 999.999, the precision of `selling_searches.quantity`.
- The estimates are stored as `numeric(14,2)`, at most 999 999 999 999.99.
- An overflow would surface as a database error and a 500, which the contract does not describe. The compare description names RD-043.
- Simulations are bounded (100–5 000 kg) and are not persisted, so they are unaffected.

### EVIDENCE

- [ARCHITECTURAL DECISION] ADR-0023 item 1: quantity > 0 "with no further limits, because the specification states none for compare".
- [ARCHITECTURAL DECISION] `DATA_DICTIONARY.md`:
  - `selling_searches.quantity` is `numeric(12,3)`;
  - `price_observations.modal_price` is `numeric(14,2)`;
  - `selling_options.estimated_*` are `numeric(14,2)`.
- [SSOT REQUIREMENT] §28: errors use the error format and never expose stack traces. §29 describes failure handling. Neither expects a 500 for client input.
- [VERIFIED TECHNICAL FACT] Arithmetic: 999 999 999.999 QUINTAL × ₹3 000 ≈ 3.0 × 10¹² rupees, above the column maximum.
  - Covering every combination of column-bounded inputs would need about 28 digits.
  - Browsers parse JSON numbers as IEEE 754 doubles, exact for integers only up to 2⁵³ − 1 (MDN, `Number.MAX_SAFE_INTEGER`). Amounts therefore keep paise precision only up to about 9 × 10¹³ rupees.
- [CONFLICT] ADR-0023 item 1 (no limit) against the approved column precision.

### OPTIONS

- **A.** Cap `quantity`. This amends ADR-0023 item 1 and needs a product limit that no source gives.
- **B.** Widen the estimate columns before the first migration. Fixed widths must cover extreme input combinations, and browser precision is still lost for absurd values.
- **C.** Reject results that do not fit the stored precision with a new data-dependent 422 code. Nothing is persisted. `ErrorCode` is extensible, so the addition is additive.

### RECOMMENDATION

**Option C.** Supported by the evidence:

- it keeps ADR-0023 item 1 and the approved column design unchanged;
- it turns an undefined 500 into a documented error.

**This revises the Task 8 register entry**, which leaned toward B before the precision analysis above.

### CONSEQUENCES

| Option | Technical | Product |
|---|---|---|
| A | Request schema change; an entry superseding ADR-0023 | An invented limit could reject a legitimate large quantity |
| B | Database design change (`DATA_DICTIONARY.md`); no API change | Absurd amounts shown imprecisely |
| C | New error code, catalogue rule, mock and checker rule; a check before persisting | One extra error state with copy (Task 9) |

### FILES AFFECTED (option C)

- `contracts/error-response.schema.json` (new code, status 422 and title).
- `contracts/openapi.yaml` (`ComparisonUnprocessable` and the compare description).
- `docs/api/API_CONVENTIONS.md` §6.1 and `FRONTEND_CONSUMPTION.md` §2.
- A new mock, and checker rule D14.
- `docs/architecture/DECISION_LOG.md` (decision entry) and `docs/REQUIRES_DECISION.md`.

### HUMAN DECISION REQUIRED

Choose A, B or C. For C, approve the code name and title as part of the contract change (spec §41).

---

## RD-044 — Account deactivation mechanism and audit (new item, found in Task 8.5)

### QUESTION

Who deactivates and reactivates a user account (`users.is_active`), by what mechanism, and how is it audited?

### CURRENT STATE

- The contract already handles inactive accounts:
  - login answers 401 `INVALID_CREDENTIALS`;
  - refresh refuses the token and revokes its family;
  - alert and admin operations answer 401.
- No operation or procedure sets `is_active`.
- The closed audit vocabulary, enforced by a database CHECK, has no deactivation action.

### EVIDENCE

- [SSOT REQUIREMENT] §20: `users.is_active`.
- [ARCHITECTURAL DECISION] ADR-0029 item 2: "Admins are deactivated, never deleted".
- [ARCHITECTURAL DECISION] ADR-0020 item 6: admin changes are audited from a closed vocabulary.
- [ARCHITECTURAL DECISION] `ck_admin_audit_events_action` in `INDEXES_AND_CONSTRAINTS.md`.
- [REQUIRES_DECISION] No entry defines the mechanism.

### OPTIONS

The same family as RD-040:

- **A.** Admin HTTP operations.
- **B.** A database runbook.
- **C.** An in-application operator procedure.

Options A and C need new audit actions (for example `ACCOUNT_DEACTIVATED` and `ACCOUNT_REACTIVATED`), which change the CHECK list and the contract's `AdminAuditAction` enumeration.

### RECOMMENDATION

Decide together with RD-040 and use the same mechanism. Extending the audit vocabulary follows from ADR-0020 item 6, because deactivating an admin is an admin change that must be audited. The action names are a proposal.

### CONSEQUENCES

- **Until decided:** a compromised or departing admin account can only be deactivated by unaudited manual SQL (security finding SR-03).
- **Once decided:** the existing contract rules cut access at once (refresh, alert and admin operations).

### FILES AFFECTED

- `docs/architecture/DECISION_LOG.md`, `docs/REQUIRES_DECISION.md`.
- `docs/database/DATA_DICTIONARY.md` and `INDEXES_AND_CONSTRAINTS.md` (audit vocabulary).
- `contracts/openapi.yaml` (`AdminAuditAction`, and operations if A is chosen).
- The runbook.

### HUMAN DECISION REQUIRED

Choose the mechanism (with RD-040), and approve the audit-vocabulary extension.

---

## Appendix — Cross-document consistency check (Task 8.5, Part E)

**Checked:** the SSOT, the decision log, the architecture documents, the database documents, the API contract and its documentation, the new security review, and the ML documentation. **Not corrected silently:** only the ML documentation was corrected, as Task 8.5 Part D authorized. Every other finding below awaits a human decision.

| Topic | Result | Evidence and action |
|---|---|---|
| data.gov.in source semantics | Consistent | Both resource IDs are identical in all nine documents that cite them. The non-government CSV is evidence-only everywhere (ADR-0005). The government API is called only by ingestion (spec §31 RULE 1) |
| Source outage behavior | Consistent | Spec §29, ADR-0005, ADR-0011, `RESILIENCE_ARCHITECTURE.md`, `INGESTION_ARCHITECTURE.md`, `PROVENANCE_AND_FRESHNESS.md` and `SourceSyncStatus` agree: stored data is served with its true dates; no zero, carried-forward or interpolated price |
| Price units | Consistent | `Rs./Quintal` only in calculations, with no conversion. ADR-0008 settles spec §32's "normalizedModalPrice" |
| Provenance | Consistent | ADR-0022 item 9, ADR-0010 item 5, the database columns and `PriceQuote` match |
| Freshness | Consistent, with one copy issue | The spec §16 rules equal the contract's. The demo line in spec §46, "FRESH — reported today", is narrower than FRESH under the default threshold (today or yesterday). **Action:** Task 9 copy must not claim "today" for every FRESH price. The spec is not edited |
| Recommendation behavior | Consistent, with one interpretation | Spec §16, ADR-0023, ADR-0024, `RECOMMENDATION_ENGINE.md` and the contract agree. The tie-break "alphabetically first market name" is applied as code-point order (`API_CONVENTIONS.md` §5, §9 item 12). **Action:** confirm the interpretation; it matters only for exact ties |
| Transport assumptions | Consistent, except RD-041 | Defaults, formula and versioning agree. The spec's `cost_per_km` and the API's `ratePerKm` are mapped in `DATA_DICTIONARY.md`. The label wording is RD-041 |
| Distance calculations | Consistent, except RD-039 | Haversine everywhere; the rounding is defined; the radius is RD-039 |
| Authentication | Consistent | Spec §28, ADR-0016, ADR-0017, `SECURITY_ARCHITECTURE.md`, `FRONTEND_ARCHITECTURE.md` and the contract agree, including the refresh limit of 10 per minute. Review pending (SR-00) |
| Account lifecycle | Gaps | Deactivation mechanism (RD-044), erasure (RD-040), and the behavior of compare and simulate with a removed account's token (SR-04) |
| Location and origin | CONFLICT (RD-042) | Spec §28 ("Frontend + Backend"), ADR-0029's anonymization premise and `SECURITY_ARCHITECTURE.md` require centroid-only origins. The contract and the module graph do not enforce them |
| Quantity handling | Consistent, except RD-043 | KG and QUINTAL with exact conversion; simulator 100–5 000 kg. ADR-0023 conflicts with the column precision (RD-043) |
| ML optionality | Corrected | `ML_ARCHITECTURE.md` §4.8–§4.9 and the deployment-diagram label "port 8000" were aligned with ADR-0030 (Part D). Container, data-flow, resilience, security, database and API documents already agreed |
| Deterministic baseline | Consistent | Spec §16, HARD RULE 2, ADR-0024, the contract's `decisionBasis` (`mlContributed: false`) |
| Phase 1 / Phase 2 boundary | Consistent | No Phase 2 operation; Phase 2 tables DEFERRED; Phase 2 screens later (ADR-0031, ADR-0032) |
| Market rename | Document correction pending | `DATA_DICTIONARY.md` (`markets.canonical_name`) says admins "may correct it (ADR-0014)". ADR-0014, ADR-0020 and the closed audit vocabulary provide no rename, and the contract has none. Under decision-log rule 7 the database document must be corrected. **Action:** approve that correction (or a decision that adds renaming); it was outside Task 8.5's scope |
| Governance text | Outdated reference | CLAUDE.md §M still says "Resolving RD-010 will define the design source of record…", but ADR-0027 resolved RD-010 on 2026-10-03. **Action:** a CLAUDE.md update needs the user's explicit instruction |
| Tooling notes in the SSOT index | Minor | The index says the eight agent files "were empty on 2026-10-02". Still true as dated, but `database-engineer.md` has had an outline since 2026-10-03. Optional refresh |
