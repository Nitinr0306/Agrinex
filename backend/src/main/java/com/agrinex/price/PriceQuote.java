package com.agrinex.price;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.agrinex.common.web.Ref;

/**
 * A government-reported observation as published, with provenance and freshness (contract
 * price-observation.schema.json PriceQuote; ADR-0022 item 9).
 */
public record PriceQuote(UUID observationId, LocalDate reportDate, Ref variety, Ref grade, BigDecimal minPrice,
		BigDecimal maxPrice, BigDecimal modalPrice, String priceUnit, SourceDataset sourceDataset,
		String sourceResourceId, Instant ingestedAt, FreshnessStatus freshnessStatus, String attribution) {

	/** spec section 31 RULE 7. */
	public static final String ATTRIBUTION = "Source: Ministry of Agriculture & FW / DMI / data.gov.in";

	public static PriceQuote of(SelectedObservation observation, FreshnessStatus freshness) {
		return new PriceQuote(observation.observationId(), observation.reportDate(),
				(observation.varietyId() != null) ? new Ref(observation.varietyId(), observation.varietyName()) : null,
				(observation.gradeId() != null) ? new Ref(observation.gradeId(), observation.gradeName()) : null,
				observation.minPrice(), observation.maxPrice(), observation.modalPrice(), observation.priceUnit(),
				observation.sourceDataset(), observation.sourceResourceId(), observation.ingestedAt(), freshness,
				ATTRIBUTION);
	}

}
