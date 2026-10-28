package com.agrinex.admin.web.dto;

import java.time.Instant;
import java.util.UUID;

import com.agrinex.commodity.CommodityInfo;

/** Contract AdminCommodityResponse: a commodity of the known list with its unit evidence (ADR-0006, ADR-0008). */
public record AdminCommodityResponse(UUID id, String name, String nameHi, String nameMr, String category,
		String priceUnit, String priceUnitSource, Instant createdAt) {

	public static AdminCommodityResponse from(CommodityInfo commodity) {
		return new AdminCommodityResponse(commodity.id(), commodity.name(), commodity.nameHi(), commodity.nameMr(),
				commodity.category(), commodity.priceUnit(), commodity.priceUnitSource(), commodity.createdAt());
	}

}
