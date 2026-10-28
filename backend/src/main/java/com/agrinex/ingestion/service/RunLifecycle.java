package com.agrinex.ingestion.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.agrinex.common.time.AgrinexCalendar;
import com.agrinex.ingestion.IngestionRunStatus;
import com.agrinex.ingestion.TriggeredBy;
import com.agrinex.ingestion.domain.IngestionErrorCode;
import com.agrinex.ingestion.domain.IngestionRunEntity;
import com.agrinex.ingestion.domain.RunCounters;
import com.agrinex.ingestion.repository.IngestionRunRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Starts and ends {@code ingestion_runs} rows (ADR-0011 items 4, 5 and 8). A run is started in its own
 * transaction, so the RUNNING row is committed before the run executes in the background, and the partial unique
 * index {@code uq_ingestion_runs_one_running} rejects a second RUNNING run of the dataset.
 */
@Component
public class RunLifecycle {

	/** Runs that loaded source data (contract SourceSyncStatus.lastSuccessfulSyncAt). */
	static final List<IngestionRunStatus> LOADED = List.of(IngestionRunStatus.SUCCESS,
			IngestionRunStatus.PARTIAL_FAILURE);

	private final IngestionRunRepository runs;

	private final ErrorLog errorLog;

	private final AgrinexCalendar calendar;

	RunLifecycle(IngestionRunRepository runs, ErrorLog errorLog, AgrinexCalendar calendar) {
		this.runs = runs;
		this.errorLog = errorLog;
		this.calendar = calendar;
	}

	/**
	 * Inserts the RUNNING row and commits it.
	 * @throws org.springframework.dao.DataIntegrityViolationException when a run of the dataset is RUNNING
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public UUID start(UUID sourceDatasetId, TriggeredBy triggeredBy, String requestScopeJson) {
		return this.runs.saveAndFlush(
				IngestionRunEntity.start(sourceDatasetId, triggeredBy, requestScopeJson, this.calendar.now()))
			.getId();
	}

	@Transactional(readOnly = true)
	public boolean isRunning(UUID sourceDatasetId) {
		return this.runs.existsBySourceDatasetIdAndStatus(sourceDatasetId, IngestionRunStatus.RUNNING);
	}

	@Transactional
	public void finish(UUID runId, IngestionRunStatus status, RunCounters counters) {
		IngestionRunEntity run = this.runs.findById(runId)
			.orElseThrow(() -> new IllegalStateException("The ingestion run no longer exists"));
		run.finish(status, counters, this.calendar.now());
	}

	/** Marks every RUNNING run FAILED with SYSTEM / RUN_INTERRUPTED (ADR-0011 item 5); returns how many. */
	@Transactional
	public int interruptRunning() {
		List<IngestionRunEntity> running = this.runs.findByStatus(IngestionRunStatus.RUNNING);
		Instant now = this.calendar.now();
		for (IngestionRunEntity run : running) {
			run.markInterrupted(now);
			this.errorLog.recordFailure(run.getId(), IngestionErrorCode.RUN_INTERRUPTED,
					"The run was still RUNNING when the application started", null, null, null);
		}
		return running.size();
	}

	/** Whether a run of the dataset loaded data and ended after {@code since} (ADR-0011 item 2). */
	@Transactional(readOnly = true)
	public boolean loadedSince(UUID sourceDatasetId, Instant since) {
		return this.runs.existsBySourceDatasetIdAndStatusInAndFinishedAtAfter(sourceDatasetId, LOADED, since);
	}

}
