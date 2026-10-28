package com.agrinex.ingestion.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.agrinex.common.web.PageParams;
import com.agrinex.common.web.PageResponse;
import com.agrinex.ingestion.DatasetHealthView;
import com.agrinex.ingestion.DatasetScheduleView;
import com.agrinex.ingestion.HistoricalCoverageView;
import com.agrinex.ingestion.IngestionApi;
import com.agrinex.ingestion.IngestionErrorType;
import com.agrinex.ingestion.IngestionErrorView;
import com.agrinex.ingestion.IngestionRunStatus;
import com.agrinex.ingestion.IngestionRunView;
import com.agrinex.ingestion.IngestionScheduleInfo;
import com.agrinex.price.SourceDataset;

import org.springframework.stereotype.Service;

/** The ingestion module's public interface, delegating to its control and query services. */
@Service
class IngestionFacade implements IngestionApi {

	private final IngestionControlService control;

	private final IngestionQueryService queries;

	IngestionFacade(IngestionControlService control, IngestionQueryService queries) {
		this.control = control;
		this.queries = queries;
	}

	@Override
	public IngestionRunView triggerRun(SourceDataset datasetType, LocalDate fromDate, LocalDate toDate) {
		UUID runId = this.control.triggerRun(datasetType, fromDate, toDate);
		return this.queries.findRun(runId).orElseThrow(() -> new IllegalStateException("The started run is missing"));
	}

	@Override
	public DatasetScheduleView pause(SourceDataset datasetType, String reason) {
		return this.control.pause(datasetType, reason);
	}

	@Override
	public DatasetScheduleView resume(SourceDataset datasetType) {
		return this.control.resume(datasetType);
	}

	@Override
	public PageResponse<IngestionRunView> listRuns(SourceDataset datasetType, IngestionRunStatus status,
			PageParams page) {
		return this.queries.listRuns(datasetType, status, page);
	}

	@Override
	public Optional<IngestionRunView> findRun(UUID runId) {
		return this.queries.findRun(runId);
	}

	@Override
	public Optional<PageResponse<IngestionErrorView>> listErrors(UUID runId, IngestionErrorType errorType,
			String errorCode, PageParams page) {
		return this.queries.listErrors(runId, errorType, errorCode, page);
	}

	@Override
	public List<DatasetHealthView> datasetHealth() {
		return this.queries.datasetHealth();
	}

	@Override
	public IngestionScheduleInfo scheduleInfo() {
		return this.queries.scheduleInfo();
	}

	@Override
	public HistoricalCoverageView historicalCoverage() {
		return this.queries.historicalCoverage();
	}

}
