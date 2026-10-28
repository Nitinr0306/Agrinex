package com.agrinex.price.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.agrinex.commodity.CommodityApi;
import com.agrinex.commodity.CommodityInfo;
import com.agrinex.commodity.NamedRef;
import com.agrinex.common.error.ApiException;
import com.agrinex.common.error.FieldErrorCode;
import com.agrinex.common.time.AgrinexCalendar;
import com.agrinex.common.web.Ref;
import com.agrinex.price.FreshnessPolicyPort;
import com.agrinex.price.FreshnessStatus;
import com.agrinex.price.FreshnessThresholds;
import com.agrinex.price.MarketInfo;
import com.agrinex.price.MarketReviewStatus;
import com.agrinex.price.PriceQuote;
import com.agrinex.price.SelectedObservation;
import com.agrinex.price.SourceSyncStatusPort;
import com.agrinex.price.TrendStatistic;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Market detail and trend for the public market operations (spec section 30; ADR-0010, ADR-0014, ADR-0025). Only
 * VERIFIED markets are visible here; freshness uses the active thresholds (ADR-0035 item 1b), and every response
 * carries the source synchronization status (ADR-0022 item 9).
 */
@Service
@Transactional(readOnly = true)
public class MarketQueryService {

	public static final int RECENT_FROM_DAYS_BACK = 6;

	public static final int BASELINE_FROM_DAYS_BACK = 13;

	public static final int BASELINE_TO_DAYS_BACK = 7;

	private final MarketDirectory directory;

	private final ObservationReader reader;

	private final CommodityApi commodities;

	private final FreshnessPolicyPort freshnessPolicy;

	private final SourceSyncStatusPort syncStatus;

	private final AgrinexCalendar calendar;

	MarketQueryService(MarketDirectory directory, ObservationReader reader, CommodityApi commodities,
			FreshnessPolicyPort freshnessPolicy, SourceSyncStatusPort syncStatus, AgrinexCalendar calendar) {
		this.directory = directory;
		this.reader = reader;
		this.commodities = commodities;
		this.freshnessPolicy = freshnessPolicy;
		this.syncStatus = syncStatus;
		this.calendar = calendar;
	}

	public MarketDetailView marketDetail(UUID marketId, UUID commodityId, UUID varietyId, UUID gradeId) {
		if (commodityId == null && (varietyId != null || gradeId != null)) {
			throw ApiException.validation("commodityId", FieldErrorCode.REQUIRED,
					"is required when varietyId or gradeId is given");
		}
		MarketInfo market = verifiedMarket(marketId);
		LocalDate today = this.calendar.today();
		FreshnessThresholds thresholds = this.freshnessPolicy.activeThresholds();
		List<SelectedObservation> selected;
		if (commodityId != null) {
			Filter filter = resolveFilter(commodityId, varietyId, gradeId);
			selected = List.copyOf(this.reader
				.selectLatest(List.of(marketId), filter.commodity().id(), varietyId, gradeId, today,
						thresholds.staleDays())
				.values());
		}
		else {
			selected = this.reader.latestPerCommodity(marketId, today, thresholds.staleDays());
		}
		Set<UUID> commodityIds = selected.stream().map(SelectedObservation::commodityId).collect(Collectors.toSet());
		Map<UUID, CommodityInfo> commodityInfo = this.commodities.findCommodities(commodityIds);
		List<LatestPriceView> latest = selected.stream()
			.map((observation) -> new LatestPriceView(commodityInfo.get(observation.commodityId()),
					quote(observation, today, thresholds)))
			.sorted(Comparator.comparing((LatestPriceView view) -> view.commodity().name(), String::compareTo)
				.thenComparing((view) -> view.commodity().id()))
			.toList();
		return new MarketDetailView(market, latest, this.syncStatus.currentDatasetStatus());
	}

	public TrendView marketTrend(UUID marketId, UUID commodityId, int days, UUID varietyId, UUID gradeId) {
		MarketInfo market = verifiedMarket(marketId);
		Filter filter = resolveFilter(commodityId, varietyId, gradeId);
		LocalDate today = this.calendar.today();
		FreshnessThresholds thresholds = this.freshnessPolicy.activeThresholds();
		TrendStatistic statistic = this.reader.trend(marketId, commodityId, varietyId, gradeId, today);
		List<PriceQuote> observations = this.reader
			.dailySeries(marketId, commodityId, varietyId, gradeId, today.minusDays(days - 1L), today)
			.stream()
			.map((observation) -> quote(observation, today, thresholds))
			.toList();
		TrendWindowView window = new TrendWindowView(today.minusDays(days - 1L), today,
				today.minusDays(RECENT_FROM_DAYS_BACK), today, today.minusDays(BASELINE_FROM_DAYS_BACK),
				today.minusDays(BASELINE_TO_DAYS_BACK));
		return new TrendView(market.ref(), filter.commodity(), filter.variety(), filter.grade(), days, window,
				statistic, observations, this.syncStatus.currentDatasetStatus());
	}

	private MarketInfo verifiedMarket(UUID marketId) {
		return this.directory.find(marketId)
			.filter((market) -> market.reviewStatus() == MarketReviewStatus.VERIFIED)
			.orElseThrow(() -> ApiException.notFound("The market does not exist."));
	}

	/** Resolves the commodity and the optional exact variety and grade filters (422 when they do not qualify). */
	private Filter resolveFilter(UUID commodityId, UUID varietyId, UUID gradeId) {
		CommodityInfo commodity = this.commodities.findCommodity(commodityId)
			.orElseThrow(() -> ApiException.referenceNotFound("commodityId"));
		Ref variety = (varietyId != null) ? this.commodities.findVariety(varietyId)
			.filter((ref) -> ref.commodityId().equals(commodityId))
			.map(MarketQueryService::ref)
			.orElseThrow(() -> ApiException.referenceNotFound("varietyId")) : null;
		Ref grade = (gradeId != null) ? this.commodities.findGrade(gradeId)
			.filter((ref) -> ref.commodityId().equals(commodityId))
			.map(MarketQueryService::ref)
			.orElseThrow(() -> ApiException.referenceNotFound("gradeId")) : null;
		return new Filter(commodity, variety, grade);
	}

	private static Ref ref(NamedRef named) {
		return new Ref(named.id(), named.name());
	}

	private static PriceQuote quote(SelectedObservation observation, LocalDate today, FreshnessThresholds thresholds) {
		return PriceQuote.of(observation, FreshnessStatus.classify(observation.reportDate(), today, thresholds));
	}

	private record Filter(CommodityInfo commodity, Ref variety, Ref grade) {
	}

	public record LatestPriceView(CommodityInfo commodity, PriceQuote price) {
	}

	public record MarketDetailView(MarketInfo market, List<LatestPriceView> latestPrices,
			com.agrinex.price.SourceSyncStatus sourceStatus) {
	}

	public record TrendWindowView(LocalDate chartFrom, LocalDate chartTo, LocalDate recentFrom, LocalDate recentTo,
			LocalDate baselineFrom, LocalDate baselineTo) {
	}

	public record TrendView(Ref market, CommodityInfo commodity, Ref variety, Ref grade, int days,
			TrendWindowView window, TrendStatistic statistic, List<PriceQuote> observations,
			com.agrinex.price.SourceSyncStatus sourceStatus) {
	}

}
