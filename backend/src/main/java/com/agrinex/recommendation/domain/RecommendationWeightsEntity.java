package com.agrinex.recommendation.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.agrinex.recommendation.WeightsUpdate;

/** One version of the scoring weights and freshness thresholds (table {@code recommendation_weights}). */
@Entity
@Table(name = "recommendation_weights")
public class RecommendationWeightsEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "w_net_realization")
	private BigDecimal wNetRealization;

	@Column(name = "w_price")
	private BigDecimal wPrice;

	@Column(name = "w_trend")
	private BigDecimal wTrend;

	@Column(name = "w_freshness")
	private BigDecimal wFreshness;

	@Column(name = "w_transport_penalty")
	private BigDecimal wTransportPenalty;

	@Column(name = "freshness_fresh_days")
	private int freshnessFreshDays;

	@Column(name = "freshness_recent_days")
	private int freshnessRecentDays;

	@Column(name = "freshness_stale_days")
	private int freshnessStaleDays;

	@Column(name = "is_active")
	private boolean active;

	@Column(name = "created_at")
	private Instant createdAt;

	protected RecommendationWeightsEntity() {
	}

	public RecommendationWeightsEntity(WeightsUpdate update, Instant createdAt) {
		this.wNetRealization = update.wNetRealization();
		this.wPrice = update.wPrice();
		this.wTrend = update.wTrend();
		this.wFreshness = update.wFreshness();
		this.wTransportPenalty = update.wTransportPenalty();
		this.freshnessFreshDays = update.freshnessFreshDays();
		this.freshnessRecentDays = update.freshnessRecentDays();
		this.freshnessStaleDays = update.freshnessStaleDays();
		this.active = true;
		this.createdAt = createdAt;
	}

	public UUID getId() {
		return this.id;
	}

	public BigDecimal getWNetRealization() {
		return this.wNetRealization;
	}

	public BigDecimal getWPrice() {
		return this.wPrice;
	}

	public BigDecimal getWTrend() {
		return this.wTrend;
	}

	public BigDecimal getWFreshness() {
		return this.wFreshness;
	}

	public BigDecimal getWTransportPenalty() {
		return this.wTransportPenalty;
	}

	public int getFreshnessFreshDays() {
		return this.freshnessFreshDays;
	}

	public int getFreshnessRecentDays() {
		return this.freshnessRecentDays;
	}

	public int getFreshnessStaleDays() {
		return this.freshnessStaleDays;
	}

	public boolean isActive() {
		return this.active;
	}

	public Instant getCreatedAt() {
		return this.createdAt;
	}

}
