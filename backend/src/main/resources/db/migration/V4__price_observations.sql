-- V4: government price observations and their revisions.
-- Owner module: price (price_observations, price_observation_revisions).
-- Design: docs/database/DATA_DICTIONARY.md and INDEXES_AND_CONSTRAINTS.md (spec section 27 and section 31
-- RULE 6; ADR-0008, ADR-0009). Prices are exact decimals in the unit stored on each row; nothing is
-- converted. business_key is the lowercase hex SHA-256 of the resolved identity
-- (DATABASE_ARCHITECTURE.md section 10.3); the identity is also enforced on the resolved columns with
-- UNIQUE NULLS NOT DISTINCT.

CREATE TABLE price_observations (
    id                uuid          NOT NULL DEFAULT gen_random_uuid(),
    market_id         uuid          NOT NULL,
    commodity_id      uuid          NOT NULL,
    variety_id        uuid,
    grade_id          uuid,
    report_date       date          NOT NULL,
    modal_price       numeric(14,2) NOT NULL,
    min_price         numeric(14,2),
    max_price         numeric(14,2),
    price_unit        varchar(50)   NOT NULL,
    business_key      varchar(64)   NOT NULL,
    source_dataset_id uuid          NOT NULL,
    ingestion_run_id  uuid          NOT NULL,
    ingested_at       timestamptz   NOT NULL DEFAULT now(),
    CONSTRAINT pk_price_observations PRIMARY KEY (id),
    CONSTRAINT fk_price_observations_market FOREIGN KEY (market_id) REFERENCES markets (id) ON DELETE RESTRICT,
    CONSTRAINT fk_price_observations_commodity FOREIGN KEY (commodity_id) REFERENCES commodities (id) ON DELETE RESTRICT,
    CONSTRAINT fk_price_observations_variety FOREIGN KEY (variety_id, commodity_id) REFERENCES varieties (id, commodity_id) ON DELETE RESTRICT,
    CONSTRAINT fk_price_observations_grade FOREIGN KEY (grade_id, commodity_id) REFERENCES grades (id, commodity_id) ON DELETE RESTRICT,
    CONSTRAINT fk_price_observations_source_dataset FOREIGN KEY (source_dataset_id) REFERENCES source_datasets (id) ON DELETE RESTRICT,
    CONSTRAINT fk_price_observations_ingestion_run FOREIGN KEY (ingestion_run_id) REFERENCES ingestion_runs (id) ON DELETE RESTRICT,
    CONSTRAINT uq_price_observations_business_key UNIQUE (business_key),
    CONSTRAINT uq_price_observations_identity UNIQUE NULLS NOT DISTINCT (market_id, commodity_id, report_date, variety_id, grade_id, source_dataset_id),
    CONSTRAINT ck_price_observations_modal_positive CHECK (modal_price > 0),
    CONSTRAINT ck_price_observations_min_positive CHECK (min_price IS NULL OR min_price > 0),
    CONSTRAINT ck_price_observations_max_positive CHECK (max_price IS NULL OR max_price > 0),
    CONSTRAINT ck_price_observations_min_le_modal CHECK (min_price IS NULL OR min_price <= modal_price),
    CONSTRAINT ck_price_observations_modal_le_max CHECK (max_price IS NULL OR modal_price <= max_price),
    CONSTRAINT ck_price_observations_business_key_format CHECK (business_key ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_price_observations_price_unit_present CHECK (price_unit <> ''),
    CONSTRAINT ck_price_observations_quintal_floor CHECK (price_unit <> 'Rs./Quintal' OR modal_price >= 100)
);

CREATE INDEX ix_price_observations_ingestion_run_id ON price_observations (ingestion_run_id);

CREATE TABLE price_observation_revisions (
    id                   uuid          NOT NULL DEFAULT gen_random_uuid(),
    price_observation_id uuid          NOT NULL,
    ingestion_run_id     uuid          NOT NULL,
    previous_modal_price numeric(14,2) NOT NULL,
    previous_min_price   numeric(14,2),
    previous_max_price   numeric(14,2),
    previous_ingested_at timestamptz   NOT NULL,
    revised_at           timestamptz   NOT NULL DEFAULT now(),
    CONSTRAINT pk_price_observation_revisions PRIMARY KEY (id),
    CONSTRAINT fk_price_observation_revisions_observation FOREIGN KEY (price_observation_id) REFERENCES price_observations (id) ON DELETE RESTRICT,
    CONSTRAINT fk_price_observation_revisions_run FOREIGN KEY (ingestion_run_id) REFERENCES ingestion_runs (id) ON DELETE RESTRICT,
    CONSTRAINT uq_price_observation_revisions_observation_run UNIQUE (price_observation_id, ingestion_run_id),
    CONSTRAINT ck_price_observation_revisions_prices CHECK (previous_modal_price > 0 AND (previous_min_price IS NULL OR (previous_min_price > 0 AND previous_min_price <= previous_modal_price)) AND (previous_max_price IS NULL OR previous_modal_price <= previous_max_price)),
    CONSTRAINT ck_price_observation_revisions_time_order CHECK (revised_at >= previous_ingested_at)
);

CREATE INDEX ix_price_observation_revisions_run ON price_observation_revisions (ingestion_run_id);
