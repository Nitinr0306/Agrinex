package com.agrinex.recommendation.service;

import java.math.BigDecimal;

/**
 * The What-If values a user changed (spec section 17; ADR-0026 item 4): quantity in kilograms, rate in rupees per
 * km per trip, radius in whole kilometres. A null value keeps the original comparison's.
 */
public record SimulationOverrides(BigDecimal quantityKg, BigDecimal transportRatePerKm, Integer maxDistanceKm) {
}
