package com.agrinex.price;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.agrinex.common.web.PageParams;
import com.agrinex.common.web.PageResponse;

/**
 * Admin curation of markets (ADR-0014 item 4, ADR-0020 item 1), used only by the admin module, which audits each
 * change in the same transaction.
 */
public interface MarketCurationApi {

	PageResponse<MarketInfo> listMarkets(MarketReviewStatus reviewStatus, Boolean active, UUID districtId,
			String search, PageParams page);

	Optional<MarketInfo> findMarket(UUID marketId);

	List<MarketAliasView> aliases(UUID marketId);

	Optional<LocalDate> latestReportDate(UUID marketId);

	MarketCurationResult verify(UUID marketId);

	MarketCurationResult merge(UUID marketId, UUID targetMarketId, String evidenceNote);

	MarketCurationResult deactivate(UUID marketId);

	MarketCurationResult reactivate(UUID marketId);

	MarketCurationResult setCoordinates(UUID marketId, BigDecimal lat, BigDecimal lon, String coordinateSource);

}
