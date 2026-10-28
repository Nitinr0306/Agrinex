package com.agrinex.alert.service;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agrinex.alert.repository.AlertEventRepository;
import com.agrinex.common.time.AgrinexCalendar;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** The alert module's daily purge: events older than 180 days (ADR-0029 item 1). */
@Component
class AlertRetentionJob {

	private static final Logger log = LoggerFactory.getLogger(AlertRetentionJob.class);

	static final Duration EVENT_RETENTION = Duration.ofDays(180);

	private final AlertEventRepository events;

	private final AgrinexCalendar calendar;

	AlertRetentionJob(AlertEventRepository events, AgrinexCalendar calendar) {
		this.events = events;
		this.calendar = calendar;
	}

	@Scheduled(cron = "${agrinex.retention.purge-cron}", zone = AgrinexCalendar.ZONE_ID)
	@Transactional
	public void purge() {
		int deleted = this.events.deleteTriggeredBefore(this.calendar.now().minus(EVENT_RETENTION));
		log.atInfo().addKeyValue("alertEventsDeleted", deleted).log("Alert retention purge completed");
	}

}
