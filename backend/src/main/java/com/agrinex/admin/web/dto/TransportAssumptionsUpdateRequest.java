package com.agrinex.admin.web.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import com.agrinex.common.validation.MaxDecimalPlaces;

/** Contract TransportAssumptionsUpdateRequest: each value greater than 0 (ADR-0020 item 1). */
public record TransportAssumptionsUpdateRequest(
		@NotNull @DecimalMin(value = "0", inclusive = false) @DecimalMax("99999999.99") @MaxDecimalPlaces(2)
		BigDecimal ratePerKm,
		@NotNull @DecimalMin(value = "0", inclusive = false) @DecimalMax("9999999999.99") @MaxDecimalPlaces(2)
		BigDecimal minimumTripCharge,
		@NotNull @DecimalMin(value = "0", inclusive = false) @DecimalMax("99999999.99") @MaxDecimalPlaces(2)
		BigDecimal vehicleCapacityKg) {
}
