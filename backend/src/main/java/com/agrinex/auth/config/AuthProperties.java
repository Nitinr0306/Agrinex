package com.agrinex.auth.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Authentication settings (spec section 28; ADR-0016, ADR-0048). The JWT secret comes only from the environment
 * ({@code JWT_SECRET}) and must hold at least 256 random bits.
 */
@ConfigurationProperties("agrinex.security")
public record AuthProperties(String jwtSecret, Duration accessTokenTtl, Duration refreshTokenTtl, int bcryptStrength,
		Duration emailVerificationTtl, Duration passwordResetTtl) {
}
