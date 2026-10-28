package com.agrinex.commodity.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.agrinex.commodity.domain.GradeEntity;

import org.springframework.data.jpa.repository.JpaRepository;

/** Repository of the commodity module. Only this module's code uses it (spec section 27; ADR-0013). */
public interface GradeRepository extends JpaRepository<GradeEntity, UUID> {

	List<GradeEntity> findByCommodityId(UUID commodityId);

	Optional<GradeEntity> findByCommodityIdAndCanonicalName(UUID commodityId, String canonicalName);

}
