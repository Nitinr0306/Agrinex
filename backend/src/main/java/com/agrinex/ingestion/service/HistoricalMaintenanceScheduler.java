package com.agrinex.ingestion.service;

import com.agrinex.common.time.AgrinexCalendar;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The daily gap check and the weekly reconciliation of HISTORICAL_PRICE report dates (ADR-0054 items 3 and 4), in
 * Asia/Kolkata. Active only while scheduling is enabled ({@code agrinex.scheduling.enabled}); a paused dataset
 * skips both, and a RUNNING run of the dataset skips them until the next time.
 */
@Component
class HistoricalMaintenanceScheduler {

	private final IngestionControlService control;

	HistoricalMaintenanceScheduler(IngestionControlService control) {
		this.control = control;
	}

	@Scheduled(cron = "${agrinex.ingestion.maintenance.gap-check-cron}", zone = AgrinexCalendar.ZONE_ID)
	void runGapCheck() {
		this.control.runGapBackfill();
	}

	@Scheduled(cron = "${agrinex.ingestion.maintenance.reconciliation-cron}", zone = AgrinexCalendar.ZONE_ID)
	void runReconciliation() {
		this.control.runReconciliation();
	}

}
