package com.agrinex.auth.mapper;

import com.agrinex.auth.domain.FarmerProfileEntity;
import com.agrinex.auth.domain.UserEntity;
import com.agrinex.auth.service.AuthenticatedUserView;

/** Maps auth entities to the views the web layer returns. Credentials are never mapped. */
public final class AuthMapper {

	private AuthMapper() {
	}

	public static AuthenticatedUserView toView(UserEntity user, FarmerProfileEntity profile) {
		return new AuthenticatedUserView(user.getId(), user.getRole(),
				(profile != null) ? profile.getDisplayName() : null,
				(profile != null) ? profile.getDefaultDistrictId() : null, user.isEmailVerified());
	}

}
