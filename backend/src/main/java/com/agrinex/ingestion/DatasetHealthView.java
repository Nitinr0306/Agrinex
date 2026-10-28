package com.agrinex.ingestion;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.agrinex.price.SourceDatasetInfo;

/**
 * Ingestion health of one dataset (contract DatasetHealth; FR-18): its schedule state, the running run, the latest
 * completed run, the end of the last run that loaded data, the consecutive FAILED runs that drive the admin
 * banner (ADR-0019 item 7) and the newest stored report date.
 */
public record DatasetHealthView(SourceDatasetInfo dataset, DatasetScheduleView schedule, UUID runningRunId,
		IngestionRunView latestCompletedRun, Instant lastSuccessfulRunAt, long consecutiveFailures,
		LocalDate latestReportDate) {
}
