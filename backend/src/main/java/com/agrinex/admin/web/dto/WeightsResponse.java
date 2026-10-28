package com.agrinex.admin.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.agrinex.recommendation.WeightsVersion;

/** Contract WeightsResponse (spec section 30): one version. */
public record WeightsResponse(UUID versionId, BigDecimal wNetRealization, BigDecimal wPrice, BigDecimal wTrend,
		BigDecimal wFreshness, BigDecimal wTransportPenalty, int freshnessFreshDays, int freshnessRecentDays,
		int freshnessStaleDays, boolean active, Instant createdAt) {

	public static WeightsResponse from(WeightsVersion version) {
		return new WeightsResponse(version.versionId(), version.wNetRealization(), version.wPrice(), version.wTrend(),
				version.wFreshness(), version.wTransportPenalty(), version.freshnessFreshDays(),
				version.freshnessRecentDays(), version.freshnessStaleDays(), version.active(), version.createdAt());
	}

}
