# AgriNex — Phase 1 Local Readiness Report

**Status:** REFERENCE (2026-10-18). This report gives the state of Phase 1, deployment excluded, after a fresh verification of code, data and documents. It supersedes the status sections of `TASK_12_PHASE1_CLOSURE_REPORT.md` (2026-10-16), which remains the record of the Task 12 closure. Evidence: `docs/qa/PHASE1_LOCAL_TEST_REPORT.md` and `docs/data/PHASE1_DATA_AND_ML_READINESS_REPORT.md`.

## 1. Verdict

**READY WITH EXPLICIT LIMITATIONS** for local use.

- Every Phase 1 feature except deployment works on real data through the documented local setup. The automated suites pass, and the critical journeys work in all three languages on phone and desktop widths.
- Phase 1 is **not frozen** and **not deployed**. The freeze needs deployment (F1.25, Task 13, excluded here), Experiments 1 and 2 on real use, and the owner's freeze decision (spec §10).
- Phase 2 must not start (HARD RULE 1).

## 2. Phase 1 features (spec §7)

| # | Feature | Status | Evidence |
|---|---|---|---|
| F1.1 | Comparison without registration | Done | E2E comparison runs anonymously |
| F1.2 | Marathi / Hindi / English | Done | E2E in MR and HI; sweep of every route in 3 languages; 378 keys each |
| F1.3 | State → District → Locality, no address | Done | Sweep; 36 districts, 358 talukas; distance from the district centroid (ADR-0039) |
| F1.4 | Commodity, variety and grade | Done | Wizard "More options"; backend suite |
| F1.5 | Quantity in kg or quintal | Done | E2E (kg and quintal) |
| F1.6 | Configurable radius, default 150 km | Done | Distance choices; independent recheck |
| F1.7 | Latest reported prices (current dataset first) | Done | ADR-0010 precedence; 11 / 11 independent comparisons |
| F1.8 | Multi-market comparison table | Done | E2E; sweep |
| F1.9–F1.11 | Transport, gross value, net realization | Done | Hand-computed unit fixtures; 11 / 11 independent comparisons |
| F1.12 | Ranking with reason codes | Done | Unit tests cover all 8 codes; recheck of ranks |
| F1.13 | Price-only vs net winner | Done | Callout; `rankingsDisagree` checked in 11 scenarios |
| F1.14 | "Why recommended?" | Done | E2E; sweep |
| F1.15 | Trends, 7 and 30 days | Done | 15 / 15 independent trend checks; E2E |
| F1.16 | Freshness indicators | Done | 594 / 594 badges against report dates |
| F1.17 | Source attribution | Done | Footer on every price screen (sweep) |
| F1.18 | What-If simulator | Done | E2E; sweep |
| F1.19 | Price alerts | Done | E2E (create, manage); evaluated after each ingestion |
| F1.20 | Admin data-health dashboard | Done | E2E; admin health API matches the database |
| F1.21 | Mobile-first | Done | E2E at 375 px; sweep at 375 px |
| F1.22 | Accessibility (WCAG 2.1 AA, core flows) | Done (automated checks) | Lighthouse accessibility 100 on home and results; no manual screen-reader audit |
| F1.23 | Error and stale-data handling | Done | Outage tests; government-key test with banner; stale note (ADR-0052 item 8) |
| F1.24 | Evaluation instrumentation | Done | Every comparison recorded; `scripts/run_evaluation.sql` (tested). Results need real use |
| F1.25 | Docker-based reproducible deployment | **Not done** (excluded: Task 13) | Compose runs the database and Redis only |

Functional requirements FR-01 to FR-20 are covered by the same evidence. FR-16 and FR-17 are covered by the outage and ingestion tests, FR-18 by the admin API, FR-19 by alert evaluation after each run, and FR-20 by the RBAC probes and the backend suite. FR-21 to FR-25 belong to Phase 2.

## 3. Remaining non-deployment blockers

None blocks local use. These items remain:

| Item | Needs | Blocks |
|---|---|---|
| RD-051 / ADR-0053 (PROPOSED): offline ML evaluation before deployment | **Owner's approval** | Experiment 3 evidence only. ML is optional and does not block Phase 1 |
| RD-002: `.mcp.json` credentials | Owner: convert to `${…}` references and rotate exposed values. The `.gitignore` part is done | Committing or pushing `.mcp.json` |
| data.gov.in key exposed in Task 12 instructions | Owner: rotate (`docs/security/TASK_12_SECURITY_REVIEW.md` §7) | Should precede any deployment |
| RD-037 hosting | Owner | Deployment (Task 13) |
| No Git commit exists | Owner (commits are reserved to the owner) | Reproducible deployment |

## 4. Decisions

- **Implemented in this verification:**
  - ADR-0033 item 3: the root `.gitignore` excludes `.mcp.json`, on the owner's instruction;
  - the Experiment 1 query of spec §43, following ADR-0045 item 8. Its percentile method is an ASSUMPTION stated in the script.
- **Requiring the owner's approval:**
  - ADR-0053 (RD-051);
  - the actions of section 3.
- **For the owner's review (no change made):** in the local test comparisons, the recommended (rank-1) market had the highest estimated net realization in 50 of 87 cases. When rank 1 and the price-only winner differed, rank 1 usually had the lower estimate. This follows from the ADR-0024 weights and is allowed by ADR-0045. If real use shows the same pattern, the weights (runtime configuration) may need review against FR-10.
- **Hard rules:**
  - HARD RULE 1 (no Phase 2 before the freeze) is respected.
  - HARD RULE 2 (deterministic engine end to end) is met locally. Spec §35 criteria 2 and 3 (deployed, after the freeze) still stop ML from running on real data.

## 5. Known limitations

- **Not deployed;** LCP and load re-measurement on the VM are pending. Slow 3G LCP misses the target (Fast 3G is the decided profile, ADR-0052 item 10).
- **Freshness depends on a running backend.** No FRESH market exists after two days without ingestion.
- **Coverage gaps:** Sindhudurg and Mumbai Suburban have no market in the feed; 10 crops lack unit evidence; 477 markets stay PROVISIONAL.
- **Not human-reviewed:** the Marathi and Hindi copy has had no native-speaker review, and the security sign-offs are AI reviews under delegation (ADR-0050).
- **Console noise:** anonymous page loads log one expected 401 in the browser console (Lighthouse Best Practices 96).
- **Test data in the local database:** 136 recorded comparisons: 8 from the owner's own use on 2026-10-16, the rest from tests and checks (39 on 2026-10-18). There are 16 FARMER accounts, all test accounts. Evaluation figures must use real use after deployment.

## 6. Run and verify locally

Follow `docs/LOCAL_DEVELOPMENT_GUIDE.md`:
1. Terminal 1: database and Redis.
2. Terminal 2: backend on 8081.
3. Terminal 3: frontend on 3000.
4. Open http://localhost:3000 and follow the 10-minute demonstration script (guide §8).
5. Run the full QA of guide §10.
