package com.agrinex.ingestion.domain;

/**
 * The audit counters of one run while it executes (ADR-0012). For a run that ends SUCCESS or PARTIAL_FAILURE they
 * satisfy {@code fetched = inserted + updated + duplicates + validationErrors + conflicted + outOfScope}, which
 * {@code ck_ingestion_runs_accounting} enforces.
 */
public final class RunCounters {

	private Integer sourceTotalRecords;

	private int pagesFetched;

	private int pagesFailed;

	private int recordsFetched;

	private int recordsInserted;

	private int recordsUpdated;

	private int duplicatesSkipped;

	private int validationErrors;

	private int recordsConflicted;

	private int recordsOutOfScope;

	public void addSourceTotal(int total) {
		this.sourceTotalRecords = (this.sourceTotalRecords != null) ? this.sourceTotalRecords + total : total;
	}

	public void pageFetched(int records) {
		this.pagesFetched++;
		this.recordsFetched += records;
	}

	public void pageFailed() {
		this.pagesFailed++;
	}

	public void addUnit(UnitCounts counts) {
		this.recordsInserted += counts.inserted();
		this.recordsUpdated += counts.updated();
		this.duplicatesSkipped += counts.duplicates();
		this.validationErrors += counts.validationErrors();
		this.recordsConflicted += counts.conflicted();
		this.recordsOutOfScope += counts.outOfScope();
	}

	public boolean changedObservations() {
		return this.recordsInserted + this.recordsUpdated > 0;
	}

	public Integer sourceTotalRecords() {
		return this.sourceTotalRecords;
	}

	public int pagesFetched() {
		return this.pagesFetched;
	}

	public int pagesFailed() {
		return this.pagesFailed;
	}

	public int recordsFetched() {
		return this.recordsFetched;
	}

	public int recordsInserted() {
		return this.recordsInserted;
	}

	public int recordsUpdated() {
		return this.recordsUpdated;
	}

	public int duplicatesSkipped() {
		return this.duplicatesSkipped;
	}

	public int validationErrors() {
		return this.validationErrors;
	}

	public int recordsConflicted() {
		return this.recordsConflicted;
	}

	public int recordsOutOfScope() {
		return this.recordsOutOfScope;
	}

	/** What one committed delivery unit contributed to the record counters. */
	public record UnitCounts(int inserted, int updated, int duplicates, int validationErrors, int conflicted,
			int outOfScope) {
	}

}
