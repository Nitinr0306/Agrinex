package com.agrinex.admin.web.dto;

import jakarta.validation.constraints.NotNull;

import com.agrinex.common.validation.TextLength;

/** Contract VerifyPriceUnitRequest: the unit and its official evidence (ADR-0008 item 1). */
public record VerifyPriceUnitRequest(@NotNull @TextLength(min = 1, max = 50) String priceUnit,
		@NotNull @TextLength(min = 1, max = 300) String priceUnitSource) {
}
