package com.agrinex.ingestion.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * The executor that runs manual and startup ingestion runs in the background (ADR-0011 items 2 and 3). At most one
 * run per dataset is RUNNING (uq_ingestion_runs_one_running), so two threads are enough for the two datasets.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ IngestionProperties.class, HistoricalMaintenanceProperties.class })
public class IngestionConfiguration {

	/** Name of the executor bean that runs background ingestion runs. */
	public static final String INGESTION_EXECUTOR = "ingestionExecutor";

	private static final int RUN_THREADS = 2;

	private static final int QUEUE_CAPACITY = 2;

	@Bean(name = INGESTION_EXECUTOR)
	ThreadPoolTaskExecutor ingestionExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setThreadNamePrefix("agrinex-ingestion-");
		executor.setCorePoolSize(RUN_THREADS);
		executor.setMaxPoolSize(RUN_THREADS);
		executor.setQueueCapacity(QUEUE_CAPACITY);
		return executor;
	}

}
