package com.agrinex.admin.web.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import com.agrinex.common.validation.MaxDecimalPlaces;
import com.agrinex.common.validation.TextLength;

/** Contract SetMarketCoordinatesRequest: exact coordinates with their identified source (ADR-0014 item 4). */
public record SetMarketCoordinatesRequest(
		@NotNull @DecimalMin("-90") @DecimalMax("90") @MaxDecimalPlaces(6) BigDecimal lat,
		@NotNull @DecimalMin("-180") @DecimalMax("180") @MaxDecimalPlaces(6) BigDecimal lon,
		@NotNull @TextLength(min = 1, max = 300) String coordinateSource) {
}
