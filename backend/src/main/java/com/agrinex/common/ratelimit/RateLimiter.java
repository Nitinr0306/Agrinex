package com.agrinex.common.ratelimit;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agrinex.common.logging.WarningThrottle;
import com.agrinex.common.time.AgrinexCalendar;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

/**
 * Fixed-window counters (ADR-0017 item 6). Counters live in Redis ({@code INCR} with expiry). When Redis is
 * unavailable the same limits are applied by an in-process limiter, so abuse is neither let through nor the
 * service shut down (ADR-0018 item 4). Counter keys are SHA-256 hashes of the limited value (client IP,
 * identifier or account), so no personal data is stored in Redis.
 */
@Component
public class RateLimiter {

	private static final Logger log = LoggerFactory.getLogger(RateLimiter.class);

	private static final RedisScript<Long> INCREMENT = RedisScript.of("""
			local count = redis.call('INCR', KEYS[1])
			if count == 1 then redis.call('PEXPIRE', KEYS[1], ARGV[1]) end
			return count
			""", Long.class);

	private final StringRedisTemplate redis;

	private final AgrinexCalendar calendar;

	private final Map<String, AtomicLong> fallbackCounters = new ConcurrentHashMap<>();

	private final WarningThrottle fallbackWarnings = new WarningThrottle(Duration.ofMinutes(1));

	public RateLimiter(StringRedisTemplate redis, AgrinexCalendar calendar) {
		this.redis = redis;
		this.calendar = calendar;
	}

	/** Counts one request and tells whether it is within {@code limit} for the current window. */
	public Decision hit(String bucket, String limitedValue, int limit, Duration window) {
		Window current = window(window);
		String key = key(bucket, limitedValue, current);
		long count;
		try {
			Long value = this.redis.execute(INCREMENT, List.of(key), Long.toString(window.toMillis()));
			count = (value != null) ? value : 0;
		}
		catch (DataAccessException ex) {
			count = fallbackIncrement(key, current);
		}
		return new Decision(count <= limit, current.secondsLeft());
	}

	/** Tells whether the counter is already at or over {@code limit}, without counting this request. */
	public Decision check(String bucket, String limitedValue, int limit, Duration window) {
		Window current = window(window);
		String key = key(bucket, limitedValue, current);
		long count;
		try {
			String value = this.redis.opsForValue().get(key);
			count = (value != null) ? Long.parseLong(value) : 0;
		}
		catch (DataAccessException ex) {
			warnFallback();
			AtomicLong counter = this.fallbackCounters.get(key);
			count = (counter != null) ? counter.get() : 0;
		}
		return new Decision(count < limit, current.secondsLeft());
	}

	private long fallbackIncrement(String key, Window current) {
		warnFallback();
		if (this.fallbackCounters.size() > 10_000) {
			this.fallbackCounters.keySet().removeIf((existing) -> !existing.endsWith(":" + current.index()));
		}
		return this.fallbackCounters.computeIfAbsent(key, (k) -> new AtomicLong()).incrementAndGet();
	}

	private void warnFallback() {
		if (this.fallbackWarnings.shouldLog()) {
			log.warn("Redis unavailable; rate limiting uses the in-process fallback with the same limits");
		}
	}

	private Window window(Duration window) {
		long now = this.calendar.now().toEpochMilli();
		long size = window.toMillis();
		long index = now / size;
		long end = (index + 1) * size;
		return new Window(index, Math.max(1, (end - now + 999) / 1000));
	}

	private static String key(String bucket, String limitedValue, Window window) {
		return "agrinex:rl:" + bucket + ":" + sha256(limitedValue) + ":" + window.index();
	}

	private static String sha256(String value) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		}
		catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private record Window(long index, long secondsLeft) {
	}

	/** Whether the request is allowed, and the seconds until the current window ends (the Retry-After value). */
	public record Decision(boolean allowed, long retryAfterSeconds) {
	}

}
