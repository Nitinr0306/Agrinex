-- V6: configuration versions, recommendation results and price alerts.
-- Owner modules: transport (transport_assumptions); recommendation (recommendation_weights,
-- selling_searches, recommendation_runs, selling_options); alert (price_alerts, alert_events).
-- Design: docs/database/DATA_DICTIONARY.md and INDEXES_AND_CONSTRAINTS.md (spec section 16, 20, 32 and 34;
-- ADR-0019, ADR-0020, ADR-0023, ADR-0024, ADR-0029, ADR-0039, ADR-0040).

CREATE TABLE transport_assumptions (
    id                  uuid          NOT NULL DEFAULT gen_random_uuid(),
    cost_per_km         numeric(10,2) NOT NULL,
    minimum_trip_charge numeric(12,2) NOT NULL,
    vehicle_capacity_kg numeric(10,2) NOT NULL,
    is_active           boolean       NOT NULL DEFAULT false,
    created_at          timestamptz   NOT NULL DEFAULT now(),
    CONSTRAINT pk_transport_assumptions PRIMARY KEY (id),
    CONSTRAINT ck_transport_assumptions_values CHECK (cost_per_km >= 0 AND minimum_trip_charge >= 0 AND vehicle_capacity_kg > 0)
);

-- At most one active version, so "load active" (spec section 16) is deterministic.
CREATE UNIQUE INDEX uq_transport_assumptions_single_active ON transport_assumptions (is_active) WHERE is_active;

CREATE TABLE recommendation_weights (
    id                    uuid         NOT NULL DEFAULT gen_random_uuid(),
    w_net_realization     numeric(6,4) NOT NULL,
    w_price               numeric(6,4) NOT NULL,
    w_trend               numeric(6,4) NOT NULL,
    w_freshness           numeric(6,4) NOT NULL,
    w_transport_penalty   numeric(6,4) NOT NULL,
    freshness_fresh_days  integer      NOT NULL,
    freshness_recent_days integer      NOT NULL,
    freshness_stale_days  integer      NOT NULL,
    is_active             boolean      NOT NULL DEFAULT false,
    created_at            timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT pk_recommendation_weights PRIMARY KEY (id),
    CONSTRAINT ck_recommendation_weights_nonnegative CHECK (w_net_realization >= 0 AND w_price >= 0 AND w_trend >= 0 AND w_freshness >= 0 AND w_transport_penalty >= 0),
    CONSTRAINT ck_recommendation_weights_max CHECK (w_net_realization <= 1 AND w_price <= 1 AND w_trend <= 1 AND w_freshness <= 1 AND w_transport_penalty <= 1),
    CONSTRAINT ck_recommendation_weights_freshness_order CHECK (freshness_fresh_days >= 0 AND freshness_fresh_days <= freshness_recent_days AND freshness_recent_days <= freshness_stale_days)
);

CREATE UNIQUE INDEX uq_recommendation_weights_single_active ON recommendation_weights (is_active) WHERE is_active;

CREATE TABLE selling_searches (
    id                uuid          NOT NULL DEFAULT gen_random_uuid(),
    farmer_profile_id uuid,
    commodity_id      uuid          NOT NULL,
    variety_id        uuid,
    grade_id          uuid,
    quantity          numeric(12,3) NOT NULL,
    quantity_unit     varchar(10)   NOT NULL,
    origin_lat        numeric(9,6)  NOT NULL,
    origin_lon        numeric(9,6)  NOT NULL,
    max_distance_km   integer       NOT NULL,
    CONSTRAINT pk_selling_searches PRIMARY KEY (id),
    CONSTRAINT fk_selling_searches_farmer_profile FOREIGN KEY (farmer_profile_id) REFERENCES farmer_profiles (id) ON DELETE SET NULL,
    CONSTRAINT fk_selling_searches_commodity FOREIGN KEY (commodity_id) REFERENCES commodities (id) ON DELETE RESTRICT,
    CONSTRAINT fk_selling_searches_variety FOREIGN KEY (variety_id, commodity_id) REFERENCES varieties (id, commodity_id) ON DELETE RESTRICT,
    CONSTRAINT fk_selling_searches_grade FOREIGN KEY (grade_id, commodity_id) REFERENCES grades (id, commodity_id) ON DELETE RESTRICT,
    CONSTRAINT ck_selling_searches_quantity CHECK (quantity > 0),
    CONSTRAINT ck_selling_searches_unit CHECK (quantity_unit IN ('KG', 'QUINTAL')),
    CONSTRAINT ck_selling_searches_distance CHECK (max_distance_km > 0),
    CONSTRAINT ck_selling_searches_origin_range CHECK (origin_lat BETWEEN -90 AND 90 AND origin_lon BETWEEN -180 AND 180)
);

