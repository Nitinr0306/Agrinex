package com.agrinex.geography;

import java.io.Serializable;
import java.util.UUID;

/** A state (contract common.schema.json StateRef). Localized names are null when no reviewed value exists. */
public record StateRef(UUID id, String code, String name, String nameHi, String nameMr) implements Serializable {
}
