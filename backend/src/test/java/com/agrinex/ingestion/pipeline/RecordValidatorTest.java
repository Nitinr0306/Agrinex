package com.agrinex.ingestion.pipeline;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.cfg.JsonNodeFeature;
import tools.jackson.databind.json.JsonMapper;

import com.agrinex.commodity.CommodityApi;
import com.agrinex.commodity.CommodityInfo;
import com.agrinex.geography.DistrictInfo;
import com.agrinex.geography.GeographyApi;
import com.agrinex.geography.StateRef;
import com.agrinex.ingestion.config.IngestionProperties;
import com.agrinex.ingestion.domain.IngestionErrorCode;
import com.agrinex.ingestion.pipeline.RecordCheck.Accepted;
import com.agrinex.ingestion.pipeline.RecordCheck.OutOfScope;
import com.agrinex.ingestion.pipeline.RecordCheck.Rejected;
import com.agrinex.ingestion.source.SourceSchema;
import com.agrinex.price.PriceApi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Coverage and rules V1 to V7 (ADR-0006, ADR-0007, ADR-0008). Records use the current resource's field names; the
 * base record is shaped like the evidence capture C-19-MH-ONION, and every variation is SYNTHETIC test data.
 */
class RecordValidatorTest {

	static final LocalDate TODAY = LocalDate.of(2026, 10, 5);

	static final JsonMapper EXACT = JsonMapper.builder()
		.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
		.enable(JsonNodeFeature.USE_BIG_DECIMAL_FOR_FLOATS)
		.build();

	final CommodityApi commodities = mock(CommodityApi.class);

	final GeographyApi geography = mock(GeographyApi.class);

	final RecordValidator validator = new RecordValidator(new IngestionProperties(null, "http://localhost/",
			List.of("Maharashtra"), "0 0 0 * * *", 3, 500, 0, Duration.ZERO, Duration.ofSeconds(1),
			Duration.ofSeconds(1), 10));

	ReferenceLookups lookups;

	@BeforeEach
	void referenceData() {
		StateRef state = new StateRef(UUID.randomUUID(), "IN-MH", "Maharashtra", null, null);
		given(this.geography.resolveSourceDistrict("Maharashtra", "Ahilyanagar")).willReturn(Optional.of(
				new DistrictInfo(UUID.randomUUID(), "Synthetic District", BigDecimal.ONE, BigDecimal.ONE, state)));
		given(this.commodities.resolveSourceName("Onion"))
			.willReturn(Optional.of(commodity("Onion", CommodityInfo.RUPEES_PER_QUINTAL)));
		given(this.commodities.resolveSourceName("Synthetic Unverified"))
			.willReturn(Optional.of(commodity("Synthetic Unverified", null)));
		this.lookups = new ReferenceLookups(this.commodities, this.geography, mock(PriceApi.class), UUID.randomUUID());
	}

	@Test
	void aCompleteRecordIsAcceptedWithExactRoundedPrices() {
		RecordCheck check = check(record("\"min_price\": 500, \"max_price\": 5000, \"modal_price\": 3963.4099999999999"));
		assertThat(check).isInstanceOfSatisfying(Accepted.class, (accepted) -> {
			assertThat(accepted.record().modalPrice()).isEqualTo(new BigDecimal("3963.41"));
			assertThat(accepted.record().minPrice()).isEqualTo(new BigDecimal("500.00"));
			assertThat(accepted.record().reportDate()).isEqualTo(LocalDate.of(2026, 9, 14));
			assertThat(accepted.record().source().market()).isEqualTo("Rahuri(Vambori)");
			assertThat(accepted.record().source().marketRaw()).isEqualTo("Rahuri(Vambori) ");
		});
	}

	/**
	 * The historical resource's own layout, as the live API returned it on 2026-10-13: capitalised field names, an
	 * extra Commodity_Code, integer min and max with a fractional modal, and a market name with a trailing space.
	 */
	@Test
	void aHistoricalRecordIsReadThroughItsOwnFieldNames() {
		JsonNode historical = json("""
				{"Arrival_Date": "07/09/2026", "Commodity": "Onion", "Commodity_Code": 23, "District": "Ahilyanagar",
				 "Grade": "Local", "Market": "Rahuri(Vambori) ", "Max_Price": 4640, "Min_Price": 1780,
				 "Modal_Price": 3963.41, "State": "Maharashtra", "Variety": "Other"}
				""");
		RecordCheck check = this.validator.check(SourceRecord.from(historical, SourceSchema.HISTORICAL), this.lookups,
				TODAY);
		assertThat(check).isInstanceOfSatisfying(Accepted.class, (accepted) -> {
			assertThat(accepted.record().reportDate()).isEqualTo(LocalDate.of(2026, 9, 7));
			assertThat(accepted.record().modalPrice()).isEqualTo(new BigDecimal("3963.41"));
			assertThat(accepted.record().minPrice()).isEqualTo(new BigDecimal("1780.00"));
			assertThat(accepted.record().maxPrice()).isEqualTo(new BigDecimal("4640.00"));
			assertThat(accepted.record().source().market()).isEqualTo("Rahuri(Vambori)");
			assertThat(accepted.record().source().variety()).isEqualTo("Other");
			assertThat(accepted.record().source().grade()).isEqualTo("Local");
		});
		// Read through the current resource's lower-case names, the same record has no required field at all.
		assertThat(this.validator.check(SourceRecord.from(historical, SourceSchema.CURRENT), this.lookups, TODAY))
			.isInstanceOfSatisfying(Rejected.class,
					(rejected) -> assertThat(rejected.code()).isEqualTo(IngestionErrorCode.REQUIRED_FIELD_MISSING));
	}

