package com.agrinex.alert.service;

import com.agrinex.common.web.PageResponse;
import com.agrinex.price.SourceSyncStatus;

/** A page of the caller's events with the source synchronization status (contract AlertEventPage). */
public record AlertEventPageView(PageResponse<AlertEventView> events, SourceSyncStatus sourceStatus) {
}
