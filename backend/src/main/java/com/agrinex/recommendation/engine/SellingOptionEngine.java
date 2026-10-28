package com.agrinex.recommendation.engine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import com.agrinex.common.Decimals;
import com.agrinex.price.FreshnessStatus;
import com.agrinex.price.FreshnessThresholds;
import com.agrinex.recommendation.domain.RankingMethod;
import com.agrinex.recommendation.engine.OptionOrdering.ScoredOption;
import com.agrinex.transport.TransportCalculator;
import com.agrinex.transport.TransportEstimate;

/**
 * The deterministic decision engine (spec section 16, 17, 32; ADR-0023, ADR-0024, ADR-0026): estimates for each
 * candidate, then ranking, reason codes and winners. Pure functions of their inputs: no randomness, no clock, no
 * data access and no ML (spec P9, section 35), so the same inputs always give the same result.
 */
public final class SellingOptionEngine {

	private SellingOptionEngine() {
	}

	/**
	 * Estimates of each candidate with the rounding points of API_CONVENTIONS.md section 4.1:
	 * transport = round2(max(minimumTripCharge, distanceKm x rate) x trips); gross = round2(modal x quantity in
	 * quintals); net = gross - transport.
	 */
	public static List<EvaluatedOption> evaluate(List<Candidate> candidates, BigDecimal quantityKg,
			BigDecimal quantityInQuintal, TransportParameters transport, LocalDate today,
			FreshnessThresholds thresholds) {
		return candidates.stream().map((candidate) -> {
			TransportEstimate estimate = TransportCalculator.estimate(candidate.distanceKm(), quantityKg,
					transport.ratePerKm(), transport.minimumTripCharge(), transport.vehicleCapacityKg());
			BigDecimal gross = Decimals.round2(candidate.observation().modalPrice().multiply(quantityInQuintal));
			BigDecimal net = gross.subtract(estimate.estimatedTransportCost());
			FreshnessStatus freshness = FreshnessStatus.classify(candidate.observation().reportDate(), today,
					thresholds);
			return new EvaluatedOption(candidate, estimate, gross, net, freshness);
		}).toList();
	}

	/** Ranks the options by the method, attaches reason codes and identifies the two winners (ADR-0023 item 2). */
	public static Ranking rank(List<EvaluatedOption> options, RankingMethod method, ScoringWeights weights) {
		if (options.isEmpty()) {
			return Ranking.EMPTY;
		}
		List<ScoredOption> ordered = scored(options, method, weights);
		ReasonCodeRules rules = ReasonCodeRules.over(options);
		List<RankedOption> ranked = IntStream.range(0, ordered.size())
			.mapToObj((index) -> new RankedOption(ordered.get(index).option(), index + 1, ordered.get(index).score(),
					rules.codesFor(ordered.get(index).option())))
			.toList();
		UUID priceWinner = options.stream().min(OptionOrdering.BY_PRICE).orElseThrow().marketId();
		UUID netWinner = ranked.getFirst().option().marketId();
		return new Ranking(ranked, priceWinner, netWinner, !priceWinner.equals(netWinner));
	}

	private static List<ScoredOption> scored(List<EvaluatedOption> options, RankingMethod method,
			ScoringWeights weights) {
		List<ScoredOption> scored = new ArrayList<>();
		if (method == RankingMethod.DETERMINISTIC_COMPOSITE_SCORE) {
			List<BigDecimal> scores = CompositeScorer.scores(options, weights);
			for (int index = 0; index < options.size(); index++) {
				scored.add(new ScoredOption(options.get(index), scores.get(index)));
			}
			scored.sort(OptionOrdering.byScore());
		}
		else {
			options.stream()
				.sorted(OptionOrdering.BY_NET_REALIZATION)
				.forEach((option) -> scored.add(new ScoredOption(option, null)));
		}
		return scored;
	}

}
