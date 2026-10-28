# AgriNex — Task 12 Owner Decision Packet

**Status:** REFERENCE (2026-10-15). This packet prepared decisions for the Human Project Owner, who delegated all twelve to Claude on 2026-10-15 (ADR-0050). The dispositions below are recorded in `docs/architecture/DECISION_LOG.md` (ADR-0051, ADR-0052); the sections after them keep the evidence as it was presented.

## Dispositions (2026-10-15, decided under ADR-0050)

| # | Disposition | Record | Residual limitation |
|---|---|---|---|
| D1 | `commodity_source_aliases` (V12) with the 20 evidenced spellings; backfill re-run: 8,978 records stored, 276 quarantined conflicts, 2,083 identical to stored rows | ADR-0051; RD-050 resolved | A new legacy spelling is rejected until a migration adds it with evidence |
| D2 | The 10 crops stay unverified; the admin process to add a unit with official evidence is recorded | ADR-0052 item 2 | Their records stay rejected as `UNIT_UNVERIFIED` |
| D3 | Re-examined and re-tested; signed off as an AI review | ADR-0052 item 3; security review §6–§7 | No human has reviewed; S12-1 is a Task 13 condition |
| D4 | ADR-0049 DECIDED | ADR-0052 item 4 | — |
| D5 | RD-048 closed: ADR-0007 item 2 confirmed | ADR-0052 item 5 | — |
| D6 | 378 keys re-reviewed, automated checks, 4 corrections (role file 02 §10.3 A-09 to A-12) | ADR-0052 item 6 | No native-speaker review |
| D7 | One trilingual (EN, MR, HI) plain-text email per message, link once; 23 of 23 live checks | ADR-0052 item 7; RD-049 resolved | No per-user language; no native-speaker review |
| D8 | The note shows only when every displayed option is stale | ADR-0052 item 8 | — |
| D9 | 10,316 load-test comparisons deleted after export; 50 of uncertain origin kept | ADR-0052 item 9 | Experiment 1 needs real use after deployment |
| D10 | Acceptance profile: Chrome DevTools Fast 3G, 4× CPU, 375 px, cold cache (home 1,567 ms, compare 1,624 ms) | ADR-0052 item 10 | Slow 3G not met (5.0–6.7 s); CSS inlining is blocked by the CSP |
| D11 | 242 PROVISIONAL markets merged on same-delivery price evidence; 477 stay PROVISIONAL | ADR-0052 item 11 | Their history stays hidden until evidence exists |
| D12 | Key treated as compromised; rotation steps recorded | ADR-0052 item 12; security review §7 | **Owner action pending** |

## Summary

| # | Decision | Recommendation | Blocks |
|---|---|---|---|
| D1 | RD-050: resolve 20 legacy crop spellings | Approve a `commodity_source_aliases` table keyed on DMI evidence, then re-run the backfill for 2025-10-15 to 2026-05-06 | Storing 11,337 historical records (2.4 % of the year) |
| D2 | Unit evidence for 10 crops | Export the official report for the dates listed; record the units through *Admin → Commodities → Edit → Save verified unit* | Storing 522+ records a year for those crops |
| D3 | Security sign-off (rule 16) | Sign off the four items of `docs/security/TASK_12_SECURITY_REVIEW.md` §6 | Merging the Task 12 security-sensitive changes |
| D4 | ADR-0049 (frontend tooling, including the Playwright end-to-end tool) | Approve | Recording the tooling as decided |
| D5 | RD-048 (historical date-filter format) | Close as confirmed: ADR-0007 item 2 stands | Nothing (evidence complete) |
| D6 | Marathi and Hindi copy (ADR-0046) | Have a named native speaker review the two catalogues, starting with the Task 12 keys | Approving localized copy |
| D7 | RD-049 (account email copy and languages) | Approve the current English texts for Phase 1, or supply new copy | Production account email |
| D8 | Stale-note rule (ASSUMPTION from Task 12) | Confirm: the "prices are old" note shows only when every option is stale | Approving that results-screen behavior |
| D9 | Load-test records in the local database | Decide whether to keep or delete the anonymous comparisons the load tests recorded | Clean Experiment 1 figures |
| D10 | The "3G" profile of the LCP target (spec §48) | Choose the network profile used to judge the target | Claiming the LCP criterion |
| D11 | Legacy market spellings (703 PROVISIONAL markets) | Merge on evidence through *Admin → Markets*, case by case (ADR-0014 item 4) | Price history before 2026-09-03 for 354 verified markets |
| D12 | data.gov.in key | Rotate the key that appeared in plain text in the Task 12 instructions | Nothing technical |

## D1 — RD-050: legacy crop spellings

**Evidence.** Each of the 20 spellings carries, in the data.gov.in historical resource (published by DMI), the same `Commodity_Code` as exactly one name of the Agmarknet 2.0 master. All 20 targets already have the verified unit `Rs./Quintal`. Records sampled from 156 report dates; the full year's rejections total 11,337.

