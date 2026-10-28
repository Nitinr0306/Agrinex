package com.agrinex.admin.domain;

/** Kinds of object an audit event acts on ({@code admin_audit_events.target_type}; contract AdminAuditEventResponse). */
public enum AuditTarget {

	INGESTION_RUN("ingestion_run"), SOURCE_DATASET("source_dataset"), TRANSPORT_ASSUMPTIONS("transport_assumptions"),
	RECOMMENDATION_WEIGHTS("recommendation_weights"), MARKET("market"), COMMODITY("commodity"),
	DISTRICT_ALIAS("district_alias"), USER("user");

	private final String type;

	AuditTarget(String type) {
		this.type = type;
	}

	public String type() {
		return this.type;
	}

}
