package com.agrinex.price.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.agrinex.commodity.CommodityRef;
import com.agrinex.common.web.Ref;
import com.agrinex.price.service.ForecastQueryService.ForecastSeriesView;
import com.agrinex.price.service.ForecastQueryService.ForecastView;

/**
 * Contract ForecastResponse (spec section 29, 35; ADR-0030). Every value is a model prediction
 * ({@code kind: ML_FORECAST}) built on the representative daily series, never a reported price; there is no
 * confidence percentage, only the bounds the model produced.
 */
public record ForecastResponse(boolean available, String kind, String inputBasis, Ref market, CommodityRef commodity,
		String priceUnit, List<ForecastSeries> forecasts) {

	static final String KIND = "ML_FORECAST";

	static final String INPUT_BASIS = "REPRESENTATIVE_DAILY_SERIES";

	public static ForecastResponse from(ForecastView view) {
		return new ForecastResponse(view.available(), KIND, INPUT_BASIS, view.market(), view.commodity().ref(),
				view.commodity().priceUnit(), view.series().stream().map(ForecastSeries::of).toList());
	}

	/** Contract ForecastSeries: the latest generation of one horizon. */
	public record ForecastSeries(int forecastHorizonDays, String modelName, String modelVersion, Instant generatedAt,
			LocalDate trainingDataThrough, BigDecimal mae, BigDecimal rmse, List<ForecastPoint> forecast) {

		static ForecastSeries of(ForecastSeriesView view) {
			return new ForecastSeries(view.forecastHorizonDays(), view.modelName(), view.modelVersion(),
					view.generatedAt(), view.trainingDataThrough(), view.mae(), view.rmse(),
					view.points()
						.stream()
						.map((point) -> new ForecastPoint(point.date(), point.predictedPrice(), point.lowerBound(),
								point.upperBound()))
						.toList());
		}

	}

	/** Contract ForecastPointResponse. */
	public record ForecastPoint(LocalDate date, BigDecimal predictedPrice, BigDecimal lowerBound,
			BigDecimal upperBound) {
	}

}