| Source spelling | Code | Master name | Kind |
|---|---|---|---|
| `Arhar(Tur/Red Gram)(Whole)`, `Arhar (Tur/Red Gram)(Whole)` | 49 | `Red gram/Arhar/Tur(whole)` | rename |
| `Arhar Dal(Tur Dal)` | 260 | `Red gram split/Arhar dal/Tur dal` | rename |
| `Paddy(Dhan)(Common)` | 2 | `Paddy(Common)` | rename |
| `Sunflower` | 14 | `Sunflower/Sunflower Seed` | rename |
| `Jack Fruit` | 182 | `Jack Fruit(Ripe)` | rename |
| `Cardamoms` | 40 | `Cardamom` | rename |
| `Black Gram (Urd Beans)(Whole)` | 8 | `Black Gram(Urd Beans)(Whole)` | spacing |
| `Green Gram (Moong)(Whole)` | 9 | `Green Gram(Moong)(Whole)` | spacing |
| `Lentil (Masur)(Whole)` | 63 | `Lentil(Masur)(Whole)` | spacing |
| `Ragi (Finger Millet)` | 30 | `Ragi(Finger Millet)` | spacing |
| `Little gourd (Kundru)` | 302 | `Little gourd(Kundru)` | spacing |
| `Mango (Raw-Ripe)` | 172 | `Mango(Raw-Ripe)` | spacing |
| `Elephant Yam (Suran)` | 296 | `Elephant Yam(Suran)/Amorphophallus` | spacing and suffix |
| `Pointed gourd (Parval)` | 303 | `Pointed gourd(Parval)` | spacing |
| `Cowpea (Lobia/Karamani)` | 92 | `Cowpea(Lobia/Karamani)` | spacing |
| `French Beans (Frasbean)` | 298 | `French Beans(Frasbean)` | spacing |
| `Bengal Gram Dal (Chana Dal)` | 263 | `Bengal Gram Dal(Chana Dal)` | spacing |
| `Black Gram Dal (Urd Dal)` | 264 | `Black Gram Dal(Urd Dal)` | spacing |
| `Green Gram Dal (Moong Dal)` | 265 | `Green Gram Dal(Moong Dal)` | spacing |

**Options:**

- **A. Commodity alias table (recommended).**
  - Add `commodity_source_aliases` (commodity module), like `district_source_aliases` (ADR-0014 item 3).
  - Each row holds a source spelling, its master commodity and an evidence note naming the shared `Commodity_Code`.
  - Ingestion resolves a commodity by exact name first, then by alias.
  - Seed the 20 rows by migration; admins add later ones with evidence.
  - Then re-run the historical backfill for 2025-10-15 to 2026-05-06. Re-ingestion is idempotent (ADR-0009), so stored observations stay untouched and only the rejected records are added.
- **B. Match on `Commodity_Code`.** Only the historical resource carries the code, so current records could not use it, and two matching paths would coexist.
- **C. Keep rejecting.** No change. No record after 2026-05-06 is affected, so current comparisons and trends are unaffected.

A **broader normalization** (removing a space before "(") would cover only the 13 spacing cases. It risks silent merges, which ADR-0006 forbids.

**Needs:** a decision-log entry, because A changes the schema and the ingestion rules (CLAUDE.md §G, §I).

## D2 — Unit evidence for 10 crops

The 17 report exports for 13-Oct-2026 contain no row for these crops, so no unit is recorded. The feed's prices are not evidence of the unit. A unit inferred from similar crops (other livestock are `Rs./Unit`) would be a guess, which ADR-0008 forbids.

| Crop | Group | Maharashtra report dates in the feed (latest) | Example market |
|---|---|---|---|
| Ox | Live Stock,Poultry,Fisheries | 2026-10-08, 10-11, 10-12 | Anajngaon APMC |
| He Buffalo | Live Stock,Poultry,Fisheries | 2026-10-08 | APMC Khamgaon |
| Sheep | Live Stock,Poultry,Fisheries | 2026-09-24, 10-07 | APMC Khamgaon |
| Ghee | Oils and Fats | 2026-10-08, 10-09, 10-10 | APMC Mumbai |
| Hippe Seed | Forest Products | 2026-09-27 | Pune(Pimpri) |
| Tamarind Seed | Forest Products | 2026-08-31, 09-17 | Ahilyanagar APMC |
| Peach | Fruits | 2026-08-24, 09-04 | Pune APMC |
| Jamun(Narale Hannu) | Fruits | 2026-07-21, 08-06 | Mumbai- Fruit Market APMC |
| Cherry | Fruits | 2026-07-14, 07-17 | Mumbai- Fruit Market APMC |
| Sugarcane | Others | 2025-12-22, 12-29 | Nagpur APMC |

**Recommended action:**

1. Export the Agmarknet 2.0 "Commodity-wise, Market-wise Daily Report (Weighted Average)" for the listed dates and groups. Several crops share 2026-10-08 (Livestock, Oils and Fats); the others need their own dates. This needs the captcha, so it is a human step.
2. Copy the files into `docs/data/raw/`.
3. Record each unit with its evidence through *Admin → Commodities → Edit → Save verified unit* (ADR-0020; audited), or through a migration like V11.

