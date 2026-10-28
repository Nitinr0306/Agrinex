package com.agrinex.commodity.web.dto;

import java.util.List;

import com.agrinex.commodity.NamedRef;

/** Contract VarietyListResponse and GradeListResponse, which share one shape. */
public record NamedListResponse(List<NamedRef> items) {
}
