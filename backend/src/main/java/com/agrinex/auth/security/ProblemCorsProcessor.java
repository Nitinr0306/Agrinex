package com.agrinex.auth.security;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.agrinex.common.error.ErrorCode;
import com.agrinex.common.error.ProblemWriter;

import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.DefaultCorsProcessor;

/**
 * Spring's CORS checks with one change: a rejected cross-origin request answers the contract's 403
 * {@code ORIGIN_NOT_ALLOWED} ApiErrorResponse (ADR-0017, ADR-0022) instead of the framework's plain-text "Invalid
 * CORS request", so every error has the contract's shape (spec section 28). Which origins, methods and headers
 * are accepted does not change.
 */
class ProblemCorsProcessor extends DefaultCorsProcessor {

	static final String DETAIL = "The Origin header must equal the public origin.";

	private final ProblemWriter problems;

	ProblemCorsProcessor(ProblemWriter problems) {
		this.problems = problems;
	}

	@Override
	public boolean processRequest(CorsConfiguration configuration, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		boolean accepted = super.processRequest(configuration, request, response);
		if (!accepted) {
			this.problems.write(request, response, ErrorCode.ORIGIN_NOT_ALLOWED, DETAIL, List.of(), Map.of());
		}
		return accepted;
	}

	/** The rejection is written by {@link #processRequest} as an ApiErrorResponse; nothing is written here. */
	@Override
	protected void rejectRequest(ServerHttpResponse response) {
	}

}
