package com.agrinex.recommendation.service;

import java.time.Instant;
import java.util.UUID;

import com.agrinex.price.SourceSyncStatus;
import com.agrinex.recommendation.domain.ScenarioKind;
import com.agrinex.transport.TransportAssumptions;

/**
 * A comparison or a simulation, as the selling-options operations return it. {@code overrides} is null for a
 * comparison; {@code transportVersion} is the configured version whose minimum charge and capacity were applied
 * (ADR-0026 item 1).
 */
public record ComparisonResult(UUID recommendationId, Instant generatedAt, ScenarioKind scenario,
		SimulationOverrides overrides, ComparisonInputs inputs, TransportAssumptions transportVersion,
		ComparisonOutcome outcome, SourceSyncStatus sourceStatus) {

	public boolean persisted() {
		return this.scenario == ScenarioKind.COMPARISON;
	}

	/** Whether the simulation supplied its own rate (contract TransportAssumptionsApplied.ratePerKmSource). */
	public boolean rateOverridden() {
		return this.overrides != null && this.overrides.transportRatePerKm() != null;
	}

}
