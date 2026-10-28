package com.agrinex.auth.service;

import java.time.Instant;

import com.agrinex.auth.config.AuthProperties;
import com.agrinex.auth.domain.Identifier;
import com.agrinex.auth.domain.PasswordPolicy;
import com.agrinex.auth.domain.UserEntity;
import com.agrinex.auth.email.AuthEmailRequested;
import com.agrinex.auth.repository.UserRepository;
import com.agrinex.auth.security.Passwords;
import com.agrinex.auth.security.SecureTokens;
import com.agrinex.common.error.ApiException;
import com.agrinex.common.error.ErrorCode;
import com.agrinex.common.error.FieldErrorCode;
import com.agrinex.common.ratelimit.RateLimitProperties;
import com.agrinex.common.ratelimit.RateLimiter;
import com.agrinex.common.time.AgrinexCalendar;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Password recovery through the account's verified email (ADR-0047 item 4, ADR-0048). A request always succeeds
 * from the caller's point of view; a link is sent only to the verified email of an active account, after the
 * commit. Confirming replaces the BCrypt hash, spends the token, revokes every refresh-token family and sends a
 * "password changed" notice; it never signs the user in.
 */
@Service
@Transactional
public class PasswordResetService {

	private static final String REQUESTS_BUCKET = "password-reset-requests";

	private final UserRepository users;

	private final SecureTokens secureTokens;

	private final Passwords passwords;

	private final SessionService sessions;

	private final RateLimiter limiter;

	private final RateLimitProperties limits;

	private final AuthProperties properties;

	private final ApplicationEventPublisher events;

	private final AgrinexCalendar calendar;

	PasswordResetService(UserRepository users, SecureTokens secureTokens, Passwords passwords,
			SessionService sessions, RateLimiter limiter, RateLimitProperties limits, AuthProperties properties,
			ApplicationEventPublisher events, AgrinexCalendar calendar) {
		this.users = users;
		this.secureTokens = secureTokens;
		this.passwords = passwords;
		this.sessions = sessions;
		this.limiter = limiter;
		this.limits = limits;
		this.properties = properties;
		this.events = events;
		this.calendar = calendar;
	}

	/**
	 * Starts recovery for an identifier. The per-identifier limit (3 per hour) counts unknown identifiers like
	 * known ones; the outcome is otherwise invisible to the caller (ADR-0048 item 5).
	 */
	public void request(String rawIdentifier) {
		Identifier identifier = Identifier.parse(rawIdentifier);
		RateLimiter.Decision decision = this.limiter.hit(REQUESTS_BUCKET, identifier.limitKey(),
				this.limits.passwordResetRequestsPerIdentifier(), this.limits.perIdentifierWindow());
		if (!decision.allowed()) {
			throw ApiException.rateLimited(decision.retryAfterSeconds());
		}
		this.users.findByIdentifier(identifier)
			.filter((user) -> user.isActive() && user.isEmailVerified())
			.ifPresent(this::issue);
	}

	public void confirm(String token, String newPassword) {
		Instant now = this.calendar.now();
		UserEntity user = this.users.findByPasswordResetTokenHash(SecureTokens.hash(token))
			.filter((candidate) -> candidate.isActive() && candidate.getPasswordResetExpiresAt().isAfter(now))
			.orElseThrow(() -> new ApiException(ErrorCode.PASSWORD_RESET_TOKEN_INVALID,
					"The reset link is invalid or has expired."));
		FieldErrorCode violation = PasswordPolicy.violation(user.getRole(), newPassword);
		if (violation != null) {
			throw ApiException.validation("newPassword", violation, "does not meet the password rule of the account");
		}
		user.resetPassword(this.passwords.hash(newPassword));
		this.sessions.revokeAllSessions(user.getId());
		this.events.publishEvent(AuthEmailRequested.passwordChanged(user.getEmail()));
	}

	private void issue(UserEntity user) {
		String token = this.secureTokens.newToken();
		user.issuePasswordResetToken(SecureTokens.hash(token),
				this.calendar.now().plus(this.properties.passwordResetTtl()));
		this.events.publishEvent(AuthEmailRequested.passwordReset(user.getEmail(), token));
	}

}
