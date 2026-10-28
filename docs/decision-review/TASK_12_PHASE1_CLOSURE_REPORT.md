# AgriNex — Task 12 Phase 1 Closure Report

**Status:** REFERENCE (2026-10-16). For the current state see `PHASE1_LOCAL_READINESS_REPORT.md` (2026-10-18); since this report, the Experiment 1 query exists and the `.mcp.json` ignore rule is in place. This report records the state of Phase 1 at the end of Task 12, before deployment (Task 13). It decides nothing on its own: the decisions are ADR-0050 to ADR-0052 in `docs/architecture/DECISION_LOG.md`, taken under the Human Project Owner's delegation of 2026-10-15. Every figure below was measured on the local stack on 2026-10-15/16, using real government data in the normal local database.

## 1. Verdict

**Phase 1 is not complete and is not frozen.** Spec §10 orders Phase 1 as: development → internal testing → staging deployment → evaluation experiment → freeze. Development and internal testing are done. The staging deployment (§42 item 28, Task 13), the evaluation experiment (§42 item 29, §36) and the formal freeze have not happened.

- **Pre-deployment Phase 1 (§42 items 2–27): complete.** Everything that can be built and verified without deployment works end to end on real Maharashtra data, with the limitations of section 5.
- **GO for Task 13 (deployment)**, once the owner has completed the actions of section 6. Rotating the key and committing the work come first.
- **NO-GO for the Phase 1 freeze and for Phase 2** (HARD RULE 1), until deployment, Experiments 1 and 2, and the deployment-dependent items below are done.

Spec §10 freeze checklist:

| Item | Status | Evidence |
|---|---|---|
| **Data** | | |
| Current API integrated and paginated | MET | Two live runs, 3 pages each, every record accounted for |
| Historical API, at least 60 days | MET | 366 report dates, 2025-10-15 to 2026-10-15 |
| Normalized names; units verified | MET with limitations | V10–V12; 10 crops without unit evidence stay rejected (ADR-0052 item 2); 477 PROVISIONAL markets are hidden from farmers (item 11) |
| Duplicate prevention | MET | Repeated CURRENT run: 0 new rows; unique business key |
| Freshness on every observation | MET | 612 of 612 options classified correctly against `reportDate` |
| Ingestion audit per run | MET | `ingestion_runs` and `ingestion_errors`; admin health matches the database |
| 6-hour scheduler | MET | Scheduled runs at 00:00 IST on 2026-10-16 (CURRENT and HISTORICAL, SUCCESS) |
| **Backend** | | |
| JWT auth (register, login, refresh, logout) | MET | Backend suite; 23 of 23 live account checks |
| Geography, commodity and market price endpoints | MET | Backend suite; E2E |
| Comparison ranking; net realization; reason codes | MET | 11 of 11 comparisons match an independent recomputation; unit fixtures |
| What-If | MET | E2E |
| Alerts evaluated after every ingestion | MET | Log line "Alerts evaluated" per run; backend suite |
| Admin health API accurate | MET | Run counters, latest report dates and last success match the database exactly |
| `ApiErrorResponse` only, no stack traces | MET | 20 of 20 security probes |
| OpenAPI in `contracts/openapi.yaml` | MET | Contract tests in the backend suite |
| **Frontend** | | |
| Wizard, results, winner callout, reasons, trends (7 and 30 days), What-If, badges, attribution, admin sync status, 3 languages, 375 px | MET | 195 unit tests; 10 E2E journeys |
| **Resilience** | | |
| Government API failure (key disabled) | MET | Live: run FAILED, stored data unchanged, comparison served with the §29 banner; `SourceOutageIntegrationTest` |
| Stale data warning | MET | ADR-0052 item 8; tests |
| No-data empty state | MET | Unit tests; E2E (unsupported unit) |
| **Deployment** | | |
| `docker compose up -d` starts all services from `.env.example` | PENDING (Task 13) | Compose runs PostgreSQL and Redis only; backend and frontend have no images yet |
| Migrations on startup | MET | Flyway V1–V12 validated at start |
| `/actuator/health` | MET | Readiness, liveness and overall health |
| Demo data on first boot | PENDING (Task 13) | ADR-0028: bootstrap admin only; real data instead of fixtures |
| **Evaluation** | | |
| Experiment 1 executable from the database | PENDING | `scripts/run_evaluation.sql` is not created (SSOT index area 6); the local database holds only test comparisons (ADR-0052 item 9) |
| Results in an evaluation report | PENDING | Needs real use after deployment; Experiment 2 needs 14 days of deployed ingestion |

