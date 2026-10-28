package com.agrinex.admin.web.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.agrinex.admin.service.MarketAdminService.MarketDetail;
import com.agrinex.admin.web.dto.AdminMarketResponse.MarketCoordinates;
import com.agrinex.geography.DistrictRef;
import com.agrinex.geography.StateRef;
import com.agrinex.price.MarketAliasView;
import com.agrinex.price.MarketReviewStatus;

/** Contract AdminMarketDetailResponse: the market fields, its source aliases and its newest report date. */
public record AdminMarketDetailResponse(UUID id, String name, String slug, DistrictRef district, StateRef state,
		MarketReviewStatus reviewStatus, boolean active, UUID mergedIntoMarketId, MarketCoordinates coordinates,
		Instant createdAt, List<MarketSourceAlias> aliases, LocalDate latestReportDate) {

	public static AdminMarketDetailResponse from(MarketDetail detail) {
		AdminMarketResponse market = AdminMarketResponse.from(detail.market());
		return new AdminMarketDetailResponse(market.id(), market.name(), market.slug(), market.district(),
				market.state(), market.reviewStatus(), market.active(), market.mergedIntoMarketId(),
				market.coordinates(), market.createdAt(),
				detail.aliases().stream().map(MarketSourceAlias::of).toList(), detail.latestReportDate());
	}

	/** Contract MarketSourceAlias (spec section 47 Q4). */
	public record MarketSourceAlias(String sourceStateName, String sourceDistrictName, String sourceMarketName,
			String sourceMarketNameRaw, String evidenceNote, Instant createdAt) {

		static MarketSourceAlias of(MarketAliasView alias) {
			return new MarketSourceAlias(alias.sourceStateName(), alias.sourceDistrictName(), alias.sourceMarketName(),
					alias.sourceMarketNameRaw(), alias.evidenceNote(), alias.createdAt());
		}

	}

}
