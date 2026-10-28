package com.agrinex.price.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.agrinex.commodity.CommodityAvailabilityPort;
import com.agrinex.common.time.AgrinexCalendar;
import com.agrinex.price.FreshnessPolicyPort;
import com.agrinex.price.FreshnessThresholds;
import com.agrinex.price.MarketInfo;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements the commodity module's read-only port (ADR-0035 item 1c) over the existing price -> commodity edge:
 * the commodities with at least one observation dated within the active stale window at a VERIFIED, active market
 * with coordinates.
 */
@Component
class CommodityAvailabilityAdapter implements CommodityAvailabilityPort {

	private final MarketDirectory directory;

	private final ObservationReader reader;

	private final FreshnessPolicyPort freshnessPolicy;

	private final AgrinexCalendar calendar;

	CommodityAvailabilityAdapter(MarketDirectory directory, ObservationReader reader,
			FreshnessPolicyPort freshnessPolicy, AgrinexCalendar calendar) {
		this.directory = directory;
		this.reader = reader;
		this.freshnessPolicy = freshnessPolicy;
		this.calendar = calendar;
	}

	@Override
	@Transactional(readOnly = true)
	public Set<UUID> commoditiesWithRecentObservations() {
		FreshnessThresholds thresholds = this.freshnessPolicy.activeThresholds();
		LocalDate today = this.calendar.today();
		List<UUID> candidates = this.directory.comparisonCandidates().stream().map(MarketInfo::id).toList();
		return this.reader.commodityIdsObserved(candidates, today.minusDays(thresholds.staleDays()), today);
	}

}