Spec §48:

| Criterion | Result |
|---|---|
| Comparison p95 < 1 s at 10 users | Met: p95 165 ms and 182 ms, 0 errors (60 s runs). Not met with the dev server and browser on the same 8 GB laptop: p95 1,512 ms. Must be re-measured on the target VM |
| Ingestion completeness ≥ 95 % per run | Met by the closure runs: CURRENT 98.6 %, scheduled HISTORICAL 98.6 %, D1 backfill 96.3 %. The earlier year backfill was 93.59 %, mainly because of the 11,337 records RD-050 rejected, now resolved; validation rejections (ADR-0008) still count against long backfills |
| 0 duplicates on repeated ingestion | Met |
| Freshness accuracy 100 % | Met (612 of 612) |
| Transport, net realization, rankingsDisagree, reason codes | Met (unit fixtures; 11 independent scenarios) |
| Admin health exact match | Met |
| Government API failure | Met (live and automated) |
| Language switch, 0 untranslated strings | Met (E2E in MR and HI; 378 keys in all three catalogues) |
| 375 px, no horizontal scroll | Met (E2E) |
| LCP < 3 s on mobile 3G (§27) | Met on the chosen profile, Chrome DevTools Fast 3G (home 1,567 ms, compare 1,624 ms). Not met on Slow 3G (5.0–6.7 s) |

## 2. The twelve decisions

| # | Disposition | Record |
|---|---|---|
| D1 | `commodity_source_aliases` (V12), 20 spellings on DMI code evidence; 8,978 records stored | ADR-0051; RD-050 resolved |
| D2 | 10 crops stay unverified; admin process recorded | ADR-0052 item 2 |
| D3 | Re-examined, re-tested, signed off as an AI review | ADR-0052 item 3; security review §6–§7 |
| D4 | ADR-0049 DECIDED | ADR-0052 item 4 |
| D5 | RD-048 closed; ADR-0007 item 2 confirmed | ADR-0052 item 5 |
| D6 | 378 keys re-reviewed; 4 corrections; no native-speaker review | ADR-0052 item 6; role file 02 §10.3 A-09 to A-14 |
| D7 | Trilingual account emails, link once; 23 of 23 live checks | ADR-0052 item 7; RD-049 resolved |
| D8 | Stale note only when every displayed option is stale | ADR-0052 item 8 |
| D9 | 10,316 load-test comparisons deleted after export | ADR-0052 item 9 |
| D10 | Fast 3G acceptance profile; Slow 3G reported; CSS inlining blocked by the CSP | ADR-0052 item 10; security review S12-3 |
| D11 | 242 markets merged on same-delivery evidence; 477 stay PROVISIONAL | ADR-0052 item 11 |
| D12 | Key treated as compromised; owner must rotate it | ADR-0052 item 12; security review §7 |

## 3. Files and migrations changed in the closure

All changes are uncommitted; the repository has no commit yet.

- **Migration:** `backend/src/main/resources/db/migration/V12__commodity_source_aliases.sql` (new table, new nullable column, 20 seed rows).
- **Backend code:**
  - new: `commodity/domain/CommoditySourceAliasEntity.java`, `commodity/repository/CommoditySourceAliasRepository.java`;
  - changed: `commodity/CommodityApi.java`, `commodity/service/CommodityService.java`, `ingestion/pipeline/ReferenceLookups.java`, `ingestion/pipeline/UnitDeduplicator.java`, `price/ObservationCandidate.java`, `price/domain/PriceObservationEntity.java`, `price/service/ObservationLoader.java`, `auth/email/AuthEmails.java`.
