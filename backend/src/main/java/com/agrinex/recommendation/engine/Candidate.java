package com.agrinex.recommendation.engine;

import java.math.BigDecimal;

import com.agrinex.price.MarketInfo;
import com.agrinex.price.SelectedObservation;
import com.agrinex.price.TrendStatistic;

/**
 * An eligible market: inside the radius, with one selected observation (ADR-0010) and its trend (ADR-0025).
 * {@code distanceKm} is already rounded to 2 decimal places, the value every later step uses.
 */
public record Candidate(MarketInfo market, BigDecimal distanceKm, SelectedObservation observation,
		TrendStatistic trend) {
}
