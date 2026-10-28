package com.agrinex.ingestion.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agrinex.common.error.ApiErrorResponse.FieldError;
import com.agrinex.common.error.ApiException;
import com.agrinex.common.error.FieldErrorCode;
import com.agrinex.common.time.AgrinexCalendar;
import com.agrinex.ingestion.DatasetScheduleView;
import com.agrinex.ingestion.IngestionRunStatus;
import com.agrinex.ingestion.IngestionRunView;
import com.agrinex.ingestion.RunPurpose;
import com.agrinex.ingestion.TriggeredBy;
import com.agrinex.ingestion.config.HistoricalMaintenanceProperties;
import com.agrinex.ingestion.config.IngestionConfiguration;
import com.agrinex.ingestion.config.IngestionProperties;
import com.agrinex.ingestion.domain.IngestionErrorCode;
import com.agrinex.ingestion.domain.IngestionSourceSettingEntity;
import com.agrinex.ingestion.domain.RunCounters;
import com.agrinex.ingestion.mapper.IngestionMapper;
import com.agrinex.ingestion.repository.IngestionSourceSettingRepository;
import com.agrinex.price.PriceApi;
import com.agrinex.price.SourceDataset;
import com.agrinex.price.SourceDatasetInfo;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Starts ingestion runs (ADR-0011 items 1 to 4; the gap backfill and reconciliation of ADR-0054 items 3 to 5) and
 * pauses or resumes scheduled runs (ADR-0020 item 1). At most one run per dataset is RUNNING: a manual trigger then
 * answers 409 and a scheduler or startup start is skipped.
 */
@Service
public class IngestionControlService {

	private static final Logger log = LoggerFactory.getLogger(IngestionControlService.class);

	/** ADR-0011 item 3: a manual HISTORICAL range covers at most 366 report dates, counted inclusively. */
	static final int MAX_REPORT_DATES = 366;

	/** ADR-0011 item 2: no startup run when a CURRENT run loaded data within the previous 6 hours. */
	static final Duration STARTUP_LOOKBACK = Duration.ofHours(6);

	private final PriceApi prices;

	private final RunLifecycle lifecycle;

	private final IngestionRunner runner;

	private final ErrorLog errorLog;

	private final IngestionSourceSettingRepository settings;

	private final PayloadJson payloadJson;

	private final IngestionProperties properties;

	private final HistoricalMaintenanceProperties maintenance;

	private final HistoricalCoverageReader coverage;

	private final TaskExecutor executor;

	private final AgrinexCalendar calendar;

	IngestionControlService(PriceApi prices, RunLifecycle lifecycle, IngestionRunner runner, ErrorLog errorLog,
			IngestionSourceSettingRepository settings, PayloadJson payloadJson, IngestionProperties properties,
			HistoricalMaintenanceProperties maintenance, HistoricalCoverageReader coverage,
			@Qualifier(IngestionConfiguration.INGESTION_EXECUTOR) TaskExecutor executor, AgrinexCalendar calendar) {
		this.prices = prices;
		this.lifecycle = lifecycle;
		this.runner = runner;
		this.errorLog = errorLog;
		this.settings = settings;
		this.payloadJson = payloadJson;
		this.properties = properties;
		this.maintenance = maintenance;
		this.coverage = coverage;
		this.executor = executor;
		this.calendar = calendar;
	}

	/**
	 * A MANUAL run, started in the background (ADR-0011 item 3). It works while the schedule is paused.
	 * @return the id of the RUNNING run
	 */
	public UUID triggerRun(SourceDataset datasetType, LocalDate fromDate, LocalDate toDate) {
		List<LocalDate> reportDates = (datasetType == SourceDataset.HISTORICAL_PRICE)
				? manualReportDates(fromDate, toDate) : rejectDates(fromDate, toDate);
		SourceDatasetInfo dataset = this.prices.sourceDataset(datasetType);
		RunPurpose purpose = (datasetType == SourceDataset.HISTORICAL_PRICE) ? RunPurpose.MANUAL : null;
		UUID runId = start(dataset, TriggeredBy.MANUAL, reportDates, purpose)
			.orElseThrow(() -> ApiException.conflict("A run of this dataset is already RUNNING."));
		submit(new StartedRun(runId, dataset, TriggeredBy.MANUAL, reportDates));
		return runId;
	}

