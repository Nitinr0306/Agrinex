package com.agrinex.admin.service;

import java.time.Instant;
import java.util.List;

import com.agrinex.common.time.AgrinexCalendar;
import com.agrinex.ingestion.IngestionApi;
import com.agrinex.ingestion.IngestionScheduleInfo;
import com.agrinex.price.PriceApi;
import com.agrinex.price.SourceDatasetInfo;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.health.actuate.endpoint.HealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.contributor.Status;
import org.springframework.stereotype.Service;

/**
 * What an ADMIN may view but never change at runtime (ADR-0020 items 2 and 3): database and Redis health from the
 * application's own health indicators, whether the government API key is configured (never its value), the
 * schedule, the coverage and the source datasets.
 */
@Service
public class SystemStatusService {

	private final ObjectProvider<HealthEndpoint> health;

	private final IngestionApi ingestion;

	private final PriceApi prices;

	private final AgrinexCalendar calendar;

	SystemStatusService(ObjectProvider<HealthEndpoint> health, IngestionApi ingestion, PriceApi prices, AgrinexCalendar calendar) {
		this.health = health;
		this.ingestion = ingestion;
		this.prices = prices;
		this.calendar = calendar;
	}

	public SystemStatus status() {
		return new SystemStatus(this.calendar.now(), component("db"), component("redis"),
				this.ingestion.scheduleInfo(), this.prices.sourceDatasets());
	}

	/** UP, DOWN or UNKNOWN (contract ComponentHealth); OUT_OF_SERVICE counts as DOWN. */
	private ComponentHealth component(String name) {
		HealthEndpoint endpoint = this.health.getIfAvailable();
		HealthDescriptor descriptor = (endpoint != null) ? endpoint.healthForPath(name) : null;
		if (descriptor == null) {
			return ComponentHealth.UNKNOWN;
		}
		Status status = descriptor.getStatus();
		if (Status.UP.equals(status)) {
			return ComponentHealth.UP;
		}
		return (Status.DOWN.equals(status) || Status.OUT_OF_SERVICE.equals(status)) ? ComponentHealth.DOWN
				: ComponentHealth.UNKNOWN;
	}

	/** Contract ComponentHealth. */
	public enum ComponentHealth {

		UP, DOWN, UNKNOWN

	}

	/** Contract SystemStatusResponse, before mapping. */
	public record SystemStatus(Instant generatedAt, ComponentHealth database, ComponentHealth redis,
			IngestionScheduleInfo schedule, List<SourceDatasetInfo> datasets) {
	}

}
