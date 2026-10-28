# AgriNex — UI/UX Phase 1 Specification (role file 02)

**Status:** AUTHORITATIVE — APPROVED on 2026-10-04 by the Human Project Owner (ADR-0043, ADR-0044; Task 9 final decision lock). Created by Task 9 on 2026-10-04.

- This is the design source of record for every Phase 1 screen it specifies (spec §43, §44; ADR-0027). The approval is recorded in SSOT index area 5 and mirrored in §19.
- **Approved visual direction: Hisaab** (Concept A, the honest ledger) **with four borrowings**: the step-by-step mobile input and the answer strip from Concept B, and the trade-off sentence and the shared-scale transport bar from Concept C, all rendered in Hisaab's language (§1.1; ADR-0043). Concepts B and C remain exploration reference in [`decision-review/TASK_9_DESIGN_DIRECTION_PACKET.md`](decision-review/TASK_9_DESIGN_DIRECTION_PACKET.md), not an approved direction.
- Precedence (ADR-0027 item 3): master specification, then this file, then tool designs (Stitch, Figma). Where this file and a binding decision-log entry differ, the entry governs; correct this file. Where a design draft and this file differ, this file governs.
- Production screens are built from this file and the approved design references (CLAUDE.md §M), in the enforced development order (spec §42): the frontend core comes after the backend and the recommendation engine.

**Labels used in this file** (CLAUDE.md §D):

| Label | Meaning |
|---|---|
| SSOT | Requirement taken from the master specification (`docs/01_AGRINEX_MASTER_PRODUCT_PHASES.md`, "spec") or a binding ADR; cited |
| CONTRACT | Fact about what the API provides (`contracts/`, `docs/api/`) |
| APPROVED (PDD-nn) | Design decision made in Task 9 and approved by ADR-0044; listed in §17 |
| EXPLORATION | Belongs to a direction that was not selected (B or C). Reference only; never implemented unless §1.1 adopts it |
| PLACEHOLDER | Synthetic values from `frontend/__mocks__/api/` used in drafts. Never real data (P10, ADR-0028) |

---

## 1. Purpose and scope

The farmer journey is: "what are the mandi prices?" → "which selling option appears economically better, and why?" (spec §4.5, §25). This file specifies the Phase 1 screens, states and content that every visual direction must honour.

- **In scope:** the Phase 1 farmer UI (MR / HI / EN) and the admin UI (EN, desktop-first) — features F1.1–F1.23 (spec §7) and the screens named in spec §13, §25 and §30.
- **Out of scope:** Phase 2 (listing, bid, transaction, rating, buyer screens — HARD RULE 1); operator procedures (erasure, deactivation, reactivation have no screens: ADR-0037, ADR-0041); anything in spec §39.

### 1.1 Approved design direction (ADR-0043)

| Element | Approved definition |
|---|---|
| Base: **Hisaab** (Concept A) | Each market is a short account the farmer can check: latest reported price × quantity = gross; − estimated transport; = estimated net realization. Warm paper surface, ink text, hairline ledger rules, a double rule above the bottom line, ledger green for the net figure, tabular figures (IBM Plex Sans and Plex Sans Devanagari, IBM Plex Mono figures), 4 px corners, no shadows. Mobile: ledger cards. Desktop: a comparison sheet with markets as columns. Navigation: a top app bar only. Tokens: §14.2 |
| Borrowing 1 (from B): **step-by-step mobile input** | Below 1024 px, the comparison input (S03) asks one question per screen: place → crop → quantity → distance and review, with "Step n of 4", a back control and the answers so far. Hisaab styling: paper surface, ruled answer summary, ink buttons. At 1024 px and wider, Hisaab's single input panel beside the results |
| Borrowing 2 (from B): **answer strip** | The first block of the results (S04), at every width: "Recommended: {market}", the estimated net realization with its gloss, then distance, report date and freshness, and a "How is this ranked?" link to the basis. A Hisaab ruled strip with the net figure in ledger green, not B's filled green block |
| Borrowing 3 (from C): **trade-off sentence** | Inside the winner callout (only when `rankingsDisagree`): one sentence and one arithmetic line in rupees, worded by ADR-0045 (§8 S04) |
| Borrowing 4 (from C): **shared-scale transport bar** | Inside each ledger card (and in the transport row of the desktop sheet): a horizontal bar of the option's estimated transport cost, every bar on one shared scale from ₹0 to the largest estimated transport shown, with its amount in text and the caption of §10. Hisaab ink on a decorative rule track. In What-If it takes the hypothetical treatment with a tick at the reported value |

The internal name "Hisaab" never appears in the product; the product name is AgriNex. Refinements are allowed only for consistency, usability, responsiveness, accessibility, implementation readiness and correctness (ADR-0043 item 5).

## 2. Sources

| Topic | Source |
|---|---|
| Personas, journey | spec §6, §25, §21 |
| Features | spec §7 F1.1–F1.23 |
| Freeze checklist (frontend, resilience) | spec §10 |
| Engine, What-If, transport | spec §16, §17, §32; ADR-0023 to ADR-0026, ADR-0038 to ADR-0040 |
| Trends, alerts | spec §33, §34; ADR-0019, ADR-0025 |
| Failure messages | spec §29; `docs/architecture/RESILIENCE_ARCHITECTURE.md` §4.2 |
| Data rules, labels, claims | spec §31, §32, §49 |
| Screens ↔ operations and states | `docs/api/FRONTEND_CONSUMPTION.md` §2–§5 |
| Provenance, freshness, outage | `docs/api/PROVENANCE_AND_FRESHNESS.md` §1–§7 |
| Frontend boundary and required states | `docs/architecture/FRONTEND_ARCHITECTURE.md` §4 |
| Browser floor | ADR-0004 |
| Localized names | ADR-0015 |
| Accounts and tokens | ADR-0016, ADR-0017, ADR-0042; email, login identifiers and recovery ADR-0047, ADR-0048 |
| Approved direction, screens, design decisions, wording, copy ownership | ADR-0043, ADR-0044, ADR-0045, ADR-0046; record: `decision-review/TASK_9_FINAL_DECISION_LOCK.md` |
| Synthetic data in designs | ADR-0028; `frontend/__mocks__/api/README.md` |

## 3. Users and what "farmer-first" means here

SSOT (spec §6): Ramesh (Marathi, 5.5" Android, 4G dropping to 3G, "comfortable tapping, NOT comfortable reading dense text", English numbers only); Sunita (Hindi, older 4.7" device, intermittent 3G, sometimes voice input, wants price alerts); admin (English, laptop). What he needs: "Simple input → clear comparison → obvious winner with reason → option to act".

Adopted with the approved specification (ADR-0043, ADR-0044):

1. **Answer first, arithmetic one glance away.** The first screenful of results states which market is recommended, the estimated money after transport, and the single most important reason. The full calculation is visible without leaving the screen.
2. **Money in the farmer's terms.** "Estimated net realization" (mandatory, spec §32) is always glossed in plain words: "money after transport".
3. **Every number says what kind of number it is** (§6): reported, estimated, hypothetical or trend statistic — by a consistent visual mark, not by prose.
4. **Honesty is ambient, not buried.** Report date and freshness sit next to every price; source and disclaimer close every price screen; the outage banner sits above results.
5. **Tap, don't type.** Chips, pinned crops, steppers and number pads; typing only for quantity and sign-in.
6. **Recover, never dead-end.** Every empty or error state names the next action (spec §25 "Adjust distance").

## 4. Information architecture

### 4.1 Screen inventory

Each screen exists because the SSOT names it; nothing was added for the package.

| ID | Screen | Spec / ADR | Contract operations | Auth | Persona |
|---|---|---|---|---|---|
| S01 | Language selection (first launch) | F1.2, §25 B–C | — | Anonymous | Farmer |
| S02 | Home / landing (entry, "about the prices", disclaimer) | §25 D–E, §47 Q17, §49 | — | Anonymous | Farmer |
| S03 | Comparison input: State → District → Locality, crop (+ variety/grade), quantity + unit, distance; one question per step on mobile (§1.1) | F1.3–F1.6, §25 F–J, §21 step 3 | `listStates`, `listDistricts`, `listLocalities`, `listCommodities`, `listVarieties`, `listGrades` | Anonymous | Farmer |
| S04 | Comparison results: answer strip, ranked options with shared-scale transport bars, winner callout, excluded markets | F1.7–F1.13, F1.16, F1.17, §10, §25 K–P; ADR-0045 | `compareSellingOptions` | Anonymous (optional token) | Farmer |
| S05 | "Why recommended?" explanation | F1.14, §16 reasons, §25 R | data of S04 | Anonymous | Farmer |
| S06 | Market detail (latest prices at one market) | §30 | `getMarketDetail` | Anonymous | Farmer |
| S07 | Historical trend (7 / 30 days) | F1.15, §33, ADR-0025 | `getMarketTrend` | Anonymous | Farmer |
| S08 | Forecast section — **optional, only if the ML track exists** | §35, ADR-0030 | `getMarketForecast` | Anonymous | Farmer |
| S09 | What-If simulator | F1.18, §17, §32, ADR-0026 | client-side; `simulateSellingOptions` only when the radius grows | Anonymous | Farmer |
| S10 | Sign in (mobile number or email) / quick registration (mobile number, email, password) | §25 V–W, ADR-0016, ADR-0047 | `login`, `registerFarmer`, `refreshSession`, `logout` | — | Farmer, admin |
| S11 | Price alerts: rules, events, create | F1.19, §34, ADR-0019 | `listAlerts`, `createAlert`, `updateAlertStatus`, `deleteAlert`, `listAlertEvents`, `markAlertEventRead` | FARMER or ADMIN | Farmer |
| S12 | Account menu: who is signed in, email-confirmation status, language, sign out | §25, ADR-0016, ADR-0048 | `logout`, `resendEmailVerification` (no profile or settings operation exists — CONTRACT) | Signed in | Farmer |
| S14 | Forgot password: ask for a reset link | ADR-0047, ADR-0048 | `requestPasswordReset` | Anonymous | Farmer, admin |
| S15 | New password, opened from the reset link | ADR-0047, ADR-0048 | `confirmPasswordReset` | Anonymous | Farmer, admin |
| S16 | Email confirmation, opened from the verification link | ADR-0047, ADR-0048 | `confirmEmailVerification` | Anonymous | Farmer, admin |
| S13 | Global states: outage banner, 429, 503, 500, network | §29, F1.23 | all | — | All |
| A01 | Admin data health | F1.20, FR-18, §46 | `getIngestionHealth`, `getSystemStatus` | ADMIN | Admin |
| A02 | Ingestion runs, run detail, run errors, trigger, pause/resume | FR-17, FR-18, ADR-0011, ADR-0020 | `listIngestionRuns`, `getIngestionRun`, `listIngestionRunErrors`, `triggerIngestion`, `pauseScheduledIngestion`, `resumeScheduledIngestion` | ADMIN | Admin |
| A03 | Configuration: transport assumptions; weights and freshness thresholds; versions | §32, ADR-0020, ADR-0024 | `get/updateTransportAssumptions`, `get/updateRecommendationWeights`, `list…Versions` | ADMIN | Admin |
| A04 | Curation: markets, commodities, district aliases | ADR-0014, ADR-0015, ADR-0020 | `listAdminMarkets`, `getAdminMarket`, `verifyMarket`, `mergeMarket`, `deactivateMarket`, `reactivateMarket`, `setMarketCoordinates`, `listAdminCommodities`, `addCommodity`, `verifyCommodityPriceUnit`, `updateCommodityNames`, `listDistrictAliases`, `addDistrictAlias` | ADMIN | Admin |
| A05 | Admin audit log | ADR-0020 item 6 | `listAdminAuditEvents` | ADMIN | Admin |

