package com.agrinex.geography;

import java.time.Instant;
import java.util.UUID;

/** A district source alias with its evidence (contract DistrictAliasResponse). */
public record DistrictAliasView(UUID id, String sourceStateName, String sourceDistrictName, DistrictRef district,
		String evidenceNote, Instant createdAt) {
}
