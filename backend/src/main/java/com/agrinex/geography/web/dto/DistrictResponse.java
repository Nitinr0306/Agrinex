package com.agrinex.geography.web.dto;

import java.util.UUID;

import com.agrinex.common.web.GeoPoint;
import com.agrinex.geography.DistrictInfo;

/** Contract DistrictResponse: the centroid is the comparison origin the client sends back (ADR-0039). */
public record DistrictResponse(UUID id, UUID stateId, String name, GeoPoint centroid) {

	public static DistrictResponse from(DistrictInfo district) {
		return new DistrictResponse(district.id(), district.state().id(), district.name(),
				new GeoPoint(district.centroidLat(), district.centroidLon()));
	}

}
