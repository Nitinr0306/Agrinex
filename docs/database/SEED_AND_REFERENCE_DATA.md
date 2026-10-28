# AgriNex — Seed and Reference Data

**Status:** AUTHORITATIVE. Database architecture from Task 7 (2026-10-03), updated for the decision lock of the same day ([DECISION_LOG.md](../architecture/DECISION_LOG.md): ADR-0006, ADR-0008, ADR-0014, ADR-0015, ADR-0021, ADR-0024, ADR-0028). Where this document and a binding decision-log entry differ, the entry governs. The seeds are Flyway migrations in `backend/src/main/resources/db/migration/`: V7 (source datasets and initial configuration), V8 (the first 53 commodities), V10 (Maharashtra geography), V11 (the commodity master and report units) and V12 (commodity source aliases, ADR-0051). Section 12 records how V10 to V12 were prepared and the market merges of the Task 12 closure (2026-10-15).

> **No-fabrication rule.** Seeds contain only values from an identified source: the spec, the verified government captures, or an identified official source (LGD, Survey of India, DMI's Agmarknet 2.0). No market, commodity, variety, grade, coordinate, unit, translation or price is invented. Missing data stays missing (P10, §31 RULE 5; CLAUDE.md §C).

## 1. Principles

- **What a seed contains:** reference data and initial configuration.
  - Reference data: geography, district aliases, commodities with verified units, and source datasets.
  - Initial configuration: ingestion settings, transport assumptions and recommendation weights.
  - Never price observations presented as real (ADR-0028).
- **How it loads:** reference data and initial configuration are applied by Flyway at backend start, so they load automatically on first boot, exactly once (ADR-0021).
  - The file layout is a choice for the seed task, provided Flyway versions and applies the content: a versioned migration, or §43's `fixtures/seed_data.sql` used as a Flyway location.
  - Demo fixtures and test accounts load only in demo-profile environments (ADR-0021, ADR-0028).
- **Idempotent:** every seed row has a natural key, so a seed can insert only if absent and be re-run safely. The keys are:
  - the state LGD code, the district LGD code and the sub-district LGD code;
  - the district alias pair;
  - the commodity name;
  - the resource ID.
- **Data-driven growth.** A new commodity, variety, grade, market or district is a new row, not a schema change (§27, §38).
  - Ingestion registers varieties and grades automatically, and markets as PROVISIONAL.
  - Only admins add commodities, each with an official reference (ADR-0006, ADR-0014, ADR-0020).
- **Maharashtra statewide:** the Phase 1 data scope is all of Maharashtra (ADR-0006), not a district or pilot region.

## 2. Evidence base and verification status

| Item | Value |
|---|---|
| Government captures | `docs/data/AGRINEX_VERIFIED_GOVERNMENT_API_DATA_REFERENCE.md`, Appendix A: seven unique captures (three historical, four current). 505 records, 322 of them with state `Maharashtra` |
| Capture completeness | Several captures are first pages only (for example 100 of 5,453, 100 of 314, 100 of 647, 100 of 103). Missing pages are not reconstructed (evidence Appendix B) |
| Official Agmarknet 2.0 masters (DMI) | Public filter data, retrieved 2026-10-03: 605 commodities in 16 commodity groups, 2,154 varieties, 18 grades, and for Maharashtra 38 district entries and 429 markets (337 market names with leading, trailing or doubled spaces). Report and atlas data sit behind a captcha and are not used (ADR-0005) |
| Official report exports | `docs/data/raw/`: five "Commodity-wise, Market-wise Daily Report (Weighted Average)" files and one "Marketwise Price & Arrival Report", all for 01-Oct-2026. Every one of the 2,732 market rows states the unit `Rs./Quintal` |
| Agmarknet 2.0 Market-wise dashboard | Retrieved 2026-10-03: "Price (Rs./Quintal)" for the 27 commodities in its filter list, including Onion, Potato and Tomato |
| data.gov.in | `www.data.gov.in` answered HTTP 403 on 2026-10-03 (portal under maintenance). The state catalog page (`karnataka.data.gov.in`) confirms that DMI publishes the price resources through Agmarknet |
| Non-government file | `docs/data/raw/Agriculture_price_dataset.csv` has unknown provenance. It is validation evidence only and never seeds anything (ADR-0005) |

## 3. Maharashtra geography

### 3.1 State

| Column | Seed value | Basis |
|---|---|---|
| `name` | `Maharashtra` | Spec §1, §6; every evidence record |
| `code` | `IN-MH` | ISO 3166-2 (ADR-0006) |
| `lgd_code` | the LGD state code, read from LGD at seed time | ADR-0014 |
| `name_hi`, `name_mr` | curated, with provenance recorded | ADR-0015 |

Other states are not seeded in Phase 1. Adding one needs a decision entry plus its reference data (ADR-0006). The feed's state spellings can differ from official names (for example `Chattisgarh`, `Keralam`); district and market aliases absorb such spellings.

### 3.2 Districts and district aliases

- **Districts:** the 36 Maharashtra districts with names and codes as published by LGD, for example Ahilyanagar 466, Chhatrapati Sambhajinagar 469, Dharashiv 488 (ADR-0014).
- **Centroids:** computed once from Survey of India district boundaries (WGS84; geometric centroid, or a point on the surface when the centroid falls outside the polygon). `centroid_source` records the dataset, version and method. A team member must register on the SoI portal to obtain the boundaries. Until then, districts cannot be seeded, because no estimated coordinate is allowed. (Obtained and seeded in Task 12: section 12.2.)
- **District aliases:** one `district_source_aliases` row per source spelling, each with an official evidence note:
  - the captures contain 17 Maharashtra district spellings: `Ahilyanagar`, `Akola`, `Amarawati`, `Chandrapur`, `Chattrapati Sambhajinagar`, `Dharashiv`, `Dhule`, `Jalgaon`, `Kolhapur`, `Mumbai`, `Nagpur`, `Nashik`, `Pune`, `Sangli`, `Satara`, `Solapur`, `Thane`;
  - the Agmarknet 2.0 master has 38 entries, including renamed duplicates (`Osmanabad` and `Dharashiv`) and entries that are not districts (`Bandra(E)`, `Murum`).

  Renames are mapped on the strength of the government rename notifications. An entry that is not a district is mapped only with official evidence of the district that contains it; without that evidence it stays unmapped, and its records are rejected as `UNKNOWN_DISTRICT`, where admins can see them.

### 3.3 Localities

Localities are the LGD sub-districts (talukas) of each district, with LGD names and codes. Maharashtra has 358. They carry no coordinates in Phase 1, because the origin is always the district centroid (ADR-0014).

## 4. Commodities

### 4.1 The known list

- **Source:** the official Agmarknet 2.0 commodity master, 605 names in 16 commodity groups (ADR-0006). Names are stored whitespace-normalized; two master names carry a trailing space. `category` is the official commodity-group name.
- **Checked against the feed:** all 52 Maharashtra commodity values in the verified captures match master names exactly, as do all 11 Maharashtra headings in the official report exports once the report-only "(MSP: …)" annotation is removed (VERIFIED, 2026-10-03).
- Values are exact source strings, including parentheses and spelling (for example `Cucumbar(Kheera)`, `Ridgeguard(Tori)`, `Soyabean`). Spelling is never corrected, because exact matching depends on it (evidence §0.10).
- `Onion` and `Onion Green` are distinct commodities (evidence §20.3).

The Maharashtra commodity values observed in the captures (VERIFIED OBSERVATION; the historical captures contain only `Onion`, because they were onion-scoped):

| # | Source value (exact) | Seen in | Varieties observed (Maharashtra) | Grades observed (Maharashtra) |
|---|---|---|---|---|
| 1 | `Amla(Nelli Kai)` | current | `Other` | `Local` |
| 2 | `Apple` | current | `Simla` | `Local` |
| 3 | `Bajra(Pearl Millet/Cumbu)` | current | `Other` | `FAQ`, `Non-FAQ` |
| 4 | `Beans` | current | `Other` | `Local` |
| 5 | `Beetroot` | current | `Other` | `Local` |
| 6 | `Bengal Gram(Gram)(Whole)` | current | `Kabul Small`, `Other` | `FAQ`, `Non-FAQ` |
| 7 | `Bhindi(Ladies Finger)` | current | `Other` | `Local` |
| 8 | `Bitter gourd` | current | `Other` | `Local` |
| 9 | `Black Gram(Urd Beans)(Whole)` | current | `Other` | `FAQ`, `Non-FAQ` |
| 10 | `Bottle gourd` | current | `Other` | `Local` |
| 11 | `Brinjal` | current | `Other` | `Local` |
| 12 | `Cabbage` | current | `Other` | `Local` |
| 13 | `Carrot` | current | `Other` | `Local` |
| 14 | `Cauliflower` | current | `Other` | `Local` |
| 15 | `Chikoos(Sapota)` | current | `Other` | `Local` |
| 16 | `Chilly Capsicum` | current | `Other` | `Local` |
| 17 | `Coriander(Leaves)` | current | `Other` | `Local` |
| 18 | `Cucumbar(Kheera)` | current | `Other` | `Local` |
| 19 | `Elephant Yam(Suran)/Amorphophallus` | current | `Other` | `Local` |
| 20 | `Garlic` | current | `Other` | `Local` |
| 21 | `Ginger(Green)` | current | `Other` | `Local` |
| 22 | `Green Chilli` | current | `Other` | `Local` |
| 23 | `Green Gram(Moong)(Whole)` | current | `Green (Whole)`, `Other` | `FAQ`, `Non-FAQ` |
| 24 | `Ground Nut Seed` | current | `Local` | `Local` |
| 25 | `Guar` | current | `Other` | `Local` |
| 26 | `Guava` | current | `Other` | `Local` |
| 27 | `Gur(Jaggery)` | current | `Other` | `Local` |
| 28 | `Jowar(Sorghum)` | current | `Other` | `FAQ`, `Local` |
| 29 | `Karbuja(Musk Melon)` | current | `Other` | `Local` |
| 30 | `Lime` | current | `Other` | `Local` |
| 31 | `Little gourd(Kundru)` | current | `Other` | `Local` |
| 32 | `Maize` | current | `Deshi Red`, `Other` | `Non-FAQ` |
| 33 | `Mango(Raw-Ripe)` | current | `Other` | `Local` |
| 34 | `Methi(Leaves)` | current | `Other` | `Local` |
| 35 | `Mint(Pudina)` | current | `Other` | `Local` |
| 36 | `Mousambi(Sweet Lime)` | current | `Other` | `Local` |
| 37 | `Onion` | current, historical | `1st Sort`, `2nd Sort`, `Chinchwad`, `Fursungi`, `Halva`, `Local`, `Other`, `Red`, `Unhali`, `White` | `Local` |
| 38 | `Onion Green` | current | `Other` | `Local` |
| 39 | `Paddy(Common)` | current | `Other` | `FAQ` |
| 40 | `Papaya` | current | `Other` | `Local` |
| 41 | `Pineapple` | current | `Other` | `Local` |
| 42 | `Plum` | current | `Other` | `Local` |
| 43 | `Pomegranate` | current | `Other` | `Local` |
| 44 | `Potato` | current | `Local`, `Other` | `Local` |
| 45 | `Pumpkin` | current | `Other` | `Local` |
| 46 | `Red gram/Arhar/Tur(whole)` | current | `Other` | `FAQ` |
| 47 | `Ridgeguard(Tori)` | current | `Other` | `Local` |
| 48 | `Spinach` | current | `Other` | `Local` |
| 49 | `Sweet Pumpkin` | current | `Other` | `Local` |
| 50 | `Tomato` | current | `Local` | `Local` |
| 51 | `Water Melon` | current | `Other` | `Local` |
| 52 | `Wheat` | current | `Other` | `FAQ`, `Non-FAQ` |

### 4.2 The persona commodities

| Commodity named in §6 | Source spelling | Evidence |
|---|---|---|
| Onion | `Onion` | Captures; dashboard |
| Tomato | `Tomato` | Captures; dashboard |
| Cotton | `Cotton` | Master; Maharashtra rows in the 01-Oct-2026 Fibre Crops export |
| Soybean | `Soyabean` | Master; Marketwise report export |

All four come from the master. None is seeded under a guessed spelling.

### 4.3 Units (ADR-0008)

- `price_unit` and `price_unit_source` are set only from official evidence that states the unit for that commodity:
  - **The five report exports** (`Rs./Quintal`): Almond(Badam), Ambady/Mesta/Patson, Arecanut(Betelnut/Supari), Asalia, Ashwagandha, Asparagus, Bajra(Pearl Millet/Cumbu), Barley(Jau), Beaten Rice, Betal Leaves, Broken Rice, Cashewnuts, Chicory(Chikori/Kasni), Cocoa, Coffee, Cotton, Dry Grapes, Foxtail Millet(Navane), Isabgul(Psyllium), Jaee, Jowar(Sorghum), Jute, Kodo Millet(Varagu), Kutki, Mahua, Maize, Makhana(Foxnut), Mentha Oil, Paddy(Basmati), Paddy(Common), Quinoa, Ragi(Finger Millet), Rice, Same/Savi, Sweet Corn, Tobacco, Walnut, Wheat, White Muesli, basil, nigella seeds.
  - **The Marketwise report export** (`Rs./Quintal`): its 15 cereals, oil seeds and pulses.
  - **The Market-wise dashboard** (`Rs./Quintal`): its 27 MSP and Tomato/Onion/Potato commodities.
- **Every other commodity** is verified during seed preparation, by exporting the official Daily Price and Arrival Report for its commodity group and recording the export and its date.
- Commodities left without evidence keep `price_unit` NULL. Ingestion rejects their records as `UNIT_UNVERIFIED` until an admin records official evidence (ADR-0020).

### 4.4 Localized names

`name_hi` and `name_mr` are curated, either from an official publication or as a team translation reviewed by a named native speaker, and the provenance is recorded with the seed. A missing value falls back to the English name (ADR-0015).

## 5. Varieties and grades

- **Not seeded.** Ingestion registers them per commodity on first appearance, with the exact normalized source value (ADR-0006). The official master lists 2,154 varieties and 18 grades.
- **Observed Maharashtra varieties (14):** `1st Sort`, `2nd Sort`, `Chinchwad`, `Deshi Red`, `Fursungi`, `Green (Whole)`, `Halva`, `Kabul Small`, `Local`, `Other`, `Red`, `Simla`, `Unhali`, `White`.
- **Observed Maharashtra grades (3):** `FAQ`, `Local`, `Non-FAQ`.
- `Other` and `Local` are real source values. `Local` appears both as a variety and as a grade. Neither means "unknown".

## 6. Markets and source aliases

### 6.1 Observed source strings

The captures contain **131 distinct normalized Maharashtra market strings**. 74 of the raw strings had leading, trailing or doubled internal whitespace. Grouped by the feed's district spelling:

| Source district (as spelled by the feed) | Normalized source market strings observed | Count |
|---|---|---|
| `Ahilyanagar` | `Ahilyanagar APMC`, `APMC Ahilyanagar`, `APMC Jamkhed`, `APMC Karjat`, `APMC Kopargaon`, `APMC Newasa`, `APMC Parner`, `APMC Rahata`, `APMC Sangamner`, `APMC Shevgaon`, `Jamkhed APMC`, `Karjat(Rashin)`, `Kopargaon APMC`, `Newasa(Ghodegaon)`, `Newasa(Ghodegaon) APMC`, `Omchatinya Multi State Agro Purpose Co-Op Society, Dist Ahilyanagar`, `Prasanna Krushi Market, Tal Parnet, Dist Ahilyanagar`, `Rahata APMC`, `Rahuri(Vambori)`, `Rahuri(Vambori) APMC`, `Shevgaon APMC`, `Shree Sairaj Krushi Market, Dist Ahilyanagar` | 22 |
| `Akola` | `Akola APMC`, `APMC Akola` | 2 |
| `Amarawati` | `Amrawati(Frui & Veg. Market)`, `Amrawati(Frui & Veg. Market) APMC` | 2 |
| `Chandrapur` | `APMC Mul`, `Chandrapur(Ganjwad)`, `Chandrapur(Ganjwad) APMC` | 3 |
| `Chattrapati Sambhajinagar` | `APMC Chattrapati Sambhajinagar`, `APMC Gangapur`, `APMC Lasur Station`, `APMC Paithan`, `APMC Vaijpur`, `Chattrapati Sambhajinagar APMC`, `Lasur Station APMC` | 7 |
| `Dharashiv` | `APMC Dharashiv`, `Dharashiv APMC` | 2 |
| `Dhule` | `APMC Dhule`, `APMC Sakri`, `APMC Shirpur`, `Dhule APMC`, `Pratap Nana Mahale Khajgi Krushi Uttpana Bazar, Samati-Dhule`, `Sakri APMC`, `Shirpur APMC` | 7 |
| `Jalgaon` | `APMC Bhusaval`, `APMC Jalgaon`, `Jalgaon APMC` | 3 |
| `Kolhapur` | `APMC Kolhapur`, `APMC Vadgaonpeth`, `Kolhapur APMC`, `Vadgaonpeth APMC` | 4 |
| `Mumbai` | `APMC Mumbai`, `Mumbai- Fruit Market`, `Mumbai-Onion & Potato Market`, `Mumbai-Onion & Potato Market APMC` | 4 |
| `Nagpur` | `APMC Hingna`, `APMC Kamthi`, `APMC Nagpur`, `APMC Ramtek`, `HINGNA - APMC`, `Nagpur APMC`, `Ramtek APMC` | 7 |
| `Nashik` | `APMC Chandwad`, `APMC Devala`, `APMC Dindori`, `APMC Kalvan`, `APMC Lasalgaon`, `APMC Manmad`, `APMC Nampur`, `APMC Nandgaon`, `APMC Nasik`, `APMC Pimpalgaon Baswant`, `APMC Satana`, `APMC Sinner`, `APMC Umrane`, `APMC Yeola`, `Dindori APMC`, `Dindori(Vani)`, `Dindori(Vani) APMC`, `Kalvan APMC`, `Lasalgaon APMC`, `Lasalgaon(Niphad)`, `Lasalgaon(Niphad) APMC`, `Lasalgaon(Vinchur)`, `Malharshree Khajgi Krushi Utpann Bajar, Chandanpuri Tal Malegaon, Nashik`, `Manmad APMC`, `Nasik APMC`, `Pimpalgaon Baswant APMC`, `Pimpalgaon Baswant(Saykheda)`, `Pimpalgaon Baswant(Saykheda) APMC`, `Shivsiddha Govind Producer Company Ltd Sanchalit Khajgi Krush Utpann Bajar, Samati-Nashik`, `Shivsiddha Govind Producer Company Ltd Sanchalit Khajgi Krushi Utpann Bajar Samati-Abhona-Kalwan`, `Shri Rameshwar Krushi Market, M.Nagar, Tal Deola, Dist Nashik`, `Yeola APMC` | 32 |
| `Pune` | `APMC Baramati`, `APMC Khed`, `APMC Pune`, `Junnar(Alephata)`, `Junnar(Narayangaon)`, `Junnar(Narayangaon) APMC`, `Junnar(Otur)`, `Pune(Khadiki)`, `Pune(Khadiki) APMC`, `Pune(Manjri)`, `Pune(Manjri) APMC`, `Pune(Moshi)`, `Pune(Moshi) APMC`, `Pune(Pimpri)`, `Pune(Pimpri) APMC` | 15 |
| `Sangli` | `APMC Vita`, `Sangli(Phale, Bhajipala Market)`, `Sangli(Phale, Bhajipala Market) APMC`, `Vita APMC` | 4 |
| `Satara` | `APMC Karad`, `APMC Lonand`, `APMC Patan`, `APMC Satara`, `APMC Vaduj`, `APMC Vai`, `Karad APMC`, `Satara APMC`, `Vaduj APMC`, `Vai APMC` | 10 |
| `Solapur` | `APMC Akluj`, `APMC Mangal Wedha`, `APMC Solapur`, `Laxmi-Sopan Agriculture Produce Marketing Co Ltd, Barshi, Dist Solapur`, `Mangal Wedha APMC`, `SHRI.SIDDHESHWAR APMC` | 6 |
| `Thane` | `APMC Kalyan` | 1 |

These are source spellings, not canonical markets:

- Several are the same market under two spellings, for example `Manmad APMC` and `APMC Manmad`. H-01AUG-2026-P0 contains 47 candidate groups with identical observations under two spellings.
- Some sub-yards are separate markets, for example `APMC Karjat` and `Karjat(Rashin)`.

### 6.2 Strategy (ADR-0014)

1. **Markets are not pre-seeded.** Ingestion registers each new spelling as a PROVISIONAL market during the historical bootstrap. The market gets its district, an alias row with the raw spelling, and approximate coordinates equal to its district centroid.
2. **Admins curate before public launch.**
   - VERIFY each real market.
   - MERGE spelling variants into one market, only on evidence: same state and district, names that differ only in form (for example the position of "APMC"), and identical prices for the same commodity, variety, grade and date in the same delivery, or an official DMI reference. The evidence goes into the alias note and the audit.
   - SET exact coordinates only from an identified source, such as the DMI National Agriculture Market Atlas.
3. **PROVISIONAL and MERGED markets never reach farmers.** Only VERIFIED, active markets with coordinates are comparison and alert candidates.
4. **The number of Maharashtra markets is not fixed.** It grows with the feed; the official master lists 429.

## 7. Source dataset records

| `resource_id` | `title` | `dataset_type` | Basis |
|---|---|---|---|
| `9ef84268-d588-465a-a308-a864a43d0070` | `Current Daily Price of Various Commodities from Various Markets (Mandi)` | `CURRENT_PRICE` | §1; §45 trigger body; evidence §19.2 envelope `title` |
| `35985678-0d79-46b4-9ed6-6f13308a1d24` | `Variety-wise Daily Market Prices Data of Commodity` | `HISTORICAL_PRICE` | §1; ADR-0006; evidence §19.1 envelope `title` |

Each dataset also gets one `ingestion_source_settings` row with `is_paused = false` (ADR-0020).

## 8. Initial configuration

### 8.1 Transport assumptions

One active row with the §32 defaults (REQUIREMENT FROM SSOT):

| Column | Value | Basis |
|---|---|---|
| `cost_per_km` | 8.00 | §32 `ratePerKm` default ₹8.00 |
| `minimum_trip_charge` | 300.00 | §32 `minimumTripCharge` default ₹300 |
| `vehicle_capacity_kg` | 1000.00 | §32 `vehicleCapacityKg` default 1,000 kg |
| `is_active` | true | §16 "Load active transport_assumptions" |

### 8.2 Recommendation weights

One active row (ADR-0024):

| Column | Value | Basis |
|---|---|---|
| `w_net_realization` | 0.55 | §21 step 6; ADR-0024 |
| `w_price` | 0.15 | §21 step 6; ADR-0024 |
| `w_trend` | 0.10 | ADR-0024 |
| `w_freshness` | 0.10 | ADR-0024 |
| `w_transport_penalty` | 0.10 | ADR-0024 |
| `freshness_fresh_days` | 1 | §16 default |
| `freshness_recent_days` | 4 | §16 default |
| `freshness_stale_days` | 10 | §16 default |
| `is_active` | true | §16 |

Admins can change these values at runtime. Each change creates a new version and is audited (ADR-0020).

## 9. Accounts, demo data and test fixtures

These are not reference data. They are listed only to keep them out of the reference seed:

- **First admin:** created at backend start from bootstrap credentials in the deployment environment, only when no ADMIN exists. The creation is audited (ADR-0028). The demo admin credentials in §46 are never used, seeded or repeated.
- **Test accounts** (`fixtures/test_accounts.sql`, one per role, §43–§44): only in demo-profile environments, with credentials generated per environment, never committed (ADR-0028).
- **Demo price observations** (`fixtures/demo_price_observations.sql`, §43): an exact export of real ingested observations, provenance columns included, loaded only in demo-profile environments (ADR-0028).
- **Evidence captures as test fixtures:** the seven captures can be kept as raw integration-test fixtures (evidence Appendix C). They are test inputs and are never loaded into a deployed database.

## 10. Seed order

The foreign keys fix the order:

1. states → districts → district source aliases → localities;
2. commodities, with their units;
3. source datasets → ingestion source settings;
4. transport assumptions and recommendation weights.

Markets, aliases, varieties and grades come from ingestion. Every insert uses the table's natural key to skip existing rows, so the seed can be re-run.

## 11. Prerequisites

| Seed item | Prerequisite (human action, not a decision) | Status (2026-10-15) |
|---|---|---|
| District centroids | Survey of India boundary data, obtained by a registered team member | Done: V10 (section 12.2) |
| District and sub-district names and codes | LGD download for Maharashtra | Done: V10 (section 12.1) |
| Units for commodities beyond the current evidence | Official Daily Price and Arrival Report exports per commodity group | Done for the 298 commodities with rows on 13-Oct-2026: V11 (section 12.4). 307 master commodities still have no evidence |
| Localized state and commodity names | A curated list with provenance (ADR-0015) | Open, except the state's Marathi name from LGD |
| Aliases for non-district source entries | An official reference for the containing district | Open: `Bandra(E)` and `Murum` stay unmapped; neither occurred in the feed from 2025-10-14 to 2026-10-14 |

## 12. Preparation of V10 to V12 and the market merges (Task 12, 2026-10-15)

Evidence files are in `docs/data/raw/`. The large owner downloads they were derived from stay outside version control in `local-reference-data/` (ignored by Git).

### 12.1 Names and codes (LGD)

- Source: the Local Government Directory web services `stateList`, `districtList` (state 27) and `subdistrictList` (each district) at `lgdirectory.gov.in`, retrieved 2026-10-15 and saved as `LGD_Maharashtra_2026-10-15.json`.
- Maharashtra: LGD state code 27, 36 districts (LGD 466–500 and Palghar 665), 358 sub-districts. Mumbai (482) has no sub-district.
- The state's `name_mr` is the LGD local-language name. `name_hi` is not seeded (ADR-0015).

### 12.2 District centroids (Survey of India)

- Source: Survey of India Administrative Boundary Database, District Boundary, identifier `SOI/ABDB/VECTOR/50000/2025/DISTRICT/INDIA`, published 2026-05-06, 1:50,000, ORGI-harmonized for Maharashtra in 2025. Supplied by the project owner as `State_District_Subdistrict_PAN INDIA.rar` (SHA-256 `b8325e5d9dd0f04a6663d775363fe38cd2f23bd9dbae3fb7118b4e6e0ce0bcb7`; `District Boundary.shp` SHA-256 `05efac0a02c113f25cd55df3f01a4aee7201bfc9b299f73bb3e273b9aa7e3740`) with its ISO 19115 metadata (`Metadata_ABDB.zip`). The PDF map in the third download was not used.
- CRS: `LCC_WGS84`, which is EPSG:7755 (WGS 84 / India NSF LCC). Transforming with the `.prj` parameters and with EPSG:7755 differs by less than 2 × 10⁻⁷ degrees.
- Join: the `DIST_LGD` attribute of the SoI layer holds census 2011 district codes (for example 522 for Ahilyanagar), not LGD codes. Each of the 35 older districts was joined through the census 2011 code that LGD publishes for it. Palghar (created 2014, no census 2011 code; SoI code 681) was joined by name.
- Method (ADR-0014 item 2): the area centroid of the district's (multi)polygon in EPSG:7755, checked to lie inside the polygon (true for all 36, so no point-on-surface substitute was needed), transformed to WGS 84 with PROJ 9.8.1 and rounded half-up to 6 decimal places. An independent shoelace computation over the raw shapefile rings agreed within 0.07 m for every district, and every polygon area matched the layer's `Shape_Area`.
- Results: `SoI_Maharashtra_district_centroids_2026-10-15.csv`. Each district's `centroid_source` names the dataset, the polygon and the method.

### 12.3 District source aliases

The feed's Maharashtra district spellings were collected from both resources over 156 report dates (2025-10-14 to 2026-10-14; 236,368 records): 39 spellings. 31 are LGD names. Eight are mapped only because the markets reported under them name the LGD sub-districts of exactly that district:

| Source spelling | LGD district | Kind |
|---|---|---|
| `Amarawati` | 468 Amravati | spelling variant |
| `Chattrapati Sambhajinagar` | 469 Chhatrapati Sambhajinagar | spelling variant |
| `Gondiya` | 476 Gondia | spelling variant (LGD sub-district 4046 is `Gondiya`) |
| `Ahmednagar` | 466 Ahilyanagar | former name (census 2011 code 522) |
| `Dharashiv(Usmanabad)` | 488 Dharashiv | current name with the former name |
| `Jalana` | 479 Jalna | spelling variant |
| `Sholapur` | 496 Solapur | spelling variant |
| `Vashim` | 499 Washim | spelling variant |

`Sindhudurg` is added from the Agmarknet 2.0 district master (40 aliases in all). The master's `Osmanabad`, `Bandra(E)` and `Murum` never occur in the feed and stay unmapped. Every alias carries its evidence note.

### 12.4 Commodity known list and units

- Known list (ADR-0006 item 2): the Agmarknet 2.0 commodity master, 605 names in 16 groups, read from the public filter data of the Daily Price and Arrival report (`api.agmarknet.gov.in/v1/daily-price-arrival/filters`, retrieved 2026-10-15; `Agmarknet_daily-price-arrival_filters_2026-10-15.json`). These are the same counts as the master cited in ADR-0006 (605 commodities, 16 groups, 2,154 varieties, 18 grades).
- Units (ADR-0008 item 1): the project owner exported the 17 "Commodity-wise, Market-wise Daily Report (Weighted Average)" files for 13-Oct-2026, all states, all 16 groups (`Commodity-wise_Market-wise_Daily_Weighted_Avg_Report_15-10-2026_*.csv`). Every row states "Unit of Price", and no commodity shows two units:

  | Unit | Commodities |
  |---|---|
  | `Rs./Quintal` | 277 (including the 53 of V8) |
  | `Rs./Unit` | 18 (flowers such as Rose(Local) and Marigold(Calcutta); livestock such as Cow and Goat; Bunch Beans, Fig(Anjura/Anjeer)) |
  | `Rs./Bundle` | 3: Coriander(Leaves), Mint(Pudina), Leafy Vegetable |

  This confirms that the unit is not uniform across the master (ADR-0008 context). The 1,292 Maharashtra report rows with the same market, variety and grade as a data.gov.in historical record of 13-Oct-2026 have the same modal price in 1,287 cases, so the feed's prices are in the report's units.
- 307 master commodities had no row on 13-Oct-2026 and keep `price_unit` NULL. In the feed from 2025-10-14 to 2026-10-14 they cover 10 commodities and 522 records (for example Jamun(Narale Hannu) and Ox), rejected as `UNIT_UNVERIFIED`.
- 20 older commodity spellings in the historical resource (2,045 records, last seen 2026-05-04) are not master names; RD-050 recorded them, and ADR-0051 maps them (section 12.5).

### 12.5 Commodity source aliases (V12, ADR-0051)

- `commodity_source_aliases` maps the 20 legacy spellings to their master commodity. Each row's evidence note names the DMI `Commodity_Code` the spelling shares, in the historical resource, with exactly one master spelling. The mapping table is in `docs/decision-review/TASK_12_OWNER_DECISION_PACKET.md` D1.
- 12 spellings differ from the master name only in spacing, for example `Lentil (Masur)(Whole)`; `Elephant Yam (Suran)` differs in spacing and the suffix `/Amorphophallus` (code 296). 7 are renames: `Arhar(Tur/Red Gram)(Whole)` and `Arhar (Tur/Red Gram)(Whole)` (code 49), `Paddy(Dhan)(Common)` (2), `Arhar Dal(Tur Dal)` (260), `Sunflower` (14), `Jack Fruit` (182), `Cardamoms` (40). Every target has the verified unit `Rs./Quintal`.
- Ingestion matches the master name first and an alias second, both exactly. A row resolved through an alias keeps the source spelling in `price_observations.source_commodity_name`.
- Re-run of the historical backfill for 2025-10-15 to 2026-05-06, after V12, on the local database (run `d417d32f`, 2026-10-15):
  - 536 pages, 0 failed, 222,072 records fetched;
  - 8,978 inserted (all alias-resolved), 0 `UNKNOWN_COMMODITY`.

  Of the 11,337 records rejected before:
  - 8,978 are stored;
  - 276 are quarantined as same-key conflicts (ADR-0009);
  - 2,083 were identical to an observation already stored under the master spelling, because in April and May 2026 the source reported both spellings.

### 12.6 Market merges on evidence (ADR-0052 item 11)

- Legacy market spellings (for example `Akola APMC`, used until 2026-09-02) were registered as PROVISIONAL markets, separate from the VERIFIED current spellings (`APMC Akola`).
- On 2026-10-15, 242 of them were merged into their VERIFIED market through the admin API. Each merge met all of these:
  - the same district;
  - names that differ only in form (case, punctuation, "APMC", word order);
  - at least 3 observations with identical min, modal and max prices for the same commodity, variety, grade and report date in the same delivery;
  - no differing overlapping observation.
- Each merge is audited as `MARKET_MERGED` with its evidence note. 34,531 identical copies were dropped (ADR-0009), and 286 of the 354 VERIFIED markets now have history from 2025-10-15.
- 477 markets stay PROVISIONAL because the evidence is missing or contradicted:
  - 302 share no delivery with a candidate;
  - 10 share fewer than 3 observations;
  - 6 have a differing observation;
  - 16 match more than one VERIFIED market;
  - 143 match none.
