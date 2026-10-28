-- SYNTHETIC data for the throwaway ML sandbox database only (run_sandbox.ps1; never the real agrinex database). Every name says SYNTHETIC;
-- the prices are a deterministic formula, not government data (spec P10, section 31 RULE 5; ADR-0030).
-- 2 synthetic markets x 1 synthetic commodity, 120 consecutive days ending yesterday (Asia/Kolkata).
BEGIN;
INSERT INTO states (id, name, code, lgd_code)
VALUES ('00000000-0000-0000-0000-00000000a001', 'SYNTHETIC State', 'IN-ZZ', 99001);
INSERT INTO districts (id, state_id, name, lgd_code, centroid_lat, centroid_lon, centroid_source)
VALUES ('00000000-0000-0000-0000-00000000a002', '00000000-0000-0000-0000-00000000a001', 'SYNTHETIC District', 99002,
        20.000000, 74.000000, 'SYNTHETIC');
INSERT INTO markets (id, canonical_name, canonical_slug, district_id)
VALUES ('00000000-0000-0000-0000-00000000a011', 'SYNTHETIC Market One', 'synthetic-market-one', '00000000-0000-0000-0000-00000000a002'),
       ('00000000-0000-0000-0000-00000000a012', 'SYNTHETIC Market Two', 'synthetic-market-two', '00000000-0000-0000-0000-00000000a002');
INSERT INTO commodities (id, canonical_name)
VALUES ('00000000-0000-0000-0000-00000000a021', 'SYNTHETIC Crop');
INSERT INTO source_datasets (id, resource_id, title, dataset_type)
VALUES ('00000000-0000-0000-0000-00000000a031', 'synthetic-resource', 'SYNTHETIC dataset', 'CURRENT_PRICE');
INSERT INTO ingestion_runs (id, source_dataset_id, triggered_by, status, request_scope, started_at, finished_at)
VALUES ('00000000-0000-0000-0000-00000000a041', '00000000-0000-0000-0000-00000000a031', 'MANUAL', 'SUCCESS',
        '{"synthetic": true}', now(), now());
INSERT INTO price_observations (market_id, commodity_id, report_date, modal_price, min_price, max_price, price_unit,
                                business_key, source_dataset_id, ingestion_run_id)
SELECT m.id, '00000000-0000-0000-0000-00000000a021', d::date,
       round((2000 + m.offset_rs + 150 * sin(i / 7.0) + 3 * i)::numeric, 2),
       round((1900 + m.offset_rs + 150 * sin(i / 7.0) + 3 * i)::numeric, 2),
       round((2100 + m.offset_rs + 150 * sin(i / 7.0) + 3 * i)::numeric, 2),
       'Rs./Quintal', encode(sha256(convert_to('synthetic|' || m.id || '|' || d::date, 'UTF8')), 'hex'),
       '00000000-0000-0000-0000-00000000a031', '00000000-0000-0000-0000-00000000a041'
FROM (VALUES ('00000000-0000-0000-0000-00000000a011'::uuid, 0), ('00000000-0000-0000-0000-00000000a012'::uuid, 250))
         AS m(id, offset_rs),
     LATERAL (SELECT d, row_number() OVER (ORDER BY d) AS i
              FROM generate_series((now() AT TIME ZONE 'Asia/Kolkata')::date - 120,
                                   (now() AT TIME ZONE 'Asia/Kolkata')::date - 1, interval '1 day') AS d) AS days;
COMMIT;
SELECT count(*) AS synthetic_observations, min(report_date), max(report_date) FROM price_observations;
SELECT count(*) AS series_rows FROM daily_price_series;
