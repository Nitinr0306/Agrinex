package com.agrinex;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.agrinex.commodity.CommodityApi;
import com.agrinex.geography.DistrictInfo;
import com.agrinex.geography.GeographyApi;

import static com.agrinex.SyntheticFlows.record;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * The real reference data of migrations V10 and V11 (Task 12): Maharashtra's LGD districts with Survey of India
 * centroids, the district spellings the government feed uses, the LGD sub-districts, and the Agmarknet 2.0 commodity
 * master with the units of the 13-Oct-2026 report exports. Expected values come from the evidence files in
 * docs/data/raw/ (LGD_Maharashtra_2026-10-15.json, SoI_Maharashtra_district_centroids_2026-10-15.csv and the
 * report exports). The ingestion case sends SYNTHETIC prices and market names through the real spellings. V12's
 * commodity aliases come from the DMI Commodity_Code evidence in docs/decision-review/TASK_12_OWNER_DECISION_PACKET.md
 * (D1, ADR-0051).
 */
class ReferenceDataSeedIntegrationTest extends IntegrationTestSupport {

	/** Every Maharashtra district spelling in both resources, 2025-10-14 to 2026-10-14, plus Sindhudurg. */
	static final Map<String, Integer> SOURCE_DISTRICTS = Map.ofEntries(entry("Ahilyanagar", 466),
			entry("Ahmednagar", 466), entry("Akola", 467), entry("Amarawati", 468), entry("Beed", 470),
			entry("Bhandara", 471), entry("Buldhana", 472), entry("Chandrapur", 473),
			entry("Chattrapati Sambhajinagar", 469), entry("Dharashiv", 488), entry("Dharashiv(Usmanabad)", 488),
			entry("Dhule", 474), entry("Gadchiroli", 475), entry("Gondiya", 476), entry("Hingoli", 477),
			entry("Jalana", 479), entry("Jalgaon", 478), entry("Jalna", 479), entry("Kolhapur", 480),
			entry("Latur", 481), entry("Mumbai", 482), entry("Nagpur", 484), entry("Nanded", 485),
			entry("Nandurbar", 486), entry("Nashik", 487), entry("Palghar", 665), entry("Parbhani", 489),
			entry("Pune", 490), entry("Raigad", 491), entry("Ratnagiri", 492), entry("Sangli", 493),
			entry("Satara", 494), entry("Sholapur", 496), entry("Sindhudurg", 495), entry("Solapur", 496),
			entry("Thane", 497), entry("Vashim", 499), entry("Wardha", 498), entry("Washim", 499),
			entry("Yavatmal", 500));

	@Autowired
	GeographyApi geography;

	@Autowired
	CommodityApi commodities;

