package com.agrinex.common.error;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Replaces Spring Boot's default error page and JSON: errors that reach the servlet container's error
 * dispatch are answered with the same {@link ApiErrorResponse}, never with framework details.
 */
@RestController
class FallbackErrorController implements ErrorController {

	private final ProblemWriter problemWriter;

	FallbackErrorController(ProblemWriter problemWriter) {
		this.problemWriter = problemWriter;
	}

	@RequestMapping("/error")
	void error(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		int code = databaseUnavailable(request.getAttribute(RequestDispatcher.ERROR_EXCEPTION)) ? 503
				: (status instanceof Integer value) ? value : 500;
		ErrorCode errorCode = switch (code) {
			case 400 -> ErrorCode.MALFORMED_REQUEST;
			case 401 -> ErrorCode.UNAUTHORIZED;
			case 403 -> ErrorCode.FORBIDDEN;
			case 404 -> ErrorCode.NOT_FOUND;
			case 405 -> ErrorCode.METHOD_NOT_ALLOWED;
			case 406 -> ErrorCode.NOT_ACCEPTABLE;
			case 415 -> ErrorCode.UNSUPPORTED_MEDIA_TYPE;
			case 503 -> ErrorCode.SERVICE_UNAVAILABLE;
			default -> ErrorCode.INTERNAL_ERROR;
		};
		this.problemWriter.write(request, response, errorCode, null, List.of(), Map.of());
	}

	/**
	 * Whether a failure that escaped the filter chain, such as resolving the caller's account, was caused by an
	 * unavailable database, which answers 503 (spec section 29).
	 */
	private static boolean databaseUnavailable(Object exception) {
		for (Throwable cause = (exception instanceof Throwable throwable) ? throwable : null; cause != null;
				cause = cause.getCause()) {
			if (cause instanceof DataAccessResourceFailureException
					|| cause instanceof CannotCreateTransactionException) {
				return true;
			}
		}
		return false;
	}

}
