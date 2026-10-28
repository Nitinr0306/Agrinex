package com.agrinex.ingestion.domain;

/** Outcome of the last attempt at a report date (column {@code ingestion_report_dates.last_outcome}; ADR-0054). */
public enum ReportDateOutcome {

	/** Every page of the date was fetched and its unit loaded. */
	FETCHED,

	/** A page or the load of the date failed. */
	FAILED

}
