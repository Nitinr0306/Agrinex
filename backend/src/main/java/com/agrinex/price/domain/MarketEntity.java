package com.agrinex.price.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.agrinex.price.MarketReviewStatus;

/**
 * A market (table {@code markets}; ADR-0014). Registered PROVISIONAL from the feed with its district centroid as
 * approximate coordinates; curated by admins (verify, merge, deactivate, reactivate, set coordinates). No
 * operation renames a market.
 */
@Entity
@Table(name = "markets")
public class MarketEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "canonical_name")
	private String canonicalName;

	@Column(name = "canonical_slug")
	private String canonicalSlug;

	@Column(name = "district_id")
	private UUID districtId;

	private BigDecimal lat;

	private BigDecimal lon;

	@Column(name = "is_coordinate_approximate")
	private boolean coordinateApproximate;

	@Column(name = "coordinate_source")
	private String coordinateSource;

	@Column(name = "is_active")
	private boolean active;

	@Enumerated(EnumType.STRING)
	@Column(name = "review_status")
	private MarketReviewStatus reviewStatus;

	@Column(name = "merged_into_market_id")
	private UUID mergedIntoMarketId;

	@Column(name = "created_at")
	private Instant createdAt;

	protected MarketEntity() {
	}

	/** A PROVISIONAL market at its district centroid, marked approximate (ADR-0014 item 4). */
	public static MarketEntity provisional(String canonicalName, String canonicalSlug, UUID districtId, BigDecimal lat,
			BigDecimal lon, String coordinateSource, Instant createdAt) {
		MarketEntity market = new MarketEntity();
		market.canonicalName = canonicalName;
		market.canonicalSlug = canonicalSlug;
		market.districtId = districtId;
		market.lat = lat;
		market.lon = lon;
		market.coordinateApproximate = true;
		market.coordinateSource = coordinateSource;
		market.active = true;
		market.reviewStatus = MarketReviewStatus.PROVISIONAL;
		market.createdAt = createdAt;
		return market;
	}

	public void verify() {
		this.reviewStatus = MarketReviewStatus.VERIFIED;
	}

	public void deactivate() {
		this.active = false;
	}

	public void reactivate() {
		this.active = true;
	}

	public void setExactCoordinates(BigDecimal lat, BigDecimal lon, String coordinateSource) {
		this.lat = lat;
		this.lon = lon;
		this.coordinateSource = coordinateSource;
		this.coordinateApproximate = false;
	}

	public void mergeInto(UUID targetMarketId) {
		this.reviewStatus = MarketReviewStatus.MERGED;
		this.mergedIntoMarketId = targetMarketId;
		this.active = false;
	}

	public UUID getId() {
		return this.id;
	}

	public String getCanonicalName() {
		return this.canonicalName;
	}

	public String getCanonicalSlug() {
		return this.canonicalSlug;
	}

	public UUID getDistrictId() {
		return this.districtId;
	}

	public BigDecimal getLat() {
		return this.lat;
	}

	public BigDecimal getLon() {
		return this.lon;
	}

	public boolean isCoordinateApproximate() {
		return this.coordinateApproximate;
	}

	public String getCoordinateSource() {
		return this.coordinateSource;
	}

	public boolean isActive() {
		return this.active;
	}

	public MarketReviewStatus getReviewStatus() {
		return this.reviewStatus;
	}

	public UUID getMergedIntoMarketId() {
		return this.mergedIntoMarketId;
	}

	public Instant getCreatedAt() {
		return this.createdAt;
	}

}
