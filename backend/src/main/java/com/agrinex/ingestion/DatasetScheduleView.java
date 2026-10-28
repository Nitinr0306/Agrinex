package com.agrinex.ingestion;

import java.time.Instant;

import com.agrinex.price.SourceDataset;

/** Pause state of a dataset's scheduled runs (contract DatasetScheduleState; ADR-0020 item 1). */
public record DatasetScheduleView(SourceDataset datasetType, boolean paused, String pausedReason, Instant updatedAt) {
}
