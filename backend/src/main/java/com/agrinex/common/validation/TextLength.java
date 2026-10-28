package com.agrinex.common.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Length in Unicode characters (code points), as JSON Schema's {@code minLength} and {@code maxLength} count it.
 * A violation is reported as TOO_SHORT or TOO_LONG. {@code null} is valid; combine with {@code @NotNull}.
 */
@Documented
@Constraint(validatedBy = TextLengthValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT })
@Retention(RetentionPolicy.RUNTIME)
public @interface TextLength {

	String message() default "has an invalid length";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	int min() default 0;

	int max() default Integer.MAX_VALUE;

}
