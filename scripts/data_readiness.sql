-- AgriNex Phase 1 data and ML readiness: read-only measurement (temporary tables only; nothing persistent is written).
-- Run: psql -d agrinex -f scripts/data_readiness.sql
-- Results are reported in docs/data/PHASE1_DATA_AND_ML_READINESS_REPORT.md. The ML split thresholds in section 9
-- (>= 30 observations January-July 2026, >= 5 in each of August and September) are an ASSUMPTION of the report;
-- spec section 35 names the periods but no minimum per period.
\pset footer off
\echo '== 1. Observations by dataset'
SELECT d.dataset_type, count(*) AS observations, count(DISTINCT o.report_date) AS report_dates,
       min(o.report_date) AS first_date, max(o.report_date) AS last_date
FROM price_observations o JOIN source_datasets d ON d.id = o.source_dataset_id GROUP BY 1 ORDER BY 1;

\echo '== 2. Duplicate safety: identity groups with more than one row (expect 0) and business-key uniqueness'
SELECT (SELECT count(*) FROM (SELECT 1 FROM price_observations
          GROUP BY market_id, commodity_id, variety_id, grade_id, report_date, source_dataset_id HAVING count(*) > 1) x) AS duplicate_identities,
       (SELECT count(*) - count(DISTINCT business_key) FROM price_observations) AS duplicate_business_keys;

\echo '== 3. Units: stored units, rows whose unit differs from the commodity verified unit, rows of unverified commodities'
SELECT price_unit, count(*) FROM price_observations GROUP BY 1 ORDER BY 2 DESC;
SELECT (SELECT count(*) FROM price_observations o JOIN commodities c ON c.id = o.commodity_id WHERE o.price_unit <> c.price_unit) AS unit_mismatch,
       (SELECT count(*) FROM price_observations o JOIN commodities c ON c.id = o.commodity_id WHERE c.price_unit IS NULL) AS unverified_commodity_rows,
       (SELECT count(*) FROM commodities WHERE price_unit IS NOT NULL) AS commodities_with_unit,
       (SELECT count(*) FROM commodities WHERE price_unit IS NULL) AS commodities_without_unit;

\echo '== 4. Dates: future report dates (expect 0), weekdays of historical report dates'
SELECT (SELECT count(*) FROM price_observations WHERE report_date > (now() AT TIME ZONE 'Asia/Kolkata')::date) AS future_dates,
       (SELECT count(*) FROM price_observations WHERE report_date::text !~ '^\d{4}-\d{2}-\d{2}$') AS malformed;
SELECT to_char(report_date, 'Dy') AS weekday, count(DISTINCT report_date) AS dates, count(*) AS rows
FROM price_observations GROUP BY 1, extract(isodow FROM report_date) ORDER BY extract(isodow FROM report_date);

\echo '== 5. Geography: districts, verified markets and talukas (LGD sub-districts) per district'
SELECT d.lgd_code, d.name,
       (SELECT count(*) FROM localities l WHERE l.district_id = d.id) AS talukas,
       (SELECT count(*) FROM markets m WHERE m.district_id = d.id AND m.review_status = 'VERIFIED') AS verified_markets,
       (SELECT count(*) FROM markets m WHERE m.district_id = d.id AND m.review_status = 'PROVISIONAL') AS provisional_markets,
       (SELECT count(DISTINCT o.market_id) FROM price_observations o JOIN markets m ON m.id = o.market_id
         WHERE m.district_id = d.id AND m.review_status = 'VERIFIED' AND o.report_date > current_date - 30) AS verified_reporting_30d
FROM districts d JOIN states s ON s.id = d.state_id WHERE s.lgd_code = 27 ORDER BY d.name;

\echo '== 6. Freshness of VERIFIED markets today (latest report date of any commodity; thresholds from active weights)'
WITH w AS (SELECT freshness_fresh_days f, freshness_recent_days r, freshness_stale_days s FROM recommendation_weights WHERE is_active),
latest AS (SELECT m.id, max(o.report_date) d FROM markets m LEFT JOIN price_observations o ON o.market_id = m.id
           WHERE m.review_status = 'VERIFIED' GROUP BY m.id),
t AS (SELECT (now() AT TIME ZONE 'Asia/Kolkata')::date AS today)
SELECT CASE WHEN d IS NULL THEN 'UNAVAILABLE' WHEN t.today - d <= w.f THEN 'FRESH' WHEN t.today - d <= w.r THEN 'RECENT'
            WHEN t.today - d <= w.s THEN 'STALE' ELSE 'UNAVAILABLE' END AS freshness, count(*)
