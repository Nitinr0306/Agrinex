package com.agrinex.common.config;

import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The profile and production rules of StartupProfileValidator, on SYNTHETIC values. A complete production setting
 * passes; each missing, unsafe or inconsistent value is named, and no message ever contains a value.
 */
class StartupProfileValidatorTest {

	static final String SECRET = "synthetic-secret-value-0123456789";

	static final String JWT = Base64.getEncoder().encodeToString(new byte[32]);

	@Test
	void exactlyOneProfileMustBeSelected() {
		assertThat(StartupProfileValidator.problems(withProfiles())).singleElement()
			.asString()
			.startsWith("select a profile with SPRING_PROFILES_ACTIVE");
		assertThat(StartupProfileValidator.problems(withProfiles("local"))).isEmpty();
		assertThat(StartupProfileValidator.problems(withProfiles("dev"))).isEmpty();
		assertThat(StartupProfileValidator.problems(withProfiles("test"))).isEmpty();
		for (String[] pair : new String[][] { { "prod", "local" }, { "prod", "dev" }, { "prod", "test" },
				{ "local", "test" } }) {
			assertThat(StartupProfileValidator.problems(withProfiles(pair))).singleElement()
				.asString()
				.contains("exclude each other");
		}
	}

	@Test
	void aCompleteProductionSettingPasses() {
		assertThat(StartupProfileValidator.problems(production())).isEmpty();
		MockEnvironment named = production().withProperty("agrinex.email.from", "AgriNex <noreply@agrinex.test>");
		assertThat(StartupProfileValidator.problems(named)).isEmpty();
	}

	@Test
	void everyMissingProductionValueIsNamedWithoutAnyValue() {
		MockEnvironment empty = withProfiles("prod");
		List<String> problems = StartupProfileValidator.problems(empty);
		assertThat(problems).containsExactly("AGRINEX_DOMAIN must be set", "AGRINEX_PUBLIC_ORIGIN must be set",
				"POSTGRES_HOST, POSTGRES_PORT and POSTGRES_DB must be set", "POSTGRES_USER must be set",
				"POSTGRES_PASSWORD must be set", "REDIS_HOST must be set", "REDIS_PORT must be set",
				"REDIS_PASSWORD must be set", "JWT_SECRET must be set", "DATA_GOV_API_KEY must be set",
				"agrinex.ingestion.base-url must be an https:// URL", "AGRINEX_EMAIL_PROVIDER must be resend",
				"RESEND_API_KEY must be set", "AGRINEX_EMAIL_FROM must be set",
				"agrinex.email.resend-base-url must be an https:// URL");
	}

	@Test
	void anUnresolvedEnvironmentVariableCountsAsMissing() {
		MockEnvironment unresolved = production().withProperty("spring.datasource.password", "${POSTGRES_PASSWORD}")
			.withProperty("spring.data.redis.host", "${REDIS_HOST}");
		assertThat(StartupProfileValidator.problems(unresolved)).containsExactly("POSTGRES_PASSWORD must be set",
				"REDIS_HOST must be set");
	}

	@Test
	void unsafeOrInconsistentProductionValuesAreRefused() {
		MockEnvironment unsafe = production().withProperty("agrinex.public-origin", "http://agrinex.test")
			.withProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/agrinex")
			.withProperty("spring.data.redis.host", "127.0.0.1")
			.withProperty("agrinex.security.jwt-secret", Base64.getEncoder().encodeToString(new byte[16]))
			.withProperty("agrinex.email.provider", "none")
			.withProperty("agrinex.email.from", "not-an-address")
			.withProperty("agrinex.cors.extra-origins[0]", "http://localhost:3000");

		assertThat(StartupProfileValidator.problems(unsafe)).containsExactly(
				"AGRINEX_PUBLIC_ORIGIN must be an https:// origin",
				"POSTGRES_HOST must name the database service, not this machine",
				"REDIS_HOST must name the Redis service, not this machine",
				"JWT_SECRET must hold at least 256 random bits", "AGRINEX_EMAIL_PROVIDER must be resend",
				"AGRINEX_EMAIL_FROM must be an email address, optionally as \"Name <address>\"",
				"agrinex.cors.extra-origins must be empty in production: the public origin is the only one");
	}

	@Test
	void thePublicOriginMustBeExactlyTheDomainOverHttps() {
		assertThat(StartupProfileValidator
			.problems(production().withProperty("agrinex.public-origin", "https://other.agrinex.test")))
			.containsExactly("AGRINEX_PUBLIC_ORIGIN must be exactly https:// followed by AGRINEX_DOMAIN");
		assertThat(StartupProfileValidator.problems(production().withProperty("agrinex.domain", "localhost")))
			.contains("AGRINEX_DOMAIN must be a public host name in lower case, without scheme, port or path");
		assertThat(StartupProfileValidator.problems(production().withProperty("agrinex.security.jwt-secret", "%%%")))
			.containsExactly("JWT_SECRET must be base64-encoded");
	}

	@Test
	void noMessageContainsASecretValue() {
		MockEnvironment broken = production().withProperty("agrinex.public-origin", "http://" + SECRET)
			.withProperty("agrinex.security.jwt-secret", SECRET)
			.withProperty("agrinex.email.from", SECRET);
		assertThat(String.join("; ", StartupProfileValidator.problems(broken))).doesNotContain(SECRET);
	}

	private static MockEnvironment withProfiles(String... profiles) {
		MockEnvironment environment = new MockEnvironment();
		environment.setActiveProfiles(profiles);
		return environment;
	}

	/** A complete SYNTHETIC production setting. */
	private static MockEnvironment production() {
		return withProfiles("prod").withProperty("agrinex.domain", "agrinex.test")
			.withProperty("agrinex.public-origin", "https://agrinex.test")
			.withProperty("spring.datasource.url", "jdbc:postgresql://agrinex-db:5432/agrinex")
			.withProperty("spring.datasource.username", "agrinex")
			.withProperty("spring.datasource.password", SECRET)
			.withProperty("spring.data.redis.host", "agrinex-redis")
			.withProperty("spring.data.redis.port", "6379")
			.withProperty("spring.data.redis.password", SECRET)
			.withProperty("agrinex.security.jwt-secret", JWT)
			.withProperty("agrinex.ingestion.api-key", SECRET)
			.withProperty("agrinex.ingestion.base-url", "https://api.data.gov.in/resource/")
			.withProperty("agrinex.email.provider", "resend")
			.withProperty("agrinex.email.resend-api-key", SECRET)
			.withProperty("agrinex.email.from", "noreply@agrinex.test")
			.withProperty("agrinex.email.resend-base-url", "https://api.resend.com");
	}

}
