package com.agrinex.alert.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import com.agrinex.alert.domain.AlertType;
import com.agrinex.alert.service.NewAlert;
import com.agrinex.common.validation.MaxDecimalPlaces;

/** Contract CreateAlertRequest (spec section 34). */
public record CreateAlertRequest(
		@NotNull AlertType alertType,
		@NotNull UUID commodityId,
		UUID marketId,
		@NotNull @DecimalMin(value = "0", inclusive = false) @DecimalMax("9999999999.9999") @MaxDecimalPlaces(4)
		BigDecimal thresholdValue) {

	public NewAlert toNewAlert() {
		return new NewAlert(this.alertType, this.commodityId, this.marketId, this.thresholdValue);
	}

}
