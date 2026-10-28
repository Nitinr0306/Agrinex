package com.agrinex.price.repository;

import java.util.UUID;

import com.agrinex.price.domain.SourceDatasetEntity;

import org.springframework.data.jpa.repository.JpaRepository;

/** Repository of the price module. Only this module's code uses it (spec section 27; ADR-0013). */
public interface SourceDatasetRepository extends JpaRepository<SourceDatasetEntity, UUID> {

}
