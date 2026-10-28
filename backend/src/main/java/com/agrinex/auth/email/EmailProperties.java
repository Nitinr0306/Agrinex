package com.agrinex.auth.email;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Account-email delivery settings (ADR-0048 item 8). {@code provider} is {@code none} (non-delivering adapter,
 * local development and tests) or {@code resend}. The API key comes only from {@code RESEND_API_KEY} and is
 * never logged, returned or stored.
 */
@ConfigurationProperties("agrinex.email")
public record EmailProperties(Provider provider, String resendApiKey, String from, String resendBaseUrl,
		Duration timeout) {

	public enum Provider {

		NONE, RESEND

	}

}
