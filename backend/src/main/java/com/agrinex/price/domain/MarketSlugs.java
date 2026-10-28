package com.agrinex.price.domain;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * The market slug rule of ADR-0014 item 6: the lower-case name with every run of non-alphanumeric characters
 * replaced by one hyphen; edge hyphens are kept (contract AdminMarketFields.slug, for example {@code pune-moshi-}).
 */
public final class MarketSlugs {

	private static final Pattern NON_ALPHANUMERIC_RUN = Pattern.compile("[^a-z0-9]+");

	private MarketSlugs() {
	}

	public static String slugify(String name) {
		return NON_ALPHANUMERIC_RUN.matcher(name.toLowerCase(Locale.ROOT)).replaceAll("-");
	}

}
