package com.agrinex.admin.domain;

/**
 * The closed vocabulary of admin audit actions (ADR-0020 item 6, ADR-0037, ADR-0041), held identically by the
 * database check {@code ck_admin_audit_events_action}. The last four are written only at start-up or by operator
 * procedures, never by an HTTP operation.
 */
public enum AuditAction {

	INGESTION_TRIGGERED, INGESTION_PAUSED, INGESTION_RESUMED, TRANSPORT_ASSUMPTIONS_UPDATED,
	RECOMMENDATION_WEIGHTS_UPDATED, MARKET_VERIFIED, MARKET_MERGED, MARKET_DEACTIVATED, MARKET_REACTIVATED,
	MARKET_COORDINATES_SET, COMMODITY_ADDED, COMMODITY_UNIT_VERIFIED, COMMODITY_NAMES_UPDATED, DISTRICT_ALIAS_ADDED,
	ADMIN_BOOTSTRAPPED, ACCOUNT_ERASED, ACCOUNT_DEACTIVATED, ACCOUNT_REACTIVATED

}
