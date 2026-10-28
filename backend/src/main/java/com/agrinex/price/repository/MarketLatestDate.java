package com.agrinex.price.repository;

import java.time.LocalDate;
import java.util.UUID;

/** Projection: the newest report date stored for a market, across commodities. */
public record MarketLatestDate(UUID marketId, LocalDate latestReportDate) {
}
