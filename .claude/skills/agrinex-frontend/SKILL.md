---
name: agrinex-frontend
description: Building and reviewing the AgriNex Next.js frontend. Covers the farmer-first comparison wizard, results, trends, What-If simulator, alerts, sign-in and admin screens, consumed strictly from the approved /api/v1 contract or contract-matching mocks. Use this skill whenever a task touches frontend code, pages, components, MSW mock mode, client-side calculations, authentication handling, localization (Marathi, Hindi, English), accessibility, responsive layout, CSP, or how the UI shows loading, empty, stale, outage and error states.
---

# AgriNex frontend

## Purpose

Deliver honest, farmer-first screens that show only what the API returns: real government prices with provenance and freshness, never invented values. Build them only from approved designs.

This skill points to the authoritative documents. If it disagrees with them or with a binding decision, they win: correct this file.

## When to use

- Scaffolding or changing the Next.js application.
- Writing or reviewing pages, components, hooks, the API client or MSW handlers.
- Client-side What-If logic, authentication and refresh handling, localization, accessibility, responsiveness, CSP.

## Source hierarchy

This follows CLAUDE.md §B:

1. The master specification.
2. Binding decision-log entries.
3. The authoritative documents:
   - `contracts/` and `docs/api/`;
   - `docs/architecture/FRONTEND_ARCHITECTURE.md`;
   - role files 02 and 03, once created and listed as authoritative.
4. Current official Next.js, React, Tailwind, Recharts and MSW documentation for the baseline versions, fetched with Context7.

## Required reading (READ SSOT FIRST)

1. `CLAUDE.md` §H and §M, then `docs/00_SSOT_INDEX.md` areas 4 and 5, and `docs/REQUIRES_DECISION.md`.
2. `docs/architecture/FRONTEND_ARCHITECTURE.md`.
3. `docs/api/FRONTEND_CONSUMPTION.md`:
   - §2, the screen-state matrix and mandatory content;
   - §3, the client-side What-If algorithm;
   - §4, the authentication flow.
4. `docs/api/API_CONVENTIONS.md`: §4 numbers, §6 errors, §7 security.
5. `docs/api/PROVENANCE_AND_FRESHNESS.md` §5, the states and messages.
6. `contracts/openapi.yaml`, and `frontend/__mocks__/api/README.md`.
7. `docs/architecture/CURRENT_TECHNOLOGY_BASELINE.md`, and the entries ADR-0004, ADR-0015, ADR-0016, ADR-0017, ADR-0026, ADR-0027, ADR-0034, ADR-0038, ADR-0039, ADR-0042, ADR-0043 to ADR-0048.

## Non-negotiable AgriNex rules

- **Data source.** Use only the approved contract, or mocks that match it. Never depend on database internals or invent fields (CLAUDE.md §H).
- **Screens** are built only from approved designs: role file 02, with each approval recorded in SSOT index area 5 (ADR-0027). The approved direction is Hisaab with its four borrowings (ADR-0043). Screens running on mocks are labelled as prototypes.
- **Recommendation labels** (ADR-0045; `FRONTEND_CONSUMPTION.md` §2.1): "Recommended" only for rank 1; "Highest price" and "Highest estimated net realization" only where true; the trade-off sentence is worded by the sign of the estimated difference; What-If rank 1 is "Highest estimated net (what-if)". Never re-rank or show a score.
- **Copy:** Marathi and Hindi strings come from role file 02 §10 and its glossary (ADR-0046).
- **Never fabricate or fill prices.**
  - Show each price with its report date, freshness badge, dataset, variety, grade, unit and attribution.
  - Say "Latest reported price", never "live" or "real-time" (spec §31 RULE 4, §49).
- **Mandatory labels:**
  - "Estimated — based on ₹{appliedRatePerKm}/km assumption. Actual costs will vary.", showing the rate actually applied (ADR-0038). Never hard-code ₹8/km;
  - "Estimated net realization";
  - approximate distances;
  - the standard disclaimer (spec §32, §49).
