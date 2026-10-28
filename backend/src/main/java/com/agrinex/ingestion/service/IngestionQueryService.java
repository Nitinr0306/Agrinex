package com.agrinex.ingestion.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.agrinex.common.time.AgrinexCalendar;
import com.agrinex.common.web.PageParams;
import com.agrinex.common.web.PageResponse;
import com.agrinex.ingestion.DatasetHealthView;
import com.agrinex.ingestion.DatasetScheduleView;
import com.agrinex.ingestion.HistoricalCoverageView;
import com.agrinex.ingestion.HistoricalCoverageView.DateEntry;
import com.agrinex.ingestion.HistoricalCoverageView.RunProgress;
import com.agrinex.ingestion.HistoricalCoverageView.StatusCounts;
import com.agrinex.ingestion.IngestionErrorType;
import com.agrinex.ingestion.IngestionErrorView;
import com.agrinex.ingestion.IngestionRunStatus;
import com.agrinex.ingestion.IngestionRunView;
import com.agrinex.ingestion.IngestionScheduleInfo;
import com.agrinex.ingestion.ReportDateStatus;
import com.agrinex.ingestion.config.HistoricalMaintenanceProperties;
import com.agrinex.ingestion.config.IngestionProperties;
import com.agrinex.ingestion.domain.IngestionErrorEntity;
import com.agrinex.ingestion.domain.IngestionReportDateEntity;
import com.agrinex.ingestion.domain.IngestionRunEntity;
import com.agrinex.ingestion.mapper.IngestionMapper;
import com.agrinex.ingestion.repository.IngestionErrorRepository;
import com.agrinex.ingestion.repository.IngestionReportDateRepository;
import com.agrinex.ingestion.repository.IngestionRunRepository;
import com.agrinex.ingestion.repository.IngestionSourceSettingRepository;
import com.agrinex.ingestion.repository.RunErrorCount;
import com.agrinex.price.PriceApi;
import com.agrinex.price.SourceDataset;
import com.agrinex.price.SourceDatasetInfo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read side of ingestion for the admin views (FR-18; spec section 30, 46): run history, run errors, per-dataset
 * health, the read-only schedule (ADR-0011, ADR-0012, ADR-0019 item 7, ADR-0020 item 2) and the coverage of the
 * historical report dates (ADR-0054 item 6). Every count comes from the database, so it matches it exactly (spec
 * section 48).
 */
@Service
@Transactional(readOnly = true)
public class IngestionQueryService {

	/** startedAt descending, then id (contract listIngestionRuns). */
	static final Sort RUN_ORDER = Sort.by(Sort.Order.desc("startedAt"), Sort.Order.asc("id"));

	/** createdAt ascending, then id (contract listIngestionRunErrors). */
	static final Sort ERROR_ORDER = Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id"));

	private static final DateTimeFormatter RUN_TIME = DateTimeFormatter.ofPattern("HH:mm");

	private final IngestionRunRepository runs;

	private final IngestionErrorRepository errors;

	private final IngestionSourceSettingRepository settings;

	private final PriceApi prices;

	private final PayloadJson payloadJson;

	private final IngestionProperties properties;

	private final HistoricalMaintenanceProperties maintenance;

	private final HistoricalCoverageReader coverage;

	private final IngestionReportDateRepository reportDates;

	private final AgrinexCalendar calendar;

	IngestionQueryService(IngestionRunRepository runs, IngestionErrorRepository errors,
			IngestionSourceSettingRepository settings, PriceApi prices, PayloadJson payloadJson,
			IngestionProperties properties, HistoricalMaintenanceProperties maintenance,
			HistoricalCoverageReader coverage, IngestionReportDateRepository reportDates, AgrinexCalendar calendar) {
		this.runs = runs;
		this.errors = errors;
		this.settings = settings;
		this.prices = prices;
		this.payloadJson = payloadJson;
		this.properties = properties;
		this.maintenance = maintenance;
		this.coverage = coverage;
		this.reportDates = reportDates;
		this.calendar = calendar;
	}

	public PageResponse<IngestionRunView> listRuns(SourceDataset datasetType, IngestionRunStatus status,
			PageParams page) {
		PageRequest request = PageRequest.of(page.page(), page.size(), RUN_ORDER);
		UUID datasetId = (datasetType != null) ? this.prices.sourceDataset(datasetType).id() : null;
		Page<IngestionRunEntity> result = (datasetId != null && status != null)
				? this.runs.findBySourceDatasetIdAndStatus(datasetId, status, request)
				: (datasetId != null) ? this.runs.findBySourceDatasetId(datasetId, request)
						: (status != null) ? this.runs.findByStatus(status, request) : this.runs.findAll(request);
		return page.toResponse(views(result.getContent()), result.getTotalElements());
	}

