package com.agrinex.recommendation.engine;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.agrinex.geography.DistrictInfo;
import com.agrinex.geography.StateRef;
import com.agrinex.price.FreshnessStatus;
import com.agrinex.price.FreshnessThresholds;
import com.agrinex.price.MarketInfo;
import com.agrinex.price.MarketReviewStatus;
import com.agrinex.price.SelectedObservation;
import com.agrinex.price.SourceDataset;
import com.agrinex.price.TrendDirection;
import com.agrinex.price.TrendStatistic;
import com.agrinex.recommendation.domain.RankingMethod;
import com.agrinex.recommendation.domain.ReasonCode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The deterministic engine on a hand-computed SYNTHETIC scenario (spec section 16, 32, 48; ADR-0024, ADR-0026). The
 * markets and prices are invented test data, never government observations.
 *
 * <pre>
 * 20 quintal = 2000 kg, rate 8.00/km, minimum 300.00/trip, capacity 1000 kg -> 2 trips
 * A  30 km  modal 2500  today      RISING   transport max(300, 240)=300 x2 =  600  gross 50000  net 49400
 * B  60 km  modal 2600  today - 2  STABLE   transport max(300, 480)=480 x2 =  960  gross 52000  net 51040
 * C 250 km  modal 2650  today - 6  FALLING  transport max(300,2000)=2000x2 = 4000  gross 53000  net 49000
 * normalized net   A 400/2040  B 1      C 0
 * normalized price A 0         B 100/150 C 1
 * normalized cost  A 0         B 360/3400 C 1
 * score A = .55(400/2040) + .10(1.0) + .10(1.0)                         = 0.307843 (6 dp)
 * score B = .55 + .15(2/3) + .10(0.5) + .10(0.7) - .10(360/3400)        = 0.759412
 * score C = .15 + .10(0.0) + .10(0.3) - .10                              = 0.080000
 * </pre>
 */
class SellingOptionEngineTest {

	static final LocalDate TODAY = LocalDate.of(2026, 10, 5);

	static final FreshnessThresholds DEFAULT_THRESHOLDS = new FreshnessThresholds(1, 4, 10);

	static final ScoringWeights DEFAULT_WEIGHTS = new ScoringWeights(new BigDecimal("0.5500"),
			new BigDecimal("0.1500"), new BigDecimal("0.1000"), new BigDecimal("0.1000"), new BigDecimal("0.1000"));

	static final TransportParameters DEFAULT_TRANSPORT = new TransportParameters(new BigDecimal("8.00"),
			new BigDecimal("300.00"), new BigDecimal("1000.00"));

	static final BigDecimal TWENTY_QUINTAL_KG = new BigDecimal("2000");

	static final BigDecimal TWENTY_QUINTAL = new BigDecimal("20");

	final Candidate marketA = candidate("Synthetic Market A", "30.00", "2500.00", TODAY, TrendDirection.RISING);

	final Candidate marketB = candidate("Synthetic Market B", "60.00", "2600.00", TODAY.minusDays(2),
			TrendDirection.STABLE);

	final Candidate marketC = candidate("Synthetic Market C", "250.00", "2650.00", TODAY.minusDays(6),
			TrendDirection.FALLING);

	@Test
	void estimatesFollowTheSpecTransportFormula() {
		List<EvaluatedOption> options = evaluate(List.of(this.marketA, this.marketB, this.marketC));
		assertThat(options).extracting((option) -> option.transport().trips()).containsExactly(2L, 2L, 2L);
		assertThat(options).extracting(EvaluatedOption::transportCost)
			.containsExactly(new BigDecimal("600.00"), new BigDecimal("960.00"), new BigDecimal("4000.00"));
		assertThat(options).extracting(EvaluatedOption::grossValue)
			.containsExactly(new BigDecimal("50000.00"), new BigDecimal("52000.00"), new BigDecimal("53000.00"));
		assertThat(options).extracting(EvaluatedOption::netRealization)
			.containsExactly(new BigDecimal("49400.00"), new BigDecimal("51040.00"), new BigDecimal("49000.00"));
		assertThat(options).extracting(EvaluatedOption::freshness)
			.containsExactly(FreshnessStatus.FRESH, FreshnessStatus.RECENT, FreshnessStatus.STALE);
	}

	@Test
	void compositeScoreRanksAndExplainsTheOptions() {
		Ranking ranking = SellingOptionEngine.rank(evaluate(List.of(this.marketA, this.marketB, this.marketC)),
				RankingMethod.DETERMINISTIC_COMPOSITE_SCORE, DEFAULT_WEIGHTS);
		assertThat(ranking.options()).extracting((ranked) -> ranked.option().marketName())
			.containsExactly("Synthetic Market B", "Synthetic Market A", "Synthetic Market C");
		assertThat(ranking.options()).extracting(RankedOption::score)
			.containsExactly(new BigDecimal("0.759412"), new BigDecimal("0.307843"), new BigDecimal("0.080000"));
		assertThat(ranking.options()).extracting(RankedOption::rank).containsExactly(1, 2, 3);
		assertThat(ranking.netRealizationWinnerId()).isEqualTo(this.marketB.market().id());
		assertThat(ranking.priceOnlyWinnerId()).isEqualTo(this.marketC.market().id());
		assertThat(ranking.rankingsDisagree()).isTrue();
	}

