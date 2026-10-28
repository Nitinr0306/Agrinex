package com.agrinex.price;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A validated, deduplicated and conflict-free record of one delivery unit, ready to load (ADR-0009 item 5;
 * ADR-0011 item 7). Prices are exact decimals rounded half-up to 2 dp in the commodity's verified unit.
 */
public record ObservationCandidate(UUID marketId, UUID commodityId, UUID varietyId, UUID gradeId,
		LocalDate reportDate, BigDecimal modalPrice, BigDecimal minPrice, BigDecimal maxPrice, String priceUnit,
		String sourceCommodityName) {
}
