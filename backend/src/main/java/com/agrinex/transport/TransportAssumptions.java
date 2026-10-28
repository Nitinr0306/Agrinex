package com.agrinex.transport;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * One version of the spec section 32 parameters: rate per km per trip, minimum charge per trip and vehicle
 * capacity in kilograms. Versions are immutable; an update inserts a new active version (ADR-0020 item 1).
 */
public record TransportAssumptions(UUID versionId, BigDecimal ratePerKm, BigDecimal minimumTripCharge,
		BigDecimal vehicleCapacityKg, boolean active, Instant createdAt) {
}
