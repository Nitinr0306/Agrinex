package com.agrinex.auth.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.agrinex.auth.domain.AccountRules;
import com.agrinex.common.validation.TextLength;
import com.agrinex.common.validation.Utf8MaxBytes;

/**
 * Contract PasswordResetConfirmRequest (ADR-0048). The ADMIN minimum of 15 characters is checked once the token
 * names the account (ADR-0016 item 3).
 */
public record PasswordResetConfirmRequest(
		@NotNull @Pattern(regexp = AccountRules.LINK_TOKEN_PATTERN) String token,
		@NotNull @TextLength(min = 8, max = 64) @Utf8MaxBytes(72) String newPassword) {

	@Override
	public String toString() {
		return "PasswordResetConfirmRequest[]";
	}

}
