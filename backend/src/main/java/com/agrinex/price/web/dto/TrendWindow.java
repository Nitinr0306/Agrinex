package com.agrinex.price.web.dto;

import java.time.LocalDate;

/** Contract TrendWindow: the chart window and the two ADR-0025 comparison windows, inclusive. */
public record TrendWindow(LocalDate chartFrom, LocalDate chartTo, LocalDate recentFrom, LocalDate recentTo,
		LocalDate baselineFrom, LocalDate baselineTo) {
}
