package com.agrinex.recommendation.engine;

import java.util.List;
import java.util.UUID;

/**
 * Ranked options with the two winners of ADR-0023 item 2. With no option both winners are null and the rankings
 * do not disagree (ADR-0023 item 3).
 */
public record Ranking(List<RankedOption> options, UUID priceOnlyWinnerId, UUID netRealizationWinnerId,
		boolean rankingsDisagree) {

	public static final Ranking EMPTY = new Ranking(List.of(), null, null, false);

	/** Whether every estimate of every option fits the stored precision (ADR-0040 item 1). */
	public boolean withinStoredPrecision() {
		return this.options.stream().allMatch((ranked) -> ranked.option().withinStoredPrecision());
	}

}
