package com.agrinex.admin.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.agrinex.transport.TransportAssumptions;

/** Contract TransportAssumptionResponse (spec section 30): one version. */
public record TransportAssumptionResponse(UUID versionId, BigDecimal ratePerKm, BigDecimal minimumTripCharge,
		BigDecimal vehicleCapacityKg, boolean active, Instant createdAt) {

	public static TransportAssumptionResponse from(TransportAssumptions version) {
		return new TransportAssumptionResponse(version.versionId(), version.ratePerKm(), version.minimumTripCharge(),
				version.vehicleCapacityKg(), version.active(), version.createdAt());
	}

}
