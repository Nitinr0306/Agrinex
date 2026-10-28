package com.agrinex.auth.security;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Reads the {@code Authorization} header only on the operations whose contract {@code security} lists
 * {@code bearerAuth}; every other operation ignores it, so a stale header never blocks a refresh or a logout
 * (docs/api/API_CONVENTIONS.md section 7). Tokens are accepted only in the header, never in a query or form
 * parameter (RFC 6750 section 2).
 */
final class ContractBearerTokenResolver implements BearerTokenResolver {

	/** The admin operations: ADMIN only (ADR-0020). */
	static final RequestMatcher ADMIN = PathPatternRequestMatcher.pathPattern("/api/v1/admin/**");

	/** Operations that require a token: alerts, admin and the verification-link resend. */
	static final RequestMatcher TOKEN_REQUIRED = new OrRequestMatcher(
			PathPatternRequestMatcher.pathPattern("/api/v1/alerts/**"), ADMIN,
			PathPatternRequestMatcher.pathPattern("/api/v1/auth/email-verification/resend"));

	/** Operations that accept an optional token, which links the recorded search to the caller (ADR-0042). */
	static final RequestMatcher TOKEN_OPTIONAL = new OrRequestMatcher(
			PathPatternRequestMatcher.pathPattern("/api/v1/selling-options/compare"),
			PathPatternRequestMatcher.pathPattern("/api/v1/selling-options/simulate"));

	private final DefaultBearerTokenResolver headerResolver = new DefaultBearerTokenResolver();

	@Override
	public String resolve(HttpServletRequest request) {
		if (TOKEN_REQUIRED.matches(request) || TOKEN_OPTIONAL.matches(request)) {
			return this.headerResolver.resolve(request);
		}
		return null;
	}

}
