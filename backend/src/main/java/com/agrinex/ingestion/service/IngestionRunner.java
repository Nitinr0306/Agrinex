package com.agrinex.ingestion.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agrinex.alert.AlertApi;
import com.agrinex.common.time.AgrinexCalendar;
import com.agrinex.ingestion.IngestionRunStatus;
import com.agrinex.ingestion.config.IngestionProperties;
import com.agrinex.ingestion.domain.IngestionErrorCode;
import com.agrinex.ingestion.domain.RunCounters;
import com.agrinex.ingestion.pipeline.DeliveryUnitProcessor;
import com.agrinex.ingestion.pipeline.ProcessedUnit;
import com.agrinex.ingestion.pipeline.ReferenceLookups;
import com.agrinex.ingestion.pipeline.SourceRecord;
import com.agrinex.ingestion.service.StartedRun.DeliveryUnit;
import com.agrinex.ingestion.source.DataGovClient;
import com.agrinex.ingestion.source.SourceFetchException;
import com.agrinex.ingestion.source.SourcePage;
import com.agrinex.ingestion.source.SourceSchema;

import org.springframework.stereotype.Service;

/**
 * Executes one started run (ADR-0011 items 6 to 10): each delivery unit is fetched page by page, then validated,
 * deduplicated and loaded in its own transaction; the run then ends SUCCESS, PARTIAL_FAILURE or FAILED, and a
 * run that inserted or revised observations triggers alert evaluation. A failed page ends its delivery unit;
 * what was fetched before it is still processed. Farmers keep reading stored data throughout (spec section 27).
 * Each HISTORICAL_PRICE unit's outcome is written to the report-date ledger (ADR-0054 item 1): FETCHED only when
 * every page was fetched and the unit loaded.
 */
@Service
public class IngestionRunner {

	private static final Logger log = LoggerFactory.getLogger(IngestionRunner.class);

	private final DataGovClient client;

	private final DeliveryUnitProcessor processor;

	private final UnitLoader unitLoader;

	private final RunLifecycle lifecycle;

	private final ErrorLog errorLog;

	private final AlertApi alerts;

	private final ReportDateLedger ledger;

	private final IngestionProperties properties;

	private final AgrinexCalendar calendar;

	IngestionRunner(DataGovClient client, DeliveryUnitProcessor processor, UnitLoader unitLoader,
			RunLifecycle lifecycle, ErrorLog errorLog, AlertApi alerts, ReportDateLedger ledger,
			IngestionProperties properties, AgrinexCalendar calendar) {
		this.client = client;
		this.processor = processor;
		this.unitLoader = unitLoader;
		this.lifecycle = lifecycle;
		this.errorLog = errorLog;
		this.alerts = alerts;
		this.ledger = ledger;
		this.properties = properties;
		this.calendar = calendar;
	}

	public void execute(StartedRun run) {
		UUID runId = run.runId();
		RunCounters counters = new RunCounters();
		boolean unitLoaded = false;
		boolean failed = false;
		try {
			ReferenceLookups lookups = this.processor.lookupsFor(runId);
			LocalDate today = this.calendar.today();
			for (DeliveryUnit unit : run.deliveryUnits()) {
				int pagesFailedBefore = counters.pagesFailed();
				List<SourceRecord> records = fetch(run, unit, counters);
				if (records == null) {
					recordDate(run, unit, false, 0);
					continue;
				}
				boolean everyPage = counters.pagesFailed() == pagesFailedBefore;
				ProcessedUnit processed = this.processor.process(records, lookups, today);
				try {
					counters.addUnit(this.unitLoader.load(runId, run.dataset().id(), processed));
					unitLoaded = true;
					recordDate(run, unit, everyPage, records.size());
				}
				catch (RuntimeException ex) {
					failed = true;
					recordDate(run, unit, false, 0);
					log.atError()
						.addKeyValue("runId", runId)
						.addKeyValue("reportDate", unit.reportDate())
						.setCause(ex)
						.log("Ingestion delivery unit failed to load");
					this.errorLog.recordFailure(runId, IngestionErrorCode.LOAD_FAILED,
							"The load transaction of the delivery unit failed (" + ex.getClass().getSimpleName() + ")",
							null, null, unit.reportDate());
				}
			}
		}
		catch (RuntimeException ex) {
			failed = true;
			log.atError().addKeyValue("runId", runId).setCause(ex).log("Ingestion run stopped on an unexpected error");
			this.errorLog.recordFailure(runId, IngestionErrorCode.UNEXPECTED_FAILURE,
					"The run stopped on an unexpected error (" + ex.getClass().getSimpleName() + ")", null, null, null);
		}
		IngestionRunStatus status = statusOf(counters, unitLoaded, failed);
		this.lifecycle.finish(runId, status, counters);
		logCompletion(run, status, counters);
		if (counters.changedObservations()) {
			evaluateAlerts(runId);
		}
	}

