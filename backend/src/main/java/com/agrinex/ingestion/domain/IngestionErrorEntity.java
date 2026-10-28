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

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.agrinex.ingestion.IngestionErrorType;

/**
 * One rejected record, conflict variant or failure of a run (table {@code ingestion_errors}; spec section 15,
 * 37; ADR-0008, ADR-0009, ADR-0011). The message and payload are redacted before they are built: they never hold
 * the API key (ADR-0020 item 5). Kept 180 days (ADR-0029).
 */
@Entity
@Table(name = "ingestion_errors")
public class IngestionErrorEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "ingestion_run_id")
	private UUID ingestionRunId;

	@Enumerated(EnumType.STRING)
	@Column(name = "error_type")
	private IngestionErrorType errorType;

	@Column(name = "error_code")
	private String errorCode;

	@Column(name = "error_message")
	private String errorMessage;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "raw_payload")
	private String rawPayload;

	@Column(name = "page_offset")
	private Integer pageOffset;

	@Column(name = "request_report_date")
	private LocalDate requestReportDate;

	@Column(name = "created_at")
	private Instant createdAt;

	protected IngestionErrorEntity() {
	}

	public IngestionErrorEntity(UUID ingestionRunId, IngestionErrorCode code, String errorMessage, String rawPayload,
			Integer pageOffset, LocalDate requestReportDate, Instant createdAt) {
		this.ingestionRunId = ingestionRunId;
		this.errorType = code.type();
		this.errorCode = code.name();
		this.errorMessage = errorMessage;
		this.rawPayload = rawPayload;
		this.pageOffset = pageOffset;
		this.requestReportDate = requestReportDate;
		this.createdAt = createdAt;
	}

	public UUID getId() {
		return this.id;
	}

	public UUID getIngestionRunId() {
		return this.ingestionRunId;
	}

	public IngestionErrorType getErrorType() {
		return this.errorType;
	}

	public String getErrorCode() {
		return this.errorCode;
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}

	public String getRawPayload() {
		return this.rawPayload;
	}

	public Integer getPageOffset() {
		return this.pageOffset;
	}

	public LocalDate getRequestReportDate() {
		return this.requestReportDate;
	}

	public Instant getCreatedAt() {
		return this.createdAt;
	}

}
