package com.agrinex.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

class TextLengthValidator implements ConstraintValidator<TextLength, String> {

	private int min;

	private int max;

	@Override
	public void initialize(TextLength constraint) {
		this.min = constraint.min();
		this.max = constraint.max();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}
		int length = value.codePointCount(0, value.length());
		return length >= this.min && length <= this.max;
	}

}
