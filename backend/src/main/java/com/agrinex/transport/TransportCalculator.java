package com.agrinex.transport;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.agrinex.common.Decimals;

/**
 * The spec section 32 transport model, as exact decimal arithmetic with the rounding points of
 * docs/api/API_CONVENTIONS.md section 4.1. The same formula serves comparisons and server-side What-If
 * (ADR-0026 item 1). These are estimates, never transport quotes (spec section 49).
 */
public final class TransportCalculator {

	/** WGS 84 / GRS 80 mean Earth radius R1 in kilometres (ADR-0036). */
	public static final double EARTH_RADIUS_KM = 6371.0088;

	private TransportCalculator() {
	}

	/**
	 * Haversine distance in double precision, converted through its shortest decimal representation
	 * ({@link BigDecimal#valueOf(double)}) and rounded half-up to 2 decimal places before any further use
	 * (ADR-0036; FR-06).
	 */
	public static BigDecimal distanceKm(BigDecimal fromLat, BigDecimal fromLon, BigDecimal toLat, BigDecimal toLon) {
		double phi1 = Math.toRadians(fromLat.doubleValue());
		double phi2 = Math.toRadians(toLat.doubleValue());
		double deltaPhi = Math.toRadians(toLat.doubleValue() - fromLat.doubleValue());
		double deltaLambda = Math.toRadians(toLon.doubleValue() - fromLon.doubleValue());
		double a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2)
				+ Math.cos(phi1) * Math.cos(phi2) * Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
		double distance = 2 * EARTH_RADIUS_KM * Math.asin(Math.min(1.0, Math.sqrt(a)));
		return Decimals.round2(BigDecimal.valueOf(distance));
	}

	/**
	 * trips = ceil(quantityKg / vehicleCapacityKg); perTripCost = max(minimumTripCharge, distanceKm x ratePerKm);
	 * estimatedTransportCost = round2(perTripCost x trips).
	 */
	public static TransportEstimate estimate(BigDecimal distanceKm, BigDecimal quantityKg, BigDecimal ratePerKm,
			BigDecimal minimumTripCharge, BigDecimal vehicleCapacityKg) {
		long trips = quantityKg.divide(vehicleCapacityKg, 0, RoundingMode.CEILING).longValueExact();
		BigDecimal distanceCost = distanceKm.multiply(ratePerKm);
		BigDecimal perTripCost = distanceCost.max(minimumTripCharge);
		BigDecimal total = Decimals.round2(perTripCost.multiply(BigDecimal.valueOf(trips)));
		return new TransportEstimate(trips, total);
	}

}
