package com.agrinex.common.ratelimit;

import java.time.Duration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.agrinex.common.error.ApiException;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Enforces {@link RateLimited} per client IP. The client IP is the request's remote address, which the native
 * forward-headers strategy resolves from {@code X-Forwarded-For} only behind a trusted proxy (ADR-0017 item 5).
 */
public class RateLimitInterceptor implements HandlerInterceptor {

	private static final Duration MINUTE = Duration.ofMinutes(1);

	private final RateLimiter rateLimiter;

	private final RateLimitProperties properties;

	public RateLimitInterceptor(RateLimiter rateLimiter, RateLimitProperties properties) {
		this.rateLimiter = rateLimiter;
		this.properties = properties;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (handler instanceof HandlerMethod handlerMethod) {
			RateLimited rateLimited = handlerMethod.getMethodAnnotation(RateLimited.class);
			if (rateLimited != null) {
				RateLimited.Bucket bucket = rateLimited.value();
				RateLimiter.Decision decision = this.rateLimiter.hit("ip:" + bucket.name(), request.getRemoteAddr(),
						bucket.limit(this.properties), MINUTE);
				if (!decision.allowed()) {
					throw ApiException.rateLimited(decision.retryAfterSeconds());
				}
			}
		}
		return true;
	}

}
