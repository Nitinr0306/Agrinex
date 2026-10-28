package com.agrinex.ingestion;

/** Error types of {@code ingestion_errors} (ADR-0009, ADR-0011 item 9). */
public enum IngestionErrorType {

	VALIDATION, API_ERROR, CONFLICT, SCHEMA_ERROR, SYSTEM

}
