package com.agrinex.auth.security;

import java.util.UUID;

import com.agrinex.auth.config.AuthProperties;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * BCrypt password hashing with cost 12 (spec section 28; ADR-0016 item 3). An unknown identifier still costs one
 * BCrypt verification against a dummy hash, so sign-in timing does not reveal whether an account exists.
 */
@Component
public class Passwords {

	private final BCryptPasswordEncoder encoder;

	private final String dummyHash;

	Passwords(AuthProperties properties) {
		this.encoder = new BCryptPasswordEncoder(properties.bcryptStrength());
		this.dummyHash = this.encoder.encode(UUID.randomUUID().toString());
	}

	public String hash(String rawPassword) {
		return this.encoder.encode(rawPassword);
	}

	public boolean matches(String rawPassword, String passwordHash) {
		return this.encoder.matches(rawPassword, passwordHash);
	}

	/** Spends the same work as a real verification and always fails. */
	public void verifyAgainstDummy(String rawPassword) {
		this.encoder.matches(rawPassword, this.dummyHash);
	}

}
