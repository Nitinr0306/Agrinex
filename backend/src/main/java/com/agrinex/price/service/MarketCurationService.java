package com.agrinex.price.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.agrinex.common.error.ApiException;
import com.agrinex.common.web.PageParams;
import com.agrinex.common.web.PageResponse;
import com.agrinex.price.MarketAliasView;
import com.agrinex.price.MarketCurationApi;
import com.agrinex.price.MarketCurationResult;
import com.agrinex.price.MarketInfo;
import com.agrinex.price.MarketReviewStatus;
import com.agrinex.price.domain.BusinessKeys;
import com.agrinex.price.domain.MarketEntity;
import com.agrinex.price.domain.MarketSourceAliasEntity;
import com.agrinex.price.domain.PriceObservationEntity;
import com.agrinex.price.mapper.PriceMapper;
import com.agrinex.price.repository.MarketRepository;
import com.agrinex.price.repository.MarketSourceAliasRepository;
import com.agrinex.price.repository.PriceObservationRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin curation of markets (ADR-0014 item 4; ADR-0020 item 1). Merges are never automatic: they need an evidence
 * note, move the aliases to the target and re-point the observations under the identity rules of ADR-0009. Each
 * change returns masked before/after details for the audit event the admin module writes in the same transaction.
 */
@Service
@Transactional(readOnly = true)
class MarketCurationService implements MarketCurationApi {

	private static final Comparator<MarketAliasView> ALIAS_ORDER = Comparator.comparing(MarketAliasView::createdAt)
		.thenComparing(MarketAliasView::sourceStateName, String::compareTo)
		.thenComparing(MarketAliasView::sourceDistrictName, String::compareTo)
		.thenComparing(MarketAliasView::sourceMarketName, String::compareTo);

	private final MarketRepository markets;

	private final MarketSourceAliasRepository aliases;

	private final PriceObservationRepository observations;

	private final MarketDirectory directory;

	MarketCurationService(MarketRepository markets, MarketSourceAliasRepository aliases,
			PriceObservationRepository observations, MarketDirectory directory) {
		this.markets = markets;
		this.aliases = aliases;
		this.observations = observations;
		this.directory = directory;
	}

	@Override
	public PageResponse<MarketInfo> listMarkets(MarketReviewStatus reviewStatus, Boolean active, UUID districtId,
			String search, PageParams page) {
		String status = (reviewStatus != null) ? reviewStatus.name() : null;
		List<MarketEntity> entities = this.markets.findAdminPage(status, active, districtId, search, page.size(),
				page.offset());
		return page.toResponse(this.directory.toInfo(entities),
				this.markets.countAdminPage(status, active, districtId, search));
	}

	@Override
	public Optional<MarketInfo> findMarket(UUID marketId) {
		return this.directory.find(marketId);
	}

	@Override
	public List<MarketAliasView> aliases(UUID marketId) {
		return this.aliases.findByMarketId(marketId).stream().map(PriceMapper::toView).sorted(ALIAS_ORDER).toList();
	}

	@Override
	public Optional<LocalDate> latestReportDate(UUID marketId) {
		return this.observations.findLatestReportDateOfMarket(marketId);
	}

	@Override
	@Transactional
	public MarketCurationResult verify(UUID marketId) {
		MarketEntity market = lock(marketId);
		if (market.getReviewStatus() != MarketReviewStatus.PROVISIONAL) {
			throw ApiException.conflict("Only a PROVISIONAL market can be verified.");
		}
		market.verify();
		return result(market, change("reviewStatus", MarketReviewStatus.PROVISIONAL, MarketReviewStatus.VERIFIED));
	}

	@Override
	@Transactional
	public MarketCurationResult deactivate(UUID marketId) {
		MarketEntity market = lock(marketId);
		requireNotMerged(market);
		if (!market.isActive()) {
			throw ApiException.conflict("The market is already inactive.");
		}
		market.deactivate();
		return result(market, change("active", true, false));
	}

	@Override
	@Transactional
	public MarketCurationResult reactivate(UUID marketId) {
		MarketEntity market = lock(marketId);
		requireNotMerged(market);
		if (market.isActive()) {
			throw ApiException.conflict("The market is already active.");
		}
		market.reactivate();
		return result(market, change("active", false, true));
	}