CREATE INDEX ix_selling_searches_farmer_profile ON selling_searches (farmer_profile_id);

CREATE TABLE recommendation_runs (
    id                          uuid        NOT NULL DEFAULT gen_random_uuid(),
    selling_search_id           uuid        NOT NULL,
    weights_id                  uuid        NOT NULL,
    transport_id                uuid        NOT NULL,
    price_only_winner_market_id uuid,
    net_winner_market_id        uuid,
    rankings_disagree           boolean     NOT NULL,
    created_at                  timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT pk_recommendation_runs PRIMARY KEY (id),
    CONSTRAINT fk_recommendation_runs_search FOREIGN KEY (selling_search_id) REFERENCES selling_searches (id) ON DELETE CASCADE,
    CONSTRAINT fk_recommendation_runs_weights FOREIGN KEY (weights_id) REFERENCES recommendation_weights (id) ON DELETE RESTRICT,
    CONSTRAINT fk_recommendation_runs_transport FOREIGN KEY (transport_id) REFERENCES transport_assumptions (id) ON DELETE RESTRICT,
    CONSTRAINT fk_recommendation_runs_price_winner FOREIGN KEY (price_only_winner_market_id) REFERENCES markets (id) ON DELETE RESTRICT,
    CONSTRAINT fk_recommendation_runs_net_winner FOREIGN KEY (net_winner_market_id) REFERENCES markets (id) ON DELETE RESTRICT,
    CONSTRAINT ck_recommendation_runs_winners_together CHECK ((price_only_winner_market_id IS NULL) = (net_winner_market_id IS NULL)),
    CONSTRAINT ck_recommendation_runs_disagree_consistent CHECK (rankings_disagree = (price_only_winner_market_id IS DISTINCT FROM net_winner_market_id))
);

CREATE TABLE selling_options (
    id                        uuid          NOT NULL DEFAULT gen_random_uuid(),
    recommendation_run_id     uuid          NOT NULL,
    market_id                 uuid          NOT NULL,
    price_observation_id      uuid          NOT NULL,
    distance_km               numeric(8,2)  NOT NULL,
    estimated_transport_cost  numeric(14,2) NOT NULL,
    estimated_gross_value     numeric(14,2) NOT NULL,
    estimated_net_realization numeric(14,2) NOT NULL,
    trend_direction           varchar(20)   NOT NULL,
    freshness_status          varchar(20)   NOT NULL,
    rank                      integer       NOT NULL,
    score                     numeric(10,6) NOT NULL,
    reason_codes              text[]        NOT NULL DEFAULT '{}',
    CONSTRAINT pk_selling_options PRIMARY KEY (id),
    CONSTRAINT fk_selling_options_run FOREIGN KEY (recommendation_run_id) REFERENCES recommendation_runs (id) ON DELETE CASCADE,
    CONSTRAINT fk_selling_options_market FOREIGN KEY (market_id) REFERENCES markets (id) ON DELETE RESTRICT,
    CONSTRAINT fk_selling_options_observation FOREIGN KEY (price_observation_id) REFERENCES price_observations (id) ON DELETE RESTRICT,
    CONSTRAINT uq_selling_options_run_market UNIQUE (recommendation_run_id, market_id),
    CONSTRAINT uq_selling_options_run_rank UNIQUE (recommendation_run_id, rank),
    CONSTRAINT ck_selling_options_rank CHECK (rank >= 1),
    CONSTRAINT ck_selling_options_amounts CHECK (distance_km >= 0 AND estimated_transport_cost >= 0 AND estimated_gross_value >= 0),
    CONSTRAINT ck_selling_options_trend CHECK (trend_direction IN ('RISING', 'STABLE', 'FALLING', 'INSUFFICIENT_DATA')),
    CONSTRAINT ck_selling_options_freshness CHECK (freshness_status IN ('FRESH', 'RECENT', 'STALE', 'UNAVAILABLE')),
    CONSTRAINT ck_selling_options_reason_codes CHECK (reason_codes <@ ARRAY['PRICE_ADVANTAGE', 'LOWER_TRANSPORT', 'POSITIVE_TREND', 'STABLE_TREND', 'RECENT_DATA', 'SUITABLE_DISTANCE', 'NEGATIVE_TREND', 'STALE_DATA']::text[])
);

