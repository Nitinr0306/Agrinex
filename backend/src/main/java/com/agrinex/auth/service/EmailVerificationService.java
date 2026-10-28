package com.agrinex.auth.service;

import java.time.Instant;
import java.util.UUID;

import com.agrinex.auth.config.AuthProperties;
import com.agrinex.auth.domain.UserEntity;
import com.agrinex.auth.email.AuthEmailRequested;
import com.agrinex.auth.repository.UserRepository;
import com.agrinex.auth.security.SecureTokens;
import com.agrinex.common.error.ApiException;
import com.agrinex.common.error.ErrorCode;
import com.agrinex.common.ratelimit.RateLimitProperties;
import com.agrinex.common.ratelimit.RateLimiter;
import com.agrinex.common.time.AgrinexCalendar;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Email verification by single-use link (ADR-0048 items 1 to 4): a 256-bit token valid for 24 hours, stored only
 * as its SHA-256 hash; a new link replaces the previous one, and confirming spends it. An unknown, expired,
 * spent or replaced token, or one of an inactive account, answers 422 EMAIL_VERIFICATION_TOKEN_INVALID.
 */
@Service
@Transactional
public class EmailVerificationService {

	private static final String RESENDS_BUCKET = "verification-resends";

	private final UserRepository users;

	private final SecureTokens secureTokens;

	private final RateLimiter limiter;

	private final RateLimitProperties limits;

	private final AuthProperties properties;

	private final ApplicationEventPublisher events;

	private final AgrinexCalendar calendar;

	EmailVerificationService(UserRepository users, SecureTokens secureTokens, RateLimiter limiter,
			RateLimitProperties limits, AuthProperties properties, ApplicationEventPublisher events,
			AgrinexCalendar calendar) {
		this.users = users;
		this.secureTokens = secureTokens;
		this.limiter = limiter;
		this.limits = limits;
		this.properties = properties;
		this.events = events;
		this.calendar = calendar;
	}

	public void confirm(String token) {
		Instant now = this.calendar.now();
		UserEntity user = this.users.findByEmailVerificationTokenHash(SecureTokens.hash(token))
			.filter((candidate) -> candidate.isActive() && candidate.getEmailVerificationExpiresAt().isAfter(now))
			.orElseThrow(() -> new ApiException(ErrorCode.EMAIL_VERIFICATION_TOKEN_INVALID,
					"The verification link is invalid or has expired."));
		user.verifyEmail(now);
	}

	/**
	 * Sends a new link to the caller's own email while it is unverified (3 requests per account per hour,
	 * ADR-0048 item 6). Nothing is sent when the email is already verified.
	 */
	public void resend(UUID userId) {
		RateLimiter.Decision decision = this.limiter.hit(RESENDS_BUCKET, userId.toString(),
				this.limits.verificationResendsPerAccount(), this.limits.perIdentifierWindow());
		if (!decision.allowed()) {
			throw ApiException.rateLimited(decision.retryAfterSeconds());
		}
		this.users.findForUpdate(userId)
			.filter((user) -> user.isActive() && !user.isEmailVerified())
			.ifPresent(this::issue);
	}

	/** Issues a link for an unverified email and requests its delivery after the commit. */
	void issue(UserEntity user) {
		String token = this.secureTokens.newToken();
		user.issueEmailVerificationToken(SecureTokens.hash(token),
				this.calendar.now().plus(this.properties.emailVerificationTtl()));
		this.events.publishEvent(AuthEmailRequested.verification(user.getEmail(), token));
	}

}
