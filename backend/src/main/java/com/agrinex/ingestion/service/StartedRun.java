package com.agrinex.ingestion.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.agrinex.ingestion.TriggeredBy;
import com.agrinex.price.SourceDataset;
import com.agrinex.price.SourceDatasetInfo;

/**
 * A run whose RUNNING row is committed, with its delivery units: the whole run for CURRENT_PRICE, one report date
 * each for HISTORICAL_PRICE (ADR-0011 item 6).
 */
public record StartedRun(UUID runId, SourceDatasetInfo dataset, TriggeredBy triggeredBy,
		List<LocalDate> reportDates) {

	/** The delivery units in order; the report date is null for the single CURRENT_PRICE unit. */
	List<DeliveryUnit> deliveryUnits() {
		return (this.dataset.type() == SourceDataset.CURRENT_PRICE) ? List.of(new DeliveryUnit(null))
				: this.reportDates.stream().map(DeliveryUnit::new).toList();
	}

	/** One delivery unit: a HISTORICAL report date, or the CURRENT run when the date is null. */
	record DeliveryUnit(LocalDate reportDate) {
	}

}
