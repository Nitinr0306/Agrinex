package com.agrinex.ingestion.mapper;

import java.util.Map;

import com.agrinex.ingestion.DatasetScheduleView;
import com.agrinex.ingestion.IngestionErrorView;
import com.agrinex.ingestion.IngestionRunView;
import com.agrinex.ingestion.domain.IngestionErrorEntity;
import com.agrinex.ingestion.domain.IngestionRunEntity;
import com.agrinex.ingestion.domain.IngestionSourceSettingEntity;
import com.agrinex.price.SourceDataset;

/** Maps ingestion entities to the module's public records. */
public final class IngestionMapper {

	/** Contract IngestionErrorResponse.rawPayloadExcerpt: at most 4,000 characters (a contract-design limit). */
	public static final int RAW_PAYLOAD_EXCERPT_LENGTH = 4000;

	private IngestionMapper() {
	}

	/** {@code requestScope} is the run's stored request scope, parsed. */
	public static IngestionRunView toView(IngestionRunEntity run, SourceDataset datasetType,
			Map<String, Object> requestScope, long errorCount) {
		return new IngestionRunView(run.getId(), datasetType, run.getTriggeredBy(), run.getStatus(), run.getStartedAt(),
				run.getFinishedAt(), requestScope, run.getSourceTotalRecords(), run.getPagesFetched(),
				run.getPagesFailed(), run.getRecordsFetched(), run.getRecordsInserted(), run.getRecordsUpdated(),
				run.getDuplicatesSkipped(), run.getValidationErrorsCount(), run.getRecordsConflicted(),
				run.getRecordsOutOfScope(), errorCount);
	}

	/** {@code compactPayload} is the stored payload as compact JSON text, or null when none was kept. */
	public static IngestionErrorView toView(IngestionErrorEntity error, String compactPayload) {
		boolean truncated = compactPayload != null
				&& compactPayload.codePointCount(0, compactPayload.length()) > RAW_PAYLOAD_EXCERPT_LENGTH;
		String excerpt = truncated
				? compactPayload.substring(0, compactPayload.offsetByCodePoints(0, RAW_PAYLOAD_EXCERPT_LENGTH))
				: compactPayload;
		return new IngestionErrorView(error.getId(), error.getIngestionRunId(), error.getErrorType(),
				error.getErrorCode(), error.getErrorMessage(), error.getPageOffset(), error.getRequestReportDate(),
				excerpt, truncated, error.getCreatedAt());
	}

	public static DatasetScheduleView toView(IngestionSourceSettingEntity setting, SourceDataset datasetType) {
		return new DatasetScheduleView(datasetType, setting.isPaused(), setting.getPausedReason(),
				setting.getUpdatedAt());
	}

}
