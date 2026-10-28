package com.agrinex.geography;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.agrinex.common.web.PageParams;
import com.agrinex.common.web.PageResponse;

/**
 * Public interface of the geography module (owner of states, districts, localities and district source
 * aliases; ADR-0013, ADR-0014). Geography is a leaf module: it depends on no other module.
 */
public interface GeographyApi {

	List<StateRef> listStates();

	/** The districts of a state ordered by name, or empty when the state does not exist. */
	Optional<List<DistrictInfo>> listDistricts(UUID stateId);

	/** The localities of a district ordered by name, or empty when the district does not exist. */
	Optional<List<LocalityInfo>> listLocalities(UUID districtId);

	Optional<DistrictInfo> findDistrict(UUID districtId);

	Map<UUID, DistrictInfo> findDistricts(Collection<UUID> districtIds);

	/**
	 * The district whose stored centroid equals the given coordinates at 6 decimal places, used to accept a
	 * comparison origin (ADR-0039 item 2).
	 */
	Optional<DistrictInfo> findDistrictByCentroid(BigDecimal latitude, BigDecimal longitude);

	/**
	 * Resolves a normalized source (state, district) spelling through the district source aliases (ADR-0014
	 * item 3). Empty when the spelling has no mapping, which ingestion rejects as UNKNOWN_DISTRICT.
	 */
	Optional<DistrictInfo> resolveSourceDistrict(String sourceStateName, String sourceDistrictName);

	PageResponse<DistrictAliasView> listDistrictAliases(UUID districtId, PageParams page);

	/** Adds an alias; throws CONFLICT when the spelling is already mapped and REFERENCE_NOT_FOUND for an unknown district. */
	DistrictAliasView addDistrictAlias(String sourceStateName, String sourceDistrictName, UUID districtId,
			String evidenceNote);

}
