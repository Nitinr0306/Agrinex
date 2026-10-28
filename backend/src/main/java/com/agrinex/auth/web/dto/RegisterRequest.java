package com.agrinex.auth.web.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.agrinex.auth.domain.AccountRules;
import com.agrinex.auth.service.Registration;
import com.agrinex.common.error.ApiException;
import com.agrinex.common.error.FieldErrorCode;
import com.agrinex.common.validation.TextLength;
import com.agrinex.common.validation.Utf8MaxBytes;

/** Contract RegisterRequest (ADR-0016 item 3, ADR-0047 item 1). */
public record RegisterRequest(
		@NotNull @Pattern(regexp = AccountRules.PHONE_PATTERN) String phoneNumber,
		@NotNull @TextLength(min = AccountRules.EMAIL_MIN_LENGTH, max = AccountRules.EMAIL_MAX_LENGTH)
		@Pattern(regexp = AccountRules.EMAIL_PATTERN) String email,
		@NotNull @TextLength(min = 8, max = 64) @Utf8MaxBytes(72) String password,
		@TextLength(min = 1, max = AccountRules.DISPLAY_NAME_MAX_LENGTH) String displayName,
		UUID defaultDistrictId) {

	/** The display name is trimmed; a name that is empty once trimmed is too short (contract RegisterRequest). */
	public Registration toRegistration() {
		String trimmedName = (this.displayName != null) ? this.displayName.strip() : null;
		if (trimmedName != null && trimmedName.isEmpty()) {
			throw ApiException.validation("displayName", FieldErrorCode.TOO_SHORT, "must not be blank");
		}
		return new Registration(this.phoneNumber, this.email, this.password, trimmedName, this.defaultDistrictId);
	}

	@Override
	public String toString() {
		return "RegisterRequest[]";
	}

}
