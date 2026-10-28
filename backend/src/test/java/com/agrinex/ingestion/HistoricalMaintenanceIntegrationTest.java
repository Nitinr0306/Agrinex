package com.agrinex.ingestion;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.agrinex.IntegrationTestSupport;
import com.agrinex.SyntheticFlows;
import com.agrinex.ingestion.service.IngestionControlService;
import com.agrinex.ingestion.service.RunLifecycle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Historical maintenance (ADR-0054) on SYNTHETIC TEST DATA through the source stub: the report-date ledger after
 * fetched, revised and failed dates; the coverage view; the gap check that retries an incomplete date; the weekly
 * reconciliation; the pause; and recovery after an interrupted run (ADR-0011 item 5).
 */
class HistoricalMaintenanceIntegrationTest extends IntegrationTestSupport {

	static final String HISTORICAL_RESOURCE = "35985678-0d79-46b4-9ed6-6f13308a1d24";

	static final String DISTRICT = "Synthetic Maintenance District";

	@Autowired
	IngestionControlService control;

	@Autowired
	RunLifecycle lifecycle;

	String token;

	@BeforeEach
	void signIn() throws Exception {
		this.token = adminToken();
	}

	@AfterEach
	void sourceBack() {
		SOURCE.recover();
	}

	@Test
	void theLedgerFollowsFetchedRevisedAndFailedDatesAndTheGapCheckRetriesThem() throws Exception {
		new SyntheticFlows(this.mvc, this.jdbc, this.token).districtMappedFrom(DISTRICT, "SYNTHETIC-HM", 990031,
				"18.600000", "73.800000");
		LocalDate today = SyntheticFlows.todayInIndia();
		LocalDate day = today.minusDays(10);

		SOURCE.serve(HISTORICAL_RESOURCE, List.of(historical(day, "2000")));
		String first = triggerHistorical(day, day);
		assertThat(JsonPath.<String>read(first, "$.status")).isEqualTo("SUCCESS");
		assertThat(JsonPath.<String>read(first, "$.requestScope.purpose")).isEqualTo("MANUAL");
		assertThat(ledger(day)).containsEntry("last_outcome", "FETCHED")
			.containsEntry("success_records_fetched", 1)
			.containsEntry("consecutive_failures", 0)
			.containsEntry("last_success_run_id", UUID.fromString(JsonPath.read(first, "$.id")));

		// A changed price is a revision of the stored observation (ADR-0009 item 3).
		SOURCE.serve(HISTORICAL_RESOURCE, List.of(historical(day, "2100")));
		String revised = triggerHistorical(day, day);
		assertThat(JsonPath.<Integer>read(revised, "$.recordsUpdated")).isEqualTo(1);
		assertThat(JsonPath.<Integer>read(revised, "$.recordsInserted")).isZero();
		assertThat(this.jdbc.queryForObject(
				"SELECT previous_modal_price::text FROM price_observation_revisions WHERE ingestion_run_id = ?::uuid",
				String.class, JsonPath.<String>read(revised, "$.id")))
			.isEqualTo("2000.00");
		assertThat(this.jdbc.queryForObject("""
				SELECT o.modal_price::text FROM price_observations o JOIN markets m ON m.id = o.market_id
				WHERE m.canonical_name = 'Synthetic Maintenance Mandi' AND o.report_date = ?""", String.class, day))
			.isEqualTo("2100.00");

		// A source outage fails the date; the last successful fetch is kept.
		SOURCE.failWith(503, "<html>Service Unavailable</html>");
		String failed = triggerHistorical(day, day);
		SOURCE.recover();
		assertThat(JsonPath.<String>read(failed, "$.status")).isEqualTo("FAILED");
		assertThat(ledger(day)).containsEntry("last_outcome", "FAILED")
			.containsEntry("consecutive_failures", 1)
			.containsEntry("last_success_run_id", UUID.fromString(JsonPath.read(revised, "$.id")));

		String coverage = coverage();
		assertThat(JsonPath.<Integer>read(coverage, "$.counts.settled") + JsonPath.<Integer>read(coverage,
				"$.counts.pending") + JsonPath.<Integer>read(coverage, "$.counts.unsettled")
				+ JsonPath.<Integer>read(coverage, "$.counts.failed") + JsonPath.<Integer>read(coverage,
						"$.counts.missing"))
			.isEqualTo(366);
		assertThat(JsonPath.<String>read(coverage, "$.coverageTo")).isEqualTo(today.toString());
		List<Map<String, Object>> entry = JsonPath.read(coverage,
				"$.incompleteDates[?(@.reportDate == '" + day + "')]");
		assertThat(entry).singleElement()
			.satisfies((row) -> assertThat(row).containsEntry("status", "FAILED").containsEntry("consecutiveFailures", 1));

		// The gap check fetches the newest incomplete dates, this one included, in one SCHEDULER run.
		List<String> incomplete = JsonPath.read(coverage, "$.incompleteDates[*].reportDate");
		LocalDate to = incomplete.stream()
			.map(LocalDate::parse)
			.filter((date) -> !date.isAfter(today.minusDays(3)))
			.findFirst()
			.orElseThrow();
		LocalDate from = incomplete.stream()
			.map(LocalDate::parse)
			.filter((date) -> !date.isAfter(to) && !date.isBefore(to.minusDays(30)))
			.min(LocalDate::compareTo)
			.orElseThrow();
		this.control.runGapBackfill();
		Map<String, Object> gapRun = latestHistoricalRun();
		assertThat(gapRun).containsEntry("triggered_by", "SCHEDULER")
			.containsEntry("status", "SUCCESS")
			.containsEntry("purpose", "GAP_BACKFILL")
			.containsEntry("report_date_from", from.toString())
			.containsEntry("report_date_to", to.toString());
		assertThat(ledger(day)).containsEntry("last_outcome", "FETCHED")
			.containsEntry("consecutive_failures", 0)
			.containsEntry("last_attempt_run_id", gapRun.get("id"));
		assertThat(this.jdbc.queryForObject(
				"SELECT count(*) FROM ingestion_report_dates WHERE last_attempt_run_id = ?", Integer.class,
				gapRun.get("id")))
			.isEqualTo((int) (to.toEpochDay() - from.toEpochDay()) + 1);
	}

