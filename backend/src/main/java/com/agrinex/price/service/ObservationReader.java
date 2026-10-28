package com.agrinex.price.service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.agrinex.commodity.CommodityApi;
import com.agrinex.commodity.NamedRef;
import com.agrinex.price.MarketCommodity;
import com.agrinex.price.SelectedObservation;
import com.agrinex.price.TrendStatistic;
import com.agrinex.price.domain.ObservationRow;
import com.agrinex.price.domain.ObservationSelection;
import com.agrinex.price.domain.PriceObservationEntity;
import com.agrinex.price.domain.TrendCalculator;
import com.agrinex.price.mapper.PriceMapper;
import com.agrinex.price.repository.PriceObservationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reads stored observations and applies the selection rules of ADR-0010 and the trend statistic of ADR-0025. The
 * variety and grade names used by the tie-break come from the commodity module (price -> commodity); no table of
 * another module is read.
 */
@Service
@Transactional(readOnly = true)
class ObservationReader {

	private final PriceObservationRepository observations;

	private final SourceDatasetCatalog datasets;

	private final CommodityApi commodities;

	ObservationReader(PriceObservationRepository observations, SourceDatasetCatalog datasets, CommodityApi commodities) {
		this.observations = observations;
		this.datasets = datasets;
		this.commodities = commodities;
	}

	/** For each market, the selected observation of the latest report date within {@code today - staleDays .. today}. */
	public Map<UUID, SelectedObservation> selectLatest(Collection<UUID> marketIds, UUID commodityId, UUID varietyId,
			UUID gradeId, LocalDate today, int staleDays) {
		if (marketIds.isEmpty()) {
			return Map.of();
		}
		List<ObservationRow> candidates = toRows(this.observations.findCandidates(marketIds, commodityId,
				today.minusDays(staleDays), today, varietyId, gradeId));
		Map<UUID, SelectedObservation> selected = new LinkedHashMap<>();
		ObservationSelection.latestPerMarket(candidates)
			.forEach((marketId, row) -> selected.put(marketId, PriceMapper.toSelected(row)));
		return selected;
	}

	/** One selected observation per date with data in {@code fromDate .. toDate}, ascending (ADR-0010 rule 7). */
	public List<SelectedObservation> dailySeries(UUID marketId, UUID commodityId, UUID varietyId, UUID gradeId,
			LocalDate fromDate, LocalDate toDate) {
		return dailyRows(marketId, commodityId, varietyId, gradeId, fromDate, toDate).stream()
			.map(PriceMapper::toSelected)
			.toList();
	}

	public TrendStatistic trend(UUID marketId, UUID commodityId, UUID varietyId, UUID gradeId, LocalDate today) {
		List<ObservationRow> series = dailyRows(marketId, commodityId, varietyId, gradeId,
				TrendCalculator.spanStart(today), today);
		return TrendCalculator.compute(series, today);
	}

	/**
	 * {@link #trend} for several markets from one read: the candidates of all markets are fetched together and
	 * split by market before the daily series is selected, so each market's result equals {@link #trend}. A
	 * comparison needs one trend per option; reading them one by one cost three queries per option (Task 12).
	 */
	public Map<UUID, TrendStatistic> trends(Collection<UUID> marketIds, UUID commodityId, UUID varietyId,
			UUID gradeId, LocalDate today) {
		if (marketIds.isEmpty()) {
			return Map.of();
		}
		Map<UUID, List<ObservationRow>> byMarket = toRows(this.observations.findCandidates(marketIds, commodityId,
				TrendCalculator.spanStart(today), today, varietyId, gradeId))
			.stream()
			.collect(Collectors.groupingBy(ObservationRow::marketId));
		Map<UUID, TrendStatistic> trends = new LinkedHashMap<>();
		for (UUID marketId : marketIds) {
			List<ObservationRow> series = ObservationSelection.dailySeries(byMarket.getOrDefault(marketId, List.of()));
			trends.put(marketId, TrendCalculator.compute(series, today));
		}
		return trends;
	}

	/** For one market, the selected observation of every commodity with data in the window (market detail). */
	public List<SelectedObservation> latestPerCommodity(UUID marketId, LocalDate today, int staleDays) {
		List<ObservationRow> rows = toRows(
				this.observations.findByMarketIdAndReportDateBetween(marketId, today.minusDays(staleDays), today));
		return rows.stream()
			.collect(Collectors.groupingBy(ObservationRow::commodityId))
			.values()
			.stream()
			.map(ObservationSelection::latestPerMarket)
			.map((perMarket) -> perMarket.get(marketId))
			.filter(Objects::nonNull)
			.map(PriceMapper::toSelected)
			.toList();
	}

	public Map<UUID, SelectedObservation> byIds(Collection<UUID> observationIds) {
		if (observationIds.isEmpty()) {
			return Map.of();
		}
		return toRows(this.observations.findAllById(observationIds)).stream()
			.collect(Collectors.toMap(ObservationRow::id, PriceMapper::toSelected));
	}

	public Set<UUID> commodityIdsObserved(Collection<UUID> marketIds, LocalDate fromDate, LocalDate toDate) {
		if (marketIds.isEmpty()) {
			return Set.of();
		}
		return this.observations.findCommodityIdsObserved(marketIds, fromDate, toDate);
	}

	/** Market and commodity pairs with an observation the run inserted or revised (ADR-0019 item 1). */
	public Set<MarketCommodity> pairsChangedByRun(UUID ingestionRunId) {
		Set<MarketCommodity> changed = new HashSet<>(this.observations.findPairsInsertedByRun(ingestionRunId));
		changed.addAll(this.observations.findPairsRevisedByRun(ingestionRunId));
		return changed;
	}

	private List<ObservationRow> dailyRows(UUID marketId, UUID commodityId, UUID varietyId, UUID gradeId,
			LocalDate fromDate, LocalDate toDate) {
		List<ObservationRow> candidates = toRows(this.observations.findCandidates(List.of(marketId), commodityId,
				fromDate, toDate, varietyId, gradeId));
		return ObservationSelection.dailySeries(candidates);
	}

	private List<ObservationRow> toRows(List<PriceObservationEntity> entities) {
		Set<UUID> varietyIds = entities.stream()
			.map(PriceObservationEntity::getVarietyId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
		Set<UUID> gradeIds = entities.stream()
			.map(PriceObservationEntity::getGradeId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
		Map<UUID, NamedRef> varieties = varietyIds.isEmpty() ? Map.of() : this.commodities.findVarieties(varietyIds);
		Map<UUID, NamedRef> grades = gradeIds.isEmpty() ? Map.of() : this.commodities.findGrades(gradeIds);
		return entities.stream()
			.map((entity) -> PriceMapper.toRow(entity, this.datasets.byId(entity.getSourceDatasetId()), varieties,
					grades))
			.toList();
	}

}
