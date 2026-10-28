package com.agrinex.alert.web.dto;

import java.util.List;

import com.agrinex.alert.service.AlertEventPageView;
import com.agrinex.common.web.PageResponse;
import com.agrinex.price.SourceSyncStatus;

/** Contract AlertEventPage: page metadata, the events and the source synchronization status. */
public record AlertEventPageResponse(List<AlertEventResponse> items, int page, int size, long totalItems,
		long totalPages, SourceSyncStatus sourceStatus) {

	public static AlertEventPageResponse from(AlertEventPageView view) {
		PageResponse<?> page = view.events();
		return new AlertEventPageResponse(view.events().items().stream().map(AlertEventResponse::from).toList(),
				page.page(), page.size(), page.totalItems(), page.totalPages(), view.sourceStatus());
	}

}