FROM latest, w, t GROUP BY 1 ORDER BY 1;

\echo '== 7. Ingestion runs: status by dataset and trigger; completeness of SUCCESS runs'
SELECT d.dataset_type, r.triggered_by, r.status, count(*) FROM ingestion_runs r JOIN source_datasets d ON d.id = r.source_dataset_id
GROUP BY 1, 2, 3 ORDER BY 1, 2, 3;
SELECT d.dataset_type, count(*) AS success_runs_with_records,
       round(min(100.0 * (r.records_inserted + r.records_updated + r.duplicates_skipped) / nullif(r.records_fetched - r.records_out_of_scope, 0)), 2) AS min_completeness,
       round(avg(100.0 * (r.records_inserted + r.records_updated + r.duplicates_skipped) / nullif(r.records_fetched - r.records_out_of_scope, 0)), 2) AS avg_completeness
FROM ingestion_runs r JOIN source_datasets d ON d.id = r.source_dataset_id
WHERE r.status = 'SUCCESS' AND r.records_fetched > 0 GROUP BY 1;

\echo '== 8. Rejected or quarantined records (all runs) by code'
SELECT error_code, count(*) FROM ingestion_errors GROUP BY 1 ORDER BY 2 DESC;

\echo '== 9. ML coverage: representative daily series (view daily_price_series) per market-commodity pair'
CREATE TEMP TABLE pair AS
SELECT s.market_id, s.commodity_id, m.review_status, c.price_unit,
       count(*) AS obs, min(s.report_date) AS first_date, max(s.report_date) AS last_date,
       count(*) FILTER (WHERE s.report_date BETWEEN '2026-01-01' AND '2026-07-31') AS train_jan_jul,
       count(*) FILTER (WHERE s.report_date BETWEEN '2026-08-01' AND '2026-08-31') AS val_aug,
       count(*) FILTER (WHERE s.report_date BETWEEN '2026-09-01' AND '2026-09-30') AS test_sep
FROM daily_price_series s JOIN markets m ON m.id = s.market_id JOIN commodities c ON c.id = s.commodity_id
GROUP BY 1, 2, 3, 4;
CREATE TEMP TABLE gaps AS
SELECT market_id, commodity_id, max(gap) AS max_gap_days FROM (
  SELECT market_id, commodity_id, report_date - lag(report_date) OVER (PARTITION BY market_id, commodity_id ORDER BY report_date) AS gap
  FROM daily_price_series) g GROUP BY 1, 2;
SELECT review_status, count(*) AS pairs,
       count(*) FILTER (WHERE obs >= 90) AS pairs_ge_90,
       count(*) FILTER (WHERE obs >= 90 AND train_jan_jul >= 30 AND val_aug >= 5 AND test_sep >= 5) AS pairs_ge_90_with_spec35_split,
       count(*) FILTER (WHERE obs BETWEEN 60 AND 89) AS pairs_60_89,
       count(*) FILTER (WHERE obs < 60) AS pairs_lt_60
FROM pair GROUP BY ROLLUP (review_status) ORDER BY 1 NULLS LAST;
\echo '-- VERIFIED pairs with >= 90 observations: distribution'
SELECT percentile_disc(0.5) WITHIN GROUP (ORDER BY p.obs) AS median_obs, max(p.obs) AS max_obs,
       percentile_disc(0.5) WITHIN GROUP (ORDER BY g.max_gap_days) AS median_max_gap_days,
       percentile_disc(0.9) WITHIN GROUP (ORDER BY g.max_gap_days) AS p90_max_gap_days,
       count(*) FILTER (WHERE p.price_unit = 'Rs./Quintal') AS quintal_pairs,
       count(DISTINCT p.commodity_id) AS commodities, count(DISTINCT p.market_id) AS markets
FROM pair p JOIN gaps g USING (market_id, commodity_id) WHERE p.review_status = 'VERIFIED' AND p.obs >= 90;
\echo '-- VERIFIED pairs with >= 90 observations: top commodities'
SELECT c.canonical_name, count(*) AS pairs FROM pair p JOIN commodities c ON c.id = p.commodity_id
WHERE p.review_status = 'VERIFIED' AND p.obs >= 90 GROUP BY 1 ORDER BY 2 DESC, 1 LIMIT 15;
