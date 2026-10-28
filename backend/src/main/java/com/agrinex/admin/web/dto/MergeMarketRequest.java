package com.agrinex.admin.web.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import com.agrinex.common.validation.TextLength;

/** Contract MergeMarketRequest: the target market and the evidence that both denote one market (ADR-0014). */
public record MergeMarketRequest(@NotNull UUID targetMarketId,
		@NotNull @TextLength(min = 1, max = 500) String evidenceNote) {
}
