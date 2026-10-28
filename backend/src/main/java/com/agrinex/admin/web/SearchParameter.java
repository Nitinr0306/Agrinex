package com.agrinex.admin.web;

import com.agrinex.common.error.ApiException;
import com.agrinex.common.error.FieldErrorCode;

import org.springframework.stereotype.Component;

/** The {@code search} query parameter of the curation lists: 1 to 100 characters (contract SearchQuery). */
@Component
class SearchParameter {

	static final int MAX_LENGTH = 100;

	/** The value unchanged, or null when absent; an empty or too long value answers 400. */
	String validated(String search) {
		if (search == null) {
			return null;
		}
		int length = search.codePointCount(0, search.length());
		if (length < 1) {
			throw ApiException.validation("search", FieldErrorCode.TOO_SHORT, "must not be empty");
		}
		if (length > MAX_LENGTH) {
			throw ApiException.validation("search", FieldErrorCode.TOO_LONG, "must be at most 100 characters");
		}
		return search;
	}

}
