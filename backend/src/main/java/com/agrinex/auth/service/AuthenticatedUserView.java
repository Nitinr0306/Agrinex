package com.agrinex.auth.service;

import java.util.UUID;

import com.agrinex.auth.Role;

/** The signed-in account as the auth operations return it (contract AuthenticatedUser). Never the email. */
public record AuthenticatedUserView(UUID id, Role role, String displayName, UUID defaultDistrictId,
		boolean emailVerified) {
}
