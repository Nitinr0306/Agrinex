package com.agrinex.auth.web.dto;

import jakarta.validation.constraints.NotNull;

import com.agrinex.auth.domain.AccountRules;
import com.agrinex.common.validation.TextLength;
import com.agrinex.common.validation.Utf8MaxBytes;

/** Contract LoginRequest: one identifier and the password (ADR-0016 item 2, ADR-0047 item 3). */
public record LoginRequest(
		@NotNull @TextLength(min = 1, max = AccountRules.IDENTIFIER_MAX_LENGTH) String identifier,
		@NotNull @TextLength(min = 1, max = 64) @Utf8MaxBytes(72) String password) {

	@Override
	public String toString() {
		return "LoginRequest[]";
	}

}
