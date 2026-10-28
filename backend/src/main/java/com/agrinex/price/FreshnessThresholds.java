package com.agrinex.price;

/**
 * Freshness thresholds in days of a recommendation-weights version (spec section 16; ADR-0024: 1, 4 and 10 by
 * default). They also bound the selection window (ADR-0010 item 2).
 */
public record FreshnessThresholds(int freshDays, int recentDays, int staleDays) {
}
