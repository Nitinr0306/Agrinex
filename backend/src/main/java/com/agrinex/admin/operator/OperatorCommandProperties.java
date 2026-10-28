package com.agrinex.admin.operator;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Arguments of a one-off operator command (ADR-0037 item 4, ADR-0041 item 3), given on the command line, for
 * example {@code --agrinex.operator.command=erase-farmer}. Personal data in them is never logged.
 *
 * @param command erase-farmer, deactivate-account or reactivate-account
 * @param actor email of the acting ADMIN, which must be an existing, active ADMIN account
 * @param phone the FARMER's phone number in E.164 form (erase-farmer)
 * @param identifier the account's phone number or email (deactivate-account, reactivate-account)
 * @param reference the request reference (erase-farmer)
 * @param reason why the procedure is run
 */
@ConfigurationProperties("agrinex.operator")
record OperatorCommandProperties(String command, String actor, String phone, String identifier, String reference,
		String reason) {

	@Override
	public String toString() {
		return "OperatorCommandProperties[command=" + this.command + "]";
	}

}
