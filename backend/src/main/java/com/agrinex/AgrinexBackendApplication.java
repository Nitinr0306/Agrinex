package com.agrinex;

import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.TimeZone;
import java.util.stream.Stream;

import com.agrinex.common.scheduling.SchedulingConfiguration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The AgriNex backend: one modular-monolith process (spec section 12). Started with
 * {@code --agrinex.operator.command=...}, it instead runs one account operator procedure without a web server
 * or scheduler and exits (ADR-0037, ADR-0041).
 */
@SpringBootApplication
public class AgrinexBackendApplication {

	static final String OPERATOR_COMMAND_ARGUMENT = "--agrinex.operator.command=";

	public static void main(String[] args) {
		useUtcAsDefaultTimeZone();
		if (Arrays.stream(args).anyMatch((arg) -> arg.startsWith(OPERATOR_COMMAND_ARGUMENT))) {
			SpringApplication operator = new SpringApplication(AgrinexBackendApplication.class);
			operator.setWebApplicationType(WebApplicationType.NONE);
			String[] operatorArgs = Stream
				.concat(Arrays.stream(args), Stream.of("--" + SchedulingConfiguration.ENABLED_PROPERTY + "=false"))
				.toArray(String[]::new);
			System.exit(SpringApplication.exit(operator.run(operatorArgs)));
		}
		SpringApplication.run(AgrinexBackendApplication.class, args);
	}

	/**
	 * The process never depends on the host's time zone: "today" is always computed in Asia/Kolkata explicitly
	 * (ADR-0007 item 3) and instants are UTC. A UTC default also keeps the JDBC driver from sending a host-specific
	 * zone alias as the session time zone, which a database server may not know.
	 */
	static void useUtcAsDefaultTimeZone() {
		TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
	}

}
