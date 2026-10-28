package com.agrinex.recommendation.service;

import java.math.BigDecimal;
import java.util.UUID;

import com.agrinex.recommendation.domain.QuantityUnit;

/**
 * A comparison request that passed the contract's data-independent checks (ADR-0023 item 1).
 * {@code maxDistanceKm} is null when the default radius applies.
 */
public record CompareCommand(UUID commodityId, UUID varietyId, UUID gradeId, BigDecimal quantity,
		QuantityUnit quantityUnit, BigDecimal originLat, BigDecimal originLon, Integer maxDistanceKm) {
}
