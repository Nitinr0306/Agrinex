package com.agrinex.commodity;

import java.io.Serializable;
import java.util.UUID;

/**
 * A variety or grade exactly as the source reports it, for example "Other" or "FAQ" (contract VarietyRef,
 * GradeRef; ADR-0006 item 3). Never renamed or merged.
 */
public record NamedRef(UUID id, UUID commodityId, String name) implements Serializable {
}
