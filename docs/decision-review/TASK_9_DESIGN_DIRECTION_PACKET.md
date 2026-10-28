# AgriNex — Task 9 Design Direction Packet

**Status:** REFERENCE (2026-10-04). Prepared by Task 9 for a human decision. Nothing in this packet is approved. It is never a source of requirements; the master specification and binding ADRs govern, and role file 02 ([`../02_AgriNex_UIUX_Phase1_Specification.md`](../02_AgriNex_UIUX_Phase1_Specification.md), DRAFT) holds the concept-independent specification.

> **Decided on 2026-10-04 (Task 9 final decision lock).** The Human Project Owner approved **Concept A, Hisaab**, with four borrowings: B's step-by-step mobile input and answer strip, and C's trade-off sentence and shared-scale transport bar (ADR-0043). The PDDs were approved (ADR-0044); RD-045, RD-046 and RD-047 are resolved (ADR-0045 to ADR-0048). Role file 02 is now AUTHORITATIVE. This packet stays as the record of the exploration: Concepts B and C are reference material, not an approved direction, and the statements below describe the situation before the decision. Record of the lock: `TASK_9_FINAL_DECISION_LOCK.md`.

**Decision requested from the Human Project Owner (historical):** choose the visual and interaction direction AgriNex builds (A, B, C, or a stated combination), then approve or amend the proposed design decisions (role file 02 §17) and resolve RD-045 to RD-047. Screen approvals follow ADR-0027: the Document Owner records each approved screen in SSOT index area 5.

**All data in every artifact is SYNTHETIC** (from `frontend/__mocks__/api/`: TEST Market A–F, Test District North/South, 3 Oct 2026). Every screen carries a "SYNTHETIC DATA · PROTOTYPE" tag (ADR-0028; CLAUDE.md §M).

---

## 1. What Task 9 learned from the SSOT (the design problem)

- The product does not show prices; it answers "after transport, which market may leave me more money, and why?" (spec §2, §4.5). The hero insight in the synthetic data: TEST Market B has the highest price (₹3,100/q) but is ≈128 km away; TEST Market C (₹3,000/q, ≈42 km) leaves about ₹188 more after estimated transport for 500 kg.
- **The central design tension** is honesty vs. simplicity. Every price must carry its report date, freshness, variety, grade and attribution; every transport cost the applied-rate label; every net figure "Estimated net realization"; every price screen the 45-word disclaimer (spec §31, §32, §49; ADR-0038) — for a user who is "NOT comfortable reading dense text" on a 5.5" screen (§6).
- **The data honestly produces small differences.** Transport is 2–7 % of gross value in realistic cases, so the decisive difference (₹188 ≈ 1.3 %) is invisible in honest zero-based bars. Any design that makes it look dramatic by truncating axes would mislead (P3, P10). Concepts must make the trade-off *understandable*, not *large*.
- Ranking is a composite score (net realization dominant, plus price, trend, freshness, transport penalty); scores are never shown and the screen must state the basis (ADR-0024, ADR-0026). This revealed **RD-045**: the spec's banner text "Highest Net Realization" is not always true of rank 1.
- What-If is client-side, hypothetical, unsaved, and ranks by net only — a *different* basis from the comparison (ADR-0026), so hypothetical results must look unmistakably different from reported data.
- Accounts exist only for alerts; there is no profile, settings or password-recovery operation (contract 1.0.0) → **RD-047**. Marathi and Hindi copy has no defined review source → **RD-046**.

## 2. The three directions

### 2.1 Concept A — "Hisaab" (the honest ledger)

