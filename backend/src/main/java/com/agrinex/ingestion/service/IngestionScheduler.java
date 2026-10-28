package com.agrinex.ingestion.service;

import com.agrinex.common.time.AgrinexCalendar;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The 6-hourly schedule at 00:00, 06:00, 12:00 and 18:00 Asia/Kolkata (spec section 15; ADR-0011 item 1). Active
 * only while scheduling is enabled ({@code agrinex.scheduling.enabled}).
 */
@Component
class IngestionScheduler {

	private final IngestionControlService control;

	IngestionScheduler(IngestionControlService control) {
		this.control = control;
	}

	@Scheduled(cron = "${agrinex.ingestion.schedule-cron}", zone = AgrinexCalendar.ZONE_ID)
	void runScheduledCycle() {
		this.control.runScheduledCycle();
	}

}
