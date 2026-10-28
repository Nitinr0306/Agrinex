package com.agrinex.price.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.agrinex.price.MarketCommodity;
import com.agrinex.price.domain.PriceObservationEntity;
import com.agrinex.price.domain.PriceObservationRevisionEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repository of the price module. Only this module's code uses it (spec section 27; ADR-0013). */
public interface PriceObservationRepository extends JpaRepository<PriceObservationEntity, UUID> {

	List<PriceObservationEntity> findByBusinessKeyIn(Collection<String> businessKeys);

	List<PriceObservationEntity> findByMarketId(UUID marketId);

	List<PriceObservationEntity> findByMarketIdAndReportDateBetween(UUID marketId, LocalDate fromDate,
			LocalDate toDate);

	/** Selection candidates of one commodity at some markets in a date window, with the exact filters of ADR-0010 item 3. */
	@Query("""
			select o from PriceObservationEntity o
			where o.marketId in :marketIds and o.commodityId = :commodityId
			  and o.reportDate between :fromDate and :toDate
			  and (:varietyId is null or o.varietyId = :varietyId)
			  and (:gradeId is null or o.gradeId = :gradeId)
			""")
	List<PriceObservationEntity> findCandidates(@Param("marketIds") Collection<UUID> marketIds,
			@Param("commodityId") UUID commodityId, @Param("fromDate") LocalDate fromDate,
			@Param("toDate") LocalDate toDate, @Param("varietyId") UUID varietyId, @Param("gradeId") UUID gradeId);

	@Query("""
			select distinct o.commodityId from PriceObservationEntity o
			where o.marketId in :marketIds and o.reportDate between :fromDate and :toDate
			""")
	Set<UUID> findCommodityIdsObserved(@Param("marketIds") Collection<UUID> marketIds,
			@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

	@Query("""
			select distinct new com.agrinex.price.MarketCommodity(o.marketId, o.commodityId)
			from PriceObservationEntity o where o.ingestionRunId = :runId
			""")
	Set<MarketCommodity> findPairsInsertedByRun(@Param("runId") UUID runId);

	@Query("""
			select distinct new com.agrinex.price.MarketCommodity(o.marketId, o.commodityId)
			from PriceObservationRevisionEntity r, PriceObservationEntity o
			where o.id = r.priceObservationId and r.ingestionRunId = :runId
			""")
	Set<MarketCommodity> findPairsRevisedByRun(@Param("runId") UUID runId);

	@Query("select max(o.reportDate) from PriceObservationEntity o where o.sourceDatasetId = :sourceDatasetId")
	Optional<LocalDate> findLatestReportDateOfDataset(@Param("sourceDatasetId") UUID sourceDatasetId);

	@Query("select max(o.reportDate) from PriceObservationEntity o where o.marketId = :marketId")
	Optional<LocalDate> findLatestReportDateOfMarket(@Param("marketId") UUID marketId);

	@Query("""
			select new com.agrinex.price.repository.MarketLatestDate(o.marketId, max(o.reportDate))
			from PriceObservationEntity o where o.marketId in :marketIds group by o.marketId
			""")
	List<MarketLatestDate> findLatestReportDates(@Param("marketIds") Collection<UUID> marketIds);

}
