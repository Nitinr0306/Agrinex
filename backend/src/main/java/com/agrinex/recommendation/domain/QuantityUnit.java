package com.agrinex.recommendation.domain;

import java.math.BigDecimal;

/**
 * Unit of a farmer quantity (spec section 16, 32; ADR-0023). Only the farmer quantity is converted; source prices
 * never are (ADR-0008). 1 QUINTAL = 100 KG.
 */
public enum QuantityUnit {

	KG(BigDecimal.ONE), QUINTAL(BigDecimal.valueOf(100));

	private final BigDecimal kilograms;

	QuantityUnit(BigDecimal kilograms) {
		this.kilograms = kilograms;
	}

	/** quantityInKg = quantity x (1 if KG else 100), exact (API_CONVENTIONS.md section 4.1). */
	public BigDecimal toKilograms(BigDecimal quantity) {
		return quantity.multiply(this.kilograms);
	}

	/** quantityInQuintal = quantityInKg / 100, exact: at most 5 decimal places for a 3-decimal quantity. */
	public BigDecimal toQuintals(BigDecimal quantity) {
		return toKilograms(quantity).movePointLeft(2);
	}

}
