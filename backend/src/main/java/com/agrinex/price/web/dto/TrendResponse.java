package com.agrinex.price.web.dto;

import java.math.BigDecimal;
import java.util.List;

import com.agrinex.commodity.CommodityRef;
import com.agrinex.common.web.Ref;
import com.agrinex.price.PriceQuote;
import com.agrinex.price.SourceSyncStatus;
import com.agrinex.price.TrendDirection;
import com.agrinex.price.service.MarketQueryService.TrendView;

/** Contract TrendResponse (spec section 33; ADR-0025). The change is displayed rounded to 1 decimal place. */
public record TrendResponse(Ref market, CommodityRef commodity, Ref variety, Ref grade, String priceUnit, int days,
		TrendWindow window, TrendDirection trendDirection, BigDecimal changePercent7Day,
		List<PriceQuote> observations, SourceSyncStatus sourceStatus) {

	public static TrendResponse from(TrendView view) {
		TrendWindow window = new TrendWindow(view.window().chartFrom(), view.window().chartTo(),
				view.window().recentFrom(), view.window().recentTo(), view.window().baselineFrom(),
				view.window().baselineTo());
		return new TrendResponse(view.market(), view.commodity().ref(), view.variety(), view.grade(),
				view.commodity().priceUnit(), view.days(), window, view.statistic().direction(),
				view.statistic().displayChangePercent(), view.observations(), view.sourceStatus());
	}

}
