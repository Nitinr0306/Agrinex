package com.agrinex.commodity.service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.agrinex.commodity.CommodityApi;
import com.agrinex.commodity.CommodityAvailabilityPort;
import com.agrinex.commodity.CommodityInfo;
import com.agrinex.commodity.NamedRef;
import com.agrinex.commodity.domain.CommodityEntity;
import com.agrinex.commodity.domain.GradeEntity;
import com.agrinex.commodity.domain.VarietyEntity;
import com.agrinex.commodity.mapper.CommodityMapper;
import com.agrinex.commodity.repository.CommodityRepository;
import com.agrinex.commodity.repository.CommoditySourceAliasRepository;
import com.agrinex.commodity.repository.GradeRepository;
import com.agrinex.commodity.repository.VarietyRepository;
import com.agrinex.common.TextNormalizer;
import com.agrinex.common.cache.ReferenceCache;
import com.agrinex.common.error.ApiErrorResponse.FieldError;
import com.agrinex.common.error.ApiException;
import com.agrinex.common.error.FieldErrorCode;
import com.agrinex.common.time.AgrinexCalendar;
import com.agrinex.common.web.PageParams;
import com.agrinex.common.web.PageResponse;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The commodity known list (ADR-0006), verified units (ADR-0008), varieties and grades. Reference reads are
 * cached for one hour and evicted by this module on every write, including the automatic registration of
 * varieties and grades (ADR-0018 item 1). Lists are ordered by name in code-point order, then id.
 */
@Service
@Transactional(readOnly = true)
class CommodityService implements CommodityApi {

	static final String ALL_COMMODITIES_CACHE = "commodities";

	static final String VARIETIES_CACHE = "commodity-varieties";

	static final String GRADES_CACHE = "commodity-grades";

	private static final String ALL_KEY = "all";

	private static final Comparator<NamedRef> NAMED_ORDER = Comparator.comparing(NamedRef::name, String::compareTo)
		.thenComparing(NamedRef::id);

	private final CommodityRepository commodities;

	private final CommoditySourceAliasRepository aliases;

	private final VarietyRepository varieties;

	private final GradeRepository grades;

	private final ReferenceRegistrar registrar;

	private final ReferenceCache cache;

	private final AgrinexCalendar calendar;

	private final ObjectProvider<CommodityAvailabilityPort> availability;

	CommodityService(CommodityRepository commodities, CommoditySourceAliasRepository aliases,
			VarietyRepository varieties, GradeRepository grades, ReferenceRegistrar registrar, ReferenceCache cache,
			AgrinexCalendar calendar, ObjectProvider<CommodityAvailabilityPort> availability) {
		this.commodities = commodities;
		this.aliases = aliases;
		this.varieties = varieties;
		this.grades = grades;
		this.registrar = registrar;
		this.cache = cache;
		this.calendar = calendar;
		this.availability = availability;
	}

	@Override
	public List<CommodityInfo> listOfferedCommodities() {
		Set<UUID> available = this.availability.getObject().commoditiesWithRecentObservations();
		return allCommodities().stream().filter((commodity) -> available.contains(commodity.id())).toList();
	}

	@Override
	public Optional<List<NamedRef>> listVarieties(UUID commodityId) {
		if (!this.commodities.existsById(commodityId)) {
			return Optional.empty();
		}
		return Optional.of(this.cache.get(VARIETIES_CACHE, commodityId,
				() -> sorted(this.varieties.findByCommodityId(commodityId).stream().map(CommodityMapper::toRef))));
	}

	@Override
	public Optional<List<NamedRef>> listGrades(UUID commodityId) {
		if (!this.commodities.existsById(commodityId)) {
			return Optional.empty();
		}
		return Optional.of(this.cache.get(GRADES_CACHE, commodityId,
				() -> sorted(this.grades.findByCommodityId(commodityId).stream().map(CommodityMapper::toRef))));
	}

	@Override
	public Optional<CommodityInfo> findCommodity(UUID commodityId) {
		return this.commodities.findById(commodityId).map(CommodityMapper::toInfo);
	}

	@Override
	public Map<UUID, CommodityInfo> findCommodities(Collection<UUID> commodityIds) {
		return this.commodities.findAllById(commodityIds)
			.stream()
			.map(CommodityMapper::toInfo)
			.collect(Collectors.toMap(CommodityInfo::id, Function.identity()));
	}

	@Override
	public Optional<CommodityInfo> findByCanonicalName(String normalizedName) {
		return this.commodities.findByCanonicalName(normalizedName).map(CommodityMapper::toInfo);
	}

	@Override
	public Optional<CommodityInfo> resolveSourceName(String normalizedName) {
		Optional<CommodityInfo> canonical = findByCanonicalName(normalizedName);
		if (canonical.isPresent()) {
			return canonical;
		}
		return this.aliases.findBySourceCommodityName(normalizedName)
			.flatMap((alias) -> this.commodities.findById(alias.getCommodityId()))
			.map(CommodityMapper::toInfo);
	}

	@Override
	public Optional<NamedRef> findVariety(UUID varietyId) {
		return this.varieties.findById(varietyId).map(CommodityMapper::toRef);
	}

