package com.agrinex.price.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.agrinex.price.SourceDataset;

import static org.assertj.core.api.Assertions.assertThat;

/** The ADR-0010 selection rules on SYNTHETIC rows (never government observations). */
class ObservationSelectionTest {

	static final UUID MARKET = UUID.randomUUID();

	static final UUID COMMODITY = UUID.randomUUID();

	static final LocalDate DAY = LocalDate.of(2026, 10, 3);

	@Test
	void theCurrentDatasetRowWinsOverTheHistoricalRowOfTheSameIdentity() {
		UUID variety = UUID.randomUUID();
		ObservationRow historical = row(variety, "Red", null, null, DAY, "2000", SourceDataset.HISTORICAL_PRICE);
		ObservationRow current = row(variety, "Red", null, null, DAY, "2100", SourceDataset.CURRENT_PRICE);
		assertThat(ObservationSelection.applyDatasetPrecedence(List.of(historical, current))).containsExactly(current);
	}

	@Test
	void anIdentityOnlyInTheHistoricalDatasetIsKept() {
		ObservationRow historical = row(UUID.randomUUID(), "Red", null, null, DAY, "2000",
				SourceDataset.HISTORICAL_PRICE);
		ObservationRow current = row(UUID.randomUUID(), "White", null, null, DAY, "2100", SourceDataset.CURRENT_PRICE);
		assertThat(ObservationSelection.applyDatasetPrecedence(List.of(historical, current)))
			.containsExactlyInAnyOrder(historical, current);
	}

	@Test
	void theRepresentativeIsTheLowerMedianOfAnEvenCount() {
		// Sorted by modal: 1000, 2000, 3000, 4000 -> index floor((4 - 1) / 2) = 1 -> 2000.
		List<ObservationRow> rows = List.of(variety("A", "4000"), variety("B", "1000"), variety("C", "3000"),
				variety("D", "2000"));
		assertThat(ObservationSelection.representative(rows).modalPrice()).isEqualByComparingTo("2000");
	}

	@Test
	void theRepresentativeOfAnOddCountIsTheMedian() {
		List<ObservationRow> rows = List.of(variety("A", "300"), variety("B", "100"), variety("C", "200"));
		assertThat(ObservationSelection.representative(rows).modalPrice()).isEqualByComparingTo("200");
	}

	@Test
	void equalModalPricesAreOrderedByVarietyNameWithNullFirst() {
		ObservationRow unnamed = row(null, null, null, null, DAY, "2000", SourceDataset.CURRENT_PRICE);
		ObservationRow other = variety("Other", "2000");
		ObservationRow local = variety("Local", "2000");
		// Order: null, Local, Other -> lower median of 3 is index 1 -> Local.
		assertThat(ObservationSelection.representative(List.of(other, unnamed, local))).isEqualTo(local);
	}

	@Test
	void eachMarketUsesItsLatestDateWithACandidate() {
		ObservationRow older = row(null, null, null, null, DAY.minusDays(2), "1000", SourceDataset.CURRENT_PRICE);
		ObservationRow newer = row(null, null, null, null, DAY, "1500", SourceDataset.CURRENT_PRICE);
		Map<UUID, ObservationRow> selected = ObservationSelection.latestPerMarket(List.of(older, newer));
		assertThat(selected).containsEntry(MARKET, newer);
	}

	@Test
	void theDailySeriesHasOneSelectedRowPerDateInAscendingOrder() {
		ObservationRow dayOneLow = variety("A", "100");
		ObservationRow dayOneHigh = variety("B", "300");
		ObservationRow dayTwo = row(null, null, null, null, DAY.plusDays(1), "200", SourceDataset.HISTORICAL_PRICE);
		assertThat(ObservationSelection.dailySeries(List.of(dayTwo, dayOneHigh, dayOneLow)))
			.containsExactly(dayOneLow, dayTwo);
	}

	static ObservationRow variety(String name, String modal) {
		return row(UUID.randomUUID(), name, null, null, DAY, modal, SourceDataset.CURRENT_PRICE);
	}

	static ObservationRow row(UUID varietyId, String varietyName, UUID gradeId, String gradeName, LocalDate date,
			String modal, SourceDataset dataset) {
		return new ObservationRow(UUID.randomUUID(), MARKET, COMMODITY, varietyId, varietyName, gradeId, gradeName,
				date, new BigDecimal(modal), null, null, "Rs./Quintal", dataset, "synthetic-resource",
				Instant.parse("2026-10-05T00:00:00Z"));
	}

}
