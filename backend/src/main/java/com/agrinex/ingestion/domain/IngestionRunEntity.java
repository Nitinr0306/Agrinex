package com.agrinex.ingestion.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.agrinex.ingestion.IngestionRunStatus;
import com.agrinex.ingestion.TriggeredBy;

/**
 * One ingestion run with its audit counters (table {@code ingestion_runs}; spec section 15, 20; ADR-0011,
 * ADR-0012). {@code request_scope} holds the request parameters as JSON text and never credentials (ADR-0020
 * item 5).
 */
@Entity
@Table(name = "ingestion_runs")
public class IngestionRunEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "source_dataset_id")
	private UUID sourceDatasetId;

	@Enumerated(EnumType.STRING)
	@Column(name = "triggered_by")
	private TriggeredBy triggeredBy;

	@Enumerated(EnumType.STRING)
	private IngestionRunStatus status;

	@Column(name = "started_at")
	private Instant startedAt;

	@Column(name = "finished_at")
	private Instant finishedAt;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "request_scope")
	private String requestScope;

	@Column(name = "source_total_records")
	private Integer sourceTotalRecords;

	@Column(name = "pages_fetched")
	private int pagesFetched;

	@Column(name = "pages_failed")
	private int pagesFailed;

	@Column(name = "records_fetched")
	private int recordsFetched;

	@Column(name = "records_inserted")
	private int recordsInserted;

	@Column(name = "records_updated")
	private int recordsUpdated;

	@Column(name = "duplicates_skipped")
	private int duplicatesSkipped;

	@Column(name = "validation_errors_count")
	private int validationErrorsCount;

	@Column(name = "records_out_of_scope")
	private int recordsOutOfScope;

	@Column(name = "records_conflicted")
	private int recordsConflicted;

	protected IngestionRunEntity() {
	}

	public static IngestionRunEntity start(UUID sourceDatasetId, TriggeredBy triggeredBy, String requestScope,
			Instant startedAt) {
		IngestionRunEntity run = new IngestionRunEntity();
		run.sourceDatasetId = sourceDatasetId;
		run.triggeredBy = triggeredBy;
		run.status = IngestionRunStatus.RUNNING;
		run.requestScope = requestScope;
		run.startedAt = startedAt;
		return run;
	}

	/** Ends the run with its final status and counters (ADR-0011 item 8). */
	public void finish(IngestionRunStatus finalStatus, RunCounters counters, Instant finishedAt) {
		if (finalStatus == IngestionRunStatus.RUNNING) {
			throw new IllegalArgumentException("A run cannot finish as RUNNING");
		}
		this.status = finalStatus;
		this.finishedAt = finishedAt;
		this.sourceTotalRecords = counters.sourceTotalRecords();
		this.pagesFetched = counters.pagesFetched();
		this.pagesFailed = counters.pagesFailed();
		this.recordsFetched = counters.recordsFetched();
		this.recordsInserted = counters.recordsInserted();
		this.recordsUpdated = counters.recordsUpdated();
		this.duplicatesSkipped = counters.duplicatesSkipped();
		this.validationErrorsCount = counters.validationErrors();
		this.recordsConflicted = counters.recordsConflicted();
		this.recordsOutOfScope = counters.recordsOutOfScope();
	}

	/** A run found RUNNING at start-up is FAILED (ADR-0011 item 5); its counters stay as last written. */
	public void markInterrupted(Instant finishedAt) {
		this.status = IngestionRunStatus.FAILED;
		this.finishedAt = finishedAt.isBefore(this.startedAt) ? this.startedAt : finishedAt;
	}

	public UUID getId() {
		return this.id;
	}

	public UUID getSourceDatasetId() {
		return this.sourceDatasetId;
	}

	public TriggeredBy getTriggeredBy() {
		return this.triggeredBy;
	}

	public IngestionRunStatus getStatus() {
		return this.status;
	}

	public Instant getStartedAt() {
		return this.startedAt;
	}

	public Instant getFinishedAt() {
		return this.finishedAt;
	}

	public String getRequestScope() {
		return this.requestScope;
	}

	public Integer getSourceTotalRecords() {
		return this.sourceTotalRecords;
	}

	public int getPagesFetched() {
		return this.pagesFetched;
	}

	public int getPagesFailed() {
		return this.pagesFailed;
	}

	public int getRecordsFetched() {
		return this.recordsFetched;
	}

	public int getRecordsInserted() {
		return this.recordsInserted;
	}

	public int getRecordsUpdated() {
		return this.recordsUpdated;
	}

	public int getDuplicatesSkipped() {
		return this.duplicatesSkipped;
	}

	public int getValidationErrorsCount() {
		return this.validationErrorsCount;
	}

	public int getRecordsOutOfScope() {
		return this.recordsOutOfScope;
	}

	public int getRecordsConflicted() {
		return this.recordsConflicted;
	}

}
