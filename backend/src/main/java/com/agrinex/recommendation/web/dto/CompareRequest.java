package com.agrinex.recommendation.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import com.agrinex.common.validation.MaxDecimalPlaces;
import com.agrinex.recommendation.domain.QuantityUnit;
import com.agrinex.recommendation.service.CompareCommand;

/** Contract CompareRequest (recommendation.schema.json; ADR-0023 item 1). */
public record CompareRequest(
		@NotNull UUID commodityId,
		UUID varietyId,
		UUID gradeId,
		@NotNull @DecimalMin(value = "0", inclusive = false) @DecimalMax("999999999.999") @MaxDecimalPlaces(3)
		BigDecimal quantity,
		@NotNull QuantityUnit quantityUnit,
		@NotNull @DecimalMin("-90") @DecimalMax("90") @MaxDecimalPlaces(6) BigDecimal originLat,
		@NotNull @DecimalMin("-180") @DecimalMax("180") @MaxDecimalPlaces(6) BigDecimal originLon,
		@Min(1) Integer maxDistanceKm) {

	public CompareCommand toCommand() {
		return new CompareCommand(this.commodityId, this.varietyId, this.gradeId, this.quantity, this.quantityUnit,
				this.originLat, this.originLon, this.maxDistanceKm);
	}

}
