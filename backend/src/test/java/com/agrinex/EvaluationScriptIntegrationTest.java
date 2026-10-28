package com.agrinex;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.agrinex.SyntheticFlows.record;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Experiment 1 (spec section 36; ADR-0045 item 8) as `scripts/run_evaluation.sql` computes it, on hand-computed
 * SYNTHETIC TEST DATA: three synthetic markets, and comparison rows inserted with chosen estimates inside a window
 * of 2001 that no other test uses. The script runs unchanged except for its two window bounds.
 */
class EvaluationScriptIntegrationTest extends IntegrationTestSupport {

	static final Path SCRIPT = Path.of("..", "scripts", "run_evaluation.sql");

	static final OffsetDateTime WINDOW_START = OffsetDateTime.parse("2001-01-01T00:00:00Z");

	static final OffsetDateTime WINDOW_END = OffsetDateTime.parse("2001-02-01T00:00:00Z");

	UUID onion;

	Map<String, UUID> markets;

	Map<String, UUID> observations;

	@BeforeEach
	void syntheticMarkets() throws Exception {
		SyntheticFlows flows = new SyntheticFlows(this.mvc, this.jdbc, adminToken());
		LocalDate today = SyntheticFlows.todayInIndia();
		SOURCE.serve(SyntheticFlows.CURRENT_RESOURCE,
				List.of(record("Pune", "SYNTHETIC Eval Mandi A", "Onion", today, "2000"),
						record("Pune", "SYNTHETIC Eval Mandi B", "Onion", today, "1900"),
						record("Pune", "SYNTHETIC Eval Mandi C", "Onion", today, "1800")));
		flows.ingest();
		this.onion = flows.commodity("Onion");
		this.markets = Map.of("A", market("SYNTHETIC Eval Mandi A"), "B", market("SYNTHETIC Eval Mandi B"), "C",
				market("SYNTHETIC Eval Mandi C"));
		this.observations = Map.of("A", observation("A"), "B", observation("B"), "C", observation("C"));
	}

	@Test
	void ratesAndNetDifferencesMatchTheHandComputedScenarios() throws Exception {
		// Scenario 1: highest price A (net 1000.00), recommended B (net 1200.00): disagreement, difference +200.00.
		comparison(at(2), "A", "B", option("B", 1, "1200.00"), option("A", 2, "1000.00"), option("C", 3, "900.00"));
		// Scenario 2: A is both winners: no disagreement.
		comparison(at(3), "A", "A", option("A", 1, "1500.00"), option("B", 2, "1400.00"));
		// Scenario 3: highest price A (net 900.00), recommended B (net 850.00): difference -50.00, kept (ADR-0045).
		comparison(at(4), "A", "B", option("B", 1, "850.00"), option("A", 2, "900.00"));
		// Scenario 4: highest price C (net 700.00), recommended A (net 1200.00): difference +500.00.
		comparison(at(5), "C", "A", option("A", 1, "1200.00"), option("B", 2, "1000.00"), option("C", 3, "700.00"));
		// Not scenarios: one eligible market only; and a disagreement outside the window.
		comparison(at(6), "A", "A", option("A", 1, "2000.00"));
		comparison(OffsetDateTime.parse("2001-03-01T00:00:00Z"), "A", "B", option("B", 1, "9000.00"),
				option("A", 2, "1.00"));

		Map<String, Object> result = this.jdbc.queryForMap(script(WINDOW_START, WINDOW_END));

		// 4 scenarios, 3 disagreements -> 75.00 %. Differences +200, -50, +500:
		// average 650 / 3 = 216.666... -> 216.67; median 200.00; 90th percentile: position 0.9 x (3 - 1) = 1.8,
		// 200 + 0.8 x (500 - 200) = 440.00.
		assertThat(result.get("scenarios")).isEqualTo(4L);
		assertThat(result.get("disagreements")).isEqualTo(3L);
		assertThat((BigDecimal) result.get("ranking_disagreement_rate_pct")).isEqualByComparingTo("75.00");
		assertThat((BigDecimal) result.get("average_estimated_net_difference")).isEqualByComparingTo("216.67");
		assertThat((BigDecimal) result.get("median_estimated_net_difference")).isEqualByComparingTo("200.00");
		assertThat((BigDecimal) result.get("percentile90_estimated_net_difference")).isEqualByComparingTo("440.00");
		assertThat(result.get("disagreements_with_negative_difference")).isEqualTo(1L);
		assertThat(result.get("first_scenario_at")).isNotNull();
	}

