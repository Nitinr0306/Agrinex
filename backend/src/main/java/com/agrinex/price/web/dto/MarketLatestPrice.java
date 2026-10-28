package com.agrinex.price.web.dto;

import com.agrinex.commodity.CommodityRef;
import com.agrinex.price.PriceQuote;

/** Contract MarketLatestPrice: the latest reported price of one commodity at the market. */
public record MarketLatestPrice(CommodityRef commodity, PriceQuote price) {
}
