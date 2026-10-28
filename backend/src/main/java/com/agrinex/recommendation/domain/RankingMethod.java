package com.agrinex.recommendation.domain;

/**
 * How options are ranked (contract RankingMethod): the spec section 16 composite score for comparisons
 * (ADR-0024), estimated net realization for What-If simulations (spec section 17; ADR-0026 item 2).
 */
public enum RankingMethod {

	DETERMINISTIC_COMPOSITE_SCORE, NET_REALIZATION

}
