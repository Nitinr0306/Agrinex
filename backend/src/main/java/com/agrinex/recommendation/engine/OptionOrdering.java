package com.agrinex.recommendation.engine;

import java.math.BigDecimal;
import java.util.Comparator;

/**
 * The deterministic orders of the engine. Market names compare in code-point order ({@link String#compareTo}),
 * and the market id breaks any remaining tie (contract RankingMethod, docs/api/API_CONVENTIONS.md section 5).
 */
final class OptionOrdering {

	/** Higher estimated net realization, then shorter distance, then market name, then market id. */
	static final Comparator<EvaluatedOption> BY_NET_REALIZATION = Comparator
		.comparing(EvaluatedOption::netRealization, Comparator.reverseOrder())
		.thenComparing(EvaluatedOption::distanceKm)
		.thenComparing(EvaluatedOption::marketName)
		.thenComparing(EvaluatedOption::marketId);

	/** The price-only winner: highest modal price, then the net-realization order (ADR-0023 item 2). */
	static final Comparator<EvaluatedOption> BY_PRICE = Comparator
		.comparing(EvaluatedOption::modalPrice, Comparator.reverseOrder())
		.thenComparing(BY_NET_REALIZATION);

	private OptionOrdering() {
	}

	/** Composite score descending, then the net-realization order (ADR-0024 item 5). */
	static Comparator<ScoredOption> byScore() {
		return Comparator.comparing(ScoredOption::score, Comparator.reverseOrder())
			.thenComparing(ScoredOption::option, BY_NET_REALIZATION);
	}

	/** An option with its composite score, while ranking. */
	record ScoredOption(EvaluatedOption option, BigDecimal score) {
	}

}
