package com.agrinex.price.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.agrinex.price.domain.MarketSourceAliasEntity;

import org.springframework.data.jpa.repository.JpaRepository;

/** Repository of the price module. Only this module's code uses it (spec section 27; ADR-0013). */
public interface MarketSourceAliasRepository extends JpaRepository<MarketSourceAliasEntity, UUID> {

	Optional<MarketSourceAliasEntity> findBySourceStateNameAndSourceDistrictNameAndSourceMarketName(
			String sourceStateName, String sourceDistrictName, String sourceMarketName);

	List<MarketSourceAliasEntity> findByMarketId(UUID marketId);

}
