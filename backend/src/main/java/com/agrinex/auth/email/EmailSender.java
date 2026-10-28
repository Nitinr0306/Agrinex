package com.agrinex.auth.email;

/** Delivers one account email. Implementations throw on failure; callers log the failure without personal data. */
public interface EmailSender {

	void send(EmailMessage message);

}
