package com.agrinex.common.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * At most {@code value} decimal places, the contract's {@code x-agrinex-max-decimal-places}
 * (docs/api/API_CONVENTIONS.md section 4). A violation is reported as PRECISION_EXCEEDED.
 */
@Documented
@Constraint(validatedBy = MaxDecimalPlacesValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT })
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxDecimalPlaces {

	String message() default "has too many decimal places";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	int value();

}
