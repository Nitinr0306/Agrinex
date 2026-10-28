package com.agrinex.price;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.agrinex.common.web.Ref;
import com.agrinex.geography.DistrictInfo;

/** A market with its district, coordinates and curation state (table markets; ADR-0014). */
public record MarketInfo(UUID id, String name, String slug, DistrictInfo district, BigDecimal lat, BigDecimal lon,
		boolean coordinateApproximate, String coordinateSource, boolean active, MarketReviewStatus reviewStatus,
		UUID mergedIntoMarketId, Instant createdAt) {

	public Ref ref() {
		return new Ref(this.id, this.name);
	}

	public MarketSummary summary() {
		return new MarketSummary(this.id, this.name, this.district.ref(), this.district.state());
	}

	public boolean hasCoordinates() {
		return this.lat != null && this.lon != null;
	}

	/** A comparison candidate: VERIFIED, active and with coordinates (ADR-0014 item 5). */
	public boolean comparisonCandidate() {
		return this.reviewStatus == MarketReviewStatus.VERIFIED && this.active && hasCoordinates();
	}

}
