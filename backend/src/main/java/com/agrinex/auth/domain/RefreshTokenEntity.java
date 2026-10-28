package com.agrinex.auth.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A refresh token, stored only as the SHA-256 hash of the opaque value (table {@code refresh_tokens}; spec
 * section 28; ADR-0016 item 5). Every token issued from one sign-in shares a family; presenting a revoked token
 * revokes the whole family (RFC 9700 section 4.14.2).
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "user_id")
	private UUID userId;

	@Column(name = "token_hash")
	private String tokenHash;

	@Column(name = "expires_at")
	private Instant expiresAt;

	@Column(name = "family_id")
	private UUID familyId;

	@Column(name = "created_at")
	private Instant createdAt;

	@Column(name = "revoked_at")
	private Instant revokedAt;

	protected RefreshTokenEntity() {
	}

	public RefreshTokenEntity(UUID userId, String tokenHash, UUID familyId, Instant createdAt, Instant expiresAt) {
		this.userId = userId;
		this.tokenHash = tokenHash;
		this.familyId = familyId;
		this.createdAt = createdAt;
		this.expiresAt = expiresAt;
	}

	public boolean isRevoked() {
		return this.revokedAt != null;
	}

	public boolean isExpired(Instant now) {
		return !this.expiresAt.isAfter(now);
	}

	public void revoke(Instant now) {
		if (this.revokedAt == null) {
			this.revokedAt = now;
		}
	}

	public UUID getUserId() {
		return this.userId;
	}

	public UUID getFamilyId() {
		return this.familyId;
	}

}
