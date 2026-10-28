package com.agrinex.recommendation.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * One recorded comparison (table {@code recommendation_runs}; spec section 16, 20, 36): the configuration
 * versions it used and its winners. Winners are NULL together when no option was eligible (ADR-0023 item 4).
 */
@Entity
@Table(name = "recommendation_runs")
public class RecommendationRunEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "selling_search_id")
	private UUID sellingSearchId;

	@Column(name = "weights_id")
	private UUID weightsId;

	@Column(name = "transport_id")
	private UUID transportId;

	@Column(name = "price_only_winner_market_id")
	private UUID priceOnlyWinnerMarketId;

	@Column(name = "net_winner_market_id")
	private UUID netWinnerMarketId;

	@Column(name = "rankings_disagree")
	private boolean rankingsDisagree;

	@Column(name = "created_at")
	private Instant createdAt;

	protected RecommendationRunEntity() {
	}

	public RecommendationRunEntity(UUID sellingSearchId, UUID weightsId, UUID transportId, UUID priceOnlyWinnerMarketId,
			UUID netWinnerMarketId, boolean rankingsDisagree, Instant createdAt) {
		this.sellingSearchId = sellingSearchId;
		this.weightsId = weightsId;
		this.transportId = transportId;
		this.priceOnlyWinnerMarketId = priceOnlyWinnerMarketId;
		this.netWinnerMarketId = netWinnerMarketId;
		this.rankingsDisagree = rankingsDisagree;
		this.createdAt = createdAt;
	}

	public UUID getId() {
		return this.id;
	}

	public UUID getSellingSearchId() {
		return this.sellingSearchId;
	}

	public UUID getWeightsId() {
		return this.weightsId;
	}

	public UUID getTransportId() {
		return this.transportId;
	}

}
