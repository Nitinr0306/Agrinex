package com.agrinex.ingestion;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.agrinex.common.web.PageParams;
import com.agrinex.common.web.PageResponse;
import com.agrinex.price.SourceDataset;

/**
 * Public interface of the ingestion module for the admin module (admin -> ingestion, spec section 13; ADR-0013):
 * run history, errors and health, the manual trigger, and pausing or resuming scheduled runs (ADR-0011, ADR-0020).
 */
public interface IngestionApi {

	/**
	 * Starts a MANUAL run in the background and returns it as RUNNING (ADR-0011 item 3).
	 * @param fromDate first report date of a HISTORICAL_PRICE range, or null for the trailing window
	 * @param toDate last report date of a HISTORICAL_PRICE range, or null for the trailing window
	 * @throws com.agrinex.common.error.ApiException VALIDATION_FAILED for an invalid date range, CONFLICT while a
	 * run of the dataset is RUNNING
	 */
	IngestionRunView triggerRun(SourceDataset datasetType, LocalDate fromDate, LocalDate toDate);

	/** @throws com.agrinex.common.error.ApiException CONFLICT when already paused */
	DatasetScheduleView pause(SourceDataset datasetType, String reason);

	/** @throws com.agrinex.common.error.ApiException CONFLICT when not paused */
	DatasetScheduleView resume(SourceDataset datasetType);

	/** Runs newest first (startedAt descending, then id); either filter may be null. */
	PageResponse<IngestionRunView> listRuns(SourceDataset datasetType, IngestionRunStatus status, PageParams page);

	Optional<IngestionRunView> findRun(UUID runId);

	/**
	 * Errors of a run in the order recorded (createdAt ascending, then id); empty when the run does not exist.
	 */
	Optional<PageResponse<IngestionErrorView>> listErrors(UUID runId, IngestionErrorType errorType, String errorCode,
			PageParams page);

	/** One entry per dataset, CURRENT_PRICE first. */
	List<DatasetHealthView> datasetHealth();

	IngestionScheduleInfo scheduleInfo();

	/** Coverage of the HISTORICAL_PRICE report dates, with the incomplete dates newest first (ADR-0054 item 6). */
	HistoricalCoverageView historicalCoverage();

}
