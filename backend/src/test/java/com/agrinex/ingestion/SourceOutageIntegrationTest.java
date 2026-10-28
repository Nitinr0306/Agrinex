package com.agrinex.ingestion;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.agrinex.IntegrationTestSupport;
import com.agrinex.SyntheticFlows;

import static com.agrinex.SyntheticFlows.record;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Government source outage (spec section 29, section 48 resilience; ADR-0005, ADR-0011 item 8, ADR-0022 item 9), on
 * SYNTHETIC TEST DATA only: a failed run is FAILED, never "zero records"; stored observations stay as they were; a
 * comparison keeps serving them with the source status that drives the outage banner; admins see the failure.
 */
class SourceOutageIntegrationTest extends IntegrationTestSupport {

	private static final String DISTRICT = "Synthetic Outage District";

	@AfterEach
	void sourceBack() {
		SOURCE.recover();
	}

	@Test
	void aFailedRunKeepsStoredPricesAndIsReportedAsAnOutage() throws Exception {
		String admin = adminToken();
		SyntheticFlows flows = new SyntheticFlows(this.mvc, this.jdbc, admin);
		flows.districtMappedFrom(DISTRICT, "SYNTHETIC-OUT", 990013, "10.000000", "77.000000");
		LocalDate today = SyntheticFlows.todayInIndia();
		SOURCE.serve(SyntheticFlows.CURRENT_RESOURCE, List.of(record(DISTRICT, "Synthetic Outage Mandi", "Onion", today,
				"2400")));
		assertThat(JsonPath.<String>read(flows.ingest(), "$.status")).isEqualTo("SUCCESS");
		UUID market = flows.provisionalMarket("Synthetic Outage Mandi");
		flows.verifyAt(market, "10.010000", "77.000000");
		UUID onion = flows.commodity("Onion");
		String request = """
				{"commodityId": "%s", "quantity": 10, "quantityUnit": "QUINTAL",
				 "originLat": 10.000000, "originLon": 77.000000, "maxDistanceKm": 25}""".formatted(onion);
		String before = compare(request);
		assertThat(JsonPath.<Boolean>read(before, "$.sourceStatus.latestSyncFailed")).isFalse();
		String lastSuccess = JsonPath.read(before, "$.sourceStatus.lastSuccessfulSyncAt");
		Integer stored = this.jdbc.queryForObject("SELECT count(*) FROM price_observations", Integer.class);

		SOURCE.failWith(503, "<html><body>Service Unavailable</body></html>");
		String run = flows.ingest();

		assertThat(JsonPath.<String>read(run, "$.status")).isEqualTo("FAILED");
		assertThat(JsonPath.<Integer>read(run, "$.pagesFailed")).isEqualTo(1);
		assertThat(JsonPath.<Integer>read(run, "$.recordsFetched")).isZero();
		this.mvc
			.perform(get("/api/v1/admin/ingestion/runs/" + JsonPath.read(run, "$.id") + "/errors")
				.header("Authorization", bearer(admin)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].errorCode").value("HTTP_STATUS"));
		assertThat(this.jdbc.queryForObject("SELECT count(*) FROM price_observations", Integer.class)).isEqualTo(stored);

		String after = compare(request);
		assertThat(JsonPath.<Boolean>read(after, "$.sourceStatus.latestSyncFailed")).isTrue();
		assertThat(JsonPath.<String>read(after, "$.sourceStatus.lastSuccessfulSyncAt")).isEqualTo(lastSuccess);
		assertThat(JsonPath.<List<Double>>read(after, "$.options[*].price.modalPrice")).containsExactly(2400.0);

		this.mvc.perform(get("/api/v1/admin/ingestion/health").header("Authorization", bearer(admin)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.datasets[?(@.datasetType == 'CURRENT_PRICE')].consecutiveFailures").value(1));
	}

	private String compare(String body) throws Exception {
		return this.mvc
			.perform(post("/api/v1/selling-options/compare").contentType(MediaType.APPLICATION_JSON).content(body))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
	}

}
