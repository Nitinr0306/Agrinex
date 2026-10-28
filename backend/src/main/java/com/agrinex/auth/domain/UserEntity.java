package com.agrinex.auth.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.agrinex.auth.Role;

/**
 * An account (table {@code users}; spec section 20, 28; ADR-0016, ADR-0047, ADR-0048). The password and the
 * email-verification and password-reset tokens are stored only as hashes. Credential columns never leave this
 * module and are never returned or logged.
 */
@Entity
@Table(name = "users")
public class UserEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "phone_number")
	private String phoneNumber;

	private String email;

	@Column(name = "password_hash")
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	private Role role;

	@Column(name = "is_active")
	private boolean active;

	@Column(name = "created_at")
	private Instant createdAt;

	@Column(name = "email_verified_at")
	private Instant emailVerifiedAt;

	@Column(name = "email_verification_token_hash")
	private String emailVerificationTokenHash;

	@Column(name = "email_verification_expires_at")
	private Instant emailVerificationExpiresAt;

	@Column(name = "password_reset_token_hash")
	private String passwordResetTokenHash;

	@Column(name = "password_reset_expires_at")
	private Instant passwordResetExpiresAt;

	protected UserEntity() {
	}

	public static UserEntity farmer(String phoneNumber, String email, String passwordHash, Instant createdAt) {
		UserEntity user = new UserEntity();
		user.phoneNumber = phoneNumber;
		user.email = email;
		user.passwordHash = passwordHash;
		user.role = Role.FARMER;
		user.active = true;
		user.createdAt = createdAt;
		return user;
	}

	/** A provisioned ADMIN; the operator supplies the email, so it is recorded as verified (ADR-0048 item 4). */
	public static UserEntity provisionedAdmin(String email, String passwordHash, Instant createdAt) {
		UserEntity user = new UserEntity();
		user.email = email;
		user.passwordHash = passwordHash;
		user.role = Role.ADMIN;
		user.active = true;
		user.createdAt = createdAt;
		user.emailVerifiedAt = createdAt;
		return user;
	}

	public boolean isEmailVerified() {
		return this.emailVerifiedAt != null;
	}

	/** Replaces any earlier verification link (ADR-0048 item 1); only while the email is unverified. */
	public void issueEmailVerificationToken(String tokenHash, Instant expiresAt) {
		if (isEmailVerified()) {
			throw new IllegalStateException("The email is already verified");
		}
		this.emailVerificationTokenHash = tokenHash;
		this.emailVerificationExpiresAt = expiresAt;
	}

	/** Marks the email verified and spends the verification token. */
	public void verifyEmail(Instant verifiedAt) {
		this.emailVerifiedAt = verifiedAt;
		this.emailVerificationTokenHash = null;
		this.emailVerificationExpiresAt = null;
	}

	/** Replaces any earlier reset link (ADR-0048 item 1); reset links go only to a verified email. */
	public void issuePasswordResetToken(String tokenHash, Instant expiresAt) {
		if (!isEmailVerified()) {
			throw new IllegalStateException("Reset links are issued only for a verified email");
		}
		this.passwordResetTokenHash = tokenHash;
		this.passwordResetExpiresAt = expiresAt;
	}

	/** Replaces the password hash and spends the reset token. */
	public void resetPassword(String newPasswordHash) {
		this.passwordHash = newPasswordHash;
		this.passwordResetTokenHash = null;
		this.passwordResetExpiresAt = null;
	}

	public void deactivate() {
		this.active = false;
	}

	public void reactivate() {
		this.active = true;
	}

	public UUID getId() {
		return this.id;
	}

	public String getPhoneNumber() {
		return this.phoneNumber;
	}

	public String getEmail() {
		return this.email;
	}

	public String getPasswordHash() {
		return this.passwordHash;
	}

	public Role getRole() {
		return this.role;
	}

	public boolean isActive() {
		return this.active;
	}

	public Instant getEmailVerificationExpiresAt() {
		return this.emailVerificationExpiresAt;
	}

	public Instant getPasswordResetExpiresAt() {
		return this.passwordResetExpiresAt;
	}

}
