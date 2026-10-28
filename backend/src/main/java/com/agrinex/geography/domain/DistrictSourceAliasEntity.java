package com.agrinex.geography.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Maps a normalized source (state, district) spelling to an LGD district, with evidence (ADR-0014 item 3). */
@Entity
@Table(name = "district_source_aliases")
public class DistrictSourceAliasEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "district_id")
	private DistrictEntity district;

	@Column(name = "source_state_name")
	private String sourceStateName;

	@Column(name = "source_district_name")
	private String sourceDistrictName;

	@Column(name = "evidence_note")
	private String evidenceNote;

	@Column(name = "created_at")
	private Instant createdAt;

	protected DistrictSourceAliasEntity() {
	}

	public DistrictSourceAliasEntity(DistrictEntity district, String sourceStateName, String sourceDistrictName,
			String evidenceNote, Instant createdAt) {
		this.district = district;
		this.sourceStateName = sourceStateName;
		this.sourceDistrictName = sourceDistrictName;
		this.evidenceNote = evidenceNote;
		this.createdAt = createdAt;
	}

	public UUID getId() {
		return this.id;
	}

	public DistrictEntity getDistrict() {
		return this.district;
	}

	public String getSourceStateName() {
		return this.sourceStateName;
	}

	public String getSourceDistrictName() {
		return this.sourceDistrictName;
	}

	public String getEvidenceNote() {
		return this.evidenceNote;
	}

	public Instant getCreatedAt() {
		return this.createdAt;
	}

}
