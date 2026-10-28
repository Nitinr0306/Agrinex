package com.agrinex.commodity;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.agrinex.common.web.PageParams;
import com.agrinex.common.web.PageResponse;

/**
 * Public interface of the commodity module (owner of commodities, varieties and grades; ADR-0013). Commodity is
 * a leaf module.
 */
public interface CommodityApi {

	/** The commodity selector: commodities with recent observations in coverage, ordered by name (ADR-0035). */
	List<CommodityInfo> listOfferedCommodities();

	Optional<List<NamedRef>> listVarieties(UUID commodityId);

	Optional<List<NamedRef>> listGrades(UUID commodityId);

	Optional<CommodityInfo> findCommodity(UUID commodityId);

	Map<UUID, CommodityInfo> findCommodities(Collection<UUID> commodityIds);

	/** Exact, case-sensitive match of a whitespace-normalized source name (ADR-0006 item 2). */
	Optional<CommodityInfo> findByCanonicalName(String normalizedName);

	/**
	 * The commodity a whitespace-normalized source name stands for: an exact match of the master name first, then
	 * an exact match in {@code commodity_source_aliases} (ADR-0051). Empty when neither matches.
	 */
	Optional<CommodityInfo> resolveSourceName(String normalizedName);

	Optional<NamedRef> findVariety(UUID varietyId);

	Optional<NamedRef> findGrade(UUID gradeId);

	Map<UUID, NamedRef> findVarieties(Collection<UUID> varietyIds);

	Map<UUID, NamedRef> findGrades(Collection<UUID> gradeIds);

	/** Registers a variety on first appearance with the exact normalized source value, or returns the existing one (ADR-0006 item 3). */
	UUID registerVariety(UUID commodityId, String normalizedName);

	/** Registers a grade on first appearance with the exact normalized source value, or returns the existing one. */
	UUID registerGrade(UUID commodityId, String normalizedName);

	PageResponse<CommodityInfo> listAdminCommodities(Boolean unitVerified, String search, PageParams page);

	/** Adds a commodity with its verified unit and evidence; CONFLICT when the name exists (ADR-0006, ADR-0008). */
	CommodityInfo addCommodity(String name, String category, String priceUnit, String priceUnitSource);

	/** Records the unit of a commodity that has none; CONFLICT when a unit is already recorded (ADR-0008). */
	CommodityInfo verifyPriceUnit(UUID commodityId, String priceUnit, String priceUnitSource);

	/** Replaces both localized names; null removes a translation (ADR-0015). */
	CommodityInfo updateNames(UUID commodityId, String nameHi, String nameMr);

}
