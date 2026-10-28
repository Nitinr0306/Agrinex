package com.agrinex.alert.service;

import java.math.BigDecimal;
import java.util.UUID;

import com.agrinex.alert.domain.AlertType;

/** A new alert as the caller submits it (contract CreateAlertRequest); {@code marketId} is null for all markets. */
public record NewAlert(AlertType alertType, UUID commodityId, UUID marketId, BigDecimal thresholdValue) {
}
