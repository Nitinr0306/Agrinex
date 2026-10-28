package com.agrinex.auth.web.dto;

import java.util.UUID;

import com.agrinex.auth.Role;
import com.agrinex.auth.service.AuthenticatedUserView;
import com.agrinex.auth.service.SessionGrant;

/**
 * Contract AuthResponse (spec section 30). The refresh token is never in the body: it travels only in the
 * HttpOnly cookie (ADR-0017 item 3).
 */
public record AuthResponse(String accessToken, String tokenType, long expiresIn, AuthenticatedUser user) {

	static final String TOKEN_TYPE = "Bearer";

	public static AuthResponse of(SessionGrant grant) {
		AuthenticatedUserView user = grant.user();
		return new AuthResponse(grant.accessToken(), TOKEN_TYPE, grant.expiresInSeconds(),
				new AuthenticatedUser(user.id(), user.role(), user.displayName(), user.defaultDistrictId(),
						user.emailVerified()));
	}

	@Override
	public String toString() {
		return "AuthResponse[user=" + this.user.id() + "]";
	}

	/** Contract AuthenticatedUser: never the phone number or the email (ADR-0047 item 2). */
	record AuthenticatedUser(UUID id, Role role, String displayName, UUID defaultDistrictId, boolean emailVerified) {
	}

}
