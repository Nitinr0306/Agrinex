package com.agrinex.price;

import java.time.LocalDate;

/**
 * Freshness of an observation, derived on every response from its report date and "today" in Asia/Kolkata and
 * never stored on observations (spec section 16, F1.16, FR-13; DATABASE_ARCHITECTURE.md section 10.6).
 */
public enum FreshnessStatus {

	FRESH, RECENT, STALE, UNAVAILABLE;

	public static FreshnessStatus classify(LocalDate reportDate, LocalDate today, FreshnessThresholds thresholds) {
		if (!reportDate.isBefore(today.minusDays(thresholds.freshDays()))) {
			return FRESH;
		}
		if (!reportDate.isBefore(today.minusDays(thresholds.recentDays()))) {
			return RECENT;
		}
		if (!reportDate.isBefore(today.minusDays(thresholds.staleDays()))) {
			return STALE;
		}
		return UNAVAILABLE;
	}

}
