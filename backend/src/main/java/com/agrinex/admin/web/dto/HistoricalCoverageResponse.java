package com.agrinex.admin.web.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.agrinex.ingestion.HistoricalCoverageView;
import com.agrinex.ingestion.HistoricalCoverageView.DateEntry;
import com.agrinex.ingestion.HistoricalCoverageView.RunProgress;
import com.agrinex.ingestion.HistoricalCoverageView.StatusCounts;

/** Contract HistoricalCoverageResponse (ADR-0054 item 6). The nested objects are the ingestion module's views. */
public record HistoricalCoverageResponse(Instant generatedAt, LocalDate coverageFrom, LocalDate coverageTo,
		int settleAfterDays, boolean scheduledRunsPaused, StatusCounts counts, List<DateEntry> incompleteDates,
		RunProgress runningRun, Instant nextGapCheckAt, Instant nextReconciliationAt, int maxDatesPerGapRun,
		int reconciliationWindowDays) {

	public static HistoricalCoverageResponse from(HistoricalCoverageView view) {
		return new HistoricalCoverageResponse(view.generatedAt(), view.coverageFrom(), view.coverageTo(),
				view.settleAfterDays(), view.scheduledRunsPaused(), view.counts(), view.incompleteDates(),
				view.runningRun(), view.nextGapCheckAt(), view.nextReconciliationAt(), view.maxDatesPerGapRun(),
				view.reconciliationWindowDays());
	}

}
