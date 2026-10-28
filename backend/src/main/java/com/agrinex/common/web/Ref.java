package com.agrinex.common.web;

import java.io.Serializable;
import java.util.UUID;

/** An identified, named reference as the contract returns it: MarketRef, VarietyRef, GradeRef ({@code id}, {@code name}). */
public record Ref(UUID id, String name) implements Serializable {
}