	@Test
	void theReconciliationRefetchesTheLastFourteenDatesAndAPauseSkipsMaintenance() throws Exception {
		LocalDate today = SyntheticFlows.todayInIndia();
		this.mvc
			.perform(post("/api/v1/admin/ingestion/datasets/HISTORICAL_PRICE/pause")
				.header("Authorization", bearer(this.token))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"reason\": \"SYNTHETIC TEST: maintenance pause\"}"))
			.andExpect(status().isOk());
		Object before;
		try {
			before = latestHistoricalRunId();
			this.control.runReconciliation();
			this.control.runGapBackfill();
			assertThat(latestHistoricalRunId()).isEqualTo(before);
			assertThat(JsonPath.<Boolean>read(coverage(), "$.scheduledRunsPaused")).isTrue();
		}
		finally {
			this.mvc
				.perform(post("/api/v1/admin/ingestion/datasets/HISTORICAL_PRICE/resume")
					.header("Authorization", bearer(this.token)))
				.andExpect(status().isOk());
		}

		this.control.runReconciliation();
		Map<String, Object> run = latestHistoricalRun();
		assertThat(run).containsEntry("triggered_by", "SCHEDULER")
			.containsEntry("purpose", "RECONCILIATION")
			.containsEntry("report_date_from", today.minusDays(14).toString())
			.containsEntry("report_date_to", today.minusDays(1).toString());
		assertThat(this.jdbc.queryForObject(
				"SELECT count(*) FROM ingestion_report_dates WHERE last_attempt_run_id = ?", Integer.class,
				run.get("id")))
			.isEqualTo(14);
	}

	@Test
	void anInterruptedRunIsFailedAtStartupAndTheDatasetAcceptsANewRun() throws Exception {
		UUID datasetId = this.jdbc.queryForObject(
				"SELECT id FROM source_datasets WHERE dataset_type = 'HISTORICAL_PRICE'", UUID.class);
		LocalDate day = SyntheticFlows.todayInIndia().minusDays(12);
		UUID interrupted = this.lifecycle.start(datasetId, TriggeredBy.MANUAL,
				"{\"reportDateFrom\": \"%s\", \"reportDateTo\": \"%s\"}".formatted(day, day));

		assertThat(this.lifecycle.interruptRunning()).isGreaterThanOrEqualTo(1);

		assertThat(this.jdbc.queryForObject("SELECT status FROM ingestion_runs WHERE id = ?", String.class,
				interrupted))
			.isEqualTo("FAILED");
		assertThat(this.jdbc.queryForObject(
				"SELECT error_code FROM ingestion_errors WHERE ingestion_run_id = ?", String.class, interrupted))
			.isEqualTo("RUN_INTERRUPTED");
		SOURCE.serve(HISTORICAL_RESOURCE, List.of());
		assertThat(JsonPath.<String>read(triggerHistorical(day, day), "$.status")).isEqualTo("SUCCESS");
	}

	/** One SYNTHETIC record in the historical resource's field layout (evidence file section 19.3). */
	static String historical(LocalDate date, String modal) {
		return """
				{"State": "%s", "District": "%s", "Market": "Synthetic Maintenance Mandi", "Commodity": "Onion",
				 "Commodity_Code": 23, "Variety": "Other", "Grade": "FAQ", "Arrival_Date": "%02d/%02d/%d",
				 "Min_Price": %s, "Max_Price": %s, "Modal_Price": %s}
				""".formatted(SyntheticFlows.COVERAGE_STATE, DISTRICT, date.getDayOfMonth(), date.getMonthValue(),
				date.getYear(), modal, modal, modal);
	}

	/** Triggers a MANUAL HISTORICAL_PRICE run over the dates and returns the finished run. */
	private String triggerHistorical(LocalDate from, LocalDate to) throws Exception {
		String runId = JsonPath.read(this.mvc
			.perform(post("/api/v1/admin/ingestion/trigger").header("Authorization", bearer(this.token))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"datasetType\": \"HISTORICAL_PRICE\", \"fromDate\": \"%s\", \"toDate\": \"%s\"}"
					.formatted(from, to)))
			.andExpect(status().isAccepted())
			.andReturn()
			.getResponse()
			.getContentAsString(), "$.runId");
		String[] run = new String[1];
		await().atMost(Duration.ofSeconds(60)).pollInterval(Duration.ofMillis(200)).until(() -> {
			run[0] = this.mvc
				.perform(get("/api/v1/admin/ingestion/runs/" + runId).header("Authorization", bearer(this.token)))
				.andReturn()
				.getResponse()
				.getContentAsString();
			return !"RUNNING".equals(JsonPath.read(run[0], "$.status"));
		});
		return run[0];
	}

	private String coverage() throws Exception {
		return this.mvc
			.perform(get("/api/v1/admin/ingestion/historical-coverage").header("Authorization", bearer(this.token)))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
	}

	private Map<String, Object> ledger(LocalDate day) {
		return this.jdbc.queryForMap("""
				SELECT d.* FROM ingestion_report_dates d JOIN source_datasets s ON s.id = d.source_dataset_id
				WHERE s.dataset_type = 'HISTORICAL_PRICE' AND d.report_date = ?""", day);
	}

	private Map<String, Object> latestHistoricalRun() {
		return this.jdbc.queryForMap("""
				SELECT r.id, r.triggered_by, r.status, r.request_scope ->> 'purpose' AS purpose,
				       r.request_scope ->> 'reportDateFrom' AS report_date_from,
				       r.request_scope ->> 'reportDateTo' AS report_date_to
				FROM ingestion_runs r JOIN source_datasets s ON s.id = r.source_dataset_id
				WHERE s.dataset_type = 'HISTORICAL_PRICE' ORDER BY r.started_at DESC LIMIT 1""");
	}

	private Object latestHistoricalRunId() {
		List<UUID> ids = this.jdbc.queryForList("""
				SELECT r.id FROM ingestion_runs r JOIN source_datasets s ON s.id = r.source_dataset_id
				WHERE s.dataset_type = 'HISTORICAL_PRICE' ORDER BY r.started_at DESC LIMIT 1""", UUID.class);
		return ids.isEmpty() ? null : ids.getFirst();
	}

}
