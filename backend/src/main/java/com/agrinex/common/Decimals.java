package com.agrinex.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Rounding rules shared by every calculation and every published number: exact decimal arithmetic,
 * rounded half-up only at the named points (ADR-0008 item 4; docs/api/API_CONVENTIONS.md section 4).
 */
public final class Decimals {

	private Decimals() {
	}

	/** Half-up to 2 decimal places: prices, distances and INR amounts. */
	public static BigDecimal round2(BigDecimal value) {
		return value.setScale(2, RoundingMode.HALF_UP);
	}

	/** Half-up to 1 decimal place: the displayed 7-day change (ADR-0025 item 1). */
	public static BigDecimal round1(BigDecimal value) {
		return value.setScale(1, RoundingMode.HALF_UP);
	}

	/** Half-up to 4 decimal places: alert thresholds and trigger values (numeric(14,4)). */
	public static BigDecimal round4(BigDecimal value) {
		return value.setScale(4, RoundingMode.HALF_UP);
	}

	/** Number of digits after the decimal point, ignoring trailing zeros. */
	public static int decimalPlaces(BigDecimal value) {
		BigDecimal stripped = value.stripTrailingZeros();
		return Math.max(stripped.scale(), 0);
	}

}
