package com.agrinex.ingestion.source;

import com.agrinex.ingestion.domain.IngestionErrorCode;

/**
 * A page that could not be fetched as the expected JSON structure (ADR-0005 item 4). The message and the body
 * are already redacted: they never contain the API key (ADR-0020 item 5). A failure is never read as "zero
 * records".
 */
public class SourceFetchException extends Exception {

	private final IngestionErrorCode code;

	private final String responseBody;

	public SourceFetchException(IngestionErrorCode code, String message, String responseBody) {
		super(message);
		this.code = code;
		this.responseBody = responseBody;
	}

	public IngestionErrorCode code() {
		return this.code;
	}

	/** The redacted response body, or null when there was none. */
	public String responseBody() {
		return this.responseBody;
	}

}
