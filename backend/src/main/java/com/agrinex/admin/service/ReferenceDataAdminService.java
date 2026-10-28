package com.agrinex.admin.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.agrinex.admin.domain.AuditAction;
import com.agrinex.admin.domain.AuditTarget;
import com.agrinex.commodity.CommodityApi;
import com.agrinex.commodity.CommodityInfo;
import com.agrinex.common.error.ApiException;
import com.agrinex.common.web.PageParams;
import com.agrinex.common.web.PageResponse;
import com.agrinex.geography.DistrictAliasView;
import com.agrinex.geography.GeographyApi;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Curation of reference data with evidence (ADR-0006, ADR-0008, ADR-0014 item 3, ADR-0015, ADR-0020 item 1): the
 * commodity known list, verified units, localized names and district source aliases. Each change goes through
 * the owning module and is audited with its evidence in the same transaction.
 */
@Service
public class ReferenceDataAdminService {

	private final CommodityApi commodities;

	private final GeographyApi geography;

	private final AdminAuditLog audit;

	ReferenceDataAdminService(CommodityApi commodities, GeographyApi geography, AdminAuditLog audit) {
		this.commodities = commodities;
		this.geography = geography;
		this.audit = audit;
	}

	public PageResponse<CommodityInfo> listCommodities(Boolean unitVerified, String search, PageParams page) {
		return this.commodities.listAdminCommodities(unitVerified, search, page);
	}

	/** The source reference is kept in the audit details (contract AddCommodityRequest). */
	@Transactional
	public CommodityInfo addCommodity(UUID actorUserId, String name, String category, String priceUnit,
			String priceUnitSource, String sourceReference) {
		CommodityInfo added = this.commodities.addCommodity(name, category, priceUnit, priceUnitSource);
		Map<String, Object> details = new LinkedHashMap<>();
		details.put("after", commodityValues(added));
		details.put("sourceReference", sourceReference);
		this.audit.record(actorUserId, AuditAction.COMMODITY_ADDED, AuditTarget.COMMODITY, added.id(), details);
		return added;
	}

	@Transactional
	public CommodityInfo verifyPriceUnit(UUID actorUserId, UUID commodityId, String priceUnit,
			String priceUnitSource) {
		CommodityInfo before = requireCommodity(commodityId);
		CommodityInfo after = this.commodities.verifyPriceUnit(commodityId, priceUnit, priceUnitSource);
		this.audit.record(actorUserId, AuditAction.COMMODITY_UNIT_VERIFIED, AuditTarget.COMMODITY, commodityId,
				Map.of("before", unitValues(before), "after", unitValues(after)));
		return after;
	}

	/** The provenance note is kept in the audit details (ADR-0015 item 1). */
	@Transactional
	public CommodityInfo updateNames(UUID actorUserId, UUID commodityId, String nameHi, String nameMr,
			String provenanceNote) {
		CommodityInfo before = requireCommodity(commodityId);
		CommodityInfo after = this.commodities.updateNames(commodityId, nameHi, nameMr);
		Map<String, Object> details = new LinkedHashMap<>();
		details.put("before", nameValues(before));
		details.put("after", nameValues(after));
		details.put("provenanceNote", provenanceNote);
		this.audit.record(actorUserId, AuditAction.COMMODITY_NAMES_UPDATED, AuditTarget.COMMODITY, commodityId,
				details);
		return after;
	}

	/** An unknown districtId answers 422 REFERENCE_NOT_FOUND (contract DistrictIdQuery). */
	public PageResponse<DistrictAliasView> listDistrictAliases(UUID districtId, PageParams page) {
		if (districtId != null && this.geography.findDistrict(districtId).isEmpty()) {
			throw ApiException.referenceNotFound("districtId");
		}
		return this.geography.listDistrictAliases(districtId, page);
	}

	@Transactional
	public DistrictAliasView addDistrictAlias(UUID actorUserId, String sourceStateName, String sourceDistrictName,
			UUID districtId, String evidenceNote) {
		DistrictAliasView alias = this.geography.addDistrictAlias(sourceStateName, sourceDistrictName, districtId,
				evidenceNote);
		Map<String, Object> after = new LinkedHashMap<>();
		after.put("sourceStateName", alias.sourceStateName());
		after.put("sourceDistrictName", alias.sourceDistrictName());
		after.put("districtId", alias.district().id().toString());
		after.put("evidenceNote", alias.evidenceNote());
		this.audit.record(actorUserId, AuditAction.DISTRICT_ALIAS_ADDED, AuditTarget.DISTRICT_ALIAS, alias.id(),
				Map.of("after", after));
		return alias;
	}

	private CommodityInfo requireCommodity(UUID commodityId) {
		return this.commodities.findCommodity(commodityId)
			.orElseThrow(() -> ApiException.notFound("The commodity does not exist."));
	}

	private static Map<String, Object> commodityValues(CommodityInfo commodity) {
		Map<String, Object> values = new LinkedHashMap<>();
		values.put("name", commodity.name());
		values.put("category", commodity.category());
		values.putAll(unitValues(commodity));
		return values;
	}

	private static Map<String, Object> unitValues(CommodityInfo commodity) {
		Map<String, Object> values = new LinkedHashMap<>();
		values.put("priceUnit", commodity.priceUnit());
		values.put("priceUnitSource", commodity.priceUnitSource());
		return values;
	}

	private static Map<String, Object> nameValues(CommodityInfo commodity) {
		Map<String, Object> values = new LinkedHashMap<>();
		values.put("nameHi", commodity.nameHi());
		values.put("nameMr", commodity.nameMr());
		return values;
	}

}
