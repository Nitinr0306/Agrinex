package com.agrinex;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Admin steps shared by the flow tests, performed through the real API with SYNTHETIC TEST DATA: a synthetic
 * district (inserted directly, because reference districts have no API), its source alias, ingestion of stubbed
 * records, and market coordinates and verification. "Maharashtra" is only the configured coverage value.
 */
public final class SyntheticFlows {

	public static final String CURRENT_RESOURCE = "9ef84268-d588-465a-a308-a864a43d0070";

	public static final String COVERAGE_STATE = "Maharashtra";

	public static final String EVIDENCE = "SYNTHETIC TEST DATA";

	private final MockMvc mvc;

	private final JdbcTemplate jdbc;

	private final String adminToken;

	public SyntheticFlows(MockMvc mvc, JdbcTemplate jdbc, String adminToken) {
		this.mvc = mvc;
		this.jdbc = jdbc;
		this.adminToken = adminToken;
	}

	public static LocalDate todayInIndia() {
		return LocalDate.now(ZoneId.of("Asia/Kolkata"));
	}

	/** A synthetic state and district with the given unique code and LGD number, mapped from a source name. */
	public UUID districtMappedFrom(String sourceDistrictName, String code, int lgdCode, String centroidLat,
			String centroidLon) throws Exception {
		UUID stateId = this.jdbc.queryForObject(
				"INSERT INTO states (name, code, lgd_code) VALUES (?, ?, ?) RETURNING id", UUID.class,
				"Synthetic State " + code, code, lgdCode);
		UUID districtId = this.jdbc.queryForObject("""
				INSERT INTO districts (state_id, name, lgd_code, centroid_lat, centroid_lon, centroid_source)
				VALUES (?, ?, ?, ?::numeric, ?::numeric, ?) RETURNING id""", UUID.class, stateId,
				"Synthetic District " + code, lgdCode, centroidLat, centroidLon, EVIDENCE);
		this.mvc
			.perform(post("/api/v1/admin/district-aliases").header("Authorization", bearer())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"sourceStateName": "%s", "sourceDistrictName": "%s", "districtId": "%s", "evidenceNote": "%s"}
						""".formatted(COVERAGE_STATE, sourceDistrictName, districtId, EVIDENCE)))
			.andExpect(status().isCreated());
		return districtId;
	}

	/** Triggers a CURRENT_PRICE run and returns the finished run. */
	public String ingest() throws Exception {
		String runId = JsonPath.read(this.mvc
			.perform(post("/api/v1/admin/ingestion/trigger").header("Authorization", bearer())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"datasetType\": \"CURRENT_PRICE\"}"))
			.andExpect(status().isAccepted())
			.andReturn()
			.getResponse()
			.getContentAsString(), "$.runId");
		String[] run = new String[1];
		await().atMost(Duration.ofSeconds(60)).pollInterval(Duration.ofMillis(200)).until(() -> {
			run[0] = this.mvc.perform(get("/api/v1/admin/ingestion/runs/" + runId).header("Authorization", bearer()))
				.andReturn()
				.getResponse()
				.getContentAsString();
			return !"RUNNING".equals(JsonPath.read(run[0], "$.status"));
		});
		return run[0];
	}

	/** The id of the PROVISIONAL market the ingestion registered under this name. */
	public UUID provisionalMarket(String name) throws Exception {
		String page = this.mvc
			.perform(get("/api/v1/admin/markets").header("Authorization", bearer())
				.param("reviewStatus", "PROVISIONAL")
				.param("search", name))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		List<Map<String, Object>> items = JsonPath.read(page, "$.items[?(@.name == '" + name + "')]");
		assertThat(items).hasSize(1);
		return UUID.fromString((String) items.getFirst().get("id"));
	}

	public void verifyAt(UUID market, String lat, String lon) throws Exception {
		this.mvc
			.perform(put("/api/v1/admin/markets/" + market + "/coordinates").header("Authorization", bearer())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"lat\": %s, \"lon\": %s, \"coordinateSource\": \"%s\"}".formatted(lat, lon, EVIDENCE)))
			.andExpect(status().isOk());
		this.mvc.perform(post("/api/v1/admin/markets/" + market + "/verify").header("Authorization", bearer()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.reviewStatus").value("VERIFIED"));
	}

	public UUID commodity(String canonicalName) {
		return this.jdbc.queryForObject("SELECT id FROM commodities WHERE canonical_name = ?", UUID.class,
				canonicalName);
	}

	/** One SYNTHETIC record in the current resource's field layout (evidence capture C-19-MH-ONION). */
	public static String record(String district, String market, String commodity, LocalDate date, String modal) {
		return """
				{"state": "%s", "district": "%s", "market": "%s", "commodity": "%s", "variety": "Other",
				 "grade": "FAQ", "arrival_date": "%s", "modal_price": %s}
				""".formatted(COVERAGE_STATE, district, market, commodity, "%02d/%02d/%d"
			.formatted(date.getDayOfMonth(), date.getMonthValue(), date.getYear()), modal);
	}

	private String bearer() {
		return "Bearer " + this.adminToken;
	}

}
