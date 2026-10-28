package com.agrinex.price.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.agrinex.commodity.CommodityApi;
import com.agrinex.commodity.CommodityInfo;
import com.agrinex.common.error.ApiException;
import com.agrinex.common.time.AgrinexCalendar;
import com.agrinex.common.web.Ref;
import com.agrinex.price.MarketReviewStatus;
import com.agrinex.price.domain.ForecastEntity;
import com.agrinex.price.repository.ForecastRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The optional forecast operation (spec section 35; ADR-0030 item 4): it reads stored forecasts only and never calls
 * the ML job. For each horizon it returns the latest generation's points dated today or later (Asia/Kolkata); with
 * none, the forecast is unavailable, which is the normal state without ML.
 */
@Service
@Transactional(readOnly = true)
public class ForecastQueryService {

	private final MarketDirectory markets;

	private final CommodityApi commodities;

	private final ForecastRepository forecasts;

	private final AgrinexCalendar calendar;

	ForecastQueryService(MarketDirectory markets, CommodityApi commodities, ForecastRepository forecasts,
			AgrinexCalendar calendar) {
		this.markets = markets;
		this.commodities = commodities;
		this.forecasts = forecasts;
		this.calendar = calendar;
	}

	public ForecastView marketForecast(UUID marketId, UUID commodityId) {
		Ref market = this.markets.find(marketId)
			.filter((candidate) -> candidate.reviewStatus() == MarketReviewStatus.VERIFIED)
			.map((candidate) -> candidate.ref())
			.orElseThrow(() -> ApiException.notFound("The market does not exist."));
		CommodityInfo commodity = this.commodities.findCommodity(commodityId)
			.orElseThrow(() -> ApiException.referenceNotFound("commodityId"));
		List<ForecastEntity> points = this.forecasts.findPointsFrom(marketId, commodityId, this.calendar.today());
		return new ForecastView(market, commodity, latestGenerationPerHorizon(points));
	}

	/** Points arrive ordered by horizon, newest generation first, then date; keeps the newest generation of each. */
	private static List<ForecastSeriesView> latestGenerationPerHorizon(List<ForecastEntity> points) {
		Map<Integer, List<ForecastEntity>> byHorizon = new LinkedHashMap<>();
		for (ForecastEntity point : points) {
			List<ForecastEntity> series = byHorizon.computeIfAbsent(point.getForecastHorizonDays(),
					(horizon) -> new ArrayList<>());
			if (series.isEmpty() || series.getFirst().getGeneratedAt().equals(point.getGeneratedAt())) {
				series.add(point);
			}
		}
		return byHorizon.values().stream().map(ForecastQueryService::series).toList();
	}

	private static ForecastSeriesView series(List<ForecastEntity> generation) {
		ForecastEntity first = generation.getFirst();
		return new ForecastSeriesView(first.getForecastHorizonDays(), first.getModelName(), first.getModelVersion(),
				first.getGeneratedAt(), first.getTrainingDataThrough(), first.getMae(), first.getRmse(),
				generation.stream()
					.map((point) -> new ForecastPointView(point.getForecastDate(), point.getPredictedPrice(),
							point.getLowerBound(), point.getUpperBound()))
					.toList());
	}

	/** A market's forecast for a commodity; available exactly when at least one horizon has points. */
	public record ForecastView(Ref market, CommodityInfo commodity, List<ForecastSeriesView> series) {

		public boolean available() {
			return !this.series.isEmpty();
		}

	}

	/** The latest generation of one horizon (contract ForecastSeries). */
	public record ForecastSeriesView(int forecastHorizonDays, String modelName, String modelVersion,
			Instant generatedAt, LocalDate trainingDataThrough, BigDecimal mae, BigDecimal rmse,
			List<ForecastPointView> points) {
	}

	/** One predicted point (contract ForecastPointResponse). */
	public record ForecastPointView(LocalDate date, BigDecimal predictedPrice, BigDecimal lowerBound,
			BigDecimal upperBound) {
	}

}
