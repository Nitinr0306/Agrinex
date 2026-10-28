package com.agrinex.alert.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A farmer's price alert (table {@code price_alerts}; spec section 20, 34). Type, commodity, market and threshold
 * never change; only the status does. Deleting it deletes its events (ON DELETE CASCADE).
 */
@Entity
@Table(name = "price_alerts")
public class PriceAlertEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "farmer_profile_id")
	private UUID farmerProfileId;

	@Enumerated(EnumType.STRING)
	@Column(name = "alert_type")
	private AlertType alertType;

	@Column(name = "commodity_id")
	private UUID commodityId;

	@Column(name = "market_id")
	private UUID marketId;

	@Column(name = "threshold_value")
	private BigDecimal thresholdValue;

	@Enumerated(EnumType.STRING)
	private AlertStatus status;

	@Column(name = "last_triggered_at")
	private Instant lastTriggeredAt;

	protected PriceAlertEntity() {
	}

	public PriceAlertEntity(UUID farmerProfileId, AlertType alertType, UUID commodityId, UUID marketId,
			BigDecimal thresholdValue) {
		this.farmerProfileId = farmerProfileId;
		this.alertType = alertType;
		this.commodityId = commodityId;
		this.marketId = marketId;
		this.thresholdValue = thresholdValue;
		this.status = AlertStatus.ACTIVE;
	}

	public void changeStatus(AlertStatus newStatus) {
		this.status = newStatus;
	}

	public void markTriggered(Instant triggeredAt) {
		this.lastTriggeredAt = triggeredAt;
	}

	public UUID getId() {
		return this.id;
	}

	public UUID getFarmerProfileId() {
		return this.farmerProfileId;
	}

	public AlertType getAlertType() {
		return this.alertType;
	}

	public UUID getCommodityId() {
		return this.commodityId;
	}

	public UUID getMarketId() {
		return this.marketId;
	}

	public BigDecimal getThresholdValue() {
		return this.thresholdValue;
	}

	public AlertStatus getStatus() {
		return this.status;
	}

	public Instant getLastTriggeredAt() {
		return this.lastTriggeredAt;
	}

}
