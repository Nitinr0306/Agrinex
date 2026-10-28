package com.agrinex.auth.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.stereotype.Component;

/**
 * Opaque single-use tokens: 256 bits from a cryptographically secure generator, encoded base64url without padding
 * (43 characters). Only the lowercase hex SHA-256 of a token is ever stored (spec section 28; ADR-0016 item 5,
 * ADR-0048 item 1).
 */
@Component
public class SecureTokens {

	static final int TOKEN_BYTES = 32;

	private final SecureRandom random = new SecureRandom();

	public String newToken() {
		byte[] bytes = new byte[TOKEN_BYTES];
		this.random.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	public static String hash(String token) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		}
		catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 is required by the Java platform", ex);
		}
	}

}
