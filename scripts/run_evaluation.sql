-- AgriNex Experiment 1: price-only ranking vs net-realization ranking (spec section 36; ADR-0045 item 8).
--
-- Research question: does including transport cost change which market AgriNex recommends, and by how much?
-- Method (spec section 36):
--   1. Scenarios are the recorded comparisons (recommendation_runs) in which at least 2 markets were eligible,
--      that is, runs with at least 2 selling_options. What-If simulations are never recorded (ADR-0026).
--   2. Each scenario has price_only_winner_market_id (highest reported price) and net_winner_market_id
--      (the recommended, rank-1 market by composite score; ADR-0023 item 2, ADR-0045).
--   3. rankingDisagreementRate = scenarios whose winners differ / scenarios x 100.
--   4. For each disagreement, the estimated net difference = estimated net realization of the recommended (rank-1)
--      market - estimated net realization of the highest-price market. Negative values are reported, never
--      dropped (ADR-0045 item 8).
--   5. Average, median and 90th percentile of that difference.
--
-- ASSUMPTION (statistic method, not set by the spec): the median and the 90th percentile use PostgreSQL's
-- percentile_cont, the continuous percentile with linear interpolation between the two nearest values.
--
-- Honest framing (spec section 36): "These are estimated outcomes based on our configured transport assumptions,
-- not actual transaction data."
--
-- Which comparisons count: by default all recorded comparisons. Set the two bounds in the params CTE below to
-- limit the experiment to a period of real use. A local or test database holds only test comparisons; its figures
-- are not evidence for the experiment (ADR-0052 item 9).
--
-- Run: psql -d agrinex -f scripts/run_evaluation.sql
-- The statement only reads. It is one SELECT, so it also runs unchanged from a JDBC client (backend test
-- EvaluationScriptIntegrationTest).
WITH params AS (
    SELECT CAST(NULL AS timestamptz) AS from_ts,  -- inclusive lower bound on recommendation_runs.created_at; NULL: none
           CAST(NULL AS timestamptz) AS to_ts     -- exclusive upper bound; NULL: none
),
scenarios AS (
    SELECT r.id, r.price_only_winner_market_id, r.net_winner_market_id, r.created_at
    FROM recommendation_runs r
    CROSS JOIN params p
    WHERE (p.from_ts IS NULL OR r.created_at >= p.from_ts)
      AND (p.to_ts IS NULL OR r.created_at < p.to_ts)
      AND (SELECT count(*) FROM selling_options o WHERE o.recommendation_run_id = r.id) >= 2
),
disagreements AS (
    SELECT s.id,
           recommended.estimated_net_realization - highest_price.estimated_net_realization AS estimated_net_difference
    FROM scenarios s
    JOIN selling_options recommended
      ON recommended.recommendation_run_id = s.id AND recommended.market_id = s.net_winner_market_id
    JOIN selling_options highest_price
      ON highest_price.recommendation_run_id = s.id AND highest_price.market_id = s.price_only_winner_market_id
    WHERE s.price_only_winner_market_id <> s.net_winner_market_id
)
SELECT
    (SELECT count(*) FROM scenarios) AS scenarios,
    (SELECT count(*) FROM disagreements) AS disagreements,
    round(100.0 * (SELECT count(*) FROM disagreements) / nullif((SELECT count(*) FROM scenarios), 0), 2)
        AS ranking_disagreement_rate_pct,
    round((SELECT avg(estimated_net_difference) FROM disagreements), 2) AS average_estimated_net_difference,
    round(CAST((SELECT percentile_cont(0.5) WITHIN GROUP (ORDER BY estimated_net_difference) FROM disagreements)
        AS numeric), 2) AS median_estimated_net_difference,
    round(CAST((SELECT percentile_cont(0.9) WITHIN GROUP (ORDER BY estimated_net_difference) FROM disagreements)
        AS numeric), 2) AS percentile90_estimated_net_difference,
    (SELECT count(*) FROM disagreements WHERE estimated_net_difference < 0) AS disagreements_with_negative_difference,
    (SELECT min(created_at) FROM scenarios) AS first_scenario_at,
    (SELECT max(created_at) FROM scenarios) AS last_scenario_at;
