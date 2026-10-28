package com.agrinex.ingestion.service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import com.agrinex.common.time.AgrinexCalendar;
import com.agrinex.ingestion.domain.IngestionReportDateEntity;
import com.agrinex.ingestion.repository.IngestionReportDateRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Writes the outcome of each HISTORICAL_PRICE delivery unit to the report-date ledger (ADR-0054 item 1), in its
 * own transaction after the unit. At most one run per dataset is RUNNING (ADR-0011 item 4), so two runs never
 * write the same date at once.
 */
@Component
class ReportDateLedger {

	private final IngestionReportDateRepository dates;

	private final AgrinexCalendar calendar;

	ReportDateLedger(IngestionReportDateRepository dates, AgrinexCalendar calendar) {
		this.dates = dates;
		this.calendar = calendar;
	}

	@Transactional
	public void recordFetched(UUID sourceDatasetId, LocalDate reportDate, UUID runId, int recordsFetched) {
		record(sourceDatasetId, reportDate, (entry) -> entry.fetched(runId, this.calendar.now(), recordsFetched));
	}

	@Transactional
	public void recordFailed(UUID sourceDatasetId, LocalDate reportDate, UUID runId) {
		record(sourceDatasetId, reportDate, (entry) -> entry.failed(runId, this.calendar.now()));
	}

	/** Applies the outcome before a new row is saved, because an insert carries the values it was saved with. */
	private void record(UUID sourceDatasetId, LocalDate reportDate, Consumer<IngestionReportDateEntity> outcome) {
		Optional<IngestionReportDateEntity> existing = this.dates.findBySourceDatasetIdAndReportDate(sourceDatasetId,
				reportDate);
		IngestionReportDateEntity entry = existing.orElseGet(() -> IngestionReportDateEntity.of(sourceDatasetId,
				reportDate));
		outcome.accept(entry);
		if (existing.isEmpty()) {
			this.dates.save(entry);
		}
	}

}
