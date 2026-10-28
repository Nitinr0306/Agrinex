package com.agrinex.common.error;

import java.util.Locale;

/**
 * Stable top-level error codes with their fixed HTTP status and title (contracts/error-response.schema.json;
 * docs/api/API_CONVENTIONS.md section 6.1). The problem type is {@code /problems/<code in kebab case>}.
 */
public enum ErrorCode {

	MALFORMED_REQUEST(400, "Malformed request"),
	VALIDATION_FAILED(400, "Validation failed"),
	UNAUTHORIZED(401, "Unauthorized"),
	INVALID_CREDENTIALS(401, "Invalid credentials"),
	REFRESH_TOKEN_INVALID(401, "Session cannot be refreshed"),
	FORBIDDEN(403, "Forbidden"),
	ORIGIN_NOT_ALLOWED(403, "Origin not allowed"),
	NOT_FOUND(404, "Not found"),
	METHOD_NOT_ALLOWED(405, "Method not allowed"),
	NOT_ACCEPTABLE(406, "Not acceptable"),
	CONFLICT(409, "Conflict"),
	UNSUPPORTED_MEDIA_TYPE(415, "Unsupported media type"),
	NO_MARKETS_IN_RANGE(422, "Insufficient data"),
	UNSUPPORTED_PRICE_UNIT(422, "Unsupported price unit"),
	REFERENCE_NOT_FOUND(422, "Referenced resource not found"),
	ESTIMATE_OUT_OF_RANGE(422, "Estimate out of range"),
	PASSWORD_RESET_TOKEN_INVALID(422, "Reset link invalid or expired"),
	EMAIL_VERIFICATION_TOKEN_INVALID(422, "Verification link invalid or expired"),
	RATE_LIMITED(429, "Too many requests"),
	INTERNAL_ERROR(500, "Internal error"),
	SERVICE_UNAVAILABLE(503, "Service unavailable");

	private final int status;

	private final String title;

	ErrorCode(int status, String title) {
		this.status = status;
		this.title = title;
	}

	public int status() {
		return this.status;
	}

	public String title() {
		return this.title;
	}

	public String type() {
		return "/problems/" + name().toLowerCase(Locale.ROOT).replace('_', '-');
	}

	/** 500 and 503 never carry a detail, so no internal information can leak (error-response schema). */
	public boolean allowsDetail() {
		return this != INTERNAL_ERROR && this != SERVICE_UNAVAILABLE;
	}

}