	public Optional<IngestionRunView> findRun(UUID runId) {
		return this.runs.findById(runId).map((run) -> views(List.of(run)).getFirst());
	}

	public Optional<PageResponse<IngestionErrorView>> listErrors(UUID runId, IngestionErrorType errorType,
			String errorCode, PageParams page) {
		if (!this.runs.existsById(runId)) {
			return Optional.empty();
		}
		PageRequest request = PageRequest.of(page.page(), page.size(), ERROR_ORDER);
		Page<IngestionErrorEntity> result = (errorType != null && errorCode != null)
				? this.errors.findByIngestionRunIdAndErrorTypeAndErrorCode(runId, errorType, errorCode, request)
				: (errorType != null) ? this.errors.findByIngestionRunIdAndErrorType(runId, errorType, request)
						: (errorCode != null) ? this.errors.findByIngestionRunIdAndErrorCode(runId, errorCode, request)
								: this.errors.findByIngestionRunId(runId, request);
		List<IngestionErrorView> items = result.getContent()
			.stream()
			.map((error) -> IngestionMapper.toView(error, this.payloadJson.compact(error.getRawPayload())))
			.toList();
		return Optional.of(page.toResponse(items, result.getTotalElements()));
	}

	/** One entry per dataset, CURRENT_PRICE first (contract IngestionHealthResponse). */
	public List<DatasetHealthView> datasetHealth() {
		return this.prices.sourceDatasets()
			.stream()
			.sorted(Comparator.comparing(SourceDatasetInfo::type))
			.map(this::health)
			.toList();
	}

	public IngestionScheduleInfo scheduleInfo() {
		return new IngestionScheduleInfo(AgrinexCalendar.ZONE_ID, runTimes(), this.properties.historicalWindowDaysBack(),
				List.copyOf(this.properties.coverageStates()), this.properties.apiKeyConfigured());
	}

	/** Coverage of the HISTORICAL_PRICE report dates (ADR-0054 item 6). */
	public HistoricalCoverageView historicalCoverage() {
		LocalDate today = this.calendar.today();
		UUID datasetId = this.prices.sourceDataset(SourceDataset.HISTORICAL_PRICE).id();
		HistoricalCoverageReader.Coverage covered = this.coverage.read(datasetId, today);
		Map<ReportDateStatus, Long> counts = covered.statuses()
			.values()
			.stream()
			.collect(Collectors.groupingBy((status) -> status, Collectors.counting()));
		List<DateEntry> incomplete = covered.statuses()
			.descendingMap()
			.entrySet()
			.stream()
			.filter((entry) -> entry.getValue().incomplete())
			.map((entry) -> entry(entry.getKey(), entry.getValue(), covered.entries().get(entry.getKey())))
			.toList();
		boolean paused = this.settings.findBySourceDatasetId(datasetId)
			.map((setting) -> setting.isPaused())
			.orElse(false);
		return new HistoricalCoverageView(this.calendar.now(), this.coverage.coverageFrom(today), today,
				this.coverage.settleDays(), paused,
				new StatusCounts(count(counts, ReportDateStatus.SETTLED), count(counts, ReportDateStatus.PENDING),
						count(counts, ReportDateStatus.UNSETTLED), count(counts, ReportDateStatus.FAILED),
						count(counts, ReportDateStatus.MISSING)),
				incomplete, runningProgress(datasetId), nextRun(this.maintenance.gapCheckCron()),
				nextRun(this.maintenance.reconciliationCron()), this.maintenance.maxDatesPerGapRun(),
				this.maintenance.reconciliationWindowDays());
	}

	private static DateEntry entry(LocalDate date, ReportDateStatus status, IngestionReportDateEntity ledger) {
		return (ledger == null) ? new DateEntry(date, status, null, null, null, 0)
				: new DateEntry(date, status, ledger.getLastAttemptAt(), ledger.getLastSuccessAt(),
						ledger.getSuccessRecordsFetched(), ledger.getConsecutiveFailures());
	}

	private static int count(Map<ReportDateStatus, Long> counts, ReportDateStatus status) {
		return counts.getOrDefault(status, 0L).intValue();
	}

