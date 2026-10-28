package com.agrinex.ingestion.pipeline;

import com.agrinex.ingestion.domain.IngestionErrorCode;

/** The outcome of checking one source record against coverage and rules V1 to V7. */
public sealed interface RecordCheck {

	/** The record passed; V8 and deduplication come next. */
	record Accepted(ValidRecord record) implements RecordCheck {
	}

	/** The record is rejected with a VALIDATION code; its raw payload is kept (ADR-0008 item 5). */
	record Rejected(IngestionErrorCode code, String message) implements RecordCheck {
	}

	/** The record's state is outside coverage: counted in records_out_of_scope, not stored (ADR-0006 item 1). */
	record OutOfScope() implements RecordCheck {
	}

}
