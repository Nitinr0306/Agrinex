package com.agrinex.transport.mapper;

import com.agrinex.transport.TransportAssumptions;
import com.agrinex.transport.domain.TransportAssumptionEntity;

/** Maps transport entities to the module's public records; entities never leave the module. */
public final class TransportMapper {

	private TransportMapper() {
	}

	public static TransportAssumptions toView(TransportAssumptionEntity entity) {
		return new TransportAssumptions(entity.getId(), entity.getCostPerKm(), entity.getMinimumTripCharge(),
				entity.getVehicleCapacityKg(), entity.isActive(), entity.getCreatedAt());
	}

}
