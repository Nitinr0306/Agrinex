package com.agrinex.admin.web.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import com.agrinex.common.validation.MaxDecimalPlaces;
import com.agrinex.recommendation.WeightsUpdate;

/**
 * Contract WeightsUpdateRequest: each weight 0 to 1 with at most 4 decimal places, freshness days from 0. The order
 * fresh <= recent <= stale is checked by the recommendation module, which owns it.
 */
public record WeightsUpdateRequest(
		@NotNull @DecimalMin("0") @DecimalMax("1") @MaxDecimalPlaces(4) BigDecimal wNetRealization,
		@NotNull @DecimalMin("0") @DecimalMax("1") @MaxDecimalPlaces(4) BigDecimal wPrice,
		@NotNull @DecimalMin("0") @DecimalMax("1") @MaxDecimalPlaces(4) BigDecimal wTrend,
		@NotNull @DecimalMin("0") @DecimalMax("1") @MaxDecimalPlaces(4) BigDecimal wFreshness,
		@NotNull @DecimalMin("0") @DecimalMax("1") @MaxDecimalPlaces(4) BigDecimal wTransportPenalty,
		@NotNull @Min(0) Integer freshnessFreshDays,
		@NotNull @Min(0) Integer freshnessRecentDays,
		@NotNull @Min(0) Integer freshnessStaleDays) {

	public WeightsUpdate toUpdate() {
		return new WeightsUpdate(this.wNetRealization, this.wPrice, this.wTrend, this.wFreshness,
				this.wTransportPenalty, this.freshnessFreshDays, this.freshnessRecentDays, this.freshnessStaleDays);
	}

}
