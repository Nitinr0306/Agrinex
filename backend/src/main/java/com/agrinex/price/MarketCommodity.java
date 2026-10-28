package com.agrinex.price;

import java.util.UUID;

/** A market and commodity pair. */
public record MarketCommodity(UUID marketId, UUID commodityId) {
}
