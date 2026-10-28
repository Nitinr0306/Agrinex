package com.agrinex.ingestion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Historical maintenance schedule and bounds (ADR-0054 items 3, 4 and 7): deployment configuration, changed only by
 * redeployment (ADR-0020 item 3). Times are in Asia/Kolkata.
 *
 * @param gapCheckCron the daily gap check
 * @param reconciliationCron the weekly re-fetch of recent report dates
 * @param coverageDays report dates covered, ending today (ADR-0011 item 3 maximum: 366)
 * @param maxDatesPerGapRun the most report dates one gap-backfill run spans
 * @param reconciliationWindowDays report dates re-fetched by the reconciliation, ending yesterday
 */
@ConfigurationProperties("agrinex.ingestion.maintenance")
public record HistoricalMaintenanceProperties(String gapCheckCron, String reconciliationCron, int coverageDays,
		int maxDatesPerGapRun, int reconciliationWindowDays) {

	public HistoricalMaintenanceProperties {
		if (coverageDays < 1 || coverageDays > 366) {
			throw new IllegalArgumentException("coverage-days must be between 1 and 366");
		}
		if (maxDatesPerGapRun < 1 || maxDatesPerGapRun > 366) {
			throw new IllegalArgumentException("max-dates-per-gap-run must be between 1 and 366");
		}
		if (reconciliationWindowDays < 1 || reconciliationWindowDays > 366) {
			throw new IllegalArgumentException("reconciliation-window-days must be between 1 and 366");
		}
	}

}
