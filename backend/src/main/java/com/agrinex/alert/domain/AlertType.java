package com.agrinex.alert.domain;

import java.math.BigDecimal;
import java.util.Comparator;

/**
 * The four alert types of spec section 34 with the trigger rules of ADR-0019 item 2. Price types compare the
 * latest selected modal price (ADR-0010); percentage types compare the unrounded 7-day change (ADR-0025), and
 * INSUFFICIENT_DATA never triggers them. For an alert without a market, the event names the market with the most
 * extreme qualifying value (ADR-0019 item 3).
 */
public enum AlertType {

	/** modal >= threshold; the highest price is the most extreme. */
	PRICE_ABOVE(true, Comparator.reverseOrder()),
	/** modal <= threshold; the lowest price is the most extreme. */
	PRICE_BELOW(true, Comparator.naturalOrder()),
	/** change >= threshold; the largest rise is the most extreme. */
	PERCENTAGE_RISE(false, Comparator.reverseOrder()),
	/** change <= -threshold; the largest fall is the most extreme. */
	PERCENTAGE_FALL(false, Comparator.naturalOrder());

	private final boolean priceBased;

	private final Comparator<BigDecimal> mostExtremeFirst;

	AlertType(boolean priceBased, Comparator<BigDecimal> mostExtremeFirst) {
		this.priceBased = priceBased;
		this.mostExtremeFirst = mostExtremeFirst;
	}

	/** Whether the alert compares a price; otherwise it compares the 7-day change in percent. */
	public boolean priceBased() {
		return this.priceBased;
	}

	public boolean triggeredBy(BigDecimal value, BigDecimal threshold) {
		return switch (this) {
			case PRICE_ABOVE, PERCENTAGE_RISE -> value.compareTo(threshold) >= 0;
			case PRICE_BELOW -> value.compareTo(threshold) <= 0;
			case PERCENTAGE_FALL -> value.compareTo(threshold.negate()) <= 0;
		};
	}

	/** Orders qualifying values so that the most extreme comes first. */
	public Comparator<BigDecimal> mostExtremeFirst() {
		return this.mostExtremeFirst;
	}

}
