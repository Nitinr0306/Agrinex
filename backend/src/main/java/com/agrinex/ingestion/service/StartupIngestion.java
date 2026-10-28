package com.agrinex.ingestion.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agrinex.common.scheduling.SchedulingConfiguration;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

/**
 * At application start (ADR-0011 items 2 and 5): runs left RUNNING by a stopped process are marked FAILED with
 * RUN_INTERRUPTED, then the STARTUP run begins when no CURRENT run loaded data in the previous 6 hours. Only the
 * web application does this, never a one-off operator command, and only while scheduling is enabled. A failure
 * here is logged and never stops the application.
 */
@Component
@ConditionalOnWebApplication
@ConditionalOnProperty(name = SchedulingConfiguration.ENABLED_PROPERTY, havingValue = "true", matchIfMissing = true)
class StartupIngestion implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(StartupIngestion.class);

	private final RunLifecycle lifecycle;

	private final IngestionControlService control;

	StartupIngestion(RunLifecycle lifecycle, IngestionControlService control) {
		this.lifecycle = lifecycle;
		this.control = control;
	}

	@Override
	public void run(ApplicationArguments args) {
		try {
			int interrupted = this.lifecycle.interruptRunning();
			if (interrupted > 0) {
				log.atWarn().addKeyValue("runs", interrupted).log("Interrupted ingestion runs marked FAILED");
			}
			this.control.startStartupRun();
		}
		catch (RuntimeException ex) {
			log.atError().setCause(ex).log("Startup ingestion could not start");
		}
	}

}
