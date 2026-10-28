# AgriNex frontend (Phase 1)

The farmer-facing web app (Marathi, Hindi, English) and the English admin area of AgriNex. Next.js 16 App Router, React 19, TypeScript 6, Tailwind CSS 4 (approved baseline ADR-0002; tooling ADR-0049, decided under ADR-0050).

Sources of truth, in order: the master specification, the decision log, `contracts/openapi.yaml`, and the design source of record, role file 02 (`docs/02_AgriNex_UIUX_Phase1_Specification.md`; approved direction ADR-0043). This README only explains how to run the code.

## Requirements

- Node.js 24 (`engines`: `>=24.0.0 <25`) and npm 11.
- For real data: the AgriNex backend serving `/api/v1` on the same origin (in production, through the edge proxy; ADR-0017).

## Commands

| Command | What it does |
|---|---|
| `npm ci` | Installs the pinned dependencies |
| `npm run dev` | Development server on http://localhost:3000 |
| `npm run build` / `npm start` | Production build and server |
| `npm run typecheck` | `tsc --noEmit` |
| `npm run lint` | ESLint (eslint-config-next) |
| `npm test` | Vitest: unit, component, hook and contract tests |
| `npm run generate:api-types` | Regenerates `lib/api/schema.d.ts` from `../contracts/openapi.yaml` (run it after every contract change) |
| `npm run generate:mocks` | Regenerates `lib/mocks/catalog.generated.ts` from `__mocks__/api/` |

## Environment variables

No secret is ever needed or allowed in this app (CLAUDE.md §K; spec §27, §28).

| Variable | Where | Purpose |
|---|---|---|
| `NEXT_PUBLIC_USE_MOCK_API` | Build time, public | `true` starts mock mode (below). Leave unset for real data |
| `AGRINEX_DEV_API_ORIGIN` | Development server only | For example `http://localhost:8080`: forwards `/api/v1/*` to a local backend. Ignored outside `next dev` |

## Mock mode

With `NEXT_PUBLIC_USE_MOCK_API=true`, the browser answers API calls from the synthetic, contract-validated files in `__mocks__/api/` through MSW (spec §44). A "SYNTHETIC DATA · PROTOTYPE" banner is shown on every screen; mock data is never presented as real (spec P10).

- The answer for a request is chosen by path, query and request-body similarity. `?mock=<file name without .json>` selects a scenario for the tab, for example `?mock=selling-options-compare.200-no-recent-data`.
- The mock sign-in and the chosen scenarios last for the browser tab (sessionStorage); alert changes last until the page reloads (memory).
- A path without a mock answers a contract-shaped 404.

`lib/api/contract.test.ts` validates every mock against the operation and status it declares in the contract.

## Security notes

- The access token is held in memory only; the refresh token is the server's HttpOnly cookie, which this code never reads (ADR-0016).
- `proxy.ts` sets a nonce-based Content-Security-Policy on every page. The edge proxy (Nginx) must pass it through and must not add a second CSP for pages. Development relaxes `script-src` (`'unsafe-eval'`) and `style-src` for React's development tooling only.
- Emailed links carry their token in the URL fragment; `/reset-password` and `/verify-email` remove it from the address bar at once and send `Referrer-Policy: no-referrer` (ADR-0048).
- The CSP and the authentication code are security-sensitive and need explicit human review before merge (CLAUDE.md rule 16).

## Layout

| Path | Contents |
|---|---|
| `app/` | Routes: farmer screens S01–S16, admin A01–A05 under `/admin`, error and not-found states |
| `components/` | Screens and UI parts, grouped by area |
| `lib/api/` | The only HTTP client, typed endpoints, errors, generated contract types |
| `lib/auth/` | Session store (memory token, serialized refresh), identifiers, emailed-link token |
| `lib/compare/` | Comparison input, view model, the client What-If calculation, recommendation labels |
| `lib/i18n/` | Language cookie, typed catalogues (`public/locales/`), number and date formatting |
| `lib/mocks/` | MSW handlers for mock mode |
