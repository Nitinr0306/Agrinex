package com.agrinex.ingestion.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * The ledger row of one report date of a dataset (table {@code ingestion_report_dates}; ADR-0054 item 1): the last
 * attempt, the last successful fetch and the count of consecutive failed attempts since it.
 */
@Entity
@Table(name = "ingestion_report_dates")
public class IngestionReportDateEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "source_dataset_id")
	private UUID sourceDatasetId;

	@Column(name = "report_date")
	private LocalDate reportDate;

	@Column(name = "last_attempt_run_id")
	private UUID lastAttemptRunId;

	@Column(name = "last_attempt_at")
	private Instant lastAttemptAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "last_outcome")
	private ReportDateOutcome lastOutcome;

	@Column(name = "last_success_run_id")
	private UUID lastSuccessRunId;

	@Column(name = "last_success_at")
	private Instant lastSuccessAt;

	@Column(name = "success_records_fetched")
	private Integer successRecordsFetched;

	@Column(name = "consecutive_failures")
	private int consecutiveFailures;

	protected IngestionReportDateEntity() {
	}

	public static IngestionReportDateEntity of(UUID sourceDatasetId, LocalDate reportDate) {
		IngestionReportDateEntity entry = new IngestionReportDateEntity();
		entry.sourceDatasetId = sourceDatasetId;
		entry.reportDate = reportDate;
		return entry;
	}

	/** Every page of the date was fetched and its unit loaded. */
	public void fetched(UUID runId, Instant at, int recordsFetched) {
		this.lastAttemptRunId = runId;
		this.lastAttemptAt = at;
		this.lastOutcome = ReportDateOutcome.FETCHED;
		this.lastSuccessRunId = runId;
		this.lastSuccessAt = at;
		this.successRecordsFetched = recordsFetched;
		this.consecutiveFailures = 0;
	}

	/** A page or the load of the date failed; the last successful fetch is kept. */
	public void failed(UUID runId, Instant at) {
		this.lastAttemptRunId = runId;
		this.lastAttemptAt = at;
		this.lastOutcome = ReportDateOutcome.FAILED;
		this.consecutiveFailures++;
	}

	public UUID getId() {
		return this.id;
	}

	public UUID getSourceDatasetId() {
		return this.sourceDatasetId;
	}

	public LocalDate getReportDate() {
		return this.reportDate;
	}

	public UUID getLastAttemptRunId() {
		return this.lastAttemptRunId;
	}

	public Instant getLastAttemptAt() {
		return this.lastAttemptAt;
	}

	public ReportDateOutcome getLastOutcome() {
		return this.lastOutcome;
	}

	public UUID getLastSuccessRunId() {
		return this.lastSuccessRunId;
	}

	public Instant getLastSuccessAt() {
		return this.lastSuccessAt;
	}

	public Integer getSuccessRecordsFetched() {
		return this.successRecordsFetched;
	}

	public int getConsecutiveFailures() {
		return this.consecutiveFailures;
	}

}
