package com.agrinex.auth.domain;

import java.nio.charset.StandardCharsets;

import com.agrinex.auth.Role;
import com.agrinex.common.error.FieldErrorCode;

/**
 * The password rule of ADR-0016 item 3: FARMER 8 to 64 characters, ADMIN 15 to 64, never more than 72 bytes in
 * UTF-8 and never truncated. No composition rules.
 */
public final class PasswordPolicy {

	static final int FARMER_MIN_LENGTH = 8;

	static final int ADMIN_MIN_LENGTH = 15;

	static final int MAX_LENGTH = 64;

	static final int MAX_UTF8_BYTES = 72;

	private PasswordPolicy() {
	}

	/** The field error code for a password that breaks the rule of the role, or null when it complies. */
	public static FieldErrorCode violation(Role role, String password) {
		int length = password.codePointCount(0, password.length());
		int minimum = (role == Role.ADMIN) ? ADMIN_MIN_LENGTH : FARMER_MIN_LENGTH;
		if (length < minimum) {
			return FieldErrorCode.TOO_SHORT;
		}
		if (length > MAX_LENGTH || password.getBytes(StandardCharsets.UTF_8).length > MAX_UTF8_BYTES) {
			return FieldErrorCode.TOO_LONG;
		}
		return null;
	}

}
