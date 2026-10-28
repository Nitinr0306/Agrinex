package com.agrinex.alert;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.agrinex.IntegrationTestSupport;
import com.agrinex.SyntheticFlows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Alert evaluation after an ingestion run (spec section 34; ADR-0019), on SYNTHETIC TEST DATA. It uses Potato and
 * markets about 800 km from the comparison flow's markets, so the two flows never see each other's rows.
 *
 * <pre>
 * run 1: East modal 2500 (today), West modal 2600 (today - 2); then both markets are verified
 * alerts: PRICE_ABOVE 2650 at West; PRICE_BELOW 2550 for Potato at any market
 * run 2: West modal 2700 (today - 1)          -> West's latest is now 2700 >= 2650: one event, value 2700
 *                                                East's latest 2500 <= 2550 (West 2700 is not): one event at East
 * evaluating run 2 again records nothing (one event per alert and run)
 * </pre>
 */
class AlertFlowIntegrationTest extends IntegrationTestSupport {

	static final String SOURCE_DISTRICT = "Synthetic Alert Source District";

	@Autowired
	AlertApi alerts;

	@Test
	void anIngestionRunTriggersEachMatchingAlertOnce() throws Exception {
		SyntheticFlows flows = new SyntheticFlows(this.mvc, this.jdbc, adminToken());
		flows.districtMappedFrom(SOURCE_DISTRICT, "SYNTHETIC-AL", 990011, "26.000000", "80.000000");
		LocalDate today = SyntheticFlows.todayInIndia();
		String east = "Synthetic Alert Mandi East";
		String west = "Synthetic Alert Mandi West";
		SOURCE.serve(SyntheticFlows.CURRENT_RESOURCE,
				List.of(SyntheticFlows.record(SOURCE_DISTRICT, east, "Potato", today, "2500"),
						SyntheticFlows.record(SOURCE_DISTRICT, west, "Potato", today.minusDays(2), "2600")));
		flows.ingest();
		UUID eastId = flows.provisionalMarket(east);
		UUID westId = flows.provisionalMarket(west);
		flows.verifyAt(eastId, "26.000000", "80.000000");
		flows.verifyAt(westId, "26.000000", "80.100000");
		UUID potato = flows.commodity("Potato");

		String phone = syntheticPhone();
		this.mvc
			.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content("{\"phoneNumber\": \"%s\", \"email\": \"alerts-%s@agrinex.invalid\", \"password\": \"%s\"}"
					.formatted(phone, phone.substring(1), "synthetic-pass-123")))
			.andExpect(status().isCreated());
		String farmer = login(phone, "synthetic-pass-123");
		createAlert(farmer, "PRICE_ABOVE", potato, westId, "2650");
		createAlert(farmer, "PRICE_BELOW", potato, null, "2550");
		this.mvc.perform(get("/api/v1/alerts/events").header("Authorization", bearer(farmer)))
			.andExpect(jsonPath("$.totalItems").value(0));

		SOURCE.serve(SyntheticFlows.CURRENT_RESOURCE,
				List.of(SyntheticFlows.record(SOURCE_DISTRICT, west, "Potato", today.minusDays(1), "2700")));
		String run = flows.ingest();
		assertThat(JsonPath.<Integer>read(run, "$.recordsInserted")).isEqualTo(1);

		// Alerts are evaluated after the run has finished, in their own transaction (ADR-0019 item 1), so the
		// events appear shortly after the run status does; clients poll for them (PDD-01).
		await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofMillis(200)).until(() -> JsonPath
			.<Integer>read(this.mvc.perform(get("/api/v1/alerts/events").header("Authorization", bearer(farmer)))
				.andReturn()
				.getResponse()
				.getContentAsString(), "$.totalItems") > 0);
		this.mvc.perform(get("/api/v1/alerts/events").header("Authorization", bearer(farmer)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalItems").value(2))
			.andExpect(jsonPath("$.items[*].alertType", containsInAnyOrder("PRICE_ABOVE", "PRICE_BELOW")))
			.andExpect(jsonPath("$.items[?(@.alertType == 'PRICE_ABOVE')].market.id").value(westId.toString()))
			.andExpect(jsonPath("$.items[?(@.alertType == 'PRICE_ABOVE')].triggerValue").value(2700.00))
			.andExpect(jsonPath("$.items[?(@.alertType == 'PRICE_BELOW')].market.id").value(eastId.toString()))
			.andExpect(jsonPath("$.items[?(@.alertType == 'PRICE_BELOW')].triggerValue").value(2500.00));

		assertThat(this.alerts.evaluateRun(UUID.fromString(JsonPath.read(run, "$.id")))).isZero();
	}

	private void createAlert(String farmer, String type, UUID commodity, UUID market, String threshold)
			throws Exception {
		String marketField = (market != null) ? ", \"marketId\": \"" + market + "\"" : "";
		this.mvc
			.perform(post("/api/v1/alerts").header("Authorization", bearer(farmer))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"alertType\": \"%s\", \"commodityId\": \"%s\"%s, \"thresholdValue\": %s}".formatted(type,
						commodity, marketField, threshold)))
			.andExpect(status().isCreated());
	}

}
