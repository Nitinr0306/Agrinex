package com.agrinex.ingestion.domain;

import com.agrinex.ingestion.IngestionErrorType;

/**
 * Codes of {@code ingestion_errors.error_code} with their error type (ADR-0008 item 5, ADR-0009 item 2, ADR-0011
 * item 9, ADR-0013 item 4). The contract keeps the list open; FIELD_TOO_LONG, PRICE_TOO_LARGE,
 * PAGE_LIMIT_EXCEEDED and API_KEY_NOT_CONFIGURED are added by this module so that a value the schema cannot hold,
 * a never-ending source or a missing key is reported instead of failing a whole run.
 */
public enum IngestionErrorCode {

	/** V1: a required field is missing or blank. */
	REQUIRED_FIELD_MISSING(IngestionErrorType.VALIDATION),
	/** A text value longer than its column allows. */
	FIELD_TOO_LONG(IngestionErrorType.VALIDATION),
	/** V2: the date is not dd/MM/yyyy. */
	DATE_FORMAT_INVALID(IngestionErrorType.VALIDATION),
	/** V2: the date lies after today (Asia/Kolkata). */
	DATE_IN_FUTURE(IngestionErrorType.VALIDATION),
	/** V3: the commodity is not in the known list. */
	UNKNOWN_COMMODITY(IngestionErrorType.VALIDATION),
	/** V3: the commodity has no evidence-verified unit. */
	UNIT_UNVERIFIED(IngestionErrorType.VALIDATION),
	/** V4: the source district does not resolve. */
	UNKNOWN_DISTRICT(IngestionErrorType.VALIDATION),
	/** V5: a present price is not a decimal number. */
	PRICE_NOT_NUMERIC(IngestionErrorType.VALIDATION),
	/** V5: a present price is zero or negative. */
	PRICE_NON_POSITIVE(IngestionErrorType.VALIDATION),
	/** A price above the stored precision, numeric(14,2). */
	PRICE_TOO_LARGE(IngestionErrorType.VALIDATION),
	/** V6: min &gt; modal or modal &gt; max. */
	PRICE_RANGE_INVALID(IngestionErrorType.VALIDATION),
	/** V7: modal below 100.00 Rs./Quintal. */
	PRICE_BELOW_UNIT_FLOOR(IngestionErrorType.VALIDATION),
	/** V8: more than 10x or less than 0.1x the group's median modal. */
	PRICE_OUTLIER(IngestionErrorType.VALIDATION),
	/** ADR-0009 item 2: same identity, different prices, in one delivery unit. */
	SAME_KEY_DIFFERENT_PRICES(IngestionErrorType.CONFLICT),
	/** The source answered with an HTTP status other than 200. */
	HTTP_STATUS(IngestionErrorType.API_ERROR),
	/** The source did not answer in time. */
	TIMEOUT(IngestionErrorType.API_ERROR),
	/** The source could not be reached. */
	CONNECTION_FAILED(IngestionErrorType.API_ERROR),
	/** No API key is configured, so no request is sent. */
	API_KEY_NOT_CONFIGURED(IngestionErrorType.API_ERROR),
	/** The body is not JSON, for example an HTML maintenance page. */
	NON_JSON_RESPONSE(IngestionErrorType.SCHEMA_ERROR),
	/** The JSON lacks the records array or the total. */
	UNEXPECTED_STRUCTURE(IngestionErrorType.SCHEMA_ERROR),
	/** A delivery unit did not end within the configured page limit. */
	PAGE_LIMIT_EXCEEDED(IngestionErrorType.SCHEMA_ERROR),
	/** The load transaction of a delivery unit failed. */
	LOAD_FAILED(IngestionErrorType.SYSTEM),
	/** The run was still RUNNING when the application started. */
	RUN_INTERRUPTED(IngestionErrorType.SYSTEM),
	/** The run stopped on an unexpected error, or could not be started in the background. */
	UNEXPECTED_FAILURE(IngestionErrorType.SYSTEM),
	/** Alert evaluation after the run failed (ADR-0013 item 4); the run status is unchanged. */
	ALERT_EVALUATION_FAILED(IngestionErrorType.SYSTEM);

	private final IngestionErrorType type;

	IngestionErrorCode(IngestionErrorType type) {
		this.type = type;
	}

	public IngestionErrorType type() {
		return this.type;
	}

}
