---
name: agrinex-design
description: AgriNex UI/UX design work for Task 9 and later. Covers role file 02 (the design source of record), Stitch or other tool drafts and their approval, farmer-first and low-literacy flows for compare, trends, What-If, alerts, sign-in and admin, communicating provenance and freshness, honest price and transport copy, accessibility, responsive layouts, design tokens and component consistency. Use this skill whenever a task designs, critiques, specifies, generates or approves AgriNex screens, user flows, copy, error and empty states, design tokens or components, or uses Stitch or Figma for AgriNex, even when the user just says "make the comparison screen".
---

# AgriNex design

## Purpose

Specify screens that let a smallholder farmer get from "what are the mandi prices?" to "which selling option appears economically better, and why?". The screens must be honest about what the data is, where it comes from and how old it is. They are specified only from the master specification and the approved decisions, and recorded in role file 02, the design source of record.

This skill points to the authoritative documents. If it disagrees with them or with a binding decision, they win: correct this file.

## When to use

- Writing or reviewing role file 02 (`docs/02_AgriNex_UIUX_Phase1_Specification.md`; AUTHORITATIVE, approved 2026-10-04).
- Generating or critiquing Stitch, Figma or other tool drafts.
- Wording copy, labels, errors, empty and stale states.
- Defining design tokens and components.
- Recording a design approval.

## Source hierarchy

This follows CLAUDE.md §B and ADR-0027:

1. The master specification.
2. Binding decision-log entries.
3. Role file 02. It is the design source of record, and its precedence is: specification, then role file 02, then tool designs. **Approved direction: Hisaab** (Concept A) with four borrowings — B's step-by-step mobile input and answer strip, C's trade-off sentence and shared-scale transport bar (ADR-0043; role file 02 §1.1). Concepts B and C are reference only; do not change the direction without a new human-approved entry.
4. Tool designs. Stitch and Figma screens are working drafts until role file 02 references them and the Document Owner records the approval in SSOT index area 5 (screen, design reference, date, approver).

## Required reading (READ SSOT FIRST)

1. `CLAUDE.md` §M, then `docs/00_SSOT_INDEX.md` area 5, and `docs/REQUIRES_DECISION.md`.
2. Spec:
   - §6 personas (Ramesh: Marathi, 5.5" Android, "comfortable tapping, NOT comfortable reading dense text"; Sunita: Hindi, older 4.7" device, intermittent 3G; admin: English, desktop);
   - §7 features F1.1–F1.22;
   - §10 frontend freeze items;
   - §16 and §17 (comparison and What-If);
   - §21 (the hero flow);
   - §25 (the farmer decision journey);
   - §29 (failure messages);
   - §31–§34 (data rules, labels, trends, alerts);
   - §37, §46 (demo), §49 (claims and disclaimers).
3. `docs/architecture/FRONTEND_ARCHITECTURE.md`: screens, states, messages, price naming.
4. `docs/api/FRONTEND_CONSUMPTION.md` §2 and `docs/api/PROVENANCE_AND_FRESHNESS.md` §5: every state the API can produce, and the fields behind each label.
5. Decision-log entries:
   - ADR-0004 (browser floor), ADR-0015 (localized names), ADR-0016 (registration fields), ADR-0019 (alert events);
   - ADR-0023 (no-data states), ADR-0024 (the 8 reason codes), ADR-0025 (trend), ADR-0026 (What-If);
   - ADR-0027 (design approval), ADR-0028 (demo uses real data), ADR-0038 (transport label), ADR-0039 (origin), ADR-0040 (estimate error);
   - ADR-0043 (approved direction and screens), ADR-0044 (PDD-01 to PDD-14), ADR-0045 (recommendation wording: "Recommended" for rank 1, factual labels only where true), ADR-0046 (Claude owns Marathi and Hindi copy, under the glossary and register of role file 02 §10), ADR-0047 and ADR-0048 (email at registration, login by email or phone, password recovery screens S14–S16).

## Non-negotiable AgriNex rules

