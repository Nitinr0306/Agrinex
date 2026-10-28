package com.agrinex.common.ratelimit;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Rate-limit values. They are deployment configuration; the defaults in application.yml are the decided
 * values (ADR-0017 item 6, ADR-0048 item 6).
 */
@ConfigurationProperties("agrinex.rate-limits")
public record RateLimitProperties(
		int sellingOptionsPerMinute,
		int loginPerMinute,
		int registerPerMinute,
		int refreshPerMinute,
		int failedLoginsPerIdentifier,
		Duration failedLoginWindow,
		int passwordResetRequestPerMinute,
		int passwordResetConfirmPerMinute,
		int emailVerificationConfirmPerMinute,
		int emailVerificationResendPerMinute,
		int passwordResetRequestsPerIdentifier,
		int verificationResendsPerAccount,
		Duration perIdentifierWindow) {
}
