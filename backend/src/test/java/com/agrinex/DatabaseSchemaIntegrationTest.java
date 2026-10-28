package com.agrinex;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

/**
 * Migrations V1 to V11 on PostgreSQL 16.15: the applied history, the observation integrity rules, and the optional
 * ML boundary of ADR-0030 (the view, the restricted role and the forecasts checks). Rows are SYNTHETIC TEST DATA.
 */
class DatabaseSchemaIntegrationTest extends IntegrationTestSupport {

	static final SecureRandom RANDOM = new SecureRandom();

	static final LocalDate DAY = LocalDate.of(2026, 9, 14);

	@Autowired
	DataSource dataSource;

	@Test
	void everyMigrationIsApplied() {
		List<String> versions = this.jdbc.queryForList(
				"SELECT version FROM flyway_schema_history WHERE success AND version IS NOT NULL ORDER BY installed_rank",
				String.class);
		assertThat(versions).containsExactly("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13");
	}

	/**
	 * A database connection failure fails fast with 503 (RESILIENCE_ARCHITECTURE.md): a request waits at most 3 s
	 * for a connection, not Hikari's default 30 s (found in Task 12 with the database stopped).
	 */
	@Test
	void aDatabaseOutageFailsFastInsteadOfWaitingThirtySeconds() throws Exception {
		HikariDataSource pool = this.dataSource.unwrap(HikariDataSource.class);
		assertThat(pool.getConnectionTimeout()).isEqualTo(3000);
		assertThat(pool.getValidationTimeout()).isEqualTo(2000);
	}

	@Test
	void theMlRoleCannotLogInAndReachesOnlyItsBoundary() {
		assertThat(this.jdbc.queryForObject("SELECT rolcanlogin FROM pg_roles WHERE rolname = 'agrinex_ml'",
				Boolean.class))
			.isFalse();
		assertThat(privilege("daily_price_series", "SELECT")).isTrue();
		assertThat(privilege("forecasts", "INSERT")).isTrue();
		assertThat(privilege("forecasts", "DELETE")).isTrue();
		assertThat(privilege("forecasts", "UPDATE")).isFalse();
		assertThat(privilege("price_observations", "SELECT")).isFalse();
		assertThat(privilege("users", "SELECT")).isFalse();
		assertThat(privilege("markets", "SELECT")).isFalse();
	}

	@Test
	void forecastRowsMustHaveAnAllowedHorizonAndATargetAfterTheTrainingData() {
		Fixture fixture = fixture("SYNTHETIC-FC", 990003);
		assertThatThrownBy(() -> insertForecast(fixture, 5, DAY.plusDays(5), DAY))
			.isInstanceOf(DataIntegrityViolationException.class)
			.hasMessageContaining("ck_forecasts_horizon");
		assertThatThrownBy(() -> insertForecast(fixture, 3, DAY, DAY))
			.isInstanceOf(DataIntegrityViolationException.class)
			.hasMessageContaining("ck_forecasts_after_training");
		insertForecast(fixture, 3, DAY.plusDays(3), DAY);
	}

	@Test
	void theBusinessKeyIsUniqueInTheDatabase() {
		Fixture fixture = fixture("SYNTHETIC-BK", 990004);
		String key = businessKey();
		insertObservation(fixture, null, DAY, "2000", "CURRENT_PRICE", key);
		assertThatThrownBy(() -> insertObservation(fixture, null, DAY.plusDays(1), "2000", "CURRENT_PRICE", key))
			.isInstanceOf(DataIntegrityViolationException.class)
			.hasMessageContaining("uq_price_observations_business_key");
	}

