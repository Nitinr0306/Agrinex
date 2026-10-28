package com.agrinex.admin.web.dto;

import java.time.Instant;

import com.agrinex.ingestion.DatasetScheduleView;
import com.agrinex.price.SourceDataset;

/** Contract DatasetScheduleState: {@code pausedReason} is present exactly while paused (ADR-0020). */
public record DatasetScheduleState(SourceDataset datasetType, boolean paused, String pausedReason, Instant updatedAt) {

	public static DatasetScheduleState from(DatasetScheduleView view) {
		return new DatasetScheduleState(view.datasetType(), view.paused(), view.pausedReason(), view.updatedAt());
	}

}