	@Test
	void maharashtraHasItsThirtySixLgdDistrictsWithSurveyOfIndiaCentroids() {
		assertThat(this.jdbc.queryForMap("SELECT name, code, lgd_code, name_mr FROM states WHERE lgd_code = 27"))
			.containsEntry("name", "Maharashtra")
			.containsEntry("code", "IN-MH")
			.containsEntry("name_mr", "महाराष्ट्र");
		List<Map<String, Object>> districts = this.jdbc.queryForList("""
				SELECT d.lgd_code, d.name, d.centroid_lat, d.centroid_lon, d.centroid_source
				FROM districts d JOIN states s ON s.id = d.state_id WHERE s.lgd_code = 27 ORDER BY d.lgd_code""");
		assertThat(districts).hasSize(36);
		// LGD codes 466 to 500 and Palghar 665 (docs/data/raw/LGD_Maharashtra_2026-10-15.json).
		List<Object> lgdCodes = new ArrayList<>(IntStream.rangeClosed(466, 500).boxed().toList());
		lgdCodes.add(665);
		assertThat(districts).extracting((row) -> row.get("lgd_code")).containsExactlyElementsOf(lgdCodes);
		assertThat(districts).allSatisfy((row) -> {
			// Maharashtra lies within about 15.6-22.1 N and 72.6-80.9 E.
			assertThat((BigDecimal) row.get("centroid_lat")).isBetween(new BigDecimal("15.6"), new BigDecimal("22.1"));
			assertThat((BigDecimal) row.get("centroid_lon")).isBetween(new BigDecimal("72.6"), new BigDecimal("80.9"));
			assertThat((String) row.get("centroid_source"))
				.startsWith("Survey of India ABDB District Boundary SOI/ABDB/VECTOR/50000/2025/DISTRICT/INDIA");
		});
		assertThat(this.jdbc.queryForMap(
				"SELECT name, centroid_lat::text AS lat, centroid_lon::text AS lon FROM districts WHERE lgd_code = 490"))
			.containsEntry("name", "Pune")
			.containsEntry("lat", "18.570554")
			.containsEntry("lon", "74.069108");
		assertThat(this.jdbc.queryForMap(
				"SELECT name, centroid_lat::text AS lat, centroid_lon::text AS lon FROM districts WHERE lgd_code = 665"))
			.containsEntry("name", "Palghar")
			.containsEntry("lat", "19.792523")
			.containsEntry("lon", "73.011140");
	}

	@Test
	void everyDistrictSpellingOfTheFeedResolvesToItsLgdDistrict() {
		SOURCE_DISTRICTS.forEach((spelling, lgdCode) -> {
			DistrictInfo district = this.geography.resolveSourceDistrict("Maharashtra", spelling).orElseThrow();
			assertThat(lgdCodeOf(district.id())).as(spelling).isEqualTo(lgdCode);
		});
		// Agmarknet master entries that never occur in the feed stay unmapped (SEED_AND_REFERENCE_DATA section 3.2).
		for (String unmapped : List.of("Osmanabad", "Bandra(E)", "Murum")) {
			assertThat(this.geography.resolveSourceDistrict("Maharashtra", unmapped)).as(unmapped).isEmpty();
		}
		// The seeded aliases only: other tests add SYNTHETIC aliases to synthetic states in the shared database.
		assertThat(this.jdbc.queryForList("""
				SELECT a.evidence_note FROM district_source_aliases a JOIN districts d ON d.id = a.district_id
				JOIN states s ON s.id = d.state_id WHERE s.lgd_code = 27""", String.class))
			.hasSize(SOURCE_DISTRICTS.size())
			.allSatisfy((note) -> assertThat(note).contains("LGD"));
	}

	@Test
	void localitiesAreTheLgdSubDistrictsWithoutCoordinates() {
		assertThat(this.jdbc.queryForObject("""
				SELECT count(*) FROM localities l JOIN districts d ON d.id = l.district_id
				JOIN states s ON s.id = d.state_id WHERE s.lgd_code = 27""", Integer.class)).isEqualTo(358);
		assertThat(this.jdbc.queryForObject("SELECT count(*) FROM localities WHERE lat IS NOT NULL", Integer.class))
			.isZero();
		assertThat(this.jdbc.queryForList("""
				SELECT l.name FROM localities l JOIN districts d ON d.id = l.district_id
				WHERE d.lgd_code = 490 ORDER BY l.name""", String.class))
			.containsExactly("Ambegaon", "Baramati", "Bhor", "Daund", "Haveli", "Indapur", "Junnar", "Khed", "Mawal",
					"Mulshi", "Pune City", "Purandhar", "Rajgad", "Shirur");
		// LGD lists no sub-district for Mumbai (city).
		assertThat(this.jdbc.queryForObject(
				"SELECT count(*) FROM localities l JOIN districts d ON d.id = l.district_id WHERE d.lgd_code = 482",
				Integer.class))
			.isZero();
	}

