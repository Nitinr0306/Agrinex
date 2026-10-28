package com.agrinex;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static com.agrinex.SyntheticFlows.record;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The Phase 1 flow through the real API, on SYNTHETIC TEST DATA only: a stubbed government page is ingested, an
 * admin maps the district, sets coordinates and verifies the markets, and a comparison ranks them. Every district,
 * market and price below is invented for the test and is never presented as government data.
 *
 * <pre>
 * origin = district centroid (18.500000, 73.800000); 20 quintal = 2000 kg -> 2 trips of 1000 kg
 * Near (18.600000, 73.800000): 11.12 km  x 8.00 =  88.96 -> minimum 300.00 x 2 = 600.00  gross 50000.00  net 49400.00
 * Far  (19.000000, 73.800000): 55.60 km  x 8.00 = 444.80 x 2 = 889.60                    gross 52000.00  net 51110.40
 * (distances: haversine with R = 6371.0088 km, computed independently: 11.1195..., 55.5975...)
 * </pre>
 */
class PhaseOneFlowIntegrationTest extends IntegrationTestSupport {

	static final String SOURCE_DISTRICT = "Synthetic Source District";

	@Test
	void ingestedMarketsAreVerifiedThenComparedWithHandComputedEstimates() throws Exception {
		String admin = adminToken();
		SyntheticFlows flows = new SyntheticFlows(this.mvc, this.jdbc, admin);
		flows.districtMappedFrom(SOURCE_DISTRICT, "SYNTHETIC-P1", 990001, "18.500000", "73.800000");

		LocalDate today = SyntheticFlows.todayInIndia();
		SOURCE.serve(SyntheticFlows.CURRENT_RESOURCE,
				List.of(record(SOURCE_DISTRICT, "Synthetic Mandi Near", "Onion", today, "2500"),
						record(SOURCE_DISTRICT, "Synthetic Mandi Far", "Onion", today.minusDays(2), "2600"),
						record(SOURCE_DISTRICT, "Synthetic Mandi Near", "Onion", today.minusDays(1), "0"),
						record("Synthetic Unmapped District", "Synthetic Mandi Lost", "Onion", today, "2500")));

		String run = flows.ingest();
		assertThat(JsonPath.<String>read(run, "$.status")).isEqualTo("SUCCESS");
		assertThat(JsonPath.<Integer>read(run, "$.recordsFetched")).isEqualTo(4);
		assertThat(JsonPath.<Integer>read(run, "$.recordsInserted")).isEqualTo(2);
		assertThat(JsonPath.<Integer>read(run, "$.validationErrorsCount")).isEqualTo(2);
		String runId = JsonPath.read(run, "$.id");
		String errors = this.mvc
			.perform(get("/api/v1/admin/ingestion/runs/" + runId + "/errors").header("Authorization", bearer(admin)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[*].errorCode", containsInAnyOrder("PRICE_NON_POSITIVE", "UNKNOWN_DISTRICT")))
			.andReturn()
			.getResponse()
			.getContentAsString();
		assertThat(errors).doesNotContain(SYNTHETIC_API_KEY);
		assertThat(SOURCE.requests()).allSatisfy((query) -> assertThat(query)
			.containsEntry("api-key", SYNTHETIC_API_KEY)
			.containsValue(SyntheticFlows.COVERAGE_STATE));

		UUID near = flows.provisionalMarket("Synthetic Mandi Near");
		UUID far = flows.provisionalMarket("Synthetic Mandi Far");
		this.mvc.perform(get("/api/v1/markets/" + near)).andExpect(status().isNotFound());
		flows.verifyAt(near, "18.600000", "73.800000");
		flows.verifyAt(far, "19.000000", "73.800000");

		UUID onion = flows.commodity("Onion");
		this.mvc.perform(get("/api/v1/markets/" + near)).andExpect(status().isOk());
		this.mvc.perform(get("/api/v1/markets/" + near + "/forecast").param("commodityId", onion.toString()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.available").value(false));

		String comparison = this.mvc
			.perform(post("/api/v1/selling-options/compare").contentType(MediaType.APPLICATION_JSON).content("""
					{"commodityId": "%s", "quantity": 20, "quantityUnit": "QUINTAL",
					 "originLat": 18.500000, "originLon": 73.800000}
					""".formatted(onion)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.options[*].market.id", contains(far.toString(), near.toString())))
			.andExpect(jsonPath("$.options[*].distanceKm", contains(55.60, 11.12)))
			.andExpect(jsonPath("$.options[*].estimates.trips", contains(2, 2)))
			.andExpect(jsonPath("$.options[*].estimates.estimatedTransportCost", contains(889.60, 600.00)))
			.andExpect(jsonPath("$.options[*].estimates.estimatedGrossValue", contains(52000.00, 50000.00)))
			.andExpect(jsonPath("$.options[*].estimates.estimatedNetRealization", contains(51110.40, 49400.00)))
			.andExpect(jsonPath("$.netRealizationWinnerId").value(far.toString()))
			.andExpect(jsonPath("$.priceOnlyWinnerId").value(far.toString()))
			.andExpect(jsonPath("$.rankingsDisagree").value(false))
			.andExpect(jsonPath("$.decisionBasis.mlContributed").value(false))
			.andReturn()
			.getResponse()
			.getContentAsString();

		// What-If (ADR-0026): 1000 kg = 10 quintal -> 1 trip, rate 10.00/km. Near 11.12 x 10 = 111.20 -> minimum
		// 300.00, net 25000.00 - 300.00 = 24700.00; Far 55.60 x 10 = 556.00, net 26000.00 - 556.00 = 25444.00.
		this.mvc
			.perform(post("/api/v1/selling-options/simulate").contentType(MediaType.APPLICATION_JSON).content("""
					{"recommendationId": "%s", "quantity": 1000, "transportRatePerKm": 10}
					""".formatted(JsonPath.<String>read(comparison, "$.recommendationId"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.scenario.kind").value("SIMULATION"))
			.andExpect(jsonPath("$.scenario.persisted").value(false))
			.andExpect(jsonPath("$.quantityUnit").value("KG"))
			.andExpect(jsonPath("$.transportAssumptions.ratePerKmSource").value("USER_OVERRIDE"))
			.andExpect(jsonPath("$.decisionBasis.rankingMethod").value("NET_REALIZATION"))
			.andExpect(jsonPath("$.options[*].market.id", contains(far.toString(), near.toString())))
			.andExpect(jsonPath("$.options[*].estimates.trips", contains(1, 1)))
			.andExpect(jsonPath("$.options[*].estimates.estimatedTransportCost", contains(556.00, 300.00)))
			.andExpect(jsonPath("$.options[*].estimates.estimatedNetRealization", contains(25444.00, 24700.00)));

		// The same page again inserts nothing: the business key keeps observations unique (spec section 31 RULE 6).
		String rerun = flows.ingest();
		assertThat(JsonPath.<Integer>read(rerun, "$.recordsInserted")).isZero();
		assertThat(this.jdbc.queryForObject("SELECT count(*) FROM price_observations WHERE market_id IN (?, ?)",
				Integer.class, near, far))
			.isEqualTo(2);
	}

}
