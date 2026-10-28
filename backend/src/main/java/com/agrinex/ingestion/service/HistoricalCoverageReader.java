package com.agrinex.ingestion.service;

import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.agrinex.ingestion.ReportDateStatus;
import com.agrinex.ingestion.config.HistoricalMaintenanceProperties;
import com.agrinex.ingestion.config.IngestionProperties;
import com.agrinex.ingestion.domain.IngestionReportDateEntity;
import com.agrinex.ingestion.repository.IngestionReportDateRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reads the report-date ledger over the covered dates and computes each date's status (ADR-0054 item 2). The
 * covered dates are the last {@code coverage-days} report dates, ending today; the settle age is the trailing
 * window's depth.
 */
@Component
class HistoricalCoverageReader {

	private final IngestionReportDateRepository dates;

	private final IngestionProperties ingestion;

	private final HistoricalMaintenanceProperties maintenance;

	HistoricalCoverageReader(IngestionReportDateRepository dates, IngestionProperties ingestion,
			HistoricalMaintenanceProperties maintenance) {
		this.dates = dates;
		this.ingestion = ingestion;
		this.maintenance = maintenance;
	}

	int settleDays() {
		return this.ingestion.historicalWindowDaysBack();
	}

	LocalDate coverageFrom(LocalDate today) {
		return today.minusDays(this.maintenance.coverageDays() - 1L);
	}

	@Transactional(readOnly = true)
	public Coverage read(UUID sourceDatasetId, LocalDate today) {
		LocalDate from = coverageFrom(today);
		Map<LocalDate, IngestionReportDateEntity> entries = this.dates
			.findBySourceDatasetIdAndReportDateBetween(sourceDatasetId, from, today)
			.stream()
			.collect(Collectors.toMap(IngestionReportDateEntity::getReportDate, Function.identity()));
		NavigableMap<LocalDate, ReportDateStatus> statuses = new TreeMap<>();
		for (LocalDate date = from; !date.isAfter(today); date = date.plusDays(1)) {
			statuses.put(date, HistoricalCoverage.status(date, today, settleDays(), entries.get(date)));
		}
		return new Coverage(statuses, entries);
	}

	/** Each covered date's status in date order, and the ledger entries that exist. */
	record Coverage(NavigableMap<LocalDate, ReportDateStatus> statuses,
			Map<LocalDate, IngestionReportDateEntity> entries) {
	}

}
