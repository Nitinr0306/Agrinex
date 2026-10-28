-- V1: geography and commodity reference tables.
-- Owner modules: geography (states, districts, localities, district_source_aliases);
--                commodity (commodities, varieties, grades).
-- Design: docs/database/DATA_DICTIONARY.md and INDEXES_AND_CONSTRAINTS.md (approved 2026-10-03;
-- ADR-0006, ADR-0008, ADR-0014, ADR-0015). Text columns used for matching hold whitespace-normalized
-- values, enforced by CHECK constraints (DATABASE_ARCHITECTURE.md section 9.2).

CREATE TABLE states (
    id       uuid         NOT NULL DEFAULT gen_random_uuid(),
    name     varchar(100) NOT NULL,
    name_hi  varchar(100),
    name_mr  varchar(100),
    code     varchar(20)  NOT NULL,
    lgd_code integer      NOT NULL,
    CONSTRAINT pk_states PRIMARY KEY (id),
    CONSTRAINT uq_states_code UNIQUE (code),
    CONSTRAINT uq_states_name UNIQUE (name),
    CONSTRAINT uq_states_lgd_code UNIQUE (lgd_code),
    CONSTRAINT ck_states_name_normalized CHECK (name = btrim(regexp_replace(name, '\s+', ' ', 'g')) AND name <> ''),
    CONSTRAINT ck_states_code_format CHECK (code <> '' AND code !~ '\s'),
    CONSTRAINT ck_states_lgd_code_positive CHECK (lgd_code > 0)
);

CREATE TABLE districts (
    id              uuid         NOT NULL DEFAULT gen_random_uuid(),
    state_id        uuid         NOT NULL,
    name            varchar(150) NOT NULL,
    lgd_code        integer      NOT NULL,
    centroid_lat    numeric(9,6) NOT NULL,
    centroid_lon    numeric(9,6) NOT NULL,
    centroid_source varchar(300) NOT NULL,
    CONSTRAINT pk_districts PRIMARY KEY (id),
    CONSTRAINT fk_districts_state FOREIGN KEY (state_id) REFERENCES states (id) ON DELETE RESTRICT,
    CONSTRAINT uq_districts_state_name UNIQUE (state_id, name),
    CONSTRAINT uq_districts_lgd_code UNIQUE (lgd_code),
    CONSTRAINT ck_districts_name_normalized CHECK (name = btrim(regexp_replace(name, '\s+', ' ', 'g')) AND name <> ''),
    CONSTRAINT ck_districts_centroid_range CHECK (centroid_lat BETWEEN -90 AND 90 AND centroid_lon BETWEEN -180 AND 180),
    CONSTRAINT ck_districts_centroid_source CHECK (centroid_source <> ''),
    CONSTRAINT ck_districts_lgd_code_positive CHECK (lgd_code > 0)
);

CREATE TABLE district_source_aliases (
    id                   uuid         NOT NULL DEFAULT gen_random_uuid(),
    district_id          uuid         NOT NULL,
    source_state_name    varchar(100) NOT NULL,
    source_district_name varchar(150) NOT NULL,
    evidence_note        varchar(500) NOT NULL,
    created_at           timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT pk_district_source_aliases PRIMARY KEY (id),
    CONSTRAINT fk_district_source_aliases_district FOREIGN KEY (district_id) REFERENCES districts (id) ON DELETE RESTRICT,
    CONSTRAINT uq_district_source_aliases_source UNIQUE (source_state_name, source_district_name),
    CONSTRAINT ck_district_source_aliases_state_normalized CHECK (source_state_name = btrim(regexp_replace(source_state_name, '\s+', ' ', 'g')) AND source_state_name <> ''),
    CONSTRAINT ck_district_source_aliases_district_normalized CHECK (source_district_name = btrim(regexp_replace(source_district_name, '\s+', ' ', 'g')) AND source_district_name <> ''),
    CONSTRAINT ck_district_source_aliases_evidence CHECK (evidence_note <> '')
);

