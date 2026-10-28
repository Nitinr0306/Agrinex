package com.agrinex.price.service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.agrinex.geography.DistrictInfo;
import com.agrinex.price.FreshnessPolicyPort;
import com.agrinex.price.FreshnessThresholds;
import com.agrinex.price.LoadResult;
import com.agrinex.price.MarketCommodity;
import com.agrinex.price.MarketFreshnessSummary;
import com.agrinex.price.MarketInfo;
import com.agrinex.price.MarketRegistration;
import com.agrinex.price.ObservationCandidate;
import com.agrinex.price.PriceApi;
import com.agrinex.price.SelectedObservation;
import com.agrinex.price.SourceDataset;
import com.agrinex.price.SourceDatasetInfo;
import com.agrinex.price.TrendStatistic;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** The price module's public interface, delegating to its cohesive services. */
@Service
@Transactional(readOnly = true)
class PriceFacade implements PriceApi {

	private final SourceDatasetCatalog datasets;

	private final MarketDirectory directory;

	private final ObservationReader reader;

	private final ObservationLoader loader;

	private final MarketRegistry registry;

	private final MarketFreshnessReporter freshnessReporter;

	private final FreshnessPolicyPort freshnessPolicy;

	PriceFacade(SourceDatasetCatalog datasets, MarketDirectory directory, ObservationReader reader,
			ObservationLoader loader, MarketRegistry registry, MarketFreshnessReporter freshnessReporter,
			FreshnessPolicyPort freshnessPolicy) {
		this.datasets = datasets;
		this.directory = directory;
		this.reader = reader;
		this.loader = loader;
		this.registry = registry;
		this.freshnessReporter = freshnessReporter;
		this.freshnessPolicy = freshnessPolicy;
	}

	@Override
	public List<SourceDatasetInfo> sourceDatasets() {
		return this.datasets.all();
	}

	@Override
	public SourceDatasetInfo sourceDataset(SourceDataset type) {
		return this.datasets.byType(type);
	}

	@Override
	public FreshnessThresholds activeFreshnessThresholds() {
		return this.freshnessPolicy.activeThresholds();
	}

	@Override
	public Optional<MarketInfo> findMarket(UUID marketId) {
		return this.directory.find(marketId);
	}

	@Override
	public Map<UUID, MarketInfo> findMarkets(Collection<UUID> marketIds) {
		return this.directory.findAll(marketIds);
	}

	@Override
	public List<MarketInfo> comparisonCandidates() {
		return this.directory.comparisonCandidates();
	}

	@Override
	public List<MarketInfo> verifiedActiveMarkets() {
		return this.directory.verifiedActive();
	}

	@Override
	public Map<UUID, SelectedObservation> selectLatest(Collection<UUID> marketIds, UUID commodityId, UUID varietyId,
			UUID gradeId, LocalDate today, int staleDays) {
		return this.reader.selectLatest(marketIds, commodityId, varietyId, gradeId, today, staleDays);
	}

	@Override
	public TrendStatistic trend(UUID marketId, UUID commodityId, UUID varietyId, UUID gradeId, LocalDate today) {
		return this.reader.trend(marketId, commodityId, varietyId, gradeId, today);
	}

	@Override
	public Map<UUID, TrendStatistic> trends(Collection<UUID> marketIds, UUID commodityId, UUID varietyId,
			UUID gradeId, LocalDate today) {
		return this.reader.trends(marketIds, commodityId, varietyId, gradeId, today);
	}

	@Override
	public Map<UUID, SelectedObservation> observations(Collection<UUID> observationIds) {
		return this.reader.byIds(observationIds);
	}

	@Override
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public MarketRegistration resolveOrRegisterMarket(String sourceStateName, String sourceDistrictName,
			String sourceMarketName, String sourceMarketNameRaw, DistrictInfo district, UUID ingestionRunId) {
		return this.registry.resolveOrRegister(sourceStateName, sourceDistrictName, sourceMarketName,
				sourceMarketNameRaw, district, ingestionRunId);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public LoadResult loadObservations(UUID ingestionRunId, UUID sourceDatasetId,
			List<ObservationCandidate> candidates) {
		return this.loader.load(ingestionRunId, sourceDatasetId, candidates);
	}

	@Override
	public Set<MarketCommodity> changedByRun(UUID ingestionRunId) {
		Set<MarketCommodity> changed = this.reader.pairsChangedByRun(ingestionRunId);
		Set<UUID> eligibleMarkets = this.directory.verifiedActive()
			.stream()
			.map(MarketInfo::id)
			.collect(Collectors.toSet());
		return changed.stream()
			.filter((pair) -> eligibleMarkets.contains(pair.marketId()))
			.collect(Collectors.toUnmodifiableSet());
	}

	@Override
	public Optional<LocalDate> latestReportDate(UUID sourceDatasetId) {
		return this.freshnessReporter.latestReportDateOfDataset(sourceDatasetId);
	}

	@Override
	public MarketFreshnessSummary marketFreshness(LocalDate today, FreshnessThresholds thresholds) {
		return this.freshnessReporter.summary(today, thresholds);
	}

}
