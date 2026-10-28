package com.agrinex.ingestion;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;

import com.agrinex.IntegrationTestSupport;
import com.agrinex.SyntheticFlows;

import static com.agrinex.SyntheticFlows.record;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression for the incomplete paging found against the live source on 2026-10-13: without a sort, offset pages
 * of the same result overlap and leave records out while the counts still add up. On SYNTHETIC TEST DATA only, a
 * delivery unit of 23 records (five pages of five) must store all 23 observations, and every page request must
 * carry the sort, with the current resource's text fields sorted through ".keyword".
 */
class IngestionPagingIntegrationTest extends IntegrationTestSupport {

	private static final int RECORDS = 23;

	@Test
	void everyRecordOfAMultiPageDeliveryUnitIsStored() throws Exception {
		SyntheticFlows flows = new SyntheticFlows(this.mvc, this.jdbc, adminToken());
		flows.districtMappedFrom("Synthetic Paging District", "SYNTHETIC-PG", 990007, "18.700000", "73.900000");
		LocalDate today = SyntheticFlows.todayInIndia();
		List<String> records = new ArrayList<>();
		for (int i = 0; i < RECORDS; i++) {
			records.add(record("Synthetic Paging District", "Synthetic Paging Mandi %02d".formatted(i), "Onion", today,
					String.valueOf(2000 + i)));
		}
		int before = SOURCE.requests().size();
		SOURCE.serve(SyntheticFlows.CURRENT_RESOURCE, records);

		String run = flows.ingest();

		assertThat(JsonPath.<String>read(run, "$.status")).isEqualTo("SUCCESS");
		assertThat(JsonPath.<Integer>read(run, "$.sourceTotalRecords")).isEqualTo(RECORDS);
		assertThat(JsonPath.<Integer>read(run, "$.pagesFetched")).isEqualTo(5);
		assertThat(JsonPath.<Integer>read(run, "$.recordsInserted")).isEqualTo(RECORDS);
		assertThat(JsonPath.<Integer>read(run, "$.duplicatesSkipped")).isZero();
		assertThat(this.jdbc.queryForObject("""
				SELECT count(DISTINCT m.id) FROM price_observations o JOIN markets m ON m.id = o.market_id
				WHERE m.canonical_name LIKE 'Synthetic Paging Mandi %'""", Integer.class))
			.isEqualTo(RECORDS);
		List<Map<String, String>> pageRequests = SOURCE.requests().subList(before, SOURCE.requests().size());
		assertThat(pageRequests).hasSize(5).allSatisfy((query) -> assertThat(query)
			.containsEntry("sort[market.keyword]", "asc")
			.containsEntry("sort[modal_price]", "asc")
			.doesNotContainKey("sort[market]"));
	}

}
