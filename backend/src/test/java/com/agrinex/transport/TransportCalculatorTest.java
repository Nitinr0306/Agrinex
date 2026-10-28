package com.agrinex.transport;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** The spec section 32 transport model with hand-computed values (ADR-0036; API_CONVENTIONS.md section 4.1). */
class TransportCalculatorTest {

	static final BigDecimal RATE = new BigDecimal("8.00");

	static final BigDecimal MINIMUM = new BigDecimal("300.00");

	static final BigDecimal CAPACITY = new BigDecimal("1000.00");

	@Test
	void oneDegreeOfLongitudeOnTheEquatorIsTheMeanEarthRadiusTimesPiOver180() {
		// 6371.0088 x pi / 180 = 111.195083... km, rounded half-up to 111.20.
		BigDecimal distance = TransportCalculator.distanceKm(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
				BigDecimal.ONE);
		assertThat(distance).isEqualTo(new BigDecimal("111.20"));
	}

	@Test
	void theSamePointIsZeroKilometresAway() {
		BigDecimal point = new BigDecimal("18.520430");
		assertThat(TransportCalculator.distanceKm(point, point, point, point)).isEqualByComparingTo("0");
	}

	@Test
	void theMinimumTripChargeAppliesToShortTrips() {
		// 10 km x 8 = 80 < 300; 500 kg -> 1 trip.
		TransportEstimate estimate = TransportCalculator.estimate(new BigDecimal("10.00"), new BigDecimal("500"), RATE,
				MINIMUM, CAPACITY);
		assertThat(estimate.trips()).isEqualTo(1);
		assertThat(estimate.estimatedTransportCost()).isEqualTo(new BigDecimal("300.00"));
	}

	@Test
	void tripsRoundUpAndMultiplyThePerTripCost() {
		// 2500 kg / 1000 -> 3 trips; 45.50 km x 8 = 364.00 per trip -> 1092.00.
		TransportEstimate estimate = TransportCalculator.estimate(new BigDecimal("45.50"), new BigDecimal("2500"), RATE,
				MINIMUM, CAPACITY);
		assertThat(estimate.trips()).isEqualTo(3);
		assertThat(estimate.estimatedTransportCost()).isEqualTo(new BigDecimal("1092.00"));
	}

	@Test
	void anExactlyFullVehicleIsOneTrip() {
		TransportEstimate estimate = TransportCalculator.estimate(new BigDecimal("100.00"), new BigDecimal("1000"), RATE,
				MINIMUM, CAPACITY);
		assertThat(estimate.trips()).isEqualTo(1);
		assertThat(estimate.estimatedTransportCost()).isEqualTo(new BigDecimal("800.00"));
	}

	@Test
	void costsAreRoundedHalfUpToTwoDecimals() {
		// 12.35 km x 8.33 = 102.8755 per trip, above the 100.00 minimum; 1 trip -> 102.88.
		TransportEstimate estimate = TransportCalculator.estimate(new BigDecimal("12.35"), new BigDecimal("1"),
				new BigDecimal("8.33"), new BigDecimal("100.00"), CAPACITY);
		assertThat(estimate.estimatedTransportCost()).isEqualTo(new BigDecimal("102.88"));
	}

}
