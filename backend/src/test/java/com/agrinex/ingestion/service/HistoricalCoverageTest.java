package com.agrinex.ingestion.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.UUID;

import com.agrinex.common.time.AgrinexCalendar;
import com.agrinex.ingestion.ReportDateStatus;
import com.agrinex.ingestion.domain.IngestionReportDateEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** The status rules and the gap selection of ADR-0054 items 2 and 3, with a settle age of 3 days. */
class HistoricalCoverageTest {

	static final LocalDate TODAY = LocalDate.of(2026, 10, 18);

	static final int SETTLE = 3;

	static final UUID DATASET = UUID.randomUUID();

	@Test
	void aNeverAttemptedDateIsPendingUntilTheSettleAgeThenMissing() {
		assertThat(HistoricalCoverage.status(TODAY.minusDays(2), TODAY, SETTLE, null))
			.isEqualTo(ReportDateStatus.PENDING);
		assertThat(HistoricalCoverage.status(TODAY.minusDays(3), TODAY, SETTLE, null))
			.isEqualTo(ReportDateStatus.MISSING);
	}

	@Test
	void aDateFetchedAtTheSettleAgeIsSettledAndOneFetchedEarlierIsUnsettled() {
		LocalDate date = TODAY.minusDays(5);
		assertThat(HistoricalCoverage.status(date, TODAY, SETTLE, fetchedOn(date, date.plusDays(3))))
			.isEqualTo(ReportDateStatus.SETTLED);
		assertThat(HistoricalCoverage.status(date, TODAY, SETTLE, fetchedOn(date, date.plusDays(2))))
			.isEqualTo(ReportDateStatus.UNSETTLED);
	}

	@Test
	void aRecentFetchIsPendingAndAFailedLastAttemptIsFailedAtAnyAge() {
		LocalDate recent = TODAY.minusDays(1);
		assertThat(HistoricalCoverage.status(recent, TODAY, SETTLE, fetchedOn(recent, TODAY)))
			.isEqualTo(ReportDateStatus.PENDING);
		IngestionReportDateEntity failed = fetchedOn(recent, TODAY);
		failed.failed(UUID.randomUUID(), at(TODAY));
		assertThat(HistoricalCoverage.status(recent, TODAY, SETTLE, failed)).isEqualTo(ReportDateStatus.FAILED);
	}

	@Test
	void theGapRunSpansTheNewestIncompleteDatesWithinTheLimit() {
		NavigableMap<LocalDate, ReportDateStatus> statuses = allSettled(60);
		statuses.put(TODAY.minusDays(40), ReportDateStatus.MISSING);
		statuses.put(TODAY.minusDays(20), ReportDateStatus.FAILED);
		statuses.put(TODAY.minusDays(9), ReportDateStatus.UNSETTLED);
		// Younger than the settle age: left to the trailing window even when incomplete.
		statuses.put(TODAY.minusDays(1), ReportDateStatus.FAILED);

		List<LocalDate> gap = HistoricalCoverage.gapRange(statuses, TODAY, SETTLE, 31).orElseThrow();

		assertThat(gap.getFirst()).isEqualTo(TODAY.minusDays(20));
		assertThat(gap.getLast()).isEqualTo(TODAY.minusDays(9));
		assertThat(gap).hasSize(12);
	}

	@Test
	void aSingleIncompleteDateIsItsOwnRangeAndACompleteWindowHasNoGap() {
		NavigableMap<LocalDate, ReportDateStatus> statuses = allSettled(60);
		assertThat(HistoricalCoverage.gapRange(statuses, TODAY, SETTLE, 31)).isEmpty();

		statuses.put(TODAY.minusDays(3), ReportDateStatus.MISSING);
		assertThat(HistoricalCoverage.gapRange(statuses, TODAY, SETTLE, 31))
			.contains(List.of(TODAY.minusDays(3)));
	}

	@Test
	void aLongGapIsFilledNewestFirstInRunsOfAtMostTheLimit() {
		NavigableMap<LocalDate, ReportDateStatus> statuses = allSettled(100);
		for (int age = 3; age < 100; age++) {
			statuses.put(TODAY.minusDays(age), ReportDateStatus.MISSING);
		}
		List<LocalDate> gap = HistoricalCoverage.gapRange(statuses, TODAY, SETTLE, 31).orElseThrow();
		assertThat(gap).hasSize(31);
		assertThat(gap.getFirst()).isEqualTo(TODAY.minusDays(33));
		assertThat(gap.getLast()).isEqualTo(TODAY.minusDays(3));
	}

	/** Dates from today - (days - 1) to today, settled when old enough and pending otherwise. */
	private static NavigableMap<LocalDate, ReportDateStatus> allSettled(int days) {
		NavigableMap<LocalDate, ReportDateStatus> statuses = new TreeMap<>();
		for (int age = 0; age < days; age++) {
			statuses.put(TODAY.minusDays(age), (age < SETTLE) ? ReportDateStatus.PENDING : ReportDateStatus.SETTLED);
		}
		return statuses;
	}

	private static IngestionReportDateEntity fetchedOn(LocalDate reportDate, LocalDate fetchDay) {
		IngestionReportDateEntity entry = IngestionReportDateEntity.of(DATASET, reportDate);
		entry.fetched(UUID.randomUUID(), at(fetchDay), 10);
		return entry;
	}

	/** Noon in Asia/Kolkata on the day. */
	private static Instant at(LocalDate day) {
		return day.atTime(LocalTime.NOON).atZone(AgrinexCalendar.ZONE).toInstant();
	}

}
