package com.agrinex.common.web;

import com.agrinex.common.logging.OperationInterceptor;
import com.agrinex.common.ratelimit.RateLimitInterceptor;
import com.agrinex.common.ratelimit.RateLimitProperties;
import com.agrinex.common.ratelimit.RateLimiter;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RateLimitProperties.class)
class WebMvcConfiguration implements WebMvcConfigurer {

	private final RateLimiter rateLimiter;

	private final RateLimitProperties rateLimitProperties;

	WebMvcConfiguration(RateLimiter rateLimiter, RateLimitProperties rateLimitProperties) {
		this.rateLimiter = rateLimiter;
		this.rateLimitProperties = rateLimitProperties;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new OperationInterceptor());
		registry.addInterceptor(new RateLimitInterceptor(this.rateLimiter, this.rateLimitProperties));
	}

}
