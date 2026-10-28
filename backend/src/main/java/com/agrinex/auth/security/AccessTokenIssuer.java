package com.agrinex.auth.security;

import java.time.Instant;
import java.util.UUID;

import com.agrinex.auth.Role;
import com.agrinex.auth.config.AuthProperties;
import com.agrinex.common.time.AgrinexCalendar;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

/**
 * Issues access tokens: HS256 JWTs valid for 15 minutes with the claims {@code sub} (user id), {@code role},
 * {@code iat}, {@code exp} and {@code iss} = {@code agrinex} (spec section 28; ADR-0016 item 4).
 */
@Component
public class AccessTokenIssuer {

	public static final String ISSUER = "agrinex";

	public static final String ROLE_CLAIM = "role";

	private final JwtEncoder encoder;

	private final AuthProperties properties;

	private final AgrinexCalendar calendar;

	AccessTokenIssuer(JwtEncoder encoder, AuthProperties properties, AgrinexCalendar calendar) {
		this.encoder = encoder;
		this.properties = properties;
		this.calendar = calendar;
	}

	public String issue(UUID userId, Role role) {
		Instant issuedAt = this.calendar.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
			.issuer(ISSUER)
			.subject(userId.toString())
			.claim(ROLE_CLAIM, role.name())
			.issuedAt(issuedAt)
			.expiresAt(issuedAt.plus(this.properties.accessTokenTtl()))
			.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
		return this.encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}

	public long lifetimeSeconds() {
		return this.properties.accessTokenTtl().toSeconds();
	}

}
