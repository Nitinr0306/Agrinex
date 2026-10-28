package com.agrinex.admin.web.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.agrinex.admin.service.IngestionAdminService.IngestionHealth;
import com.agrinex.ingestion.DatasetHealthView;
import com.agrinex.price.MarketFreshnessSummary;
import com.agrinex.price.SourceDataset;

/** Contract IngestionHealthResponse (spec section 30; FR-18). Datasets are listed CURRENT_PRICE first. */
public record IngestionHealthResponse(Instant generatedAt, List<DatasetHealth> datasets,
		MarketFreshnessSummary marketFreshness) {

	public static IngestionHealthResponse from(IngestionHealth health) {
		return new IngestionHealthResponse(health.generatedAt(),
				health.datasets().stream().map(DatasetHealth::of).toList(), health.marketFreshness());
	}

	/** Contract DatasetHealth. */
	public record DatasetHealth(SourceDataset datasetType, String resourceId, String title,
			DatasetScheduleState schedule, UUID runningRunId, IngestionRunResponse latestCompletedRun,
			Instant lastSuccessfulRunAt, long consecutiveFailures, LocalDate latestReportDate) {

		static DatasetHealth of(DatasetHealthView view) {
			return new DatasetHealth(view.dataset().type(), view.dataset().resourceId(), view.dataset().title(),
					DatasetScheduleState.from(view.schedule()), view.runningRunId(),
					(view.latestCompletedRun() != null) ? IngestionRunResponse.from(view.latestCompletedRun()) : null,
					view.lastSuccessfulRunAt(), view.consecutiveFailures(), view.latestReportDate());
		}

	}

}
