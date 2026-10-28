package com.agrinex.admin.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.agrinex.geography.DistrictRef;
import com.agrinex.geography.StateRef;
import com.agrinex.price.MarketInfo;
import com.agrinex.price.MarketReviewStatus;

/** Contract AdminMarketResponse: any market, PROVISIONAL and MERGED ones included (ADR-0014). */
public record AdminMarketResponse(UUID id, String name, String slug, DistrictRef district, StateRef state,
		MarketReviewStatus reviewStatus, boolean active, UUID mergedIntoMarketId, MarketCoordinates coordinates,
		Instant createdAt) {

	public static AdminMarketResponse from(MarketInfo market) {
		return new AdminMarketResponse(market.id(), market.name(), market.slug(), market.district().ref(),
				market.district().state(), market.reviewStatus(), market.active(), market.mergedIntoMarketId(),
				MarketCoordinates.of(market), market.createdAt());
	}

	/** Contract MarketCoordinates; null when the market has none. */
	public record MarketCoordinates(BigDecimal lat, BigDecimal lon, boolean approximate, String source) {

		static MarketCoordinates of(MarketInfo market) {
			return market.hasCoordinates() ? new MarketCoordinates(market.lat(), market.lon(),
					market.coordinateApproximate(), market.coordinateSource()) : null;
		}

	}

}
