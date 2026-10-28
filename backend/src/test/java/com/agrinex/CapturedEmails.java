package com.agrinex;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Primary;

import com.agrinex.auth.email.EmailMessage;
import com.agrinex.auth.email.EmailSender;

import static org.awaitility.Awaitility.await;

/**
 * Replaces email delivery in integration tests: account emails are kept in memory instead of being sent, so a test
 * can follow the link a user would receive. Nothing leaves the test process.
 */
@TestConfiguration(proxyBeanMethods = false)
@Primary
public class CapturedEmails implements EmailSender {

	private static final Pattern LINK_TOKEN = Pattern.compile("#token=([A-Za-z0-9_-]{43})");

	private final List<EmailMessage> messages = new CopyOnWriteArrayList<>();

	@Override
	public void send(EmailMessage message) {
		this.messages.add(message);
	}

	/** Waits for the email of this kind sent to the recipient and returns the token in its link. */
	public String awaitToken(String recipient, EmailMessage.Kind kind) {
		String[] token = new String[1];
		await().atMost(Duration.ofSeconds(10)).until(() -> {
			for (EmailMessage message : this.messages) {
				if (message.kind() == kind && message.to().equalsIgnoreCase(recipient)) {
					Matcher matcher = LINK_TOKEN.matcher(message.text());
					if (matcher.find()) {
						token[0] = matcher.group(1);
						return true;
					}
				}
			}
			return false;
		});
		return token[0];
	}

	/** Waits for the email of this kind sent to the recipient and returns its text. */
	public String awaitText(String recipient, EmailMessage.Kind kind) {
		String[] text = new String[1];
		await().atMost(Duration.ofSeconds(10)).until(() -> {
			for (EmailMessage message : this.messages) {
				if (message.kind() == kind && message.to().equalsIgnoreCase(recipient)) {
					text[0] = message.text();
					return true;
				}
			}
			return false;
		});
		return text[0];
	}

	public long count(String recipient, EmailMessage.Kind kind) {
		return this.messages.stream()
			.filter((message) -> message.kind() == kind && message.to().equalsIgnoreCase(recipient))
			.count();
	}

}
