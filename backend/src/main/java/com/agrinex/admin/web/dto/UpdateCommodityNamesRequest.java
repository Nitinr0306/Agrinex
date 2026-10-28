package com.agrinex.admin.web.dto;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.agrinex.common.validation.TextLength;

/**
 * Contract UpdateCommodityNamesRequest (ADR-0015): both localized names, where null removes a translation, and the
 * provenance note kept in the audit details. Both name properties are required, even when null.
 */
public record UpdateCommodityNamesRequest(@JsonProperty(required = true) @TextLength(min = 1, max = 200) String nameHi,
		@JsonProperty(required = true) @TextLength(min = 1, max = 200) String nameMr,
		@NotNull @TextLength(min = 1, max = 500) String provenanceNote) {
}
