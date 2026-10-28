package com.agrinex.geography;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * A district with its centroid (WGS84, 6 decimal places, from Survey of India boundaries; ADR-0014 item 2)
 * and its state.
 */
public record DistrictInfo(UUID id, String name, BigDecimal centroidLat, BigDecimal centroidLon, StateRef state)
		implements Serializable {

	public DistrictRef ref() {
		return new DistrictRef(this.id, this.name);
	}

}