Not screens (SSOT): erasure, deactivation, reactivation (operator procedures, ADR-0037, ADR-0041); profile editing, email change and password change while signed in (no operation in the contract; a forgotten password is handled by S14 and S15, ADR-0047); a public market directory (no public operation lists markets; markets are reached only from a comparison or an alert — CONTRACT); "history" of searches (assumption A-03, `FRONTEND_ARCHITECTURE.md` §9).

### 4.2 Navigation rules

- SSOT: comparison needs no login (F1.1, §28). Alerts need a login (§25). Admin is a separate area for ADMIN (§28).
- APPROVED (PDD-02): two farmer destinations only — **Compare** and **Alerts** — plus an account entry. Trend, What-If, explanation and market detail are *contextual* to a comparison, never top-level (they need a market and a comparison to exist). Hisaab uses a top app bar only: back, title, language, alerts (with the unread count), account. EXPLORATION, not adopted: B's bottom bar and C's top tabs.
- APPROVED: the alerts entry shows the unread-event count from `listAlertEvents?unreadOnly=true` (`totalItems`, CONTRACT).
- Admin navigation (APPROVED with the direction): Data health · Ingestion runs · Configuration · Markets · Commodities · District aliases · Audit log.

## 5. Journeys

| Journey | Steps (screens) | SSOT |
|---|---|---|
| J1 Hero comparison | S01 → S02 → S03 → (loading) → S04 → S05 → S07 → S09 → S11 | §21, §25, §46 |
| J2 Empty and failure | S03 → S04 (no market in range → adjust distance → S04) / (no recent data → change crop or distance) / outage banner over S04 | §25 L, §29 |
| J3 Alert with sign-in | S04 or S07 → "Set alert" → S10 (quick registration with mobile number, email and password) → S11 (alert saved; "confirm your email" notice) → later: event → S11 → S04 | §25 S–Y, §34; ADR-0047 |
| J5 Password recovery | S10 sign-in → "Forgot password?" → S14 (mobile number or email) → same message for every identifier → email link → S15 (new password) → S10 sign-in with the new password | ADR-0047, ADR-0048 |
| J6 Email confirmation | Registration (J3) or S12 "Send a new link" → email link → S16 (confirmed, or link no longer valid) | ADR-0047, ADR-0048 |
| J4 Admin health | S10 (email sign-in) → A01 → A02 (errors, backfill) → A03 / A04 / A05 | §46 5:45–6:30, ADR-0011, ADR-0020 |

The FigJam board (Task 9) draws J1 to J4; J5 and J6 were added by the Task 9 final lock and are specified here only. The Stitch projects hold the screens (packet §6).

## 6. The honesty system — value kinds and their encoding

CONTRACT (`PROVENANCE_AND_FRESHNESS.md` §7): each kind of value lives in its own place in the API. The UI must keep them apart just as visibly.

| Kind | API field | Required visual treatment (all directions) |
|---|---|---|
| Observed government price | `price` (`PriceQuote`) | Shown as reported: "Latest reported price ₹3,000/quintal", with report date and freshness badge beside it; never prefixed "≈" |
| Configured assumption | `transportAssumptions` | Named as an assumption: "₹8/km assumption" |
| User hypothesis (What-If) | client state; `scenario.userOverrides` | One reserved hypothetical colour + pattern + the word "What-if", on every hypothetical value and on the screen frame. Never used for reported data |
| Derived estimate | `estimates.*` | "≈" prefix and the word "Estimated"/"est."; `estimatedNetRealization` always labelled "Estimated net realization" |
| Ranking label | `rank`, `netRealizationWinnerId`, `priceOnlyWinnerId`, `estimates` | "Recommended" names the system's rank 1 and always comes with its basis; factual labels ("Highest price", "Highest estimated net realization") only where literally true (ADR-0045; `FRONTEND_CONSUMPTION.md` §2.1) |
| Trend statistic | `trendDirection`, `changePercent7Day` | Arrow + word + signed percentage, one decimal ("−8.5%") with "7-day change"; never coloured alone |
| ML prediction (optional) | `ForecastResponse` only | Separate optional section titled as a model prediction; never inside comparison, detail, trend or alerts; never a confidence percentage (ADR-0030) |

Freshness (SSOT F1.16, §16; CONTRACT §3): FRESH, RECENT, STALE, UNAVAILABLE, always icon + word, plus the absolute report date. APPROVED (PDD-12): copy never hard-codes day counts, because the thresholds are configurable per weights version (`freshness_*_days`): Fresh "reported recently", Recent "a few days old", Stale "old — may have changed", Unavailable "too old to compare". Under default thresholds FRESH covers today and yesterday, so "reported today" appears only when `reportDate` is today (design skill rule).

## 7. Mandatory content

| Content | Exact text (EN) | Data | Where |
|---|---|---|---|
| Price naming | "Latest reported price" or "Government-reported mandi price"; never "live" or "real-time" | — | Every price (§31 RULE 4, §49) |
| Attribution | "Source: Ministry of Agriculture & FW / DMI / data.gov.in" | `price.attribution` | Every price screen (§31 RULE 7) |
| Report date | "Reported 2 Oct 2026" | `price.reportDate` | Next to every price |
| Freshness | Badge per §6 | `price.freshnessStatus` | Next to every price (F1.16) |
| Transport label | "Estimated — based on ₹{appliedRatePerKm}/km assumption. Actual costs will vary." | `transportAssumptions.appliedRatePerKm` (in client What-If: the rate the browser applied) | Every screen showing a transport cost (§32; ADR-0038) |
| Net label | "Estimated net realization" | `estimates.estimatedNetRealization` | Every net figure (§32) |
| Approximate distance | "≈42 km"; plus "market location approximate" when `approximateMarketLocation` | `distanceKm` | Every distance (§47 Q6; ADR-0014) |
| Ranking basis | Comparison: "Ranked mainly by estimated net realization, also weighing price, trend, data freshness and transport." What-If: "Ranked by estimated net realization only." | `decisionBasis.rankingMethod` | Results and What-If (ADR-0026 item 2; ADR-0045 item 3) |
| Recommendation labels | Rank 1: "Recommended" (never "Best" or "Highest net"). "Highest price" on `priceOnlyWinnerId`. "Highest estimated net realization" on every option with the highest estimate. What-If rank 1: "Highest estimated net (what-if)" | §2.1 of `FRONTEND_CONSUMPTION.md` | Results, What-If (ADR-0045) |
| Standard disclaimer | "Market prices, transport estimates, and forecasts may be incomplete, delayed, revised, or different from actual transaction conditions. AgriNex provides decision-support information and does not guarantee a sale price, transport quote, profit, or future market movement." — verbatim, never paraphrased | interface text | Every price screen and the home "about" section (§49, §47 Q17) |
| Sync status | "Latest reported prices · Last synchronized {lastSuccessfulSyncAt}" | `sourceStatus` | Results, detail, trend, alert events |
| Outage banner | "Latest source synchronization is unavailable. Showing most recently stored government-reported data." | `sourceStatus.latestSyncFailed` | Above content while true (§29) |
| Prototype tag | "SYNTHETIC DATA · PROTOTYPE" | — | Every draft and mock-mode screen (CLAUDE.md §M) — not in production |

## 8. Screen specifications

Content blocks are listed in priority order (what must be visible first at 375 px).

**S01 Language selection.** Three large options in their own script (मराठी, हिंदी, English) with an English hint. Shown on first launch only (§25 B); later reachable from the account entry. Selection persists on the device (mechanism is an implementation detail, `FRONTEND_ARCHITECTURE.md` §4.6).

