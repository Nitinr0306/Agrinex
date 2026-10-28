package com.agrinex.recommendation.engine;

import java.math.BigDecimal;
import java.util.UUID;

import com.agrinex.price.FreshnessStatus;
import com.agrinex.transport.TransportEstimate;

/**
 * The ESTIMATED economics of selling the whole quantity at one candidate market (spec section 32; ADR-0026 item
 * 1), with the observation's freshness under the pinned thresholds (ADR-0035 item 2).
 */
public record EvaluatedOption(Candidate candidate, TransportEstimate transport, BigDecimal grossValue,
		BigDecimal netRealization, FreshnessStatus freshness) {

	/** The largest absolute amount {@code numeric(14,2)} stores (ADR-0040). */
	public static final BigDecimal MAX_STORED_AMOUNT = new BigDecimal("999999999999.99");

	public UUID marketId() {
		return this.candidate.market().id();
	}

	public String marketName() {
		return this.candidate.market().name();
	}

	public BigDecimal modalPrice() {
		return this.candidate.observation().modalPrice();
	}

	public BigDecimal distanceKm() {
		return this.candidate.distanceKm();
	}

	public BigDecimal transportCost() {
		return this.transport.estimatedTransportCost();
	}

	/** Whether every estimate fits the stored precision (ADR-0040 item 1). */
	public boolean withinStoredPrecision() {
		return this.transport.estimatedTransportCost().abs().compareTo(MAX_STORED_AMOUNT) <= 0
				&& this.grossValue.abs().compareTo(MAX_STORED_AMOUNT) <= 0
				&& this.netRealization.abs().compareTo(MAX_STORED_AMOUNT) <= 0;
	}

}
