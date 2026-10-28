package com.agrinex.ingestion.pipeline;

import com.agrinex.ingestion.domain.IngestionErrorCode;

/**
 * A record-level error of a delivery unit, written with the unit's load (ADR-0011 item 7). The payload is the
 * record's JSON text; it is redacted when stored.
 */
public record PendingError(IngestionErrorCode code, String message, String payloadJson) {
}
