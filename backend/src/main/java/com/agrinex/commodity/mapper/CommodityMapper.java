package com.agrinex.commodity.mapper;

import com.agrinex.commodity.CommodityInfo;
import com.agrinex.commodity.NamedRef;
import com.agrinex.commodity.domain.CommodityEntity;
import com.agrinex.commodity.domain.GradeEntity;
import com.agrinex.commodity.domain.VarietyEntity;

/** Maps commodity entities to the module's public records; entities never leave the module. */
public final class CommodityMapper {

	private CommodityMapper() {
	}

	public static CommodityInfo toInfo(CommodityEntity commodity) {
		return new CommodityInfo(commodity.getId(), commodity.getCanonicalName(), commodity.getNameHi(),
				commodity.getNameMr(), commodity.getCategory(), commodity.getPriceUnit(), commodity.getPriceUnitSource(),
				commodity.getCreatedAt());
	}

	public static NamedRef toRef(VarietyEntity variety) {
		return new NamedRef(variety.getId(), variety.getCommodityId(), variety.getCanonicalName());
	}

	public static NamedRef toRef(GradeEntity grade) {
		return new NamedRef(grade.getId(), grade.getCommodityId(), grade.getCanonicalName());
	}

}
