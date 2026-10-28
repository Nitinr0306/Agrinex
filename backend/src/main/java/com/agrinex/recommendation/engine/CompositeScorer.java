package com.agrinex.recommendation.engine;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import com.agrinex.price.FreshnessStatus;
import com.agrinex.price.TrendDirection;

/**
 * The spec section 16 composite score with the rules of ADR-0024:
 * <pre>
 * score = wNet x normalizedNet + wPrice x normalizedPrice + wTrend x trendScore
 *       + wFreshness x freshnessScore - wTransport x normalizedTransport
 * </pre>
 * Each normalized value is min-max over the eligible candidates; when a factor's maximum equals its minimum
 * every candidate gets normalizedNet = normalizedPrice = 1 and normalizedTransport = 0 (ADR-0024 item 4). The
 * arithmetic is exact decimal with 34 significant digits for the divisions, and the score is rounded half-up to
 * the 6 decimal places that {@code selling_options.score} stores; ranks use that stored value, so a recorded run
 * can be re-ranked from its own rows.
 */
final class CompositeScorer {

	static final int SCORE_SCALE = 6;

	private static final MathContext DIVISION = MathContext.DECIMAL128;

	private CompositeScorer() {
	}

	/** The scores of {@code options}, in the same order. */
	static List<BigDecimal> scores(List<EvaluatedOption> options, ScoringWeights weights) {
		Range net = Range.of(options, EvaluatedOption::netRealization);
		Range price = Range.of(options, EvaluatedOption::modalPrice);
		Range transport = Range.of(options, EvaluatedOption::transportCost);
		return options.stream().map((option) -> {
			BigDecimal score = weights.netRealization().multiply(net.normalize(option.netRealization(), BigDecimal.ONE))
				.add(weights.price().multiply(price.normalize(option.modalPrice(), BigDecimal.ONE)))
				.add(weights.trend().multiply(trendScore(option.candidate().trend().direction())))
				.add(weights.freshness().multiply(freshnessScore(option.freshness())))
				.subtract(weights.transportPenalty().multiply(transport.normalize(option.transportCost(), BigDecimal.ZERO)));
			return score.setScale(SCORE_SCALE, RoundingMode.HALF_UP);
		}).toList();
	}

	/** RISING 1.0, STABLE 0.5, FALLING 0.0 (spec section 16); INSUFFICIENT_DATA 0.5 (ADR-0024 item 2). */
	static BigDecimal trendScore(TrendDirection direction) {
		return switch (direction) {
			case RISING -> BigDecimal.ONE;
			case STABLE, INSUFFICIENT_DATA -> new BigDecimal("0.5");
			case FALLING -> BigDecimal.ZERO;
		};
	}

	/** FRESH 1.0, RECENT 0.7, STALE 0.3, UNAVAILABLE 0.0 (spec section 16). */
	static BigDecimal freshnessScore(FreshnessStatus freshness) {
		return switch (freshness) {
			case FRESH -> BigDecimal.ONE;
			case RECENT -> new BigDecimal("0.7");
			case STALE -> new BigDecimal("0.3");
			case UNAVAILABLE -> BigDecimal.ZERO;
		};
	}

	private record Range(BigDecimal min, BigDecimal max) {

		static Range of(List<EvaluatedOption> options, Function<EvaluatedOption, BigDecimal> value) {
			BigDecimal min = options.stream().map(value).min(Comparator.naturalOrder()).orElseThrow();
			BigDecimal max = options.stream().map(value).max(Comparator.naturalOrder()).orElseThrow();
			return new Range(min, max);
		}

		BigDecimal normalize(BigDecimal value, BigDecimal whenFlat) {
			BigDecimal width = this.max.subtract(this.min);
			return (width.signum() == 0) ? whenFlat : value.subtract(this.min).divide(width, DIVISION);
		}

	}

}
