package com.agrinex.auth.security;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.agrinex.common.error.ErrorCode;
import com.agrinex.common.error.ProblemWriter;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

/** Answers 403 FORBIDDEN as problem+json when the caller's role is not permitted (spec section 28, FR-20). */
final class ProblemAccessDeniedHandler implements AccessDeniedHandler {

	private final ProblemWriter problems;

	ProblemAccessDeniedHandler(ProblemWriter problems) {
		this.problems = problems;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException {
		this.problems.write(request, response, ErrorCode.FORBIDDEN, "The caller's role is not permitted.", List.of(),
				Map.of());
	}

}
