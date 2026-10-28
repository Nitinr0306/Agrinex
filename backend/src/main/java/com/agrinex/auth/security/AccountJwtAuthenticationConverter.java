package com.agrinex.auth.security;

import java.util.UUID;

import com.agrinex.auth.service.AccountResolver;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

/**
 * Turns a valid access token into an authentication by resolving its account: an inactive or removed account is
 * refused with 401 {@code invalid_token} (ADR-0042 item 2, SR-04). The role comes from the account, which is the
 * current authority, not from the token.
 */
final class AccountJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

	private final AccountResolver accounts;

	AccountJwtAuthenticationConverter(AccountResolver accounts) {
		this.accounts = accounts;
	}

	@Override
	public AbstractAuthenticationToken convert(Jwt jwt) {
		UUID userId = parseSubject(jwt.getSubject());
		return this.accounts.resolveActive(userId)
			.map(AccountAuthentication::new)
			.orElseThrow(() -> new InvalidBearerTokenException("The account is inactive or no longer exists"));
	}

	private static UUID parseSubject(String subject) {
		if (subject == null) {
			throw new InvalidBearerTokenException("The access token has no subject");
		}
		try {
			return UUID.fromString(subject);
		}
		catch (IllegalArgumentException ex) {
			throw new InvalidBearerTokenException("The access token subject is not an account id");
		}
	}

}
