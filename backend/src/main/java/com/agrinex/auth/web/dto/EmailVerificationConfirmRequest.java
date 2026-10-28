package com.agrinex.auth.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.agrinex.auth.domain.AccountRules;

/** Contract EmailVerificationConfirmRequest (ADR-0048). */
public record EmailVerificationConfirmRequest(@NotNull @Pattern(regexp = AccountRules.LINK_TOKEN_PATTERN) String token) {

	@Override
	public String toString() {
		return "EmailVerificationConfirmRequest[]";
	}

}
