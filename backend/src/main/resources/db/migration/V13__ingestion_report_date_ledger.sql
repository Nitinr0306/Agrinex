-- V13: per-report-date ledger of historical ingestion (ADR-0054 item 1).
-- Owner module: ingestion (ingestion_report_dates).
-- One row per source dataset and report date: the last attempt, the last successful fetch and the count of
-- consecutive failed attempts. HISTORICAL_PRICE delivery units write it after each unit (ADR-0011 item 6).
-- A date's status (SETTLED, PENDING, UNSETTLED, FAILED, MISSING) is computed from it, never stored.

CREATE TABLE ingestion_report_dates (
    id                      uuid        NOT NULL DEFAULT gen_random_uuid(),
    source_dataset_id       uuid        NOT NULL,
    report_date             date        NOT NULL,
    last_attempt_run_id     uuid        NOT NULL,
    last_attempt_at         timestamptz NOT NULL,
    last_outcome            varchar(20) NOT NULL,
    last_success_run_id     uuid,
    last_success_at         timestamptz,
    success_records_fetched integer,
    consecutive_failures    integer     NOT NULL DEFAULT 0,
    CONSTRAINT pk_ingestion_report_dates PRIMARY KEY (id),
    CONSTRAINT uq_ingestion_report_dates_dataset_date UNIQUE (source_dataset_id, report_date),
    CONSTRAINT fk_ingestion_report_dates_source_dataset FOREIGN KEY (source_dataset_id) REFERENCES source_datasets (id) ON DELETE RESTRICT,
    CONSTRAINT fk_ingestion_report_dates_attempt_run FOREIGN KEY (last_attempt_run_id) REFERENCES ingestion_runs (id) ON DELETE RESTRICT,
    CONSTRAINT fk_ingestion_report_dates_success_run FOREIGN KEY (last_success_run_id) REFERENCES ingestion_runs (id) ON DELETE RESTRICT,
    CONSTRAINT ck_ingestion_report_dates_outcome CHECK (last_outcome IN ('FETCHED', 'FAILED')),
    CONSTRAINT ck_ingestion_report_dates_success_pair CHECK ((last_success_run_id IS NULL) = (last_success_at IS NULL)),
    CONSTRAINT ck_ingestion_report_dates_fetched_is_success CHECK (last_outcome <> 'FETCHED' OR (last_success_run_id = last_attempt_run_id AND last_success_at = last_attempt_at)),
    CONSTRAINT ck_ingestion_report_dates_failures CHECK (consecutive_failures >= 0 AND ((last_outcome = 'FAILED') = (consecutive_failures > 0))),
    CONSTRAINT ck_ingestion_report_dates_records CHECK (success_records_fetched IS NULL OR success_records_fetched >= 0)
);

CREATE INDEX ix_ingestion_report_dates_attempt_run ON ingestion_report_dates (last_attempt_run_id);

-- Seed from the runs recorded before this migration. Every report date in the scope of a completed HISTORICAL
-- run is an attempt at the run's start. The attempt FAILED when the run FAILED as a whole (the dates it reached
-- are unknown, so all are retried) or when a page or load failure names the date (API_ERROR, SCHEMA_ERROR,
-- SYSTEM). Otherwise it FETCHED. The record count of seeded fetches is unknown and stays NULL.
WITH attempts AS (
    SELECT r.source_dataset_id, d::date AS report_date, r.id AS run_id, r.started_at,
           CASE
               WHEN r.status = 'FAILED' THEN 'FAILED'
               WHEN EXISTS (SELECT 1 FROM ingestion_errors e
                            WHERE e.ingestion_run_id = r.id AND e.request_report_date = d::date
                              AND e.error_type IN ('API_ERROR', 'SCHEMA_ERROR', 'SYSTEM')) THEN 'FAILED'
               ELSE 'FETCHED'
           END AS outcome
    FROM ingestion_runs r
    JOIN source_datasets sd ON sd.id = r.source_dataset_id AND sd.dataset_type = 'HISTORICAL_PRICE'
    CROSS JOIN LATERAL generate_series((r.request_scope ->> 'reportDateFrom')::date,
                                       (r.request_scope ->> 'reportDateTo')::date, interval '1 day') AS d
    WHERE r.status <> 'RUNNING'
      AND r.request_scope ? 'reportDateFrom' AND r.request_scope ? 'reportDateTo'
),
last_attempt AS (
    SELECT DISTINCT ON (source_dataset_id, report_date) source_dataset_id, report_date, run_id, started_at, outcome
    FROM attempts
    ORDER BY source_dataset_id, report_date, started_at DESC, run_id
),
last_success AS (
    SELECT DISTINCT ON (source_dataset_id, report_date) source_dataset_id, report_date, run_id, started_at
    FROM attempts
    WHERE outcome = 'FETCHED'
    ORDER BY source_dataset_id, report_date, started_at DESC, run_id
),
failures AS (
    SELECT a.source_dataset_id, a.report_date, count(*) AS failed_attempts
    FROM attempts a
    LEFT JOIN last_success s ON s.source_dataset_id = a.source_dataset_id AND s.report_date = a.report_date
    WHERE a.outcome = 'FAILED' AND (s.started_at IS NULL OR a.started_at > s.started_at)
    GROUP BY a.source_dataset_id, a.report_date
)
INSERT INTO ingestion_report_dates (source_dataset_id, report_date, last_attempt_run_id, last_attempt_at, last_outcome,
                                    last_success_run_id, last_success_at, success_records_fetched, consecutive_failures)
SELECT la.source_dataset_id, la.report_date, la.run_id, la.started_at, la.outcome,
       ls.run_id, ls.started_at, NULL,
       CASE WHEN la.outcome = 'FAILED' THEN f.failed_attempts ELSE 0 END
FROM last_attempt la
LEFT JOIN last_success ls ON ls.source_dataset_id = la.source_dataset_id AND ls.report_date = la.report_date
LEFT JOIN failures f ON f.source_dataset_id = la.source_dataset_id AND f.report_date = la.report_date;