CREATE TABLE localities (
    id                uuid         NOT NULL DEFAULT gen_random_uuid(),
    district_id       uuid         NOT NULL,
    name              varchar(150) NOT NULL,
    lgd_code          integer      NOT NULL,
    lat               numeric(9,6),
    lon               numeric(9,6),
    coordinate_source varchar(300),
    CONSTRAINT pk_localities PRIMARY KEY (id),
    CONSTRAINT fk_localities_district FOREIGN KEY (district_id) REFERENCES districts (id) ON DELETE RESTRICT,
    CONSTRAINT uq_localities_district_name UNIQUE (district_id, name),
    CONSTRAINT uq_localities_lgd_code UNIQUE (lgd_code),
    CONSTRAINT ck_localities_name_normalized CHECK (name = btrim(regexp_replace(name, '\s+', ' ', 'g')) AND name <> ''),
    CONSTRAINT ck_localities_coordinates_complete CHECK ((lat IS NULL AND lon IS NULL AND coordinate_source IS NULL) OR (lat IS NOT NULL AND lon IS NOT NULL AND coordinate_source IS NOT NULL)),
    CONSTRAINT ck_localities_coordinate_range CHECK ((lat IS NULL OR lat BETWEEN -90 AND 90) AND (lon IS NULL OR lon BETWEEN -180 AND 180)),
    CONSTRAINT ck_localities_lgd_code_positive CHECK (lgd_code > 0)
);

CREATE TABLE commodities (
    id                uuid         NOT NULL DEFAULT gen_random_uuid(),
    canonical_name    varchar(200) NOT NULL,
    name_hi           varchar(200),
    name_mr           varchar(200),
    category          varchar(100),
    price_unit        varchar(50),
    price_unit_source varchar(300),
    created_at        timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT pk_commodities PRIMARY KEY (id),
    CONSTRAINT uq_commodities_canonical_name UNIQUE (canonical_name),
    CONSTRAINT ck_commodities_name_normalized CHECK (canonical_name = btrim(regexp_replace(canonical_name, '\s+', ' ', 'g')) AND canonical_name <> ''),
    CONSTRAINT ck_commodities_unit_with_source CHECK ((price_unit IS NULL AND price_unit_source IS NULL) OR (price_unit <> '' AND price_unit_source <> ''))
);

CREATE TABLE varieties (
    id             uuid         NOT NULL DEFAULT gen_random_uuid(),
    commodity_id   uuid         NOT NULL,
    canonical_name varchar(200) NOT NULL,
    created_at     timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT pk_varieties PRIMARY KEY (id),
    CONSTRAINT fk_varieties_commodity FOREIGN KEY (commodity_id) REFERENCES commodities (id) ON DELETE RESTRICT,
    CONSTRAINT uq_varieties_commodity_name UNIQUE (commodity_id, canonical_name),
    CONSTRAINT uq_varieties_id_commodity UNIQUE (id, commodity_id),
    CONSTRAINT ck_varieties_name_normalized CHECK (canonical_name = btrim(regexp_replace(canonical_name, '\s+', ' ', 'g')) AND canonical_name <> '')
);

CREATE TABLE grades (
    id             uuid         NOT NULL DEFAULT gen_random_uuid(),
    commodity_id   uuid         NOT NULL,
    canonical_name varchar(200) NOT NULL,
    created_at     timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT pk_grades PRIMARY KEY (id),
    CONSTRAINT fk_grades_commodity FOREIGN KEY (commodity_id) REFERENCES commodities (id) ON DELETE RESTRICT,
    CONSTRAINT uq_grades_commodity_name UNIQUE (commodity_id, canonical_name),
    CONSTRAINT uq_grades_id_commodity UNIQUE (id, commodity_id),
    CONSTRAINT ck_grades_name_normalized CHECK (canonical_name = btrim(regexp_replace(canonical_name, '\s+', ' ', 'g')) AND canonical_name <> '')
);
