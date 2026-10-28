package com.agrinex.auth.security;

import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.agrinex.auth.config.AuthProperties;
import com.agrinex.auth.config.OriginProperties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.util.StringUtils;

/**
 * HS256 access-token signing and validation with Spring Security's resource-server JWT support (Nimbus; no
 * additional JWT library; ADR-0016 item 4). The key is {@code JWT_SECRET}, base64-encoded, at least 256 bits; a
 * missing or short secret stops the start-up. The secret is never logged or echoed in an error message.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ AuthProperties.class, OriginProperties.class })
class JwtConfiguration {

	/** 256 bits (ADR-0016 item 4; RFC 7518 section 3.2 requires a key at least as long as the hash output). */
	static final int MIN_SECRET_BYTES = 32;

	private static final String HMAC_SHA256 = "HmacSHA256";

	@Bean
	JwtEncoder accessTokenEncoder(AuthProperties properties) {
		return NimbusJwtEncoder.withSecretKey(signingKey(properties.jwtSecret())).algorithm(MacAlgorithm.HS256).build();
	}

	/**
	 * Accepts only HS256 tokens of this issuer that carry {@code sub}, {@code role} and {@code iat}, inside their
	 * validity window with no clock skew: the same backend issues and validates them.
	 */
	@Bean
	JwtDecoder accessTokenDecoder(AuthProperties properties, Clock clock) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(signingKey(properties.jwtSecret()))
			.macAlgorithm(MacAlgorithm.HS256)
			.build();
		JwtTimestampValidator timestamps = new JwtTimestampValidator(Duration.ZERO);
		timestamps.setClock(clock);
		timestamps.setAllowEmptyExpiryClaim(false);
		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(timestamps,
				new JwtIssuerValidator(AccessTokenIssuer.ISSUER),
				new JwtClaimValidator<Object>(JwtClaimNames.SUB, Objects::nonNull),
				new JwtClaimValidator<Object>(JwtClaimNames.IAT, Objects::nonNull),
				new JwtClaimValidator<Object>(AccessTokenIssuer.ROLE_CLAIM, Objects::nonNull)));
		return decoder;
	}

	static SecretKey signingKey(String secret) {
		if (!StringUtils.hasText(secret)) {
			throw new IllegalStateException("JWT_SECRET is not set");
		}
		byte[] key;
		try {
			key = Base64.getDecoder().decode(secret.strip());
		}
		catch (IllegalArgumentException ex) {
			throw new IllegalStateException("JWT_SECRET must be base64-encoded");
		}
		if (key.length < MIN_SECRET_BYTES) {
			throw new IllegalStateException("JWT_SECRET must hold at least 256 random bits");
		}
		return new SecretKeySpec(key, HMAC_SHA256);
	}

}
