package com.agrinex.recommendation.domain;

/**
 * Fixed vocabulary explaining a ranked option (spec section 16, P2, F1.12; trigger rules ADR-0024 item 6), in the
 * spec section 16 order, which is also the order codes are listed in. Database check
 * {@code ck_selling_options_reason_codes} holds the same values.
 */
public enum ReasonCode {

	PRICE_ADVANTAGE, LOWER_TRANSPORT, POSITIVE_TREND, STABLE_TREND, RECENT_DATA, SUITABLE_DISTANCE, NEGATIVE_TREND,
	STALE_DATA

}
