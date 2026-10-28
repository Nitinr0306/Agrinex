package com.agrinex.common.validation;

import java.math.BigDecimal;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.agrinex.common.Decimals;

class MaxDecimalPlacesValidator implements ConstraintValidator<MaxDecimalPlaces, BigDecimal> {

	private int max;

	@Override
	public void initialize(MaxDecimalPlaces constraint) {
		this.max = constraint.value();
	}

	@Override
	public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
		return value == null || Decimals.decimalPlaces(value) <= this.max;
	}

}