	/** The running HISTORICAL run's report dates attempted so far, out of those in its scope. */
	private RunProgress runningProgress(UUID datasetId) {
		return this.runs.findFirstBySourceDatasetIdAndStatus(datasetId, IngestionRunStatus.RUNNING)
			.map((run) -> views(List.of(run)).getFirst())
			.filter((run) -> run.reportDateFrom() != null && run.reportDateTo() != null)
			.map((run) -> new RunProgress(run.id(), run.purpose(), run.reportDateFrom(), run.reportDateTo(),
					(int) ChronoUnit.DAYS.between(run.reportDateFrom(), run.reportDateTo()) + 1,
					this.reportDates.countByLastAttemptRunId(run.id())))
			.orElse(null);
	}

	/** The next time of a cron expression in Asia/Kolkata. */
	private Instant nextRun(String cronExpression) {
		ZonedDateTime next = CronExpression.parse(cronExpression)
			.next(this.calendar.now().atZone(AgrinexCalendar.ZONE));
		return (next != null) ? next.toInstant() : null;
	}

	private DatasetHealthView health(SourceDatasetInfo dataset) {
		UUID datasetId = dataset.id();
		DatasetScheduleView schedule = this.settings.findBySourceDatasetId(datasetId)
			.map((setting) -> IngestionMapper.toView(setting, dataset.type()))
			.orElseThrow(() -> new IllegalStateException("No ingestion setting exists for " + dataset.type()));
		UUID runningRunId = this.runs.findFirstBySourceDatasetIdAndStatus(datasetId, IngestionRunStatus.RUNNING)
			.map(IngestionRunEntity::getId)
			.orElse(null);
		Optional<IngestionRunEntity> latestCompleted = this.runs
			.findFirstBySourceDatasetIdAndStatusNotOrderByStartedAtDescIdAsc(datasetId, IngestionRunStatus.RUNNING);
		long consecutiveFailures = latestCompleted.isEmpty() ? 0
				: this.runs.findLatestStartedAt(datasetId, RunLifecycle.LOADED)
					.map((lastLoaded) -> this.runs.countBySourceDatasetIdAndStatusAndStartedAtAfter(datasetId,
							IngestionRunStatus.FAILED, lastLoaded))
					.orElseGet(() -> this.runs.countBySourceDatasetIdAndStatus(datasetId, IngestionRunStatus.FAILED));
		LocalDate latestReportDate = this.prices.latestReportDate(datasetId).orElse(null);
		return new DatasetHealthView(dataset, schedule, runningRunId,
				latestCompleted.map((run) -> views(List.of(run)).getFirst()).orElse(null),
				this.runs.findLatestFinishedAt(datasetId, RunLifecycle.LOADED).orElse(null), consecutiveFailures,
				latestReportDate);
	}

	private List<IngestionRunView> views(List<IngestionRunEntity> entities) {
		Map<UUID, SourceDataset> types = this.prices.sourceDatasets()
			.stream()
			.collect(Collectors.toMap(SourceDatasetInfo::id, SourceDatasetInfo::type));
		Map<UUID, Long> errorCounts = errorCounts(entities.stream().map(IngestionRunEntity::getId).toList());
		return entities.stream()
			.map((run) -> IngestionMapper.toView(run, types.get(run.getSourceDatasetId()),
					this.payloadJson.toMap(run.getRequestScope()), errorCounts.getOrDefault(run.getId(), 0L)))
			.toList();
	}

	private Map<UUID, Long> errorCounts(Collection<UUID> runIds) {
		if (runIds.isEmpty()) {
			return Map.of();
		}
		return this.errors.countByRuns(runIds)
			.stream()
			.collect(Collectors.toMap(RunErrorCount::runId, RunErrorCount::errors));
	}

	/** The local times of one day's runs, read from the configured cron expression (ADR-0011 item 1). */
	private List<String> runTimes() {
		CronExpression cron = CronExpression.parse(this.properties.scheduleCron());
		LocalDate day = this.calendar.today();
		ZonedDateTime cursor = day.atStartOfDay(AgrinexCalendar.ZONE).minusNanos(1);
		List<String> times = new ArrayList<>();
		while (true) {
			ZonedDateTime next = cron.next(cursor);
			if (next == null || !next.toLocalDate().equals(day)) {
				return times.stream().distinct().toList();
			}
			times.add(next.toLocalTime().withSecond(0).withNano(0).format(RUN_TIME));
			cursor = next;
		}
	}

}
