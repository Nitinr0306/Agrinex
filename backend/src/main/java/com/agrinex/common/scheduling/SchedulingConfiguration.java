package com.agrinex.common.scheduling;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables the in-process scheduler (spec section 12, 15): the ingestion schedule (ADR-0011) and the daily
 * retention purges (ADR-0029). {@code agrinex.scheduling.enabled=false} turns every automatic job off, including
 * the startup ingestion run, for automated tests and one-off operator commands.
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
@ConditionalOnProperty(name = SchedulingConfiguration.ENABLED_PROPERTY, havingValue = "true", matchIfMissing = true)
public class SchedulingConfiguration {

	public static final String ENABLED_PROPERTY = "agrinex.scheduling.enabled";

}
