package com.agrinex.ingestion.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.stream.Stream;

import com.agrinex.common.time.AgrinexCalendar;
import com.agrinex.ingestion.ReportDateStatus;
import com.agrinex.ingestion.domain.IngestionReportDateEntity;
import com.agrinex.ingestion.domain.ReportDateOutcome;

/** The status rules and the gap selection of ADR-0054 (items 2 and 3), free of I/O so they can be unit-tested. */
final class HistoricalCoverage {

	private HistoricalCoverage() {
	}

	/**
	 * The status of a report date on {@code today}, from its ledger entry, which is null when never attempted.
	 * Ages are whole days between dates in Asia/Kolkata (ADR-0007).
	 */
	static ReportDateStatus status(LocalDate reportDate, LocalDate today, int settleDays,
			IngestionReportDateEntity entry) {
		boolean settleAgeReached = ChronoUnit.DAYS.between(reportDate, today) >= settleDays;
		if (entry == null) {
			return settleAgeReached ? ReportDateStatus.MISSING : ReportDateStatus.PENDING;
		}
		if (entry.getLastOutcome() == ReportDateOutcome.FAILED) {
			return ReportDateStatus.FAILED;
		}
		if (!settleAgeReached) {
			return ReportDateStatus.PENDING;
		}
		LocalDate fetchedOn = entry.getLastSuccessAt().atZone(AgrinexCalendar.ZONE).toLocalDate();
		return (ChronoUnit.DAYS.between(reportDate, fetchedOn) >= settleDays) ? ReportDateStatus.SETTLED
				: ReportDateStatus.UNSETTLED;
	}

	/**
	 * The report dates of the next gap-backfill run (ADR-0054 item 3), or empty when no date at the settle age is
	 * incomplete: from the oldest incomplete date within the {@code maxDates} dates that end at the newest
	 * incomplete date, to that newest date. Complete dates in between are included; fetching them again is
	 * idempotent.
	 */
	static Optional<List<LocalDate>> gapRange(NavigableMap<LocalDate, ReportDateStatus> statuses, LocalDate today,
			int settleDays, int maxDates) {
		LocalDate settledBefore = today.minusDays(settleDays - 1L);
		Optional<LocalDate> newest = statuses.headMap(settledBefore, false)
			.descendingMap()
			.entrySet()
			.stream()
			.filter((entry) -> entry.getValue().incomplete())
			.map(Map.Entry::getKey)
			.findFirst();
		return newest.map((to) -> {
			LocalDate earliest = to.minusDays(maxDates - 1L);
			LocalDate from = statuses.subMap(earliest, true, to, true)
				.entrySet()
				.stream()
				.filter((entry) -> entry.getValue().incomplete())
				.map(Map.Entry::getKey)
				.findFirst()
				.orElse(to);
			return Stream.iterate(from, (date) -> !date.isAfter(to), (date) -> date.plusDays(1)).toList();
		});
	}

}
