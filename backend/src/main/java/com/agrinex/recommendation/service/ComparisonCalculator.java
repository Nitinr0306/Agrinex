package com.agrinex.recommendation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.agrinex.common.error.ApiException;
import com.agrinex.common.error.ErrorCode;
import com.agrinex.common.time.AgrinexCalendar;
import com.agrinex.price.FreshnessThresholds;
import com.agrinex.price.PriceApi;
import com.agrinex.price.SelectedObservation;
import com.agrinex.price.TrendStatistic;
import com.agrinex.recommendation.engine.Candidate;
import com.agrinex.recommendation.engine.EvaluatedOption;
import com.agrinex.recommendation.engine.Ranking;
import com.agrinex.recommendation.engine.ScoringWeights;
import com.agrinex.recommendation.engine.SellingOptionEngine;

import org.springframework.stereotype.Component;

/**
 * Gathers the stored data of one calculation and runs the engine (spec section 16 flow). Only stored data is
 * read; the government API is never called here (spec section 31 RULE 1). Observations are selected within the
 * stale window of the weights version the calculation uses (ADR-0010 item 2; ADR-0035 item 2).
 */
@Component
class ComparisonCalculator {

	private final CandidateMarkets candidateMarkets;

	private final PriceApi prices;

	private final AgrinexCalendar calendar;

	ComparisonCalculator(CandidateMarkets candidateMarkets, PriceApi prices, AgrinexCalendar calendar) {
		this.candidateMarkets = candidateMarkets;
		this.prices = prices;
		this.calendar = calendar;
	}

	/** @throws ApiException 422 NO_MARKETS_IN_RANGE when no candidate market lies within the radius */
	ComparisonOutcome calculate(ComparisonInputs inputs) {
		List<MarketInRange> inRange = this.candidateMarkets.within(inputs.originLat(), inputs.originLon(),
				inputs.maxDistanceKm());
		if (inRange.isEmpty()) {
			throw new ApiException(ErrorCode.NO_MARKETS_IN_RANGE,
					"No verified, active market lies within the maximum distance.");
		}
		LocalDate today = this.calendar.today();
		FreshnessThresholds thresholds = inputs.weights().thresholds();
		ComparedCommodity references = inputs.references();
		UUID commodityId = references.commodity().id();
		Map<UUID, SelectedObservation> selected = this.prices.selectLatest(
				inRange.stream().map((market) -> market.market().id()).toList(), commodityId, references.varietyId(),
				references.gradeId(), today, thresholds.staleDays());
		// One read for the trends of all markets with an observation, not one per option (Task 12).
		Map<UUID, TrendStatistic> trends = this.prices.trends(
				inRange.stream().map((market) -> market.market().id()).filter(selected::containsKey).toList(),
				commodityId, references.varietyId(), references.gradeId(), today);
		List<Candidate> eligible = new ArrayList<>();
		List<MarketInRange> excluded = new ArrayList<>();
		for (MarketInRange market : inRange) {
			SelectedObservation observation = selected.get(market.market().id());
			if (observation == null) {
				excluded.add(market);
			}
			else {
				eligible.add(new Candidate(market.market(), market.distanceKm(), observation,
						trends.get(market.market().id())));
			}
		}
		List<EvaluatedOption> evaluated = SellingOptionEngine.evaluate(eligible,
				inputs.quantityUnit().toKilograms(inputs.quantity()), inputs.quantityUnit().toQuintals(inputs.quantity()),
				inputs.transport(), today, thresholds);
		Ranking ranking = SellingOptionEngine.rank(evaluated, inputs.rankingMethod(),
				ScoringWeights.of(inputs.weights()));
		return new ComparisonOutcome(ranking, List.copyOf(excluded));
	}

}
