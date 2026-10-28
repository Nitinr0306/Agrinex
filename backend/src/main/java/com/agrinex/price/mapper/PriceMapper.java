package com.agrinex.price.mapper;

import java.util.Map;
import java.util.UUID;

import com.agrinex.commodity.NamedRef;
import com.agrinex.geography.DistrictInfo;
import com.agrinex.price.MarketAliasView;
import com.agrinex.price.MarketInfo;
import com.agrinex.price.SelectedObservation;
import com.agrinex.price.SourceDatasetInfo;
import com.agrinex.price.domain.MarketEntity;
import com.agrinex.price.domain.MarketSourceAliasEntity;
import com.agrinex.price.domain.ObservationRow;
import com.agrinex.price.domain.PriceObservationEntity;

/** Maps price entities to the module's public records; entities never leave the module. */
public final class PriceMapper {

	private PriceMapper() {
	}

	public static MarketInfo toInfo(MarketEntity market, DistrictInfo district) {
		return new MarketInfo(market.getId(), market.getCanonicalName(), market.getCanonicalSlug(), district,
				market.getLat(), market.getLon(), market.isCoordinateApproximate(), market.getCoordinateSource(),
				market.isActive(), market.getReviewStatus(), market.getMergedIntoMarketId(), market.getCreatedAt());
	}

	public static MarketAliasView toView(MarketSourceAliasEntity alias) {
		return new MarketAliasView(alias.getSourceStateName(), alias.getSourceDistrictName(),
				alias.getSourceMarketName(), alias.getSourceMarketNameRaw(), alias.getEvidenceNote(),
				alias.getCreatedAt());
	}

	public static ObservationRow toRow(PriceObservationEntity observation, SourceDatasetInfo dataset,
			Map<UUID, NamedRef> varieties, Map<UUID, NamedRef> grades) {
		return new ObservationRow(observation.getId(), observation.getMarketId(), observation.getCommodityId(),
				observation.getVarietyId(), name(varieties, observation.getVarietyId()), observation.getGradeId(),
				name(grades, observation.getGradeId()), observation.getReportDate(), observation.getModalPrice(),
				observation.getMinPrice(), observation.getMaxPrice(), observation.getPriceUnit(), dataset.type(),
				dataset.resourceId(), observation.getIngestedAt());
	}

	public static SelectedObservation toSelected(ObservationRow row) {
		return new SelectedObservation(row.id(), row.marketId(), row.commodityId(), row.varietyId(), row.varietyName(),
				row.gradeId(), row.gradeName(), row.reportDate(), row.modalPrice(), row.minPrice(), row.maxPrice(),
				row.priceUnit(), row.sourceDataset(), row.sourceResourceId(), row.ingestedAt());
	}

	private static String name(Map<UUID, NamedRef> refs, UUID id) {
		if (id == null) {
			return null;
		}
		NamedRef ref = refs.get(id);
		return (ref != null) ? ref.name() : null;
	}

}
