package com.agrinex.commodity;

import java.util.Set;
import java.util.UUID;

/**
 * Read-only port declared by the commodity module and implemented by the price module (ADR-0035 item 1c):
 * the commodities with at least one observation dated within the active stale window at a VERIFIED, active
 * market with coordinates, that is, the commodities a comparison can currently find data for.
 */
public interface CommodityAvailabilityPort {

	Set<UUID> commoditiesWithRecentObservations();

}
