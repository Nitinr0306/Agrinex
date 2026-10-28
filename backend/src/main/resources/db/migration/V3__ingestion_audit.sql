-- V3: ingestion audit tables.
-- Owner module: ingestion (ingestion_runs, ingestion_errors).
-- Design: docs/database/DATA_DICTIONARY.md and INDEXES_AND_CONSTRAINTS.md (ADR-0009, ADR-0011,
-- ADR-0012, ADR-0020, ADR-0029).

CREATE TABLE ingestion_runs (
    id                      uuid        NOT NULL DEFAULT gen_random_uuid(),
    source_dataset_id       uuid        NOT NULL,
    triggered_by            varchar(20) NOT NULL,
    status                  varchar(20) NOT NULL,
    started_at              timestamptz NOT NULL DEFAULT now(),
    finished_at             timestamptz,
    request_scope           jsonb       NOT NULL,
    source_total_records    integer,
    pages_fetched           integer     NOT NULL DEFAULT 0,
    pages_failed            integer     NOT NULL DEFAULT 0,
    records_fetched         integer     NOT NULL DEFAULT 0,
    records_inserted        integer     NOT NULL DEFAULT 0,
    records_updated         integer     NOT NULL DEFAULT 0,
    duplicates_skipped      integer     NOT NULL DEFAULT 0,
    validation_errors_count integer     NOT NULL DEFAULT 0,
    records_out_of_scope    integer     NOT NULL DEFAULT 0,
    records_conflicted      integer     NOT NULL DEFAULT 0,
    CONSTRAINT pk_ingestion_runs PRIMARY KEY (id),
    CONSTRAINT fk_ingestion_runs_source_dataset FOREIGN KEY (source_dataset_id) REFERENCES source_datasets (id) ON DELETE RESTRICT,
    CONSTRAINT ck_ingestion_runs_triggered_by CHECK (triggered_by IN ('SCHEDULER', 'MANUAL', 'STARTUP')),
    CONSTRAINT ck_ingestion_runs_status CHECK (status IN ('RUNNING', 'SUCCESS', 'PARTIAL_FAILURE', 'FAILED')),
    CONSTRAINT ck_ingestion_runs_finished_when_terminal CHECK ((status = 'RUNNING') = (finished_at IS NULL)),
    CONSTRAINT ck_ingestion_runs_time_order CHECK (finished_at IS NULL OR finished_at >= started_at),
    CONSTRAINT ck_ingestion_runs_counters_nonnegative CHECK (pages_fetched >= 0 AND pages_failed >= 0 AND records_fetched >= 0 AND records_inserted >= 0 AND records_updated >= 0 AND duplicates_skipped >= 0 AND validation_errors_count >= 0 AND records_out_of_scope >= 0 AND records_conflicted >= 0 AND (source_total_records IS NULL OR source_total_records >= 0)),
    CONSTRAINT ck_ingestion_runs_accounting CHECK (status NOT IN ('SUCCESS', 'PARTIAL_FAILURE') OR records_fetched = records_inserted + records_updated + duplicates_skipped + validation_errors_count + records_conflicted + records_out_of_scope),
    CONSTRAINT ck_ingestion_runs_scope_has_no_credentials CHECK (NOT (request_scope ?| ARRAY['api-key', 'api_key', 'apikey']))
);

-- At most one RUNNING run per dataset (ADR-0011 item 4).
CREATE UNIQUE INDEX uq_ingestion_runs_one_running ON ingestion_runs (source_dataset_id) WHERE status = 'RUNNING';
CREATE INDEX ix_ingestion_runs_dataset_started ON ingestion_runs (source_dataset_id, started_at);

CREATE TABLE ingestion_errors (
    id                  uuid        NOT NULL DEFAULT gen_random_uuid(),
    ingestion_run_id    uuid        NOT NULL,
    error_type          varchar(30) NOT NULL,
    error_code          varchar(50) NOT NULL,
    error_message       text        NOT NULL,
    raw_payload         jsonb,
    page_offset         integer,
    request_report_date date,
    created_at          timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT pk_ingestion_errors PRIMARY KEY (id),
    CONSTRAINT fk_ingestion_errors_run FOREIGN KEY (ingestion_run_id) REFERENCES ingestion_runs (id) ON DELETE CASCADE,
    CONSTRAINT ck_ingestion_errors_type CHECK (error_type IN ('VALIDATION', 'API_ERROR', 'CONFLICT', 'SCHEMA_ERROR', 'SYSTEM')),
    CONSTRAINT ck_ingestion_errors_code_format CHECK (error_code ~ '^[A-Z][A-Z0-9_]*$'),
    CONSTRAINT ck_ingestion_errors_page_offset CHECK (page_offset IS NULL OR page_offset >= 0)
);

CREATE INDEX ix_ingestion_errors_run ON ingestion_errors (ingestion_run_id);
CREATE INDEX ix_ingestion_errors_created_at ON ingestion_errors (created_at);
