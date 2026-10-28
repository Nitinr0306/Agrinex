package com.agrinex.recommendation.service;

import java.math.BigDecimal;

import com.agrinex.recommendation.WeightsVersion;
import com.agrinex.recommendation.domain.QuantityUnit;
import com.agrinex.recommendation.domain.RankingMethod;
import com.agrinex.recommendation.engine.TransportParameters;

/** Everything one calculation needs: the validated request values and the configuration versions it uses. */
public record ComparisonInputs(ComparedCommodity references, BigDecimal quantity, QuantityUnit quantityUnit,
		BigDecimal originLat, BigDecimal originLon, int maxDistanceKm, WeightsVersion weights,
		TransportParameters transport, RankingMethod rankingMethod) {
}
