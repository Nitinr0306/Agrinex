package com.agrinex.auth.web.dto;

import jakarta.validation.constraints.NotNull;

import com.agrinex.auth.domain.AccountRules;
import com.agrinex.common.validation.TextLength;

/** Contract PasswordResetRequest (ADR-0047, ADR-0048). */
public record PasswordResetRequest(@NotNull @TextLength(min = 1, max = AccountRules.IDENTIFIER_MAX_LENGTH) String identifier) {

	@Override
	public String toString() {
		return "PasswordResetRequest[]";
	}

}
