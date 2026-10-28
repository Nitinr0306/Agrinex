package com.agrinex.price;

/**
 * Read-only port declared by the price module and implemented by the ingestion module (ADR-0035 item 1a): the
 * status of the latest completed CURRENT_PRICE run and the end of the last run that loaded data.
 */
public interface SourceSyncStatusPort {

	SourceSyncStatus currentDatasetStatus();

}
