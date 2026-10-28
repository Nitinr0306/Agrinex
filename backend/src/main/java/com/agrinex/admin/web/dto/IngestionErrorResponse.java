package com.agrinex.admin.web.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.agrinex.ingestion.IngestionErrorType;
import com.agrinex.ingestion.IngestionErrorView;

/** Contract IngestionErrorResponse: never contains the API key (ADR-0020 item 5). */
public record IngestionErrorResponse(UUID id, UUID runId, IngestionErrorType errorType, String errorCode,
		String errorMessage, Integer pageOffset, LocalDate requestReportDate, String rawPayloadExcerpt,
		boolean rawPayloadTruncated, Instant createdAt) {

	public static IngestionErrorResponse from(IngestionErrorView error) {
		return new IngestionErrorResponse(error.id(), error.runId(), error.errorType(), error.errorCode(),
				error.errorMessage(), error.pageOffset(), error.requestReportDate(), error.rawPayloadExcerpt(),
				error.rawPayloadTruncated(), error.createdAt());
	}

}
