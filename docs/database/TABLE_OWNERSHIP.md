# AgriNex — Table Ownership

**Status:** AUTHORITATIVE. This is the **resolution of RD-017** (module data ownership and cross-module access paths). Task 7 proposed it on 2026-10-03, and ADR-0013 adopted it the same day with the amendments recorded here: owners of the four tables added by the decision lock, and the approved dependency set ([DECISION_LOG.md](../architecture/DECISION_LOG.md)).

## 1. Ownership rule

- **Source rule.** "No cross-module direct DB access; each module owns its repositories" (§27; §47 Q11). Task 6 formalized it ([COMPONENT_ARCHITECTURE.md §6](../architecture/COMPONENT_ARCHITECTURE.md#6-boundaries)): modules own their tables, and cross-module interaction goes through defined module interfaces.
- **Exactly one owner per table.** The owner holds the table's repository and migrations, and is the only module whose code reads or writes it.
- **Shared data is not copied.** A module that needs another module's data calls that module's interface; it never keeps a duplicate table.
- **Foreign keys across modules are allowed** as integrity constraints. The spec's own §20 diagram has them (for example `SELLING_OPTIONS` → `PRICE_OBSERVATIONS`). A foreign key is enforced by PostgreSQL and is not module code accessing another module's table.
- **Module dependencies.** An access path between modules needs a module dependency. §13 draws four; ADR-0013 approved the others (section 4).
- **Tooling is outside the rule.** See section 7.

## 2. Ownership table

| Table | Owner Module | Why | Access Pattern | Phase |
|---|---|---|---|---|
| `states` | geography | Served by the geography API group (§30 `GET /api/v1/geography/*` → geography module). | Read: geography module (API and pre-fetched wizard data, §21 step 3). Write: reference-data seed tooling only (§42 item 7, §43). Other modules use `id` references only. | Phase 1 |
| `districts` | geography | Served by the geography API group (§30). | Read: geography module. Write: seed tooling only. `markets` (price) and `farmer_profiles` (auth) hold FK references; they read district attributes only through the geography module. Recommendation reads district centroids through the geography module for the origin check (ADR-0039). | Phase 1 |
| `localities` | geography | Served by the geography API group (§30). | Read: geography module. Write: seed tooling only. | Phase 1 |
| `district_source_aliases` | geography | District identity belongs to the geography module; ingestion resolves districts through it (ingestion → geography, ADR-0013). | Read: geography module (resolution for ingestion). Write: seed tooling and admin curation through the geography module (ADR-0020). | Phase 1 |
| `commodities` | commodity | Served by the commodity API group (§30 `GET /api/v1/commodities/*`). | Read: commodity module. Write: seed tooling and admin curation through the commodity module (ADR-0020). Ingestion validates commodity values, reads verified units and registers varieties and grades only through the commodity module interface (ADR-0006, ADR-0013). Other modules hold `id` references. | Phase 1 |
| `varieties` | commodity | Served by the commodity API group (§30 `VarietyResponse`). | As `commodities`. | Phase 1 |
| `grades` | commodity | Served by the commodity API group (§30). | As `commodities`. | Phase 1 |
| `commodity_source_aliases` | commodity | Legacy source spellings of master commodities, with DMI evidence (ADR-0051). | Read: commodity module, when ingestion resolves a source name through the commodity module interface. Write: migrations only (V12). | Phase 1 |
| `source_datasets` | price | Provenance of observations; the price module applies dataset precedence when selecting observations (ADR-0010). Owning it in the ingestion module would force a price → ingestion dependency and a module cycle (ingestion → price already exists, §13). Decided in ADR-0013. | Read: price module (observation queries); ingestion reads it through the price module interface (dependency ingestion → price exists, §13). Write: seed tooling only. | Phase 1 |
| `markets` | price | The `/api/v1/markets/*` group belongs to the price module (§30), and recommendation reaches markets through price (recommendation → price, §13). Geography ownership would add a recommendation → geography dependency that §13 does not have. | Read: price module (market detail, candidate discovery for recommendation, alias resolution for ingestion). Write: price module only: PROVISIONAL registration requested by ingestion, and admin curation (verify, merge, deactivate, set coordinates) requested by the admin module (ADR-0013, ADR-0014, ADR-0020). | Phase 1 |
| `market_source_aliases` | price | Market identity resolution belongs with market ownership; ingestion already depends on price (§13). | Read: price module (resolution for ingestion through the price interface). Write: price module only, for the seed, automatic registration and admin merges (ADR-0014, ADR-0020). | Phase 1 |
| `price_observations` | price | Market price data belongs to the price module (§13 price module; §30 `/markets/{id}`, `/markets/{id}/trends`). | Write: only the price module; ingestion submits normalized records through the price module interface (ingestion → price, §13). Read: price module (trends, market detail); recommendation through price (§13); alert evaluation through price (alert → price, ADR-0013); optional ML through a read-only daily-series view (ADR-0030). | Phase 1 |
| `price_observation_revisions` | price | Written by the price module in the same transaction as the update it records. | Write: price module only, inside the observation upsert (ADR-0009). Read: price module (audit, market detail history); admin through ingestion and price for health figures (records_updated, ADR-0012). | Phase 1 |
| `ingestion_runs` | ingestion | The ingestion module runs ingestion and records its audit trail (§13, §15). | Write: ingestion module. Read: admin module through the ingestion module (admin → ingestion, §13); offline evaluation scripts (§43). | Phase 1 |
| `ingestion_errors` | ingestion | Part of the ingestion audit trail. | Write: ingestion module. Read: admin module through the ingestion module (§13). | Phase 1 |
| `ingestion_source_settings` | ingestion | Scheduling belongs to the ingestion module; admin changes it through the ingestion interface (admin → ingestion, §13). | Read: ingestion module (scheduler). Write: ingestion module on behalf of the admin module (ADR-0020). | Phase 1 |
| `ingestion_report_dates` | ingestion | The report-date ledger of historical ingestion (ADR-0054). | Write: ingestion module, after each HISTORICAL delivery unit. Read: ingestion module (gap check, coverage); admin module through the ingestion module (§13). | Phase 1 |
| `users` | auth | Authentication and accounts belong to the auth module (§30 `POST /api/v1/auth/*`). | Read/write: auth module only. Other modules receive the caller identity from the authenticated principal (cross-cutting `SecurityConfig`, §28), never by querying `users`. | Phase 1 |
| `farmer_profiles` | auth | Part of the account (§20 USERS \|\|--o\| FARMER_PROFILES); the auth module owns account data. | Read/write: auth module. Recommendation and alert modules obtain the caller's profile ID through the auth module interface (recommendation → auth, alert → auth, ADR-0013); they never query this table. | Phase 1 |
| `refresh_tokens` | auth | Token handling is part of authentication (§28 `JwtService`, auth module). | Read/write: auth module only. | Phase 1 |
| `transport_assumptions` | transport | Only the transport module computes transport cost (§13, §32); its parameters stay with it. | Read: transport module (used by recommendation → transport, §13). Write: admin module through the transport module interface (admin → transport, ADR-0013); every change is audited (ADR-0020). | Phase 1 |
| `recommendation_weights` | recommendation | Only the recommendation module scores and ranks (§13, §16). | Read: recommendation module. Write: admin module through the recommendation module interface (admin → recommendation, ADR-0013); every change is audited (ADR-0020). | Phase 1 |
| `selling_searches` | recommendation | Written by the recommendation module as part of every comparison (§16). | Read/write: recommendation module. Offline evaluation reads (§36, §43). | Phase 1 |
| `recommendation_runs` | recommendation | Written by the recommendation module (§16). | Read/write: recommendation module. Phase 2 listing reads it through the recommendation module (Phase 2 → Phase 1 only). Offline evaluation (§36). | Phase 1 |
| `selling_options` | recommendation | Written by the recommendation module (§16). | Read/write: recommendation module. Offline evaluation (§36). | Phase 1 |
| `price_alerts` | alert | The alert module manages alerts and evaluates them (§30 `/api/v1/alerts`, §34 `AlertEvaluationService`). | Read/write: alert module. Evaluation after every run that changed observations is invoked by ingestion through the alert module interface (ingestion → alert, ADR-0013, ADR-0019). Prices are read through the price module (alert → price). | Phase 1 |
| `alert_events` | alert | Created by alert evaluation (§34). | Read/write: alert module only. | Phase 1 |
| `admin_audit_events` | admin | Admin actions are orchestrated by the admin module, which records them. | Write: admin module only (insert). Read: admin module (audit view). | Phase 1 |
| `forecasts` | price | Owned by the price module, because forecasts are per market and commodity like price data (ADR-0013, ADR-0030). The ML job writes through a restricted database role. | Write: the ML job only, through a dedicated database role limited to SELECT on the daily-series view and INSERT/DELETE on this table (ADR-0030). Read: price module, for the optional forecast endpoint only. Core flows never read it. | Optional (ML) |
| `produce_listings` | listing | Farmer produce listings, optionally linked to a Phase 1 recommendation run. Source: §23 (`INSERT produce_listings`, `recommendationRunId`, `commodityId`), F2.3, F2.4, §47 Q23 | Owner module only; may reference Phase 1 tables (users, commodities, recommendation_runs); Phase 1 never references it | PHASE 2 / DEFERRED |
| `bids` | bid | Bids by verified buyers on listings. Source: §23 (`INSERT bid`, statuses PENDING / ACCEPTED / REJECTED), F2.5–F2.7 | Owner module only; may reference Phase 1 tables (users, commodities, recommendation_runs); Phase 1 never references it | PHASE 2 / DEFERRED |
| `transactions` | transaction | Transaction created from an accepted bid; server-enforced state machine (state model deferred: RD-012, ADR-0032). Source: §18, §23 (`INSERT transactions`, `currentState`), F2.8, F2.9 | Owner module only; may reference Phase 1 tables (users, commodities, recommendation_runs); Phase 1 never references it | PHASE 2 / DEFERRED |
| `transaction_state_history` | transaction | Audit trail of every transaction state transition (actor, timestamp, reason). Source: §23, F2.11, FR-24 | Owner module only; may reference Phase 1 tables (users, commodities, recommendation_runs); Phase 1 never references it | PHASE 2 / DEFERRED |
| `buyer_verifications` | auth | Buyer verification state (§19); documents stored in PostgreSQL with access control (ADR-0031). Source: §19, §23 (`buyer_verifications WHERE status=VERIFIED`), F2.2, F2.12; ADR-0031 | Owner module only; may reference Phase 1 tables (users, commodities, recommendation_runs); Phase 1 never references it | PHASE 2 / DEFERRED |
| `rating_storage_unnamed` | rating | Post-completion ratings in both directions. Table name not given by the spec. Source: §23 (`POST /api/v1/ratings/{transactionId}`), F2.10, FR-25; the spec names no table | Owner module only; may reference Phase 1 tables (users, commodities, recommendation_runs); Phase 1 never references it | PHASE 2 / DEFERRED |

## 3. Ownership by module

| Module | Owns (Phase 1) | Owns (optional / Phase 2) |
|---|---|---|
| auth | `users`, `farmer_profiles`, `refresh_tokens` | `buyer_verifications` (PHASE 2 / DEFERRED, ADR-0031) |
| geography | `states`, `districts`, `localities`, `district_source_aliases` | — |
| commodity | `commodities`, `varieties`, `grades`, `commodity_source_aliases` | — |
| price | `source_datasets`, `markets`, `market_source_aliases`, `price_observations`, `price_observation_revisions` | `forecasts` (optional ML, ADR-0030) |
| ingestion | `ingestion_runs`, `ingestion_errors`, `ingestion_source_settings`, `ingestion_report_dates` | — |
| transport | `transport_assumptions` | — |
| recommendation | `recommendation_weights`, `selling_searches`, `recommendation_runs`, `selling_options` | — |
| alert | `price_alerts`, `alert_events` | — |
| admin | `admin_audit_events` | — |
| listing (Phase 2) | — | `produce_listings` (PHASE 2 / DEFERRED) |
| bid (Phase 2) | — | `bids` (PHASE 2 / DEFERRED) |
| transaction (Phase 2) | — | `transactions`, `transaction_state_history` (PHASE 2 / DEFERRED) |
| rating (Phase 2) | — | `rating_storage_unnamed` (PHASE 2 / DEFERRED) |

## 4. Module dependencies required by this ownership

| From module | To module | Status | Needed for |
|---|---|---|---|
| recommendation | price | EXISTS (§13) | Candidate markets within radius, observation selection (ADR-0010), trends; market names for the response. |
| recommendation | transport | EXISTS (§13) | Active transport assumptions; transport cost. |
| ingestion | price | EXISTS (§13) | Source-dataset registry; market alias resolution and PROVISIONAL registration; observation upsert with per-record outcome for the counters. |
| admin | ingestion | EXISTS (§13, §30) | Health, run history, errors, manual trigger, pause and resume. |
| ingestion | commodity | APPROVED (ADR-0013) | Commodity known-list validation and verified unit; automatic variety and grade registration (ADR-0006, ADR-0008). |
| ingestion | geography | APPROVED (ADR-0013) | District alias resolution (ADR-0014). |
| ingestion | alert | APPROVED (ADR-0013) | Invoke alert evaluation after every run that changed observations, in its own transaction (ADR-0019). |
| alert | price | APPROVED (ADR-0013) | Latest selected price and 7-day change for alert conditions (§34, ADR-0010, ADR-0025). |
| alert | commodity | APPROVED (ADR-0013) | Commodity names and unit checks in alert responses. |
| alert | auth | APPROVED (ADR-0013) | The caller's farmer profile, created on demand for ADMIN (ADR-0016). |
| recommendation | commodity | APPROVED (ADR-0013) | `commodityName` and the price-unit check (ADR-0008, ADR-0023). |
| recommendation | auth | APPROVED (ADR-0013) | The caller's farmer profile for `selling_searches`. |
| recommendation | geography | APPROVED (ADR-0039) | Read district centroids to accept only district-centroid comparison origins. |
| price | geography | APPROVED (ADR-0013) | District data for market registration and approximate coordinates (ADR-0014). |
| price | commodity | APPROVED (ADR-0013) | Variety and grade names for observation disclosure (ADR-0010, ADR-0022). |
| admin | transport | APPROVED (ADR-0013) | Admin updates transport assumptions (§30 `PUT /api/v1/admin/config/*`). |
| admin | recommendation | APPROVED (ADR-0013) | Admin updates recommendation weights. |
| admin | price | APPROVED (ADR-0013) | Market curation: verify, merge, deactivate, set coordinates (ADR-0014, ADR-0020). |
| admin | commodity | APPROVED (ADR-0013) | Add commodities, verify units, edit localized names (ADR-0020). |
| admin | geography | APPROVED (ADR-0013) | Add district aliases (ADR-0014). |
| admin | auth | APPROVED (ADR-0013) | Admin bootstrap and the audited operator procedures for account erasure, deactivation and reactivation (ADR-0028, ADR-0029, ADR-0037, ADR-0041). |
| auth | geography | APPROVED (ADR-0013) | Validate the farmer's default district. |

The four existing dependencies are drawn in §13. The others are inter-module dependencies approved in ADR-0013, plus recommendation → geography approved in ADR-0039 (CLAUDE.md §G; [COMPONENT_ARCHITECTURE.md §6](../architecture/COMPONENT_ARCHITECTURE.md#6-boundaries)); no other dependency may be implemented without a new decision. No dependency runs from a Phase 1 module to a Phase 2 module, and the graph is acyclic: geography, commodity and transport are leaves, and no module depends on admin. Read-only ports (ADR-0035) run over these edges: price declares the sync-status and freshness-policy ports that ingestion and recommendation implement, and commodity declares the availability port that price implements. No table is read across a boundary. In particular `source_datasets` is owned by price, not ingestion, because the price module filters observations by dataset and an ingestion-owned registry would need a price → ingestion dependency, closing a cycle with the existing ingestion → price.

## 5. Prohibited direct cross-module database access

Every row below is **PROHIBITED** as direct database access (repository, JPA query, native SQL, or join) from the module's code. The right-hand column is the only allowed path.

| Module | Must not directly access | Allowed path instead |
|---|---|---|
| ingestion | `price_observations` (insert, update, select) | price module observation-write interface (§13 ingestion → price) |
| ingestion | `markets`, `market_source_aliases`, `source_datasets` | price module resolution and registry interface |
| ingestion | `commodities`, `commodity_source_aliases`, `varieties`, `grades` | commodity module interface: known-list validation and alias resolution (ADR-0051), verified unit, variety and grade registration (ingestion → commodity, ADR-0013) |
| ingestion | `districts`, `district_source_aliases` | geography module district resolution (ingestion → geography, ADR-0013) |
| ingestion | `price_alerts`, `alert_events` | alert module evaluation interface, invoked after the run in its own transaction (ingestion → alert, ADR-0013, ADR-0019) |
| recommendation | `price_observations`, `markets` | price module candidate and observation interface (§13) |
| recommendation | `transport_assumptions` | transport module (§13) |
| recommendation | `commodities` | commodity module, for `commodityName` and the unit check (recommendation → commodity, ADR-0013) |
| recommendation | `users`, `farmer_profiles` | auth module interface, which resolves the caller's profile from the authenticated principal (recommendation → auth, ADR-0013) |
| alert | `price_observations`, `price_observation_revisions` | price module latest-price, 7-day-change and changed-by-run interface (alert → price, ADR-0013) |
| alert | `users`, `farmer_profiles` | auth module interface (alert → auth, ADR-0013), which also creates an ADMIN's profile on demand (ADR-0016) |
| admin | `ingestion_runs`, `ingestion_errors`, any price data for health figures | ingestion module (§13 admin → ingestion), which may use the price module |
| admin | `transport_assumptions` | transport module configuration interface (admin → transport, ADR-0013) |
| admin | `recommendation_weights` | recommendation module configuration interface (admin → recommendation, ADR-0013) |
| admin | `markets`, `market_source_aliases`, `commodities`, `district_source_aliases`, `ingestion_source_settings`, `users` | the owning module's curation or control interface (admin → price, commodity, geography, ingestion, auth; ADR-0013, ADR-0020) |
| price | `districts`, `states` | geography module, for PROVISIONAL market registration, approximate coordinates and names (price → geography, ADR-0013) |
| price | `ingestion_runs` | none needed: price stores the `ingestion_run_id` the caller passes and never reads ingestion tables |
| price | `commodities`, `varieties`, `grades` | commodity module, for variety and grade names in observation disclosure (price → commodity, ADR-0013) |
| geography, commodity, transport | any table they do not own | none needed: these modules have no outbound dependency |
| auth | `districts` | geography module, to validate a farmer's default district (auth → geography, ADR-0013) |
| Phase 2 modules (listing, bid, transaction, rating) | any Phase 1 table | the owning Phase 1 module's interface (for example listing → recommendation for the reference price, §23) |
| any module | a join across ownership boundaries, or a repository shared between modules | compose results in code from each owner's interface |

## 6. SSOT flows that touch other modules' tables

The spec describes some flows at the level of tables. Read literally, they would break §27. This section records each such flow and its decided access path (CONFLICT C-02, resolved by ADR-0013; [ARCHITECTURE_INDEX.md §12.2](../architecture/ARCHITECTURE_INDEX.md#122-conflicts-within-the-master-specification-found-in-task-6)).

| Flow (spec) | Literal statement | Problem under §27 | Proposed path | Status |
|---|---|---|---|---|
| Ingestion load (§15, §22) | `IngestionService` "UPSERT price_observations" | ingestion writes a price-owned table | ingestion submits normalized records to the price module, which upserts and returns each record's outcome for the counters | DECIDED (ADR-0013); dependency exists (§13) |
| Source registry (§22) | `IngestionService` "Load source_datasets" | reads a price-owned table | through the price module | DECIDED (ADR-0013); dependency exists |
| Post-run alerts (§15, §22) | ingestion "SELECT active price_alerts", "INSERT alert_events", "UPDATE price_alerts.last_triggered_at" | ingestion reads and writes alert-owned tables | after finalizing a run, ingestion invokes the alert module's evaluation with the run ID; the alert module does the reads and writes (§34 `AlertEvaluationService`) | DECIDED (ADR-0013, ADR-0019); ingestion → alert |
| Alert conditions (§34) | "latestModalPrice", "changePercent7Day" | alert needs price data | alert module calls the price module | DECIDED (ADR-0013); alert → price |
| Admin configuration (§30, §32) | `PUT /api/v1/admin/config/*`; "Admin-Managed in `transport_assumptions` table" | admin writes tables used by transport and recommendation | the tables are owned by their consumers; admin calls their configuration interfaces (insert-new-version, section 12 of [DATABASE_ARCHITECTURE.md](DATABASE_ARCHITECTURE.md#12-recommendation-persistence)) | DECIDED (ADR-0013); admin → transport, admin → recommendation |
| Comparison response (§16) | response includes `commodityName` | recommendation needs commodity data | recommendation calls the commodity module | DECIDED (ADR-0013); recommendation → commodity |
| Recommendation configuration (§16) | "Load active recommendation_weights / transport_assumptions" | none: weights are recommendation-owned; transport assumptions come through transport (§13) | — | consistent |
| Optional ML (§12, §14, §35) | ML "Reads from PG, writes forecasts back" | the ML container is outside the monolith | price owns `forecasts`; the ML job reads a daily-series view and writes forecasts through a restricted database role (ADR-0030) | DECIDED (ADR-0030) |
| Phase 2 listing (§23) | "Fetch recommendation_run to attach reference mandi price" | none, if done through the recommendation module | listing → recommendation (Phase 2 → Phase 1 only) | consistent; Phase 2 |

## 7. Tooling and external access

These actors are not backend module code, so the module rule does not apply to them. Each is limited as stated. ADR-0013 approved this interpretation.

| Actor | Access | Limit |
|---|---|---|
| Flyway migrations (§10, §12, §43) | DDL on all tables | One migration folder; each migration states the owning module of the tables it changes |
| Seed scripts (`fixtures/seed_data.sql`, §43, §45) | Insert reference data and initial configuration | Reference and configuration tables only; demo fixtures only in demo-profile environments and only as exact exports of real observations (ADR-0021, ADR-0028) |
| Offline evaluation (`scripts/run_evaluation.sql`, §36, §43) | Read-only SQL over recommendation and ingestion tables | Read-only; not part of the running application |
| Optional ML job (§12, §35) | Read the daily-series view, write forecasts | Dedicated database role: SELECT on the view, INSERT and DELETE on `forecasts` only (ADR-0030) |
| Database administration | As operations require | Outside application flows; security-sensitive (CLAUDE.md §K) |

## 8. Decisions behind this ownership

- **RD-017** → ADR-0013: ownership, prohibited access and the dependency set above.
- **RD-024** → ADR-0030: `forecasts` belongs to price; the ML job uses a restricted database role.
- **RD-025(c)** → ADR-0031: `buyer_verifications` (Phase 2) belongs to auth.
- **RD-018(c)** → ADR-0014: ingestion registers unknown markets as PROVISIONAL through the price module; admins verify or merge them.
- **RD-003** → ADR-0006: ingestion never creates commodities; it registers varieties and grades through the commodity module.
