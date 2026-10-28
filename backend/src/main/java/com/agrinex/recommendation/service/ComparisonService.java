package com.agrinex.recommendation.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.agrinex.auth.AuthApi;
import com.agrinex.auth.Role;
import com.agrinex.common.error.ApiErrorResponse.FieldError;
import com.agrinex.common.error.ApiException;
import com.agrinex.common.error.ErrorCode;
import com.agrinex.common.error.FieldErrorCode;
import com.agrinex.common.time.AgrinexCalendar;
import com.agrinex.geography.DistrictInfo;
import com.agrinex.geography.GeographyApi;
import com.agrinex.price.SourceSyncStatusPort;
import com.agrinex.recommendation.WeightsVersion;
import com.agrinex.recommendation.domain.RankingMethod;
import com.agrinex.recommendation.domain.ScenarioKind;
import com.agrinex.recommendation.engine.TransportParameters;
import com.agrinex.transport.TransportApi;
import com.agrinex.transport.TransportAssumptions;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The market comparison (spec section 16, 21; FR-06 to FR-13; ADR-0010, ADR-0023, ADR-0024, ADR-0039, ADR-0040).
 * Checks run before anything is recorded: the origin must be a stored district centroid (400), the commodity and
 * filters must exist and be priced per quintal (422), a market must lie in range (422), and every estimate must
 * fit the stored precision (422). Every successful comparison is then recorded, including zero-option results.
 */
@Service
public class ComparisonService {

	/** spec section 16 and F1.6; ADR-0023 item 1. */
	public static final int DEFAULT_MAX_DISTANCE_KM = 150;

	private final GeographyApi geography;

	private final CommodityReferences commodityReferences;

	private final WeightsService weights;

	private final TransportApi transport;

	private final ComparisonCalculator calculator;

	private final RecommendationRecorder recorder;

	private final SourceSyncStatusPort syncStatus;

	private final AuthApi auth;

	private final AgrinexCalendar calendar;

	ComparisonService(GeographyApi geography, CommodityReferences commodityReferences, WeightsService weights,
			TransportApi transport, ComparisonCalculator calculator, RecommendationRecorder recorder,
			SourceSyncStatusPort syncStatus, AuthApi auth, AgrinexCalendar calendar) {
		this.geography = geography;
		this.commodityReferences = commodityReferences;
		this.weights = weights;
		this.transport = transport;
		this.calculator = calculator;
		this.recorder = recorder;
		this.syncStatus = syncStatus;
		this.auth = auth;
		this.calendar = calendar;
	}

	@Transactional
	public ComparisonResult compare(CompareCommand command) {
		DistrictInfo origin = this.geography.findDistrictByCentroid(command.originLat(), command.originLon())
			.orElseThrow(ComparisonService::originNotADistrictCentroid);
		ComparedCommodity references = this.commodityReferences.resolve(command.commodityId(), command.varietyId(),
				command.gradeId());
		WeightsVersion activeWeights = this.weights.activeWeights();
		TransportAssumptions activeTransport = this.transport.activeAssumptions();
		ComparisonInputs inputs = new ComparisonInputs(references, command.quantity(), command.quantityUnit(),
				origin.centroidLat(), origin.centroidLon(),
				(command.maxDistanceKm() != null) ? command.maxDistanceKm() : DEFAULT_MAX_DISTANCE_KM, activeWeights,
				TransportParameters.of(activeTransport, activeTransport.ratePerKm()),
				RankingMethod.DETERMINISTIC_COMPOSITE_SCORE);
		ComparisonOutcome outcome = this.calculator.calculate(inputs);
		if (!outcome.ranking().withinStoredPrecision()) {
			throw estimateOutOfRange();
		}
		UUID runId = this.recorder.record(callerFarmerProfile(), inputs, activeTransport.versionId(),
				outcome.ranking());
		return new ComparisonResult(runId, this.calendar.now(), ScenarioKind.COMPARISON, null, inputs, activeTransport,
				outcome, this.syncStatus.currentDatasetStatus());
	}

	/** A signed-in FARMER or ADMIN links the search to their farmer profile, when one exists (contract compare). */
	private UUID callerFarmerProfile() {
		return this.auth.currentAccount()
			.filter((account) -> account.role() == Role.FARMER || account.role() == Role.ADMIN)
			.flatMap((account) -> this.auth.findFarmerProfile(account.userId()))
			.orElse(null);
	}

	/** ADR-0039 item 2: 400 VALIDATION_FAILED with INVALID_VALUE on both origin fields. */
	private static ApiException originNotADistrictCentroid() {
		String message = "is not the centroid of a district as listDistricts publishes it";
		return ApiException.validation(List.of(new FieldError("originLat", FieldErrorCode.INVALID_VALUE, message),
				new FieldError("originLon", FieldErrorCode.INVALID_VALUE, message)));
	}

	/** ADR-0040 item 2: nothing is recorded. */
	private static ApiException estimateOutOfRange() {
		return new ApiException(ErrorCode.ESTIMATE_OUT_OF_RANGE,
				"An estimate of some market would exceed the stored precision.",
				List.of(new FieldError("quantity", FieldErrorCode.OUT_OF_RANGE,
						"makes an estimate exceed 999999999999.99 INR")),
				Map.of());
	}

}
