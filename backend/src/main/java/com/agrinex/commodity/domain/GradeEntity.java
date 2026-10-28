package com.agrinex.commodity.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** A grade of a commodity, exactly as the source reports it (table {@code grades}; ADR-0006 item 3). */
@Entity
@Table(name = "grades")
public class GradeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "commodity_id")
	private UUID commodityId;

	@Column(name = "canonical_name")
	private String canonicalName;

	@Column(name = "created_at")
	private Instant createdAt;

	protected GradeEntity() {
	}

	public GradeEntity(UUID commodityId, String canonicalName, Instant createdAt) {
		this.commodityId = commodityId;
		this.canonicalName = canonicalName;
		this.createdAt = createdAt;
	}

	public UUID getId() {
		return this.id;
	}

	public UUID getCommodityId() {
		return this.commodityId;
	}

	public String getCanonicalName() {
		return this.canonicalName;
	}

}