	/**
	 * Writes a HISTORICAL_PRICE unit's outcome to the report-date ledger (ADR-0054 item 1). A failure to write it is
	 * logged and never changes the run: the date then keeps its previous state and is fetched again later.
	 */
	private void recordDate(StartedRun run, DeliveryUnit unit, boolean fetched, int recordsFetched) {
		if (unit.reportDate() == null) {
			return;
		}
		try {
			if (fetched) {
				this.ledger.recordFetched(run.dataset().id(), unit.reportDate(), run.runId(), recordsFetched);
			}
			else {
				this.ledger.recordFailed(run.dataset().id(), unit.reportDate(), run.runId());
			}
		}
		catch (RuntimeException ex) {
			log.atError()
				.addKeyValue("runId", run.runId())
				.addKeyValue("reportDate", unit.reportDate())
				.setCause(ex)
				.log("The report-date ledger could not be updated");
		}
	}

	/**
	 * Fetches every page of a delivery unit for each coverage state (ADR-0011 item 6). Returns the records, or null
	 * when no page of the unit could be fetched; a failed page is recorded and ends the unit.
	 */
	private List<SourceRecord> fetch(StartedRun run, DeliveryUnit unit, RunCounters counters) {
		SourceSchema schema = SourceSchema.of(run.dataset().type());
		List<SourceRecord> records = new ArrayList<>();
		boolean anyPage = false;
		for (String state : this.properties.coverageStates()) {
			Map<String, String> parameters = queryParameters(schema, state, unit.reportDate());
			int offset = 0;
			int pages = 0;
			boolean totalCounted = false;
			while (true) {
				if (pages >= this.properties.maxPagesPerUnit()) {
					counters.pageFailed();
					this.errorLog.recordFailure(run.runId(), IngestionErrorCode.PAGE_LIMIT_EXCEEDED,
							"The delivery unit did not end within " + pages + " pages", null, offset, unit.reportDate());
					return anyPage ? records : null;
				}
				SourcePage page;
				try {
					page = this.client.fetchPage(run.dataset().resourceId(), parameters, offset);
				}
				catch (SourceFetchException ex) {
					counters.pageFailed();
					this.errorLog.recordFailure(run.runId(), ex.code(), ex.getMessage(), ex.responseBody(), offset,
							unit.reportDate());
					return anyPage ? records : null;
				}
				anyPage = true;
				pages++;
				if (!totalCounted) {
					counters.addSourceTotal(page.total());
					totalCounted = true;
				}
				counters.pageFetched(page.records().size());
				page.records().forEach((record) -> records.add(SourceRecord.from(record, schema)));
				offset += page.records().size();
				if (page.records().isEmpty() || offset >= page.total()) {
					break;
				}
			}
		}
		return records;
	}

	/** The filters of the delivery unit, and the sort that makes offset paging complete (see SourceSchema). */
	static Map<String, String> queryParameters(SourceSchema schema, String state, LocalDate reportDate) {
		Map<String, String> parameters = new LinkedHashMap<>();
		parameters.put(schema.stateFilter(), state);
		if (reportDate != null) {
			parameters.put(schema.dateFilter(), reportDate.format(SourceSchema.DATE_FILTER_FORMAT));
		}
		parameters.putAll(schema.sortParameters());
		return parameters;
	}

	/**
	 * ADR-0011 item 8: SUCCESS when every page was fetched; PARTIAL_FAILURE when a unit loaded and a page failed;
	 * FAILED when nothing was fetched, a load failed or the run stopped.
	 */
	static IngestionRunStatus statusOf(RunCounters counters, boolean unitLoaded, boolean failed) {
		if (failed) {
			return IngestionRunStatus.FAILED;
		}
		if (counters.pagesFailed() == 0) {
			return IngestionRunStatus.SUCCESS;
		}
		return unitLoaded ? IngestionRunStatus.PARTIAL_FAILURE : IngestionRunStatus.FAILED;
	}

	/** ADR-0019 item 1; a failure is recorded and never changes the run status (ADR-0013 item 4). */
	private void evaluateAlerts(UUID runId) {
		try {
			int events = this.alerts.evaluateRun(runId);
			log.atInfo().addKeyValue("runId", runId).addKeyValue("alertEvents", events).log("Alerts evaluated");
		}
		catch (RuntimeException ex) {
			log.atError().addKeyValue("runId", runId).setCause(ex).log("Alert evaluation failed");
			this.errorLog.recordFailure(runId, IngestionErrorCode.ALERT_EVALUATION_FAILED,
					"Alert evaluation after the run failed (" + ex.getClass().getSimpleName() + ")", null, null, null);
		}
	}

	/** A FAILED run is also a structured ERROR event, the log half of the admin alert (ADR-0019 item 7). */
	private static void logCompletion(StartedRun run, IngestionRunStatus status, RunCounters counters) {
		var event = (status == IngestionRunStatus.FAILED) ? log.atError() : log.atInfo();
		event.addKeyValue("runId", run.runId())
			.addKeyValue("datasetType", run.dataset().type())
			.addKeyValue("triggeredBy", run.triggeredBy())
			.addKeyValue("status", status)
			.addKeyValue("pagesFetched", counters.pagesFetched())
			.addKeyValue("pagesFailed", counters.pagesFailed())
			.addKeyValue("recordsFetched", counters.recordsFetched())
			.addKeyValue("recordsInserted", counters.recordsInserted())
			.addKeyValue("recordsUpdated", counters.recordsUpdated())
			.log("Ingestion run completed");
	}

}
