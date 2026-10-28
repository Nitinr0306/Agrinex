package com.agrinex.auth.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A farmer profile (table {@code farmer_profiles}; spec section 20). Every FARMER gets one at registration; an
 * ADMIN gets one on first use, to own alerts (ADR-0016 item 6). District granularity only (spec section 28).
 */
@Entity
@Table(name = "farmer_profiles")
public class FarmerProfileEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "user_id")
	private UUID userId;

	@Column(name = "display_name")
	private String displayName;

	@Column(name = "default_district_id")
	private UUID defaultDistrictId;

	@Column(name = "preferred_locale")
	private String preferredLocale;

	protected FarmerProfileEntity() {
	}

	public FarmerProfileEntity(UUID userId, String displayName, UUID defaultDistrictId) {
		this.userId = userId;
		this.displayName = displayName;
		this.defaultDistrictId = defaultDistrictId;
	}

	public UUID getId() {
		return this.id;
	}

	public UUID getUserId() {
		return this.userId;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public UUID getDefaultDistrictId() {
		return this.defaultDistrictId;
	}

}
