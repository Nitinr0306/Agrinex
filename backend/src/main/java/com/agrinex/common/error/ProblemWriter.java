package com.agrinex.common.error;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

import com.agrinex.common.error.ApiErrorResponse.FieldError;
import com.agrinex.common.logging.CorrelationId;

import org.springframework.stereotype.Component;

/**
 * Writes {@link ApiErrorResponse} bodies as {@code application/problem+json}. Used by the MVC exception
 * handler and by the security filters, so every error has the same shape whatever layer produced it.
 */
@Component
public class ProblemWriter {

	public static final String PROBLEM_JSON = "application/problem+json";

	private final JsonMapper jsonMapper;

	public ProblemWriter(JsonMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}

	public ApiErrorResponse body(HttpServletRequest request, ErrorCode code, String detail, List<FieldError> errors) {
		return new ApiErrorResponse(code.type(), code.title(), code.status(), code.allowsDetail() ? detail : null,
				request.getRequestURI(), code, CorrelationId.of(request), List.copyOf(errors));
	}

	public void write(HttpServletRequest request, HttpServletResponse response, ApiException exception)
			throws IOException {
		write(request, response, exception.code(), exception.detail(), exception.errors(), exception.headers());
	}

	public void write(HttpServletRequest request, HttpServletResponse response, ErrorCode code, String detail,
			List<FieldError> errors, Map<String, String> headers) throws IOException {
		if (response.isCommitted()) {
			return;
		}
		response.resetBuffer();
		response.setStatus(code.status());
		headers.forEach(response::setHeader);
		response.setContentType(PROBLEM_JSON);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		this.jsonMapper.writeValue(response.getOutputStream(), body(request, code, detail, errors));
		response.flushBuffer();
	}

}
