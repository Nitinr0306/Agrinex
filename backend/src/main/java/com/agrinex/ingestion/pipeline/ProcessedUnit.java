package com.agrinex.ingestion.pipeline;

import java.util.List;

import com.agrinex.price.ObservationCandidate;

/**
 * A delivery unit after validation, the outlier rule and deduplication (ADR-0011 item 7): the candidates to load,
 * the record-level errors to store with them, and the counts. Every fetched record is in exactly one of
 * candidates, duplicates, validation errors, conflicts or out of scope (ADR-0012 item 1).
 */
public record ProcessedUnit(List<ObservationCandidate> candidates, List<PendingError> errors, int duplicates,
		int validationErrors, int conflicted, int outOfScope) {
}
