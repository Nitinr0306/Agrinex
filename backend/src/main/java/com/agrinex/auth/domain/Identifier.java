package com.agrinex.auth.domain;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * A normalized sign-in or recovery identifier (ADR-0016 item 2, ADR-0047 item 3): an Indian mobile number in
 * E.164 form, or an email address in lower case, after trimming surrounding white space. Anything else names no
 * account. {@link #limitKey()} keeps the phone number and the email of one account apart, so no rate limit links
 * them (ADR-0048 item 5).
 */
public record Identifier(Kind kind, String value) {

	private static final Pattern PHONE = Pattern.compile(AccountRules.PHONE_PATTERN);

	public static Identifier parse(String raw) {
		String trimmed = raw.strip();
		if (PHONE.matcher(trimmed).matches()) {
			return new Identifier(Kind.PHONE, trimmed);
		}
		if (trimmed.indexOf('@') > 0) {
			return new Identifier(Kind.EMAIL, trimmed.toLowerCase(Locale.ROOT));
		}
		return new Identifier(Kind.UNRECOGNIZED, trimmed);
	}

	public String limitKey() {
		return this.kind.name() + ":" + this.value;
	}

	@Override
	public String toString() {
		return "Identifier[kind=" + this.kind + "]";
	}

	public enum Kind {

		PHONE, EMAIL, UNRECOGNIZED

	}

}
