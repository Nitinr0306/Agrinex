package com.agrinex.price;

import java.time.Instant;

/**
 * Latest synchronization status of the CURRENT_PRICE dataset, carried by every price-serving response (ADR-0022
 * item 9). {@code latestSyncFailed} drives the spec section 29 banner; stored data is never changed by it.
 */
public record SourceSyncStatus(String sourceDataset, Instant lastSuccessfulSyncAt, boolean latestSyncFailed) {

	public SourceSyncStatus(Instant lastSuccessfulSyncAt, boolean latestSyncFailed) {
		this(SourceDataset.CURRENT_PRICE.name(), lastSuccessfulSyncAt, latestSyncFailed);
	}

}
