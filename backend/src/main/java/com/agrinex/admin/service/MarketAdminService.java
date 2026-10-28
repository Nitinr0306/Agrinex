package com.agrinex.admin.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.agrinex.admin.domain.AuditAction;
import com.agrinex.admin.domain.AuditTarget;
import com.agrinex.common.error.ApiException;
import com.agrinex.common.web.PageParams;
import com.agrinex.common.web.PageResponse;
import com.agrinex.geography.GeographyApi;
import com.agrinex.price.MarketAliasView;
import com.agrinex.price.MarketCurationApi;
import com.agrinex.price.MarketCurationResult;
import com.agrinex.price.MarketInfo;
import com.agrinex.price.MarketReviewStatus;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Market curation with evidence (ADR-0014 item 4, ADR-0020 item 1): each change goes through the price module and
 * is audited in the same transaction with the details the price module returns.
 */
@Service
public class MarketAdminService {

	private final MarketCurationApi curation;

	private final GeographyApi geography;

	private final AdminAuditLog audit;

	MarketAdminService(MarketCurationApi curation, GeographyApi geography, AdminAuditLog audit) {
		this.curation = curation;
		this.geography = geography;
		this.audit = audit;
	}

	/** An unknown districtId answers 422 REFERENCE_NOT_FOUND (contract DistrictIdQuery). */
	public PageResponse<MarketInfo> list(MarketReviewStatus reviewStatus, Boolean active, UUID districtId,
			String search, PageParams page) {
		if (districtId != null && this.geography.findDistrict(districtId).isEmpty()) {
			throw ApiException.referenceNotFound("districtId");
		}
		return this.curation.listMarkets(reviewStatus, active, districtId, search, page);
	}

	public MarketDetail detail(UUID marketId) {
		MarketInfo market = this.curation.findMarket(marketId)
			.orElseThrow(() -> ApiException.notFound("The market does not exist."));
		return new MarketDetail(market, this.curation.aliases(marketId),
				this.curation.latestReportDate(marketId).orElse(null));
	}

	@Transactional
	public MarketInfo verify(UUID actorUserId, UUID marketId) {
		return audited(actorUserId, AuditAction.MARKET_VERIFIED, this.curation.verify(marketId));
	}

	@Transactional
	public MarketInfo merge(UUID actorUserId, UUID marketId, UUID targetMarketId, String evidenceNote) {
		return audited(actorUserId, AuditAction.MARKET_MERGED,
				this.curation.merge(marketId, targetMarketId, evidenceNote));
	}

	@Transactional
	public MarketInfo deactivate(UUID actorUserId, UUID marketId) {
		return audited(actorUserId, AuditAction.MARKET_DEACTIVATED, this.curation.deactivate(marketId));
	}

	@Transactional
	public MarketInfo reactivate(UUID actorUserId, UUID marketId) {
		return audited(actorUserId, AuditAction.MARKET_REACTIVATED, this.curation.reactivate(marketId));
	}

	@Transactional
	public MarketInfo setCoordinates(UUID actorUserId, UUID marketId, BigDecimal lat, BigDecimal lon,
			String coordinateSource) {
		return audited(actorUserId, AuditAction.MARKET_COORDINATES_SET,
				this.curation.setCoordinates(marketId, lat, lon, coordinateSource));
	}

	private MarketInfo audited(UUID actorUserId, AuditAction action, MarketCurationResult result) {
		this.audit.record(actorUserId, action, AuditTarget.MARKET, result.market().id(), result.auditDetails());
		return result.market();
	}

	/** A market with its source aliases and newest stored report date (contract AdminMarketDetailResponse). */
	public record MarketDetail(MarketInfo market, List<MarketAliasView> aliases, LocalDate latestReportDate) {
	}

}
