package com.agrinex.recommendation.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.agrinex.commodity.CommodityInfo;
import com.agrinex.commodity.CommodityRef;
import com.agrinex.commodity.NamedRef;
import com.agrinex.common.web.GeoPoint;
import com.agrinex.common.web.Ref;
import com.agrinex.price.MarketSummary;
import com.agrinex.price.PriceQuote;
import com.agrinex.price.SourceSyncStatus;
import com.agrinex.price.TrendDirection;
import com.agrinex.recommendation.domain.QuantityUnit;
import com.agrinex.recommendation.domain.RankingMethod;
import com.agrinex.recommendation.domain.ReasonCode;
import com.agrinex.recommendation.domain.ScenarioKind;
import com.agrinex.recommendation.engine.EvaluatedOption;
import com.agrinex.recommendation.engine.RankedOption;
import com.agrinex.recommendation.engine.Ranking;
import com.agrinex.recommendation.service.ComparisonInputs;
import com.agrinex.recommendation.service.ComparisonResult;
import com.agrinex.recommendation.service.MarketInRange;
import com.agrinex.recommendation.service.SimulationOverrides;
import com.agrinex.transport.TransportAssumptions;

/**
 * Contract MarketComparisonResponse (recommendation.schema.json; spec section 16, 30). Observed prices
 * ({@code price}), user overrides, configured assumptions and ESTIMATED values ({@code estimates}) stay in separate,
 * labelled places. No ML output contributes ({@code decisionBasis.mlContributed} is always false; ADR-0030).
 */
public record MarketComparisonResponse(UUID recommendationId, Instant generatedAt, Scenario scenario,
		CommodityRef commodity, Ref variety, Ref grade, String priceUnit, BigDecimal quantity, QuantityUnit quantityUnit,
		BigDecimal quantityInQuintal, GeoPoint origin, int maxDistanceKm,
		TransportAssumptionsApplied transportAssumptions, DecisionBasis decisionBasis, UUID priceOnlyWinnerId,
		UUID netRealizationWinnerId, boolean rankingsDisagree, List<SellingOptionResponse> options,
		List<ExcludedMarket> excludedMarkets, String noDataReason, SourceSyncStatus sourceStatus) {

	public static final String NO_RECENT_PRICE_DATA = "NO_RECENT_PRICE_DATA";

	public static MarketComparisonResponse from(ComparisonResult result) {
		ComparisonInputs inputs = result.inputs();
		Ranking ranking = result.outcome().ranking();
		CommodityInfo commodity = inputs.references().commodity();
		return new MarketComparisonResponse(result.recommendationId(), result.generatedAt(), Scenario.of(result),
				commodity.ref(), ref(inputs.references().variety()), ref(inputs.references().grade()),
				CommodityInfo.RUPEES_PER_QUINTAL, inputs.quantity(), inputs.quantityUnit(),
				inputs.quantityUnit().toQuintals(inputs.quantity()), new GeoPoint(inputs.originLat(), inputs.originLon()),
				inputs.maxDistanceKm(), TransportAssumptionsApplied.of(result),
				new DecisionBasis(inputs.rankingMethod(), inputs.weights().versionId(), false),
				ranking.priceOnlyWinnerId(), ranking.netRealizationWinnerId(), ranking.rankingsDisagree(),
				ranking.options().stream().map(SellingOptionResponse::of).toList(),
				result.outcome().excludedMarkets().stream().map(ExcludedMarket::of).toList(),
				ranking.options().isEmpty() ? NO_RECENT_PRICE_DATA : null, result.sourceStatus());
	}

	private static Ref ref(NamedRef named) {
		return (named != null) ? new Ref(named.id(), named.name()) : null;
	}

	/** Contract Scenario: a recorded COMPARISON, or a SIMULATION that records nothing. */
	public record Scenario(ScenarioKind kind, boolean persisted, UserOverrides userOverrides) {

		static Scenario of(ComparisonResult result) {
			SimulationOverrides overrides = result.overrides();
			return new Scenario(result.scenario(), result.persisted(), (overrides != null)
					? new UserOverrides(overrides.quantityKg(), overrides.transportRatePerKm(), overrides.maxDistanceKm())
					: null);
		}

	}

	/** Contract UserOverrides: only the overrides actually supplied are present. */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record UserOverrides(BigDecimal quantity, BigDecimal transportRatePerKm, Integer maxDistanceKm) {
	}

	/** Contract TransportAssumptionsApplied (ADR-0026 item 1, ADR-0038). */
	public record TransportAssumptionsApplied(UUID versionId, BigDecimal ratePerKm, BigDecimal minimumTripCharge,
			BigDecimal vehicleCapacityKg, BigDecimal appliedRatePerKm, String ratePerKmSource) {

		static TransportAssumptionsApplied of(ComparisonResult result) {
			TransportAssumptions version = result.transportVersion();
			return new TransportAssumptionsApplied(version.versionId(), version.ratePerKm(), version.minimumTripCharge(),
					version.vehicleCapacityKg(), result.inputs().transport().ratePerKm(),
					result.rateOverridden() ? "USER_OVERRIDE" : "CONFIGURED");
		}

	}

	/** Contract DecisionBasis. */
	public record DecisionBasis(RankingMethod rankingMethod, UUID weightsVersionId, boolean mlContributed) {
	}

	/** Contract SellingEstimates: ESTIMATED totals in INR (spec section 32, 49). */
	public record SellingEstimates(long trips, BigDecimal estimatedTransportCost, BigDecimal estimatedGrossValue,
			BigDecimal estimatedNetRealization, String currency) {

		static final String INR = "INR";

	}

	/** Contract SellingOptionResponse. */
	public record SellingOptionResponse(int rank, MarketSummary market, BigDecimal distanceKm,
			boolean approximateMarketLocation, PriceQuote price, SellingEstimates estimates,
			TrendDirection trendDirection, BigDecimal changePercent7Day, List<ReasonCode> reasonCodes) {

		static SellingOptionResponse of(RankedOption ranked) {
			EvaluatedOption option = ranked.option();
			return new SellingOptionResponse(ranked.rank(), option.candidate().market().summary(), option.distanceKm(),
					option.candidate().market().coordinateApproximate(),
					PriceQuote.of(option.candidate().observation(), option.freshness()),
					new SellingEstimates(option.transport().trips(), option.transportCost(), option.grossValue(),
							option.netRealization(), SellingEstimates.INR),
					option.candidate().trend().direction(), option.candidate().trend().displayChangePercent(),
					ranked.reasonCodes());
		}

	}

	/** Contract ExcludedMarket: in range, but no observation in the stale window (ADR-0023 item 3). */
	public record ExcludedMarket(MarketSummary market, BigDecimal distanceKm, boolean approximateMarketLocation,
			String reason) {

		static ExcludedMarket of(MarketInRange excluded) {
			return new ExcludedMarket(excluded.market().summary(), excluded.distanceKm(),
					excluded.market().coordinateApproximate(), NO_RECENT_PRICE_DATA);
		}

	}

}
