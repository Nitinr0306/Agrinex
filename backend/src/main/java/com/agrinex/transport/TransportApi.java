package com.agrinex.transport;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import com.agrinex.common.web.PageParams;
import com.agrinex.common.web.PageResponse;

/**
 * Public interface of the transport module (owner of transport_assumptions; ADR-0013). Transport is a leaf
 * module, isolated so that routing-based distance could replace haversine later (spec section 38).
 */
public interface TransportApi {

	/** The version new comparisons use (spec section 16 "Load active transport_assumptions"). */
	TransportAssumptions activeAssumptions();

	Optional<TransportAssumptions> findVersion(UUID versionId);

	/** Inserts a new active version; earlier versions stay unchanged (ADR-0020 item 1). */
	TransportAssumptions updateAssumptions(BigDecimal ratePerKm, BigDecimal minimumTripCharge,
			BigDecimal vehicleCapacityKg);

	/** Every version, newest first (createdAt descending, then id). */
	PageResponse<TransportAssumptions> listVersions(PageParams page);

}
