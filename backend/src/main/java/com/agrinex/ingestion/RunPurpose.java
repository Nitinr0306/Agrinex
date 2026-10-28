package com.agrinex.ingestion;

/**
 * Why a HISTORICAL_PRICE run was started, recorded as {@code purpose} in its request scope (ADR-0054 item 5).
 * CURRENT_PRICE runs have no report-date range and carry no purpose.
 */
public enum RunPurpose {

	/** The 6-hourly trailing window of ADR-0011 item 1. */
	TRAILING_WINDOW,

	/** The daily gap check: the newest incomplete report dates (ADR-0054 item 3). */
	GAP_BACKFILL,

	/** The weekly re-fetch of recent report dates (ADR-0054 item 4). */
	RECONCILIATION,

	/** An admin trigger (ADR-0011 item 3). */
	MANUAL

}
