package com.agrinex.price.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.agrinex.price.FreshnessStatus;
import com.agrinex.price.FreshnessThresholds;
import com.agrinex.price.MarketFreshnessSummary;
import com.agrinex.price.MarketInfo;
import com.agrinex.price.repository.MarketLatestDate;
import com.agrinex.price.repository.PriceObservationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Data-health figures of the price module for the admin dashboard (FR-18; spec section 46). */
@Service
@Transactional(readOnly = true)
class MarketFreshnessReporter {

	private final MarketDirectory directory;

	private final PriceObservationRepository observations;

	MarketFreshnessReporter(MarketDirectory directory, PriceObservationRepository observations) {
		this.directory = directory;
		this.observations = observations;
	}

	/**
	 * Each VERIFIED, active market classified by its latest observation date across commodities; markets without
	 * observations count as unavailable (contract MarketFreshnessSummary).
	 */
	public MarketFreshnessSummary summary(LocalDate today, FreshnessThresholds thresholds) {
		List<UUID> marketIds = this.directory.verifiedActive().stream().map(MarketInfo::id).toList();
		Map<UUID, LocalDate> latest = marketIds.isEmpty() ? Map.of()
				: this.observations.findLatestReportDates(marketIds)
					.stream()
					.collect(Collectors.toMap(MarketLatestDate::marketId, MarketLatestDate::latestReportDate));
		Map<FreshnessStatus, Long> counts = marketIds.stream()
			.map((id) -> Optional.ofNullable(latest.get(id))
				.map((date) -> FreshnessStatus.classify(date, today, thresholds))
				.orElse(FreshnessStatus.UNAVAILABLE))
			.collect(Collectors.groupingBy((status) -> status, Collectors.counting()));
		return new MarketFreshnessSummary(today, marketIds.size(), counts.getOrDefault(FreshnessStatus.FRESH, 0L),
				counts.getOrDefault(FreshnessStatus.RECENT, 0L), counts.getOrDefault(FreshnessStatus.STALE, 0L),
				counts.getOrDefault(FreshnessStatus.UNAVAILABLE, 0L));
	}

	public Optional<LocalDate> latestReportDateOfDataset(UUID sourceDatasetId) {
		return this.observations.findLatestReportDateOfDataset(sourceDatasetId);
	}

}
