package com.agrinex.price.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

/**
 * One forecast point written by the optional ML job (table {@code forecasts}; spec section 35; ADR-0030). The
 * backend only reads it, for the optional forecast operation; core flows never do. Every value is as the ML job
 * wrote it.
 */
@Entity
@Immutable
@Table(name = "forecasts")
public class ForecastEntity {

	@Id
	private UUID id;

	@Column(name = "market_id")
	private UUID marketId;

	@Column(name = "commodity_id")
	private UUID commodityId;

	@Column(name = "forecast_horizon_days")
	private int forecastHorizonDays;

	@Column(name = "forecast_date")
	private LocalDate forecastDate;

	@Column(name = "predicted_price")
	private BigDecimal predictedPrice;

	@Column(name = "lower_bound")
	private BigDecimal lowerBound;

	@Column(name = "upper_bound")
	private BigDecimal upperBound;

	@Column(name = "model_name")
	private String modelName;

	@Column(name = "model_version")
	private String modelVersion;

	private BigDecimal mae;

	private BigDecimal rmse;

	@Column(name = "training_data_through")
	private LocalDate trainingDataThrough;

	@Column(name = "generated_at")
	private Instant generatedAt;

	protected ForecastEntity() {
	}

	public int getForecastHorizonDays() {
		return this.forecastHorizonDays;
	}

	public LocalDate getForecastDate() {
		return this.forecastDate;
	}

	public BigDecimal getPredictedPrice() {
		return this.predictedPrice;
	}

	public BigDecimal getLowerBound() {
		return this.lowerBound;
	}

	public BigDecimal getUpperBound() {
		return this.upperBound;
	}

	public String getModelName() {
		return this.modelName;
	}

	public String getModelVersion() {
		return this.modelVersion;
	}

	public BigDecimal getMae() {
		return this.mae;
	}

	public BigDecimal getRmse() {
		return this.rmse;
	}

	public LocalDate getTrainingDataThrough() {
		return this.trainingDataThrough;
	}

	public Instant getGeneratedAt() {
		return this.generatedAt;
	}

}
