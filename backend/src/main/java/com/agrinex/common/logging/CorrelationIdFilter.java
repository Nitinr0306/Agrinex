package com.agrinex.common.logging;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Gives every request a server-generated correlation id, returned in {@code X-Correlation-Id} and written to
 * the structured logs (spec section 27). A client-supplied value is ignored. After the request it writes one
 * access-log event with the method, path (never the query string), operation, status and duration. Request
 * and response bodies, headers and personal data are never logged (ADR-0042, SR-16).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

	/** Request attribute holding the contract operation id of the handler, set by {@link OperationInterceptor}. */
	public static final String OPERATION_ATTRIBUTE = CorrelationIdFilter.class.getName() + ".operation";

	private static final Logger accessLog = LoggerFactory.getLogger("agrinex.access");

	@Override
	protected boolean shouldNotFilterAsyncDispatch() {
		return false;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String correlationId = UUID.randomUUID().toString();
		request.setAttribute(CorrelationId.REQUEST_ATTRIBUTE, correlationId);
		response.setHeader(CorrelationId.HEADER, correlationId);
		MDC.put(CorrelationId.MDC_KEY, correlationId);
		long start = System.nanoTime();
		try {
			chain.doFilter(request, response);
		}
		finally {
			if (!request.getRequestURI().startsWith("/actuator/health")) {
				accessLog.atInfo()
					.addKeyValue("http.request.method", request.getMethod())
					.addKeyValue("url.path", request.getRequestURI())
					.addKeyValue("operation", request.getAttribute(OPERATION_ATTRIBUTE))
					.addKeyValue("http.response.status_code", response.getStatus())
					.addKeyValue("duration_ms", (System.nanoTime() - start) / 1_000_000)
					.log("request completed");
			}
			MDC.remove(CorrelationId.MDC_KEY);
		}
	}

}
