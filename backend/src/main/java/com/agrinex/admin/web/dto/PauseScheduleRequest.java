package com.agrinex.admin.web.dto;

import jakarta.validation.constraints.NotNull;

import com.agrinex.common.validation.TextLength;

/** Contract PauseScheduleRequest: why scheduled runs are paused, for example announced source maintenance. */
public record PauseScheduleRequest(@NotNull @TextLength(min = 1, max = 500) String reason) {
}
