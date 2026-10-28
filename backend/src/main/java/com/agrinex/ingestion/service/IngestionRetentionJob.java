package com.agrinex.ingestion.service;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agrinex.common.time.AgrinexCalendar;
import com.agrinex.ingestion.repository.IngestionErrorRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** The ingestion module's daily purge: error rows and raw payloads older than 180 days (ADR-0029 item 1). */
@Component
class IngestionRetentionJob {

	private static final Logger log = LoggerFactory.getLogger(IngestionRetentionJob.class);

	static final Duration ERROR_RETENTION = Duration.ofDays(180);

	private final IngestionErrorRepository errors;

	private final AgrinexCalendar calendar;

	IngestionRetentionJob(IngestionErrorRepository errors, AgrinexCalendar calendar) {
		this.errors = errors;
		this.calendar = calendar;
	}

	@Scheduled(cron = "${agrinex.retention.purge-cron}", zone = AgrinexCalendar.ZONE_ID)
	@Transactional
	public void purge() {
		int deleted = this.errors.deleteCreatedBefore(this.calendar.now().minus(ERROR_RETENTION));
		log.atInfo().addKeyValue("ingestionErrorsDeleted", deleted).log("Ingestion retention purge completed");
	}

}
