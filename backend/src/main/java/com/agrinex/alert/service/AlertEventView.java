package com.agrinex.alert.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.agrinex.alert.domain.AlertType;
import com.agrinex.commodity.CommodityInfo;
import com.agrinex.common.web.Ref;
import com.agrinex.price.PriceQuote;

/**
 * One alert event with the observation behind its trigger value, shown with its current values and provenance
 * (contract AlertEventResponse; ADR-0019 item 4).
 */
public record AlertEventView(UUID id, UUID alertId, AlertType alertType, BigDecimal thresholdValue,
		CommodityInfo commodity, Ref market, Instant triggeredAt, BigDecimal triggerValue, PriceQuote observation,
		Instant readAt) {
}
