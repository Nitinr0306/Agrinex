package com.agrinex.recommendation.service;

import java.math.BigDecimal;

import com.agrinex.price.MarketInfo;

/** A comparison candidate market inside the radius, with its distance rounded to 2 decimal places (ADR-0036). */
public record MarketInRange(MarketInfo market, BigDecimal distanceKm) {
}
