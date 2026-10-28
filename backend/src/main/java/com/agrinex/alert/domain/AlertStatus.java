package com.agrinex.alert.domain;

/** ACTIVE alerts are evaluated after each ingestion run; PAUSED alerts are not (spec section 34). */
public enum AlertStatus {

	ACTIVE, PAUSED

}
