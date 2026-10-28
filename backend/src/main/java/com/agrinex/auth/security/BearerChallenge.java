package com.agrinex.auth.security;

/**
 * Values of the {@code WWW-Authenticate} header that every 401 carries (RFC 9110 section 15.5.2; RFC 6750
 * section 3; contract header WWW-Authenticate).
 */
public final class BearerChallenge {

	/** The challenge of a 401 that is not about a presented access token. */
	public static final String REALM = "Bearer realm=\"agrinex\"";

	/** The challenge of a 401 that rejects a presented access token. */
	public static final String INVALID_TOKEN = REALM + ", error=\"invalid_token\"";

	private BearerChallenge() {
	}

}
