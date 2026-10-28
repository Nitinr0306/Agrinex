package com.agrinex.admin.web.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import com.agrinex.common.validation.TextLength;

/** Contract AddDistrictAliasRequest: a source spelling, its LGD district and the official evidence (ADR-0014). */
public record AddDistrictAliasRequest(@NotNull @TextLength(min = 1, max = 100) String sourceStateName,
		@NotNull @TextLength(min = 1, max = 150) String sourceDistrictName, @NotNull UUID districtId,
		@NotNull @TextLength(min = 1, max = 500) String evidenceNote) {
}
