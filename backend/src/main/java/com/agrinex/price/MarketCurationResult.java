package com.agrinex.price;

import java.util.Map;

/**
 * A curated market with the masked before/after details its admin audit event records (ADR-0020 item 6). For a
 * merge the details include the aliases moved and the values of any dropped observation copy (ADR-0014 item 4).
 */
public record MarketCurationResult(MarketInfo market, Map<String, Object> auditDetails) {
}
