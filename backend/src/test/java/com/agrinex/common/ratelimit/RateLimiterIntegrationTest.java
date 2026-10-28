package com.agrinex.common.ratelimit;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.agrinex.IntegrationTestSupport;

import static org.assertj.core.api.Assertions.assertThat;

/** Fixed-window counting in Redis 7.4 (ADR-0017 item 6). */
class RateLimiterIntegrationTest extends IntegrationTestSupport {

	@Autowired
	RateLimiter limiter;

	@Test
	void requestsBeyondTheLimitAreRefusedWithARetryDelay() {
		String client = "synthetic-client-" + UUID.randomUUID();
		Duration window = Duration.ofMinutes(1);
		assertThat(this.limiter.hit("test", client, 2, window).allowed()).isTrue();
		assertThat(this.limiter.hit("test", client, 2, window).allowed()).isTrue();
		RateLimiter.Decision third = this.limiter.hit("test", client, 2, window);
		assertThat(third.allowed()).isFalse();
		assertThat(third.retryAfterSeconds()).isBetween(1L, 60L);
	}

	@Test
	void checkingDoesNotCount() {
		String client = "synthetic-client-" + UUID.randomUUID();
		Duration window = Duration.ofMinutes(1);
		this.limiter.hit("test", client, 1, window);
		assertThat(this.limiter.check("test", client, 1, window).allowed()).isFalse();
		assertThat(this.limiter.check("test", "synthetic-client-" + UUID.randomUUID(), 1, window).allowed()).isTrue();
	}

	@Test
	void eachClientHasItsOwnCounter() {
		Duration window = Duration.ofMinutes(1);
		assertThat(this.limiter.hit("test", "synthetic-client-" + UUID.randomUUID(), 1, window).allowed()).isTrue();
		assertThat(this.limiter.hit("test", "synthetic-client-" + UUID.randomUUID(), 1, window).allowed()).isTrue();
	}

}
