package com.agrinex.ingestion.pipeline;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.agrinex.commodity.CommodityInfo;
import com.agrinex.geography.DistrictInfo;

/**
 * A record that passed rules V1 to V7 (ADR-0008 item 5): its commodity is known with a verified unit, its
 * district resolved, its date parsed, and its prices exact decimals rounded half-up to 2 places (absent min or
 * max prices are null).
 */
public record ValidRecord(SourceRecord source, CommodityInfo commodity, DistrictInfo district, LocalDate reportDate,
		BigDecimal modalPrice, BigDecimal minPrice, BigDecimal maxPrice) {
}
