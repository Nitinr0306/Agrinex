package com.agrinex.recommendation.engine;

import java.math.BigDecimal;
import java.util.List;

import com.agrinex.recommendation.domain.ReasonCode;

/**
 * An option with its rank (1..N), its composite score (null when ranked by net realization) and its reason codes
 * in the spec section 16 order.
 */
public record RankedOption(EvaluatedOption option, int rank, BigDecimal score, List<ReasonCode> reasonCodes) {
}
