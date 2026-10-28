package com.agrinex.price;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.agrinex.geography.DistrictInfo;

/**
 * Public interface of the price module, owner of source datasets, markets and their aliases, price observations
 * and their revisions, and the optional forecasts (ADR-0013, ADR-0030). It depends on geography and commodity.
 */
public interface PriceApi {

	List<SourceDatasetInfo> sourceDatasets();

	SourceDatasetInfo sourceDataset(SourceDataset type);

	/** Through the port implemented by recommendation (ADR-0035 items 1b and 3). */
	FreshnessThresholds activeFreshnessThresholds();

	Optional<MarketInfo> findMarket(UUID marketId);

	Map<UUID, MarketInfo> findMarkets(Collection<UUID> marketIds);

	/** VERIFIED, active markets with coordinates: the comparison candidates (ADR-0014 item 5). */
	List<MarketInfo> comparisonCandidates();

	/** VERIFIED, active markets: the markets alerts are evaluated over (ADR-0019 item 1). */
	List<MarketInfo> verifiedActiveMarkets();

	/**
	 * For each market, the observation of the commodity selected by ADR-0010 within the window
	 * {@code today - staleDays .. today}; markets without a qualifying observation are absent from the result.
	 */
	Map<UUID, SelectedObservation> selectLatest(Collection<UUID> marketIds, UUID commodityId, UUID varietyId,
			UUID gradeId, LocalDate today, int staleDays);

	/** The trend statistic of ADR-0025 on the daily series (ADR-0010 rule 7) ending {@code today}. */
	TrendStatistic trend(UUID marketId, UUID commodityId, UUID varietyId, UUID gradeId, LocalDate today);

	/**
	 * {@link #trend} for several markets, read in one query: every given market is in the result, with
	 * INSUFFICIENT_DATA when it has no series.
	 */
	Map<UUID, TrendStatistic> trends(Collection<UUID> marketIds, UUID commodityId, UUID varietyId, UUID gradeId,
			LocalDate today);

	/** The current values of stored observations, as selected rows with provenance. */
	Map<UUID, SelectedObservation> observations(Collection<UUID> observationIds);

	/** Resolves a source market spelling through its alias, registering a PROVISIONAL market on first sight. */
	MarketRegistration resolveOrRegisterMarket(String sourceStateName, String sourceDistrictName,
			String sourceMarketName, String sourceMarketNameRaw, DistrictInfo district, UUID ingestionRunId);

	/** Loads one delivery unit in the caller's transaction (ADR-0009 item 3, ADR-0011 item 7). */
	LoadResult loadObservations(UUID ingestionRunId, UUID sourceDatasetId, List<ObservationCandidate> candidates);

	/** Market and commodity pairs with an observation the run inserted or revised, in VERIFIED, active markets. */
	Set<MarketCommodity> changedByRun(UUID ingestionRunId);

	Optional<LocalDate> latestReportDate(UUID sourceDatasetId);

	MarketFreshnessSummary marketFreshness(LocalDate today, FreshnessThresholds thresholds);

}