- **Farmer-first, low-literacy.** The pattern is: simple input, then a clear comparison, then an obvious winner with its reason (§6). Design for tapping, not reading dense text, on 375px screens and slow connections. Use Marathi, Hindi and English (F1.2), with localized commodity names falling back to the official name (ADR-0015).
- **Honest prices** (spec §31 RULE 4, §49):
  - say "Latest reported price", never "live" or "real-time";
  - show the report date, the freshness badge and the source attribution with every price;
  - FRESH covers today and yesterday under the default threshold, so never claim "reported today" unless the date is today;
  - show STALE and UNAVAILABLE states openly.
- **Transport copy** (ADR-0038, departure D-14): "Estimated — based on ₹{appliedRatePerKm}/km assumption. Actual costs will vary.", always with the rate actually applied, and "Estimated net realization".
- **Distances and location.**
  - Label distances as approximate.
  - The origin is the selected district: State, then District, then Locality. Never device location or an address (spec §28; ADR-0039).
- **States to design:**
  - the winner banner, shown only when `rankingsDisagree`;
  - "Why recommended?" with the 8 reason codes;
  - insufficient trend data (§33);
  - the outage banner (§29);
  - no recent data and no market in range, with an adjust-distance control (§25);
  - errors: field validation, non-centroid origin, `ESTIMATE_OUT_OF_RANGE`, 429 with a retry wait;
  - "Forecast unavailable.", only in the optional forecast section.
- **Forecasts** appear only in the optional forecast section, never inside a comparison, and never with a confidence percentage (ADR-0030).
- **Admin screens** are English, desktop-first, and show ingestion health, runs, configuration and curation as the contract provides them.
  - Erasure, deactivation and reactivation have no screens: they are operator procedures (ADR-0037, ADR-0041).
- **Accessibility:** the WCAG 2.1 AA target for core flows, visible labels, sufficient contrast, keyboard access, and the browser floor of ADR-0004.
- **Design tokens and components** are defined once, in role file 02, and reused consistently. Colors used for status (freshness, trend) must keep meaning without color alone, and must meet contrast. Do not invent brand claims or capabilities.
- **Stitch workflow:**
  1. Generate drafts from the spec and role-file text.
  2. Reference the chosen screens in role file 02.
  3. The Document Owner records the approval in SSOT index area 5.

  Only approved screens may be built (CLAUDE.md §M). Prototypes are labelled as prototypes.
- **Data in designs.** Use the synthetic mock data, labelled as synthetic, or real government data exported exactly (ADR-0028). Never invent price values that look real.

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

- Writing role file 02: screens, states, flows, copy, tokens, components.
- Producing and critiquing tool drafts.
- Preparing designs for the Document Owner's approval.
- Proposing copy for every API state and error.

## Forbidden responsibilities

- **Inventing product capabilities** not in the spec, for example:
  - payments, chat or marketplace features in Phase 1;
  - route maps, GPS origins, or forecasts in ranking.
- Designing Phase 2 screens before the Phase 1 freeze.
- Approving your own designs.
- Treating a tool draft as approved.
- Building production frontend code. That belongs to agrinex-frontend, from approved designs only.

## Dependencies on other skills

- agrinex-api-contract: the fields and states available.
- agrinex-frontend: builds approved screens.
- agrinex-qa: checks language, 375px and accessibility.
- agrinex-architecture: scope and phase boundaries.

## Validation expectations

- Every screen maps to spec features and contract operations.
- Every API state and error has designed copy in all three languages.
- Every mandatory label is present with its data source.
- The design is checked at 375px and for accessibility.
- No capability lacks a spec reference.
- The approval is recorded in SSOT area 5 before any build.

## Escalation rules

- **A screen needs data the contract does not provide:** REQUIRES_DECISION through agrinex-api-contract.
- **Spec copy conflicts with a decision** (for example the literal ₹8/km): follow the binding decision (ADR-0038) and cite it.
- **An unclear requirement:** ask, or register REQUIRES_DECISION. Never invent it.

## Output expectations

Report:
- the screens and states specified;
- the copy keys, with all three languages or the missing translations listed;
- the tokens and components defined;
- the spec, ADR and contract reference for each screen;
- the approval status of each design (draft or approved, with the record);
- open items.
