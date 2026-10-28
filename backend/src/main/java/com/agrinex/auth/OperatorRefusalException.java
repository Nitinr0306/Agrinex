package com.agrinex.auth;

/**
 * An operator procedure refused to act and changed nothing (ADR-0037 item 6, ADR-0041 item 6). The message is
 * safe to print to the operator: it never contains personal data.
 */
public class OperatorRefusalException extends RuntimeException {

	public OperatorRefusalException(String message) {
		super(message);
	}

}
