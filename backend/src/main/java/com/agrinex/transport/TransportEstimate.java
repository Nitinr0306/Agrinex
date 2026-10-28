package com.agrinex.transport;

import java.math.BigDecimal;

/** ESTIMATED transport for one market: the trip count and the total cost in INR, rounded half-up to 2 dp. */
public record TransportEstimate(long trips, BigDecimal estimatedTransportCost) {
}
