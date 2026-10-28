package com.agrinex.recommendation.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import com.agrinex.commodity.CommodityApi;
import com.agrinex.commodity.CommodityInfo;
import com.agrinex.commodity.NamedRef;
import com.agrinex.common.error.ApiException;
import com.agrinex.common.error.ErrorCode;

import org.springframework.stereotype.Component;

/**
 * Resolves the commodity and the optional exact variety and grade filters of a comparison (ADR-0010 item 3). An
 * unknown commodity, or a variety or grade of another commodity, answers 422 REFERENCE_NOT_FOUND naming each
 * field; a commodity whose verified unit is not Rs./Quintal answers 422 UNSUPPORTED_PRICE_UNIT (ADR-0008).
 */
@Component
class CommodityReferences {

	private final CommodityApi commodities;

	CommodityReferences(CommodityApi commodities) {
		this.commodities = commodities;
	}

	ComparedCommodity resolve(UUID commodityId, UUID varietyId, UUID gradeId) {
		Optional<CommodityInfo> commodity = this.commodities.findCommodity(commodityId);
		if (commodity.isEmpty()) {
			throw ApiException.referenceNotFound("commodityId");
		}
		Optional<NamedRef> variety = lookup(varietyId, commodityId, this.commodities::findVariety);
		Optional<NamedRef> grade = lookup(gradeId, commodityId, this.commodities::findGrade);
		List<String> missing = new ArrayList<>();
		if (varietyId != null && variety.isEmpty()) {
			missing.add("varietyId");
		}
		if (gradeId != null && grade.isEmpty()) {
			missing.add("gradeId");
		}
		if (!missing.isEmpty()) {
			throw ApiException.referenceNotFound(missing.toArray(String[]::new));
		}
		if (!commodity.get().pricedPerQuintal()) {
			throw new ApiException(ErrorCode.UNSUPPORTED_PRICE_UNIT,
					"The commodity's verified price unit is not " + CommodityInfo.RUPEES_PER_QUINTAL + ".");
		}
		return new ComparedCommodity(commodity.get(), variety.orElse(null), grade.orElse(null));
	}

	/** The optional variety or grade, looked up only when given, and only if it belongs to the commodity. */
	private static Optional<NamedRef> lookup(UUID id, UUID commodityId, Function<UUID, Optional<NamedRef>> find) {
		return (id == null) ? Optional.empty() : find.apply(id).filter((ref) -> ref.commodityId().equals(commodityId));
	}

}
