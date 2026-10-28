package com.agrinex.common.validation;

import java.nio.charset.StandardCharsets;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

class Utf8MaxBytesValidator implements ConstraintValidator<Utf8MaxBytes, String> {

	private int max;

	@Override
	public void initialize(Utf8MaxBytes constraint) {
		this.max = constraint.value();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return value == null || value.getBytes(StandardCharsets.UTF_8).length <= this.max;
	}

}
