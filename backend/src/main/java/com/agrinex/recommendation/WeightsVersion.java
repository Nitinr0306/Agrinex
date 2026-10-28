package com.agrinex.recommendation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.agrinex.price.FreshnessThresholds;

/**
 * One version of the spec section 16 scoring weights and freshness thresholds (table recommendation_weights;
 * ADR-0024 defaults 0.55, 0.15, 0.10, 0.10, 0.10 and 1, 4, 10 days). Versions are immutable; an update inserts a
 * new active version (ADR-0020 item 1).
 */
public record WeightsVersion(UUID versionId, BigDecimal wNetRealization, BigDecimal wPrice, BigDecimal wTrend,
		BigDecimal wFreshness, BigDecimal wTransportPenalty, int freshnessFreshDays, int freshnessRecentDays,
		int freshnessStaleDays, boolean active, Instant createdAt) {

	public FreshnessThresholds thresholds() {
		return new FreshnessThresholds(this.freshnessFreshDays, this.freshnessRecentDays, this.freshnessStaleDays);
	}

}
