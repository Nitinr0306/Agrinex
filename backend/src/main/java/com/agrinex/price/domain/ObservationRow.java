package com.agrinex.price.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.agrinex.price.SourceDataset;

/** A stored observation with the names the selection tie-break needs (variety and grade names may be null). */
public record ObservationRow(UUID id, UUID marketId, UUID commodityId, UUID varietyId, String varietyName, UUID gradeId,
		String gradeName, LocalDate reportDate, BigDecimal modalPrice, BigDecimal minPrice, BigDecimal maxPrice,
		String priceUnit, SourceDataset sourceDataset, String sourceResourceId, Instant ingestedAt) {

	/** Identity without the dataset: rows that differ only by dataset are the same observation reported twice. */
	IdentityKey identityWithoutDataset() {
		return new IdentityKey(this.marketId, this.commodityId, this.varietyId, this.gradeId, this.reportDate);
	}

	record IdentityKey(UUID marketId, UUID commodityId, UUID varietyId, UUID gradeId, LocalDate reportDate) {
	}

}
