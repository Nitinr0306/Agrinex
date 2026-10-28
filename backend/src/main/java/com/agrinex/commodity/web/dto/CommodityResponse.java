package com.agrinex.commodity.web.dto;

import java.util.UUID;

import com.agrinex.commodity.CommodityInfo;

/** Contract CommodityResponse: the unit is never converted (ADR-0008); localized names fall back to name (ADR-0015). */
public record CommodityResponse(UUID id, String name, String nameHi, String nameMr, String category, String priceUnit) {

	public static CommodityResponse from(CommodityInfo commodity) {
		return new CommodityResponse(commodity.id(), commodity.name(), commodity.nameHi(), commodity.nameMr(),
				commodity.category(), commodity.priceUnit());
	}

}