	@Test
	void theKnownListIsTheAgmarknetMasterWithTheUnitsTheReportsShow() {
		assertThat(this.jdbc.queryForObject("SELECT count(*) FROM commodities", Integer.class)).isEqualTo(605);
		assertThat(this.jdbc.queryForObject("SELECT count(DISTINCT category) FROM commodities", Integer.class))
			.isEqualTo(16);
		Map<String, Integer> units = Map.of("Rs./Quintal", 277, "Rs./Unit", 18, "Rs./Bundle", 3);
		units.forEach((unit, count) -> assertThat(
				this.jdbc.queryForObject("SELECT count(*) FROM commodities WHERE price_unit = ?", Integer.class, unit))
			.as(unit)
			.isEqualTo(count));
		assertThat(this.jdbc.queryForObject("SELECT count(*) FROM commodities WHERE price_unit IS NULL", Integer.class))
			.isEqualTo(605 - 298);
		assertThat(unitOf("Brinjal")).containsEntry("price_unit", "Rs./Quintal")
			.containsEntry("category", "Vegetables");
		assertThat((String) unitOf("Brinjal").get("price_unit_source")).contains("report date 13-Oct-2026");
		assertThat(unitOf("Coriander(Leaves)")).containsEntry("price_unit", "Rs./Bundle");
		assertThat(unitOf("Goat")).containsEntry("price_unit", "Rs./Unit");
		// No export row on 13-Oct-2026: out of season.
		assertThat(unitOf("Jamun(Narale Hannu)")).containsEntry("price_unit", null);
		// V8's evidence is kept, and master names are stored whitespace-normalized.
		assertThat((String) unitOf("Onion").get("price_unit_source")).contains("Market-wise dashboard");
		assertThat(unitOf("Little Millet")).containsEntry("category", "Cereals");
	}

	@Test
	void ingestionResolvesRealSpellingsAndRejectsWhatTheReferenceDataCannotSupport() throws Exception {
		SyntheticFlows flows = new SyntheticFlows(this.mvc, this.jdbc, adminToken());
		LocalDate today = SyntheticFlows.todayInIndia();
		SOURCE.serve(SyntheticFlows.CURRENT_RESOURCE, List.of(
				record("Amarawati", "SYNTHETIC Seed Mandi A", "Brinjal", today, "2400"),
				record("Vashim", "SYNTHETIC Seed Mandi B", "Coriander(Leaves)", today, "10"),
				record("Pune", "SYNTHETIC Seed Mandi C", "Methi(Leaves)", today, "15"),
				record("Pune", "SYNTHETIC Seed Mandi C", "Jamun(Narale Hannu)", today, "5000"),
				record("Pune", "SYNTHETIC Seed Mandi C", "Arhar(Tur/Red Gram)(Whole)", today, "7000"),
				record("Pune", "SYNTHETIC Seed Mandi C", "SYNTHETIC Unlisted Crop", today, "7000"),
				record("Bandra(E)", "SYNTHETIC Seed Mandi D", "Onion", today, "1800")));

		String run = flows.ingest();
		UUID runId = UUID.fromString(JsonPath.read(run, "$.id"));

		assertThat(JsonPath.<Integer>read(run, "$.recordsInserted")).isEqualTo(3);
		assertThat(this.jdbc.queryForList("""
				SELECT d.lgd_code, c.canonical_name, o.price_unit, o.modal_price::text AS modal
				FROM price_observations o JOIN markets m ON m.id = o.market_id JOIN districts d ON d.id = m.district_id
				JOIN commodities c ON c.id = o.commodity_id WHERE o.ingestion_run_id = ? ORDER BY d.lgd_code""",
				runId))
			.containsExactly(Map.of("lgd_code", 468, "canonical_name", "Brinjal", "price_unit", "Rs./Quintal",
					"modal", "2400.00"),
					Map.of("lgd_code", 490, "canonical_name", "Red gram/Arhar/Tur(whole)", "price_unit",
							"Rs./Quintal", "modal", "7000.00"),
					Map.of("lgd_code", 499, "canonical_name", "Coriander(Leaves)", "price_unit", "Rs./Bundle",
							"modal", "10.00"));
		// The legacy spelling resolved through its alias is kept on the row; canonical spellings store NULL.
		assertThat(this.jdbc.queryForList("""
				SELECT coalesce(o.source_commodity_name, '<canonical>') FROM price_observations o
				JOIN commodities c ON c.id = o.commodity_id WHERE o.ingestion_run_id = ? ORDER BY c.canonical_name""",
				String.class, runId))
			.containsExactly("<canonical>", "<canonical>", "Arhar(Tur/Red Gram)(Whole)");
		assertThat(this.jdbc.queryForList(
				"SELECT error_code FROM ingestion_errors WHERE ingestion_run_id = ? ORDER BY error_code", String.class,
				runId))
			.containsExactly("PRICE_BELOW_UNIT_FLOOR", "UNIT_UNVERIFIED", "UNKNOWN_COMMODITY", "UNKNOWN_DISTRICT");
		// Auto-registered markets take their district's centroid as approximate coordinates (ADR-0014 item 4).
		assertThat(this.jdbc.queryForMap("""
				SELECT m.review_status, m.is_coordinate_approximate, m.lat = d.centroid_lat AND m.lon = d.centroid_lon AS at_centroid
				FROM markets m JOIN districts d ON d.id = m.district_id WHERE m.canonical_name = 'SYNTHETIC Seed Mandi A'"""))
			.containsEntry("review_status", "PROVISIONAL")
			.containsEntry("is_coordinate_approximate", true)
			.containsEntry("at_centroid", true);
	}

