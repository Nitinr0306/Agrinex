package com.agrinex.commodity.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A source spelling of a commodity that is not its master name, mapped on DMI evidence (table
 * {@code commodity_source_aliases}; ADR-0051). Seeded by migration only; read by ingestion's name resolution.
 */
@Entity
@Table(name = "commodity_source_aliases")
public class CommoditySourceAliasEntity {

	@Id
	private UUID id;

	@Column(name = "commodity_id")
	private UUID commodityId;

	@Column(name = "source_commodity_name")
	private String sourceCommodityName;

	@Column(name = "evidence_note")
	private String evidenceNote;

	@Column(name = "created_at")
	private Instant createdAt;

	protected CommoditySourceAliasEntity() {
	}

	public UUID getCommodityId() {
		return this.commodityId;
	}

	public String getSourceCommodityName() {
		return this.sourceCommodityName;
	}

}
