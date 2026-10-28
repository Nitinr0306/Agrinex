package com.agrinex.price.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.UUID;

/**
 * The observation business key: the lowercase hex SHA-256 of the six resolved identity components, in the order
 * source dataset, market, commodity, variety (empty when absent), grade (empty when absent) and report date
 * (ISO yyyy-MM-dd), joined with "|" (DATABASE_ARCHITECTURE.md section 10.3; ADR-0009 item 1). The delimiter cannot
 * occur in a UUID or an ISO date, so distinct identities always give distinct inputs.
 */
public final class BusinessKeys {

	static final String DELIMITER = "|";

	private BusinessKeys() {
	}

	public static String of(UUID sourceDatasetId, UUID marketId, UUID commodityId, UUID varietyId, UUID gradeId,
			LocalDate reportDate) {
		String identity = String.join(DELIMITER, sourceDatasetId.toString(), marketId.toString(),
				commodityId.toString(), (varietyId != null) ? varietyId.toString() : "",
				(gradeId != null) ? gradeId.toString() : "", reportDate.toString());
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(identity.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		}
		catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 is required by the Java platform", ex);
		}
	}

}
