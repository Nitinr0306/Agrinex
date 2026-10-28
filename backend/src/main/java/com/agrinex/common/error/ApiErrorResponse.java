package com.agrinex.common.error;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The single error body of the API: RFC 9457 Problem Details with the members {@code code},
 * {@code correlationId} and {@code errors} (spec section 10 ApiErrorResponse; ADR-0022 item 4). It never
 * contains stack traces, SQL, class names, secrets, tokens or other users' data (spec section 28).
 */
public record ApiErrorResponse(
		String type,
		String title,
		int status,
		@JsonInclude(JsonInclude.Include.NON_NULL) String detail,
		@JsonInclude(JsonInclude.Include.NON_NULL) String instance,
		ErrorCode code,
		String correlationId,
		List<FieldError> errors) {

	public record FieldError(String field, FieldErrorCode code, String message) {
	}

}
