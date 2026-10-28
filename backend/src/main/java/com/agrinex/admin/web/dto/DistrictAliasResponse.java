package com.agrinex.admin.web.dto;

import java.time.Instant;
import java.util.UUID;

import com.agrinex.geography.DistrictAliasView;
import com.agrinex.geography.DistrictRef;

/** Contract DistrictAliasResponse (ADR-0014 item 3). */
public record DistrictAliasResponse(UUID id, String sourceStateName, String sourceDistrictName, DistrictRef district,
		String evidenceNote, Instant createdAt) {

	public static DistrictAliasResponse from(DistrictAliasView alias) {
		return new DistrictAliasResponse(alias.id(), alias.sourceStateName(), alias.sourceDistrictName(),
				alias.district(), alias.evidenceNote(), alias.createdAt());
	}

}
