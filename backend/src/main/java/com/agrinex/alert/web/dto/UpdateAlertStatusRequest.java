package com.agrinex.alert.web.dto;

import jakarta.validation.constraints.NotNull;

import com.agrinex.alert.domain.AlertStatus;

/** Contract UpdateAlertStatusRequest. */
public record UpdateAlertStatusRequest(@NotNull AlertStatus status) {
}
