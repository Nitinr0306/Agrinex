package com.agrinex.auth.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The non-delivering adapter for local development and tests (ADR-0048 item 8): nothing is sent, and only the
 * message kind is logged, never the address, the token or the link.
 */
class NonDeliveringEmailSender implements EmailSender {

	private static final Logger log = LoggerFactory.getLogger(NonDeliveringEmailSender.class);

	@Override
	public void send(EmailMessage message) {
		log.info("Account email not delivered: the email provider is 'none' (kind={})", message.kind());
	}

}
