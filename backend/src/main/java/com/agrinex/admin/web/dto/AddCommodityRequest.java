package com.agrinex.admin.web.dto;

import jakarta.validation.constraints.NotNull;

import com.agrinex.common.validation.TextLength;

/**
 * Contract AddCommodityRequest (ADR-0006 item 2, ADR-0008): the official name, group, verified unit with its
 * evidence, and the official source reference of the commodity, which is kept in the audit details.
 */
public record AddCommodityRequest(
		@NotNull @TextLength(min = 1, max = 200) String name,
		@TextLength(min = 1, max = 100) String category,
		@NotNull @TextLength(min = 1, max = 50) String priceUnit,
		@NotNull @TextLength(min = 1, max = 300) String priceUnitSource,
		@NotNull @TextLength(min = 1, max = 500) String sourceReference) {
}
