package com.agrinex.admin.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.agrinex.admin.domain.AuditAction;
import com.agrinex.admin.domain.AuditTarget;
import com.agrinex.common.error.ApiException;
import com.agrinex.common.time.AgrinexCalendar;
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
import com.agrinex.price.FreshnessPolicyPort;
import com.agrinex.price.MarketFreshnessSummary;
import com.agrinex.price.PriceApi;
import com.agrinex.price.SourceDataset;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin ingestion operations (FR-17, FR-18; ADR-0011, ADR-0020): health, run history and errors through the
 * ingestion module, and the audited trigger, pause and resume.
 */
@Service
public class IngestionAdminService {

	private final IngestionApi ingestion;

	private final PriceApi prices;

	private final FreshnessPolicyPort freshnessPolicy;

	private final AdminAuditLog audit;

	private final AgrinexCalendar calendar;

	IngestionAdminService(IngestionApi ingestion, PriceApi prices, FreshnessPolicyPort freshnessPolicy,
			AdminAuditLog audit, AgrinexCalendar calendar) {
		this.ingestion = ingestion;
		this.prices = prices;
		this.freshnessPolicy = freshnessPolicy;
		this.audit = audit;
		this.calendar = calendar;
	}

	public IngestionHealth health() {
		MarketFreshnessSummary freshness = this.prices.marketFreshness(this.calendar.today(),
				this.freshnessPolicy.activeThresholds());
		return new IngestionHealth(this.calendar.now(), this.ingestion.datasetHealth(), freshness);
	}

	public HistoricalCoverageView historicalCoverage() {
		return this.ingestion.historicalCoverage();
	}

	public PageResponse<IngestionRunView> listRuns(SourceDataset datasetType, IngestionRunStatus status,
			PageParams page) {
		return this.ingestion.listRuns(datasetType, status, page);
	}

	public IngestionRunView run(UUID runId) {
		return this.ingestion.findRun(runId).orElseThrow(() -> ApiException.notFound("The ingestion run does not exist."));
	}

	public PageResponse<IngestionErrorView> runErrors(UUID runId, IngestionErrorType errorType, String errorCode,
			PageParams page) {
		return this.ingestion.listErrors(runId, errorType, errorCode, page)
			.orElseThrow(() -> ApiException.notFound("The ingestion run does not exist."));
	}

	@Transactional
	public IngestionRunView trigger(UUID actorUserId, SourceDataset datasetType, LocalDate fromDate,
			LocalDate toDate) {
		IngestionRunView run = this.ingestion.triggerRun(datasetType, fromDate, toDate);
		Map<String, Object> details = new LinkedHashMap<>();
		details.put("datasetType", datasetType.name());
		details.put("fromDate", (fromDate != null) ? fromDate.toString() : null);
		details.put("toDate", (toDate != null) ? toDate.toString() : null);
		this.audit.record(actorUserId, AuditAction.INGESTION_TRIGGERED, AuditTarget.INGESTION_RUN, run.id(), details);
		return run;
	}

	@Transactional
	public DatasetScheduleView pause(UUID actorUserId, SourceDataset datasetType, String reason) {
		DatasetScheduleView paused = this.ingestion.pause(datasetType, reason);
		this.audit.record(actorUserId, AuditAction.INGESTION_PAUSED, AuditTarget.SOURCE_DATASET,
				this.prices.sourceDataset(datasetType).id(), Map.of("before", Map.of("paused", false), "after",
						Map.of("paused", true, "pausedReason", reason)));
		return paused;
	}

	@Transactional
	public DatasetScheduleView resume(UUID actorUserId, SourceDataset datasetType) {
		DatasetScheduleView resumed = this.ingestion.resume(datasetType);
		this.audit.record(actorUserId, AuditAction.INGESTION_RESUMED, AuditTarget.SOURCE_DATASET,
				this.prices.sourceDataset(datasetType).id(),
				Map.of("before", Map.of("paused", true), "after", Map.of("paused", false)));
		return resumed;
	}

	/** Contract IngestionHealthResponse: datasets with CURRENT_PRICE first, and market freshness. */
	public record IngestionHealth(Instant generatedAt, List<DatasetHealthView> datasets,
			MarketFreshnessSummary marketFreshness) {
	}

}
