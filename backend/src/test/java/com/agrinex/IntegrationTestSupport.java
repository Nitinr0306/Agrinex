package com.agrinex;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * One application context for every integration test: PostgreSQL and Redis at the baseline versions
 * (TestcontainersConfiguration), MockMvc, a local government-source stub, and captured account emails. The JWT secret and the bootstrap
 * admin password are random per run, so no credential is stored for tests. Schedules are off, so ingestion runs
 * only when a test triggers one. Tests share the database and must use their own synthetic rows. The profile is
 * "test" (StartupProfileValidator): neither the local defaults nor the production requirements apply.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({ TestcontainersConfiguration.class, CapturedEmails.class })
public abstract class IntegrationTestSupport {

	private static final SecureRandom RANDOM = new SecureRandom();

	protected static final String PUBLIC_ORIGIN = "https://agrinex.test";

	protected static final String ADMIN_EMAIL = "test-admin@agrinex.invalid";

	protected static final String ADMIN_PASSWORD = randomBase64(24);

	protected static final String SYNTHETIC_API_KEY = "synthetic-test-key-" + HexFormat.of().formatHex(randomBytes(8));

	protected static final GovernmentSourceStub SOURCE = GovernmentSourceStub.start();

	/** Request limits high enough that one test run never reaches them; the limiter itself is tested apart. */
	private static final List<String> RAISED_RATE_LIMITS = List.of("selling-options-per-minute", "login-per-minute",
			"register-per-minute", "refresh-per-minute", "password-reset-request-per-minute",
			"password-reset-confirm-per-minute", "email-verification-confirm-per-minute",
			"email-verification-resend-per-minute");

	@Autowired
	protected MockMvc mvc;

	@Autowired
	protected JdbcTemplate jdbc;

	@Autowired
	protected CapturedEmails emails;

	@DynamicPropertySource
	static void testProperties(DynamicPropertyRegistry registry) {
		String jwtSecret = randomBase64(32);
		registry.add("agrinex.security.jwt-secret", () -> jwtSecret);
		registry.add("agrinex.public-origin", () -> PUBLIC_ORIGIN);
		registry.add("agrinex.bootstrap-admin.email", () -> ADMIN_EMAIL);
		registry.add("agrinex.bootstrap-admin.password", () -> ADMIN_PASSWORD);
		registry.add("agrinex.scheduling.enabled", () -> "false");
		registry.add("agrinex.ingestion.api-key", () -> SYNTHETIC_API_KEY);
		registry.add("agrinex.ingestion.base-url", SOURCE::baseUrl);
		registry.add("agrinex.ingestion.initial-backoff", () -> "10ms");
		// Small pages, so every multi-record test also exercises offset paging (the source's default is 500).
		registry.add("agrinex.ingestion.page-size", () -> "5");
		RAISED_RATE_LIMITS.forEach((limit) -> registry.add("agrinex.rate-limits." + limit, () -> "10000"));
	}

	/** Logs in and returns the access token. */
	protected String login(String identifier, String password) throws Exception {
		return JsonPath.read(loginResponse(identifier, password).getContentAsString(), "$.accessToken");
	}

	protected MockHttpServletResponse loginResponse(String identifier, String password) throws Exception {
		MockHttpServletResponse response = this.mvc
			.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"identifier\": \"%s\", \"password\": \"%s\"}".formatted(identifier, password)))
			.andReturn()
			.getResponse();
		assertThat(response.getStatus()).as("login status").isEqualTo(200);
		return response;
	}

	protected String adminToken() throws Exception {
		return login(ADMIN_EMAIL, ADMIN_PASSWORD);
	}

	protected static String bearer(String token) {
		return "Bearer " + token;
	}

	/** A unique synthetic Indian mobile number in E.164 form. */
	protected static String syntheticPhone() {
		return "+919" + String.format("%09d", RANDOM.nextInt(1_000_000_000));
	}

	protected static String randomBase64(int bytes) {
		return Base64.getEncoder().encodeToString(randomBytes(bytes));
	}

	private static byte[] randomBytes(int count) {
		byte[] bytes = new byte[count];
		RANDOM.nextBytes(bytes);
		return bytes;
	}

}
