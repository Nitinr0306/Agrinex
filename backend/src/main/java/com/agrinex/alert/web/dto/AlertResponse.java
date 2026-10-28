package com.agrinex.alert.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.agrinex.alert.domain.AlertStatus;
import com.agrinex.alert.domain.AlertType;
import com.agrinex.alert.service.AlertView;
import com.agrinex.commodity.CommodityRef;
import com.agrinex.common.web.Ref;

/** Contract AlertResponse (spec section 30). {@code priceUnit} is the commodity's verified unit. */
public record AlertResponse(UUID id, AlertType alertType, AlertStatus status, CommodityRef commodity, Ref market,
		BigDecimal thresholdValue, String priceUnit, Instant lastTriggeredAt) {

	public static AlertResponse from(AlertView view) {
		return new AlertResponse(view.id(), view.alertType(), view.status(), view.commodity().ref(), view.market(),
				view.thresholdValue(), view.commodity().priceUnit(), view.lastTriggeredAt());
	}

}
