package com.agrinex.price.web.dto;

import java.util.List;

import com.agrinex.price.MarketInfo;
import com.agrinex.price.MarketSummary;
import com.agrinex.price.SourceSyncStatus;
import com.agrinex.price.service.MarketQueryService.MarketDetailView;

/**
 * Contract MarketDetailResponse. {@code noDataReason} is NO_RECENT_PRICE_DATA exactly when no commodity has an
 * observation within the active stale window (spec section 29).
 */
public record MarketDetailResponse(MarketSummary market, boolean active, boolean approximateMarketLocation,
		List<MarketLatestPrice> latestPrices, String noDataReason, SourceSyncStatus sourceStatus) {

	public static final String NO_RECENT_PRICE_DATA = "NO_RECENT_PRICE_DATA";

	public static MarketDetailResponse from(MarketDetailView view) {
		MarketInfo market = view.market();
		List<MarketLatestPrice> latest = view.latestPrices()
			.stream()
			.map((price) -> new MarketLatestPrice(price.commodity().ref(), price.price()))
			.toList();
		return new MarketDetailResponse(market.summary(), market.active(), market.coordinateApproximate(), latest,
				latest.isEmpty() ? NO_RECENT_PRICE_DATA : null, view.sourceStatus());
	}

}
