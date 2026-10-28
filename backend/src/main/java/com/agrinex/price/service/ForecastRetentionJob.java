package com.agrinex.price.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agrinex.common.time.AgrinexCalendar;
import com.agrinex.price.repository.ForecastRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** The price module's daily purge: forecasts 90 days after their forecast date (ADR-0029 item 1). */
@Component
class ForecastRetentionJob {

	private static final Logger log = LoggerFactory.getLogger(ForecastRetentionJob.class);

	static final int FORECAST_RETENTION_DAYS = 90;

	private final ForecastRepository forecasts;

	private final AgrinexCalendar calendar;

	ForecastRetentionJob(ForecastRepository forecasts, AgrinexCalendar calendar) {
		this.forecasts = forecasts;
		this.calendar = calendar;
	}

	@Scheduled(cron = "${agrinex.retention.purge-cron}", zone = AgrinexCalendar.ZONE_ID)
	@Transactional
	public void purge() {
		int deleted = this.forecasts
			.deleteForecastDateBefore(this.calendar.today().minusDays(FORECAST_RETENTION_DAYS));
		log.atInfo().addKeyValue("forecastsDeleted", deleted).log("Forecast retention purge completed");
	}

}
