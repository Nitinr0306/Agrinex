package com.agrinex.price.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.List;

import com.agrinex.price.TrendDirection;
import com.agrinex.price.TrendStatistic;

/**
 * The trend statistic of spec section 33 as decided in ADR-0025, on a daily series in the Asia/Kolkata calendar:
 * <ul>
 * <li>recent window: today - 6 to today; baseline window: today - 13 to today - 7;</li>
 * <li>changePercent7Day = (mean of recent daily values - mean of baseline daily values) / baseline mean x 100;</li>
 * <li>RISING above +3.0, FALLING below -3.0, otherwise STABLE, classified on the unrounded value;</li>
 * <li>INSUFFICIENT_DATA when fewer than 4 daily values exist in the last 14 days, or a window is empty.</li>
 * </ul>
 * Missing dates are never filled or interpolated (spec section 31 RULE 5).
 */
public final class TrendCalculator {

	static final int RECENT_WINDOW_DAYS = 7;

	static final int TREND_SPAN_DAYS = 14;

	static final int MINIMUM_DAILY_VALUES = 4;

	private static final BigDecimal THRESHOLD_PERCENT = new BigDecimal("3.0");

	private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

	private TrendCalculator() {
	}

	public static LocalDate spanStart(LocalDate today) {
		return today.minusDays(TREND_SPAN_DAYS - 1);
	}

	static LocalDate recentStart(LocalDate today) {
		return today.minusDays(RECENT_WINDOW_DAYS - 1);
	}

	static LocalDate baselineEnd(LocalDate today) {
		return today.minusDays(RECENT_WINDOW_DAYS);
	}

	public static TrendStatistic compute(List<ObservationRow> dailySeries, LocalDate today) {
		LocalDate spanStart = spanStart(today);
		LocalDate recentStart = recentStart(today);
		List<ObservationRow> span = dailySeries.stream()
			.filter((row) -> !row.reportDate().isBefore(spanStart) && !row.reportDate().isAfter(today))
			.toList();
		List<BigDecimal> recent = span.stream()
			.filter((row) -> !row.reportDate().isBefore(recentStart))
			.map(ObservationRow::modalPrice)
			.toList();
		List<BigDecimal> baseline = span.stream()
			.filter((row) -> row.reportDate().isBefore(recentStart))
			.map(ObservationRow::modalPrice)
			.toList();
		if (span.size() < MINIMUM_DAILY_VALUES || recent.isEmpty() || baseline.isEmpty()) {
			return TrendStatistic.INSUFFICIENT;
		}
		BigDecimal recentMean = mean(recent);
		BigDecimal baselineMean = mean(baseline);
		BigDecimal change = recentMean.subtract(baselineMean)
			.divide(baselineMean, MathContext.DECIMAL128)
			.multiply(HUNDRED, MathContext.DECIMAL128);
		return new TrendStatistic(direction(change), change);
	}

	private static TrendDirection direction(BigDecimal changePercent) {
		if (changePercent.compareTo(THRESHOLD_PERCENT) > 0) {
			return TrendDirection.RISING;
		}
		if (changePercent.compareTo(THRESHOLD_PERCENT.negate()) < 0) {
			return TrendDirection.FALLING;
		}
		return TrendDirection.STABLE;
	}

	private static BigDecimal mean(List<BigDecimal> values) {
		BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
		return sum.divide(BigDecimal.valueOf(values.size()), MathContext.DECIMAL128);
	}

}