Records already rejected can be re-ingested afterwards with a backfill of their dates.

## D3 — Security sign-off

`docs/security/TASK_12_SECURITY_REVIEW.md` §6 lists the items:

- the CORS rejection format;
- optional-access session handling (frontend);
- S12-1, the `dev` profile, which belongs to deployment;
- S12-2, the database fail-fast timeout.

Objective checks completed:

- live CORS probes (11 cases);
- `CorsIntegrationTest`;
- the client test, which fails without the fix;
- the live database and Redis outage results;
- the full backend suite.

## D4 — ADR-0049 (PROPOSED)

Objective check, 2026-10-15: every version the entry names matches `frontend/package.json`, 17 of 17, including `@playwright/test` 1.64.1. Node `engines` is `>=24.0.0 <25`. All the tools are in use:

- Vitest: 194 tests;
- ESLint: clean;
- Ajv contract tests: in the suite;
- Playwright: 10 journeys.

The entry stays PROPOSED until you approve it.

## D5 — RD-048 (historical date filter)

Evidence:

- **Task 12 live comparison.** `07-09-2026` (the format the backend sends) and `07/09/2026` returned the same 71 records.
- **Task 12 harvest.** All 156 historical report dates were requested with `dd-MM-yyyy`. Each returned exactly its reported `total`, with records.
- **Year backfill.** The backend's 366-date backfill, sent in `dd-MM-yyyy`, fetched 466,346 records with 0 failed pages.

ADR-0007 item 2 is confirmed in practice. Recommendation: record the result and move RD-048 to Resolved.

## D6 — Marathi and Hindi copy (ADR-0046)

Objective checks, 2026-10-15, on `frontend/public/locales/{mr,hi}.json`:

- 378 keys each, the same keys as English;
- 0 placeholder mismatches, 0 empty values;
- 0 Devanagari digits (PDD-06);
- only four values identical to English, all of them formats or the trilingual language chooser.

In the browser, both languages render the results, alert and account screens.

**Needs:** a review by a named native speaker, which no tool can replace. It covers at least the Task 12 additions `alerts.no_new_events` and `error.unsupported_unit` (shown when a crop is priced per bundle or per unit), and then the full catalogues.

## D7 — RD-049 (account email copy)

The three plain-text English emails in `backend/src/main/java/com/agrinex/auth/email/AuthEmails.java` were checked against ADR-0048:

- the links use the configured public origin, with the token in the fragment;
- single use is stated;
- the lifetime is stated from configuration (24 hours, 30 minutes);
- each email says what to do if the user did not ask for it;
- the only personal data is the recipient.

Six messages were delivered live through Resend.

**Needs:** approval of the wording, and a decision on whether Marathi and Hindi versions are required for Phase 1.

## D8 — Stale-note rule

Since Task 12 the results screen shows "These prices are old" only when every compared option is STALE (`frontend/components/compare/summary.tsx`). It was previously shown when any option was stale, which put the note under a Fresh recommendation. This is an interpretation of role file 02 E03 and was labelled ASSUMPTION. **Needs:** confirmation by the UI/UX owner.

## D9 — Load-test records

Every comparison is recorded as an anonymous search (ADR-0029: kept indefinitely for Experiment 1). The Task 12 load tests on 2026-10-15 recorded about 10,150 comparisons in the local database: `selling_searches`, `recommendation_runs` and `selling_options`, all within a 3-hour window. Nothing was deleted, as the owner instructed.

**Needs:** a decision to keep them, or to delete that window before Experiment 1 figures are taken from this database.

## D10 — "3G" in the LCP target

Production build (`next start`), mobile viewport, 4× CPU slowdown, cold cache, measured 2026-10-15:

| Profile (Chrome DevTools) | Home | Compare |
|---|---|---|
| Fast 3G | 1,695 ms | 1,474 ms |
| Slow 3G | 5,045 ms | not measured |

On Slow 3G one render-blocking stylesheet costs about 2 s. Inlining critical CSS is the candidate fix, if Slow 3G is the target. **Needs:** a choice of profile for spec §48's "Frontend LCP on mobile (3G) < 3 seconds".

## D11 — Legacy market spellings

Until 2026-09-02 the feed named markets differently (`Akola APMC` against today's `APMC Akola`). 703 such markets are PROVISIONAL, and their observations are stored but hidden from farmers. ADR-0014 allows merges only on evidence: the same district, names that differ only in form, and identical prices in the same delivery or an official DMI reference. The rename is a date boundary, so same-delivery price evidence is rare. **Needs:** an admin decision per market, or acceptance that pre-September history stays out of trends.

## D12 — Key rotation

The data.gov.in key appeared in plain text in the Task 12 instructions (twice). It is stored only in `infra/.env`, which is git-ignored. Rotation is the owner's decision (CLAUDE.md §K).
