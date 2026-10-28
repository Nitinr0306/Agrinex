package com.agrinex.ingestion.pipeline;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.agrinex.commodity.CommodityApi;
import com.agrinex.commodity.CommodityInfo;
import com.agrinex.geography.DistrictInfo;
import com.agrinex.geography.GeographyApi;
import com.agrinex.price.PriceApi;

/**
 * Resolves source spellings to reference ids for one run, through the owning modules' interfaces (ADR-0013),
 * remembering each answer for the rest of the run. Commodities must already be known (ADR-0006 item 2); districts
 * resolve through their source aliases (ADR-0014 item 3); markets, varieties and grades are registered on first
 * sight (ADR-0006 item 3, ADR-0014 item 4).
 */
public final class ReferenceLookups {

	private final CommodityApi commodities;

	private final GeographyApi geography;

	private final PriceApi prices;

	private final UUID ingestionRunId;

	private final Map<String, Optional<CommodityInfo>> commodityByName = new HashMap<>();

	private final Map<List<String>, Optional<DistrictInfo>> districtBySource = new HashMap<>();

	private final Map<List<String>, UUID> marketBySource = new HashMap<>();

	private final Map<List<Object>, UUID> varietyByName = new HashMap<>();

	private final Map<List<Object>, UUID> gradeByName = new HashMap<>();

	ReferenceLookups(CommodityApi commodities, GeographyApi geography, PriceApi prices, UUID ingestionRunId) {
		this.commodities = commodities;
		this.geography = geography;
		this.prices = prices;
		this.ingestionRunId = ingestionRunId;
	}

	Optional<CommodityInfo> commodity(String normalizedName) {
		return this.commodityByName.computeIfAbsent(normalizedName, this.commodities::resolveSourceName);
	}

	Optional<DistrictInfo> district(String sourceState, String sourceDistrict) {
		return this.districtBySource.computeIfAbsent(List.of(sourceState, sourceDistrict),
				(key) -> this.geography.resolveSourceDistrict(sourceState, sourceDistrict));
	}

	UUID market(SourceRecord record, DistrictInfo district) {
		return this.marketBySource.computeIfAbsent(List.of(record.state(), record.district(), record.market()),
				(key) -> this.prices
					.resolveOrRegisterMarket(record.state(), record.district(), record.market(), record.marketRaw(),
							district, this.ingestionRunId)
					.marketId());
	}

	/** The variety id, or null when the source gives none (ADR-0009 item 1). */
	UUID variety(UUID commodityId, String normalizedName) {
		return (normalizedName == null) ? null : this.varietyByName.computeIfAbsent(List.of(commodityId, normalizedName),
				(key) -> this.commodities.registerVariety(commodityId, normalizedName));
	}

	/** The grade id, or null when the source gives none (ADR-0009 item 1). */
	UUID grade(UUID commodityId, String normalizedName) {
		return (normalizedName == null) ? null : this.gradeByName.computeIfAbsent(List.of(commodityId, normalizedName),
				(key) -> this.commodities.registerGrade(commodityId, normalizedName));
	}

}
