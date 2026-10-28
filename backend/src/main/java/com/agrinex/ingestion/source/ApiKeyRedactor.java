package com.agrinex.ingestion.source;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.agrinex.ingestion.config.IngestionProperties;

import org.springframework.stereotype.Component;

/**
 * Removes the configured API key and any {@code api-key} parameter from text before it is stored or logged
 * (ADR-0020 item 5; spec section 47 Q21), for example an error body or an exception message that echoes the
 * request URL.
 */
@Component
public class ApiKeyRedactor {

	static final String MASK = "***";

	private static final Pattern API_KEY_PARAMETER = Pattern.compile("(?i)(api[-_]?key=)[^&\\s\"'<>]+");

	private final String apiKey;

	public ApiKeyRedactor(IngestionProperties properties) {
		this.apiKey = properties.apiKeyConfigured() ? properties.apiKey() : null;
	}

	public String redact(String text) {
		if (text == null) {
			return null;
		}
		String redacted = (this.apiKey != null) ? text.replace(this.apiKey, MASK) : text;
		return API_KEY_PARAMETER.matcher(redacted).replaceAll("$1" + Matcher.quoteReplacement(MASK));
	}

}
