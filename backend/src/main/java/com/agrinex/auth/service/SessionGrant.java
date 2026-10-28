package com.agrinex.auth.service;

import java.time.Duration;

/**
 * A signed-in session: a new access token and a new refresh token for the cookie (ADR-0016 items 4 and 5). Both
 * tokens are secrets, so {@link #toString()} never shows them.
 */
public record SessionGrant(String accessToken, long expiresInSeconds, String refreshToken, Duration refreshTokenTtl,
		AuthenticatedUserView user) {

	@Override
	public String toString() {
		return "SessionGrant[user=" + this.user.id() + "]";
	}

}
