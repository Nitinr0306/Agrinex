package com.agrinex.alert.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.agrinex.alert.domain.AlertStatus;
import com.agrinex.alert.domain.AlertType;
import com.agrinex.commodity.CommodityInfo;
import com.agrinex.common.web.Ref;

/** A price alert with its commodity and optional market (spec section 30 AlertResponse). */
public record AlertView(UUID id, AlertType alertType, AlertStatus status, CommodityInfo commodity, Ref market,
		BigDecimal thresholdValue, Instant lastTriggeredAt) {
}
