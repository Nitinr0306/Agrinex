package com.agrinex.common.config;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

/**
 * Checks the selected profile and, for {@code prod}, every setting the public deployment needs (ADR-0021, ADR-0055),
 * when the environment is prepared: before the application context exists, so before any bean, connection, migration
 * or scheduled job. Registered in {@code META-INF/spring.factories}, so it runs however the application starts.
 * <ul>
 * <li>Exactly one of {@code local} (or its former name {@code dev}), {@code prod} and {@code test} is active.</li>
 * <li>{@code prod} needs an HTTPS public origin equal to {@code https://} + {@code AGRINEX_DOMAIN} (spec section 28;
 * ADR-0017, ADR-0048 item 2), database and Redis settings that do not point at this machine, their passwords
 * (ADR-0018), a JWT secret of at least 256 bits (ADR-0016), the government API key over HTTPS (ADR-0005), Resend
 * email with a sender address (ADR-0048), and no development CORS origin (Task 12 finding S12-1).</li>
 * </ul>
 * A failure names the variables to fix and never their values. The JWT rule repeats the one the auth module applies
 * when it creates its keys (JwtConfiguration), because this check runs before any module exists.
 */
public class StartupProfileValidator implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

	static final String LOCAL = "local";

	static final String LOCAL_FORMER_NAME = "dev";

	static final String PROD = "prod";

	static final String TEST = "test";

	private static final Set<String> THIS_MACHINE = Set.of("localhost", "127.0.0.1", "0.0.0.0", "::1", "[::1]");

	/** A DNS host name with at least two labels, lower case, without scheme, port or path. */
	private static final Pattern HOST_NAME = Pattern
		.compile("^(?=.{1,253}$)(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z]{2,63}$");

	private static final Pattern JDBC_HOST = Pattern.compile("^jdbc:postgresql://([^:/?]+)");

	private static final Pattern EMAIL_ADDRESS = Pattern.compile("^[^@\\s<>]+@[^@\\s<>]+\\.[^@\\s<>]+$");

	private static final int MIN_JWT_SECRET_BYTES = 32;

	@Override
	public int getOrder() {
		// After Spring Boot has loaded every configuration file and activated the profiles.
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
		List<String> problems = problems(event.getEnvironment());
		if (!problems.isEmpty()) {
			throw new IllegalStateException("AgriNex cannot start: " + String.join("; ", problems));
		}
	}

	static List<String> problems(ConfigurableEnvironment environment) {
		Set<String> active = Set.of(environment.getActiveProfiles());
		boolean prod = active.contains(PROD);
		boolean local = active.contains(LOCAL) || active.contains(LOCAL_FORMER_NAME);
		boolean test = active.contains(TEST);
		int selected = (prod ? 1 : 0) + (local ? 1 : 0) + (test ? 1 : 0);
		if (selected == 0) {
			return List.of("select a profile with SPRING_PROFILES_ACTIVE: local for development or prod for the "
					+ "public deployment");
		}
		if (selected > 1) {
			return List.of("the profiles local (or dev), prod and test exclude each other: select exactly one");
		}
		return prod ? productionProblems(environment) : List.of();
	}

	static List<String> productionProblems(ConfigurableEnvironment environment) {
		List<String> problems = new ArrayList<>();
		checkOrigin(environment, problems);
		checkDatabase(environment, problems);
		checkRedis(environment, problems);
		checkJwtSecret(environment, problems);
		checkGovernmentSource(environment, problems);
		checkEmail(environment, problems);
		List<String> extraOrigins = Binder.get(environment)
			.bind("agrinex.cors.extra-origins", Bindable.listOf(String.class))
			.orElse(List.of());
		if (!extraOrigins.isEmpty()) {
			problems.add("agrinex.cors.extra-origins must be empty in production: the public origin is the only one");
		}
		return problems;
	}

	private static void checkOrigin(Environment environment, List<String> problems) {
		String domain = value(environment, "agrinex.domain");
		String origin = value(environment, "agrinex.public-origin");
		if (domain == null) {
			problems.add("AGRINEX_DOMAIN must be set");
		}
		else if (!HOST_NAME.matcher(domain).matches()) {
			problems.add("AGRINEX_DOMAIN must be a public host name in lower case, without scheme, port or path");
		}
		if (origin == null) {
			problems.add("AGRINEX_PUBLIC_ORIGIN must be set");
		}
		else if (!origin.toLowerCase(Locale.ROOT).startsWith("https://")) {
			problems.add("AGRINEX_PUBLIC_ORIGIN must be an https:// origin");
		}
		else if (domain != null && !origin.equals("https://" + domain)) {
			problems.add("AGRINEX_PUBLIC_ORIGIN must be exactly https:// followed by AGRINEX_DOMAIN");
		}
	}

	private static void checkDatabase(Environment environment, List<String> problems) {
		String url = value(environment, "spring.datasource.url");
		Matcher host = (url != null) ? JDBC_HOST.matcher(url) : null;
		if (host == null || !host.find()) {
			problems.add("POSTGRES_HOST, POSTGRES_PORT and POSTGRES_DB must be set");
		}
		else if (THIS_MACHINE.contains(host.group(1).toLowerCase(Locale.ROOT))) {
			problems.add("POSTGRES_HOST must name the database service, not this machine");
		}
		require(environment, "spring.datasource.username", "POSTGRES_USER", problems);
		require(environment, "spring.datasource.password", "POSTGRES_PASSWORD", problems);
	}

	private static void checkRedis(Environment environment, List<String> problems) {
		String host = value(environment, "spring.data.redis.host");
		if (host == null) {
			problems.add("REDIS_HOST must be set");
		}
		else if (THIS_MACHINE.contains(host.toLowerCase(Locale.ROOT))) {
			problems.add("REDIS_HOST must name the Redis service, not this machine");
		}
		require(environment, "spring.data.redis.port", "REDIS_PORT", problems);
		require(environment, "spring.data.redis.password", "REDIS_PASSWORD", problems);
	}

	private static void checkJwtSecret(Environment environment, List<String> problems) {
		String secret = value(environment, "agrinex.security.jwt-secret");
		if (secret == null) {
			problems.add("JWT_SECRET must be set");
			return;
		}
		try {
			if (Base64.getDecoder().decode(secret).length < MIN_JWT_SECRET_BYTES) {
				problems.add("JWT_SECRET must hold at least 256 random bits");
			}
		}
		catch (IllegalArgumentException notBase64) {
			problems.add("JWT_SECRET must be base64-encoded");
		}
	}

	private static void checkGovernmentSource(Environment environment, List<String> problems) {
		require(environment, "agrinex.ingestion.api-key", "DATA_GOV_API_KEY", problems);
		requireHttps(environment, "agrinex.ingestion.base-url", problems);
	}

	private static void checkEmail(Environment environment, List<String> problems) {
		String provider = value(environment, "agrinex.email.provider");
		if (!"resend".equalsIgnoreCase(provider)) {
			problems.add("AGRINEX_EMAIL_PROVIDER must be resend");
		}
		require(environment, "agrinex.email.resend-api-key", "RESEND_API_KEY", problems);
		String from = value(environment, "agrinex.email.from");
		if (from == null) {
			problems.add("AGRINEX_EMAIL_FROM must be set");
		}
		else if (!EMAIL_ADDRESS.matcher(address(from)).matches()) {
			problems.add("AGRINEX_EMAIL_FROM must be an email address, optionally as \"Name <address>\"");
		}
		requireHttps(environment, "agrinex.email.resend-base-url", problems);
	}

	/** The address of a sender written as {@code address} or {@code Name <address>}. */
	private static String address(String from) {
		int open = from.lastIndexOf('<');
		return (open >= 0 && from.endsWith(">")) ? from.substring(open + 1, from.length() - 1).strip() : from;
	}

	private static void require(Environment environment, String property, String variable, List<String> problems) {
		if (value(environment, property) == null) {
			problems.add(variable + " must be set");
		}
	}

	private static void requireHttps(Environment environment, String property, List<String> problems) {
		String url = value(environment, property);
		if (url == null || !url.toLowerCase(Locale.ROOT).startsWith("https://")) {
			problems.add(property + " must be an https:// URL");
		}
	}

	/**
	 * The property's value, or null when it is missing, blank or refers to an environment variable that is not set
	 * (the prod document of application.yml gives no defaults, so a missing variable cannot be resolved).
	 */
	private static String value(Environment environment, String property) {
		try {
			String value = environment.getProperty(property);
			return (value == null || value.isBlank()) ? null : value.strip();
		}
		catch (IllegalArgumentException unresolvable) {
			return null;
		}
	}

}
