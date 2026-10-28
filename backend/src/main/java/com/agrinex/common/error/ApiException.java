package com.agrinex.common.error;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.agrinex.common.error.ApiErrorResponse.FieldError;

/**
 * An error answered with a contract error code. Thrown by modules and translated into an
 * {@link ApiErrorResponse} by the exception handler. {@code detail} is English text safe to show to a
 * developer: it never contains internal details or personal data.
 */
public class ApiException extends RuntimeException {

	private final ErrorCode code;

	private final String detail;

	private final List<FieldError> errors;

	private final Map<String, String> headers;

	public ApiException(ErrorCode code, String detail, List<FieldError> errors, Map<String, String> headers) {
		super(code.name());
		this.code = code;
		this.detail = code.allowsDetail() ? detail : null;
		this.errors = List.copyOf(errors);
		this.headers = Map.copyOf(headers);
	}

	public ApiException(ErrorCode code, String detail) {
		this(code, detail, List.of(), Map.of());
	}

	public static ApiException notFound(String detail) {
		return new ApiException(ErrorCode.NOT_FOUND, detail);
	}

	public static ApiException conflict(String detail) {
		return new ApiException(ErrorCode.CONFLICT, detail);
	}

	public static ApiException conflict(String detail, List<FieldError> errors) {
		return new ApiException(ErrorCode.CONFLICT, detail, errors, Map.of());
	}

	public static ApiException validation(String field, FieldErrorCode fieldCode, String message) {
		return new ApiException(ErrorCode.VALIDATION_FAILED, "The request has invalid fields.",
				List.of(new FieldError(field, fieldCode, message)), Map.of());
	}

	public static ApiException validation(List<FieldError> errors) {
		return new ApiException(ErrorCode.VALIDATION_FAILED, "The request has invalid fields.", errors, Map.of());
	}

	public static ApiException referenceNotFound(String... fields) {
		List<FieldError> errors = java.util.Arrays.stream(fields)
			.map((field) -> new FieldError(field, FieldErrorCode.NOT_FOUND, "names nothing that exists or qualifies"))
			.toList();
		return new ApiException(ErrorCode.REFERENCE_NOT_FOUND, "A referenced resource does not exist or does not qualify.",
				errors, Map.of());
	}

	public static ApiException rateLimited(long retryAfterSeconds) {
		Map<String, String> headers = new LinkedHashMap<>();
		headers.put("Retry-After", Long.toString(Math.max(1, retryAfterSeconds)));
		return new ApiException(ErrorCode.RATE_LIMITED, "Too many requests. Retry after the indicated time.", List.of(),
				headers);
	}

	public ErrorCode code() {
		return this.code;
	}

	public String detail() {
		return this.detail;
	}

	public List<FieldError> errors() {
		return this.errors;
	}

	public Map<String, String> headers() {
		return this.headers;
	}

}
