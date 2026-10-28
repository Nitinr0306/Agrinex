package com.agrinex.common.web;

import java.math.BigDecimal;

/** A WGS84 coordinate pair with at most 6 decimal places (contract common.schema.json GeoPoint). */
public record GeoPoint(BigDecimal lat, BigDecimal lon) {
}
