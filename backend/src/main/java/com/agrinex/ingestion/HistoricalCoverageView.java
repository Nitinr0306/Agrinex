package com.agrinex.ingestion;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Coverage of the HISTORICAL_PRICE report dates (ADR-0054 item 6): the status counts over the covered dates, the
 * incomplete dates newest first, the progress of a running run and the next maintenance runs.
 *
 * @param generatedAt when the view was computed
 * @param coverageFrom the oldest covered report date
 * @param coverageTo the newest covered report date (today, Asia/Kolkata)
 * @param settleAfterDays the settle age in days
 * @param scheduledRunsPaused whether scheduled runs of the dataset, the maintenance runs included, are paused
 * @param counts the number of covered dates in each status
 * @param incompleteDates dates in status UNSETTLED, FAILED or MISSING, newest first
 * @param runningRun the running HISTORICAL_PRICE run, or null
 * @param nextGapCheckAt the next daily gap check
 * @param nextReconciliationAt the next weekly reconciliation
 * @param maxDatesPerGapRun the most report dates one gap-backfill run spans
 * @param reconciliationWindowDays report dates re-fetched by a reconciliation
 */
public record HistoricalCoverageView(Instant generatedAt, LocalDate coverageFrom, LocalDate coverageTo,
		int settleAfterDays, boolean scheduledRunsPaused, StatusCounts counts, List<DateEntry> incompleteDates,
		RunProgress runningRun, Instant nextGapCheckAt, Instant nextReconciliationAt, int maxDatesPerGapRun,
		int reconciliationWindowDays) {

	/** Covered dates per status; they add up to the number of covered dates. */
	public record StatusCounts(int settled, int pending, int unsettled, int failed, int missing) {
	}

	/** One report date with its ledger values; the times and count are null when never attempted or fetched. */
	public record DateEntry(LocalDate reportDate, ReportDateStatus status, Instant lastAttemptAt,
			Instant lastSuccessAt, Integer recordsFetched, int consecutiveFailures) {
	}

	/**
	 * Progress of a RUNNING run: the report dates attempted so far out of those in its scope.
	 * @param purpose the run's purpose, or null for a run started before purposes were recorded
	 */
	public record RunProgress(UUID runId, RunPurpose purpose, LocalDate reportDateFrom, LocalDate reportDateTo,
			int datesInScope, long datesAttempted) {
	}

}
