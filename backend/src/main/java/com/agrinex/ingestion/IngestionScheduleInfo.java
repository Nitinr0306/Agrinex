package com.agrinex.ingestion;

import java.util.List;

/**
 * The read-only ingestion configuration admins may view (ADR-0020 items 2 and 3): the schedule's local run times
 * in Asia/Kolkata, the historical trailing window, the coverage states and whether the government API key is
 * configured. The key itself is never exposed.
 */
public record IngestionScheduleInfo(String timeZone, List<String> runTimes, int historicalWindowDaysBack,
		List<String> coverageStates, boolean governmentApiKeyConfigured) {
}
