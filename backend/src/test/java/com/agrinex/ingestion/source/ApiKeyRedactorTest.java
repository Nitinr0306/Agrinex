package com.agrinex.ingestion.source;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.agrinex.ingestion.config.IngestionProperties;

import static org.assertj.core.api.Assertions.assertThat;

/** No stored or logged text contains the API key (ADR-0020 item 5; spec section 47 Q21). */
class ApiKeyRedactorTest {

	static final String SYNTHETIC_KEY = "synthetic-test-key-0000";

	final ApiKeyRedactor redactor = new ApiKeyRedactor(new IngestionProperties(SYNTHETIC_KEY, "http://localhost/",
			List.of("Maharashtra"), "0 0 0 * * *", 3, 500, 0, Duration.ZERO, Duration.ofSeconds(1),
			Duration.ofSeconds(1), 10));

	@Test
	void theConfiguredKeyIsMaskedAnywhere() {
		assertThat(this.redactor.redact("error near " + SYNTHETIC_KEY + " in body")).isEqualTo("error near *** in body")
			.doesNotContain(SYNTHETIC_KEY);
	}

	@Test
	void anyApiKeyParameterIsMasked() {
		assertThat(this.redactor.redact("GET /resource/x?api-key=other-secret&format=json"))
			.isEqualTo("GET /resource/x?api-key=***&format=json");
		assertThat(this.redactor.redact("\"url\": \"https://api.data.gov.in/r?API_KEY=abc\""))
			.isEqualTo("\"url\": \"https://api.data.gov.in/r?API_KEY=***\"");
	}

	@Test
	void nullStaysNull() {
		assertThat(this.redactor.redact(null)).isNull();
	}

}
