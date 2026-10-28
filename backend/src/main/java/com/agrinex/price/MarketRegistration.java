package com.agrinex.price;

import java.util.UUID;

/** The market a source spelling resolves to, and whether this call registered it as PROVISIONAL (ADR-0014 item 4). */
public record MarketRegistration(UUID marketId, boolean registered) {
}
