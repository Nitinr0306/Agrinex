package com.agrinex.common.ratelimit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applies a per-client-IP limit of fixed one-minute windows to a controller operation (ADR-0017 item 6). The
 * request is rejected with 429 RATE_LIMITED before the controller runs, so an over-limit refresh never
 * processes its token (ADR-0042, SR-14).
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimited {

	Bucket value();

	enum Bucket {

		/** Compare and simulate share one bucket (spec section 28). */
		SELLING_OPTIONS,
		LOGIN,
		REGISTER,
		REFRESH,
		PASSWORD_RESET_REQUEST,
		PASSWORD_RESET_CONFIRM,
		EMAIL_VERIFICATION_CONFIRM,
		EMAIL_VERIFICATION_RESEND;

		int limit(RateLimitProperties properties) {
			return switch (this) {
				case SELLING_OPTIONS -> properties.sellingOptionsPerMinute();
				case LOGIN -> properties.loginPerMinute();
				case REGISTER -> properties.registerPerMinute();
				case REFRESH -> properties.refreshPerMinute();
				case PASSWORD_RESET_REQUEST -> properties.passwordResetRequestPerMinute();
				case PASSWORD_RESET_CONFIRM -> properties.passwordResetConfirmPerMinute();
				case EMAIL_VERIFICATION_CONFIRM -> properties.emailVerificationConfirmPerMinute();
				case EMAIL_VERIFICATION_RESEND -> properties.emailVerificationResendPerMinute();
			};
		}

	}

}
