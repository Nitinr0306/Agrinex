package com.agrinex.ingestion.service;

import java.time.Instant;
import java.util.UUID;

import com.agrinex.ingestion.IngestionRunStatus;
import com.agrinex.ingestion.repository.IngestionRunRepository;
import com.agrinex.price.PriceApi;
import com.agrinex.price.SourceDataset;
import com.agrinex.price.SourceSyncStatus;
import com.agrinex.price.SourceSyncStatusPort;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements the price module's read-only port (ADR-0035 item 1a): the status that drives the spec section 29
 * banner. {@code latestSyncFailed} is true while the most recent completed CURRENT_PRICE run is FAILED;
 * {@code lastSuccessfulSyncAt} is the end of the last CURRENT_PRICE run that loaded data (ADR-0005 item 5).
 */
@Component
class SourceSyncStatusAdapter implements SourceSyncStatusPort {

	private final IngestionRunRepository runs;

	private final PriceApi prices;

	SourceSyncStatusAdapter(IngestionRunRepository runs, PriceApi prices) {
		this.runs = runs;
		this.prices = prices;
	}

	@Override
	@Transactional(readOnly = true)
	public SourceSyncStatus currentDatasetStatus() {
		UUID datasetId = this.prices.sourceDataset(SourceDataset.CURRENT_PRICE).id();
		boolean latestFailed = this.runs
			.findFirstBySourceDatasetIdAndStatusNotOrderByStartedAtDescIdAsc(datasetId, IngestionRunStatus.RUNNING)
			.map((run) -> run.getStatus() == IngestionRunStatus.FAILED)
			.orElse(false);
		Instant lastLoaded = this.runs.findLatestFinishedAt(datasetId, RunLifecycle.LOADED).orElse(null);
		return new SourceSyncStatus(lastLoaded, latestFailed);
	}

}
