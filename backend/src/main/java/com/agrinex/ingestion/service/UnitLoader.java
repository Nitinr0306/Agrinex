package com.agrinex.ingestion.service;

import java.util.UUID;

import com.agrinex.ingestion.domain.RunCounters.UnitCounts;
import com.agrinex.ingestion.pipeline.ProcessedUnit;
import com.agrinex.price.LoadResult;
import com.agrinex.price.PriceApi;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads one delivery unit in one transaction (ADR-0011 item 7): its record-level errors and its observations,
 * through the price module (ADR-0013). Units already committed are never rolled back by a later failure.
 */
@Component
public class UnitLoader {

	private final ErrorLog errorLog;

	private final PriceApi prices;

	UnitLoader(ErrorLog errorLog, PriceApi prices) {
		this.errorLog = errorLog;
		this.prices = prices;
	}

	/** Identical re-deliveries of stored observations count as duplicates (ADR-0009 item 3). */
	@Transactional
	public UnitCounts load(UUID runId, UUID sourceDatasetId, ProcessedUnit unit) {
		this.errorLog.recordUnitErrors(runId, unit.errors());
		LoadResult result = this.prices.loadObservations(runId, sourceDatasetId, unit.candidates());
		return new UnitCounts(result.inserted(), result.updated(), unit.duplicates() + result.unchanged(),
				unit.validationErrors(), unit.conflicted(), unit.outOfScope());
	}

}
