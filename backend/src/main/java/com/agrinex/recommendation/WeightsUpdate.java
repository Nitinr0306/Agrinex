package com.agrinex.recommendation;

import java.math.BigDecimal;

/** A new weights version as an admin submits it (contract WeightsUpdateRequest; ADR-0020 item 1). */
public record WeightsUpdate(BigDecimal wNetRealization, BigDecimal wPrice, BigDecimal wTrend, BigDecimal wFreshness,
		BigDecimal wTransportPenalty, int freshnessFreshDays, int freshnessRecentDays, int freshnessStaleDays) {
}
