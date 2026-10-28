package com.agrinex.ingestion;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * One rejected record, conflict or failure of a run (spec section 30; ADR-0008, ADR-0009, ADR-0011). The message
 * and the payload never contain the API key: it is removed before storage (ADR-0020 item 5).
 */
public record IngestionErrorView(UUID id, UUID runId, IngestionErrorType errorType, String errorCode,
		String errorMessage, Integer pageOffset, LocalDate requestReportDate, String rawPayloadExcerpt,
		boolean rawPayloadTruncated, Instant createdAt) {
}
