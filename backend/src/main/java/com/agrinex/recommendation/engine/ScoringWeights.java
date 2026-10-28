package com.agrinex.recommendation.engine;

import java.math.BigDecimal;

import com.agrinex.recommendation.WeightsVersion;

/** The five spec section 16 weights of one recommendation-weights version (ADR-0024). */
public record ScoringWeights(BigDecimal netRealization, BigDecimal price, BigDecimal trend, BigDecimal freshness,
		BigDecimal transportPenalty) {

	public static ScoringWeights of(WeightsVersion version) {
		return new ScoringWeights(version.wNetRealization(), version.wPrice(), version.wTrend(), version.wFreshness(),
				version.wTransportPenalty());
	}

}