	@Override
	@Transactional
	public MarketCurationResult setCoordinates(UUID marketId, BigDecimal lat, BigDecimal lon, String coordinateSource) {
		MarketEntity market = lock(marketId);
		requireNotMerged(market);
		Map<String, Object> before = coordinates(market);
		market.setExactCoordinates(lat, lon, coordinateSource);
		Map<String, Object> details = new LinkedHashMap<>();
		details.put("before", before);
		details.put("after", coordinates(market));
		return result(market, details);
	}

	@Override
	@Transactional
	public MarketCurationResult merge(UUID marketId, UUID targetMarketId, String evidenceNote) {
		if (!this.markets.existsById(marketId)) {
			throw ApiException.notFound("The market does not exist.");
		}
		if (!this.markets.existsById(targetMarketId)) {
			throw ApiException.referenceNotFound("targetMarketId");
		}
		if (marketId.equals(targetMarketId)) {
			throw ApiException.conflict("A market cannot be merged into itself.");
		}
		Map<UUID, MarketEntity> locked = lockInIdOrder(marketId, targetMarketId);
		MarketEntity market = locked.get(marketId);
		MarketEntity target = locked.get(targetMarketId);
		if (market.getReviewStatus() == MarketReviewStatus.MERGED) {
			throw ApiException.conflict("The market is already merged.");
		}
		if (target.getReviewStatus() == MarketReviewStatus.MERGED) {
			throw ApiException.conflict("The target market is itself merged into another market.");
		}
		Map<String, Object> details = new LinkedHashMap<>();
		details.put("targetMarketId", targetMarketId.toString());
		details.put("evidenceNote", evidenceNote);
		details.put("aliasesMoved", moveAliases(marketId, targetMarketId, evidenceNote));
		details.putAll(repointObservations(marketId, targetMarketId));
		market.mergeInto(targetMarketId);
		try {
			this.markets.flush();
			this.observations.flush();
		}
		catch (DataIntegrityViolationException referenced) {
			throw ApiException.conflict("The merge would remove an observation that recorded comparisons, alert events "
					+ "or revisions still reference; nothing was changed.");
		}
		return result(market, details);
	}

	private List<Map<String, Object>> moveAliases(UUID marketId, UUID targetMarketId, String evidenceNote) {
		List<Map<String, Object>> moved = new ArrayList<>();
		for (MarketSourceAliasEntity alias : this.aliases.findByMarketId(marketId)) {
			Map<String, Object> entry = new LinkedHashMap<>();
			entry.put("sourceStateName", alias.getSourceStateName());
			entry.put("sourceDistrictName", alias.getSourceDistrictName());
			entry.put("sourceMarketName", alias.getSourceMarketName());
			entry.put("previousEvidenceNote", alias.getEvidenceNote());
			moved.add(entry);
			alias.moveTo(targetMarketId, evidenceNote);
		}
		return moved;
	}

	/**
	 * Re-points the merged market's observations to the target under the identity rules: a copy whose identity
	 * already exists at the target is dropped; if its prices differ, the target's value is kept and the dropped
	 * values are recorded in the audit details (ADR-0014 item 4).
	 */
	private Map<String, Object> repointObservations(UUID marketId, UUID targetMarketId) {
		List<PriceObservationEntity> moving = this.observations.findByMarketId(marketId);
		Map<String, PriceObservationEntity> newKeys = new HashMap<>();
		for (PriceObservationEntity observation : moving) {
			newKeys.put(BusinessKeys.of(observation.getSourceDatasetId(), targetMarketId, observation.getCommodityId(),
					observation.getVarietyId(), observation.getGradeId(), observation.getReportDate()), observation);
		}
		Map<String, PriceObservationEntity> atTarget = new HashMap<>();
		this.observations.findByBusinessKeyIn(newKeys.keySet())
			.forEach((existing) -> atTarget.put(existing.getBusinessKey(), existing));
		int moved = 0;
		int identicalDropped = 0;
		List<Map<String, Object>> conflictingDropped = new ArrayList<>();
		for (Map.Entry<String, PriceObservationEntity> entry : newKeys.entrySet()) {
			PriceObservationEntity observation = entry.getValue();
			PriceObservationEntity kept = atTarget.get(entry.getKey());
			if (kept == null) {
				observation.repoint(targetMarketId, entry.getKey());
				moved++;
			}
			else {
				if (samePrices(kept, observation)) {
					identicalDropped++;
				}
				else {
					conflictingDropped.add(dropped(observation, kept));
				}
				this.observations.delete(observation);
			}
		}
		Map<String, Object> details = new LinkedHashMap<>();
		details.put("observationsMoved", moved);
		details.put("identicalCopiesDropped", identicalDropped);
		details.put("conflictingCopiesDropped", conflictingDropped);
		return details;
	}

