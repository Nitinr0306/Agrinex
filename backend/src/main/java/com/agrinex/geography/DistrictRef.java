package com.agrinex.geography;

import java.io.Serializable;
import java.util.UUID;

/** A district named as published by the Local Government Directory (contract DistrictRef). */
public record DistrictRef(UUID id, String name) implements Serializable {
}