- **Backend tests:**
  - new: `auth/email/AuthEmailsTest.java`;
  - changed: `ReferenceDataSeedIntegrationTest.java`, `DatabaseSchemaIntegrationTest.java`, `ingestion/pipeline/RecordValidatorTest.java`.
- **Frontend:**
  - `public/locales/mr.json`, `public/locales/hi.json` (A-09 to A-12);
  - `components/compare/summary.tsx` (comment only);
  - `components/compare/stale-note.test.tsx`.
  - `next.config.ts` was changed and reverted, so it has no net change. Line endings of a few Task 12 files were normalized to LF, with no content change.
- **Documentation:**
  - `docs/architecture/DECISION_LOG.md` (ADR-0049 status; ADR-0050 to ADR-0052);
  - `docs/REQUIRES_DECISION.md`;
  - `docs/02_AgriNex_UIUX_Phase1_Specification.md` §10.2–§10.3;
  - the database documents (`DATA_DICTIONARY`, `INDEXES_AND_CONSTRAINTS`, `TABLE_OWNERSHIP`, `ERD`, `DATABASE_INDEX`, `DATABASE_ARCHITECTURE`, `SEED_AND_REFERENCE_DATA` §12.5–§12.6);
  - `docs/architecture/INGESTION_ARCHITECTURE.md`;
  - `docs/data/AGRINEX_VERIFIED_GOVERNMENT_API_DATA_REFERENCE.md` §23.4;
  - `docs/security/TASK_12_SECURITY_REVIEW.md`;
  - `docs/decision-review/TASK_12_OWNER_DECISION_PACKET.md`;
  - `docs/LOCAL_DEVELOPMENT_GUIDE.md`;
  - `docs/00_SSOT_INDEX.md`;
  - this report.
- **Local database:**
  - V12 applied;
  - backfill run `d417d32f`;
  - D9 and verification load-test rows removed after export;
  - 242 merges.
- **Backups**, all in `local-reference-data/backups/`, which Git ignores:
  - `agrinex_pre_task12_closure_2026-10-15.dump`;
  - `agrinex_pre_d11_merges_2026-10-15.dump`;
  - the CSV exports of deleted rows, with SHA-256 sums.

## 4. Test, security and performance results

| Suite | Result |
|---|---|
| Backend: unit, integration, migration, contract (`mvnw verify`) | 99 of 99 |
| Frontend: unit and component | 195 of 195 (17 files) |
| Frontend: type-check, lint, production build | pass |
| ML | 15 of 15 |
| E2E (Playwright, Chrome, real stack) | 10 of 10, twice (once through the guide) |
| Independent comparison recheck | 11 of 11, plus the expected 422 for a per-bundle crop |
| Independent trend recheck | 15 of 15 |
| Freshness badges vs report dates | 612 of 612 |
| Security probes (CORS, refresh `Origin`, auth, RBAC, health, errors) | 20 of 20 |
| PostgreSQL and Redis outages | 11 of 11 (database 503 in 3.0 s, recovery in 1 s; Redis degraded but serving) |
| Government key disabled | 6 of 6, banner verified in the browser |
| Live account email (Resend → disposable inbox) | 23 of 23 |
| Ingestion (two CURRENT runs, visibility, units) | 10 of 10; completeness 98.6 % |
| Log scan for personal data and secrets | 0 matches |

Performance, 10 virtual users for 60 s, backend jar, with comparisons recorded as in production:

| Run | Requests | Errors | p50 | p95 | p99 | Max |
|---|---|---|---|---|---|---|
| Dedicated (dev server stopped) 1 | 7,548 (123/s) | 0 | 64 ms | 165 ms | 251 ms | 1,682 ms |
| Dedicated 2 | 6,979 (115/s) | 0 | 70 ms | 182 ms | 275 ms | 796 ms |
| Shared with the Next dev server and Chrome | 1,290 (21/s) | 0 | 212 ms | 1,512 ms | 3,864 ms | 7,933 ms |

LCP, production build, 4× CPU, 375 × 812 at DPR 3, cold cache:

| Page | Fast 3G | Slow 3G |
|---|---|---|
| Home | 1,567 ms | 6,656 ms (5,045 ms in an earlier run) |
| Compare | 1,624 ms | 5,230 ms |

## 5. Remaining limitations

| Limitation | Blocks |
|---|---|
| Not deployed; no images, reverse proxy or full compose file | Phase 1 freeze (Task 13) |
| Experiment 1 query not written; no real usage data; Experiment 2 needs 14 deployed days | Phase 1 freeze |
| Slow 3G LCP 5.0–6.7 s; the stylesheet cannot be inlined under the nonce CSP | Nothing (the Fast 3G profile is the decided criterion); worth a deployment-time fix |
| p95 depends on host resources; the laptop with dev tooling exceeded 1 s | Must be re-measured on the VM in Task 13 |
| 10 crops without unit evidence | Nothing; those crops cannot be compared |
| 477 PROVISIONAL markets; their history stays hidden | Nothing; merges need evidence |
| Marathi and Hindi copy (UI and email) not reviewed by a native speaker | Nothing (ADR-0046); recommended enhancement |
| Security sign-offs are AI reviews under delegation | Nothing under ADR-0050; a human review is advisable before production |
| The data.gov.in key is still the exposed one | Should be rotated before deployment |
| The repository has no commit; §42 item 1 branch strategy not in place | Reproducible deployment (Task 13) |
| RD-037 (hosting), RD-002 (`.mcp.json`) open | Deployment; pushing |
| The local database holds 89 comparisons and 15 farmer accounts, all from tests | Nothing; exclude them from any evaluation |
| Long historical backfills can fall below 95 % completeness, because ADR-0008 rejections (below-floor and outlier prices, unverified units) count as not ingested | Watch in Experiment 2; the criterion is per run |

## 6. External actions for the owner

1. **Rotate the data.gov.in key.** Follow `docs/security/TASK_12_SECURITY_REVIEW.md` §7. Claude has not rotated it and cannot.
2. **Commit the work and set up the branches** (§42 item 1). Committing and Git configuration are reserved for the owner (CLAUDE.md §F). `infra/.env` and `local-reference-data/` are ignored by Git.
3. **Decide RD-037 (hosting) and RD-002 (`.mcp.json`)** before Task 13.
4. **Optional:** have a native speaker review the Marathi and Hindi copy, and have a human review the security sign-offs.

## 7. The local guide

`docs/LOCAL_DEVELOPMENT_GUIDE.md` was updated (12 migrations, trilingual email, merged markets, current test counts, load-test clean-up). It was then followed as written on 2026-10-16:

- Terminal 1 checks: both containers healthy, 415,249 observations, 354 VERIFIED markets.
- Terminal 2 backend start: `Successfully validated 12 migrations`, "Schema … is up to date".
- Terminal 3 frontend start: Ready.
- Every URL of section 7 answered 200.
- The E2E journeys of section 10 passed: 10 of 10.

## 8. Tasks before Task 13

| Task | Acceptance | Status |
|---|---|---|
| Control layer and SSOT (2026-10-02) | Governance documents in place | Met |
| Task 6 architecture, Task 7 database, decision lock | Authoritative documents; ADR-0001 to ADR-0034 | Met; schema extended by V12 under ADR-0051 |
| Task 8 contract, 8.5 review, 8.6 lock | `contracts/` and contract tests | Met; contract unchanged by the closure |
| Task 9 UI/UX | Role file 02 approved (ADR-0043 to ADR-0046) | Met; copy corrections A-09 to A-14 |
| Task 10 backend | Spec §48 backend criteria; tests | Met (99 tests; live checks) |
| Task 11 frontend | §48 frontend criteria; ADR-0049 | Met (195 tests; 10 E2E); ADR-0049 decided |
| Task 12 real data, testing, local readiness | Real data, decisions, full QA, guide | Met, with the limitations of section 5 and the owner actions of section 6 |
| §42 item 1, branch strategy | Repository with branches | **Not met.** No commit exists (owner action) |

Phase 1 can proceed to Task 13. It must not be frozen, and Phase 2 must not start, until deployment, evaluation and the owner's freeze decision have happened.
