package com.agrinex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * application.yml is the only configuration file: a shared document followed by one document per profile
 * (spring.config.activate.on-profile). Prepares the real environment for each profile and stops before the
 * application context exists, so no container is needed, and checks that each profile receives its own document and
 * never the other one.
 */
class ProfileConfigurationTest {

	private static final String EXTRA_ORIGINS = "agrinex.cors.extra-origins";

	@Test
	void theLocalProfileReceivesTheDevelopmentDefaults() {
		ConfigurableEnvironment environment = prepare(Map.of("spring.profiles.active", "local"));

		assertThat(extraOrigins(environment)).containsExactly("http://localhost:3000");
		assertThat(environment.getProperty("spring.datasource.url")).startsWith("jdbc:postgresql://");
		assertThat(environment.containsProperty("agrinex.domain")).isFalse();
		// Shared values stay in place.
		assertThat(environment.getProperty("spring.application.name")).isEqualTo("agrinex-backend");
	}

	@Test
	void theFormerNameDevStillActivatesTheLocalDocument() {
		ConfigurableEnvironment environment = prepare(Map.of("spring.profiles.active", "dev"));

		assertThat(environment.getActiveProfiles()).contains("dev", "local");
		assertThat(extraOrigins(environment)).containsExactly("http://localhost:3000");
	}

	@Test
	void theProductionProfileReceivesOnlyTheProductionDocument() {
		ConfigurableEnvironment environment = prepare(ProductionProfileStartupTest.production());

		assertThat(extraOrigins(environment)).isEmpty();
		assertThat(environment.getProperty("agrinex.domain")).isEqualTo("agrinex.test");
		assertThat(environment.getProperty("agrinex.public-origin")).isEqualTo("https://agrinex.test");
		assertThat(environment.getProperty("spring.datasource.url"))
			.isEqualTo("jdbc:postgresql://agrinex-db:5432/agrinex");
		assertThat(environment.getProperty("spring.application.name")).isEqualTo("agrinex-backend");
	}

	/** The environment Spring Boot prepares for these command-line settings; the start stops right after. */
	private static ConfigurableEnvironment prepare(Map<String, String> settings) {
		List<ConfigurableEnvironment> prepared = new ArrayList<>();
		ApplicationListener<ApplicationEnvironmentPreparedEvent> capture = (event) -> {
			prepared.add(event.getEnvironment());
			throw new EnvironmentCaptured();
		};
		String[] arguments = settings.entrySet()
			.stream()
			.map((setting) -> "--" + setting.getKey() + "=" + setting.getValue())
			.toArray(String[]::new);
		assertThatExceptionOfType(EnvironmentCaptured.class)
			.isThrownBy(() -> new SpringApplicationBuilder(AgrinexBackendApplication.class).web(WebApplicationType.NONE)
				.listeners(capture)
				.run(arguments));
		return prepared.get(0);
	}

	private static List<String> extraOrigins(ConfigurableEnvironment environment) {
		return Binder.get(environment).bind(EXTRA_ORIGINS, Bindable.listOf(String.class)).orElse(List.of());
	}

	private static final class EnvironmentCaptured extends RuntimeException {

	}

}