	private static boolean samePrices(PriceObservationEntity a, PriceObservationEntity b) {
		return a.getModalPrice().compareTo(b.getModalPrice()) == 0 && equal(a.getMinPrice(), b.getMinPrice())
				&& equal(a.getMaxPrice(), b.getMaxPrice());
	}

	private static boolean equal(BigDecimal a, BigDecimal b) {
		return (a == null) ? b == null : (b != null && a.compareTo(b) == 0);
	}

	private static Map<String, Object> dropped(PriceObservationEntity dropped, PriceObservationEntity kept) {
		Map<String, Object> entry = new LinkedHashMap<>();
		entry.put("droppedObservationId", dropped.getId().toString());
		entry.put("keptObservationId", kept.getId().toString());
		entry.put("commodityId", dropped.getCommodityId().toString());
		entry.put("varietyId", (dropped.getVarietyId() != null) ? dropped.getVarietyId().toString() : null);
		entry.put("gradeId", (dropped.getGradeId() != null) ? dropped.getGradeId().toString() : null);
		entry.put("reportDate", dropped.getReportDate().toString());
		entry.put("sourceDatasetId", dropped.getSourceDatasetId().toString());
		entry.put("droppedModalPrice", dropped.getModalPrice());
		entry.put("droppedMinPrice", dropped.getMinPrice());
		entry.put("droppedMaxPrice", dropped.getMaxPrice());
		entry.put("keptModalPrice", kept.getModalPrice());
		entry.put("keptMinPrice", kept.getMinPrice());
		entry.put("keptMaxPrice", kept.getMaxPrice());
		return entry;
	}

	private MarketEntity lock(UUID marketId) {
		return this.markets.findForUpdate(marketId).orElseThrow(() -> ApiException.notFound("The market does not exist."));
	}

	/** Locks both markets in a fixed order, so two concurrent merges cannot deadlock. */
	private Map<UUID, MarketEntity> lockInIdOrder(UUID first, UUID second) {
		Map<UUID, MarketEntity> locked = new HashMap<>();
		List<UUID> ordered = new ArrayList<>(List.of(first, second));
		ordered.sort(Comparator.naturalOrder());
		for (UUID id : ordered) {
			locked.put(id, lock(id));
		}
		return locked;
	}

	private static void requireNotMerged(MarketEntity market) {
		if (market.getReviewStatus() == MarketReviewStatus.MERGED) {
			throw ApiException.conflict("A MERGED market cannot be changed.");
		}
	}

	private static Map<String, Object> coordinates(MarketEntity market) {
		Map<String, Object> values = new LinkedHashMap<>();
		values.put("lat", market.getLat());
		values.put("lon", market.getLon());
		values.put("approximate", market.isCoordinateApproximate());
		values.put("coordinateSource", market.getCoordinateSource());
		return values;
	}

	private static Map<String, Object> change(String field, Object before, Object after) {
		Map<String, Object> details = new LinkedHashMap<>();
		details.put("before", Map.of(field, String.valueOf(before)));
		details.put("after", Map.of(field, String.valueOf(after)));
		return details;
	}

	private MarketCurationResult result(MarketEntity market, Map<String, Object> details) {
		return new MarketCurationResult(this.directory.toInfo(List.of(market)).getFirst(), details);
	}

}