	/** One scheduled cycle (ADR-0011 item 1): a CURRENT run, then a HISTORICAL run over the trailing window. */
	public void runScheduledCycle() {
		runNow(SourceDataset.CURRENT_PRICE, List.of(), null);
		runNow(SourceDataset.HISTORICAL_PRICE, trailingWindow(), RunPurpose.TRAILING_WINDOW);
	}

	/**
	 * The daily gap check (ADR-0054 item 3): a HISTORICAL run over the newest incomplete report dates, or nothing
	 * when every covered date at the settle age is complete.
	 */
	public void runGapBackfill() {
		LocalDate today = this.calendar.today();
		UUID datasetId = this.prices.sourceDataset(SourceDataset.HISTORICAL_PRICE).id();
		Optional<List<LocalDate>> gap = HistoricalCoverage.gapRange(this.coverage.read(datasetId, today).statuses(),
				today, this.coverage.settleDays(), this.maintenance.maxDatesPerGapRun());
		if (gap.isEmpty()) {
			log.atInfo().log("Gap check: every covered report date is complete");
			return;
		}
		log.atInfo()
			.addKeyValue("reportDateFrom", gap.get().getFirst())
			.addKeyValue("reportDateTo", gap.get().getLast())
			.log("Gap check: backfilling incomplete report dates");
		runNow(SourceDataset.HISTORICAL_PRICE, gap.get(), RunPurpose.GAP_BACKFILL);
	}

	/** The weekly reconciliation (ADR-0054 item 4): the report dates today - window to yesterday, again. */
	public void runReconciliation() {
		LocalDate today = this.calendar.today();
		runNow(SourceDataset.HISTORICAL_PRICE,
				dates(today.minusDays(this.maintenance.reconciliationWindowDays()), today.minusDays(1)),
				RunPurpose.RECONCILIATION);
	}

	/** The STARTUP run (ADR-0011 item 2), skipped when paused or when a run loaded data recently. */
	public void startStartupRun() {
		SourceDatasetInfo dataset = this.prices.sourceDataset(SourceDataset.CURRENT_PRICE);
		if (isPaused(dataset.id())) {
			log.atInfo().addKeyValue("datasetType", dataset.type()).log("Startup ingestion skipped: dataset paused");
			return;
		}
		if (this.lifecycle.loadedSince(dataset.id(), this.calendar.now().minus(STARTUP_LOOKBACK))) {
			log.atInfo().log("Startup ingestion skipped: a run loaded data within the last 6 hours");
			return;
		}
		start(dataset, TriggeredBy.STARTUP, List.of(), null)
			.ifPresent((runId) -> submit(new StartedRun(runId, dataset, TriggeredBy.STARTUP, List.of())));
	}

	@Transactional
	public DatasetScheduleView pause(SourceDataset datasetType, String reason) {
		IngestionSourceSettingEntity setting = lockSetting(datasetType);
		if (setting.isPaused()) {
			throw ApiException.conflict("Scheduled runs of this dataset are already paused.");
		}
		setting.pause(reason, this.calendar.now());
		return IngestionMapper.toView(setting, datasetType);
	}

	@Transactional
	public DatasetScheduleView resume(SourceDataset datasetType) {
		IngestionSourceSettingEntity setting = lockSetting(datasetType);
		if (!setting.isPaused()) {
			throw ApiException.conflict("Scheduled runs of this dataset are not paused.");
		}
		setting.resume(this.calendar.now());
		return IngestionMapper.toView(setting, datasetType);
	}

	private void runNow(SourceDataset datasetType, List<LocalDate> reportDates, RunPurpose purpose) {
		SourceDatasetInfo dataset = this.prices.sourceDataset(datasetType);
		if (isPaused(dataset.id())) {
			log.atInfo().addKeyValue("datasetType", datasetType).log("Scheduled ingestion skipped: dataset paused");
			return;
		}
		Optional<UUID> runId = start(dataset, TriggeredBy.SCHEDULER, reportDates, purpose);
		if (runId.isEmpty()) {
			log.atInfo().addKeyValue("datasetType", datasetType).log("Scheduled ingestion skipped: a run is RUNNING");
			return;
		}
		this.runner.execute(new StartedRun(runId.get(), dataset, TriggeredBy.SCHEDULER, reportDates));
	}

