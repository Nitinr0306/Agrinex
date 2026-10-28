package com.agrinex.price.domain;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.agrinex.price.FreshnessStatus;
import com.agrinex.price.FreshnessThresholds;

import static org.assertj.core.api.Assertions.assertThat;

/** Business key, slug and freshness rules (ADR-0009, ADR-0014 item 6, spec section 16). */
class PriceRulesTest {

	static final FreshnessThresholds DEFAULT = new FreshnessThresholds(1, 4, 10);

	static final LocalDate TODAY = LocalDate.of(2026, 10, 5);

	@Test
	void theBusinessKeyIsTheSha256OfTheJoinedIdentity() {
		// sha256("00000000-0000-0000-0000-000000000001|00000000-0000-0000-0000-000000000002|
		// 00000000-0000-0000-0000-000000000003|||2026-09-13"), computed independently with Python hashlib.
		String key = BusinessKeys.of(UUID.fromString("00000000-0000-0000-0000-000000000001"),
				UUID.fromString("00000000-0000-0000-0000-000000000002"),
				UUID.fromString("00000000-0000-0000-0000-000000000003"), null, null, LocalDate.of(2026, 9, 13));
		assertThat(key).isEqualTo(EXPECTED_KEY);
		assertThat(key).matches("[0-9a-f]{64}");
	}

	@Test
	void aVarietyChangesTheKey() {
		UUID dataset = UUID.randomUUID();
		UUID market = UUID.randomUUID();
		UUID commodity = UUID.randomUUID();
		LocalDate day = LocalDate.of(2026, 9, 13);
		assertThat(BusinessKeys.of(dataset, market, commodity, null, null, day))
			.isNotEqualTo(BusinessKeys.of(dataset, market, commodity, UUID.randomUUID(), null, day));
	}

	@Test
	void slugsAreLowerCaseWithOneHyphenPerRunAndEdgeHyphensKept() {
		assertThat(MarketSlugs.slugify("Pune(Moshi)")).isEqualTo("pune-moshi-");
		assertThat(MarketSlugs.slugify("APMC  Rahata")).isEqualTo("apmc-rahata");
	}

	@Test
	void freshnessUsesTheThresholdsInclusively() {
		assertThat(FreshnessStatus.classify(TODAY, TODAY, DEFAULT)).isEqualTo(FreshnessStatus.FRESH);
		assertThat(FreshnessStatus.classify(TODAY.minusDays(1), TODAY, DEFAULT)).isEqualTo(FreshnessStatus.FRESH);
		assertThat(FreshnessStatus.classify(TODAY.minusDays(2), TODAY, DEFAULT)).isEqualTo(FreshnessStatus.RECENT);
		assertThat(FreshnessStatus.classify(TODAY.minusDays(4), TODAY, DEFAULT)).isEqualTo(FreshnessStatus.RECENT);
		assertThat(FreshnessStatus.classify(TODAY.minusDays(5), TODAY, DEFAULT)).isEqualTo(FreshnessStatus.STALE);
		assertThat(FreshnessStatus.classify(TODAY.minusDays(10), TODAY, DEFAULT)).isEqualTo(FreshnessStatus.STALE);
		assertThat(FreshnessStatus.classify(TODAY.minusDays(11), TODAY, DEFAULT))
			.isEqualTo(FreshnessStatus.UNAVAILABLE);
	}

	static final String EXPECTED_KEY = "e808eee976961ad6c913482bbba5ab2f51fe5a89a76c6f9caf3d4eff8e9bce8e";

}
