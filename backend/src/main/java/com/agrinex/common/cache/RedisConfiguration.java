package com.agrinex.common.cache;

import java.time.Duration;

import io.lettuce.core.ClientOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agrinex.common.logging.WarningThrottle;

import org.springframework.boot.data.redis.autoconfigure.LettuceClientOptionsBuilderCustomizer;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redis is used only for rate-limit counters and a cache of public reference reads (ADR-0018; departure D-09).
 * When Redis is unavailable, commands are rejected at once instead of being queued, cache reads and writes
 * fall through to PostgreSQL with a warning, and rate limiting switches to its in-process fallback. A Redis
 * outage therefore degrades the service but never stops it.
 */
@Configuration(proxyBeanMethods = false)
@EnableCaching
public class RedisConfiguration implements CachingConfigurer {

	private static final Logger log = LoggerFactory.getLogger(RedisConfiguration.class);

	private final WarningThrottle cacheWarnings = new WarningThrottle(Duration.ofMinutes(1));

	@Bean
	LettuceClientOptionsBuilderCustomizer failFastWhenDisconnected() {
		return (builder) -> builder.autoReconnect(true)
			.disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS);
	}

	@Override
	public CacheErrorHandler errorHandler() {
		return new CacheErrorHandler() {

			@Override
			public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
				warn(cache);
			}

			@Override
			public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
				warn(cache);
			}

			@Override
			public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
				warn(cache);
			}

			@Override
			public void handleCacheClearError(RuntimeException exception, Cache cache) {
				warn(cache);
			}

		};
	}

	private void warn(Cache cache) {
		if (this.cacheWarnings.shouldLog()) {
			log.warn("Reference-data cache unavailable (cache {}); reading from PostgreSQL", cache.getName());
		}
	}

}
