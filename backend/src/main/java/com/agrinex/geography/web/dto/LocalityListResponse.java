package com.agrinex.geography.web.dto;

import java.util.List;

import com.agrinex.geography.LocalityInfo;

/** Contract LocalityListResponse. */
public record LocalityListResponse(List<LocalityInfo> items) {
}
