package com.agrinex.price;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.agrinex.IntegrationTestSupport;
import com.agrinex.SyntheticFlows;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The batched trend read the comparison uses (found slow in Task 12: three queries per option) gives every market
 * exactly what the one-market read gives. SYNTHETIC TEST DATA: markets with rising, falling and sparse series, two
 * varieties, both datasets on some days, and one market without observations.
 */
class TrendBatchIntegrationTest extends IntegrationTestSupport {

	@Autowired
	PriceApi prices;

	@Test
	void theBatchedTrendsEqualTheOneMarketTrends() {
		LocalDate today = SyntheticFlows.todayInIndia();
		UUID commodityId = this.jdbc.queryForObject("SELECT id FROM commodities WHERE canonical_name = 'Onion'",
				UUID.class);
		UUID districtId = district();
		UUID varietyA = variety(commodityId, "Synthetic Trend Variety A");
		UUID varietyB = variety(commodityId, "Synthetic Trend Variety B");
		List<UUID> markets = new ArrayList<>();
		for (int m = 0; m < 4; m++) {
			markets.add(this.jdbc.queryForObject(
					"INSERT INTO markets (canonical_name, canonical_slug, district_id) VALUES (?, ?, ?) RETURNING id",
					UUID.class, "Synthetic Trend Market " + m, "synthetic-trend-market-" + UUID.randomUUID(), districtId));
		}
		for (int day = 0; day < 14; day++) {
			LocalDate date = today.minusDays(day);
			// Market 0 rises, market 1 falls, market 2 has only three days, market 3 has none.
			observe(markets.get(0), commodityId, varietyA, date, 2000 - day * 40, "HISTORICAL_PRICE");
			observe(markets.get(0), commodityId, varietyB, date, 2500 - day * 30, "HISTORICAL_PRICE");
			observe(markets.get(1), commodityId, varietyA, date, 1500 + day * 25, "HISTORICAL_PRICE");
			if (day % 3 == 0) {
				observe(markets.get(1), commodityId, varietyA, date, 1400 + day * 25, "CURRENT_PRICE");
			}
			if (day < 3) {
				observe(markets.get(2), commodityId, varietyA, date, 1800, "HISTORICAL_PRICE");
			}
		}

		Map<UUID, TrendStatistic> batched = this.prices.trends(markets, commodityId, null, null, today);

		assertThat(batched).containsOnlyKeys(markets);
		for (UUID market : markets) {
			assertThat(batched.get(market)).as(market.toString())
				.isEqualTo(this.prices.trend(market, commodityId, null, null, today));
		}
		assertThat(batched.get(markets.get(0)).direction()).isEqualTo(TrendDirection.RISING);
		assertThat(batched.get(markets.get(1)).direction()).isEqualTo(TrendDirection.FALLING);
		assertThat(batched.get(markets.get(3))).isEqualTo(TrendStatistic.INSUFFICIENT);
		// The variety filter applies to every market of the batch.
		Map<UUID, TrendStatistic> filtered = this.prices.trends(markets, commodityId, varietyB, null, today);
		for (UUID market : markets) {
			assertThat(filtered.get(market)).isEqualTo(this.prices.trend(market, commodityId, varietyB, null, today));
		}
	}

	private UUID district() {
		int lgdCode = 990100 + ThreadLocalRandom.current().nextInt(800);
		String code = "SYNTHETIC-TB" + lgdCode;
		UUID stateId = this.jdbc.queryForObject("INSERT INTO states (name, code, lgd_code) VALUES (?, ?, ?) RETURNING id",
				UUID.class, "Synthetic State " + code, code, lgdCode);
		return this.jdbc.queryForObject("""
				INSERT INTO districts (state_id, name, lgd_code, centroid_lat, centroid_lon, centroid_source)
				VALUES (?, 'Synthetic Trend District', ?, 18.500000, 73.800000, 'SYNTHETIC TEST DATA') RETURNING id""",
				UUID.class, stateId, lgdCode);
	}

	private UUID variety(UUID commodityId, String name) {
		return this.jdbc.queryForObject(
				"INSERT INTO varieties (commodity_id, canonical_name) VALUES (?, ?) RETURNING id", UUID.class,
				commodityId, name + " " + UUID.randomUUID());
	}

	private void observe(UUID marketId, UUID commodityId, UUID varietyId, LocalDate day, int modal, String dataset) {
		UUID datasetId = this.jdbc.queryForObject("SELECT id FROM source_datasets WHERE dataset_type = ?", UUID.class,
				dataset);
		UUID runId = this.jdbc.queryForObject("""
				INSERT INTO ingestion_runs (source_dataset_id, triggered_by, status, started_at, finished_at, request_scope)
				VALUES (?, 'MANUAL', 'FAILED', now(), now(), '{}'::jsonb) RETURNING id""", UUID.class, datasetId);
		byte[] key = new byte[32];
		ThreadLocalRandom.current().nextBytes(key);
		this.jdbc.update("""
				INSERT INTO price_observations (market_id, commodity_id, variety_id, report_date, modal_price, price_unit,
				                                business_key, source_dataset_id, ingestion_run_id)
				VALUES (?, ?, ?, ?, ?, 'Rs./Quintal', ?, ?, ?)""", marketId, commodityId, varietyId, day, modal,
				HexFormat.of().formatHex(key), datasetId, runId);
	}

}
