package com.agrinex.recommendation.engine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.agrinex.price.FreshnessStatus;
import com.agrinex.price.TrendDirection;
import com.agrinex.recommendation.domain.ReasonCode;

/**
 * The spec section 16 reason-code triggers, with "top 25 %" and "bottom 25 %" as ADR-0024 item 6 defines them
 * over the N eligible options: PRICE_ADVANTAGE when the modal is at least the modal at position ceil(N/4) in
 * descending order; LOWER_TRANSPORT when the cost is at most the cost at position ceil(N/4) in ascending order.
 */
final class ReasonCodeRules {

	/** SUITABLE_DISTANCE applies below this distance (spec section 16: distance &lt; 50 km). */
	static final BigDecimal SUITABLE_DISTANCE_KM = BigDecimal.valueOf(50);

	private final BigDecimal priceAdvantageThreshold;

	private final BigDecimal lowerTransportThreshold;

	private ReasonCodeRules(BigDecimal priceAdvantageThreshold, BigDecimal lowerTransportThreshold) {
		this.priceAdvantageThreshold = priceAdvantageThreshold;
		this.lowerTransportThreshold = lowerTransportThreshold;
	}

	/** The thresholds of one result; {@code options} is not empty. */
	static ReasonCodeRules over(List<EvaluatedOption> options) {
		int position = (options.size() + 3) / 4;
		BigDecimal price = options.stream()
			.map(EvaluatedOption::modalPrice)
			.sorted(Comparator.reverseOrder())
			.toList()
			.get(position - 1);
		BigDecimal transport = options.stream()
			.map(EvaluatedOption::transportCost)
			.sorted()
			.toList()
			.get(position - 1);
		return new ReasonCodeRules(price, transport);
	}

	/** The codes that apply to one option, in the order of {@link ReasonCode}. */
	List<ReasonCode> codesFor(EvaluatedOption option) {
		List<ReasonCode> codes = new ArrayList<>();
		if (option.modalPrice().compareTo(this.priceAdvantageThreshold) >= 0) {
			codes.add(ReasonCode.PRICE_ADVANTAGE);
		}
		if (option.transportCost().compareTo(this.lowerTransportThreshold) <= 0) {
			codes.add(ReasonCode.LOWER_TRANSPORT);
		}
		TrendDirection trend = option.candidate().trend().direction();
		if (trend == TrendDirection.RISING) {
			codes.add(ReasonCode.POSITIVE_TREND);
		}
		if (trend == TrendDirection.STABLE) {
			codes.add(ReasonCode.STABLE_TREND);
		}
		if (option.freshness() == FreshnessStatus.FRESH) {
			codes.add(ReasonCode.RECENT_DATA);
		}
		if (option.distanceKm().compareTo(SUITABLE_DISTANCE_KM) < 0) {
			codes.add(ReasonCode.SUITABLE_DISTANCE);
		}
		if (trend == TrendDirection.FALLING) {
			codes.add(ReasonCode.NEGATIVE_TREND);
		}
		if (option.freshness() == FreshnessStatus.STALE) {
			codes.add(ReasonCode.STALE_DATA);
		}
		return List.copyOf(codes);
	}

}
