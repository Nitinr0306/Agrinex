package com.agrinex.geography.mapper;

import com.agrinex.geography.DistrictAliasView;
import com.agrinex.geography.DistrictInfo;
import com.agrinex.geography.LocalityInfo;
import com.agrinex.geography.StateRef;
import com.agrinex.geography.domain.DistrictEntity;
import com.agrinex.geography.domain.DistrictSourceAliasEntity;
import com.agrinex.geography.domain.LocalityEntity;
import com.agrinex.geography.domain.StateEntity;

/** Maps geography entities to the module's public records; entities never leave the module. */
public final class GeographyMapper {

	private GeographyMapper() {
	}

	public static StateRef toRef(StateEntity state) {
		return new StateRef(state.getId(), state.getCode(), state.getName(), state.getNameHi(), state.getNameMr());
	}

	public static DistrictInfo toInfo(DistrictEntity district) {
		return new DistrictInfo(district.getId(), district.getName(), district.getCentroidLat(),
				district.getCentroidLon(), toRef(district.getState()));
	}

	public static LocalityInfo toInfo(LocalityEntity locality) {
		return new LocalityInfo(locality.getId(), locality.getDistrictId(), locality.getName());
	}

	public static DistrictAliasView toView(DistrictSourceAliasEntity alias) {
		return new DistrictAliasView(alias.getId(), alias.getSourceStateName(), alias.getSourceDistrictName(),
				toInfo(alias.getDistrict()).ref(), alias.getEvidenceNote(), alias.getCreatedAt());
	}

}
