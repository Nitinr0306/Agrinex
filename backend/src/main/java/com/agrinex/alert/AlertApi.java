package com.agrinex.alert;

import java.util.UUID;

/**
 * Public interface of the alert module for the ingestion module (ingestion -> alert, ADR-0013; ADR-0019 item 1).
 */
public interface AlertApi {

	/**
	 * Evaluates the ACTIVE alerts whose commodity, and market when set, had an observation inserted or revised by
	 * the run in a VERIFIED, active market, in its own transaction. At most one event per alert and run is
	 * recorded, so a repeated evaluation is idempotent (ADR-0019 item 4).
	 * @return the number of events recorded
	 */
	int evaluateRun(UUID ingestionRunId);

}
