package com.agrinex.auth;

/** Account roles (spec section 28). Exactly one per account; Phase 1 never issues BUYER accounts. */
public enum Role {

	FARMER, BUYER, ADMIN

}
