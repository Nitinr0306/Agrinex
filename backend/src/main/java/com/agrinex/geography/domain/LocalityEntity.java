package com.agrinex.geography.domain;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/** An LGD sub-district (table {@code localities}; reference data, ADR-0014). */
@Entity
@Immutable
@Table(name = "localities")
public class LocalityEntity {

	@Id
	private UUID id;

	@Column(name = "district_id")
	private UUID districtId;

	private String name;

	@Column(name = "lgd_code")
	private Integer lgdCode;

	private BigDecimal lat;

	private BigDecimal lon;

	@Column(name = "coordinate_source")
	private String coordinateSource;

	protected LocalityEntity() {
	}

	public String getName() {
		return this.name;
	}

	public UUID getId() {
		return this.id;
	}

	public UUID getDistrictId() {
		return this.districtId;
	}

}
