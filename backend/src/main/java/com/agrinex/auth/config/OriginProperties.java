package com.agrinex.auth.config;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The one public origin of the deployment (ADR-0017): the CORS allow-list, the Origin check on refresh and
 * logout, and the base of email-verification and password-reset links, never the request's Host header
 * (ADR-0048 item 2). The development profile adds the frontend dev server to CORS only. The origin is required
 * and must be a bare {@code scheme://host[:port]}.
 */
@ConfigurationProperties("agrinex")
public record OriginProperties(String publicOrigin, Cors cors) {

	public OriginProperties {
		publicOrigin = normalize(publicOrigin);
	}

	/** The public origin followed by the profile's extra CORS origins. */
	public List<String> corsOrigins() {
		List<String> extra = (this.cors != null && this.cors.extraOrigins() != null) ? this.cors.extraOrigins()
				: List.of();
		return Stream.concat(Stream.of(this.publicOrigin), extra.stream()).distinct().toList();
	}

	private static String normalize(String origin) {
		if (origin == null || origin.isBlank()) {
			throw new IllegalArgumentException("AGRINEX_PUBLIC_ORIGIN must be set to the public origin");
		}
		URI uri;
		try {
			uri = URI.create(origin.strip());
		}
		catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException("AGRINEX_PUBLIC_ORIGIN is not a valid origin");
		}
		String scheme = (uri.getScheme() != null) ? uri.getScheme().toLowerCase(Locale.ROOT) : "";
		boolean bare = (uri.getRawPath() == null || uri.getRawPath().isEmpty() || "/".equals(uri.getRawPath()))
				&& uri.getRawQuery() == null && uri.getRawFragment() == null && uri.getRawUserInfo() == null;
		if (!("https".equals(scheme) || "http".equals(scheme)) || uri.getHost() == null || !bare) {
			throw new IllegalArgumentException("AGRINEX_PUBLIC_ORIGIN must have the form scheme://host[:port]");
		}
		String host = uri.getHost().toLowerCase(Locale.ROOT);
		return scheme + "://" + host + ((uri.getPort() != -1) ? ":" + uri.getPort() : "");
	}

	public record Cors(List<String> extraOrigins) {
	}

}