**S02 Home.** One primary action "Compare markets"; secondary "My price alerts". "About the prices" block: government source, freshness levels (§6 copy), "Transport is an estimate you can change", last synchronized time, attribution and the standard disclaimer (§47 Q17's "about page" content, without adding a screen).

**S03 Comparison input.** Order: location → crop → quantity → distance (§25).
- Location: State → District → Locality from the geography operations. The origin sent is the selected district's centroid exactly as `listDistricts` publishes it (ADR-0039); never device location or address (§28). Helper: "Distance is measured from the centre of your district. AgriNex never asks for your address or device location."
- APPROVED (PDD-03): Locality is shown but **optional** — it is not part of `CompareRequest` and carries no coordinates (CONTRACT; ADR-0014). See §17 PDD-03.
- Crop: searchable; the most common crops pinned (§25 G). Approved rule: "pinned" = the first N items by the order the API returns plus any crop the user chose before on this device; no popularity data exists in the contract. Variety and grade optional, default "Any" with helper "Leave as Any to use the typical reported price" (ADR-0010 lower-median rule).
- Quantity: number + unit toggle kg | quintal with live conversion "= 5 quintal" (§32). Any value > 0 (ADR-0023); field errors per §9.
- Distance: default 150 km (F1.6) with quick chips; any whole km > 0.
- APPROVED (§1.1): below 1024 px, four one-question steps, each on its own screen with "Step n of 4", the answers so far as a ruled summary, a back control and one primary button: (1) "Where are you?" State → District → Locality; (2) "What are you selling?" crop with the pinned list and a search, variety and grade under "More options"; (3) "How much?" number pad field with the kg | quintal toggle and the live conversion; (4) "How far can you travel?" distance chips and a review of all four answers with "Find markets". Each step keeps its answer when the user goes back. At 1024 px and wider, Hisaab's single input panel sits beside the results. EXPLORATION, not adopted: C's input board.

**S04 Comparison results.** In priority order:
1. Outage banner when `latestSyncFailed` (§29).
2. Search summary with edit ("Onion · 500 kg · Test District North · within 150 km").
3. **Answer strip** (§1.1; ADR-0045 item 4; PLACEHOLDER values): "Recommended: TEST Market C" · "≈₹14,664 estimated net realization — money left after estimated transport" · "≈42 km · Reported 2 Oct 2026 · Fresh" · link "How is this ranked?" to the ranking basis.
4. **Winner callout** — only when `rankingsDisagree` (§10, §25 O; ADR-0045 item 5). Title "Why the highest price is not ranked first". With P the highest-price option and R the rank-1 option (values from `FRONTEND_CONSUMPTION.md` §2.1):
   - estimate of R higher: "TEST Market B has the highest price (₹3,100/quintal), but after estimated transport TEST Market C may leave about ₹188 more." and the trade-off line "+₹500 from the higher price · −₹688 more estimated transport = ₹188 less at TEST Market B" (PLACEHOLDER values);
   - estimate of R not higher: "{P} has the highest price and about ₹{n} more estimated net realization, but {R} is ranked first because the ranking also weighs price trend, data freshness and transport." followed by P's caution reasons (for example "Price falling this week · Old price report") and the trade-off line ending "= ₹{n} more at {P}".
5. **Every option, as a Hisaab ledger card** (a column of the desktop sheet): rank; market; badges "Recommended" (rank 1), "Highest price", "Highest estimated net realization" where true (ADR-0045 item 2); ≈distance (+ approximate-location note); the ledger: "Latest reported price ₹3,000/quintal × 5 quintal = ₹15,000", "− Estimated transport ₹336", double rule, "= Estimated net realization ≈₹14,664"; **shared-scale transport bar** under the transport line (§1.1; caption in §10); report date, freshness, variety · grade; 7-day trend; reason codes (§10 copy); actions: Why? · Trend · Set alert.
6. Excluded markets: "Not compared — no recent price: …" from `excludedMarkets` (ADR-0023).
7. Ranking basis, transport label, attribution, disclaimer.
- Never show the composite score (no such field; CONTRACT). The labels never re-rank anything.
- Order of options is the API rank; the UI never re-sorts by price.
- The transport bar's scale runs from ₹0 to the largest estimated transport among the displayed options, so differences are honest; it is never truncated.

**S05 Why recommended?** For the selected option: the full calculation including trips, vehicle capacity and minimum trip charge from `transportAssumptions`; each reason code as icon + plain phrase + its evidence (e.g., "+4.0%: average of the last 7 days vs the 7 days before"); comparison against the highest-price market when rankings disagree (gross difference, transport difference, estimated net difference, worded by ADR-0045: never "more money" unless the estimate is higher); "Only reported prices and your inputs are used — no forecast." (`decisionBasis.mlContributed: false`, CONTRACT); provenance ("About this price": modal and range, report date, freshness, variety, grade, dataset, resource id, stored-at time).

**S06 Market detail.** Market name, district, `active`, approximate-location note; `latestPrices[]` per commodity with full provenance; `noDataReason` empty state; `sourceStatus`. Prices keep their own unit (CONTRACT).

**S07 Trend.** Toggle 7 | 30 days (changes only the chart window; direction and change are the same for both, ADR-0025 item 3). Trend badge + "7-day change −8.5%" + the two window averages; chart with dots only on report days; caption "Dots are days with a government report. Lines only join reports — they are not prices for the days in between. Missing days are never filled in." (P10, §31 RULE 5); a dated list with variety and dataset per point (with no variety filter each day's point is the lower-median representative and may differ in variety — CONTRACT ADR-0010). APPROVED (PDD-09): the freshness badge shown is the latest point's; historical points show date and dataset only (§17 PDD-09). INSUFFICIENT_DATA → "Insufficient historical data for this market/commodity" (§33) with no chart line.

**S08 Forecast (optional).** Only if the ML track exists (HARD RULE 2). Title marks it as a model prediction (`kind: ML_FORECAST`); shows model name/version, horizon, predicted values and bounds as provided; no confidence percentage; `available: false` → "Forecast unavailable." (§29). Never on S04 or S09.

**S09 What-If.** Controls: transport rate ₹3–₹20/km, quantity 100–5,000 kg, distance 25–300 km (§17; ADR-0026 item 4), each showing the original value as a tick. Results recalculate on change (client-side, §10); the screen frame, every changed value and the result list use the hypothetical treatment (§6); banner "What-if — hypothetical. Reported prices are unchanged. Nothing is saved. Ranked by estimated net realization only."; transport label with the applied override rate; rank 1 labelled "Highest estimated net (what-if)", never "Recommended" (ADR-0045 item 6); break-even insight when solvable between rank 1 and the highest-price option, e.g. "Below ₹6.25/km, TEST Market B's estimated net realization would be higher than TEST Market C's." (§17 G; computed exactly from the same per-option values, `FRONTEND_CONSUMPTION.md` §3); actions "Reset to reported" and "Use these numbers on the comparison" (§17 I–J). After "use", S04 keeps the hypothetical treatment and a persistent "What-if applied · Reset" strip until reset (APPROVED PDD-11). Rules for out-of-range originals and server mode: §16.

**S10 Sign in / quick registration** (ADR-0016, ADR-0047). Shown when an anonymous user taps "Set alert" (§25 V–W), or from the account entry. Hisaab layout: paper surface, one ruled form, ink primary button. Read-only summary of the pending alert when one is waiting; "Comparing markets never needs an account. Alerts do, so AgriNex can keep them for you."; two tabs: Create account | Sign in.
- **Create account:** mobile number with a fixed "+91" (normalized to E.164, ADR-0016); **email address** (required; helper: "We'll send a link to confirm it. You need it to reset a forgotten password."); password, 8–64 characters, helper "Any characters are fine" (no composition rules), with a show/hide control; optional name; optional default district, prefilled from S03. "No SMS is sent." (§39). No other field.
- **Sign in:** one field "Mobile number or email" (either identifier of the same account; admins use their email) and the password; link "Forgot password?" → S14.
- **After success:** the pending alert is created and S11 opens. After registration, S11 shows a dismissible notice "We sent a link to {the email the user typed}. Open it to confirm your email." The email is shown only from what the user typed; the API never returns it.
- **Errors:** E14 (non-revealing), E15 (409 by field), E09 field errors, E31 (429).
- **Design reference:** the Concept A registration draft for layout only; its invented reference number and footer are excluded (packet §6.1). This section governs the content.

**S11 Price alerts.** New events first (unread), each with: crop · market, trigger value vs threshold in words, report date + freshness, checked time, variety/grade/dataset; actions "Mark as read" (sets `read_at`; the alert stays ACTIVE, §34) and "Compare markets now". Rules list: type in words (at or above / at or below / rises by / falls by), threshold with unit, market or "All markets", ACTIVE/PAUSED switch, delete (confirm by name; deletes its events, §34). Create form: alert type, crop, optional market, threshold (price in ₹/quintal or percentage magnitude). Info: "Alerts are checked each time AgriNex receives new government prices (scheduled every 6 hours). You see them here in the app — AgriNex does not send SMS. A paused alert is not checked." For an all-markets alert, events name the market with the most extreme qualifying value (ADR-0019 item 3).

**S12 Account menu.** "Signed in as {displayName}" (or "Signed in" when null — CONTRACT `AuthenticatedUser.displayName`), language, sign out. Phone number and email are not shown (the API returns neither). While `AuthenticatedUser.emailVerified` is false: "Email not confirmed yet. You need a confirmed email to reset a forgotten password." with the action "Send a new link" (`resendEmailVerification`, 202 → "We sent a new link. Earlier links no longer work."). When true, nothing is shown.

**S14 Forgot password** (ADR-0047, ADR-0048). Title "Reset your password". One field "Mobile number or email" and the button "Send reset link". Helper: "If your account has a confirmed email, we will send a link to it." After the 202 the screen always shows the same message, whatever was typed: "If an account with a confirmed email matches, a link is on its way. It works for 30 minutes. Check your email, including spam." with "Back to sign in". It never says whether an account exists. Errors: E09 (empty or too long field), E31 (429), E20 (network).

**S15 New password** (opened from the reset link; ADR-0048). The page reads the token from the link's fragment and removes it from the address bar at once; it never logs or stores it, and is served with `Referrer-Policy: no-referrer` (`FRONTEND_CONSUMPTION.md` §4). Title "Choose a new password"; one password field with show/hide and the helper "8 to 64 characters. Any characters are fine."; button "Save new password". On 204: "Password changed. Please sign in with your new password." → S10 Sign in; every earlier session is signed out, and the user is not signed in automatically. On 422 (E26): "This link no longer works. It may have expired or been used. Ask for a new link." → S14. An admin account's 15-character minimum appears as a field error (E09 TOO_SHORT).

**S16 Email confirmation** (opened from the verification link; ADR-0048). Reads and removes the token like S15, then calls `confirmEmailVerification` at once, showing a busy state. On 204 (E30): "Your email is confirmed." with "Continue" (to the alerts if signed in, otherwise to the home screen). On 422 (E27): "This link no longer works. If your email is already confirmed, there is nothing more to do." with "Send a new link" when signed in (S12 action) or "Sign in" otherwise.

**A01 Admin data health.** Failure banner when any dataset has `consecutiveFailures > 0` (ADR-0019 item 7); per dataset: status of the latest completed run, last success time, latest report date, counters (fetched, inserted, updated, duplicates skipped, validation errors, conflicts, completeness, pages, duration), schedule paused/active with reason; market freshness distribution; system: database and Redis status, government API key CONFIGURED / NOT CONFIGURED (never the value, ADR-0020 item 2), schedule and coverage; recent runs; actions: trigger (dataset, optional date range for HISTORICAL_PRICE), pause/resume. English only; desktop-first; admin screens may show API `detail` text (FRONTEND_CONSUMPTION §5).

**A02–A05.** Run list and run detail with counters and the error page (raw-payload excerpts; `detail` allowed); 409 on trigger while a run is RUNNING ("A run of this dataset is still running."); configuration forms that create a new version on save, with the version history and validation (> 0 for transport values; weights 0–1; fresh ≤ recent ≤ stale days; ADR-0020 item 1); curation lists with evidence notes required on merge and aliases; audit log with masked before/after details. No secrets, schedules, rate limits or CORS settings are editable (ADR-0020 item 3). No erasure, deactivation or reactivation controls (ADR-0037, ADR-0041).

## 9. States and errors

"Loading" design belongs to Task 9 (`FRONTEND_ARCHITECTURE.md` §4.8). Copy is EN; MR/HI per §10.

| # | Trigger (CONTRACT) | UI behaviour | Copy (EN) |
|---|---|---|---|
| E01 | Request in flight | APPROVED (PDD-13): button shows busy state at once; skeleton of the results layout if the response takes longer than 300 ms; never a spinner over stale results | — |
| E02 | `sourceStatus.latestSyncFailed: true` | Banner above content; content still shown with true dates and freshness | §7 outage banner (spec §29) |
| E03 | Prices STALE | Stale badge on each; `STALE_DATA` reason; note "These prices are old. They may have changed since they were reported." | — |
| E04 | 200 `options: []`, `noDataReason: NO_RECENT_PRICE_DATA` | Empty state listing the excluded markets with ≈distance; actions "Choose another crop", "Try a larger distance" | "No recent market observation available for this commodity in your area." (§29) |
| E05 | 422 `NO_MARKETS_IN_RANGE` | Empty state with distance chips + "Search again within {n} km" | "No data in range. Try increasing distance." (§25) |
| E06 | 422 `UNSUPPORTED_PRICE_UNIT` | Field-level on crop | "Prices for this crop are not reported per quintal, so AgriNex cannot compare it yet. Choose another crop." |
| E07 | 422 `ESTIMATE_OUT_OF_RANGE` (field `quantity`) | Field error on quantity | "This quantity is too large to estimate. Enter a smaller quantity." (ADR-0040) |
| E08 | 422 `REFERENCE_NOT_FOUND` | Re-fetch reference lists; ask to choose again | "This choice is no longer available. Please choose again." |
| E09 | 400 `VALIDATION_FAILED` on fields | Inline per field from `errors[].field` + `code` (ADR-0022 item 11); never `detail` | e.g. REQUIRED "Enter the quantity." · OUT_OF_RANGE "Enter a number greater than 0." · PRECISION_EXCEEDED "Use at most 3 decimal places." |
| E10 | 400 on `originLat/originLon` (non-centroid, ADR-0039) | Clear cached geography; return to the district step | "We could not use that location. Please choose your district again." |
| E11 | 429 `RATE_LIMITED` + `Retry-After` | Disable submit; countdown from `Retry-After` | "Too many comparisons right now. You can try again in {n} seconds." |
| E12 | 401 on an anonymous-capable operation | Refresh once; if refused, retry without token (FRONTEND_CONSUMPTION §4) — invisible to the user | — |
| E13 | 401 / `REFRESH_TOKEN_INVALID` on alerts | Sign-in screen, keeping the user's place | "Please sign in again to see your alerts." |
| E14 | 401 `INVALID_CREDENTIALS` | Form error, non-revealing (SR-15) | "Mobile number, email or password is not correct." (ADR-0047) |
| E15 | 409 on registration (`errors[].field` is `phoneNumber` or `email`; SR-01, extended to email by ADR-0048) | Field error on the named field(s); offer sign-in | Phone: "This mobile number is already registered. Sign in instead." Email: "This email is already registered. Sign in instead." |
| E16 | 403 `FORBIDDEN` | Admin area for non-admin | "You do not have access to this page." |
| E17 | 404 market (provisional/merged) | Message + back to comparison | "This market is not available." |
| E18 | 503 `SERVICE_UNAVAILABLE` | Standard error page (§29) with retry and correlation id in small print | "AgriNex is temporarily unavailable. Please try again in a few minutes." |
| E19 | 500 / unknown code | Fallback by HTTP status; correlation id | "Something went wrong on our side. Please try again." |
| E20 | Network failure (no response) | Keep inputs; retry | "Could not reach AgriNex. Check your connection and try again." (no contract state) |
| E21 | Trend `INSUFFICIENT_DATA` | No line; message | "Insufficient historical data for this market/commodity" (§33) |
| E22 | Forecast `available: false` | Only in S08 | "Forecast unavailable." (§29) |
| E23 | Never synchronized (`lastSuccessfulSyncAt: null`, `latestSyncFailed: false`) | Empty states as E04 | "AgriNex has not received government prices yet." |
| E24 | Current dataset never succeeded, failed (`null` + `true`) | Outage banner (E02); historical prices with true dates | §7 outage banner |
| E25 | Alerts `items: []` | Empty state with "+ New price alert" | "You have no price alerts yet." |
| E26 | 422 `PASSWORD_RESET_TOKEN_INVALID` | S15: offer a new link (S14) | "This link no longer works. It may have expired or been used. Ask for a new link." (ADR-0048) |
| E27 | 422 `EMAIL_VERIFICATION_TOKEN_INVALID` | S16: offer "Send a new link" (signed in) or "Sign in" | "This link no longer works. If your email is already confirmed, there is nothing more to do." (ADR-0048) |
| E28 | 202 from `requestPasswordReset` | S14: the same message for every identifier | "If an account with a confirmed email matches, a link is on its way. It works for 30 minutes. Check your email, including spam." (ADR-0048) |
| E29 | 204 from `confirmPasswordReset` | Go to S10 Sign in, with the message; discard any in-memory token | "Password changed. Please sign in with your new password." (ADR-0048) |
| E30 | 204 from `confirmEmailVerification` | S16 success | "Your email is confirmed." (ADR-0048) |
| E31 | 429 `RATE_LIMITED` on an account operation (login, register, reset, verification) + `Retry-After` | Disable the button; countdown | "Too many attempts. You can try again in {n} seconds." (ADR-0048) |

## 10. Copy deck (key strings)

**Owner and status (ADR-0046).** Claude, the AI assistant maintaining this repository, authors and reviews all Marathi (MR) and Hindi (HI) interface copy; no separate human reviewer is required. EN strings marked SSOT are verbatim from the spec or an ADR. MR strings marked "spec §46" are the spec's own phrases and are used verbatim. Every other MR and HI string below was reviewed by Claude on 2026-10-04 against the approved EN meaning, the product behaviour and the glossary (§10.1); corrections made in that review are listed in §10.3. The remaining strings (including the §9 rows) are produced by Claude under the same rules during frontend implementation, and every farmer-facing string must exist in all three languages before the §48 language sign-off.

### 10.1 Glossary (binding)

One English term has one translation per language. Product claims stay claims of the same strength: an estimate is never translated as a fact.

| EN term | MR | HI | Note |
|---|---|---|---|
| Latest reported price | नवीनतम नोंदवलेला भाव | नवीनतम दर्ज भाव | Never "live" or "real-time" (§31 RULE 4) |
| Estimated | अंदाजे | अनुमानित | Every derived amount |
| Estimated net realization | अंदाजे निव्वळ मिळकत | अनुमानित शुद्ध प्राप्ति | Always with the gloss below on first use per screen (§10.3 A-01) |
| money after transport (gloss) | वाहतुकीनंतर उरणारे पैसे | ढुलाई के बाद बचने वाली रकम | |
| Transport (cost) | वाहतूक (खर्च) | ढुलाई (खर्च) | MR from spec §46 |
| Assumption (transport rate) | गृहीतक | मानकर (… मानकर) | §10.3 A-02 |
| Recommended | शिफारस केलेले | सुझाया गया | ADR-0045 |
| Highest price | सर्वाधिक भाव | सबसे ऊँचा भाव | |
| Highest estimated net realization | सर्वाधिक अंदाजे निव्वळ मिळकत | सबसे अधिक अनुमानित शुद्ध प्राप्ति | ADR-0045 |
| Price trend | भावाचा कल | भाव का रुझान | |
| Fresh / Recent / Stale / Unavailable | ताजा / अलीकडील / जुना / उपलब्ध नाही | ताज़ा / हाल का / पुराना / उपलब्ध नहीं | |
| What-if (hypothetical) | काल्पनिक | काल्पनिक | §10.3 A-03 |
| Confirmed (verified) email | पडताळलेला ईमेल | सत्यापित ईमेल | §10.3 A-04 |
| Sign in | साइन इन | साइन इन | Common loanword in Indian apps |
| quintal; kg; km | क्विंटल; किलो; किमी | क्विंटल; किलो; किमी | Digits stay Latin (PDD-06) |

### 10.2 Key strings

| Key | EN | MR | HI |
|---|---|---|---|
| price.latest | Latest reported price (SSOT) | नवीनतम नोंदवलेला भाव | नवीनतम दर्ज भाव |
| net.label | Estimated net realization (SSOT) | अंदाजे निव्वळ मिळकत | अनुमानित शुद्ध प्राप्ति |
| net.gloss | money after transport | वाहतुकीनंतर उरणारे पैसे | ढुलाई के बाद बचने वाली रकम |
| transport.label | Estimated — based on ₹{rate}/km assumption. Actual costs will vary. (SSOT, ADR-0038) | अंदाजे — ₹{rate}/किमी गृहीतकावर आधारित. प्रत्यक्ष खर्च वेगळा असेल. | अनुमानित — ₹{rate}/किमी मानकर। वास्तविक खर्च अलग होगा। |
| transport.bar_caption | Estimated transport, on one scale for all markets (₹0 to ₹{max}) | अंदाजे वाहतूक खर्च, सर्व बाजारांसाठी एकाच मापावर (₹0 ते ₹{max}) | अनुमानित ढुलाई खर्च, सभी बाज़ारों के लिए एक ही पैमाने पर (₹0 से ₹{max}) |
| badge.recommended | Recommended (ADR-0045) | शिफारस केलेले | सुझाया गया |
| badge.highest_price | Highest price | सर्वाधिक भाव | सबसे ऊँचा भाव |
| badge.highest_net | Highest estimated net realization (ADR-0045) | सर्वाधिक अंदाजे निव्वळ मिळकत | सबसे अधिक अनुमानित शुद्ध प्राप्ति |
| answer.title | Recommended: {market} | शिफारस केलेले: {market} | सुझाया गया: {market} |
| answer.net | ≈₹{net} estimated net realization — money left after estimated transport | ≈₹{net} अंदाजे निव्वळ मिळकत — अंदाजे वाहतूक खर्चानंतर उरणारे पैसे | ≈₹{net} अनुमानित शुद्ध प्राप्ति — अनुमानित ढुलाई खर्च के बाद बचने वाली रकम |
| answer.basis_link | How is this ranked? | हा क्रम कसा ठरतो? | यह क्रम कैसे तय होता है? |
| rank.basis | Ranked mainly by estimated net realization, also weighing price, trend, data freshness and transport. (ADR-0045) | क्रम मुख्यतः अंदाजे निव्वळ मिळकतीनुसार; भाव, भावाचा कल, माहिती किती ताजी आहे आणि वाहतूक यांचाही विचार केला आहे. | क्रम मुख्य रूप से अनुमानित शुद्ध प्राप्ति के अनुसार; भाव, भाव का रुझान, जानकारी कितनी ताज़ा है और ढुलाई को भी ध्यान में रखा गया है। |
| callout.title | Why the highest price is not ranked first | सर्वाधिक भावाचा बाजार पहिल्या क्रमांकावर का नाही | सबसे ऊँचे भाव वाला बाज़ार पहले स्थान पर क्यों नहीं |
| callout.more | {P} has the highest price (₹{price}/quintal), but after estimated transport {R} may leave about ₹{n} more. | {P} येथे भाव सर्वाधिक आहे (₹{price}/क्विंटल), पण अंदाजे वाहतूक खर्चानंतर {R} येथे सुमारे ₹{n} जास्त उरू शकतात. | {P} में भाव सबसे ऊँचा है (₹{price}/क्विंटल), लेकिन अनुमानित ढुलाई खर्च के बाद {R} में लगभग ₹{n} ज़्यादा बच सकते हैं। |
| callout.not_more | {P} has the highest price and about ₹{n} more estimated net realization, but {R} is ranked first because the ranking also weighs price trend, data freshness and transport. | {P} येथे भाव सर्वाधिक आहे आणि अंदाजे निव्वळ मिळकतही सुमारे ₹{n} जास्त आहे, पण क्रम ठरवताना भावाचा कल, माहिती किती ताजी आहे आणि वाहतूक यांचाही विचार होतो, म्हणून {R} पहिल्या क्रमांकावर आहे. | {P} में भाव सबसे ऊँचा है और अनुमानित शुद्ध प्राप्ति भी लगभग ₹{n} ज़्यादा है, लेकिन क्रम तय करते समय भाव का रुझान, जानकारी कितनी ताज़ा है और ढुलाई को भी देखा जाता है, इसलिए {R} पहले स्थान पर है। |
| callout.equal | {P} has the highest price and the same estimated net realization, but {R} is ranked first because the ranking also weighs price trend, data freshness and transport. | {P} येथे भाव सर्वाधिक आहे आणि अंदाजे निव्वळ मिळकत तेवढीच आहे, पण क्रम ठरवताना भावाचा कल, माहिती किती ताजी आहे आणि वाहतूक यांचाही विचार होतो, म्हणून {R} पहिल्या क्रमांकावर आहे. | {P} में भाव सबसे ऊँचा है और अनुमानित शुद्ध प्राप्ति भी उतनी ही है, लेकिन क्रम तय करते समय भाव का रुझान, जानकारी कितनी ताज़ा है और ढुलाई को भी देखा जाता है, इसलिए {R} पहले स्थान पर है। |
| callout.arith_less | +₹{gross} from the higher price · −₹{transport} more estimated transport = ₹{n} less at {P} | जास्त भावामुळे +₹{gross} · जास्त अंदाजे वाहतूक खर्च −₹{transport} = {P} येथे ₹{n} कमी | ऊँचे भाव से +₹{gross} · ज़्यादा अनुमानित ढुलाई खर्च −₹{transport} = {P} में ₹{n} कम |
| callout.arith_more | +₹{gross} from the higher price · −₹{transport} more estimated transport = ₹{n} more at {P} | जास्त भावामुळे +₹{gross} · जास्त अंदाजे वाहतूक खर्च −₹{transport} = {P} येथे ₹{n} जास्त | ऊँचे भाव से +₹{gross} · ज़्यादा अनुमानित ढुलाई खर्च −₹{transport} = {P} में ₹{n} ज़्यादा |
| fresh.FRESH / RECENT / STALE / UNAVAILABLE | Fresh / Recent / Stale / Unavailable | ताजा / अलीकडील / जुना / उपलब्ध नाही | ताज़ा / हाल का / पुराना / उपलब्ध नहीं |
| trend.RISING / STABLE / FALLING / INSUFFICIENT_DATA | Rising / Stable / Falling / Not enough data | वाढ / स्थिर / घसरण / पुरेशी माहिती नाही | बढ़त / स्थिर / गिरावट / पर्याप्त जानकारी नहीं |
| reason.PRICE_ADVANTAGE | Higher price than most markets here | इथल्या बहुतेक बाजारांपेक्षा जास्त भाव | यहाँ के ज़्यादातर बाज़ारों से ऊँचा भाव |
| reason.LOWER_TRANSPORT | Lower transport cost | कमी वाहतूक खर्च (spec §46) | कम ढुलाई खर्च |
| reason.POSITIVE_TREND | Price rising this week | भाव वाढत आहेत | भाव बढ़ रहे हैं |
| reason.STABLE_TREND | Price steady this week | स्थिर भाव (spec §46) | भाव स्थिर हैं |
| reason.NEGATIVE_TREND | Price falling this week | भाव घसरत आहेत | भाव गिर रहे हैं |
| reason.RECENT_DATA | Recently reported price | अलीकडील माहिती (spec §46) | हाल की जानकारी |
| reason.STALE_DATA | Old price report | जुनी माहिती | पुरानी जानकारी |
| reason.SUITABLE_DISTANCE | Within 50 km | 50 किमीच्या आत | 50 किमी के भीतर |
| state.outage | (SSOT §29, see §7) | स्रोताशी नवीनतम समक्रमण उपलब्ध नाही. सर्वात अलीकडे जतन केलेले सरकारी नोंदवलेले भाव दाखवत आहोत. | स्रोत से नवीनतम समकालन उपलब्ध नहीं है। सबसे हाल में सहेजे गए सरकारी दर्ज भाव दिखाए जा रहे हैं। |
| state.no_market | No data in range. Try increasing distance. (SSOT §25) | या अंतरात माहिती नाही. अंतर वाढवून पाहा. | इस दूरी में कोई जानकारी नहीं। दूरी बढ़ाकर देखें। |
| state.no_recent | No recent market observation available for this commodity in your area. (SSOT §29) | तुमच्या भागात या पिकासाठी अलीकडील बाजारभाव उपलब्ध नाही. | आपके क्षेत्र में इस फसल का हाल का कोई बाज़ार भाव उपलब्ध नहीं है। |
| state.insufficient_history | Insufficient historical data for this market/commodity (SSOT §33) | या बाजार/पिकासाठी पुरेशी पूर्वीची माहिती नाही | इस बाज़ार/फसल के लिए पर्याप्त पिछली जानकारी नहीं है |
| state.forecast_unavailable | Forecast unavailable. (SSOT §29) | पूर्वानुमान उपलब्ध नाही. | पूर्वानुमान उपलब्ध नहीं। |
| whatif.stamp | What-if | काल्पनिक | काल्पनिक |
| whatif.banner | What-if — hypothetical. Reported prices are unchanged. Nothing is saved. Ranked by estimated net realization only. | काल्पनिक गणना — नोंदवलेले भाव बदलत नाहीत. काहीही जतन होत नाही. क्रम केवळ अंदाजे निव्वळ मिळकतीनुसार. | काल्पनिक गणना — दर्ज भाव नहीं बदलते। कुछ भी सहेजा नहीं जाता। क्रम केवल अनुमानित शुद्ध प्राप्ति के अनुसार। |
| whatif.top | Highest estimated net (what-if) (ADR-0045) | सर्वाधिक अंदाजे निव्वळ मिळकत (काल्पनिक) | सबसे अधिक अनुमानित शुद्ध प्राप्ति (काल्पनिक) |
| disclaimer | (SSOT §49, see §7) | बाजारभाव, वाहतूक अंदाज आणि पूर्वानुमान अपूर्ण, उशिरा आलेले, सुधारित किंवा प्रत्यक्ष व्यवहाराच्या परिस्थितीपेक्षा वेगळे असू शकतात. AgriNex निर्णयासाठी मदत करणारी माहिती देते आणि विक्री भाव, वाहतूक दर, नफा किंवा भविष्यातील बाजारातील बदल यांची हमी देत नाही. | बाज़ार भाव, ढुलाई के अनुमान और पूर्वानुमान अधूरे, देर से आए, संशोधित या वास्तविक लेन-देन की स्थितियों से अलग हो सकते हैं। AgriNex निर्णय में सहायक जानकारी देता है और बिक्री भाव, ढुलाई दर, मुनाफ़े या भविष्य के बाज़ार बदलाव की कोई गारंटी नहीं देता। |
| action.compare / action.find | Compare markets / Find markets | बाजारांची तुलना / बाजार शोधा | बाज़ारों की तुलना / बाज़ार खोजें |
| auth.tab_create / auth.tab_signin | Create account / Sign in | खाते तयार करा / साइन इन करा | खाता बनाएँ / साइन इन करें |
| auth.phone | Mobile number | मोबाइल नंबर | मोबाइल नंबर |
| auth.email | Email address | ईमेल पत्ता | ईमेल पता |
| auth.email_help | We'll send a link to confirm it. You need it to reset a forgotten password. | याची खात्री करण्यासाठी आम्ही एक लिंक पाठवू. पासवर्ड विसरल्यास तो बदलण्यासाठी हा ईमेल लागतो. | इसकी पुष्टि के लिए हम एक लिंक भेजेंगे। पासवर्ड भूलने पर उसे बदलने के लिए यह ईमेल ज़रूरी है। |
| auth.password_help | 8 to 64 characters. Any characters are fine. | 8 ते 64 अक्षरे. कोणतीही अक्षरे चालतील. | 8 से 64 अक्षर। कोई भी अक्षर चलेंगे। |
| auth.no_sms | No SMS is sent. | कोणताही SMS पाठवला जात नाही. | कोई SMS नहीं भेजा जाता। |
| auth.identifier | Mobile number or email | मोबाइल नंबर किंवा ईमेल | मोबाइल नंबर या ईमेल |
| auth.forgot | Forgot password? | पासवर्ड विसरलात? | पासवर्ड भूल गए? |
| auth.invalid_credentials (E14) | Mobile number, email or password is not correct. | मोबाइल नंबर, ईमेल किंवा पासवर्ड चुकीचा आहे. | मोबाइल नंबर, ईमेल या पासवर्ड गलत है। |
| auth.phone_taken / auth.email_taken (E15) | This mobile number is already registered. Sign in instead. / This email is already registered. Sign in instead. | हा मोबाइल नंबर आधीच नोंदवलेला आहे. त्याऐवजी साइन इन करा. / हा ईमेल आधीच नोंदवलेला आहे. त्याऐवजी साइन इन करा. | यह मोबाइल नंबर पहले से दर्ज है। इसके बजाय साइन इन करें। / यह ईमेल पहले से दर्ज है। इसके बजाय साइन इन करें। |
| reset.title | Reset your password | पासवर्ड पुन्हा सेट करा | पासवर्ड दोबारा सेट करें |
| reset.help | If your account has a confirmed email, we will send a link to it. | तुमच्या खात्याचा ईमेल पडताळलेला असल्यास त्यावर आम्ही लिंक पाठवू. | अगर आपके खाते का ईमेल सत्यापित है, तो हम उस पर लिंक भेजेंगे। |
| reset.sent (E28) | If an account with a confirmed email matches, a link is on its way. It works for 30 minutes. Check your email, including spam. | जुळणारे खाते आणि पडताळलेला ईमेल असल्यास लिंक पाठवली जात आहे. ती 30 मिनिटे चालेल. तुमचा ईमेल पाहा, स्पॅम फोल्डरसुद्धा. | अगर सत्यापित ईमेल वाला कोई खाता मेल खाता है, तो लिंक भेजा जा रहा है। यह 30 मिनट तक काम करेगा। अपना ईमेल देखें, स्पैम फ़ोल्डर भी। |
| reset.new_title | Choose a new password | नवीन पासवर्ड निवडा | नया पासवर्ड चुनें |
| reset.done (E29) | Password changed. Please sign in with your new password. | पासवर्ड बदलला. कृपया नवीन पासवर्डने साइन इन करा. | पासवर्ड बदल गया। कृपया नए पासवर्ड से साइन इन करें। |
| reset.invalid (E26) | This link no longer works. It may have expired or been used. Ask for a new link. | ही लिंक आता चालत नाही. तिची मुदत संपली असेल किंवा ती आधीच वापरली असेल. नवीन लिंक मागवा. | यह लिंक अब काम नहीं करता। इसकी अवधि खत्म हो गई होगी या यह पहले इस्तेमाल हो चुका होगा। नया लिंक मँगवाएँ। |
| verify.sent | We sent a link to {email}. Open it to confirm your email. | आम्ही {email} वर लिंक पाठवली आहे. ईमेलची खात्री करण्यासाठी ती उघडा. | हमने {email} पर लिंक भेजा है। ईमेल की पुष्टि के लिए उसे खोलें। |
| verify.done (E30) | Your email is confirmed. | तुमचा ईमेल पडताळला गेला. | आपका ईमेल सत्यापित हो गया। |
| verify.invalid (E27) | This link no longer works. If your email is already confirmed, there is nothing more to do. | ही लिंक आता चालत नाही. तुमचा ईमेल आधीच पडताळलेला असल्यास आणखी काही करायची गरज नाही. | यह लिंक अब काम नहीं करता। अगर आपका ईमेल पहले से सत्यापित है, तो कुछ और करने की ज़रूरत नहीं। |
| verify.unconfirmed (S12) | Email not confirmed yet. You need a confirmed email to reset a forgotten password. | ईमेल अजून पडताळलेला नाही. पासवर्ड विसरल्यास तो बदलण्यासाठी पडताळलेला ईमेल लागतो. | ईमेल अभी सत्यापित नहीं है। पासवर्ड भूलने पर उसे बदलने के लिए सत्यापित ईमेल ज़रूरी है। |
| verify.resend / verify.resent | Send a new link / We sent a new link. Earlier links no longer work. | नवीन लिंक पाठवा / आम्ही नवीन लिंक पाठवली. आधीच्या लिंक आता चालणार नाहीत. | नया लिंक भेजें / हमने नया लिंक भेजा। पहले के लिंक अब काम नहीं करेंगे। |
| auth.too_many (E31) | Too many attempts. You can try again in {n} seconds. | खूप प्रयत्न झाले. {n} सेकंदांनी पुन्हा प्रयत्न करा. | बहुत ज़्यादा कोशिशें हुईं। {n} सेकंड बाद फिर कोशिश करें। |

### 10.3 Ambiguity and review register (ADR-0046)

| # | Term or string | Decision | Status |
|---|---|---|---|
| A-01 | "Net realization" has no everyday equivalent | MR "निव्वळ मिळकत", HI "शुद्ध प्राप्ति", always with the plain gloss "money after transport" in the same language. Meaning unchanged: an estimate of money left after estimated transport | Decided; watch in the §36 Experiment 4 usability study if it runs |
| A-02 | "assumption" in the transport label | HI draft "की मान्यता पर आधारित" meant "based on the belief/recognition of"; corrected to "₹{rate}/किमी मानकर" ("taking ₹{rate}/km"). MR "गृहीतक" kept | Corrected 2026-10-04 |
| A-03 | "What-if" | The drafts' MR "जर-तर" is an idiom for speculation ("ifs and buts") and HI "क्या-अगर" a calque. Both replaced by "काल्पनिक" (hypothetical), which keeps the meaning in both languages | Corrected 2026-10-04 |
| A-04 | "verified" vs "confirmed" email | EN copy says "confirm(ed)", which is plainer; MR "पडताळलेला", HI "सत्यापित". The API term stays `emailVerified` | Decided |
| A-05 | "synchronization" in the §29 outage banner | MR "समक्रमण" and HI "समकालन" are technical words. They are kept because the banner is mandatory SSOT text and a plainer paraphrase could shift its meaning | Flagged: a plainer wording needs the owner's approval of an EN wording change first |
| A-06 | reason.PRICE_ADVANTAGE drafts ("जास्त भाव", "ऊँचा भाव") dropped "than most markets here" | Corrected to keep the comparison: "इथल्या बहुतेक बाजारांपेक्षा जास्त भाव", "यहाँ के ज़्यादातर बाज़ारों से ऊँचा भाव" | Corrected 2026-10-04 |
| A-07 | "historical data" drafts ("पुरेशी जुनी माहिती", "पर्याप्त पुरानी जानकारी") read as "enough old information" | Corrected to "पूर्वीची माहिती" / "पिछली जानकारी" (earlier information) | Corrected 2026-10-04 |
| A-08 | "Sign in" | Kept as the loanword "साइन इन", common in Indian apps; "प्रवेश" or "लॉग इन" were considered | Decided |
| A-09 | transport.label "Actual costs will vary." (SSOT, ADR-0038) | The MR "वेगळा असू शकतो" and HI "अलग हो सकता है" said "may differ", which weakens a mandatory disclaimer. Corrected to the same strength: MR "प्रत्यक्ष खर्च वेगळा असेल.", HI "वास्तविक खर्च अलग होगा।" | Corrected 2026-10-15 (Task 12 closure, ADR-0052 item 6) |
| A-10 | reset.sent (E28) "matches" | HI "मिलता-जुलता खाता" means "a similar account". Corrected to "अगर सत्यापित ईमेल वाला कोई खाता मेल खाता है" (an account with a confirmed email matches) | Corrected 2026-10-15 |
| A-11 | input.distance_help "Default 150 km" | MR "मूळ अंतर" (original distance) and HI "सामान्य दूरी" (normal distance) could read as a recommended distance. Corrected to "न बदलल्यास 150 किमी" / "न बदलने पर 150 किमी" (150 km if you do not change it) | Corrected 2026-10-15 |
| A-12 | alerts.dismiss and alerts.delete_confirm (HI) | "Dismiss" used "हटाएँ", the same word as "Delete" on the same screen; corrected to "बंद करें" (close), as in MR. "Its events" used the calque "घटनाएँ"; corrected to "इससे मिले अलर्ट" (alerts received from it), the word the screen uses for events | Corrected 2026-10-15 |
| A-13 | Trend reasons "Price rising / steady / falling this week" | MR and HI omit "this week", following the spec §46 phrase "स्थिर भाव", which also omits it. The trend window is shown beside the reason ("7-day change") | Decided 2026-10-15 |
| A-14 | Account emails (ADR-0052 item 7) | One plain-text email in English, then Marathi, then Hindi, with the link once. MR and HI reuse the catalogue terms (लिंक, पडताळणे / सत्यापित, पासवर्ड, खाते / खाता, रीसेट लिंक, साइन इन) and give the lifetime as "24 तासांत" / "24 घंटे में" | Decided 2026-10-15 |

**Task 12 closure review (2026-10-15).** Claude re-reviewed all 378 keys of `frontend/public/locales/{mr,hi}.json` against the English meaning and §10.1, and ran automated checks: the same keys in all three files, matching placeholders, no empty values, no Devanagari digits, glossary terms present, the spec §46 phrases verbatim, no "live" or "real-time" wording, and consistent spellings (HI nukta forms, "भाव" for price, "ढुलाई" for transport). A-09 to A-12 were corrected. **No native speaker has reviewed the copy.** ADR-0046 does not require one; a native-speaker review is a recommended future enhancement, not a completed step.

## 11. Localization and formatting

- SSOT: MR, HI and EN on every farmer-facing screen; 0 untranslated hardcoded strings (§27, §48). Admin is English (§6).
- SSOT (ADR-0015): state and commodity names use `nameMr`/`nameHi` when not null, otherwise `name`; district, market, variety and grade names are shown as published. APPROVED (PDD-05): a fallback name is shown as-is, marked up with `lang="en"`, with no "untranslated" badge.
- APPROVED (PDD-06): Latin digits in all three languages (persona: "English (numbers only)"), Indian digit grouping (₹1,02,450), rupee sign before the amount.
- APPROVED (PDD-07): estimates shown to the whole rupee with "≈" (API carries 2 dp); reported prices shown as reported, with paise only when non-zero (₹3,037.40); percentages to one decimal (ADR-0025 item 1); distances to whole km with "≈".
- APPROVED (PDD-08): dates as "2 Oct 2026" with localized month abbreviations; times 24-hour with "IST" (ADR-0007: Asia/Kolkata).
- APPROVED (PDD-04): the attribution string is shown exactly as the API sends it (`price.attribution`, English proper nouns), with `lang="en"`, in every locale.
- Devanagari typography: line height ≥ 1.5 (1.6 preferred), no letter-spacing on Devanagari, text containers sized for about 40 % expansion over English.

## 12. Accessibility (WCAG 2.1 AA target for core flows — F1.22, §27)

- Contrast evidence (Task 9 computation) for the approved Hisaab values: every text/background pair passes 4.5:1 (lowest: rising green on paper 4.90:1; stale amber on its surface 5.74:1; What-If indigo on paper 8.48:1). Decorative hairlines (#D9CFBF, 1.43:1) are **never** the only boundary of a control or chart mark; controls use `border.strong` (#8C8070, 3.58:1) and the transport bar uses `chart.transport.fill` (#5A5248, 7.12:1). The B and C reference palettes were also checked (lowest C stale amber on white 4.67:1) but are not implemented.
- Status never by colour alone: freshness, trend, reason caution, What-If and outage each carry an icon and a word.
- Touch targets ≥ 48 × 48 px (B: 56–64 px primary buttons); 8 px minimum spacing between targets.
- Visible labels on every input; placeholders only as format hints; errors next to the field, in text, announced (`aria-live="polite"`), never only red.
- Focus: visible 2 px focus ring with 2 px offset, ≥ 3:1 against the surface; logical order; skip link on desktop; overlays trap and restore focus.
- Charts: every chart has a text equivalent (the dated list on S07; the equation lines in C); the trend direction and change are stated in text.
- Reduced motion: re-ranking in What-If cross-fades instead of moving rows when `prefers-reduced-motion` is set.
- Minimum text 12 px (14 px body, 16–18 px body for B); `lang` attributes per language; screen-reader text for "≈" ("approximately").

## 13. Responsive behaviour

- SSOT: no horizontal scroll at 375 px, all elements visible (§10, §48); browser floor Chrome/Edge 111+, Safari 16.4+, Firefox 128+; Android 7.0+ via Chrome (ADR-0004); LCP < 3 s on 3G (§27).
- APPROVED breakpoints: < 600 px single column; 600–1023 px two columns where useful (tablet); ≥ 1024 px desktop layouts. Hisaab (§1.1): ledger cards below 1024 px, the comparison sheet with markets as columns at 1024 px and wider; the input is one question per step below 1024 px and a single panel beside the results at 1024 px and wider; the answer strip sits at the top at every width. EXPLORATION, not adopted: B's centred two-column layout and C's three-panel workbench.
- Mobile-first content priority follows §8 S04 ordering; desktop adds side-by-side context, never new data.
- No web fonts block first render (font-display: swap); no imagery or map tiles (none are used).

## 14. Design tokens

### 14.1 Semantic layer (all directions)

| Token | Meaning |
|---|---|
| `color.surface.page` / `.raised` / `.sunken` | Backgrounds |
| `color.text.primary` / `.secondary` / `.inverse` | Text |
| `color.border.subtle` | Decorative rules only (may be < 3:1) |
| `color.border.strong` | Controls, chart marks (≥ 3:1) |
| `color.action.primary.bg` / `.fg` | Primary buttons |
| `color.value.net` | Estimated net realization emphasis |
| `color.value.hypothetical` / `.hypothetical-surface` | What-If only |
| `color.status.{fresh,recent,stale,unavailable}.{fg,bg}` | Freshness badges |
| `color.trend.{rising,stable,falling,insufficient}` | Trend (spec §33: rising green, stable grey, falling red) |
| `color.signal.outage.{fg,bg,border}` / `color.signal.error` | Banners and errors |
| `color.chart.transport.fill` / `.track` | The shared-scale transport bar (§1.1): fill ≥ 3:1 against the surface; the track is decorative |
| `color.focus.ring` | Focus indicator |
| `radius.control` / `radius.card` | Corner radii |
| `space.1…12` | 4-pt scale (4, 8, 12, 16, 20, 24, 32, 40, 48, 56, 64, 80) |
| `type.{display,title,heading,body,label,caption,numeric}` | Type roles |
| `motion.duration.{fast,base}` | 120 ms / 200 ms; none under reduced motion |

### 14.2 Approved values (Hisaab, ADR-0043) and reference values

The **A Hisaab** column holds the approved token values. The B and C columns are EXPLORATION reference only and are not implemented.

| Token | A Hisaab (ledger) — APPROVED | B guided helper — reference | C trade-off board — reference |
|---|---|---|---|
| surface.page | #FAF6EE | #FFFFFF | #FFFFFF |
| text.primary / secondary | #1D1A16 / #5A5248 | #16201C / #4A5550 | #0E0E0E / #5B616B |
| border.subtle / strong | #D9CFBF / #8C8070 | #E1E6E3 / #7A847F | #E3E5E8 / #6B7280 |
| action.primary | #1E5B3A on white | #0B5D45 on white | #0E0E0E on white |
| value.net | #1E5B3A | #0B5D45 | #0A7A4B |
| value.hypothetical | #3D3A9E (dashed outline) | #5B3CC4 on #F1ECFF | #5B2EDB |
| status.stale | #8A5300 on #FFF3D6 | #8A5300 on #FFF3D6 | #B25E00 on white |
| trend.falling / rising | #B42318 / #1E7B45 | #B42318 / #1E7B45 | #B42318 / #0A7A4B |
| highlight (highest price) | ink outline label | #F2B33D fill behind dark text | black outline tag |
| radius control / card | 4 / 4 | 999 (pill) / 16–20 | 4 / 4 |
| type (production fonts; availability and Devanagari coverage verified at implementation) | IBM Plex Sans + IBM Plex Sans Devanagari; IBM Plex Mono figures | Mukta (Latin + Devanagari) | Archivo Narrow headings; Noto Sans + Noto Sans Devanagari; JetBrains Mono figures |
| status.fresh / recent / unavailable (fg on bg) | #1E5B3A on #EAF3EC / #0E5E63 on #E3F1F1 / #5A5248 on #F1EDE6 | #0B5D45 on #E6F4EE / #0E5E63 on #E3F1F1 / #4A5550 on #F1F3F2 | #0A7A4B / #0E5E63 / #5B616B, each on white |
| signal.outage (text / bg / border) | #1D1A16 / #FFF3D6 / #8A5300 | #16201C / #FFF3D6 / #8A5300 | #0E0E0E / #FFF3D6 / #B25E00 |
| chart.transport (fill / track) | #5A5248 / #D9CFBF (fill 7.12:1 on #FAF6EE) | — | — |
| type ramp, px size/line (display · title · heading · body · label · caption · numeric) | 28/36 · 22/28 · 18/24 · 16/24 · 14/20 · 13/18 · 24/32 | 40/48 · 28/36 · 22/30 · 18/28 · 16/24 · 14/20 · 40/48 | 32/36 · 24/28 · 18/22 · 15/22 · 13/18 · 12/16 · 22/28 |

The Task 9 Figma file holds these values as variables (one theme collection per direction, aliased to a primitives collection) and as text styles (packet §6.2). The "Theme A · Hisaab" collection and the "A Hisaab/…" text styles are the approved values; `chart.transport` is defined here only.

Stitch drafts use stand-in fonts where the tool offers no Devanagari family (B drafts use Lexend/Nunito Sans). Font availability, licensing and Devanagari coverage are verified at implementation (CLAUDE.md §J).

## 15. Components (semantic inventory)

| Component | Variants / states | Data |
|---|---|---|
| AppBar / TopNav | farmer, admin; with unread count | — |
| LanguageSwitch | MR · HI · EN | — |
| SourceStatusLine | normal; never synced | `sourceStatus` |
| OutageBanner | latestSyncFailed | `sourceStatus` |
| FreshnessBadge | FRESH, RECENT, STALE, UNAVAILABLE; sm/md | `freshnessStatus`, `reportDate` |
| TrendIndicator | RISING, STABLE, FALLING, INSUFFICIENT_DATA | `trendDirection`, `changePercent7Day` |
| ReasonChip | 8 codes; positive vs caution | `reasonCodes[]` |
| PriceQuote | inline, block | `PriceQuote` |
| ProvenancePanel | collapsed, expanded | `PriceQuote` |
| EstimateFigure | net (hero), net (row), transport, gross; reported vs hypothetical | `estimates` |
| CalculationBlock | comparison, what-if, outage | `price`, `estimates`, `transportAssumptions` |
| MarketOption | recommended, highest-price, default, stale; collapsed/expanded | `SellingOptionResponse` |
| AnswerStrip | recommended; outage (with the banner above it) | rank-1 option, `estimates`, `price` |
| WinnerCallout + TradeoffSentence | shown only if `rankingsDisagree`; two wordings by the sign of the estimated net difference (ADR-0045) | winners + estimates |
| TransportScaleBar | reported; what-if (hypothetical treatment, tick at the reported value) | `estimates.estimatedTransportCost` over all displayed options |
| InputStep + StepProgress | steps 1–4 below 1024 px; review | geography, commodity and quantity inputs |
| ExcludedMarketsList | — | `excludedMarkets[]` |
| RankingBasisNote / TransportLabel / Disclaimer | — | `decisionBasis`, `transportAssumptions` |
| WhatIfFrame, AssumptionControl, BreakevenInsight | default, changed, out-of-range original, server-loading | client state |
| TrendChart, PriceDiaryTable | 7d, 30d, insufficient | `TrendResponse` |
| AlertRuleRow, AlertEventCard, AlertForm | active, paused; unread, read; validation | alert operations |
| Form controls | CascadingSelect, SearchSelect (pinned), NumberWithUnit, ChipGroup, PhoneField (+91), EmailField, IdentifierField (mobile number or email), PasswordField (show/hide), Button (primary, secondary, tertiary, destructive; busy, disabled) | — |
| Account screens | S10 tabs, S14 request, S15 new password, S16 confirmation; link-token reader (fragment) | auth operations |
| EmptyState, ErrorState, Skeleton | per §9 | — |
| Admin: DatasetHealthCard, FreshnessDistribution, SystemStatusList, RunsTable, VersionedConfigForm, CurationTable, AuditTable | — | admin operations |

Figma components exist for FreshnessBadge, TrendIndicator, EstimateFigure (net hero, reported vs hypothetical) and OutageBanner, with one Theme variant per explored direction (packet §6.2). The Theme=A variants are the approved ones; Theme=B and Theme=C are reference. Every other component is specified here only.

## 16. Interaction rules

- **What-If computation** follows `FRONTEND_CONSUMPTION.md` §3 exactly (exact decimals; rank by net, then distance, then name; only `PRICE_ADVANTAGE` and `LOWER_TRANSPORT` re-evaluated; composite scores never computed).
- **Server mode:** only when the distance slider is released above the comparison's `maxDistanceKm`; show "Checking more markets…" on the results while waiting; render the response as the new base (it may contain newer observations — say "Prices re-checked {time}").
- **Quantity outside the slider range** (compare allows any quantity, ADR-0023): show "Your quantity: 8,000 kg (outside the what-if range 100–5,000 kg)"; the slider starts unset and sets a value only when moved; an unchanged quantity is never sent (FRONTEND_CONSUMPTION §3). APPROVED (PDD-10).
- **Units:** What-If quantity is in kg (contract); show the quintal equivalent beside it.
- **Alerts polling** (ADR-0019 asks this file to set the interval): APPROVED (PDD-01) — poll `listAlertEvents?unreadOnly=true` on app start, on page focus, and every 5 minutes while a page is open and visible; stop when hidden. Rationale: events are created only after ingestion runs (every 6 hours, §15), so 5 minutes gives timely display with negligible load.
- **Session:** refresh on page load; serialized refresh on 401; never store the access token in Web Storage (ADR-0017). A 429 on refresh waits `Retry-After` and retries once (ADR-0042). Sign-out calls `logout` and clears in-memory state.
- **Email links** (ADR-0048): S15 and S16 read the token from the URL fragment, remove it from the address bar at once (`history.replaceState`), send it only in the request body, and never log, store or display it. The reset page is served with `Referrer-Policy: no-referrer`. A reset never signs the user in.
- **Account emails take time:** after S10 registration, S12 "Send a new link" and S14, the screens say a link may take a few minutes and offer to send again after the per-hour limit (ADR-0048).
- **Destructive actions:** deleting an alert names it in the confirmation ("Delete alert 'Onion · TEST Market B · at or above ₹3,000'?").

## 17. Design decisions (APPROVED by ADR-0044)

All fourteen decisions were approved on 2026-10-04 (H2). The frontend implements them as written; a change needs a new decision-log entry.

| ID | Decision | Why |
|---|---|---|
| PDD-01 | Alert events polled on start, on focus, and every 5 min while visible | ADR-0019 requires role file 02 to set the interval; runs are 6-hourly |
| PDD-02 | Two farmer destinations (Compare, Alerts) + account; everything else contextual | No public operation lists markets; trend/what-if need a comparison |
| PDD-03 | Locality step shown but optional | Not part of `CompareRequest`; no coordinates (ADR-0014); F1.3 does not say "required" |
| PDD-04 | Attribution string shown verbatim from the API in all locales | It is a provenance statement with proper nouns; translating official names needs a source |
| PDD-05 | Fallback names shown as-is with `lang="en"`, no badge | ADR-0015 asks this file to specify fallback display |
| PDD-06 | Latin digits + Indian grouping in all locales | Persona: "English (numbers only)" |
| PDD-07 | Estimates to whole rupees with "≈"; reported prices as reported | Honest precision; the API keeps 2 dp |
| PDD-08 | Absolute dates ("2 Oct 2026"), 24-hour IST times | Freshness wording must not over-claim "today" |
| PDD-09 | Trend points show date and dataset; freshness badge only for the latest point | "Unavailable" on a plotted historical point would contradict what is shown |
| PDD-10 | Out-of-range original quantity displayed, slider unset until moved | ADR-0023 has no compare bound; FRONTEND_CONSUMPTION §3 leaves the display to Task 9 |
| PDD-11 | After "Use these numbers", results keep the What-If treatment until reset | §17 J applies simulator inputs as base; they remain hypothetical |
| PDD-12 | Freshness copy never hard-codes day counts | Thresholds are configurable per weights version |
| PDD-13 | Loading: busy button immediately, skeleton after 300 ms | Compare target < 1 s (§27); avoids flicker |
| PDD-14 | Winner callout states the trade-off in rupees, with the arithmetic; wording per ADR-0045 | Makes F1.13 concrete without new data; truthful whether or not rank 1 has the highest estimate |

## 18. Open items

No design decision is open. RD-045, RD-046 and RD-047 are resolved (ADR-0045, ADR-0046, ADR-0047 with ADR-0048), and the direction is selected (ADR-0043).

Work that remains, by design, for implementation (not decisions):

- **Marathi and Hindi copy** for every remaining string, including the §9 rows, produced by Claude under ADR-0046 before the §48 language sign-off.
- **Email delivery gate:** a live Resend send before the auth module relies on it, and a verified sending domain on the project domain (ADR-0048 item 9; RD-037).
- **CSP gate:** the final Content-Security-Policy is verified during frontend implementation (ADR-0042, SR-11).
- **Missing renderings:** screens without a rendered draft (S06 desktop, S07 and S09 desktop, S08, S10 sign-in tab, S12, S14–S16, A02–A05, the Marathi results) are built from this file and the approved tokens; no new visual direction is introduced.

## 19. Approval record

Mirrors SSOT index area 5 (ADR-0027 item 2). Approver for every row: the Human Project Owner, 2026-10-04 (ADR-0043, ADR-0044), recorded at the owner's instruction in the Task 9 final decision lock.

| Screen / section | Design reference (Stitch screen IDs; project A `16473179131950483450` unless stated) | Status |
|---|---|---|
| Whole specification (§1–§18), tokens §14.2 column A | This file | APPROVED |
| S01/S02 first launch and home (mobile) | `73a8d7641a654d0687f9304e4d2ba972` | APPROVED (layout and visual language) |
| S02 landing (desktop) | `e942607371fe4455ba2e52b32ef26218` | APPROVED (layout and visual language) |
| S03 input — mobile steps | Pattern: Concept B steps (project `6060147904238362683`: `e375344003e24a8e9e3657e05d8df95a`, `29028be0d7b647318f2608b31e2e3f23`, `4095f70c979449ca9024935ae9c72c83`, `691450c3f42b46698434a84f26ae2505`), rendered in Hisaab style from the A form `1434e413f6b74e37908ab65fe5fabdba` | APPROVED (flow from B, visual language from A; §1.1) |
| S03 + S04 comparison sheet (desktop) | `08275aed05844078a623ac91e62b2bcd` | APPROVED (layout and visual language) |
| S04 results (mobile) | `5ce101cb69ac4a14bbd0c6515e9dc9fb`; answer strip pattern from B `94962ca061fb44868a5da3b24f2c2ec6`; trade-off sentence and shared transport scale pattern from C (project `7462455097525525857`) `1d477ed7e7474e1ba3b6686aaf69c813` | APPROVED (patterns rendered in Hisaab style; wording per ADR-0045) |
| S05 why recommended | `1654e3dc58054495b04ae01e5f0c0000` | APPROVED (layout and visual language) |
| S07 trend (price diary) | `5f93505d36694dcd95885c380ee0a019` | APPROVED (layout and visual language) |
| S09 What-If (mobile) | `fa95cea48dfe400a851dafd3bf8ede76` | APPROVED (layout; labels per ADR-0045 item 6) |
| S10 registration | `44544e48277d4165ba8574716011e0a9` | APPROVED for layout only; content per §8 S10 (invented reference number and footer excluded) |
| S11 price alerts | `89779b622d8849988b914f3975263895` | APPROVED (layout and visual language) |
| E02 outage + stale | `aa19f90c43324750a291f345297b175f` | APPROVED (layout and visual language) |
| E05 no market in range | `42fc34d22b3443e3b1b359e6d32382fd` | APPROVED (layout and visual language) |
| A01 admin data health (desktop) | `3217f9a370184c48b4957721a6419962` | APPROVED for layout only; invented "Node environment" panel and header label excluded |
| States E01–E31 | Figma file `YGFBNHyWCwyFaszM4205Q6`, board 09 (rendered with Hisaab tokens) and §9 | APPROVED |
| S06, S08, S12, S14, S15, S16, A02–A05, desktop S07 and S09, Marathi rendering | This file only (no rendered draft) | APPROVED as specified; rendered with the approved tokens |

Concepts B and C (all other drafts in the packet §6.1) are exploration reference, not approved designs. Any tool-invented content listed in the packet §6.1 is excluded from every approval.
