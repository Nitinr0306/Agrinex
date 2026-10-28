package com.agrinex.auth.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.agrinex.auth.config.AuthProperties;
import com.agrinex.auth.domain.FarmerProfileEntity;
import com.agrinex.auth.domain.RefreshTokenEntity;
import com.agrinex.auth.domain.UserEntity;
import com.agrinex.auth.mapper.AuthMapper;
import com.agrinex.auth.repository.FarmerProfileRepository;
import com.agrinex.auth.repository.RefreshTokenRepository;
import com.agrinex.auth.repository.UserRepository;
import com.agrinex.auth.security.AccessTokenIssuer;
import com.agrinex.auth.security.SecureTokens;
import com.agrinex.common.time.AgrinexCalendar;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Refresh-token families (spec section 28; ADR-0016 item 5; RFC 9700 section 4.14.2): every sign-in starts a
 * family, every refresh rotates the presented token inside its family, presenting a revoked token revokes the
 * whole family, and logout revokes the family. Rejections return an empty result instead of throwing, so a
 * revocation made while rejecting is committed.
 */
@Service
@Transactional
public class SessionService {

	private final RefreshTokenRepository tokens;

	private final UserRepository users;

	private final FarmerProfileRepository profiles;

	private final SecureTokens secureTokens;

	private final AccessTokenIssuer accessTokens;

	private final AuthProperties properties;

	private final AgrinexCalendar calendar;

	SessionService(RefreshTokenRepository tokens, UserRepository users, FarmerProfileRepository profiles,
			SecureTokens secureTokens, AccessTokenIssuer accessTokens, AuthProperties properties,
			AgrinexCalendar calendar) {
		this.tokens = tokens;
		this.users = users;
		this.profiles = profiles;
		this.secureTokens = secureTokens;
		this.accessTokens = accessTokens;
		this.properties = properties;
		this.calendar = calendar;
	}

	/** Signs an account in: a new refresh-token family and a new access token. */
	public SessionGrant open(UserEntity user) {
		return grant(user, issueRefreshToken(user.getId(), UUID.randomUUID(), this.calendar.now()));
	}

	/**
	 * Rotates the presented refresh token. Empty when it is missing, unknown, expired or revoked, or when its
	 * account is inactive or gone; a revoked token (reuse) and a token of an inactive account also revoke their
	 * family.
	 */
	public Optional<SessionGrant> refresh(String presentedToken) {
		if (presentedToken == null || presentedToken.isEmpty()) {
			return Optional.empty();
		}
		Optional<RefreshTokenEntity> found = this.tokens.findForRotation(SecureTokens.hash(presentedToken));
		if (found.isEmpty()) {
			return Optional.empty();
		}
		RefreshTokenEntity token = found.get();
		Instant now = this.calendar.now();
		if (token.isRevoked()) {
			this.tokens.revokeFamily(token.getFamilyId(), now);
			return Optional.empty();
		}
		if (token.isExpired(now)) {
			return Optional.empty();
		}
		Optional<UserEntity> user = this.users.findById(token.getUserId()).filter(UserEntity::isActive);
		if (user.isEmpty()) {
			this.tokens.revokeFamily(token.getFamilyId(), now);
			return Optional.empty();
		}
		token.revoke(now);
		return Optional.of(grant(user.get(), issueRefreshToken(user.get().getId(), token.getFamilyId(), now)));
	}

	/** Revokes the family of the presented token, if it names one. Idempotent. */
	public void logout(String presentedToken) {
		if (presentedToken == null || presentedToken.isEmpty()) {
			return;
		}
		this.tokens.findByTokenHash(SecureTokens.hash(presentedToken))
			.ifPresent((token) -> this.tokens.revokeFamily(token.getFamilyId(), this.calendar.now()));
	}

	/** Revokes every family of the account (password reset, deactivation). Joins the caller's transaction. */
	void revokeAllSessions(UUID userId) {
		this.tokens.revokeAllOfUser(userId, this.calendar.now());
	}

	private String issueRefreshToken(UUID userId, UUID familyId, Instant now) {
		String token = this.secureTokens.newToken();
		this.tokens.save(new RefreshTokenEntity(userId, SecureTokens.hash(token), familyId, now,
				now.plus(this.properties.refreshTokenTtl())));
		return token;
	}

	private SessionGrant grant(UserEntity user, String refreshToken) {
		FarmerProfileEntity profile = this.profiles.findByUserId(user.getId()).orElse(null);
		return new SessionGrant(this.accessTokens.issue(user.getId(), user.getRole()),
				this.accessTokens.lifetimeSeconds(), refreshToken, this.properties.refreshTokenTtl(),
				AuthMapper.toView(user, profile));
	}

}
