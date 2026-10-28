package com.agrinex.recommendation.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import com.agrinex.common.validation.MaxDecimalPlaces;
import com.agrinex.recommendation.service.SimulationOverrides;

/**
 * Contract SimulateRequest (spec section 17; ADR-0026 items 3 and 4): quantity 100 to 5,000 kg, rate 3 to 20
 * rupees per km, radius 25 to 300 km. An omitted or null value keeps the original comparison's.
 */
public record SimulateRequest(
		@NotNull UUID recommendationId,
		@DecimalMin("100") @DecimalMax("5000") @MaxDecimalPlaces(3) BigDecimal quantity,
		@DecimalMin("3") @DecimalMax("20") @MaxDecimalPlaces(2) BigDecimal transportRatePerKm,
		@Min(25) @Max(300) Integer maxDistanceKm) {

	public SimulationOverrides toOverrides() {
		return new SimulationOverrides(this.quantity, this.transportRatePerKm, this.maxDistanceKm);
	}

}
