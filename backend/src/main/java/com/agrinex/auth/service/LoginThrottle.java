package com.agrinex.auth.service;

import com.agrinex.auth.domain.Identifier;
import com.agrinex.common.error.ApiException;
import com.agrinex.common.ratelimit.RateLimitProperties;
import com.agrinex.common.ratelimit.RateLimiter;

import org.springframework.stereotype.Component;

/**
 * The failed-login limit: 10 failures per normalized identifier per 15 minutes (ADR-0017 item 6). Unknown
 * identifiers count exactly like known ones, and the phone number and the email of one account are separate
 * identifiers (ADR-0048 item 5). While an identifier is over the limit every login for it answers 429.
 */
@Component
class LoginThrottle {

	private static final String BUCKET = "login-failures";

	private final RateLimiter limiter;

	private final RateLimitProperties limits;

	LoginThrottle(RateLimiter limiter, RateLimitProperties limits) {
		this.limiter = limiter;
		this.limits = limits;
	}

	void requireNotLocked(Identifier identifier) {
		RateLimiter.Decision decision = this.limiter.check(BUCKET, identifier.limitKey(),
				this.limits.failedLoginsPerIdentifier(), this.limits.failedLoginWindow());
		if (!decision.allowed()) {
			throw ApiException.rateLimited(decision.retryAfterSeconds());
		}
	}

	void recordFailure(Identifier identifier) {
		this.limiter.hit(BUCKET, identifier.limitKey(), this.limits.failedLoginsPerIdentifier(),
				this.limits.failedLoginWindow());
	}

}
