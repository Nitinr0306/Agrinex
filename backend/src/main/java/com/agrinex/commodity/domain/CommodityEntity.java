package com.agrinex.commodity.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** A commodity of the known list (table {@code commodities}; ADR-0006, ADR-0008, ADR-0015). */
@Entity
@Table(name = "commodities")
public class CommodityEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "canonical_name")
	private String canonicalName;

	@Column(name = "name_hi")
	private String nameHi;

	@Column(name = "name_mr")
	private String nameMr;

	private String category;

	@Column(name = "price_unit")
	private String priceUnit;

	@Column(name = "price_unit_source")
	private String priceUnitSource;

	@Column(name = "created_at")
	private Instant createdAt;

	protected CommodityEntity() {
	}

	public CommodityEntity(String canonicalName, String category, String priceUnit, String priceUnitSource,
			Instant createdAt) {
		this.canonicalName = canonicalName;
		this.category = category;
		this.priceUnit = priceUnit;
		this.priceUnitSource = priceUnitSource;
		this.createdAt = createdAt;
	}

	public UUID getId() {
		return this.id;
	}

	public String getCanonicalName() {
		return this.canonicalName;
	}

	public String getNameHi() {
		return this.nameHi;
	}

	public String getNameMr() {
		return this.nameMr;
	}

	public String getCategory() {
		return this.category;
	}

	public String getPriceUnit() {
		return this.priceUnit;
	}

	public String getPriceUnitSource() {
		return this.priceUnitSource;
	}

	public Instant getCreatedAt() {
		return this.createdAt;
	}

	public void verifyUnit(String priceUnit, String priceUnitSource) {
		this.priceUnit = priceUnit;
		this.priceUnitSource = priceUnitSource;
	}

	public void rename(String nameHi, String nameMr) {
		this.nameHi = nameHi;
		this.nameMr = nameMr;
	}

}
