package com.agrinex.price.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.agrinex.price.domain.ForecastEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repository of the price module for the optional ML forecasts (ADR-0030). Only this module's code uses it. */
public interface ForecastRepository extends JpaRepository<ForecastEntity, UUID> {

	/** Every point dated on or after {@code from}, by horizon, newest generation first, then by date. */
	@Query("""
			select f from ForecastEntity f
			where f.marketId = :marketId and f.commodityId = :commodityId and f.forecastDate >= :from
			order by f.forecastHorizonDays, f.generatedAt desc, f.forecastDate
			""")
	List<ForecastEntity> findPointsFrom(@Param("marketId") UUID marketId, @Param("commodityId") UUID commodityId,
			@Param("from") LocalDate from);

	/** Retention: forecasts are deleted 90 days after their forecast date (ADR-0029 item 1). */
	@Modifying
	@Query("delete from ForecastEntity f where f.forecastDate < :cutoff")
	int deleteForecastDateBefore(@Param("cutoff") LocalDate cutoff);

}
