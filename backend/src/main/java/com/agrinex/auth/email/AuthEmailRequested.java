package com.agrinex.auth.email;

/**
 * An account email to send once the current transaction commits (ADR-0048 item 8). The recipient and the token
 * are secret or personal data, so {@link #toString()} shows only the kind. {@code token} is null for the
 * password-changed notice.
 */
public record AuthEmailRequested(EmailMessage.Kind kind, String recipient, String token) {

	public static AuthEmailRequested verification(String recipient, String token) {
		return new AuthEmailRequested(EmailMessage.Kind.EMAIL_VERIFICATION, recipient, token);
	}

	public static AuthEmailRequested passwordReset(String recipient, String token) {
		return new AuthEmailRequested(EmailMessage.Kind.PASSWORD_RESET, recipient, token);
	}

	public static AuthEmailRequested passwordChanged(String recipient) {
		return new AuthEmailRequested(EmailMessage.Kind.PASSWORD_CHANGED, recipient, null);
	}

	@Override
	public String toString() {
		return "AuthEmailRequested[kind=" + this.kind + "]";
	}

}
