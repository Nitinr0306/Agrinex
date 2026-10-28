package com.agrinex.admin.web.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.agrinex.ingestion.IngestionRunStatus;
import com.agrinex.ingestion.IngestionRunView;
import com.agrinex.ingestion.TriggeredBy;
import com.agrinex.price.SourceDataset;

/** Contract TriggerResponse (spec section 30): the started MANUAL run. */
public record TriggerResponse(UUID runId, SourceDataset datasetType, TriggeredBy triggeredBy,
		IngestionRunStatus status, Instant startedAt, LocalDate reportDateFrom, LocalDate reportDateTo) {

	public static TriggerResponse from(IngestionRunView run) {
		return new TriggerResponse(run.id(), run.datasetType(), run.triggeredBy(), run.status(), run.startedAt(),
				run.reportDateFrom(), run.reportDateTo());
	}

}
