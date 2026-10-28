package com.agrinex.recommendation.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import com.agrinex.price.PriceApi;
import com.agrinex.transport.TransportCalculator;

import org.springframework.stereotype.Component;

/**
 * Finds the VERIFIED, active markets with coordinates within the radius of the origin by haversine distance
 * (spec section 16 FIND; ADR-0014 item 5, ADR-0036). The rounded distance is the one compared with the radius,
 * shown and used in the transport formula.
 */
@Component
class CandidateMarkets {

	/** Distance, then market name in code-point order, then market id (contract ExcludedMarket order). */
	static final Comparator<MarketInRange> BY_DISTANCE = Comparator.comparing(MarketInRange::distanceKm)
		.thenComparing((market) -> market.market().name())
		.thenComparing((market) -> market.market().id());

	private final PriceApi prices;

	CandidateMarkets(PriceApi prices) {
		this.prices = prices;
	}

	List<MarketInRange> within(BigDecimal originLat, BigDecimal originLon, int maxDistanceKm) {
		BigDecimal radius = BigDecimal.valueOf(maxDistanceKm);
		return this.prices.comparisonCandidates()
			.stream()
			.map((market) -> new MarketInRange(market,
					TransportCalculator.distanceKm(originLat, originLon, market.lat(), market.lon())))
			.filter((candidate) -> candidate.distanceKm().compareTo(radius) <= 0)
			.sorted(BY_DISTANCE)
			.toList();
	}

}