	/** The new run's id, or empty when a run of the dataset is RUNNING (ADR-0011 item 4). */
	private Optional<UUID> start(SourceDatasetInfo dataset, TriggeredBy triggeredBy, List<LocalDate> reportDates,
			RunPurpose purpose) {
		if (this.lifecycle.isRunning(dataset.id())) {
			return Optional.empty();
		}
		try {
			return Optional.of(this.lifecycle.start(dataset.id(), triggeredBy, requestScope(reportDates, purpose)));
		}
		catch (DataIntegrityViolationException alreadyRunning) {
			return Optional.empty();
		}
	}

	private void submit(StartedRun run) {
		try {
			this.executor.execute(() -> this.runner.execute(run));
		}
		catch (TaskRejectedException ex) {
			this.errorLog.recordFailure(run.runId(), IngestionErrorCode.UNEXPECTED_FAILURE,
					"The run could not be started in the background", null, null, null);
			this.lifecycle.finish(run.runId(), IngestionRunStatus.FAILED, new RunCounters());
			throw new IllegalStateException("The ingestion run could not be started in the background", ex);
		}
	}

	/** Request parameters of a run, without credentials (ADR-0020 item 5), with its purpose (ADR-0054 item 5). */
	private String requestScope(List<LocalDate> reportDates, RunPurpose purpose) {
		Map<String, Object> scope = new LinkedHashMap<>();
		scope.put("sourceStates", this.properties.coverageStates());
		scope.put("pageSize", this.properties.pageSize());
		if (!reportDates.isEmpty()) {
			scope.put(IngestionRunView.SCOPE_REPORT_DATE_FROM, reportDates.getFirst().toString());
			scope.put(IngestionRunView.SCOPE_REPORT_DATE_TO, reportDates.getLast().toString());
		}
		if (purpose != null) {
			scope.put(IngestionRunView.SCOPE_PURPOSE, purpose.name());
		}
		return this.payloadJson.write(scope);
	}

	private List<LocalDate> trailingWindow() {
		LocalDate today = this.calendar.today();
		return dates(today.minusDays(this.properties.historicalWindowDaysBack()), today);
	}

	/** ADR-0011 item 3; contract TriggerIngestionRequest. Range errors are named on toDate, the later field. */
	private List<LocalDate> manualReportDates(LocalDate fromDate, LocalDate toDate) {
		if (fromDate == null && toDate == null) {
			return trailingWindow();
		}
		LocalDate today = this.calendar.today();
		String problem = (fromDate == null || toDate == null) ? "fromDate and toDate must be given together"
				: toDate.isBefore(fromDate) ? "must not be before fromDate"
						: toDate.isAfter(today) ? "must not be after today (Asia/Kolkata)"
								: (ChronoUnit.DAYS.between(fromDate, toDate) + 1 > MAX_REPORT_DATES)
										? "the range covers more than 366 report dates" : null;
		if (problem != null) {
			throw ApiException.validation("toDate", FieldErrorCode.INVALID_RANGE, problem);
		}
		return dates(fromDate, toDate);
	}

	private static List<LocalDate> rejectDates(LocalDate fromDate, LocalDate toDate) {
		List<FieldError> errors = new ArrayList<>();
		if (fromDate != null) {
			errors.add(new FieldError("fromDate", FieldErrorCode.NOT_ALLOWED, "is allowed only for HISTORICAL_PRICE"));
		}
		if (toDate != null) {
			errors.add(new FieldError("toDate", FieldErrorCode.NOT_ALLOWED, "is allowed only for HISTORICAL_PRICE"));
		}
		if (!errors.isEmpty()) {
			throw ApiException.validation(errors);
		}
		return List.of();
	}

	private static List<LocalDate> dates(LocalDate from, LocalDate to) {
		return Stream.iterate(from, (date) -> !date.isAfter(to), (date) -> date.plusDays(1)).toList();
	}

	private boolean isPaused(UUID sourceDatasetId) {
		return this.settings.findBySourceDatasetId(sourceDatasetId)
			.map(IngestionSourceSettingEntity::isPaused)
			.orElse(false);
	}

	private IngestionSourceSettingEntity lockSetting(SourceDataset datasetType) {
		UUID datasetId = this.prices.sourceDataset(datasetType).id();
		return this.settings.findForUpdate(datasetId)
			.orElseThrow(() -> new IllegalStateException("No ingestion setting exists for " + datasetType));
	}

}