- **Origin.** Send `originLat` and `originLon` exactly as `listDistricts` publishes the selected district's centroid. Never use device location. The server rejects anything else (ADR-0039).
- **Every state is designed:**
  - loading, empty and no recent data;
  - no market in range, with an adjust-distance control;
  - the stale and outage banner;
  - insufficient trend data;
  - forecast unavailable;
  - each error, including a non-centroid origin (400), `ESTIMATE_OUT_OF_RANGE` (422) and 429 with `Retry-After`.

  Map errors by `code` and `errors[]`. Never show `detail` to farmers.
- **Authentication** (ADR-0016, ADR-0017, ADR-0042):
  - keep the access token in memory only, never in Web Storage;
  - serialize refresh calls;
  - on 401, refresh once and retry;
  - after `REFRESH_TOKEN_INVALID`, retry operations that allow anonymous access without the token;
  - a 429 from refresh means wait `Retry-After` and retry once;
  - the browser carries the `__Secure-agrinex_refresh` cookie, and frontend code never reads it;
  - registration sends the mobile number and the email; login sends either as `identifier` (ADR-0047);
  - verification and reset links carry the token in the URL fragment: read it, remove it from the address bar at once, send it only in the body, never log or store it; the reset page uses `Referrer-Policy: no-referrer`; a reset never signs in (ADR-0048; `FRONTEND_CONSUMPTION.md` §4).
- **What-If** follows `FRONTEND_CONSUMPTION.md` §3:
  - exact decimal arithmetic;
  - rank by net realization;
  - re-evaluate only `PRICE_ADVANTAGE` and `LOWER_TRANSPORT`;
  - send only the values the user changed;
  - call simulate on slider release;
  - use the server response as the new base.

  Composite scores are never computed in the browser.
- **Configuration:**
  - no secret in `NEXT_PUBLIC_*` variables;
  - server-side rendering does not call the backend in Phase 1 (ADR-0017 item 4).
- **Localization, accessibility and responsiveness:**
  - Marathi, Hindi and English, falling back to `name` when a localized name is null (ADR-0015);
  - WCAG 2.1 AA target for core flows;
  - no horizontal scroll at 375px (spec §48);
  - the browser floor of ADR-0004.
- **Forecasts** appear only in the optional forecast section, if the ML track exists. Never mix them into comparisons, and never show a confidence percentage (ADR-0030).
- **CSP release gate.** The final Content-Security-Policy is verified during frontend implementation, before release (ADR-0017 item 7; ADR-0042, SR-11).

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

- Implementing approved screens and client logic against the contract and mocks.
- MSW mock mode, frontend tests, localization files, accessibility and responsive fixes.

## Forbidden responsibilities

- Building screens from unapproved or invented designs.
- Inventing endpoints, fields or features.
- Computing scores, or fabricating or caching prices as current.
- Storing tokens in Web Storage, or putting secrets in client code.
- Building screens for operator procedures (there are none) or for Phase 2 before the freeze.

## Dependencies on other skills

- agrinex-design: approved screens and copy.
- agrinex-api-contract: fields and errors.
- agrinex-qa: tests.
- agrinex-release: gates, including CSP.

## Validation expectations

- Validate every mock against the contract.
- Unit tests for the What-If arithmetic against hand-computed values.
- Tests that each mandatory label renders with the right data, including the applied rate.
- Auth-flow tests: 401 then refresh; refresh refused; 429.
- Check 375px, the three languages with no hard-coded farmer strings (spec §48), and keyboard and contrast.
- Choose the test tools at scaffolding (ADR-0034).

## Escalation rules

- **No approved design for a screen:** stop and ask (CLAUDE.md §M).
- **The contract lacks a field or state the design needs:** REQUIRES_DECISION through agrinex-api-contract.
- **Auth, cookie or CSP changes:** human security review.

## Output expectations

Report:
- the screens and components changed, with their approved design reference;
- the contract operations used;
- the states covered;
- the tests run with their results;
- open items;
- what was not done.
