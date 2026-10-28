-- V9: database boundary of the optional ML track (spec section 35; ADR-0030).
-- Owner module: price (forecasts, daily_price_series).
-- Design: docs/database/DATA_DICTIONARY.md (forecasts), INDEXES_AND_CONSTRAINTS.md (forecasts),
-- docs/api/ML_CONTRACT_BOUNDARY.md (DailyPriceSeries, ForecastOutput), ADR-0010 rule 7, ADR-0029, ADR-0030.
-- The backend never calls the ML job; the job reads the view and writes forecasts through the restricted
-- role below, and core flows never read forecasts. Nothing here runs the ML track: it starts only when the
-- HARD RULE 2 gate and the spec section 35 criteria are met (ML_CONTRACT_BOUNDARY.md section 8).

CREATE TABLE forecasts (
    id                    uuid          NOT NULL DEFAULT gen_random_uuid(),
    market_id             uuid          NOT NULL,
    commodity_id          uuid          NOT NULL,
    forecast_horizon_days integer       NOT NULL,
    forecast_date         date          NOT NULL,
    predicted_price       numeric(14,2) NOT NULL,
    lower_bound           numeric(14,2),
    upper_bound           numeric(14,2),
    model_name            varchar(100)  NOT NULL,
    model_version         varchar(50)   NOT NULL,
    mae                   numeric(14,4),
    rmse                  numeric(14,4),
    training_data_through date          NOT NULL,
    generated_at          timestamptz   NOT NULL,
    CONSTRAINT pk_forecasts PRIMARY KEY (id),
    CONSTRAINT fk_forecasts_market FOREIGN KEY (market_id) REFERENCES markets (id) ON DELETE RESTRICT,
    CONSTRAINT fk_forecasts_commodity FOREIGN KEY (commodity_id) REFERENCES commodities (id) ON DELETE RESTRICT,
    CONSTRAINT uq_forecasts_point UNIQUE (market_id, commodity_id, forecast_horizon_days, generated_at, forecast_date),
    CONSTRAINT ck_forecasts_horizon CHECK (forecast_horizon_days IN (3, 7)),
    CONSTRAINT ck_forecasts_after_training CHECK (forecast_date > training_data_through),
    CONSTRAINT ck_forecasts_bounds CHECK ((lower_bound IS NULL OR lower_bound <= predicted_price) AND (upper_bound IS NULL OR predicted_price <= upper_bound)),
    CONSTRAINT ck_forecasts_errors_nonnegative CHECK ((mae IS NULL OR mae >= 0) AND (rmse IS NULL OR rmse >= 0))
);

-- The input contract DailyPriceSeries (ML_CONTRACT_BOUNDARY.md section 3): one point per market, commodity and
-- report date, chosen by ADR-0010 rules 1 and 4 exactly as the price module chooses it:
--   1. for the same identity (market, commodity, variety, grade, date) the CURRENT_PRICE row takes precedence;
--   2. among the remaining candidates of the date, sorted by modal price, then variety name and grade name in
--      code-point order with NULL first, the lower median (position floor((n-1)/2), zero-based) is taken.
-- It is never an average and never interpolated; dates without data are absent.
CREATE VIEW daily_price_series AS
WITH by_dataset AS (
    SELECT po.id, po.market_id, po.commodity_id, po.variety_id, po.grade_id, po.report_date, po.modal_price,
           po.min_price, po.max_price, po.price_unit, sd.dataset_type,
           row_number() OVER (
               PARTITION BY po.market_id, po.commodity_id, po.variety_id, po.grade_id, po.report_date
               ORDER BY CASE sd.dataset_type WHEN 'CURRENT_PRICE' THEN 0 ELSE 1 END) AS dataset_rank
    FROM price_observations po
    JOIN source_datasets sd ON sd.id = po.source_dataset_id
),
candidates AS (
    SELECT b.*, v.canonical_name AS variety_name, g.canonical_name AS grade_name
    FROM by_dataset b
    LEFT JOIN varieties v ON v.id = b.variety_id
    LEFT JOIN grades g ON g.id = b.grade_id
    WHERE b.dataset_rank = 1
),
ordered AS (
    SELECT c.*,
           row_number() OVER (
               PARTITION BY c.market_id, c.commodity_id, c.report_date
               ORDER BY c.modal_price, c.variety_name COLLATE "C" NULLS FIRST, c.grade_name COLLATE "C" NULLS FIRST)
               AS position,
           count(*) OVER (PARTITION BY c.market_id, c.commodity_id, c.report_date) AS candidate_count
    FROM candidates c
)
SELECT market_id, commodity_id, report_date, modal_price, min_price, max_price, price_unit,
       id AS observation_id, variety_id, grade_id, dataset_type AS source_dataset
FROM ordered
WHERE position = (candidate_count - 1) / 2 + 1;

-- The ML job's restricted role (ADR-0030 item 1): SELECT on the view, INSERT and DELETE on forecasts, nothing
-- else. It is created without login; the operator enables login with a password from the deployment
-- environment only when the ML track starts. No credential is ever stored in a migration.
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'agrinex_ml') THEN
        CREATE ROLE agrinex_ml NOLOGIN;
    END IF;
END
$$;

GRANT USAGE ON SCHEMA public TO agrinex_ml;
GRANT SELECT ON daily_price_series TO agrinex_ml;
GRANT INSERT, DELETE ON forecasts TO agrinex_ml;
