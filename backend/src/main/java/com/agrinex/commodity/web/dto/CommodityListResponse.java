package com.agrinex.commodity.web.dto;

import java.util.List;

/** Contract CommodityListResponse. */
public record CommodityListResponse(List<CommodityResponse> items) {
}
