package com.agrinex.auth.domain;

import java.util.regex.Pattern;

/**
 * Identifier and profile rules of the account model (ADR-0016 item 1, ADR-0047 item 2; contract RegisterRequest),
 * shared by the request validation and the operator procedures. The email pattern matches the database check
 * {@code ck_users_email_format}; {@code (?U)} makes {@code \s} cover Unicode white space, as JSON Schema's does.
 */
public final class AccountRules {

	public static final String PHONE_PATTERN = "^\\+91[6-9][0-9]{9}$";

	public static final String EMAIL_PATTERN = "(?U)^[^@\\s]+@[^@\\s]+$";

	public static final int EMAIL_MIN_LENGTH = 3;

	public static final int EMAIL_MAX_LENGTH = 254;

	public static final int DISPLAY_NAME_MAX_LENGTH = 100;

	public static final int IDENTIFIER_MAX_LENGTH = 254;

	/** Base64url without padding of 256 random bits (ADR-0048 item 1). */
	public static final String LINK_TOKEN_PATTERN = "^[A-Za-z0-9_-]{43}$";

	private static final Pattern EMAIL = Pattern.compile(EMAIL_PATTERN);

	private AccountRules() {
	}

	public static boolean isValidEmail(String email) {
		int length = email.codePointCount(0, email.length());
		return length >= EMAIL_MIN_LENGTH && length <= EMAIL_MAX_LENGTH && EMAIL.matcher(email).matches();
	}

}
