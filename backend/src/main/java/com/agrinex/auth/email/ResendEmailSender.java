package com.agrinex.auth.email;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/**
 * Sends account emails through Resend's HTTPS API with Spring Framework's {@link RestClient}: no SDK and no
 * additional library (ADR-0048 item 8; Resend API reference, {@code POST /emails}). Each message carries its own
 * {@code Idempotency-Key}. There is no retry: a failure is reported to the caller, and the user can ask for a
 * new link.
 */
class ResendEmailSender implements EmailSender {

	static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

	private final RestClient restClient;

	private final String from;

	ResendEmailSender(RestClient restClient, String from) {
		this.restClient = restClient;
		this.from = from;
	}

	@Override
	public void send(EmailMessage message) {
		this.restClient.post()
			.uri("/emails")
			.contentType(MediaType.APPLICATION_JSON)
			.header(IDEMPOTENCY_KEY_HEADER, message.idempotencyKey())
			.body(new SendEmailRequest(this.from, List.of(message.to()), message.subject(), message.text()))
			.retrieve()
			.toBodilessEntity();
	}

	/** Resend's request body: sender, recipients, subject and the plain-text body. */
	record SendEmailRequest(String from, List<String> to, String subject, String text) {
	}

}
