package com.agrinex.alert.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * One trigger of an alert by an ingestion run (table {@code alert_events}; ADR-0019 item 4): at most one per
 * alert and run. Reading it sets {@code read_at} once; the alert stays ACTIVE (spec section 34).
 */
@Entity
@Table(name = "alert_events")
public class AlertEventEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "price_alert_id")
	private UUID priceAlertId;

	@Column(name = "triggered_at")
	private Instant triggeredAt;

	@Column(name = "trigger_value")
	private BigDecimal triggerValue;

	@Column(name = "ingestion_run_id")
	private UUID ingestionRunId;

	@Column(name = "market_id")
	private UUID marketId;

	@Column(name = "price_observation_id")
	private UUID priceObservationId;

	@Column(name = "read_at")
	private Instant readAt;

	protected AlertEventEntity() {
	}

	public AlertEventEntity(UUID priceAlertId, Instant triggeredAt, BigDecimal triggerValue, UUID ingestionRunId,
			UUID marketId, UUID priceObservationId) {
		this.priceAlertId = priceAlertId;
		this.triggeredAt = triggeredAt;
		this.triggerValue = triggerValue;
		this.ingestionRunId = ingestionRunId;
		this.marketId = marketId;
		this.priceObservationId = priceObservationId;
	}

	/** Idempotent: an event already read keeps its first readAt. */
	public void markRead(Instant now) {
		if (this.readAt == null) {
			this.readAt = now;
		}
	}

	public UUID getId() {
		return this.id;
	}

	public UUID getPriceAlertId() {
		return this.priceAlertId;
	}

	public Instant getTriggeredAt() {
		return this.triggeredAt;
	}

	public BigDecimal getTriggerValue() {
		return this.triggerValue;
	}

	public UUID getMarketId() {
		return this.marketId;
	}

	public UUID getPriceObservationId() {
		return this.priceObservationId;
	}

	public Instant getReadAt() {
		return this.readAt;
	}

}
