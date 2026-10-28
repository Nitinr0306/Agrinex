package com.agrinex.alert.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.agrinex.alert.domain.AlertEventEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository of the alert module. Only this module's code uses it (spec section 27; ADR-0013). An event belongs
 * to the owner of its alert; lists are ordered by the caller's {@link Pageable} sort (triggeredAt descending,
 * then id).
 */
public interface AlertEventRepository extends JpaRepository<AlertEventEntity, UUID> {

	@Query(value = """
			select e from AlertEventEntity e, PriceAlertEntity a
			where e.priceAlertId = a.id and a.farmerProfileId = :profileId
			""", countQuery = """
			select count(e) from AlertEventEntity e, PriceAlertEntity a
			where e.priceAlertId = a.id and a.farmerProfileId = :profileId
			""")
	Page<AlertEventEntity> findOwnedBy(@Param("profileId") UUID farmerProfileId, Pageable pageable);

	@Query(value = """
			select e from AlertEventEntity e, PriceAlertEntity a
			where e.priceAlertId = a.id and a.farmerProfileId = :profileId and e.readAt is null
			""", countQuery = """
			select count(e) from AlertEventEntity e, PriceAlertEntity a
			where e.priceAlertId = a.id and a.farmerProfileId = :profileId and e.readAt is null
			""")
	Page<AlertEventEntity> findUnreadOwnedBy(@Param("profileId") UUID farmerProfileId, Pageable pageable);

	@Query("""
			select e from AlertEventEntity e, PriceAlertEntity a
			where e.id = :eventId and e.priceAlertId = a.id and a.farmerProfileId = :profileId
			""")
	Optional<AlertEventEntity> findOwned(@Param("eventId") UUID eventId, @Param("profileId") UUID farmerProfileId);

	boolean existsByPriceAlertIdAndIngestionRunId(UUID priceAlertId, UUID ingestionRunId);

	/** Retention: events are kept 180 days (ADR-0029 item 1). */
	@Modifying
	@Query("delete from AlertEventEntity e where e.triggeredAt < :cutoff")
	int deleteTriggeredBefore(@Param("cutoff") Instant cutoff);

}
