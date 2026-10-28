package com.agrinex.recommendation.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.agrinex.price.FreshnessStatus;
import com.agrinex.price.TrendDirection;

/**
 * One eligible, ranked market of a recorded comparison (table {@code selling_options}; spec section 16, 20).
 * Excluded markets are never stored (ADR-0023 item 4).
 */
@Entity
@Table(name = "selling_options")
public class SellingOptionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "recommendation_run_id")
	private UUID recommendationRunId;

	@Column(name = "market_id")
	private UUID marketId;

	@Column(name = "price_observation_id")
	private UUID priceObservationId;

	@Column(name = "distance_km")
	private BigDecimal distanceKm;

	@Column(name = "estimated_transport_cost")
	private BigDecimal estimatedTransportCost;

	@Column(name = "estimated_gross_value")
	private BigDecimal estimatedGrossValue;

	@Column(name = "estimated_net_realization")
	private BigDecimal estimatedNetRealization;

	@Enumerated(EnumType.STRING)
	@Column(name = "trend_direction")
	private TrendDirection trendDirection;

	@Enumerated(EnumType.STRING)
	@Column(name = "freshness_status")
	private FreshnessStatus freshnessStatus;

	private int rank;

	private BigDecimal score;

	@JdbcTypeCode(SqlTypes.ARRAY)
	@Column(name = "reason_codes")
	private String[] reasonCodes;

	protected SellingOptionEntity() {
	}

	public SellingOptionEntity(UUID recommendationRunId, UUID marketId, UUID priceObservationId, BigDecimal distanceKm,
			BigDecimal estimatedTransportCost, BigDecimal estimatedGrossValue, BigDecimal estimatedNetRealization,
			TrendDirection trendDirection, FreshnessStatus freshnessStatus, int rank, BigDecimal score,
			List<ReasonCode> reasonCodes) {
		this.recommendationRunId = recommendationRunId;
		this.marketId = marketId;
		this.priceObservationId = priceObservationId;
		this.distanceKm = distanceKm;
		this.estimatedTransportCost = estimatedTransportCost;
		this.estimatedGrossValue = estimatedGrossValue;
		this.estimatedNetRealization = estimatedNetRealization;
		this.trendDirection = trendDirection;
		this.freshnessStatus = freshnessStatus;
		this.rank = rank;
		this.score = score;
		this.reasonCodes = reasonCodes.stream().map(ReasonCode::name).toArray(String[]::new);
	}

	public UUID getId() {
		return this.id;
	}

}
