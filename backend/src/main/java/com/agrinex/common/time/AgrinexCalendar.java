package com.agrinex.common.time;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.stereotype.Component;

/**
 * The reference calendar of AgriNex. "Today" is always the Asia/Kolkata date, computed explicitly and
 * never from the server's default time zone (ADR-0007 item 3). Instants stay in UTC.
 */
@Component
public class AgrinexCalendar {

	/** The zone id as a constant expression, for annotations such as {@code @Scheduled(zone = ...)}. */
	public static final String ZONE_ID = "Asia/Kolkata";

	public static final ZoneId ZONE = ZoneId.of(ZONE_ID);

	private final Clock clock;

	public AgrinexCalendar(Clock clock) {
		this.clock = clock;
	}

	public LocalDate today() {
		return LocalDate.now(this.clock.withZone(ZONE));
	}

	public Instant now() {
		return this.clock.instant();
	}

}
