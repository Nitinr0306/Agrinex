package com.agrinex.alert.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.agrinex.alert.domain.AlertType;
import com.agrinex.alert.service.AlertEventView;
import com.agrinex.commodity.CommodityRef;
import com.agrinex.common.web.Ref;
import com.agrinex.price.PriceQuote;

/** Contract AlertEventResponse (ADR-0019 item 4). */
public record AlertEventResponse(UUID id, UUID alertId, AlertType alertType, BigDecimal thresholdValue,
		CommodityRef commodity, Ref market, Instant triggeredAt, BigDecimal triggerValue, String priceUnit,
		PriceQuote observation, Instant readAt) {

	public static AlertEventResponse from(AlertEventView view) {
		return new AlertEventResponse(view.id(), view.alertId(), view.alertType(), view.thresholdValue(),
				view.commodity().ref(), view.market(), view.triggeredAt(), view.triggerValue(),
				view.commodity().priceUnit(), view.observation(), view.readAt());
	}

}
