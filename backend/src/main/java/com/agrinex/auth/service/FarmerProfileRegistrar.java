package com.agrinex.auth.service;

import java.util.UUID;

import com.agrinex.auth.domain.FarmerProfileEntity;
import com.agrinex.auth.repository.FarmerProfileRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates an ADMIN's farmer profile on first use (ADR-0016 item 6) in its own transaction, so a concurrent
 * creation that hits {@code uq_farmer_profiles_user} fails here without affecting the caller's transaction.
 */
@Component
class FarmerProfileRegistrar {

	private final FarmerProfileRepository profiles;

	FarmerProfileRegistrar(FarmerProfileRepository profiles) {
		this.profiles = profiles;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public UUID create(UUID userId) {
		return this.profiles.saveAndFlush(new FarmerProfileEntity(userId, null, null)).getId();
	}

}