	@Test
	void aWindowWithoutScenariosReportsZeroAndNoStatistics() throws Exception {
		Map<String, Object> result = this.jdbc.queryForMap(script(OffsetDateTime.parse("2001-06-01T00:00:00Z"),
				OffsetDateTime.parse("2001-07-01T00:00:00Z")));

		assertThat(result.get("scenarios")).isEqualTo(0L);
		assertThat(result.get("disagreements")).isEqualTo(0L);
		assertThat(result.get("ranking_disagreement_rate_pct")).isNull();
		assertThat(result.get("average_estimated_net_difference")).isNull();
		assertThat(result.get("median_estimated_net_difference")).isNull();
	}

	/** The script as committed, with only its two window bounds set. */
	private static String script(OffsetDateTime from, OffsetDateTime to) throws Exception {
		String sql = Files.readString(SCRIPT);
		String fromBound = "CAST(NULL AS timestamptz) AS from_ts";
		String toBound = "CAST(NULL AS timestamptz) AS to_ts";
		assertThat(sql).contains(fromBound).contains(toBound);
		return sql.replace(fromBound, "CAST('" + from + "' AS timestamptz) AS from_ts")
			.replace(toBound, "CAST('" + to + "' AS timestamptz) AS to_ts");
	}

	private static OffsetDateTime at(int day) {
		return WINDOW_START.plusDays(day);
	}

	private record Option(String market, int rank, String net) {
	}

	private static Option option(String market, int rank, String net) {
		return new Option(market, rank, net);
	}

	private void comparison(OffsetDateTime createdAt, String priceWinner, String netWinner, Option... options) {
		UUID search = UUID.randomUUID();
		this.jdbc.update("""
				INSERT INTO selling_searches (id, commodity_id, quantity, quantity_unit, origin_lat, origin_lon, max_distance_km)
				VALUES (?, ?, 10, 'QUINTAL', 18.570554, 74.069108, 150)""", search, this.onion);
		UUID run = UUID.randomUUID();
		this.jdbc.update("""
				INSERT INTO recommendation_runs (id, selling_search_id, weights_id, transport_id,
				    price_only_winner_market_id, net_winner_market_id, rankings_disagree, created_at)
				VALUES (?, ?, (SELECT id FROM recommendation_weights WHERE is_active),
				    (SELECT id FROM transport_assumptions WHERE is_active), ?, ?, ?, ?)""", run, search,
				this.markets.get(priceWinner), this.markets.get(netWinner), !priceWinner.equals(netWinner), createdAt);
		for (Option option : options) {
			BigDecimal net = new BigDecimal(option.net());
			this.jdbc.update("""
					INSERT INTO selling_options (recommendation_run_id, market_id, price_observation_id, distance_km,
					    estimated_transport_cost, estimated_gross_value, estimated_net_realization, trend_direction,
					    freshness_status, rank, score)
					VALUES (?, ?, ?, 10.00, 100.00, ?, ?, 'STABLE', 'FRESH', ?, 0.5)""", run,
					this.markets.get(option.market()), this.observations.get(option.market()),
					net.add(new BigDecimal("100.00")), net, option.rank());
		}
	}

	private UUID market(String name) {
		return this.jdbc.queryForObject("SELECT id FROM markets WHERE canonical_name = ?", UUID.class, name);
	}

	private UUID observation(String market) {
		return this.jdbc.queryForObject(
				"SELECT id FROM price_observations WHERE market_id = ? ORDER BY report_date DESC LIMIT 1", UUID.class,
				this.markets.get(market));
	}

}
