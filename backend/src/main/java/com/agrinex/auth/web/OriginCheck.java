package com.agrinex.auth.web;

import com.agrinex.auth.config.OriginProperties;
import com.agrinex.common.error.ApiException;
import com.agrinex.common.error.ErrorCode;

import org.springframework.stereotype.Component;

/**
 * The Origin check of the two cookie-authenticated operations, refresh and logout (ADR-0017 item 3): an absent,
 * {@code null} or different Origin answers 403 ORIGIN_NOT_ALLOWED, before the cookie is used.
 */
@Component
class OriginCheck {

	private final OriginProperties origins;

	OriginCheck(OriginProperties origins) {
		this.origins = origins;
	}

	void requirePublicOrigin(String origin) {
		if (origin == null || !origin.equals(this.origins.publicOrigin())) {
			throw new ApiException(ErrorCode.ORIGIN_NOT_ALLOWED, "The Origin header must equal the public origin.");
		}
	}

}
