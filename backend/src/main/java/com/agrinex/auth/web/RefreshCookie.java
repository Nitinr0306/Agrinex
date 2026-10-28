package com.agrinex.auth.web;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.agrinex.auth.security.BearerChallenge;
import com.agrinex.common.error.ApiException;
import com.agrinex.common.error.ErrorCode;

import org.springframework.http.HttpHeaders;

/**
 * The refresh-token cookie {@code __Secure-agrinex_refresh} (ADR-0016, ADR-0017 item 3, ADR-0042 item 3). The
 * {@code Set-Cookie} values are written exactly as the contract's headers Set-Cookie-Refresh and
 * Set-Cookie-Clear define them, attribute order included, so they are built here rather than by a cookie API
 * that adds {@code Expires} or reorders attributes.
 */
final class RefreshCookie {

	static final String NAME = "__Secure-agrinex_refresh";

	static final String ATTRIBUTES = "; Path=/api/v1/auth; HttpOnly; Secure; SameSite=Strict";

	static final String CLEAR = NAME + "=; Max-Age=0" + ATTRIBUTES;

	private RefreshCookie() {
	}

	static String set(String refreshToken, Duration lifetime) {
		return NAME + "=" + refreshToken + "; Max-Age=" + lifetime.toSeconds() + ATTRIBUTES;
	}

	/** 401 REFRESH_TOKEN_INVALID, which also clears the cookie: the client signs in again. */
	static ApiException invalid() {
		return new ApiException(ErrorCode.REFRESH_TOKEN_INVALID, "The session cannot be refreshed. Sign in again.",
				List.of(), Map.of(HttpHeaders.SET_COOKIE, CLEAR, HttpHeaders.WWW_AUTHENTICATE, BearerChallenge.REALM));
	}

}
