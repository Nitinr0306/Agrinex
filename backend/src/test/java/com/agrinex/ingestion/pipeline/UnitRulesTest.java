package com.agrinex.ingestion.pipeline;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import com.agrinex.commodity.CommodityInfo;
import com.agrinex.ingestion.pipeline.UnitDeduplicator.ResolvedRecord;
import com.agrinex.ingestion.source.SourceSchema;

import static org.assertj.core.api.Assertions.assertThat;

/** Rule V8 and identity handling within one delivery unit (ADR-0008 item 5, ADR-0009 item 2), on SYNTHETIC data. */
class UnitRulesTest {

	static final LocalDate DAY = LocalDate.of(2026, 9, 14);

	static final CommodityInfo ONION = new CommodityInfo(UUID.randomUUID(), "Onion", null, null, "Vegetables",
			"Rs./Quintal", "synthetic test evidence", Instant.parse("2026-01-01T00:00:00Z"));

	@Test
	void v8RejectsValuesMoreThanTenTimesOrUnderATenthOfTheMedian() {
		// Modals 100, 1000, 1100, 1200, 15000: median 1100; 15000 > 11000 and 100 < 110 are outliers.
		List<ValidRecord> records = List.of(valid("100"), valid("1000"), valid("1100"), valid("1200"), valid("15000"));
		OutlierRule.Split split = OutlierRule.apply(records);
		assertThat(split.outliers()).extracting((outlier) -> outlier.record().modalPrice())
			.containsExactlyInAnyOrder(new BigDecimal("100"), new BigDecimal("15000"));
		assertThat(split.kept()).extracting(ValidRecord::modalPrice)
			.containsExactly(new BigDecimal("1000"), new BigDecimal("1100"), new BigDecimal("1200"));
	}

	@Test
	void v8NeedsAtLeastFiveRecordsInTheGroup() {
		List<ValidRecord> records = List.of(valid("100"), valid("1000"), valid("1100"), valid("15000"));
		assertThat(OutlierRule.apply(records).outliers()).isEmpty();
	}

	@Test
	void theMedianOfAnEvenCountIsTheMeanOfTheMiddleValues() {
		assertThat(OutlierRule.median(List.of(new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("4"),
				new BigDecimal("9")))).isEqualByComparingTo("3");
	}

	@Test
	void identicalCopiesBecomeOneCandidateAndCountAsDuplicates() {
		UUID market = UUID.randomUUID();
		UnitDeduplicator.Result result = UnitDeduplicator.deduplicate(
				List.of(resolved(market, "4100"), resolved(market, "4100"), resolved(market, "4100")));
		assertThat(result.candidates()).hasSize(1);
		assertThat(result.duplicates()).isEqualTo(2);
		assertThat(result.conflicts()).isEmpty();
	}

	@Test
	void sameIdentityWithDifferentPricesIsAConflictForEveryRecord() {
		UUID market = UUID.randomUUID();
		UnitDeduplicator.Result result = UnitDeduplicator
			.deduplicate(List.of(resolved(market, "4100"), resolved(market, "4200"), resolved(UUID.randomUUID(), "4100")));
		assertThat(result.candidates()).hasSize(1);
		assertThat(result.conflicts()).hasSize(2);
		assertThat(result.duplicates()).isZero();
	}

	static ValidRecord valid(String modal) {
		SourceRecord source = SourceRecord.from(JsonMapper.builder().build().readTree("{\"state\": \"Maharashtra\"}"),
				SourceSchema.CURRENT);
		return new ValidRecord(source, ONION, null, DAY, new BigDecimal(modal), null, null);
	}

	static ResolvedRecord resolved(UUID market, String modal) {
		return new ResolvedRecord(valid(modal), market, null, null);
	}

}
