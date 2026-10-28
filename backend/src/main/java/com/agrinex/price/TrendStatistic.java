package com.agrinex.price;

import java.math.BigDecimal;

import com.agrinex.common.Decimals;

/**
 * The trend statistic of ADR-0025: direction plus the unrounded 7-day change in percent (null with
 * INSUFFICIENT_DATA). The direction and percentage alerts use the unrounded value; responses display it
 * rounded half-up to 1 decimal place.
 */
public record TrendStatistic(TrendDirection direction, BigDecimal changePercent) {

	public static final TrendStatistic INSUFFICIENT = new TrendStatistic(TrendDirection.INSUFFICIENT_DATA, null);

	public BigDecimal displayChangePercent() {
		return (this.changePercent != null) ? Decimals.round1(this.changePercent) : null;
	}

}
