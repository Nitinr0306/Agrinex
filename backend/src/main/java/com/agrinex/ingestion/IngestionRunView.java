package com.agrinex.ingestion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import com.agrinex.price.SourceDataset;

/**
 * One ingestion run with its audit counters (spec section 30 IngestionRunResponse; ADR-0012). {@code requestScope}
 * holds the request parameters, never credentials (ADR-0020 item 5).
 */
public record IngestionRunView(UUID id, SourceDataset datasetType, TriggeredBy triggeredBy, IngestionRunStatus status,
		Instant startedAt, Instant finishedAt, Map<String, Object> requestScope, Integer sourceTotalRecords, int pagesFetched,
		int pagesFailed, int recordsFetched, int recordsInserted, int recordsUpdated, int duplicatesSkipped,
		int validationErrorsCount, int recordsConflicted, int recordsOutOfScope, long errorCount) {

	/** Request-scope keys of the report dates of a HISTORICAL_PRICE run. */
	public static final String SCOPE_REPORT_DATE_FROM = "reportDateFrom";

	public static final String SCOPE_REPORT_DATE_TO = "reportDateTo";

	/** Request-scope key of a HISTORICAL_PRICE run's purpose (ADR-0054 item 5). */
	public static final String SCOPE_PURPOSE = "purpose";

	private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

	/** First report date requested (HISTORICAL_PRICE); null for CURRENT_PRICE. */
	public LocalDate reportDateFrom() {
		return scopeDate(SCOPE_REPORT_DATE_FROM);
	}

	/** Last report date requested (HISTORICAL_PRICE); null for CURRENT_PRICE. */
	public LocalDate reportDateTo() {
		return scopeDate(SCOPE_REPORT_DATE_TO);
	}

	/** Why a HISTORICAL_PRICE run was started; null for CURRENT_PRICE and for runs recorded before ADR-0054. */
	public RunPurpose purpose() {
		Object value = (this.requestScope != null) ? this.requestScope.get(SCOPE_PURPOSE) : null;
		return (value instanceof String name) ? RunPurpose.valueOf(name) : null;
	}

	/** finishedAt minus startedAt in whole seconds, rounded half-up; null while RUNNING. */
	public Long durationSeconds() {
		if (this.finishedAt == null) {
			return null;
		}
		Duration duration = Duration.between(this.startedAt, this.finishedAt);
		return BigDecimal.valueOf(duration.toMillis()).divide(BigDecimal.valueOf(1000), 0, RoundingMode.HALF_UP)
			.longValueExact();
	}

	private LocalDate scopeDate(String key) {
		Object value = (this.requestScope != null) ? this.requestScope.get(key) : null;
		return (value != null) ? LocalDate.parse(value.toString()) : null;
	}

	/**
	 * (inserted + updated + duplicates) / (fetched - out of scope) x 100, rounded half-up to 2 decimal places
	 * (ADR-0012 item 2); null while RUNNING or when the denominator is 0.
	 */
	public BigDecimal completenessPercent() {
		int inScope = this.recordsFetched - this.recordsOutOfScope;
		if (this.status == IngestionRunStatus.RUNNING || inScope <= 0) {
			return null;
		}
		BigDecimal stored = BigDecimal.valueOf((long) this.recordsInserted + this.recordsUpdated + this.duplicatesSkipped);
		return stored.multiply(HUNDRED).divide(BigDecimal.valueOf(inScope), 2, RoundingMode.HALF_UP);
	}

}