| Aspect | Description |
|---|---|
| Philosophy | Show the sum. Each market is a short account a farmer can check in their head: reported price × quantity = gross; − estimated transport; = estimated net realization. Trust comes from verifiable arithmetic, the way farmers and commission agents already keep *hisaab*. |
| Visual language | Warm paper (#FAF6EE), ink text, hairline ledger rules, double rule above the bottom line, ledger green for the net figure, indigo dashed "WHAT-IF" stamp for hypotheticals. IBM Plex Sans (+ Devanagari) with tabular, right-aligned figures. 4 px corners, no shadows. |
| Information hierarchy | Recommended card first with its full calculation; a two-line "Highest price is not the most money" mini-ledger when rankings disagree; every option shows the same three ledger lines. |
| Interaction ideas | What-If edits the assumptions of the ledger itself (rate, quantity, distance) and re-renders every card as a dashed "draft" with "was ≈₹14,664" beside each new value; break-even line. The price trend is a "price diary" (dated list + chart with the two 7-day windows shaded). |
| Navigation | Top app bar only (back, title, language, alerts, account). Desktop: input panel on the left, a comparison *sheet* with markets as columns. |
| Strengths | Highest transparency and verifiability; mandatory labels fit naturally (they are ledger lines and footnotes); strongest for the faculty evaluator (P2, §36 story); desktop sheet is excellent for side-by-side reading; calm, credible, non-generic. |
| Weaknesses | More numbers per screen than B; needs basic numeracy (persona reads English numbers, so acceptable); single long input form on mobile is heavier than B's steps; paper styling must stay restrained to avoid looking old-fashioned. |
| Key risks | Density creep (adding provenance rows to every card); small grey footnotes drifting below 12 px. |
| Implementation implications | Simple components (cards, ruled rows, table); a responsive table → cards transformation; What-If re-render with a draft style; tabular figures font. Low–medium effort. |

### 2.2 Concept B — guided helper (one question at a time)

| Aspect | Description |
|---|---|
| Philosophy | Ask one question per screen with big tap targets, then give one clear answer ("Consider selling at TEST Market C") with plain-language reasons; details on demand. Built for thumbs, glances, sunlight and low reading confidence. |
| Visual language | White, deep green answer block with white text, marigold fill for the highest-price note, violet panels for What-If; large friendly type (answer figures 36–44 px, body 18 px), 16–20 px radii, pill buttons, bottom navigation (Compare · Alerts · Account). |
| Information hierarchy | Answer card → highest-price note → "Why?" reasons (three icon rows) → see all markets → trust box with the mandatory text. |
| Interaction ideas | Four-step wizard with progress (place → crop with pinned tiles → number pad → distance + review); What-If as large steppers with a violet result panel and a "still looks best" sentence; alerts in plain words ("At or above ₹3,000 per quintal"). |
| Strengths | Lowest cognitive load and best touch ergonomics; best for Ramesh and Sunita on small phones; clearest single answer; highest accessibility (size, contrast, targets). |
| Weaknesses | Arithmetic is one tap away, so the answer can feel like a black box; the mandatory labels collect in a "trust box" at the bottom where they are least read; desktop is a stretched mobile layout and under-uses the space; the generic "app" look differentiates least. |
| Key risks | Over-trust ("the app told me to go there") despite "Consider…" wording; answer-first framing hides that differences can be small (₹188). |
| Implementation implications | Simplest component set; wizard state handling; number pad; low effort and low maintenance. |

### 2.3 Concept C — trade-off board (see what transport takes)

| Aspect | Description |
|---|---|
| Philosophy | Make the price-versus-transport trade-off visible. Every market is a row on one shared transport scale; a mono equation "Gross − transport = Estimated net realization"; an annotation states the trade-off in rupees ("+₹500 from the higher price, −₹688 more transport = ₹188 less"). The board is also the simulator. |
| Visual language | Crisp information design: white, black type, Archivo Narrow headings, JetBrains Mono figures, hatched transport bars, signal green for net, violet for What-If, amber stale corner tags. |
| Information hierarchy | Input chips → legend → trade-off annotation → ranked rows (bar + equation + net) → excluded markets; market detail opens as a drawer (mobile) or right panel (desktop). |
| Interaction ideas | Docked What-If sheet always present; changing a slider turns the board violet with ghost markers of the previous transport values; desktop three-panel workbench (inputs/what-if · board · detail/trend). |
| Strengths | Most distinctive; the trade-off is legible at a glance; strongest demo moment; desktop workbench is powerful for operators, FPO staff and evaluators; What-If feels direct. |
| Weaknesses | Demands graph literacy; dense mono text tends toward 10–12 px on phones; the honest bars show small differences (see §5.1); most complex to build accessibly (chart text equivalents, sliders, sticky sheet). |
| Key risks | Looking like an analytics dashboard to farmers; drafts repeatedly drifted into horizontal overflow and tiny text at 390 px. |
| Implementation implications | Custom bar rendering on a shared scale, slider + sheet interactions, server-mode What-If when distance grows; medium–high effort; more QA for 375 px and screen readers. |

## 3. Coverage

Screens are Stitch drafts (projects listed in §6). "v2" marks a regenerated screen that supersedes a defective v1 (both are kept in the projects as the exploration record).

| Journey step / state | A mobile | A desktop | B mobile | B desktop | C mobile | C desktop |
|---|---|---|---|---|---|---|
| First launch + language + home | ✓ | ✓ landing | ✓ (defect) | ✓ landing (defect) | ✓ (defect) | ✓ landing (defect) |
| Input (location, crop, quantity, distance) | ✓ form | ✓ left panel | ✓ 4 steps | — (summary row) | ✓ panel + location sheet | ✓ inputs panel |
| Comparison results + winner callout | ✓ v2 | ✓ sheet | ✓ v2 + all markets | ✓ | ✓ v2 (defect) | ✓ workbench (defect) |
| Why recommended / explanation | ✓ | ✓ (in sheet) | ✓ (in answer) | ✓ "why not first" (defect) | ✓ drawer (defect) | ✓ right panel |
| Trend / price diary | ✓ | submitted | — | ✓ (in "why not first") | ✓ (defect) | ✓ (defect) |
| What-If | ✓ | submitted | ✓ | ✓ (defect) | ✓ v2 | ✓ (defect) |
| Alerts (rules + events) | ✓ | — | ✓ | — | ✓ (defect) | — |
| Sign-in / quick registration | ✓ (defect) | — | submitted | — | — | — |
| Outage + stale | ✓ | — | ✓ v2 | — | ✓ | — |
| No market in range | ✓ | — | ✓ v2 | — | ✓ (defect) | — |
| Marathi results (draft copy) | submitted | — | ✓ | — | ✓ | — |
| Admin data health | — | ✓ (defect) | — | ✓ (defect) | — | submitted |

"Submitted" = generation request accepted but timed out at the tool; the job may appear in the project later (see §6.3). "(defect)" = the draft contains something the tool invented or mislabelled; each is named in §6.1 and marked on the Figma boards. Remaining states (no recent data, 429, 400, 422s, 503, 500, network, session) are specified in role file 02 §9 and drawn once, concept-neutrally, on Figma board 09 "States and errors".

## 4. Comparison

Scores 1 (weak) – 5 (strong), judged against the SSOT personas and rules; the reasoning matters more than the totals.

| Criterion | A Hisaab | B guided | C board | Reasoning |
|---|---|---|---|---|
| Usability for Ramesh/Sunita (375 px, tapping) | 4 | 5 | 3 | B's one-question steps and 56–64 px targets are easiest; C's density and sliders are hardest |
| Farmer comprehension of *why* | 5 | 3 | 4 | A shows the sum farmers already do; B hides it one tap away; C shows it as an equation plus a bar |
| Decision clarity | 4 | 5 | 4 | B states one answer; A and C state it and show the competition |
| Trust and transparency (P2, P3, P6) | 5 | 3 | 4 | A integrates provenance and estimates into every card; B batches them at the bottom |
| Honest treatment of small differences | 5 | 3 | 4 | A shows ₹188 as arithmetic; B says "about ₹188 less"; C needed a redesign to avoid misleading bars (§5.1) |
| Accessibility (WCAG 2.1 AA) | 4 | 5 | 3 | All palettes pass text contrast (role file 02 §12); C relies most on charts and small mono text |
| Responsive quality | 5 | 3 | 4 | A cards → sheet is natural; B desktop is a stretched phone; C mobile is cramped, desktop excellent |
| Information hierarchy | 4 | 5 | 3 | |
| Technical feasibility (Next.js + Tailwind + Recharts baseline) | 4 | 5 | 3 | C needs custom shared-scale bars and an accessible sticky sheet |
| Maintainability / consistency | 4 | 5 | 3 | |
| Product differentiation | 4 | 2 | 5 | B looks like many apps; C is the most memorable |
| Fit with mandatory content load | 5 | 3 | 4 | |
| **Total (of 60)** | **53** | **47** | **44** | |

## 5. Exploration findings worth keeping

1. **Honest bars hide small differences.** C's first board drew zero-based gross bars (net + transport bite). With transport at 2–7 % of gross, all bars looked identical and the ₹188 difference vanished; truncating the axis would have exaggerated it. C v2 moves the bars onto one shared *transport* scale (₹300 vs ₹1,024 is visibly 3.4×) and states the trade-off as arithmetic. Applies to any chart in AgriNex.
2. **The What-If basis differs from the comparison basis.** All concepts label "Ranked by estimated net realization only" in What-If and keep the hypothetical colour on every changed value; A and C also show "was ≈₹14,664" deltas so the user sees what changed and what did not (reported prices never change).
3. **AI-generated drafts invent things.** Across ~60 Stitch generations the QA lint and visual review caught: invented footers and claims ("Regulated Mandi Data", Terms links, "indicative and non-binding", version and reference numbers); concept names leaking into the product name ("AgriNex Saathi", "Mandi Board Logic"); fabricated data — a travel time ("about 1 hr 15 mins"), distances that contradict the mock (150 / 95 / 40 km instead of ≈42 / ≈128 / ≈15 km), and six real APMC market names inserted into a synthetic admin screen; an invented "Recommended: 50 km"; invented actions ("Clear history", "Export comparison"); wrong freshness definitions ("48 hours", "7+ days", "< 2h"); forbidden "live" and "real-time" wording; paraphrased disclaimers; bare or abbreviated "Net realization"; "in hand", "haul" and "haulage" wording. Implication: **no tool output may be treated as approved copy or data**; role file 02 is the copy source, the mocks are the data source, and every screen is checked against both before approval (ADR-0027).
4. **Stitch editing limitation.** `edit_screens` changes are not reflected in the downloadable HTML or screenshots, so fixes were made by regeneration (v2) and the defective v1 screens remain in the projects as the exploration record.

## 6. Artifacts

### 6.1 Stitch (visual exploration — working drafts, not approved)

| Project | Stitch project ID | Design system asset |
|---|---|---|
| Concept A · Hisaab | `16473179131950483450` | `assets/18005103446771815393` |
| Concept B · guided | `6060147904238362683` | `assets/13681345266281136436` |
| Concept C · trade-off board | `7462455097525525857` | `assets/13641327598095513343` (v1), `assets/6168537023959817733` (v2: naming and anti-fabrication rules) |

Canonical screens per concept (Stitch screen IDs):

| Screen | A | B | C |
|---|---|---|---|
| Home / first launch | `73a8d7641a654d0687f9304e4d2ba972` | `0538143216554f9491250d8baeb6e15a` (defect: "AgriNex Saathi") | `9a2343d6b5ed4c33b0e1f02535b578dc` (defect: duplicate nav) |
| Input | `1434e413f6b74e37908ab65fe5fabdba` | `e375344003e24a8e9e3657e05d8df95a`, `29028be0d7b647318f2608b31e2e3f23`, `4095f70c979449ca9024935ae9c72c83`, `691450c3f42b46698434a84f26ae2505` | `f768aedc291a440087951e621fab955e` |
| Results (v2) | `5ce101cb69ac4a14bbd0c6515e9dc9fb` | `94962ca061fb44868a5da3b24f2c2ec6`; all markets `e564f8fa1f66419b8048e56807b84f25` | `1d477ed7e7474e1ba3b6686aaf69c813` (defect: "haul"; rows abbreviate the net label) |
| Why / drawer | `1654e3dc58054495b04ae01e5f0c0000` | (in results) | `08704b2d80ad401fb8b74f4a6fafb7f6` (defect: "net in hand", "haulage") |
| Trend | `5f93505d36694dcd95885c380ee0a019` | — | `89a22e6435c742f486d86b592de72b99` (defect: axis labelled "(gross)") |
| What-If | `fa95cea48dfe400a851dafd3bf8ede76` | `2928d9d7951b4c1a8f07d2396dcb0d56` | `b0df1c464a1d41df81d381b606843878` |
| Alerts | `89779b622d8849988b914f3975263895` | `4393d059333c42a491d1a2357a7a5327` | `44eafa3d16d84d57bc47853c812464e9` (defects) |
| Sign-in / register | `44544e48277d4165ba8574716011e0a9` (defect: invented reference + concept name) | submitted | — |
| Outage + stale | `aa19f90c43324750a291f345297b175f` | `c27c9e98766e405b82022440a1da240b` | `8a95eaca517648b4bd63adc57db3bb40` |
| No market in range | `42fc34d22b3443e3b1b359e6d32382fd` | `3bdadb43d80a45d4bf6efa1b2e192988` | `af6aca8e2bcf4ff5b470f5393a68a40c` (defect: naming) |
| Marathi results (draft copy) | submitted | `c363f3ee070d4eed8b1476c50825ea1a` | `75ed5ae4368e4b2eb6c62e998267d69c` |
| Desktop landing | `e942607371fe4455ba2e52b32ef26218` | `cf7db3f579ec43e9b80df505e2836f09` (defect: invented legal footer; "Estimated highest net realization") | `bb1a94c8bb4744c593c6642168fa6782` (defect: version strings, pseudo-legal line, wrong freshness definitions — excluded from the Figma boards) |
| Desktop compare | `08275aed05844078a623ac91e62b2bcd` | `d15edca601ec4674bf77de0bdacb74ad` | `bfbe2241b97a4e799c6c7a52e6e80f98` (defect: "Est." abbreviates the mandatory label) |
| Desktop detail / trend | submitted | `566c238c97534418ac2c91d9789cf1e0` (defect: invented legal footer) | `19bf2d86619c40e49b2c49dba12aeeba` (defect: "MANDI BOARD LOGIC V3.4", "CONCEPT C STANDARD", invented narrative) |
| Desktop What-If | submitted | `e6e53135688f4ed98bbf72fe0f4ee36a` (defect: fabricated distances 150 / 95 / 40 km, "Highest margin", invented legal footer — do not reuse) | `c5b98352526b4f148932f07852eba9c9` (defect: invented "Export comparison") |
| Admin data health (desktop) | `3217f9a370184c48b4957721a6419962` (defect: invented "Node environment" panel; "Hisaab Ledger" header label) | `d981d2a4ac04476888c51cb7ebf66e10` (defect: "Live audit trail", "Real-time freshness breakdown", "Node cluster active", invented legal footer) | submitted |

Superseded v1 and orphan duplicates (exploration record, not canonical): A `91579f4f170d404ab53e490a9008a011` (results v1), `836fcce1d5bb4afe8fb737530adefe91`, `760e89aca265458cb2626e1aaad83f79`; B `6d965a6a68984f6aa704f556a6f7f58c` (answer v1), `b7a634fe7465457b8550b967965bdeac` (outage v1, fabricated travel time), `fe3aad7f715b4e08a2e164cbf8d01417` (no-market v1), `359f534841144e6b8fcdca7a87da250b`, `ed463fa8e0ee4e5a930d79556e5a8f84` (admin duplicate — rejected: inserts six real APMC market names into synthetic data); C `0ad02af17c184a1ea80f3c201f8c4285` (board v1, gross bars), `d24845e697cc427483d77f45cfe404a0` (what-if v1, desktop layout).

### 6.2 Figma (structured design work — working drafts, not approved)

| Artifact | Link | Contents |
|---|---|---|
| Design file "AgriNex — Task 9 design exploration (DRAFT, synthetic data)" | <https://www.figma.com/design/YGFBNHyWCwyFaszM4205Q6> | Page **Task 9 · Review boards (DRAFT)**, 13 frames: 00 cover and how to review · 01 honesty system · 02 information architecture and journeys · 03–08 each direction's mobile journey and desktop screens (the Stitch drafts, with defects marked) · 09 states and errors (concept-neutral, role file 02 §9) · 10 design-system board · 11 comparison and recommendation · 12 exploration findings. Page **Task 9 · Design-system foundations (candidate)**: the variables, text styles and components below |
| FigJam board "AgriNex Phase 1 journeys J1-J4 (Task 9 draft)" | <https://www.figma.com/board/f2vcuvnOsEDViyGgZNyGBQ> (open once to claim it into your drafts) | Editable flowchart of journeys J1–J4 (role file 02 §5) with the E01, E02, E04, E05 and E11 branches |

Candidate design-system foundations in the design file (role file 02 §14–15; nothing approved):

- **Variables (134):** "Primitives · candidate palettes" (32 colours, hidden from pickers); "Spacing (candidate)" (`space/1`–`space/12`); one theme collection per direction — "Theme A · Hisaab", "Theme B · guided helper", "Theme C · trade-off board" — each holding the 28 semantic colour tokens of role file 02 §14.1 plus `radius/control` and `radius/card`, aliased to the primitives. Scopes and WEB code syntax are set on every variable. The file's plan refused a second variable mode ("Limited to 1 modes only"), so the themes are separate collections instead of modes of one collection.
- **Text styles (21):** display, title, heading, body, label, caption and numeric for each direction.
- **Components (4 sets, 33 variants):** FreshnessBadge (Theme × Status), TrendIndicator (Theme × Direction), EstimateFigure (Theme × Basis: reported or hypothetical; net hero only), OutageBanner (Theme; spec §29 sentence verbatim). Fills, strokes, padding, gaps and radii are bound to the variables; icons are imported SVG vectors. Every text/background pair in them meets WCAG 2.1 AA (lowest 4.67:1).

### 6.3 Known limitations of the artifacts

- Tool timeouts: several generations were accepted by Stitch but timed out before returning; they may appear later in the projects as extra screens. Project listings lag behind generation by hours, so they were not used to reconcile.
- Defects listed in §6.1 are known and documented rather than regenerated where they do not change the concept evaluation; any chosen direction is rebuilt from role file 02, not from the drafts.
- Marathi and Hindi strings in drafts are unreviewed (RD-046).
- Figma: the account (Starter plan, View seat) allows about 6 Figma MCP calls a month, so the Figma work was batched into three calls (review boards, design-system foundations, FigJam journeys). The boards and the design-system page were checked with screenshots returned by the build call; the FigJam board was not screenshot-checked after creation. The review boards are plain layers built from HTML, and the screens on them are images of the Stitch drafts.
- Fonts in Figma: every candidate family loaded; JetBrains Mono SemiBold was not offered, so the C numeric style uses JetBrains Mono Bold.

## 7. Recommendation (professional opinion, not a decision)

**Build Concept A ("Hisaab") as the base direction, with two named borrowings:**

1. From B — the **guided, one-question-per-step input on mobile** (A's single long form is the heaviest part of A for Ramesh) and B's **answer strip** at the top of results ("Consider selling at TEST Market C · ≈₹14,664 estimated net realization"), immediately followed by A's ledger cards.
2. From C — the **trade-off sentence** ("+₹500 from the higher price, −₹688 more transport = ₹188 less") inside A's winner callout, and C's **shared-scale transport bar** as an optional glance aid in each ledger card. Not C's workbench or docked sheet as the default.

Why: A scores highest on the two things that make AgriNex defensible — verifiable reasoning (P2) and honest presentation of estimates (P3, §49) — and its desktop sheet serves the evaluator and admin personas without a second design language. The borrowings remove A's main weaknesses (mobile input weight, answer not stated in words) without importing B's black-box risk or C's chart burden.

If the owner prefers maximum simplicity for field users over evaluator transparency, choose B and require the calculation to be visible on the answer screen (not behind a tap). If the owner prefers differentiation and a stronger demo, choose C and budget for accessibility work and a farmer usability test (spec §36 Experiment 4).

## 8. Decisions requiring human selection

| # | Decision | Options | Where recorded after decision |
|---|---|---|---|
| H1 | Visual and interaction direction | A · B · C · A + named borrowings (recommended) · other combination | SSOT index area 5 + role file 02 §14 values |
| H2 | Proposed design decisions PDD-01 to PDD-14 | Approve / amend each | Role file 02 §17 |
| H3 | Winner-callout wording | RD-045 options A / B / C | Decision log → RD-045 resolved |
| H4 | Marathi and Hindi copy review source | RD-046 options A / B / C | Decision log → RD-046 resolved |
| H5 | Password recovery in Phase 1 | RD-047 options A / B / C | Decision log → RD-047 resolved |
| H6 | Which draft screens to refine into approved designs | Per screen, after H1 | SSOT index area 5 (screen, design reference, date, approver) |

**Outcome (2026-10-04):** H1 — A Hisaab with the four named borrowings (ADR-0043). H2 — PDD-01 to PDD-14 approved, PDD-14 reworded (ADR-0044). H3 — "Recommended" for rank 1, factual labels only where true, trade-off sentence by sign (ADR-0045). H4 — Claude owns the Marathi and Hindi copy (ADR-0046). H5 — email at registration, login by email or phone, recovery by emailed link to the verified email, no SMS or OTP (ADR-0047, ADR-0048). H6 — the Hisaab screens and role file 02, recorded in SSOT index area 5 (ADR-0043).

## 9. What Task 9 did not do

- No application code, no Next.js, React or Tailwind code, no production CSS, no API integration, no backend, database, ML or deployment work.
- No screen approved; no decision-log entry approved; the master specification and contracts unchanged.
- Task 10 not started.
