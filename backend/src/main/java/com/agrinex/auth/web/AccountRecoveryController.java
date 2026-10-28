package com.agrinex.auth.web;

import jakarta.validation.Valid;

import com.agrinex.auth.CurrentAccount;
import com.agrinex.auth.service.EmailVerificationService;
import com.agrinex.auth.service.PasswordResetService;
import com.agrinex.auth.web.dto.EmailVerificationConfirmRequest;
import com.agrinex.auth.web.dto.PasswordResetConfirmRequest;
import com.agrinex.auth.web.dto.PasswordResetRequest;
import com.agrinex.common.ratelimit.RateLimited;
import com.agrinex.common.ratelimit.RateLimited.Bucket;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Email verification and password recovery by emailed single-use links (contract tag Auth; ADR-0047,
 * ADR-0048). None of these operations reveals whether an account exists.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AccountRecoveryController {

	private final PasswordResetService passwordResets;

	private final EmailVerificationService emailVerification;

	AccountRecoveryController(PasswordResetService passwordResets, EmailVerificationService emailVerification) {
		this.passwordResets = passwordResets;
		this.emailVerification = emailVerification;
	}

	@PostMapping("/password-reset/request")
	@RateLimited(Bucket.PASSWORD_RESET_REQUEST)
	public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
		this.passwordResets.request(request.identifier());
		return ResponseEntity.accepted().build();
	}

	@PostMapping("/password-reset/confirm")
	@RateLimited(Bucket.PASSWORD_RESET_CONFIRM)
	public ResponseEntity<Void> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
		this.passwordResets.confirm(request.token(), request.newPassword());
		return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, RefreshCookie.CLEAR).build();
	}

	@PostMapping("/email-verification/confirm")
	@RateLimited(Bucket.EMAIL_VERIFICATION_CONFIRM)
	public ResponseEntity<Void> confirmEmailVerification(@Valid @RequestBody EmailVerificationConfirmRequest request) {
		this.emailVerification.confirm(request.token());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/email-verification/resend")
	@RateLimited(Bucket.EMAIL_VERIFICATION_RESEND)
	@PreAuthorize("hasAnyRole('FARMER', 'ADMIN')")
	public ResponseEntity<Void> resendEmailVerification(@AuthenticationPrincipal CurrentAccount account) {
		this.emailVerification.resend(account.userId());
		return ResponseEntity.accepted().build();
	}

}
