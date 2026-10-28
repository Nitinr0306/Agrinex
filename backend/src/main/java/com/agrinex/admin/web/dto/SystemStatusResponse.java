package com.agrinex.admin.web.dto;

import java.time.Instant;
import java.util.List;

import com.agrinex.admin.service.SystemStatusService.ComponentHealth;
import com.agrinex.admin.service.SystemStatusService.SystemStatus;
import com.agrinex.ingestion.IngestionScheduleInfo;
import com.agrinex.price.SourceDataset;
import com.agrinex.price.SourceDatasetInfo;

/** Contract SystemStatusResponse (ADR-0020 items 2 and 3). The API key value is never part of it. */
public record SystemStatusResponse(Instant generatedAt, Components components, String governmentApiKey,
		Schedule schedule, Coverage coverage, List<DatasetIdentity> datasets) {

	public static SystemStatusResponse from(SystemStatus status) {
		IngestionScheduleInfo schedule = status.schedule();
		return new SystemStatusResponse(status.generatedAt(), new Components(status.database(), status.redis()),
				schedule.governmentApiKeyConfigured() ? "CONFIGURED" : "NOT_CONFIGURED",
				new Schedule(schedule.timeZone(), schedule.runTimes(), schedule.historicalWindowDaysBack()),
				new Coverage(schedule.coverageStates()),
				status.datasets().stream().map(DatasetIdentity::of).toList());
	}

	/** Database and Redis health. */
	public record Components(ComponentHealth database, ComponentHealth redis) {
	}

	/** The configured ingestion schedule (ADR-0011 item 1). */
	public record Schedule(String timeZone, List<String> runTimes, int historicalWindowDaysBack) {
	}

	/** The coverage states (ADR-0006 item 1). */
	public record Coverage(List<String> sourceStates) {
	}

	/** Contract DatasetIdentity. */
	public record DatasetIdentity(SourceDataset datasetType, String resourceId, String title) {

		static DatasetIdentity of(SourceDatasetInfo dataset) {
			return new DatasetIdentity(dataset.type(), dataset.resourceId(), dataset.title());
		}

	}

}
