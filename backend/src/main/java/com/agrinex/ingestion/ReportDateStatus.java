package com.agrinex.ingestion;

/**
 * Status of one HISTORICAL_PRICE report date, computed from the report-date ledger (ADR-0054 item 2). The settle
 * age is the trailing window's depth, the measured publication lag of the historical resource.
 */
public enum ReportDateStatus {

	/** A successful fetch happened when the date was at least the settle age old. */
	SETTLED,

	/** Younger than the settle age: the trailing window still fetches it. */
	PENDING,

	/** At least the settle age old, but every successful fetch happened earlier. */
	UNSETTLED,

	/** The last attempt failed. */
	FAILED,

	/** Never attempted. */
	MISSING;

	/** Whether the daily gap check fetches a date in this status once it has reached the settle age. */
	public boolean incomplete() {
		return this == UNSETTLED || this == FAILED || this == MISSING;
	}

}
