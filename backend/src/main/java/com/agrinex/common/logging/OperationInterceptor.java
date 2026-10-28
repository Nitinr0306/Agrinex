package com.agrinex.common.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Records the handler's operation id for the access log. Controller methods are named after the contract's
 * {@code operationId}, so the log names the operation exactly as {@code contracts/openapi.yaml} does.
 */
public class OperationInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (handler instanceof HandlerMethod handlerMethod) {
			request.setAttribute(CorrelationIdFilter.OPERATION_ATTRIBUTE, handlerMethod.getMethod().getName());
		}
		return true;
	}

}
