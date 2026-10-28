package com.agrinex.recommendation.service;

import java.util.UUID;

import com.agrinex.common.time.AgrinexCalendar;
import com.agrinex.recommendation.domain.RecommendationRunEntity;
import com.agrinex.recommendation.domain.SellingOptionEntity;
import com.agrinex.recommendation.domain.SellingSearchEntity;
import com.agrinex.recommendation.engine.EvaluatedOption;
import com.agrinex.recommendation.engine.RankedOption;
import com.agrinex.recommendation.engine.Ranking;
import com.agrinex.recommendation.repository.RecommendationRunRepository;
import com.agrinex.recommendation.repository.SellingOptionRepository;
import com.agrinex.recommendation.repository.SellingSearchRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Records a comparison (spec section 16 PERSIST; ADR-0023 item 4): its search, its run with the configuration
 * versions and winners, and one option row per eligible market. Zero-option results are recorded too; excluded
 * markets never are.
 */
@Component
class RecommendationRecorder {

	private final SellingSearchRepository searches;

	private final RecommendationRunRepository runs;

	private final SellingOptionRepository options;

	private final AgrinexCalendar calendar;

	RecommendationRecorder(SellingSearchRepository searches, RecommendationRunRepository runs,
			SellingOptionRepository options, AgrinexCalendar calendar) {
		this.searches = searches;
		this.runs = runs;
		this.options = options;
		this.calendar = calendar;
	}

	/** Joins the comparison's transaction; returns the run id, the response's recommendationId. */
	@Transactional(propagation = Propagation.MANDATORY)
	public UUID record(UUID farmerProfileId, ComparisonInputs inputs, UUID transportVersionId, Ranking ranking) {
		ComparedCommodity references = inputs.references();
		SellingSearchEntity search = this.searches.save(new SellingSearchEntity(farmerProfileId,
				references.commodity().id(), references.varietyId(), references.gradeId(), inputs.quantity(),
				inputs.quantityUnit(), inputs.originLat(), inputs.originLon(), inputs.maxDistanceKm()));
		RecommendationRunEntity run = this.runs.save(new RecommendationRunEntity(search.getId(),
				inputs.weights().versionId(), transportVersionId, ranking.priceOnlyWinnerId(),
				ranking.netRealizationWinnerId(), ranking.rankingsDisagree(), this.calendar.now()));
		this.options.saveAll(ranking.options().stream().map((option) -> toEntity(run.getId(), option)).toList());
		return run.getId();
	}

	private static SellingOptionEntity toEntity(UUID runId, RankedOption ranked) {
		EvaluatedOption option = ranked.option();
		return new SellingOptionEntity(runId, option.marketId(), option.candidate().observation().observationId(),
				option.distanceKm(), option.transportCost(), option.grossValue(), option.netRealization(),
				option.candidate().trend().direction(), option.freshness(), ranked.rank(), ranked.score(),
				ranked.reasonCodes());
	}

}
