package com.agrinex.price.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.agrinex.common.time.AgrinexCalendar;
import com.agrinex.geography.DistrictInfo;
import com.agrinex.price.domain.MarketEntity;
import com.agrinex.price.domain.MarketSlugs;
import com.agrinex.price.domain.MarketSourceAliasEntity;
import com.agrinex.price.repository.MarketRepository;
import com.agrinex.price.repository.MarketSourceAliasRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registers a market spelling seen for the first time as a PROVISIONAL market (ADR-0014 item 4): its resolved
 * district, its whitespace-normalized name, approximate coordinates equal to the district centroid, and an alias
 * row with the raw spelling and the note "auto-registered by run ...". Runs in its own transaction, so a race with
 * a concurrent run registering the same spelling fails only this insert.
 */
@Component
class MarketRegistrar {

	static final int SLUG_MAX_LENGTH = 220;

	private static final String AUTO_REGISTERED_NOTE = "auto-registered by run ";

	private final MarketRepository markets;

	private final MarketSourceAliasRepository aliases;

	private final AgrinexCalendar calendar;

	MarketRegistrar(MarketRepository markets, MarketSourceAliasRepository aliases, AgrinexCalendar calendar) {
		this.markets = markets;
		this.aliases = aliases;
		this.calendar = calendar;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public UUID register(String sourceStateName, String sourceDistrictName, String sourceMarketName,
			String sourceMarketNameRaw, DistrictInfo district, UUID ingestionRunId) {
		String slug = uniqueSlug(sourceMarketName, district);
		MarketEntity market = this.markets.saveAndFlush(MarketEntity.provisional(sourceMarketName, slug, district.id(),
				district.centroidLat(), district.centroidLon(), centroidSource(district), this.calendar.now()));
		this.aliases.saveAndFlush(new MarketSourceAliasEntity(market.getId(), sourceStateName, sourceDistrictName,
				sourceMarketName, sourceMarketNameRaw, AUTO_REGISTERED_NOTE + ingestionRunId, this.calendar.now()));
		return market.getId();
	}

	/**
	 * The slug rule of ADR-0014 item 6: the name slug; on collision the district slug is appended, then the
	 * lower-case state code. A collision beyond that (a second market with the same name in the same district,
	 * reached through another district spelling) is not covered by the rule; a numeric suffix keeps the slug unique.
	 */
	private String uniqueSlug(String name, DistrictInfo district) {
		String base = MarketSlugs.slugify(name);
		String withDistrict = base + "-" + MarketSlugs.slugify(district.name());
		String withState = withDistrict + "-" + district.state().code().toLowerCase(Locale.ROOT);
		List<String> candidates = new ArrayList<>(List.of(base, withDistrict, withState));
		for (String candidate : candidates) {
			if (candidate.length() <= SLUG_MAX_LENGTH && !this.markets.existsByCanonicalSlug(candidate)) {
				return candidate;
			}
		}
		String prefix = withState.substring(0, Math.min(withState.length(), SLUG_MAX_LENGTH - 6));
		for (int suffix = 2;; suffix++) {
			String candidate = prefix + "-" + suffix;
			if (!this.markets.existsByCanonicalSlug(candidate)) {
				return candidate;
			}
		}
	}

	private static String centroidSource(DistrictInfo district) {
		return "District centroid of " + district.name() + " (approximate market location, ADR-0014)";
	}

}
