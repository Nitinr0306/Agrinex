package com.agrinex.recommendation.mapper;

import com.agrinex.recommendation.WeightsVersion;
import com.agrinex.recommendation.domain.RecommendationWeightsEntity;

/** Maps recommendation entities to the module's public records. */
public final class RecommendationMapper {

	private RecommendationMapper() {
	}

	public static WeightsVersion toView(RecommendationWeightsEntity entity) {
		return new WeightsVersion(entity.getId(), entity.getWNetRealization(), entity.getWPrice(), entity.getWTrend(),
				entity.getWFreshness(), entity.getWTransportPenalty(), entity.getFreshnessFreshDays(),
				entity.getFreshnessRecentDays(), entity.getFreshnessStaleDays(), entity.isActive(),
				entity.getCreatedAt());
	}

}
