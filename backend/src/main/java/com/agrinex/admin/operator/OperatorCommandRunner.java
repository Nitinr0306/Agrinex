package com.agrinex.admin.operator;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agrinex.admin.service.AccountProcedureService;
import com.agrinex.auth.OperatorRefusalException;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Runs one account operator procedure and ends the process (ADR-0037 item 2, ADR-0041 item 1): there is no HTTP
 * operation for them. The process exits 0 when the procedure completed, 2 when it was refused (nothing changed,
 * no audit row) and 64 when the arguments are incomplete. Personal data is never logged.
 */
@Component
@ConditionalOnProperty(name = OperatorCommandRunner.COMMAND_PROPERTY)
@EnableConfigurationProperties(OperatorCommandProperties.class)
class OperatorCommandRunner implements ApplicationRunner, ExitCodeGenerator {

	static final String COMMAND_PROPERTY = "agrinex.operator.command";

	static final int EXIT_DONE = 0;

	static final int EXIT_FAILED = 1;

	static final int EXIT_REFUSED = 2;

	/** sysexits.h EX_USAGE. */
	static final int EXIT_USAGE = 64;

	private static final Logger log = LoggerFactory.getLogger(OperatorCommandRunner.class);

	private final OperatorCommandProperties arguments;

	private final AccountProcedureService procedures;

	private int exitCode = EXIT_FAILED;

	OperatorCommandRunner(OperatorCommandProperties arguments, AccountProcedureService procedures) {
		this.arguments = arguments;
		this.procedures = procedures;
	}

	@Override
	public void run(ApplicationArguments args) {
		Map<String, Supplier<Object>> commands = Map.of(
				"erase-farmer", () -> this.procedures.eraseFarmer(require("actor", this.arguments.actor()),
						require("phone", this.arguments.phone()), require("reference", this.arguments.reference()),
						require("reason", this.arguments.reason())),
				"deactivate-account", () -> this.procedures.deactivate(require("actor", this.arguments.actor()),
						require("identifier", this.arguments.identifier()), require("reason", this.arguments.reason())),
				"reactivate-account", () -> this.procedures.reactivate(require("actor", this.arguments.actor()),
						require("identifier", this.arguments.identifier()), require("reason", this.arguments.reason())));
		Supplier<Object> command = commands.get(this.arguments.command());
		if (command == null) {
			log.atError()
				.addKeyValue("command", this.arguments.command())
				.log("Unknown operator command; use one of " + Arrays.toString(commands.keySet().stream().sorted().toArray()));
			this.exitCode = EXIT_USAGE;
			return;
		}
		try {
			Object result = command.get();
			log.atInfo().addKeyValue("command", this.arguments.command()).addKeyValue("result", result)
				.log("Operator procedure completed");
			this.exitCode = EXIT_DONE;
		}
		catch (MissingArgumentException ex) {
			log.atError().addKeyValue("command", this.arguments.command()).log(ex.getMessage());
			this.exitCode = EXIT_USAGE;
		}
		catch (OperatorRefusalException ex) {
			log.atWarn().addKeyValue("command", this.arguments.command())
				.log("Operator procedure refused: " + ex.getMessage());
			this.exitCode = EXIT_REFUSED;
		}
	}

	@Override
	public int getExitCode() {
		return this.exitCode;
	}

	private static String require(String name, String value) {
		if (!StringUtils.hasText(value)) {
			throw new MissingArgumentException("Missing argument --agrinex.operator." + name);
		}
		return value;
	}

	/** An incomplete command line; nothing is changed. */
	private static final class MissingArgumentException extends RuntimeException {

		MissingArgumentException(String message) {
			super(message);
		}

	}

}
