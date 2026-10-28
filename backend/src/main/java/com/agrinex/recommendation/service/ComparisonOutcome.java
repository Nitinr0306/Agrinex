package com.agrinex.recommendation.service;

import java.util.List;

import com.agrinex.recommendation.engine.Ranking;

/**
 * The ranked eligible markets and the markets in range without a recent observation, ordered by distance, then
 * name, then id (ADR-0023 item 3).
 */
public record ComparisonOutcome(Ranking ranking, List<MarketInRange> excludedMarkets) {
}
