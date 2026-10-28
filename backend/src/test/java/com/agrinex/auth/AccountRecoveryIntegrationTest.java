package com.agrinex.auth;

import java.time.Duration;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.RequestBuilder;

import com.agrinex.IntegrationTestSupport;
import com.agrinex.auth.email.EmailMessage.Kind;

import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Email verification and password recovery by email link (ADR-0047, ADR-0048), following the token a user would
 * receive. Emails are captured in memory (CapturedEmails); accounts are SYNTHETIC on the reserved .invalid domain.
 */
class AccountRecoveryIntegrationTest extends IntegrationTestSupport {

	static final String PASSWORD = "synthetic-pass-123";

	static final String NEW_PASSWORD = "synthetic-new-pass-456";

	@Test
	void aVerificationLinkConfirmsTheEmailOnce() throws Exception {
		Account account = register();
		String token = this.emails.awaitToken(account.email(), Kind.EMAIL_VERIFICATION);
		this.mvc.perform(json("/api/v1/auth/email-verification/confirm", "{\"token\": \"" + token + "\"}"))
			.andExpect(status().isNoContent());
		this.mvc.perform(json("/api/v1/auth/email-verification/confirm", "{\"token\": \"" + token + "\"}"))
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.code").value("EMAIL_VERIFICATION_TOKEN_INVALID"));
	}

	@Test
	void aResetLinkSetsANewPasswordAndEndsEverySession() throws Exception {
		Account account = register();
		confirmEmail(account);
		Cookie session = loginResponse(account.phone(), PASSWORD).getCookie("__Secure-agrinex_refresh");

		this.mvc.perform(json("/api/v1/auth/password-reset/request", "{\"identifier\": \"" + account.phone() + "\"}"))
			.andExpect(status().isAccepted());
		String token = this.emails.awaitToken(account.email(), Kind.PASSWORD_RESET);
		this.mvc
			.perform(json("/api/v1/auth/password-reset/confirm",
					"{\"token\": \"%s\", \"newPassword\": \"%s\"}".formatted(token, NEW_PASSWORD)))
			.andExpect(status().isNoContent());

		this.mvc.perform(json("/api/v1/auth/login",
				"{\"identifier\": \"%s\", \"password\": \"%s\"}".formatted(account.phone(), PASSWORD)))
			.andExpect(status().isUnauthorized());
		loginResponse(account.phone(), NEW_PASSWORD);
		this.mvc.perform(post("/api/v1/auth/refresh").cookie(session).header("Origin", PUBLIC_ORIGIN))
			.andExpect(status().isUnauthorized());
		this.mvc
			.perform(json("/api/v1/auth/password-reset/confirm",
					"{\"token\": \"%s\", \"newPassword\": \"%s\"}".formatted(token, "synthetic-third-pass")))
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.code").value("PASSWORD_RESET_TOKEN_INVALID"));
		await().atMost(Duration.ofSeconds(10))
			.until(() -> this.emails.count(account.email(), Kind.PASSWORD_CHANGED) == 1);
	}

	@Test
	void anUnconfirmedEmailGetsTheSameAnswerButNoResetLink() throws Exception {
		Account account = register();
		this.mvc.perform(json("/api/v1/auth/password-reset/request", "{\"identifier\": \"" + account.email() + "\"}"))
			.andExpect(status().isAccepted());
		this.mvc.perform(json("/api/v1/auth/password-reset/request",
				"{\"identifier\": \"nobody-" + account.phone().substring(1) + "@agrinex.invalid\"}"))
			.andExpect(status().isAccepted());
		// Emails leave asynchronously, so "no reset link" must hold for a while, not just at one instant.
		await().during(Duration.ofSeconds(2)).atMost(Duration.ofSeconds(3))
			.until(() -> this.emails.count(account.email(), Kind.PASSWORD_RESET) == 0);
	}

	private Account register() throws Exception {
		String phone = syntheticPhone();
		String email = "recovery-" + phone.substring(1) + "@agrinex.invalid";
		this.mvc
			.perform(json("/api/v1/auth/register", "{\"phoneNumber\": \"%s\", \"email\": \"%s\", \"password\": \"%s\"}"
				.formatted(phone, email, PASSWORD)))
			.andExpect(status().isCreated());
		return new Account(phone, email);
	}

	private void confirmEmail(Account account) throws Exception {
		String token = this.emails.awaitToken(account.email(), Kind.EMAIL_VERIFICATION);
		this.mvc.perform(json("/api/v1/auth/email-verification/confirm", "{\"token\": \"" + token + "\"}"))
			.andExpect(status().isNoContent());
	}

	private static RequestBuilder json(String path, String body) {
		return post(path).contentType(MediaType.APPLICATION_JSON).content(body);
	}

	record Account(String phone, String email) {
	}

}
