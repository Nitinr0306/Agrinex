package com.agrinex.commodity;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * A commodity of the known list (ADR-0006) with its official group and its evidence-verified price unit
 * (ADR-0008). {@code priceUnit} is null while the unit is unverified.
 */
public record CommodityInfo(UUID id, String name, String nameHi, String nameMr, String category, String priceUnit,
		String priceUnitSource, Instant createdAt) implements Serializable {

	/** The only unit comparison and alerts accept (ADR-0008, ADR-0023). */
	public static final String RUPEES_PER_QUINTAL = "Rs./Quintal";

	public CommodityRef ref() {
		return new CommodityRef(this.id, this.name, this.nameHi, this.nameMr);
	}

	public boolean pricedPerQuintal() {
		return RUPEES_PER_QUINTAL.equals(this.priceUnit);
	}

}
