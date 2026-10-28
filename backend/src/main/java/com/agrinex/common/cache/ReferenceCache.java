package com.agrinex.common.cache;

import java.time.Duration;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agrinex.common.logging.WarningThrottle;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * Programmatic access to the reference-data cache for code that must read, evict or clear entries itself
 * (ADR-0018 item 1: explicit eviction by the owning module on every write). Any cache failure is logged
 * as a throttled warning and the call falls through to the loader, so a Redis outage never fails a request.
 */
@Component
public class ReferenceCache {

	private static final Logger log = LoggerFactory.getLogger(ReferenceCache.class);

	private final CacheManager cacheManager;

	private final WarningThrottle warnings = new WarningThrottle(Duration.ofMinutes(1));

	public ReferenceCache(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String cacheName, Object key, Supplier<T> loader) {
		Cache cache = this.cacheManager.getCache(cacheName);
		if (cache != null) {
			try {
				Cache.ValueWrapper cached = cache.get(key);
				if (cached != null && cached.get() != null) {
					return (T) cached.get();
				}
			}
			catch (RuntimeException ex) {
				warn(cacheName);
				return loader.get();
			}
		}
		T value = loader.get();
		if (cache != null && value != null) {
			try {
				cache.put(key, value);
			}
			catch (RuntimeException ex) {
				warn(cacheName);
			}
		}
		return value;
	}

	public void evict(String cacheName, Object key) {
		Cache cache = this.cacheManager.getCache(cacheName);
		if (cache != null) {
			try {
				cache.evict(key);
			}
			catch (RuntimeException ex) {
				warn(cacheName);
			}
		}
	}

	public void clear(String cacheName) {
		Cache cache = this.cacheManager.getCache(cacheName);
		if (cache != null) {
			try {
				cache.clear();
			}
			catch (RuntimeException ex) {
				warn(cacheName);
			}
		}
	}

	private void warn(String cacheName) {
		if (this.warnings.shouldLog()) {
			log.warn("Reference-data cache unavailable (cache {}); reading from PostgreSQL", cacheName);
		}
	}

}
