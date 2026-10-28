package com.agrinex.geography.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.agrinex.common.TextNormalizer;
import com.agrinex.common.error.ApiErrorResponse.FieldError;
import com.agrinex.common.error.ApiException;
import com.agrinex.common.error.FieldErrorCode;
import com.agrinex.common.time.AgrinexCalendar;
import com.agrinex.common.web.PageParams;
import com.agrinex.common.web.PageResponse;
import com.agrinex.geography.DistrictAliasView;
import com.agrinex.geography.DistrictInfo;
import com.agrinex.geography.GeographyApi;
import com.agrinex.geography.LocalityInfo;
import com.agrinex.geography.StateRef;
import com.agrinex.geography.domain.DistrictEntity;
import com.agrinex.geography.domain.DistrictSourceAliasEntity;
import com.agrinex.geography.domain.LocalityEntity;
import com.agrinex.geography.domain.StateEntity;
import com.agrinex.geography.mapper.GeographyMapper;
import com.agrinex.geography.repository.DistrictRepository;
import com.agrinex.geography.repository.DistrictSourceAliasRepository;
import com.agrinex.geography.repository.LocalityRepository;
import com.agrinex.geography.repository.StateRepository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Geography reference data (F1.3; ADR-0014). Public lists are ordered by name in code-point order, then id
 * (docs/api/API_CONVENTIONS.md section 5), and cached in Redis for one hour (ADR-0018). States, districts and
 * localities change only through migrations, so these caches need no eviction by the API.
 */
@Service
@Transactional(readOnly = true)
class GeographyService implements GeographyApi {

	private static final Comparator<String> CODE_POINT_ORDER = String::compareTo;

	private final StateRepository states;

	private final DistrictRepository districts;

	private final LocalityRepository localities;

	private final DistrictSourceAliasRepository aliases;

	private final AgrinexCalendar calendar;

	GeographyService(StateRepository states, DistrictRepository districts, LocalityRepository localities,
			DistrictSourceAliasRepository aliases, AgrinexCalendar calendar) {
		this.states = states;
		this.districts = districts;
		this.localities = localities;
		this.aliases = aliases;
		this.calendar = calendar;
	}

	@Override
	@Cacheable("geography-states")
	public List<StateRef> listStates() {
		return this.states.findAll()
			.stream()
			.sorted(Comparator.comparing(StateEntity::getName, CODE_POINT_ORDER).thenComparing(StateEntity::getId))
			.map(GeographyMapper::toRef)
			.toList();
	}

	@Override
	@Cacheable(cacheNames = "geography-districts", unless = "#result == null")
	public Optional<List<DistrictInfo>> listDistricts(UUID stateId) {
		if (!this.states.existsById(stateId)) {
			return Optional.empty();
		}
		return Optional.of(this.districts.findByStateId(stateId)
			.stream()
			.sorted(Comparator.comparing(DistrictEntity::getName, CODE_POINT_ORDER).thenComparing(DistrictEntity::getId))
			.map(GeographyMapper::toInfo)
			.toList());
	}

	@Override
	@Cacheable(cacheNames = "geography-localities", unless = "#result == null")
	public Optional<List<LocalityInfo>> listLocalities(UUID districtId) {
		if (!this.districts.existsById(districtId)) {
			return Optional.empty();
		}
		return Optional.of(this.localities.findByDistrictId(districtId)
			.stream()
			.sorted(Comparator.comparing(LocalityEntity::getName, CODE_POINT_ORDER).thenComparing(LocalityEntity::getId))
			.map(GeographyMapper::toInfo)
			.toList());
	}

	@Override
	public Optional<DistrictInfo> findDistrict(UUID districtId) {
		return this.districts.findById(districtId).map(GeographyMapper::toInfo);
	}

	@Override
	public Map<UUID, DistrictInfo> findDistricts(Collection<UUID> districtIds) {
		return this.districts.findAllById(districtIds)
			.stream()
			.map(GeographyMapper::toInfo)
			.collect(Collectors.toMap(DistrictInfo::id, Function.identity()));
	}

	@Override
	public Optional<DistrictInfo> findDistrictByCentroid(BigDecimal latitude, BigDecimal longitude) {
		return this.districts.findByCentroidLatAndCentroidLon(latitude, longitude)
			.stream()
			.findFirst()
			.map(GeographyMapper::toInfo);
	}

	@Override
	public Optional<DistrictInfo> resolveSourceDistrict(String sourceStateName, String sourceDistrictName) {
		return this.aliases.findBySourceStateNameAndSourceDistrictName(sourceStateName, sourceDistrictName)
			.map((alias) -> GeographyMapper.toInfo(alias.getDistrict()));
	}

	@Override
	public PageResponse<DistrictAliasView> listDistrictAliases(UUID districtId, PageParams page) {
		if (districtId != null && !this.districts.existsById(districtId)) {
			throw ApiException.referenceNotFound("districtId");
		}
		List<DistrictAliasView> items = this.aliases.findPage(districtId, page.size(), page.offset())
			.stream()
			.map(GeographyMapper::toView)
			.toList();
		return page.toResponse(items, this.aliases.countPage(districtId));
	}

	@Override
	@Transactional
	public DistrictAliasView addDistrictAlias(String sourceStateName, String sourceDistrictName, UUID districtId,
			String evidenceNote) {
		String state = requireNormalized("sourceStateName", sourceStateName);
		String district = requireNormalized("sourceDistrictName", sourceDistrictName);
		DistrictEntity target = this.districts.findById(districtId)
			.orElseThrow(() -> ApiException.referenceNotFound("districtId"));
		if (this.aliases.findBySourceStateNameAndSourceDistrictName(state, district).isPresent()) {
			throw alreadyMapped();
		}
		try {
			DistrictSourceAliasEntity alias = new DistrictSourceAliasEntity(target, state, district, evidenceNote,
					this.calendar.now());
			return GeographyMapper.toView(this.aliases.saveAndFlush(alias));
		}
		catch (DataIntegrityViolationException ex) {
			throw alreadyMapped();
		}
	}

	private static String requireNormalized(String field, String value) {
		String normalized = TextNormalizer.normalize(value);
		if (normalized == null) {
			throw ApiException.validation(field, FieldErrorCode.TOO_SHORT, "is empty after whitespace normalization");
		}
		return normalized;
	}

	private static ApiException alreadyMapped() {
		return ApiException.conflict("This source district spelling is already mapped.",
				List.of(new FieldError("sourceDistrictName", FieldErrorCode.ALREADY_EXISTS, "is already mapped")));
	}

}
