package com.agrinex.commodity;

import java.io.Serializable;
import java.util.UUID;

/** A commodity with its curated localized names (contract common.schema.json CommodityRef; ADR-0015). */
public record CommodityRef(UUID id, String name, String nameHi, String nameMr) implements Serializable {
}
