package com.agrinex.recommendation.engine;

import java.math.BigDecimal;

import com.agrinex.transport.TransportAssumptions;

/**
 * The spec section 32 parameters one calculation applies: the rate is the configured one or a simulation's
 * override (ADR-0026 item 1; ADR-0038).
 */
public record TransportParameters(BigDecimal ratePerKm, BigDecimal minimumTripCharge, BigDecimal vehicleCapacityKg) {

	public static TransportParameters of(TransportAssumptions assumptions, BigDecimal appliedRatePerKm) {
		return new TransportParameters(appliedRatePerKm, assumptions.minimumTripCharge(),
				assumptions.vehicleCapacityKg());
	}

}
