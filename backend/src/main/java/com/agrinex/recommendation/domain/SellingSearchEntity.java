package com.agrinex.recommendation.domain;

import java.math.BigDecimal;
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
 * The inputs of a recorded comparison (table {@code selling_searches}; spec section 16 PERSIST). The origin is
 * always a validated district centroid (ADR-0039); {@code farmer_profile_id} is set only for a signed-in caller
 * with a profile and is set to NULL on account erasure (ADR-0029).
 */
@Entity
@Table(name = "selling_searches")
public class SellingSearchEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "farmer_profile_id")
	private UUID farmerProfileId;

	@Column(name = "commodity_id")
	private UUID commodityId;

	@Column(name = "variety_id")
	private UUID varietyId;

	@Column(name = "grade_id")
	private UUID gradeId;

	private BigDecimal quantity;

	@Enumerated(EnumType.STRING)
	@Column(name = "quantity_unit")
	private QuantityUnit quantityUnit;

	@Column(name = "origin_lat")
	private BigDecimal originLat;

	@Column(name = "origin_lon")
	private BigDecimal originLon;

	@Column(name = "max_distance_km")
	private int maxDistanceKm;

	protected SellingSearchEntity() {
	}

	public SellingSearchEntity(UUID farmerProfileId, UUID commodityId, UUID varietyId, UUID gradeId, BigDecimal quantity,
			QuantityUnit quantityUnit, BigDecimal originLat, BigDecimal originLon, int maxDistanceKm) {
		this.farmerProfileId = farmerProfileId;
		this.commodityId = commodityId;
		this.varietyId = varietyId;
		this.gradeId = gradeId;
		this.quantity = quantity;
		this.quantityUnit = quantityUnit;
		this.originLat = originLat;
		this.originLon = originLon;
		this.maxDistanceKm = maxDistanceKm;
	}

	public UUID getId() {
		return this.id;
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

	public BigDecimal getQuantity() {
		return this.quantity;
	}

	public QuantityUnit getQuantityUnit() {
		return this.quantityUnit;
	}

	public BigDecimal getOriginLat() {
		return this.originLat;
	}

	public BigDecimal getOriginLon() {
		return this.originLon;
	}

	public int getMaxDistanceKm() {
		return this.maxDistanceKm;
	}

}
