package com.agrinex.admin.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.agrinex.ingestion.IngestionRunStatus;
import com.agrinex.ingestion.IngestionRunView;
import com.agrinex.ingestion.TriggeredBy;
import com.agrinex.price.SourceDataset;

/** Contract IngestionRunResponse (spec section 30; ADR-0012). */
public record IngestionRunResponse(UUID id, SourceDataset datasetType, TriggeredBy triggeredBy,
		IngestionRunStatus status, Instant startedAt, Instant finishedAt, Long durationSeconds,
		Map<String, Object> requestScope, Integer sourceTotalRecords, int pagesFetched, int pagesFailed,
		int recordsFetched, int recordsInserted, int recordsUpdated, int duplicatesSkipped, int validationErrorsCount,
		int recordsConflicted, int recordsOutOfScope, BigDecimal completenessPercent, long errorCount) {

	public static IngestionRunResponse from(IngestionRunView run) {
		return new IngestionRunResponse(run.id(), run.datasetType(), run.triggeredBy(), run.status(), run.startedAt(),
				run.finishedAt(), run.durationSeconds(), run.requestScope(), run.sourceTotalRecords(),
				run.pagesFetched(), run.pagesFailed(), run.recordsFetched(), run.recordsInserted(),
				run.recordsUpdated(), run.duplicatesSkipped(), run.validationErrorsCount(), run.recordsConflicted(),
				run.recordsOutOfScope(), run.completenessPercent(), run.errorCount());
	}

}
