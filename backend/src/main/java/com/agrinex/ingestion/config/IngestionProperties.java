package com.agrinex.ingestion.config;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Ingestion deployment configuration (ADR-0020 item 3: changed only by redeployment). The API key comes only from
 * {@code DATA_GOV_API_KEY}; it is never logged, stored or returned, and admins see only whether it is set.
 *
 * @param apiKey the data.gov.in API key, blank when not configured
 * @param baseUrl the data.gov.in resource endpoint
 * @param coverageStates source state values ingested (ADR-0006 item 1)
 * @param scheduleCron the run schedule in Asia/Kolkata (ADR-0011 item 1)
 * @param historicalWindowDaysBack scheduled historical runs cover today minus this many days to today
 * @param pageSize records requested per page (spec section 15: 500)
 * @param maxRetries retries per page after the first attempt (spec section 22: 3)
 * @param initialBackoff the first retry delay, doubled for each later retry
 * @param connectTimeout connection timeout of a page request
 * @param readTimeout read timeout of a page request
 * @param maxPagesPerUnit guard against a source that never ends a delivery unit (evidence file section 0.5)
 */
@ConfigurationProperties("agrinex.ingestion")
public record IngestionProperties(String apiKey, String baseUrl, List<String> coverageStates, String scheduleCron,
		int historicalWindowDaysBack, int pageSize, int maxRetries, Duration initialBackoff, Duration connectTimeout,
		Duration readTimeout, int maxPagesPerUnit) {

	public boolean apiKeyConfigured() {
		return this.apiKey != null && !this.apiKey.isBlank();
	}

}
