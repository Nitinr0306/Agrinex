package com.agrinex.common.logging;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.MDC;

/** Access to the server-generated correlation id of the current request (spec section 27). */
public final class CorrelationId {

	public static final String HEADER = "X-Correlation-Id";

	public static final String MDC_KEY = "correlationId";

	static final String REQUEST_ATTRIBUTE = CorrelationId.class.getName();

	private CorrelationId() {
	}

	public static String of(HttpServletRequest request) {
		Object value = request.getAttribute(REQUEST_ATTRIBUTE);
		return (value != null) ? value.toString() : current();
	}

	/** The correlation id bound to the current thread, or {@code null} outside a request or job. */
	public static String current() {
		return MDC.get(MDC_KEY);
	}

}
