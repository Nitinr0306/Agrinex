package com.agrinex.geography.web.dto;

import java.util.List;

import com.agrinex.geography.StateRef;

/** Contract StateListResponse. */
public record StateListResponse(List<StateRef> items) {
}
