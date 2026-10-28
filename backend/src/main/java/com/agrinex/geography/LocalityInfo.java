package com.agrinex.geography;

import java.io.Serializable;
import java.util.UUID;

/** An LGD sub-district (taluka). Localities carry no coordinates in Phase 1 (ADR-0014 item 1). */
public record LocalityInfo(UUID id, UUID districtId, String name) implements Serializable {
}
