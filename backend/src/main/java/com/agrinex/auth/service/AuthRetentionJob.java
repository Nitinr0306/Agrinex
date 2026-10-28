package com.agrinex.auth.service;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agrinex.auth.repository.RefreshTokenRepository;
import com.agrinex.auth.repository.UserRepository;
import com.agrinex.common.time.AgrinexCalendar;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * The auth module's daily purge (ADR-0029 item 1, ADR-0048 item 11): expired verification and reset links are
 * cleared, and refresh tokens are deleted one day after they expire. Revoked tokens stay until then, so reuse
 * of a revoked token is still detected.
 */
@Component
class AuthRetentionJob {

	private static final Logger log = LoggerFactory.getLogger(AuthRetentionJob.class);

	/** ADR-0029 item 1: refresh tokens are deleted one day after expiry. */
	static final Duration REFRESH_TOKEN_RETENTION_AFTER_EXPIRY = Duration.ofDays(1);

	private final UserRepository users;

	private final RefreshTokenRepository tokens;

	private final AgrinexCalendar calendar;

	AuthRetentionJob(UserRepository users, RefreshTokenRepository tokens, AgrinexCalendar calendar) {
		this.users = users;
		this.tokens = tokens;
		this.calendar = calendar;
	}

	@Scheduled(cron = "${agrinex.retention.purge-cron}", zone = AgrinexCalendar.ZONE_ID)
	@Transactional
	public void purge() {
		Instant now = this.calendar.now();
		int verificationLinks = this.users.clearExpiredVerificationTokens(now);
		int resetLinks = this.users.clearExpiredResetTokens(now);
		int refreshTokens = this.tokens.deleteExpiredBefore(now.minus(REFRESH_TOKEN_RETENTION_AFTER_EXPIRY));
		log.atInfo()
			.addKeyValue("expiredVerificationLinks", verificationLinks)
			.addKeyValue("expiredResetLinks", resetLinks)
			.addKeyValue("refreshTokensDeleted", refreshTokens)
			.log("Auth retention purge completed");
	}

}
