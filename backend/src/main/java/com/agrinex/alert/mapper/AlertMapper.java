package com.agrinex.alert.mapper;

import com.agrinex.alert.domain.AlertEventEntity;
import com.agrinex.alert.domain.PriceAlertEntity;
import com.agrinex.alert.service.AlertEventView;
import com.agrinex.alert.service.AlertView;
import com.agrinex.commodity.CommodityInfo;
import com.agrinex.common.web.Ref;
import com.agrinex.price.PriceQuote;

/** Maps alert entities, with their resolved references, to the views the web layer returns. */
public final class AlertMapper {

	private AlertMapper() {
	}

	public static AlertView toView(PriceAlertEntity alert, CommodityInfo commodity, Ref market) {
		return new AlertView(alert.getId(), alert.getAlertType(), alert.getStatus(), commodity, market,
				alert.getThresholdValue(), alert.getLastTriggeredAt());
	}

	public static AlertEventView toView(AlertEventEntity event, PriceAlertEntity alert, CommodityInfo commodity,
			Ref market, PriceQuote observation) {
		return new AlertEventView(event.getId(), alert.getId(), alert.getAlertType(), alert.getThresholdValue(),
				commodity, market, event.getTriggeredAt(), event.getTriggerValue(), observation, event.getReadAt());
	}

}
