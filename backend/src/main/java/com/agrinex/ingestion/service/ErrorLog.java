package com.agrinex.ingestion.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.agrinex.common.time.AgrinexCalendar;
import com.agrinex.ingestion.domain.IngestionErrorCode;
import com.agrinex.ingestion.domain.IngestionErrorEntity;
import com.agrinex.ingestion.pipeline.PendingError;
import com.agrinex.ingestion.repository.IngestionErrorRepository;
import com.agrinex.ingestion.source.ApiKeyRedactor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Writes {@code ingestion_errors}: record-level errors with their unit's load, and page, unit and run failures
 * on their own. Every message and payload passes through the API-key redactor first (ADR-0020 item 5).
 */
@Component
public class ErrorLog {

	private final IngestionErrorRepository errors;

	private final ApiKeyRedactor redactor;

	private final PayloadJson payloadJson;

	private final AgrinexCalendar calendar;

	ErrorLog(IngestionErrorRepository errors, ApiKeyRedactor redactor, PayloadJson payloadJson,
			AgrinexCalendar calendar) {
		this.errors = errors;
		this.redactor = redactor;
		this.payloadJson = payloadJson;
		this.calendar = calendar;
	}

	/** Rejected records and conflict variants of one unit, in the unit's load transaction (ADR-0011 item 7). */
	@Transactional(propagation = Propagation.MANDATORY)
	public void recordUnitErrors(UUID runId, List<PendingError> pending) {
		Instant now = this.calendar.now();
		this.errors.saveAll(pending.stream()
			.map((error) -> new IngestionErrorEntity(runId, error.code(), this.redactor.redact(error.message()),
					this.redactor.redact(error.payloadJson()), null, null, now))
			.toList());
	}

	/**
	 * A failure of a page, a delivery unit or the run (ADR-0011 item 9); a HISTORICAL failure records its report
	 * date. Joins the caller's transaction or runs in its own.
	 */
	@Transactional
	public void recordFailure(UUID runId, IngestionErrorCode code, String message, String responseBody,
			Integer pageOffset, LocalDate requestReportDate) {
		String payload = (responseBody != null) ? this.payloadJson.bodyAsJson(this.redactor.redact(responseBody))
				: null;
		this.errors.save(new IngestionErrorEntity(runId, code, this.redactor.redact(message), payload, pageOffset,
				requestReportDate, this.calendar.now()));
	}

}
