package com.agrinex.common.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * At most {@code value} bytes in UTF-8. Passwords are limited to 72 bytes, BCrypt's input limit; a longer value is
 * rejected as TOO_LONG and never truncated (ADR-0016 item 3).
 */
@Documented
@Constraint(validatedBy = Utf8MaxBytesValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT })
@Retention(RetentionPolicy.RUNTIME)
public @interface Utf8MaxBytes {

	String message() default "is longer than allowed in UTF-8 bytes";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	int value();

}
