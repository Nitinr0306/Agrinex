package com.agrinex.auth.security;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.agrinex.common.error.ErrorCode;
import com.agrinex.common.error.ProblemWriter;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * Answers 401 UNAUTHORIZED as problem+json with {@code WWW-Authenticate: Bearer realm="agrinex"}, adding
 * {@code error="invalid_token"} when a presented access token was rejected (RFC 6750 section 3; contract
 * header WWW-Authenticate). The reason is never detailed beyond the contract's wording.
 */
final class ProblemAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ProblemWriter problems;

	ProblemAuthenticationEntryPoint(ProblemWriter problems) {
		this.problems = problems;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		boolean tokenRejected = authException instanceof OAuth2AuthenticationException;
		String detail = tokenRejected ? "The access token is invalid or expired, or its account is not active."
				: "This operation requires an access token.";
		this.problems.write(request, response, ErrorCode.UNAUTHORIZED, detail, List.of(),
				Map.of(HttpHeaders.WWW_AUTHENTICATE, tokenRejected ? BearerChallenge.INVALID_TOKEN : BearerChallenge.REALM));
	}

}
