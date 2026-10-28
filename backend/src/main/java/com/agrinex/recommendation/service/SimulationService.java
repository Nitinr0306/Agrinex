package com.agrinex.recommendation.service;

import java.math.BigDecimal;
import java.util.UUID;

import com.agrinex.common.error.ApiException;
import com.agrinex.common.time.AgrinexCalendar;
import com.agrinex.price.SourceSyncStatusPort;
import com.agrinex.recommendation.WeightsVersion;
import com.agrinex.recommendation.domain.QuantityUnit;
import com.agrinex.recommendation.domain.RankingMethod;
import com.agrinex.recommendation.domain.RecommendationRunEntity;
import com.agrinex.recommendation.domain.ScenarioKind;
import com.agrinex.recommendation.domain.SellingSearchEntity;
import com.agrinex.recommendation.engine.TransportParameters;
import com.agrinex.recommendation.repository.RecommendationRunRepository;
import com.agrinex.recommendation.repository.SellingSearchRepository;
import com.agrinex.transport.TransportApi;
import com.agrinex.transport.TransportAssumptions;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Server-side What-If (spec section 17; ADR-0026 item 3): re-runs a recorded comparison with the user's overrides,
 * the original run's weights and transport versions and its stored, already validated origin (ADR-0039 item 5).
 * Observations are selected again at simulation time, options are ranked by estimated net realization, and
 * nothing is recorded (spec section 32 "session-only").
 */
@Service
@Transactional(readOnly = true)
public class SimulationService {

	private final RecommendationRunRepository runs;

	private final SellingSearchRepository searches;

	private final WeightsService weights;

	private final TransportApi transport;

	private final CommodityReferences commodityReferences;

	private final ComparisonCalculator calculator;

	private final SourceSyncStatusPort syncStatus;

	private final AgrinexCalendar calendar;

	SimulationService(RecommendationRunRepository runs, SellingSearchRepository searches, WeightsService weights,
			TransportApi transport, CommodityReferences commodityReferences, ComparisonCalculator calculator,
			SourceSyncStatusPort syncStatus, AgrinexCalendar calendar) {
		this.runs = runs;
		this.searches = searches;
		this.weights = weights;
		this.transport = transport;
		this.commodityReferences = commodityReferences;
		this.calculator = calculator;
		this.syncStatus = syncStatus;
		this.calendar = calendar;
	}

	public ComparisonResult simulate(UUID recommendationId, SimulationOverrides overrides) {
		RecommendationRunEntity run = this.runs.findById(recommendationId)
			.orElseThrow(() -> ApiException.referenceNotFound("recommendationId"));
		SellingSearchEntity search = this.searches.findById(run.getSellingSearchId())
			.orElseThrow(() -> new IllegalStateException("A recommendation run has no search"));
		WeightsVersion runWeights = this.weights.findVersion(run.getWeightsId())
			.orElseThrow(() -> new IllegalStateException("A recommendation run references no weights version"));
		TransportAssumptions runTransport = this.transport.findVersion(run.getTransportId())
			.orElseThrow(() -> new IllegalStateException("A recommendation run references no transport version"));
		ComparedCommodity references = this.commodityReferences.resolve(search.getCommodityId(),
				search.getVarietyId(), search.getGradeId());
		boolean quantityOverridden = overrides.quantityKg() != null;
		BigDecimal rate = (overrides.transportRatePerKm() != null) ? overrides.transportRatePerKm()
				: runTransport.ratePerKm();
		ComparisonInputs inputs = new ComparisonInputs(references,
				quantityOverridden ? overrides.quantityKg() : search.getQuantity(),
				quantityOverridden ? QuantityUnit.KG : search.getQuantityUnit(), search.getOriginLat(),
				search.getOriginLon(),
				(overrides.maxDistanceKm() != null) ? overrides.maxDistanceKm() : search.getMaxDistanceKm(),
				runWeights, TransportParameters.of(runTransport, rate), RankingMethod.NET_REALIZATION);
		ComparisonOutcome outcome = this.calculator.calculate(inputs);
		return new ComparisonResult(run.getId(), this.calendar.now(), ScenarioKind.SIMULATION, overrides, inputs,
				runTransport, outcome, this.syncStatus.currentDatasetStatus());
	}

}
