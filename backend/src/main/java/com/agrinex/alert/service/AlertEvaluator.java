package com.agrinex.alert.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.agrinex.alert.AlertApi;
import com.agrinex.alert.domain.AlertEventEntity;
import com.agrinex.alert.domain.AlertStatus;
import com.agrinex.alert.domain.AlertType;
import com.agrinex.alert.domain.PriceAlertEntity;
import com.agrinex.alert.repository.AlertEventRepository;
import com.agrinex.alert.repository.PriceAlertRepository;
import com.agrinex.common.time.AgrinexCalendar;
import com.agrinex.price.FreshnessPolicyPort;
import com.agrinex.price.MarketCommodity;
import com.agrinex.price.MarketInfo;
import com.agrinex.price.PriceApi;
import com.agrinex.price.SelectedObservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Alert evaluation after an ingestion run (spec section 34; ADR-0019): the ACTIVE alerts whose commodity, and
 * market when set, had an observation inserted or revised by the run in a VERIFIED, active market. Prices are
 * the ADR-0010 selection within the active stale window, changes the ADR-0025 statistic; an alert without a market
 * is evaluated over every VERIFIED, active market and names the market with the most extreme qualifying value,
 * ties by market name, then id. At most one event per alert and run (ADR-0019 item 4).
 */
@Service
class AlertEvaluator implements AlertApi {

	/** alert_events.trigger_value numeric(14,4). */
	static final int TRIGGER_VALUE_SCALE = 4;

	private final PriceAlertRepository alerts;

	private final AlertEventRepository events;

	private final PriceApi prices;

	private final FreshnessPolicyPort freshnessPolicy;

	private final AgrinexCalendar calendar;

	AlertEvaluator(PriceAlertRepository alerts, AlertEventRepository events, PriceApi prices,
			FreshnessPolicyPort freshnessPolicy, AgrinexCalendar calendar) {
		this.alerts = alerts;
		this.events = events;
		this.prices = prices;
		this.freshnessPolicy = freshnessPolicy;
		this.calendar = calendar;
	}

	@Override
	@Transactional
	public int evaluateRun(UUID ingestionRunId) {
		Set<MarketCommodity> changed = this.prices.changedByRun(ingestionRunId);
		if (changed.isEmpty()) {
			return 0;
		}
		Set<UUID> commodityIds = changed.stream().map(MarketCommodity::commodityId).collect(Collectors.toSet());
		List<PriceAlertEntity> candidates = this.alerts.findByStatusAndCommodityIdIn(AlertStatus.ACTIVE, commodityIds);
		Evaluation evaluation = new Evaluation(this.prices.verifiedActiveMarkets(), this.calendar.today(),
				this.freshnessPolicy.activeThresholds().staleDays());
		Instant now = this.calendar.now();
		int recorded = 0;
		for (PriceAlertEntity alert : candidates) {
			if (alert.getMarketId() != null
					&& !changed.contains(new MarketCommodity(alert.getMarketId(), alert.getCommodityId()))) {
				continue;
			}
			Optional<Trigger> trigger = evaluation.trigger(alert);
			if (trigger.isPresent() && !this.events.existsByPriceAlertIdAndIngestionRunId(alert.getId(), ingestionRunId)) {
				Trigger found = trigger.get();
				this.events.save(new AlertEventEntity(alert.getId(), now,
						found.value().setScale(TRIGGER_VALUE_SCALE, RoundingMode.HALF_UP), ingestionRunId,
						found.market().id(), found.observation().observationId()));
				alert.markTriggered(now);
				recorded++;
			}
		}
		return recorded;
	}

	/** The market, observation and value that met an alert's condition. */
	private record Trigger(MarketInfo market, SelectedObservation observation, BigDecimal value) {
	}

	/** The selected prices and trends of one evaluation, read once per commodity and market. */
	private final class Evaluation {

		private final Map<UUID, MarketInfo> markets;

		private final LocalDate today;

		private final int staleDays;

		private final Map<UUID, Map<UUID, SelectedObservation>> latestByCommodity = new HashMap<>();

		private final Map<MarketCommodity, Optional<BigDecimal>> changeByPair = new HashMap<>();

		Evaluation(List<MarketInfo> verifiedActive, LocalDate today, int staleDays) {
			this.markets = verifiedActive.stream().collect(Collectors.toMap(MarketInfo::id, (market) -> market));
			this.today = today;
			this.staleDays = staleDays;
		}

		Optional<Trigger> trigger(PriceAlertEntity alert) {
			List<UUID> marketIds = (alert.getMarketId() != null) ? List.of(alert.getMarketId())
					: List.copyOf(this.markets.keySet());
			Map<UUID, SelectedObservation> latest = latest(alert.getCommodityId());
			AlertType type = alert.getAlertType();
			return marketIds.stream()
				.filter(this.markets::containsKey)
				.filter(latest::containsKey)
				.map((marketId) -> value(type, marketId, alert.getCommodityId(), latest.get(marketId))
					.map((value) -> new Trigger(this.markets.get(marketId), latest.get(marketId), value)))
				.flatMap(Optional::stream)
				.filter((candidate) -> type.triggeredBy(candidate.value(), alert.getThresholdValue()))
				.min(Comparator.comparing(Trigger::value, type.mostExtremeFirst())
					.thenComparing((candidate) -> candidate.market().name())
					.thenComparing((candidate) -> candidate.market().id()));
		}

		private Optional<BigDecimal> value(AlertType type, UUID marketId, UUID commodityId,
				SelectedObservation observation) {
			if (type.priceBased()) {
				return Optional.of(observation.modalPrice());
			}
			return this.changeByPair.computeIfAbsent(new MarketCommodity(marketId, commodityId),
					(pair) -> Optional.ofNullable(
							AlertEvaluator.this.prices.trend(marketId, commodityId, null, null, this.today).changePercent()));
		}

		private Map<UUID, SelectedObservation> latest(UUID commodityId) {
			return this.latestByCommodity.computeIfAbsent(commodityId, (id) -> AlertEvaluator.this.prices
				.selectLatest(this.markets.keySet(), id, null, null, this.today, this.staleDays));
		}

	}

}