CREATE TABLE price_alerts (
    id                uuid          NOT NULL DEFAULT gen_random_uuid(),
    farmer_profile_id uuid          NOT NULL,
    alert_type        varchar(30)   NOT NULL,
    commodity_id      uuid          NOT NULL,
    market_id         uuid,
    threshold_value   numeric(14,4) NOT NULL,
    status            varchar(10)   NOT NULL DEFAULT 'ACTIVE',
    last_triggered_at timestamptz,
    CONSTRAINT pk_price_alerts PRIMARY KEY (id),
    CONSTRAINT fk_price_alerts_farmer_profile FOREIGN KEY (farmer_profile_id) REFERENCES farmer_profiles (id) ON DELETE CASCADE,
    CONSTRAINT fk_price_alerts_commodity FOREIGN KEY (commodity_id) REFERENCES commodities (id) ON DELETE RESTRICT,
    CONSTRAINT fk_price_alerts_market FOREIGN KEY (market_id) REFERENCES markets (id) ON DELETE RESTRICT,
    CONSTRAINT ck_price_alerts_type CHECK (alert_type IN ('PRICE_ABOVE', 'PRICE_BELOW', 'PERCENTAGE_RISE', 'PERCENTAGE_FALL')),
    CONSTRAINT ck_price_alerts_status CHECK (status IN ('ACTIVE', 'PAUSED')),
    CONSTRAINT ck_price_alerts_threshold CHECK (threshold_value > 0)
);

CREATE INDEX ix_price_alerts_farmer_profile ON price_alerts (farmer_profile_id);
CREATE INDEX ix_price_alerts_active_commodity ON price_alerts (commodity_id) WHERE status = 'ACTIVE';

CREATE TABLE alert_events (
    id                   uuid          NOT NULL DEFAULT gen_random_uuid(),
    price_alert_id       uuid          NOT NULL,
    triggered_at         timestamptz   NOT NULL DEFAULT now(),
    trigger_value        numeric(14,4) NOT NULL,
    ingestion_run_id     uuid          NOT NULL,
    market_id            uuid          NOT NULL,
    price_observation_id uuid          NOT NULL,
    read_at              timestamptz,
    CONSTRAINT pk_alert_events PRIMARY KEY (id),
    CONSTRAINT fk_alert_events_alert FOREIGN KEY (price_alert_id) REFERENCES price_alerts (id) ON DELETE CASCADE,
    CONSTRAINT fk_alert_events_run FOREIGN KEY (ingestion_run_id) REFERENCES ingestion_runs (id) ON DELETE RESTRICT,
    CONSTRAINT fk_alert_events_market FOREIGN KEY (market_id) REFERENCES markets (id) ON DELETE RESTRICT,
    CONSTRAINT fk_alert_events_observation FOREIGN KEY (price_observation_id) REFERENCES price_observations (id) ON DELETE RESTRICT,
    CONSTRAINT uq_alert_events_alert_run UNIQUE (price_alert_id, ingestion_run_id)
);

CREATE INDEX ix_alert_events_alert_triggered ON alert_events (price_alert_id, triggered_at);
