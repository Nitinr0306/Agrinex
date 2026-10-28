package com.agrinex.common.error;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

import com.agrinex.common.error.ApiErrorResponse.FieldError;
import com.agrinex.common.validation.TextLength;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Translates every exception into an {@link ApiErrorResponse} (spec section 10, 28; ADR-0022 item 4). Bodies
 * are written directly as {@code application/problem+json}, so an error is answered even when the client's
 * Accept header excludes JSON. Unexpected faults are logged with their stack trace server-side only.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	private static final Pattern MISSING_REQUIRED_PROPERTY = Pattern.compile("Missing required creator property '([^']+)'");

	private final ProblemWriter problemWriter;

	public GlobalExceptionHandler(ProblemWriter problemWriter) {
		this.problemWriter = problemWriter;
	}

	@ExceptionHandler(ApiException.class)
	void handleApiException(ApiException ex, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		this.problemWriter.write(request, response, ex);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	void handleBeanValidation(MethodArgumentNotValidException ex, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		List<FieldError> errors = new ArrayList<>();
		for (org.springframework.validation.FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
			errors.add(new FieldError(fieldError.getField(), fieldCode(fieldError), message(fieldError)));
		}
		for (var globalError : ex.getBindingResult().getGlobalErrors()) {
			errors.add(new FieldError(globalError.getObjectName(), FieldErrorCode.INVALID_VALUE,
					String.valueOf(globalError.getDefaultMessage())));
		}
		write(request, response, ErrorCode.VALIDATION_FAILED, "The request has invalid fields.", errors);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	void handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		Throwable cause = ex.getMostSpecificCause();
		if (cause instanceof UnrecognizedPropertyException unknown) {
			String field = path(unknown);
			write(request, response, ErrorCode.VALIDATION_FAILED, "The request has invalid fields.",
					List.of(new FieldError(field.isEmpty() ? unknown.getPropertyName() : field,
							FieldErrorCode.UNKNOWN_FIELD, "is not defined by the contract")));
			return;
		}
		if (cause instanceof MismatchedInputException missing && missingRequiredProperty(missing) != null) {
			write(request, response, ErrorCode.VALIDATION_FAILED, "The request has invalid fields.",
					List.of(new FieldError(missingRequiredProperty(missing), FieldErrorCode.REQUIRED, "is required")));
			return;
		}
		if (cause instanceof InvalidFormatException invalidFormat && !path(invalidFormat).isEmpty()) {
			Class<?> target = invalidFormat.getTargetType();
			boolean enumeration = (target != null) && target.isEnum();
			write(request, response, ErrorCode.VALIDATION_FAILED, "The request has invalid fields.",
					List.of(new FieldError(path(invalidFormat),
							enumeration ? FieldErrorCode.INVALID_VALUE : FieldErrorCode.INVALID_FORMAT,
							enumeration ? "is not one of the allowed values" : "has an invalid format")));
			return;
		}
		if (cause instanceof MismatchedInputException mismatched && !path(mismatched).isEmpty()) {
			write(request, response, ErrorCode.VALIDATION_FAILED, "The request has invalid fields.",
					List.of(new FieldError(path(mismatched), FieldErrorCode.INVALID_TYPE, "has the wrong JSON type")));
			return;
		}
		String detail = (cause instanceof StreamReadException) ? "The request body is not valid JSON."
				: "The request body is missing or is not a JSON object.";
		write(request, response, ErrorCode.MALFORMED_REQUEST, detail, List.of());
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	void handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		Class<?> required = ex.getRequiredType();
		FieldErrorCode code;
		String message;
		if (required != null && required.isEnum()) {
			code = FieldErrorCode.INVALID_VALUE;
			message = "is not one of the allowed values";
		}
		else if (required == UUID.class || (required != null && java.time.temporal.Temporal.class.isAssignableFrom(required))) {
			code = FieldErrorCode.INVALID_FORMAT;
			message = "has an invalid format";
		}
		else {
			code = FieldErrorCode.INVALID_TYPE;
			message = "has the wrong type";
		}
		write(request, response, ErrorCode.VALIDATION_FAILED, "The request has invalid fields.",
				List.of(new FieldError(ex.getName(), code, message)));
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	void handleMissingParameter(MissingServletRequestParameterException ex, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		write(request, response, ErrorCode.VALIDATION_FAILED, "The request has invalid fields.",
				List.of(new FieldError(ex.getParameterName(), FieldErrorCode.REQUIRED, "is required")));
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	void handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		Map<String, String> headers = new LinkedHashMap<>();
		if (ex.getSupportedHttpMethods() != null && !ex.getSupportedHttpMethods().isEmpty()) {
			headers.put("Allow", ex.getSupportedHttpMethods()
				.stream()
				.map(Object::toString)
				.collect(Collectors.joining(", ")));
		}
		this.problemWriter.write(request, response, ErrorCode.METHOD_NOT_ALLOWED,
				"The HTTP method is not supported on this path.", List.of(), headers);
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	void handleMediaTypeNotSupported(HttpServletRequest request, HttpServletResponse response) throws IOException {
		write(request, response, ErrorCode.UNSUPPORTED_MEDIA_TYPE, "The request body must be application/json.",
				List.of());
	}

	@ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
	void handleNotAcceptable(HttpServletRequest request, HttpServletResponse response) throws IOException {
		write(request, response, ErrorCode.NOT_ACCEPTABLE, "The response is only available as JSON.", List.of());
	}

	@ExceptionHandler(NoResourceFoundException.class)
	void handleNoResource(HttpServletRequest request, HttpServletResponse response) throws IOException {
		write(request, response, ErrorCode.NOT_FOUND, "No resource exists at this path.", List.of());
	}

	/** Security exceptions are answered by the security filter chain (401 or 403), never here. */
	@ExceptionHandler({ AccessDeniedException.class, AuthenticationException.class })
	void rethrowSecurityException(RuntimeException ex) {
		throw ex;
	}

	@ExceptionHandler({ DataAccessResourceFailureException.class, CannotCreateTransactionException.class })
	void handleDatabaseUnavailable(Exception ex, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		log.error("Database unavailable", ex);
		write(request, response, ErrorCode.SERVICE_UNAVAILABLE, null, List.of());
	}

	@ExceptionHandler(Exception.class)
	void handleUnexpected(Exception ex, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		log.error("Unexpected error", ex);
		write(request, response, ErrorCode.INTERNAL_ERROR, null, List.of());
	}

	private void write(HttpServletRequest request, HttpServletResponse response, ErrorCode code, String detail,
			List<FieldError> errors) throws IOException {
		this.problemWriter.write(request, response, code, detail, errors, Map.of());
	}

	private static String path(JacksonException ex) {
		StringBuilder path = new StringBuilder();
		for (JacksonException.Reference reference : ex.getPath()) {
			if (reference.getPropertyName() != null) {
				if (!path.isEmpty()) {
					path.append('.');
				}
				path.append(reference.getPropertyName());
			}
			else if (reference.getIndex() >= 0) {
				path.append('[').append(reference.getIndex()).append(']');
			}
		}
		return path.toString();
	}

	/**
	 * The property a request omitted although its record component is {@code @JsonProperty(required = true)}, used
	 * where the contract requires a property that may be null. Jackson reports it only by message.
	 */
	private static String missingRequiredProperty(MismatchedInputException ex) {
		Matcher matcher = MISSING_REQUIRED_PROPERTY.matcher(String.valueOf(ex.getOriginalMessage()));
		return matcher.lookingAt() ? matcher.group(1) : null;
	}

	private static FieldErrorCode fieldCode(org.springframework.validation.FieldError fieldError) {
		String constraint = fieldError.getCode();
		if (constraint == null) {
			return FieldErrorCode.INVALID_VALUE;
		}
		return switch (constraint) {
			case "NotNull" -> FieldErrorCode.REQUIRED;
			case "Size", "TextLength" -> lengthCode(fieldError);
			case "Pattern" -> FieldErrorCode.INVALID_FORMAT;
			case "Min", "Max", "DecimalMin", "DecimalMax", "Positive", "PositiveOrZero" -> FieldErrorCode.OUT_OF_RANGE;
			case "Digits", "MaxDecimalPlaces" -> FieldErrorCode.PRECISION_EXCEEDED;
			case "Utf8MaxBytes" -> FieldErrorCode.TOO_LONG;
			default -> FieldErrorCode.INVALID_VALUE;
		};
	}

	/** A length violation is TOO_SHORT below the minimum and TOO_LONG otherwise. */
	private static FieldErrorCode lengthCode(org.springframework.validation.FieldError fieldError) {
		if (fieldError.contains(ConstraintViolation.class)) {
			ConstraintViolation<?> violation = fieldError.unwrap(ConstraintViolation.class);
			if (violation.getInvalidValue() instanceof CharSequence value) {
				int length = value.toString().codePointCount(0, value.length());
				Object annotation = violation.getConstraintDescriptor().getAnnotation();
				int min = (annotation instanceof Size size) ? size.min()
						: (annotation instanceof TextLength textLength) ? textLength.min() : 0;
				return (length < min) ? FieldErrorCode.TOO_SHORT : FieldErrorCode.TOO_LONG;
			}
		}
		return FieldErrorCode.INVALID_VALUE;
	}

	private static String message(org.springframework.validation.FieldError fieldError) {
		String message = fieldError.getDefaultMessage();
		return (message == null || message.isBlank()) ? "is invalid" : message;
	}

}
