package com.agrinex.ingestion.repository;

import java.util.UUID;

/** Number of error rows of one run, a JPQL constructor projection of {@link IngestionErrorRepository}. */
public record RunErrorCount(UUID runId, long errors) {
}
