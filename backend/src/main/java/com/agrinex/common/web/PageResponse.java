package com.agrinex.common.web;

import java.util.List;
import java.util.function.Function;

/** A page of a list: {@code items} plus the contract's PageMetadata (ADR-0022 item 5). */
public record PageResponse<T>(List<T> items, int page, int size, long totalItems, long totalPages) {

	/** The same page with each item mapped, for example from a service view to a response DTO. */
	public <R> PageResponse<R> map(Function<? super T, ? extends R> mapper) {
		return new PageResponse<>(this.items.stream().<R>map(mapper).toList(), this.page, this.size, this.totalItems,
				this.totalPages);
	}

}
