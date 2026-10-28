package com.agrinex.geography.domain;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/** A district with its Survey of India centroid (table {@code districts}; reference data, ADR-0014). */
@Entity
@Immutable
@Table(name = "districts")
public class DistrictEntity {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "state_id")
	private StateEntity state;

	private String name;

	@Column(name = "lgd_code")
	private Integer lgdCode;

	@Column(name = "centroid_lat")
	private BigDecimal centroidLat;

	@Column(name = "centroid_lon")
	private BigDecimal centroidLon;

	@Column(name = "centroid_source")
	private String centroidSource;

	protected DistrictEntity() {
	}

	public UUID getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public StateEntity getState() {
		return this.state;
	}

	public BigDecimal getCentroidLat() {
		return this.centroidLat;
	}

	public BigDecimal getCentroidLon() {
		return this.centroidLon;
	}

}
