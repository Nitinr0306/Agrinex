package com.agrinex.price.web;

import java.util.UUID;

import com.agrinex.common.error.ApiException;
import com.agrinex.common.error.FieldErrorCode;
import com.agrinex.price.service.ForecastQueryService;
import com.agrinex.price.service.MarketQueryService;
import com.agrinex.price.service.MarketQueryService.MarketDetailView;
import com.agrinex.price.service.MarketQueryService.TrendView;
import com.agrinex.price.web.dto.ForecastResponse;
import com.agrinex.price.web.dto.MarketDetailResponse;
import com.agrinex.price.web.dto.TrendResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Public market operations (contract tag Markets; price module, spec section 30). Anonymous. */
@RestController
@RequestMapping("/api/v1/markets")
class MarketController {

	private final MarketQueryService queries;

	private final ForecastQueryService forecasts;

	MarketController(MarketQueryService queries, ForecastQueryService forecasts) {
		this.queries = queries;
		this.forecasts = forecasts;
	}

	@GetMapping("/{marketId}")
	MarketDetailResponse getMarketDetail(@PathVariable UUID marketId,
			@RequestParam(required = false) UUID commodityId, @RequestParam(required = false) UUID varietyId,
			@RequestParam(required = false) UUID gradeId) {
		MarketDetailView view = this.queries.marketDetail(marketId, commodityId, varietyId, gradeId);
		return MarketDetailResponse.from(view);
	}

	@GetMapping("/{marketId}/trends")
	TrendResponse getMarketTrend(@PathVariable UUID marketId, @RequestParam UUID commodityId,
			@RequestParam Integer days, @RequestParam(required = false) UUID varietyId,
			@RequestParam(required = false) UUID gradeId) {
		if (days != 7 && days != 30) {
			throw ApiException.validation("days", FieldErrorCode.INVALID_VALUE, "must be 7 or 30");
		}
		TrendView view = this.queries.marketTrend(marketId, commodityId, days, varietyId, gradeId);
		return TrendResponse.from(view);
	}

	/** Optional ML track (ADR-0030): stored forecasts only; available false without them, never a 5xx. */
	@GetMapping("/{marketId}/forecast")
	ForecastResponse getMarketForecast(@PathVariable UUID marketId, @RequestParam UUID commodityId) {
		return ForecastResponse.from(this.forecasts.marketForecast(marketId, commodityId));
	}

}