	@Override
	public Optional<NamedRef> findGrade(UUID gradeId) {
		return this.grades.findById(gradeId).map(CommodityMapper::toRef);
	}

	@Override
	public Map<UUID, NamedRef> findVarieties(Collection<UUID> varietyIds) {
		return this.varieties.findAllById(varietyIds)
			.stream()
			.map(CommodityMapper::toRef)
			.collect(Collectors.toMap(NamedRef::id, Function.identity()));
	}

	@Override
	public Map<UUID, NamedRef> findGrades(Collection<UUID> gradeIds) {
		return this.grades.findAllById(gradeIds)
			.stream()
			.map(CommodityMapper::toRef)
			.collect(Collectors.toMap(NamedRef::id, Function.identity()));
	}

	@Override
	public UUID registerVariety(UUID commodityId, String normalizedName) {
		return findOrRegister(() -> this.varieties.findByCommodityIdAndCanonicalName(commodityId, normalizedName)
			.map(VarietyEntity::getId), () -> this.registrar.insertVariety(commodityId, normalizedName),
				VARIETIES_CACHE, commodityId);
	}

	@Override
	public UUID registerGrade(UUID commodityId, String normalizedName) {
		return findOrRegister(() -> this.grades.findByCommodityIdAndCanonicalName(commodityId, normalizedName)
			.map(GradeEntity::getId), () -> this.registrar.insertGrade(commodityId, normalizedName), GRADES_CACHE,
				commodityId);
	}

	@Override
	public PageResponse<CommodityInfo> listAdminCommodities(Boolean unitVerified, String search, PageParams page) {
		List<CommodityInfo> items = this.commodities.findPage(unitVerified, search, page.size(), page.offset())
			.stream()
			.map(CommodityMapper::toInfo)
			.toList();
		return page.toResponse(items, this.commodities.countPage(unitVerified, search));
	}

	@Override
	@Transactional
	public CommodityInfo addCommodity(String name, String category, String priceUnit, String priceUnitSource) {
		String canonicalName = TextNormalizer.normalize(name);
		if (canonicalName == null) {
			throw ApiException.validation("name", FieldErrorCode.TOO_SHORT, "is empty after whitespace normalization");
		}
		if (this.commodities.findByCanonicalName(canonicalName).isPresent()) {
			throw nameExists();
		}
		try {
			CommodityEntity saved = this.commodities.saveAndFlush(
					new CommodityEntity(canonicalName, category, priceUnit, priceUnitSource, this.calendar.now()));
			this.cache.clear(ALL_COMMODITIES_CACHE);
			return CommodityMapper.toInfo(saved);
		}
		catch (DataIntegrityViolationException ex) {
			throw nameExists();
		}
	}

	@Override
	@Transactional
	public CommodityInfo verifyPriceUnit(UUID commodityId, String priceUnit, String priceUnitSource) {
		CommodityEntity commodity = requireCommodity(commodityId);
		if (commodity.getPriceUnit() != null) {
			throw ApiException.conflict("A verified unit is already recorded and is never changed.");
		}
		commodity.verifyUnit(priceUnit, priceUnitSource);
		this.cache.clear(ALL_COMMODITIES_CACHE);
		return CommodityMapper.toInfo(commodity);
	}

	@Override
	@Transactional
	public CommodityInfo updateNames(UUID commodityId, String nameHi, String nameMr) {
		CommodityEntity commodity = requireCommodity(commodityId);
		commodity.rename(nameHi, nameMr);
		this.cache.clear(ALL_COMMODITIES_CACHE);
		return CommodityMapper.toInfo(commodity);
	}

	private List<CommodityInfo> allCommodities() {
		return this.cache.get(ALL_COMMODITIES_CACHE, ALL_KEY, () -> this.commodities.findAll()
			.stream()
			.map(CommodityMapper::toInfo)
			.sorted(Comparator.comparing(CommodityInfo::name, String::compareTo).thenComparing(CommodityInfo::id))
			.toList());
	}

	private CommodityEntity requireCommodity(UUID commodityId) {
		return this.commodities.findById(commodityId)
			.orElseThrow(() -> ApiException.notFound("The commodity does not exist."));
	}

	/** Finds the row, or inserts it; a concurrent insert of the same name is resolved by reading the winner. */
	private UUID findOrRegister(Supplier<Optional<UUID>> finder, Supplier<UUID> inserter, String cacheName,
			UUID commodityId) {
		Optional<UUID> existing = finder.get();
		if (existing.isPresent()) {
			return existing.get();
		}
		try {
			UUID id = inserter.get();
			this.cache.evict(cacheName, commodityId);
			return id;
		}
		catch (DataIntegrityViolationException raced) {
			return finder.get().orElseThrow(() -> raced);
		}
	}

	private static List<NamedRef> sorted(java.util.stream.Stream<NamedRef> refs) {
		return refs.sorted(NAMED_ORDER).toList();
	}

	private static ApiException nameExists() {
		return ApiException.conflict("A commodity with this name already exists.",
				List.of(new FieldError("name", FieldErrorCode.ALREADY_EXISTS, "already exists")));
	}

}
