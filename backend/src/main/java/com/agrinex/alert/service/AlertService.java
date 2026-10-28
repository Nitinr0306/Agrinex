package com.agrinex.alert.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.agrinex.alert.domain.AlertStatus;
import com.agrinex.alert.domain.PriceAlertEntity;
import com.agrinex.alert.mapper.AlertMapper;
import com.agrinex.alert.repository.PriceAlertRepository;
import com.agrinex.auth.AuthApi;
import com.agrinex.commodity.CommodityApi;
import com.agrinex.commodity.CommodityInfo;
import com.agrinex.common.error.ApiException;
import com.agrinex.common.error.ErrorCode;
import com.agrinex.common.web.PageParams;
import com.agrinex.common.web.PageResponse;
import com.agrinex.price.MarketInfo;
import com.agrinex.price.MarketReviewStatus;
import com.agrinex.price.PriceApi;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The caller's own price alerts (spec section 34; ADR-0016 item 6, ADR-0019). Alerts belong to the caller's farmer
 * profile; another user's alert answers 404. An ADMIN's profile is created on first use.
 */
@Service
@Transactional(readOnly = true)
public class AlertService {

	/** lastTriggeredAt descending, never-triggered last, then id (contract listAlerts). */
	static final Sort ALERT_ORDER = Sort.by(Sort.Order.desc("lastTriggeredAt").nullsLast(), Sort.Order.asc("id"));

	private final PriceAlertRepository alerts;

	private final AuthApi auth;

	private final CommodityApi commodities;

	private final PriceApi prices;

	AlertService(PriceAlertRepository alerts, AuthApi auth, CommodityApi commodities, PriceApi prices) {
		this.alerts = alerts;
		this.auth = auth;
		this.commodities = commodities;
		this.prices = prices;
	}

	public PageResponse<AlertView> list(UUID userId, AlertStatus status, PageParams page) {
		Optional<UUID> profileId = this.auth.findFarmerProfile(userId);
		if (profileId.isEmpty()) {
			return page.toResponse(List.of(), 0);
		}
		PageRequest request = PageRequest.of(page.page(), page.size(), ALERT_ORDER);
		Page<PriceAlertEntity> result = (status != null)
				? this.alerts.findByFarmerProfileIdAndStatus(profileId.get(), status, request)
				: this.alerts.findByFarmerProfileId(profileId.get(), request);
		return page.toResponse(views(result.getContent()), result.getTotalElements());
	}

	/**
	 * Creates an ACTIVE alert. 422 REFERENCE_NOT_FOUND for an unknown commodity or a market that is unknown or not
	 * VERIFIED (ADR-0014); 422 UNSUPPORTED_PRICE_UNIT when the commodity is not priced in Rs./Quintal (ADR-0008).
	 */
	@Transactional
	public AlertView create(UUID userId, NewAlert newAlert) {
		Optional<CommodityInfo> commodity = this.commodities.findCommodity(newAlert.commodityId());
		Optional<MarketInfo> market = Optional.ofNullable(newAlert.marketId())
			.flatMap(this.prices::findMarket)
			.filter((candidate) -> candidate.reviewStatus() == MarketReviewStatus.VERIFIED);
		List<String> missing = new ArrayList<>();
		if (commodity.isEmpty()) {
			missing.add("commodityId");
		}
		if (newAlert.marketId() != null && market.isEmpty()) {
			missing.add("marketId");
		}
		if (!missing.isEmpty()) {
			throw ApiException.referenceNotFound(missing.toArray(String[]::new));
		}
		if (!commodity.get().pricedPerQuintal()) {
			throw new ApiException(ErrorCode.UNSUPPORTED_PRICE_UNIT,
					"The commodity's verified price unit is not " + CommodityInfo.RUPEES_PER_QUINTAL + ".");
		}
		UUID profileId = this.auth.requireFarmerProfile(userId);
		PriceAlertEntity alert = this.alerts.save(new PriceAlertEntity(profileId, newAlert.alertType(),
				newAlert.commodityId(), newAlert.marketId(), newAlert.thresholdValue()));
		return AlertMapper.toView(alert, commodity.get(), market.map(MarketInfo::ref).orElse(null));
	}

	/** Setting the current status again is a no-op that returns the alert (contract updateAlertStatus). */
	@Transactional
	public AlertView updateStatus(UUID userId, UUID alertId, AlertStatus status) {
		PriceAlertEntity alert = owned(userId, alertId);
		alert.changeStatus(status);
		return views(List.of(alert)).getFirst();
	}

	/** Deletes the alert; its events go with it (spec section 34 lifecycle step 5). */
	@Transactional
	public void delete(UUID userId, UUID alertId) {
		this.alerts.delete(owned(userId, alertId));
	}

	private PriceAlertEntity owned(UUID userId, UUID alertId) {
		return this.auth.findFarmerProfile(userId)
			.flatMap((profileId) -> this.alerts.findByIdAndFarmerProfileId(alertId, profileId))
			.orElseThrow(() -> ApiException.notFound("The alert does not exist."));
	}

	private List<AlertView> views(List<PriceAlertEntity> entities) {
		Map<UUID, CommodityInfo> commodityInfo = this.commodities
			.findCommodities(entities.stream().map(PriceAlertEntity::getCommodityId).collect(Collectors.toSet()));
		Set<UUID> marketIds = entities.stream()
			.map(PriceAlertEntity::getMarketId)
			.filter((id) -> id != null)
			.collect(Collectors.toSet());
		Map<UUID, MarketInfo> markets = marketIds.isEmpty() ? Map.of() : this.prices.findMarkets(marketIds);
		return entities.stream()
			.map((alert) -> AlertMapper.toView(alert, commodityInfo.get(alert.getCommodityId()),
					(alert.getMarketId() != null) ? markets.get(alert.getMarketId()).ref() : null))
			.toList();
	}

}
