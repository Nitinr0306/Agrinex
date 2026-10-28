package com.agrinex.common;

import java.util.regex.Pattern;

/**
 * The AgriNex normalization rule for text used in matching: every run of whitespace becomes one space and
 * both ends are trimmed. Case, punctuation and parentheses are never changed, and no fuzzy matching is applied
 * (DATABASE_ARCHITECTURE.md section 9.2). The database enforces the same rule with CHECK constraints of the
 * form {@code x = btrim(regexp_replace(x, '\s+', ' ', 'g'))}.
 * <p>
 * Unicode whitespace is collapsed as well, so the result only ever contains single ASCII spaces and always
 * satisfies those constraints, whatever the database's character classification.
 */
public final class TextNormalizer {

	private static final Pattern WHITESPACE_RUN = Pattern.compile("\\s+", Pattern.UNICODE_CHARACTER_CLASS);

	private TextNormalizer() {
	}

	/** Returns the normalized value, or {@code null} when the input is {@code null} or normalizes to empty. */
	public static String normalize(String value) {
		if (value == null) {
			return null;
		}
		String collapsed = WHITESPACE_RUN.matcher(value).replaceAll(" ").strip();
		return collapsed.isEmpty() ? null : collapsed;
	}

}
