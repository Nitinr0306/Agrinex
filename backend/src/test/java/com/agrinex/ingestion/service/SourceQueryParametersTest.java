package com.agrinex.ingestion.service;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.agrinex.ingestion.source.SourceSchema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * The query parameters of a page request, as verified against the live resources on 2026-10-13: the filter
 * names are case-sensitive (a wrong one is silently ignored or matches nothing), the historical date filter is
 * {@code dd-MM-yyyy} (ADR-0007 item 2), and a deterministic sort keeps offset paging complete.
 */
class SourceQueryParametersTest {

	@Test
	void currentResourceFiltersTheStateAndSortsTextFieldsThroughKeyword() {
		Map<String, String> parameters = IngestionRunner.queryParameters(SourceSchema.CURRENT, "Maharashtra", null);

		assertThat(parameters).containsExactly(entry("filters[state]", "Maharashtra"),
				entry("sort[district.keyword]", "asc"), entry("sort[market.keyword]", "asc"),
				entry("sort[commodity.keyword]", "asc"), entry("sort[variety.keyword]", "asc"),
				entry("sort[grade.keyword]", "asc"), entry("sort[min_price]", "asc"), entry("sort[modal_price]", "asc"),
				entry("sort[max_price]", "asc"));
	}

	@Test
	void historicalResourceFiltersStateAndReportDateAndSortsByItsOwnFieldNames() {
		Map<String, String> parameters = IngestionRunner.queryParameters(SourceSchema.HISTORICAL, "Maharashtra",
				LocalDate.of(2026, 9, 7));

		assertThat(parameters).containsExactly(entry("filters[State]", "Maharashtra"),
				entry("filters[Arrival_Date]", "07-09-2026"), entry("sort[District]", "asc"),
				entry("sort[Market]", "asc"), entry("sort[Commodity]", "asc"), entry("sort[Variety]", "asc"),
				entry("sort[Grade]", "asc"), entry("sort[Min_Price]", "asc"), entry("sort[Modal_Price]", "asc"),
				entry("sort[Max_Price]", "asc"));
	}

}
