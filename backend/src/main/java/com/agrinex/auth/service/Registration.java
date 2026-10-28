package com.agrinex.auth.service;

import java.util.UUID;

/**
 * A farmer's quick registration (spec section 25; ADR-0016 item 3, ADR-0047 item 1), already checked against the
 * contract's data-independent rules. {@link #toString()} never shows the password or the personal data.
 */
public record Registration(String phoneNumber, String email, String password, String displayName,
		UUID defaultDistrictId) {

	@Override
	public String toString() {
		return "Registration[defaultDistrictId=" + this.defaultDistrictId + "]";
	}

}
