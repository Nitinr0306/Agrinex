package com.agrinex.price.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.agrinex.geography.DistrictInfo;
import com.agrinex.geography.GeographyApi;
import com.agrinex.price.MarketInfo;
import com.agrinex.price.MarketReviewStatus;
import com.agrinex.price.domain.MarketEntity;
import com.agrinex.price.mapper.PriceMapper;
import com.agrinex.price.repository.MarketRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Read access to markets, with their districts obtained through the geography module (price -> geography). */
@Service
@Transactional(readOnly = true)
class MarketDirectory {

	private final MarketRepository markets;

	private final GeographyApi geography;

	MarketDirectory(MarketRepository markets, GeographyApi geography) {
		this.markets = markets;
		this.geography = geography;
	}

	public Optional<MarketInfo> find(UUID marketId) {
		return this.markets.findById(marketId).map((market) -> toInfo(List.of(market)).getFirst());
	}

	public Map<UUID, MarketInfo> findAll(Collection<UUID> marketIds) {
		return toInfo(this.markets.findAllById(marketIds)).stream()
			.collect(Collectors.toMap(MarketInfo::id, Function.identity()));
	}

	/** VERIFIED, active markets with coordinates: comparison candidates (ADR-0014 item 5). */
	public List<MarketInfo> comparisonCandidates() {
		return verifiedActive().stream().filter(MarketInfo::comparisonCandidate).toList();
	}

	/** VERIFIED, active markets (alerts, ADR-0019; admin freshness summary). */
	public List<MarketInfo> verifiedActive() {
		return toInfo(this.markets.findByReviewStatusAndActiveTrue(MarketReviewStatus.VERIFIED));
	}

	public List<MarketInfo> toInfo(List<MarketEntity> entities) {
		Set<UUID> districtIds = entities.stream().map(MarketEntity::getDistrictId).collect(Collectors.toSet());
		Map<UUID, DistrictInfo> districts = this.geography.findDistricts(districtIds);
		return entities.stream()
			.map((market) -> PriceMapper.toInfo(market, districts.get(market.getDistrictId())))
			.toList();
	}

}
