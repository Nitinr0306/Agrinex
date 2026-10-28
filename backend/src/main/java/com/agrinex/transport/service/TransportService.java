package com.agrinex.transport.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.agrinex.common.time.AgrinexCalendar;
import com.agrinex.common.web.PageParams;
import com.agrinex.common.web.PageResponse;
import com.agrinex.transport.TransportApi;
import com.agrinex.transport.TransportAssumptions;
import com.agrinex.transport.domain.TransportAssumptionEntity;
import com.agrinex.transport.mapper.TransportMapper;
import com.agrinex.transport.repository.TransportAssumptionRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Versioned transport assumptions (spec section 32; ADR-0020 item 1). */
@Service
@Transactional(readOnly = true)
class TransportService implements TransportApi {

	private final TransportAssumptionRepository versions;

	private final AgrinexCalendar calendar;

	TransportService(TransportAssumptionRepository versions, AgrinexCalendar calendar) {
		this.versions = versions;
		this.calendar = calendar;
	}

	@Override
	public TransportAssumptions activeAssumptions() {
		return this.versions.findActive()
			.map(TransportMapper::toView)
			.orElseThrow(() -> new IllegalStateException("No active transport assumptions version exists"));
	}

	@Override
	public Optional<TransportAssumptions> findVersion(UUID versionId) {
		return this.versions.findById(versionId).map(TransportMapper::toView);
	}

	@Override
	@Transactional
	public TransportAssumptions updateAssumptions(BigDecimal ratePerKm, BigDecimal minimumTripCharge,
			BigDecimal vehicleCapacityKg) {
		this.versions.lockForVersionUpdate();
		this.versions.deactivateAll();
		TransportAssumptionEntity created = new TransportAssumptionEntity(ratePerKm, minimumTripCharge,
				vehicleCapacityKg, this.calendar.now());
		return TransportMapper.toView(this.versions.saveAndFlush(created));
	}

	@Override
	public PageResponse<TransportAssumptions> listVersions(PageParams page) {
		var result = this.versions.findAllByOrderByCreatedAtDescIdAsc(PageRequest.of(page.page(), page.size()));
		List<TransportAssumptions> items = result.getContent().stream().map(TransportMapper::toView).toList();
		return page.toResponse(items, result.getTotalElements());
	}

}
