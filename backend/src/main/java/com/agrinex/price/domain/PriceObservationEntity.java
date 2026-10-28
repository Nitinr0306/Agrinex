package com.agrinex.price.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A government-reported price observation (table {@code price_observations}; spec section 20; ADR-0008,
 * ADR-0009). {@code report_date} is the date the source observation describes and is never overwritten;
 * {@code ingested_at} is when the current price values were written. {@code ingestion_run_id} is the run that
 * inserted the row and never changes. The unit is copied from the commodity at load time and never changes.
 */
@Entity
@Table(name = "price_observations")
public class PriceObservationEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "market_id")
	private UUID marketId;

	@Column(name = "commodity_id")
	private UUID commodityId;

	@Column(name = "variety_id")
	private UUID varietyId;

	@Column(name = "grade_id")
	private UUID gradeId;

	@Column(name = "report_date")
	private LocalDate reportDate;

	@Column(name = "modal_price")
	private BigDecimal modalPrice;

	@Column(name = "min_price")
	private BigDecimal minPrice;

	@Column(name = "max_price")
	private BigDecimal maxPrice;

	@Column(name = "price_unit")
	private String priceUnit;

	/** The source's commodity spelling when it differs from the master name (ADR-0051); null otherwise. */
	@Column(name = "source_commodity_name")
	private String sourceCommodityName;

	@Column(name = "business_key")
	private String businessKey;

	@Column(name = "source_dataset_id")
	private UUID sourceDatasetId;

	@Column(name = "ingestion_run_id")
	private UUID ingestionRunId;

	@Column(name = "ingested_at")
	private Instant ingestedAt;

	protected PriceObservationEntity() {
	}

	public PriceObservationEntity(UUID marketId, UUID commodityId, UUID varietyId, UUID gradeId, LocalDate reportDate,
			BigDecimal modalPrice, BigDecimal minPrice, BigDecimal maxPrice, String priceUnit, String sourceCommodityName,
			String businessKey, UUID sourceDatasetId, UUID ingestionRunId, Instant ingestedAt) {
		this.marketId = marketId;
		this.commodityId = commodityId;
		this.varietyId = varietyId;
		this.gradeId = gradeId;
		this.reportDate = reportDate;
		this.modalPrice = modalPrice;
		this.minPrice = minPrice;
		this.maxPrice = maxPrice;
		this.priceUnit = priceUnit;
		this.sourceCommodityName = sourceCommodityName;
		this.businessKey = businessKey;
		this.sourceDatasetId = sourceDatasetId;
		this.ingestionRunId = ingestionRunId;
		this.ingestedAt = ingestedAt;
	}

	/** Applies changed source prices from a newer run; the replaced values go to a revision row first (ADR-0009). */
	public void revise(BigDecimal modalPrice, BigDecimal minPrice, BigDecimal maxPrice, Instant revisedAt) {
		this.modalPrice = modalPrice;
		this.minPrice = minPrice;
		this.maxPrice = maxPrice;
		this.ingestedAt = revisedAt;
	}

	/** Re-points the observation to a merge target under the identity rules (ADR-0014 item 4, ADR-0009). */
	public void repoint(UUID targetMarketId, String newBusinessKey) {
		this.marketId = targetMarketId;
		this.businessKey = newBusinessKey;
	}

	public UUID getId() {
		return this.id;
	}

	public UUID getMarketId() {
		return this.marketId;
	}

	public UUID getCommodityId() {
		return this.commodityId;
	}

	public UUID getVarietyId() {
		return this.varietyId;
	}

	public UUID getGradeId() {
		return this.gradeId;
	}

	public LocalDate getReportDate() {
		return this.reportDate;
	}

	public BigDecimal getModalPrice() {
		return this.modalPrice;
	}

	public BigDecimal getMinPrice() {
		return this.minPrice;
	}

	public BigDecimal getMaxPrice() {
		return this.maxPrice;
	}

	public String getPriceUnit() {
		return this.priceUnit;
	}

	public String getSourceCommodityName() {
		return this.sourceCommodityName;
	}

	public String getBusinessKey() {
		return this.businessKey;
	}

	public UUID getSourceDatasetId() {
		return this.sourceDatasetId;
	}

	public UUID getIngestionRunId() {
		return this.ingestionRunId;
	}

	public Instant getIngestedAt() {
		return this.ingestedAt;
	}

}
