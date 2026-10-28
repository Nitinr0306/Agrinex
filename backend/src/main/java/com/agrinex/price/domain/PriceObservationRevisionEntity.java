package com.agrinex.price.domain;

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
 * The prices an observation had before a newer run revised them (table {@code price_observation_revisions};
 * ADR-0009 item 3). Each row is counted in the revising run's {@code records_updated} (ADR-0012 item 3).
 */
@Entity
@Table(name = "price_observation_revisions")
public class PriceObservationRevisionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "price_observation_id")
	private UUID priceObservationId;

	@Column(name = "ingestion_run_id")
	private UUID ingestionRunId;

	@Column(name = "previous_modal_price")
	private BigDecimal previousModalPrice;

	@Column(name = "previous_min_price")
	private BigDecimal previousMinPrice;

	@Column(name = "previous_max_price")
	private BigDecimal previousMaxPrice;

	@Column(name = "previous_ingested_at")
	private Instant previousIngestedAt;

	@Column(name = "revised_at")
	private Instant revisedAt;

	protected PriceObservationRevisionEntity() {
	}

	public PriceObservationRevisionEntity(PriceObservationEntity observation, UUID ingestionRunId, Instant revisedAt) {
		this.priceObservationId = observation.getId();
		this.ingestionRunId = ingestionRunId;
		this.previousModalPrice = observation.getModalPrice();
		this.previousMinPrice = observation.getMinPrice();
		this.previousMaxPrice = observation.getMaxPrice();
		this.previousIngestedAt = observation.getIngestedAt();
		this.revisedAt = revisedAt;
	}

	public UUID getPriceObservationId() {
		return this.priceObservationId;
	}

}
