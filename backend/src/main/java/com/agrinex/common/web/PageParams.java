package com.agrinex.common.web;

import com.agrinex.common.error.ApiException;
import com.agrinex.common.error.FieldErrorCode;

import java.util.List;

/**
 * Validated pagination parameters (ADR-0022 item 5): {@code page} is zero-based (default 0) and {@code size} is
 * 1 to 100 (default 20). A larger size is rejected, never clamped.
 */
public record PageParams(int page, int size) {

	public static final int DEFAULT_SIZE = 20;

	public static final int MAX_SIZE = 100;

	public static PageParams of(Integer page, Integer size) {
		int resolvedPage = (page != null) ? page : 0;
		int resolvedSize = (size != null) ? size : DEFAULT_SIZE;
		if (resolvedPage < 0) {
			throw ApiException.validation("page", FieldErrorCode.OUT_OF_RANGE, "must be 0 or greater");
		}
		if (resolvedSize < 1 || resolvedSize > MAX_SIZE) {
			throw ApiException.validation("size", FieldErrorCode.OUT_OF_RANGE, "must be between 1 and 100");
		}
		return new PageParams(resolvedPage, resolvedSize);
	}

	public long offset() {
		return (long) this.page * this.size;
	}

	public <T> PageResponse<T> toResponse(List<T> items, long totalItems) {
		long totalPages = (totalItems == 0) ? 0 : (totalItems + this.size - 1) / this.size;
		return new PageResponse<>(items, this.page, this.size, totalItems, totalPages);
	}

}