	@Test
	void theDailySeriesViewSelectsOneRealObservationPerDay() {
		Fixture fixture = fixture("SYNTHETIC-VW", 990005);
		UUID varietyA = variety(fixture, "Synthetic Variety A");
		UUID varietyB = variety(fixture, "Synthetic Variety B");
		UUID varietyC = variety(fixture, "Synthetic Variety C");
		// Day 1, current dataset: 300, 100, 200 -> lower median (position 2 of 3) is 200. The historical copy of
		// variety B is ignored because the current dataset takes precedence (ADR-0010).
		insertObservation(fixture, varietyA, DAY, "300", "CURRENT_PRICE", businessKey());
		insertObservation(fixture, varietyB, DAY, "100", "CURRENT_PRICE", businessKey());
		insertObservation(fixture, varietyC, DAY, "200", "CURRENT_PRICE", businessKey());
		insertObservation(fixture, varietyB, DAY, "999", "HISTORICAL_PRICE", businessKey());
		// Day 2, historical only: 150, 250 -> lower median (position 1 of 2) is 150; prices are never averaged.
		insertObservation(fixture, varietyA, DAY.plusDays(1), "150", "HISTORICAL_PRICE", businessKey());
		insertObservation(fixture, varietyB, DAY.plusDays(1), "250", "HISTORICAL_PRICE", businessKey());

		List<Map<String, Object>> series = this.jdbc.queryForList("""
				SELECT report_date, modal_price, source_dataset FROM daily_price_series
				WHERE market_id = ? ORDER BY report_date""", fixture.marketId());
		assertThat(series).extracting((row) -> row.get("modal_price").toString(), (row) -> row.get("source_dataset"))
			.containsExactly(tuple("200.00", "CURRENT_PRICE"),
					tuple("150.00", "HISTORICAL_PRICE"));
	}

	private boolean privilege(String table, String privilege) {
		return Boolean.TRUE.equals(this.jdbc.queryForObject("SELECT has_table_privilege('agrinex_ml', ?, ?)",
				Boolean.class, table, privilege));
	}

	private Fixture fixture(String code, int lgdCode) {
		UUID stateId = this.jdbc.queryForObject(
				"INSERT INTO states (name, code, lgd_code) VALUES (?, ?, ?) RETURNING id", UUID.class,
				"Synthetic State " + code, code, lgdCode);
		UUID districtId = this.jdbc.queryForObject("""
				INSERT INTO districts (state_id, name, lgd_code, centroid_lat, centroid_lon, centroid_source)
				VALUES (?, 'Synthetic District', ?, 18.500000, 73.800000, 'SYNTHETIC TEST DATA') RETURNING id""",
				UUID.class, stateId, lgdCode);
		String slug = code.toLowerCase() + "-market";
		UUID marketId = this.jdbc.queryForObject("""
				INSERT INTO markets (canonical_name, canonical_slug, district_id) VALUES (?, ?, ?) RETURNING id""",
				UUID.class, "Synthetic Market " + code, slug, districtId);
		UUID commodityId = this.jdbc.queryForObject("SELECT id FROM commodities WHERE canonical_name = 'Onion'",
				UUID.class);
		return new Fixture(marketId, commodityId);
	}

	private UUID variety(Fixture fixture, String name) {
		return this.jdbc.queryForObject(
				"INSERT INTO varieties (commodity_id, canonical_name) VALUES (?, ?) RETURNING id", UUID.class,
				fixture.commodityId(), name + " " + fixture.marketId());
	}

	private void insertObservation(Fixture fixture, UUID varietyId, LocalDate day, String modal, String dataset,
			String businessKey) {
		UUID datasetId = this.jdbc.queryForObject("SELECT id FROM source_datasets WHERE dataset_type = ?", UUID.class,
				dataset);
		UUID runId = this.jdbc.queryForObject("""
				INSERT INTO ingestion_runs (source_dataset_id, triggered_by, status, started_at, finished_at,
				                            request_scope)
				VALUES (?, 'MANUAL', 'FAILED', now(), now(), '{}'::jsonb) RETURNING id""", UUID.class, datasetId);
		this.jdbc.update("""
				INSERT INTO price_observations (market_id, commodity_id, variety_id, report_date, modal_price,
				                                price_unit, business_key, source_dataset_id, ingestion_run_id)
				VALUES (?, ?, ?, ?, ?::numeric, 'Rs./Quintal', ?, ?, ?)""", fixture.marketId(), fixture.commodityId(),
				varietyId, day, modal, businessKey, datasetId, runId);
	}

	private void insertForecast(Fixture fixture, int horizon, LocalDate target, LocalDate trainedThrough) {
		this.jdbc.update("""
				INSERT INTO forecasts (market_id, commodity_id, forecast_horizon_days, forecast_date, predicted_price,
				                       model_name, model_version, training_data_through, generated_at)
				VALUES (?, ?, ?, ?, 100.00, 'naive-last-value', '0.1.0', ?, now())""", fixture.marketId(),
				fixture.commodityId(), horizon, target, trainedThrough);
	}

	private static String businessKey() {
		byte[] bytes = new byte[32];
		RANDOM.nextBytes(bytes);
		return HexFormat.of().formatHex(bytes);
	}

	record Fixture(UUID marketId, UUID commodityId) {
	}

}
