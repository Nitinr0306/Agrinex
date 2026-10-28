package com.agrinex.price;

/**
 * Read-only port declared by the price module and implemented by the recommendation module (ADR-0035 item 1b):
 * the freshness thresholds of the active recommendation-weights version, used by market detail, trends,
 * alerts and the commodity filter. Comparisons pass their own pinned thresholds instead (ADR-0035 item 2).
 */
public interface FreshnessPolicyPort {

	FreshnessThresholds activeThresholds();

}
