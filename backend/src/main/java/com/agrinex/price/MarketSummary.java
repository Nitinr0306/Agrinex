package com.agrinex.price;

import java.util.UUID;

import com.agrinex.geography.DistrictRef;
import com.agrinex.geography.StateRef;

/** A market with its district and state (contract common.schema.json MarketSummary). */
public record MarketSummary(UUID id, String name, DistrictRef district, StateRef state) {
}
