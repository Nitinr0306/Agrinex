package com.agrinex.price;

/**
 * Outcome of loading one delivery unit against stored observations (ADR-0009 item 3): new identities inserted,
 * changed prices revised, and identical re-deliveries left unwritten.
 */
public record LoadResult(int inserted, int updated, int unchanged) {
}
