package com.agrinex.commodity.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.agrinex.commodity.domain.VarietyEntity;

import org.springframework.data.jpa.repository.JpaRepository;

/** Repository of the commodity module. Only this module's code uses it (spec section 27; ADR-0013). */
public interface VarietyRepository extends JpaRepository<VarietyEntity, UUID> {

	List<VarietyEntity> findByCommodityId(UUID commodityId);

	Optional<VarietyEntity> findByCommodityIdAndCanonicalName(UUID commodityId, String canonicalName);

}
