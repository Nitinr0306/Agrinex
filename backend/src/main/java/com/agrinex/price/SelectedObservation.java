package com.agrinex.price;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * One real stored observation chosen by the selection rules of ADR-0010, with its provenance. It is never an
 * average, a carried-forward value or an interpolation (spec section 31 RULE 5).
 */
public record SelectedObservation(UUID observationId, UUID marketId, UUID commodityId, UUID varietyId,
		String varietyName, UUID gradeId, String gradeName, LocalDate reportDate, BigDecimal modalPrice,
		BigDecimal minPrice, BigDecimal maxPrice, String priceUnit, SourceDataset sourceDataset,
		String sourceResourceId, Instant ingestedAt) {
}
