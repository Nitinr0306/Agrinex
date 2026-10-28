package com.agrinex.auth.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.agrinex.common.logging.CorrelationId;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestClientResponseException;

/**
 * Sends account emails after the transaction that requested them commits, on a separate thread, so the
 * response neither waits for nor reveals the delivery (ADR-0048 items 5 and 8). A failure is logged as an ERROR
 * event with the request's correlation id and without personal data; there is no queue and no retry.
 */
@Component
class AuthEmailDispatcher {

	private static final Logger log = LoggerFactory.getLogger(AuthEmailDispatcher.class);

	private final AuthEmails emails;

	private final EmailSender sender;

	private final TaskExecutor executor;

	AuthEmailDispatcher(AuthEmails emails, EmailSender sender, @Qualifier(EmailConfiguration.EMAIL_EXECUTOR) TaskExecutor executor) {
		this.emails = emails;
		this.sender = sender;
		this.executor = executor;
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	void onEmailRequested(AuthEmailRequested request) {
		EmailMessage message = this.emails.compose(request);
		String correlationId = MDC.get(CorrelationId.MDC_KEY);
		try {
			this.executor.execute(() -> deliver(message, correlationId));
		}
		catch (TaskRejectedException ex) {
			log.error("Account email not sent: the email executor is saturated (kind={})", message.kind());
		}
	}

	private void deliver(EmailMessage message, String correlationId) {
		if (correlationId != null) {
			MDC.put(CorrelationId.MDC_KEY, correlationId);
		}
		try {
			this.sender.send(message);
		}
		catch (RestClientResponseException ex) {
			log.error("Account email not sent: the provider answered HTTP {} (kind={})", ex.getStatusCode().value(),
					message.kind());
		}
		catch (RuntimeException ex) {
			log.error("Account email not sent: {} (kind={})", ex.getClass().getSimpleName(), message.kind());
		}
		finally {
			MDC.remove(CorrelationId.MDC_KEY);
		}
	}

}
