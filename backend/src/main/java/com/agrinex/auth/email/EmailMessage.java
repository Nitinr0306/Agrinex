package com.agrinex.auth.email;

/**
 * One account email, plain text only. The recipient, the text and any link in it are personal or secret data:
 * a message is never logged; only its {@link Kind} may be (ADR-0048 item 10).
 */
public record EmailMessage(Kind kind, String to, String subject, String text, String idempotencyKey) {

	@Override
	public String toString() {
		return "EmailMessage[kind=" + this.kind + "]";
	}

	/** The three account emails of ADR-0048 item 8. */
	public enum Kind {

		EMAIL_VERIFICATION, PASSWORD_RESET, PASSWORD_CHANGED

	}

}
