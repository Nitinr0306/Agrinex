package com.agrinex.recommendation.repository;

import java.util.UUID;

import com.agrinex.recommendation.domain.SellingOptionEntity;

import org.springframework.data.jpa.repository.JpaRepository;

/** Repository of the recommendation module. Only this module's code uses it (spec section 27; ADR-0013). */
public interface SellingOptionRepository extends JpaRepository<SellingOptionEntity, UUID> {

}
