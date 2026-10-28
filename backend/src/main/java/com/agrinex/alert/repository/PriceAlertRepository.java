package com.agrinex.alert.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.agrinex.alert.domain.AlertStatus;
import com.agrinex.alert.domain.PriceAlertEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository of the alert module. Only this module's code uses it (spec section 27; ADR-0013). Lists are ordered
 * by the caller's {@link Pageable} sort (lastTriggeredAt descending, never-triggered last, then id).
 */
public interface PriceAlertRepository extends JpaRepository<PriceAlertEntity, UUID> {

	Page<PriceAlertEntity> findByFarmerProfileId(UUID farmerProfileId, Pageable pageable);

	Page<PriceAlertEntity> findByFarmerProfileIdAndStatus(UUID farmerProfileId, AlertStatus status,
			Pageable pageable);

	Optional<PriceAlertEntity> findByIdAndFarmerProfileId(UUID id, UUID farmerProfileId);

	/** The ACTIVE alerts of the commodities a run changed (ADR-0019 item 1). */
	List<PriceAlertEntity> findByStatusAndCommodityIdIn(AlertStatus status, Collection<UUID> commodityIds);

	List<PriceAlertEntity> findByIdIn(Collection<UUID> ids);

}
