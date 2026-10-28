package com.agrinex.commodity.web;

import java.util.UUID;

import com.agrinex.commodity.CommodityApi;
import com.agrinex.commodity.web.dto.CommodityListResponse;
import com.agrinex.commodity.web.dto.CommodityResponse;
import com.agrinex.commodity.web.dto.NamedListResponse;
import com.agrinex.common.error.ApiException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Commodities, varieties and grades for the comparison wizard (contract tag Commodities; F1.4). Anonymous. */
@RestController
@RequestMapping("/api/v1/commodities")
class CommodityController {

	private final CommodityApi commodities;

	CommodityController(CommodityApi commodities) {
		this.commodities = commodities;
	}

	@GetMapping
	CommodityListResponse listCommodities() {
		return new CommodityListResponse(
				this.commodities.listOfferedCommodities().stream().map(CommodityResponse::from).toList());
	}

	@GetMapping("/{commodityId}/varieties")
	NamedListResponse listVarieties(@PathVariable UUID commodityId) {
		return new NamedListResponse(this.commodities.listVarieties(commodityId)
			.orElseThrow(() -> ApiException.notFound("The commodity does not exist.")));
	}

	@GetMapping("/{commodityId}/grades")
	NamedListResponse listGrades(@PathVariable UUID commodityId) {
		return new NamedListResponse(this.commodities.listGrades(commodityId)
			.orElseThrow(() -> ApiException.notFound("The commodity does not exist.")));
	}

}
