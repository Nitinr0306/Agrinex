package com.agrinex.common.error;

/** Codes of field-level problems (contracts/error-response.schema.json FieldErrorCode). */
public enum FieldErrorCode {

	REQUIRED,
	INVALID_TYPE,
	INVALID_FORMAT,
	INVALID_VALUE,
	OUT_OF_RANGE,
	TOO_SHORT,
	TOO_LONG,
	PRECISION_EXCEEDED,
	INVALID_RANGE,
	NOT_ALLOWED,
	UNKNOWN_FIELD,
	NOT_FOUND,
	ALREADY_EXISTS

}
