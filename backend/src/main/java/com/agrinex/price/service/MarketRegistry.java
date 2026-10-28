package com.agrinex.price.service;

import java.util.Optional;
import java.util.UUID;

import com.agrinex.geography.DistrictInfo;
import com.agrinex.price.MarketRegistration;
import com.agrinex.price.domain.MarketSourceAliasEntity;
import com.agrinex.price.repository.MarketSourceAliasRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * Resolves source market spellings to markets through {@code market_source_aliases}, registering unknown spellings
 * as PROVISIONAL markets (ADR-0014 item 4). There is no fuzzy matching and no automatic merge: a new (state,
 * district, market) spelling is always a new market until an admin merges it with evidence.
 */
@Service
class MarketRegistry {

	private final MarketSourceAliasRepository aliases;

	private final MarketRegistrar registrar;

	MarketRegistry(MarketSourceAliasRepository aliases, MarketRegistrar registrar) {
		this.aliases = aliases;
		this.registrar = registrar;
	}

	public MarketRegistration resolveOrRegister(String sourceStateName, String sourceDistrictName,
			String sourceMarketName, String sourceMarketNameRaw, DistrictInfo district, UUID ingestionRunId) {
		Optional<UUID> existing = find(sourceStateName, sourceDistrictName, sourceMarketName);
		if (existing.isPresent()) {
			return new MarketRegistration(existing.get(), false);
		}
		try {
			UUID marketId = this.registrar.register(sourceStateName, sourceDistrictName, sourceMarketName,
					sourceMarketNameRaw, district, ingestionRunId);
			return new MarketRegistration(marketId, true);
		}
		catch (DataIntegrityViolationException raced) {
			return new MarketRegistration(
					find(sourceStateName, sourceDistrictName, sourceMarketName).orElseThrow(() -> raced), false);
		}
	}

	private Optional<UUID> find(String sourceStateName, String sourceDistrictName, String sourceMarketName) {
		return this.aliases
			.findBySourceStateNameAndSourceDistrictNameAndSourceMarketName(sourceStateName, sourceDistrictName,
					sourceMarketName)
			.map(MarketSourceAliasEntity::getMarketId);
	}

}
