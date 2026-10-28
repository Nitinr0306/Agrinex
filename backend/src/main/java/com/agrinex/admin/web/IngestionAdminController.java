package com.agrinex.admin.web;

import java.net.URI;
import java.util.UUID;
import java.util.regex.Pattern;

import jakarta.validation.Valid;

import com.agrinex.admin.service.IngestionAdminService;
import com.agrinex.admin.web.dto.DatasetScheduleState;
import com.agrinex.admin.web.dto.HistoricalCoverageResponse;
import com.agrinex.admin.web.dto.IngestionErrorResponse;
import com.agrinex.admin.web.dto.IngestionHealthResponse;
import com.agrinex.admin.web.dto.IngestionRunResponse;
import com.agrinex.admin.web.dto.PauseScheduleRequest;
import com.agrinex.admin.web.dto.TriggerIngestionRequest;
import com.agrinex.admin.web.dto.TriggerResponse;
import com.agrinex.auth.CurrentAccount;
import com.agrinex.common.error.ApiException;
import com.agrinex.common.error.FieldErrorCode;
import com.agrinex.common.web.PageParams;
import com.agrinex.common.web.PageResponse;
import com.agrinex.ingestion.IngestionErrorType;
import com.agrinex.ingestion.IngestionRunStatus;
import com.agrinex.ingestion.IngestionRunView;
import com.agrinex.price.SourceDataset;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ingestion health, run history, errors, manual trigger, pause and resume (contract tag Admin - Ingestion; FR-17,
 * FR-18; ADR-0011, ADR-0020). ADMIN only; every change is audited.
 */
@RestController
@RequestMapping("/api/v1/admin/ingestion")
@PreAuthorize("hasRole('ADMIN')")
public class IngestionAdminController {

	static final String RUNS_PATH = "/api/v1/admin/ingestion/runs/";

	private static final Pattern ERROR_CODE = Pattern.compile("[A-Z][A-Z0-9_]{0,49}");

	private final IngestionAdminService ingestion;

	IngestionAdminController(IngestionAdminService ingestion) {
		this.ingestion = ingestion;
	}

	@GetMapping("/health")
	public IngestionHealthResponse getIngestionHealth() {
		return IngestionHealthResponse.from(this.ingestion.health());
	}

	/** Coverage of the historical report dates and the progress of maintenance runs (ADR-0054 item 6). */
	@GetMapping("/historical-coverage")
	public HistoricalCoverageResponse getHistoricalCoverage() {
		return HistoricalCoverageResponse.from(this.ingestion.historicalCoverage());
	}

	@GetMapping("/runs")
	public PageResponse<IngestionRunResponse> listIngestionRuns(
			@RequestParam(required = false) SourceDataset datasetType,
			@RequestParam(required = false) IngestionRunStatus status, @RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size) {
		return this.ingestion.listRuns(datasetType, status, PageParams.of(page, size)).map(IngestionRunResponse::from);
	}

	@GetMapping("/runs/{runId}")
	public IngestionRunResponse getIngestionRun(@PathVariable UUID runId) {
		return IngestionRunResponse.from(this.ingestion.run(runId));
	}

	@GetMapping("/runs/{runId}/errors")
	public PageResponse<IngestionErrorResponse> listIngestionRunErrors(@PathVariable UUID runId,
			@RequestParam(required = false) IngestionErrorType errorType,
			@RequestParam(required = false) String errorCode, @RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size) {
		return this.ingestion.runErrors(runId, errorType, validatedErrorCode(errorCode), PageParams.of(page, size))
			.map(IngestionErrorResponse::from);
	}

	/** Contract IngestionErrorCode: UPPER_SNAKE, at most 50 characters. */
	private static String validatedErrorCode(String errorCode) {
		if (errorCode != null && !ERROR_CODE.matcher(errorCode).matches()) {
			throw ApiException.validation("errorCode", FieldErrorCode.INVALID_FORMAT, "must be an UPPER_SNAKE code");
		}
		return errorCode;
	}

	@PostMapping("/trigger")
	public ResponseEntity<TriggerResponse> triggerIngestion(@AuthenticationPrincipal CurrentAccount caller,
			@Valid @RequestBody TriggerIngestionRequest request) {
		IngestionRunView run = this.ingestion.trigger(caller.userId(), request.datasetType(), request.fromDate(),
				request.toDate());
		return ResponseEntity.accepted().location(URI.create(RUNS_PATH + run.id())).body(TriggerResponse.from(run));
	}

	@PostMapping("/datasets/{datasetType}/pause")
	public DatasetScheduleState pauseScheduledIngestion(@AuthenticationPrincipal CurrentAccount caller,
			@PathVariable SourceDataset datasetType, @Valid @RequestBody PauseScheduleRequest request) {
		return DatasetScheduleState.from(this.ingestion.pause(caller.userId(), datasetType, request.reason()));
	}

	@PostMapping("/datasets/{datasetType}/resume")
	public DatasetScheduleState resumeScheduledIngestion(@AuthenticationPrincipal CurrentAccount caller,
			@PathVariable SourceDataset datasetType) {
		return DatasetScheduleState.from(this.ingestion.resume(caller.userId(), datasetType));
	}

}
