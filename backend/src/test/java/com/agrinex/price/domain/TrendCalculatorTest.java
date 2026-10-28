package com.agrinex.price.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.agrinex.price.SourceDataset;
import com.agrinex.price.TrendDirection;
import com.agrinex.price.TrendStatistic;

import static org.assertj.core.api.Assertions.assertThat;

/** The ADR-0025 trend on hand-computed SYNTHETIC daily series (never government observations). */
class TrendCalculatorTest {

	static final LocalDate TODAY = LocalDate.of(2026, 10, 5);

	@Test
	void aRiseAboveThreePercentIsRising() {
		// Baseline (today-13..today-7): 100, 100 -> mean 100. Recent (today-6..today): 104, 104 -> mean 104. +4 %.
		TrendStatistic trend = TrendCalculator.compute(series(13, "100", 10, "100", 6, "104", 0, "104"), TODAY);
		assertThat(trend.direction()).isEqualTo(TrendDirection.RISING);
		assertThat(trend.changePercent()).isEqualByComparingTo("4");
	}

	@Test
	void exactlyThreePercentIsStable() {
		TrendStatistic trend = TrendCalculator.compute(series(13, "100", 7, "100", 6, "103", 0, "103"), TODAY);
		assertThat(trend.direction()).isEqualTo(TrendDirection.STABLE);
	}

	@Test
	void aFallBelowMinusThreePercentIsFalling() {
		// Baseline 200, 200; recent 190, 192 -> mean 191: (191 - 200) / 200 = -4.5 %.
		TrendStatistic trend = TrendCalculator.compute(series(12, "200", 8, "200", 5, "190", 1, "192"), TODAY);
		assertThat(trend.direction()).isEqualTo(TrendDirection.FALLING);
		assertThat(trend.changePercent()).isEqualByComparingTo("-4.5");
		assertThat(trend.displayChangePercent()).isEqualByComparingTo("-4.5");
	}

	@Test
	void fewerThanFourDailyValuesIsInsufficient() {
		TrendStatistic trend = TrendCalculator.compute(series(13, "100", 6, "110", 0, "120"), TODAY);
		assertThat(trend).isEqualTo(TrendStatistic.INSUFFICIENT);
	}

	@Test
	void anEmptyWindowIsInsufficient() {
		// Four values, all in the recent window: the baseline window is empty.
		TrendStatistic trend = TrendCalculator.compute(series(6, "100", 4, "100", 2, "100", 0, "100"), TODAY);
		assertThat(trend).isEqualTo(TrendStatistic.INSUFFICIENT);
	}

	@Test
	void valuesOutsideTheFourteenDaysAreIgnored() {
		// today-14 (1000) is outside the span: the baseline stays 100, 100 and the change stays +4 %.
		TrendStatistic trend = TrendCalculator.compute(series(14, "1000", 13, "100", 10, "100", 6, "104", 0, "104"),
				TODAY);
		assertThat(trend.changePercent()).isEqualByComparingTo("4");
		// Without today-10 only three values remain in the span.
		TrendStatistic shorter = TrendCalculator.compute(series(14, "1000", 13, "100", 6, "104", 0, "104"), TODAY);
		assertThat(shorter).isEqualTo(TrendStatistic.INSUFFICIENT);
	}

	/** Pairs of (days before today, modal price), one synthetic observation each. */
	static List<ObservationRow> series(Object... daysAndPrices) {
		List<ObservationRow> rows = new ArrayList<>();
		for (int index = 0; index < daysAndPrices.length; index += 2) {
			rows.add(row(TODAY.minusDays((Integer) daysAndPrices[index]), (String) daysAndPrices[index + 1]));
		}
		return rows;
	}

	static ObservationRow row(LocalDate date, String modal) {
		return new ObservationRow(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, null, null, null, date,
				new BigDecimal(modal), null, null, "Rs./Quintal", SourceDataset.CURRENT_PRICE, "synthetic-resource",
				Instant.parse("2026-10-05T00:00:00Z"));
	}

}
