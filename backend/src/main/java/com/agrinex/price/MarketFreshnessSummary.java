package com.agrinex.price;

import java.time.LocalDate;

/**
 * Freshness of the VERIFIED, active markets as of a date, each classified by its latest observation date across
 * commodities; markets without observations count as unavailable (contract MarketFreshnessSummary).
 */
public record MarketFreshnessSummary(LocalDate asOfDate, long totalMarkets, long fresh, long recent, long stale,
		long unavailable) {
}
