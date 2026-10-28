package com.agrinex.alert.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.agrinex.alert.domain.AlertEventEntity;
import com.agrinex.alert.domain.PriceAlertEntity;
import com.agrinex.alert.mapper.AlertMapper;
import com.agrinex.alert.repository.AlertEventRepository;
import com.agrinex.alert.repository.PriceAlertRepository;
import com.agrinex.auth.AuthApi;
import com.agrinex.commodity.CommodityApi;
import com.agrinex.commodity.CommodityInfo;
import com.agrinex.common.error.ApiException;
import com.agrinex.common.time.AgrinexCalendar;
import com.agrinex.common.web.PageParams;
import com.agrinex.common.web.PageResponse;
import com.agrinex.price.FreshnessPolicyPort;
import com.agrinex.price.FreshnessStatus;
import com.agrinex.price.FreshnessThresholds;
import com.agrinex.price.MarketInfo;
import com.agrinex.price.PriceApi;
import com.agrinex.price.PriceQuote;
import com.agrinex.price.SelectedObservation;
import com.agrinex.price.SourceSyncStatusPort;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * In-app delivery of alert events by polling (spec section 34; ADR-0019 item 5): the caller's events and the
 * read action. Another user's event answers 404.
 */
@Service
@Transactional(readOnly = true)
public class AlertEventService {

	/** triggeredAt descending, then id (contract listAlertEvents). */
	static final Sort EVENT_ORDER = Sort.by(Sort.Order.desc("triggeredAt"), Sort.Order.asc("id"));

	private final AlertEventRepository events;

	private final PriceAlertRepository alerts;

	private final AuthApi auth;

	private final CommodityApi commodities;

	private final PriceApi prices;

	private final FreshnessPolicyPort freshnessPolicy;

	private final SourceSyncStatusPort syncStatus;

	private final AgrinexCalendar calendar;

	AlertEventService(AlertEventRepository events, PriceAlertRepository alerts, AuthApi auth,
			CommodityApi commodities, PriceApi prices, FreshnessPolicyPort freshnessPolicy,
			SourceSyncStatusPort syncStatus, AgrinexCalendar calendar) {
		this.events = events;
		this.alerts = alerts;
		this.auth = auth;
		this.commodities = commodities;
		this.prices = prices;
		this.freshnessPolicy = freshnessPolicy;
		this.syncStatus = syncStatus;
		this.calendar = calendar;
	}

	/** {@code unreadOnly} returns only unread events, and then totalItems is the unread count. */
	public AlertEventPageView list(UUID userId, boolean unreadOnly, PageParams page) {
		Optional<UUID> profileId = this.auth.findFarmerProfile(userId);
		PageResponse<AlertEventView> result;
		if (profileId.isEmpty()) {
			result = page.toResponse(List.of(), 0);
		}
		else {
			PageRequest request = PageRequest.of(page.page(), page.size(), EVENT_ORDER);
			Page<AlertEventEntity> found = unreadOnly ? this.events.findUnreadOwnedBy(profileId.get(), request)
					: this.events.findOwnedBy(profileId.get(), request);
			result = page.toResponse(views(found.getContent()), found.getTotalElements());
		}
		return new AlertEventPageView(result, this.syncStatus.currentDatasetStatus());
	}

	/** Idempotent: an event already read keeps its first readAt; the alert stays ACTIVE (spec section 34). */
	@Transactional
	public void markRead(UUID userId, UUID eventId) {
		this.auth.findFarmerProfile(userId)
			.flatMap((profileId) -> this.events.findOwned(eventId, profileId))
			.orElseThrow(() -> ApiException.notFound("The alert event does not exist."))
			.markRead(this.calendar.now());
	}

	private List<AlertEventView> views(List<AlertEventEntity> entities) {
		if (entities.isEmpty()) {
			return List.of();
		}
		Map<UUID, PriceAlertEntity> alertsById = this.alerts
			.findByIdIn(entities.stream().map(AlertEventEntity::getPriceAlertId).collect(Collectors.toSet()))
			.stream()
			.collect(Collectors.toMap(PriceAlertEntity::getId, Function.identity()));
		Map<UUID, CommodityInfo> commodityInfo = this.commodities.findCommodities(alertsById.values()
			.stream()
			.map(PriceAlertEntity::getCommodityId)
			.collect(Collectors.toSet()));
		Map<UUID, MarketInfo> markets = this.prices
			.findMarkets(entities.stream().map(AlertEventEntity::getMarketId).collect(Collectors.toSet()));
		Map<UUID, SelectedObservation> observations = this.prices
			.observations(entities.stream().map(AlertEventEntity::getPriceObservationId).collect(Collectors.toSet()));
		LocalDate today = this.calendar.today();
		FreshnessThresholds thresholds = this.freshnessPolicy.activeThresholds();
		return entities.stream().map((event) -> {
			PriceAlertEntity alert = alertsById.get(event.getPriceAlertId());
			SelectedObservation observation = observations.get(event.getPriceObservationId());
			return AlertMapper.toView(event, alert, commodityInfo.get(alert.getCommodityId()),
					markets.get(event.getMarketId()).ref(),
					PriceQuote.of(observation, FreshnessStatus.classify(observation.reportDate(), today, thresholds)));
		}).toList();
	}

}