	@Test
	void anAbsentMinOrMaxPriceIsStoredAsNullAndAPriceMayArriveAsText() {
		RecordCheck check = check(record("\"modal_price\": \"4100\""));
		assertThat(check).isInstanceOfSatisfying(Accepted.class, (accepted) -> {
			assertThat(accepted.record().modalPrice()).isEqualTo(new BigDecimal("4100.00"));
			assertThat(accepted.record().minPrice()).isNull();
			assertThat(accepted.record().maxPrice()).isNull();
		});
	}

	@Test
	void anotherStateIsOutOfScope() {
		assertThat(check(record("Karnataka", "14/09/2026", "Onion", "\"modal_price\": 4100")))
			.isInstanceOf(OutOfScope.class);
	}

	@Test
	void v1MissingOrBlankRequiredFields() {
		assertRejected(record("Maharashtra", "14/09/2026", "  ", "\"modal_price\": 4100"),
				IngestionErrorCode.REQUIRED_FIELD_MISSING);
		assertRejected(record("\"min_price\": 500"), IngestionErrorCode.REQUIRED_FIELD_MISSING);
	}

	@Test
	void v2DatesAreStrictlyDayMonthYearAndNeverInTheFuture() {
		assertRejected(record("Maharashtra", "2026-09-14", "Onion", "\"modal_price\": 4100"),
				IngestionErrorCode.DATE_FORMAT_INVALID);
		assertRejected(record("Maharashtra", "31/02/2026", "Onion", "\"modal_price\": 4100"),
				IngestionErrorCode.DATE_FORMAT_INVALID);
		assertRejected(record("Maharashtra", "06/10/2026", "Onion", "\"modal_price\": 4100"),
				IngestionErrorCode.DATE_IN_FUTURE);
	}

	@Test
	void v3CommodityMustBeKnownWithAVerifiedUnit() {
		assertRejected(record("Maharashtra", "14/09/2026", "Onion Green", "\"modal_price\": 4100"),
				IngestionErrorCode.UNKNOWN_COMMODITY);
		assertRejected(record("Maharashtra", "14/09/2026", "Synthetic Unverified", "\"modal_price\": 4100"),
				IngestionErrorCode.UNIT_UNVERIFIED);
	}

	@Test
	void v4TheDistrictMustResolve() {
		RecordCheck check = check(json("""
				{"state": "Maharashtra", "district": "Synthetic Nowhere", "market": "M", "commodity": "Onion",
				 "arrival_date": "14/09/2026", "modal_price": 4100}
				"""));
		assertThat(check).isInstanceOfSatisfying(Rejected.class,
				(rejected) -> assertThat(rejected.code()).isEqualTo(IngestionErrorCode.UNKNOWN_DISTRICT));
	}

	@Test
	void v5PricesMustBePositiveDecimals() {
		assertRejected(record("\"modal_price\": \"abc\""), IngestionErrorCode.PRICE_NOT_NUMERIC);
		assertRejected(record("\"modal_price\": 0"), IngestionErrorCode.PRICE_NON_POSITIVE);
		assertRejected(record("\"min_price\": -5, \"modal_price\": 4100"), IngestionErrorCode.PRICE_NON_POSITIVE);
	}

	@Test
	void v6PricesMustBeOrdered() {
		assertRejected(record("\"min_price\": 4200, \"modal_price\": 4100"), IngestionErrorCode.PRICE_RANGE_INVALID);
		assertRejected(record("\"modal_price\": 4100, \"max_price\": 4000"), IngestionErrorCode.PRICE_RANGE_INVALID);
	}

	@Test
	void v7APerQuintalModalBelowOneHundredIsRejected() {
		assertRejected(record("\"modal_price\": 99.99"), IngestionErrorCode.PRICE_BELOW_UNIT_FLOOR);
		assertThat(check(record("\"modal_price\": 100"))).isInstanceOf(Accepted.class);
	}

	private void assertRejected(JsonNode record, IngestionErrorCode code) {
		assertThat(check(record)).isInstanceOfSatisfying(Rejected.class,
				(rejected) -> assertThat(rejected.code()).isEqualTo(code));
	}

	private RecordCheck check(JsonNode record) {
		return this.validator.check(SourceRecord.from(record, SourceSchema.CURRENT), this.lookups, TODAY);
	}

	static JsonNode record(String prices) {
		return record("Maharashtra", "14/09/2026", "Onion", prices);
	}

	static JsonNode record(String state, String date, String commodity, String prices) {
		return json("""
				{"state": "%s", "district": "Ahilyanagar", "market": "Rahuri(Vambori) ", "commodity": "%s",
				 "variety": "Other", "grade": "Local", "arrival_date": "%s", %s}
				""".formatted(state, commodity, date, prices));
	}

	static JsonNode json(String text) {
		return EXACT.readTree(text);
	}

	static CommodityInfo commodity(String name, String unit) {
		return new CommodityInfo(UUID.randomUUID(), name, null, null, "Vegetables", unit,
				(unit != null) ? "synthetic test evidence" : null, Instant.parse("2026-01-01T00:00:00Z"));
	}

}
