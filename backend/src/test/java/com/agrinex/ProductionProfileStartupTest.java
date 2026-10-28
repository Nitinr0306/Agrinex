package com.agrinex;

import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Starts the real application with the production profile and SYNTHETIC settings that are missing, unsafe or
 * conflicting, and checks that the start stops while the environment is prepared: no application context is
 * initialized, so no bean, database connection, migration or scheduled job ever exists. Every setting is passed as
 * a command-line argument (empty when it must be missing), which takes precedence over any variable of the machine
 * running the test. No container is needed: nothing gets far enough to connect.
 */
class ProductionProfileStartupTest {

	static final String SECRET = "synthetic-secret-value-0123456789";

	@Test
	void aProductionStartWithoutItsSettingsStopsBeforeTheContextExists() {
		Map<String, String> settings = production();
		List.of("AGRINEX_DOMAIN", "AGRINEX_PUBLIC_ORIGIN", "REDIS_HOST", "REDIS_PORT", "REDIS_PASSWORD", "JWT_SECRET",
				"DATA_GOV_API_KEY", "AGRINEX_EMAIL_PROVIDER", "RESEND_API_KEY", "AGRINEX_EMAIL_FROM")
			.forEach((name) -> settings.put(name, ""));
		List<ApplicationEvent> events = new ArrayList<>();

		assertThatIllegalStateException().isThrownBy(() -> start(events, settings))
			.withMessageStartingWith("AgriNex cannot start: AGRINEX_DOMAIN must be set")
			.withMessageContaining("REDIS_HOST must be set")
			.withMessageContaining("JWT_SECRET must be set")
			.withMessageContaining("DATA_GOV_API_KEY must be set")
			.withMessageContaining("AGRINEX_EMAIL_PROVIDER must be resend")
			.withMessageNotContaining("POSTGRES_PASSWORD must be set")
			.withMessageNotContaining(SECRET);
		assertThat(events).hasAtLeastOneElementOfType(ApplicationStartingEvent.class)
			.noneMatch(ApplicationContextInitializedEvent.class::isInstance);
	}

	@Test
	void unsafeProductionValuesStopTheStartToo() {
		Map<String, String> settings = production();
		settings.put("AGRINEX_PUBLIC_ORIGIN", "http://agrinex.test");
		settings.put("POSTGRES_HOST", "localhost");
		List<ApplicationEvent> events = new ArrayList<>();

		assertThatIllegalStateException().isThrownBy(() -> start(events, settings))
			.withMessage("AgriNex cannot start: AGRINEX_PUBLIC_ORIGIN must be an https:// origin; "
					+ "POSTGRES_HOST must name the database service, not this machine")
			.withMessageNotContaining(SECRET);
		assertThat(events).noneMatch(ApplicationContextInitializedEvent.class::isInstance);
	}

	@Test
	void productionNeverRunsTogetherWithTheLocalProfileOrItsFormerName() {
		for (String profiles : List.of("prod,local", "prod,dev")) {
			Map<String, String> settings = production();
			settings.put("spring.profiles.active", profiles);
			assertThatIllegalStateException().isThrownBy(() -> start(new ArrayList<>(), settings))
				.withMessage("AgriNex cannot start: the profiles local (or dev), prod and test exclude each other: "
						+ "select exactly one");
		}
	}

	@Test
	void aStartWithoutTheLocalOrProductionProfileIsRefused() {
		List<ApplicationEvent> events = new ArrayList<>();
		// "default" overrides a SPRING_PROFILES_ACTIVE of the machine and is neither local, prod nor test.
		assertThatIllegalStateException().isThrownBy(() -> start(events, Map.of("spring.profiles.active", "default")))
			.withMessageStartingWith("AgriNex cannot start: select a profile with SPRING_PROFILES_ACTIVE");
		assertThat(events).noneMatch(ApplicationContextInitializedEvent.class::isInstance);
	}

	/** A complete SYNTHETIC production setting, by variable name. */
	static Map<String, String> production() {
		Map<String, String> settings = new LinkedHashMap<>();
		settings.put("spring.profiles.active", "prod");
		settings.put("AGRINEX_DOMAIN", "agrinex.test");
		settings.put("AGRINEX_PUBLIC_ORIGIN", "https://agrinex.test");
		settings.put("POSTGRES_HOST", "agrinex-db");
		settings.put("POSTGRES_PORT", "5432");
		settings.put("POSTGRES_DB", "agrinex");
		settings.put("POSTGRES_USER", "agrinex");
		settings.put("POSTGRES_PASSWORD", SECRET);
		settings.put("REDIS_HOST", "agrinex-redis");
		settings.put("REDIS_PORT", "6379");
		settings.put("REDIS_PASSWORD", SECRET);
		settings.put("JWT_SECRET", Base64.getEncoder().encodeToString(new byte[32]));
		settings.put("DATA_GOV_API_KEY", SECRET);
		settings.put("AGRINEX_EMAIL_PROVIDER", "resend");
		settings.put("RESEND_API_KEY", SECRET);
		settings.put("AGRINEX_EMAIL_FROM", "noreply@agrinex.test");
		return settings;
	}

	private static void start(List<ApplicationEvent> events, Map<String, String> settings) {
		ApplicationListener<ApplicationEvent> recorder = events::add;
		String[] arguments = settings.entrySet()
			.stream()
			.map((setting) -> "--" + setting.getKey() + "=" + setting.getValue())
			.toArray(String[]::new);
		new SpringApplicationBuilder(AgrinexBackendApplication.class).web(WebApplicationType.NONE)
			.listeners(recorder)
			.run(arguments)
			.close();
	}

}
