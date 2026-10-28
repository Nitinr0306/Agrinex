package com.agrinex.ingestion;

/** Status of an ingestion run (spec section 15, 29; status rule ADR-0011 item 8). */
public enum IngestionRunStatus {

	RUNNING, SUCCESS, PARTIAL_FAILURE, FAILED

}
