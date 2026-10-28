package com.agrinex.auth.repository;

import java.util.Optional;
import java.util.UUID;

import com.agrinex.auth.domain.FarmerProfileEntity;

import org.springframework.data.jpa.repository.JpaRepository;

/** Repository of the auth module. Only this module's code uses it (spec section 27; ADR-0013). */
public interface FarmerProfileRepository extends JpaRepository<FarmerProfileEntity, UUID> {

	Optional<FarmerProfileEntity> findByUserId(UUID userId);

}
