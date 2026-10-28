package com.agrinex.price.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A source market spelling within its source state and district, mapped to one market (table
 * {@code market_source_aliases}; spec section 47 Q4; ADR-0014). The raw spelling is kept as first received.
 */
@Entity
@Table(name = "market_source_aliases")
public class MarketSourceAliasEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "market_id")
	private UUID marketId;

	@Column(name = "source_state_name")
	private String sourceStateName;

	@Column(name = "source_district_name")
	private String sourceDistrictName;

	@Column(name = "source_market_name")
	private String sourceMarketName;

	@Column(name = "source_market_name_raw")
	private String sourceMarketNameRaw;

	@Column(name = "evidence_note")
	private String evidenceNote;

	@Column(name = "created_at")
	private Instant createdAt;

	protected MarketSourceAliasEntity() {
	}

	public MarketSourceAliasEntity(UUID marketId, String sourceStateName, String sourceDistrictName, String sourceMarketName,
			String sourceMarketNameRaw, String evidenceNote, Instant createdAt) {
		this.marketId = marketId;
		this.sourceStateName = sourceStateName;
		this.sourceDistrictName = sourceDistrictName;
		this.sourceMarketName = sourceMarketName;
		this.sourceMarketNameRaw = sourceMarketNameRaw;
		this.evidenceNote = evidenceNote;
		this.createdAt = createdAt;
	}

	/** Moves the alias to the merge target with the merge evidence (ADR-0014 item 4). */
	public void moveTo(UUID targetMarketId, String mergeEvidence) {
		this.marketId = targetMarketId;
		this.evidenceNote = mergeEvidence;
	}

	public UUID getMarketId() {
		return this.marketId;
	}

	public String getSourceStateName() {
		return this.sourceStateName;
	}

	public String getSourceDistrictName() {
		return this.sourceDistrictName;
	}

	public String getSourceMarketName() {
		return this.sourceMarketName;
	}

	public String getSourceMarketNameRaw() {
		return this.sourceMarketNameRaw;
	}

	public String getEvidenceNote() {
		return this.evidenceNote;
	}

	public Instant getCreatedAt() {
		return this.createdAt;
	}

}
