package com.agrinex.transport.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** One version of the transport assumptions (table {@code transport_assumptions}; spec section 32). */
@Entity
@Table(name = "transport_assumptions")
public class TransportAssumptionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "cost_per_km")
	private BigDecimal costPerKm;

	@Column(name = "minimum_trip_charge")
	private BigDecimal minimumTripCharge;

	@Column(name = "vehicle_capacity_kg")
	private BigDecimal vehicleCapacityKg;

	@Column(name = "is_active")
	private boolean active;

	@Column(name = "created_at")
	private Instant createdAt;

	protected TransportAssumptionEntity() {
	}

	public TransportAssumptionEntity(BigDecimal costPerKm, BigDecimal minimumTripCharge, BigDecimal vehicleCapacityKg,
			Instant createdAt) {
		this.costPerKm = costPerKm;
		this.minimumTripCharge = minimumTripCharge;
		this.vehicleCapacityKg = vehicleCapacityKg;
		this.active = true;
		this.createdAt = createdAt;
	}

	public UUID getId() {
		return this.id;
	}

	public BigDecimal getCostPerKm() {
		return this.costPerKm;
	}

	public BigDecimal getMinimumTripCharge() {
		return this.minimumTripCharge;
	}

	public BigDecimal getVehicleCapacityKg() {
		return this.vehicleCapacityKg;
	}

	public boolean isActive() {
		return this.active;
	}

	public Instant getCreatedAt() {
		return this.createdAt;
	}

}
