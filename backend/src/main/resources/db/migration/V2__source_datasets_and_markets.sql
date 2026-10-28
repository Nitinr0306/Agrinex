-- V2: government source datasets, ingestion settings, markets and market source aliases.
-- Owner modules: price (source_datasets, markets, market_source_aliases);
--                ingestion (ingestion_source_settings).
-- Design: docs/database/DATA_DICTIONARY.md and INDEXES_AND_CONSTRAINTS.md (ADR-0006, ADR-0013,
-- ADR-0014, ADR-0020).

CREATE TABLE source_datasets (
    id           uuid         NOT NULL DEFAULT gen_random_uuid(),
    resource_id  varchar(64)  NOT NULL,
    title        varchar(300) NOT NULL,
    dataset_type varchar(30)  NOT NULL,
    CONSTRAINT pk_source_datasets PRIMARY KEY (id),
    CONSTRAINT uq_source_datasets_resource_id UNIQUE (resource_id),
    CONSTRAINT uq_source_datasets_dataset_type UNIQUE (dataset_type),
    CONSTRAINT ck_source_datasets_resource_id_format CHECK (resource_id <> '' AND resource_id !~ '\s'),
    CONSTRAINT ck_source_datasets_type CHECK (dataset_type IN ('CURRENT_PRICE', 'HISTORICAL_PRICE'))
);

CREATE TABLE ingestion_source_settings (
    id                uuid         NOT NULL DEFAULT gen_random_uuid(),
    source_dataset_id uuid         NOT NULL,
    is_paused         boolean      NOT NULL DEFAULT false,
    paused_reason     varchar(500),
    updated_at        timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT pk_ingestion_source_settings PRIMARY KEY (id),
    CONSTRAINT fk_ingestion_source_settings_dataset FOREIGN KEY (source_dataset_id) REFERENCES source_datasets (id) ON DELETE RESTRICT,
    CONSTRAINT uq_ingestion_source_settings_dataset UNIQUE (source_dataset_id),
    CONSTRAINT ck_ingestion_source_settings_reason CHECK (is_paused = (paused_reason IS NOT NULL AND paused_reason <> ''))
);

CREATE TABLE markets (
    id                        uuid         NOT NULL DEFAULT gen_random_uuid(),
    canonical_name            varchar(200) NOT NULL,
    canonical_slug            varchar(220) NOT NULL,
    district_id               uuid         NOT NULL,
    lat                       numeric(9,6),
    lon                       numeric(9,6),
    is_coordinate_approximate boolean      NOT NULL DEFAULT false,
    coordinate_source         varchar(300),
    is_active                 boolean      NOT NULL DEFAULT true,
    review_status             varchar(12)  NOT NULL DEFAULT 'PROVISIONAL',
    merged_into_market_id     uuid,
    created_at                timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT pk_markets PRIMARY KEY (id),
    CONSTRAINT fk_markets_district FOREIGN KEY (district_id) REFERENCES districts (id) ON DELETE RESTRICT,
    CONSTRAINT fk_markets_merged_into FOREIGN KEY (merged_into_market_id) REFERENCES markets (id) ON DELETE RESTRICT,
    CONSTRAINT uq_markets_canonical_slug UNIQUE (canonical_slug),
    CONSTRAINT ck_markets_name_normalized CHECK (canonical_name = btrim(regexp_replace(canonical_name, '\s+', ' ', 'g')) AND canonical_name <> ''),
    CONSTRAINT ck_markets_slug_format CHECK (canonical_slug <> '' AND canonical_slug !~ '\s'),
    CONSTRAINT ck_markets_coordinates_complete CHECK ((lat IS NULL AND lon IS NULL AND coordinate_source IS NULL) OR (lat IS NOT NULL AND lon IS NOT NULL AND coordinate_source IS NOT NULL)),
    CONSTRAINT ck_markets_coordinate_range CHECK ((lat IS NULL OR lat BETWEEN -90 AND 90) AND (lon IS NULL OR lon BETWEEN -180 AND 180)),
    CONSTRAINT ck_markets_approximate_needs_coordinates CHECK (NOT is_coordinate_approximate OR lat IS NOT NULL),
    CONSTRAINT ck_markets_review_status CHECK (review_status IN ('PROVISIONAL', 'VERIFIED', 'MERGED')),
    CONSTRAINT ck_markets_merge_target CHECK ((review_status = 'MERGED') = (merged_into_market_id IS NOT NULL) AND merged_into_market_id IS DISTINCT FROM id),
    CONSTRAINT ck_markets_merged_inactive CHECK (review_status <> 'MERGED' OR NOT is_active)
);

CREATE INDEX ix_markets_district_id ON markets (district_id);

CREATE TABLE market_source_aliases (
    id                     uuid         NOT NULL DEFAULT gen_random_uuid(),
    market_id              uuid         NOT NULL,
    source_state_name      varchar(100) NOT NULL,
    source_district_name   varchar(150) NOT NULL,
    source_market_name     varchar(200) NOT NULL,
    source_market_name_raw varchar(200) NOT NULL,
    evidence_note          varchar(500) NOT NULL,
    created_at             timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT pk_market_source_aliases PRIMARY KEY (id),
    CONSTRAINT fk_market_source_aliases_market FOREIGN KEY (market_id) REFERENCES markets (id) ON DELETE RESTRICT,
    CONSTRAINT uq_market_source_aliases_source UNIQUE (source_state_name, source_district_name, source_market_name),
    CONSTRAINT ck_market_source_aliases_state_normalized CHECK (source_state_name = btrim(regexp_replace(source_state_name, '\s+', ' ', 'g')) AND source_state_name <> ''),
    CONSTRAINT ck_market_source_aliases_district_normalized CHECK (source_district_name = btrim(regexp_replace(source_district_name, '\s+', ' ', 'g')) AND source_district_name <> ''),
    CONSTRAINT ck_market_source_aliases_market_normalized CHECK (source_market_name = btrim(regexp_replace(source_market_name_raw, '\s+', ' ', 'g')) AND source_market_name <> ''),
    CONSTRAINT ck_market_source_aliases_evidence CHECK (evidence_note <> '')
);