	@Test
	void legacyDmiSpellingsResolveOneToOneThroughEvidencedAliases() {
		List<Map<String, Object>> aliases = this.jdbc.queryForList("""
				SELECT a.source_commodity_name, c.canonical_name, c.price_unit, a.evidence_note
				FROM commodity_source_aliases a JOIN commodities c ON c.id = a.commodity_id""");
		assertThat(aliases).hasSize(20).allSatisfy((row) -> {
			assertThat((String) row.get("evidence_note")).contains("Commodity_Code").contains("ADR-0051");
			assertThat(row.get("price_unit")).isEqualTo("Rs./Quintal");
		});
		// An alias never names a master commodity, so the exact master match always wins.
		assertThat(this.jdbc.queryForObject("""
				SELECT count(*) FROM commodity_source_aliases a
				JOIN commodities c ON c.canonical_name = a.source_commodity_name""", Integer.class)).isZero();
		// Two spellings carry DMI code 49; each still names exactly one master commodity.
		assertThat(this.commodities.resolveSourceName("Arhar(Tur/Red Gram)(Whole)").orElseThrow().name())
			.isEqualTo("Red gram/Arhar/Tur(whole)");
		assertThat(this.commodities.resolveSourceName("Arhar (Tur/Red Gram)(Whole)").orElseThrow().name())
			.isEqualTo("Red gram/Arhar/Tur(whole)");
		assertThat(this.commodities.resolveSourceName("Cardamoms").orElseThrow().name()).isEqualTo("Cardamom");
		assertThat(this.commodities.resolveSourceName("Brinjal").orElseThrow().name()).isEqualTo("Brinjal");
		// Matching stays exact: no case folding and no fuzzy matching.
		assertThat(this.commodities.resolveSourceName("cardamoms")).isEmpty();
		assertThat(this.commodities.resolveSourceName("Arhar")).isEmpty();
	}

	private int lgdCodeOf(UUID districtId) {
		return this.jdbc.queryForObject("SELECT lgd_code FROM districts WHERE id = ?", Integer.class, districtId);
	}

	private Map<String, Object> unitOf(String commodity) {
		return this.jdbc.queryForMap(
				"SELECT price_unit, price_unit_source, category FROM commodities WHERE canonical_name = ?", commodity);
	}

}