	@Test
	void everyReasonCodeTriggersUnderItsRule() {
		Ranking ranking = SellingOptionEngine.rank(evaluate(List.of(this.marketA, this.marketB, this.marketC)),
				RankingMethod.DETERMINISTIC_COMPOSITE_SCORE, DEFAULT_WEIGHTS);
		assertThat(ranking.options().get(1).reasonCodes()).containsExactly(ReasonCode.LOWER_TRANSPORT,
				ReasonCode.POSITIVE_TREND, ReasonCode.RECENT_DATA, ReasonCode.SUITABLE_DISTANCE);
		assertThat(ranking.options().get(0).reasonCodes()).containsExactly(ReasonCode.STABLE_TREND);
		assertThat(ranking.options().get(2).reasonCodes()).containsExactly(ReasonCode.PRICE_ADVANTAGE,
				ReasonCode.NEGATIVE_TREND, ReasonCode.STALE_DATA);
	}

	@Test
	void simulationRanksByNetRealizationWithoutScores() {
		Ranking ranking = SellingOptionEngine.rank(evaluate(List.of(this.marketA, this.marketB, this.marketC)),
				RankingMethod.NET_REALIZATION, DEFAULT_WEIGHTS);
		assertThat(ranking.options()).extracting((ranked) -> ranked.option().marketName())
			.containsExactly("Synthetic Market B", "Synthetic Market A", "Synthetic Market C");
		assertThat(ranking.options()).extracting(RankedOption::score).containsOnlyNulls();
	}

	@Test
	void aSingleCandidateGetsFullNormalizedNetAndPriceAndNoTransportPenalty() {
		// ADR-0024 item 4: 0.55 x 1 + 0.15 x 1 + 0.10 x 1.0 (RISING) + 0.10 x 1.0 (FRESH) - 0.10 x 0 = 0.900000.
		Ranking ranking = SellingOptionEngine.rank(evaluate(List.of(this.marketA)),
				RankingMethod.DETERMINISTIC_COMPOSITE_SCORE, DEFAULT_WEIGHTS);
		assertThat(ranking.options()).singleElement().satisfies((only) -> {
			assertThat(only.score()).isEqualByComparingTo("0.900000");
			assertThat(only.reasonCodes()).contains(ReasonCode.PRICE_ADVANTAGE, ReasonCode.LOWER_TRANSPORT);
		});
		assertThat(ranking.rankingsDisagree()).isFalse();
	}

	@Test
	void tiesFallBackToNetThenDistanceThenName() {
		Candidate second = candidate("Synthetic Market Z", "30.00", "2500.00", TODAY, TrendDirection.RISING);
		Candidate first = candidate("Synthetic Market Y", "30.00", "2500.00", TODAY, TrendDirection.RISING);
		Ranking ranking = SellingOptionEngine.rank(evaluate(List.of(second, first)),
				RankingMethod.DETERMINISTIC_COMPOSITE_SCORE, DEFAULT_WEIGHTS);
		assertThat(ranking.options()).extracting((ranked) -> ranked.option().marketName())
			.containsExactly("Synthetic Market Y", "Synthetic Market Z");
	}

	@Test
	void noEligibleOptionGivesNoWinners() {
		assertThat(SellingOptionEngine.rank(List.of(), RankingMethod.DETERMINISTIC_COMPOSITE_SCORE, DEFAULT_WEIGHTS))
			.isEqualTo(Ranking.EMPTY);
	}

	@Test
	void estimatesBeyondTheStoredPrecisionAreDetected() {
		// ADR-0040: 999 999 999.999 quintal at 2500.00 is far above 999 999 999 999.99 INR.
		List<EvaluatedOption> options = SellingOptionEngine.evaluate(List.of(this.marketA),
				new BigDecimal("99999999999.9"), new BigDecimal("999999999.999"), DEFAULT_TRANSPORT, TODAY,
				DEFAULT_THRESHOLDS);
		assertThat(options.getFirst().withinStoredPrecision()).isFalse();
	}

	private static List<EvaluatedOption> evaluate(List<Candidate> candidates) {
		return SellingOptionEngine.evaluate(candidates, TWENTY_QUINTAL_KG, TWENTY_QUINTAL, DEFAULT_TRANSPORT, TODAY,
				DEFAULT_THRESHOLDS);
	}

	static Candidate candidate(String name, String distanceKm, String modal, LocalDate reportDate,
			TrendDirection trend) {
		StateRef state = new StateRef(UUID.randomUUID(), "IN-MH", "Synthetic State", null, null);
		DistrictInfo district = new DistrictInfo(UUID.randomUUID(), "Synthetic District", new BigDecimal("18.500000"),
				new BigDecimal("73.800000"), state);
		MarketInfo market = new MarketInfo(UUID.randomUUID(), name, name.toLowerCase().replace(' ', '-'), district,
				new BigDecimal("18.500000"), new BigDecimal("73.800000"), false, "synthetic test coordinates", true,
				MarketReviewStatus.VERIFIED, null, Instant.parse("2026-01-01T00:00:00Z"));
		SelectedObservation observation = new SelectedObservation(UUID.randomUUID(), market.id(), UUID.randomUUID(),
				null, null, null, null, reportDate, new BigDecimal(modal), null, null, "Rs./Quintal",
				SourceDataset.CURRENT_PRICE, "synthetic-resource", Instant.parse("2026-10-05T00:00:00Z"));
		TrendStatistic statistic = new TrendStatistic(trend, (trend == TrendDirection.RISING) ? new BigDecimal("5")
				: (trend == TrendDirection.FALLING) ? new BigDecimal("-5") : BigDecimal.ZERO);
		return new Candidate(market, new BigDecimal(distanceKm), observation, statistic);
	}

}
