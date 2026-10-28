package com.agrinex.common.logging;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/** Limits a repeated warning to one log event per interval, so a Redis outage does not flood the logs. */
public final class WarningThrottle {

	private final long intervalNanos;

	private final AtomicLong lastLogged = new AtomicLong(Long.MIN_VALUE);

	public WarningThrottle(Duration interval) {
		this.intervalNanos = interval.toNanos();
	}

	public boolean shouldLog() {
		long now = System.nanoTime();
		long last = this.lastLogged.get();
		return (last == Long.MIN_VALUE || now - last >= this.intervalNanos) && this.lastLogged.compareAndSet(last, now);
	}

}
