package com.agrinex.geography.repository;

import java.util.UUID;

import com.agrinex.geography.domain.StateEntity;

import org.springframework.data.jpa.repository.JpaRepository;

/** Repository of the geography module. Only this module's code uses it (spec section 27; ADR-0013). */
public interface StateRepository extends JpaRepository<StateEntity, UUID> {

}
