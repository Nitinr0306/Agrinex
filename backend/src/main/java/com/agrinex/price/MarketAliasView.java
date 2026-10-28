package com.agrinex.price;

import java.time.Instant;

/** One source spelling mapped to a market, with its evidence (contract MarketSourceAlias; spec section 47 Q4). */
public record MarketAliasView(String sourceStateName, String sourceDistrictName, String sourceMarketName,
		String sourceMarketNameRaw, String evidenceNote, Instant createdAt) {
}
