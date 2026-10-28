package com.agrinex.commodity.repository;

import java.util.Optional;
import java.util.UUID;

import com.agrinex.commodity.domain.CommoditySourceAliasEntity;

import org.springframework.data.jpa.repository.JpaRepository;

/** Repository of the commodity module. Only this module's code uses it (spec section 27; ADR-0013, ADR-0051). */
public interface CommoditySourceAliasRepository extends JpaRepository<CommoditySourceAliasEntity, UUID> {

	Optional<CommoditySourceAliasEntity> findBySourceCommodityName(String sourceCommodityName);

}
