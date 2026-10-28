package com.agrinex.geography.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.agrinex.geography.domain.DistrictEntity;

import org.springframework.data.jpa.repository.JpaRepository;

/** Repository of the geography module. Only this module's code uses it (spec section 27; ADR-0013). */
public interface DistrictRepository extends JpaRepository<DistrictEntity, UUID> {

	List<DistrictEntity> findByStateId(UUID stateId);

	List<DistrictEntity> findByCentroidLatAndCentroidLon(BigDecimal centroidLat, BigDecimal centroidLon);

}
