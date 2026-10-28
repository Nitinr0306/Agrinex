package com.agrinex.commodity.service;

import java.util.UUID;

import com.agrinex.commodity.domain.GradeEntity;
import com.agrinex.commodity.domain.VarietyEntity;
import com.agrinex.commodity.repository.GradeRepository;
import com.agrinex.commodity.repository.VarietyRepository;
import com.agrinex.common.time.AgrinexCalendar;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inserts a newly seen variety or grade in its own transaction, so that a unique-constraint race with a
 * concurrent ingestion run fails only this insert and the caller can read the row the other run created.
 */
@Component
class ReferenceRegistrar {

	private final VarietyRepository varieties;

	private final GradeRepository grades;

	private final AgrinexCalendar calendar;

	ReferenceRegistrar(VarietyRepository varieties, GradeRepository grades, AgrinexCalendar calendar) {
		this.varieties = varieties;
		this.grades = grades;
		this.calendar = calendar;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public UUID insertVariety(UUID commodityId, String canonicalName) {
		return this.varieties.saveAndFlush(new VarietyEntity(commodityId, canonicalName, this.calendar.now())).getId();
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public UUID insertGrade(UUID commodityId, String canonicalName) {
		return this.grades.saveAndFlush(new GradeEntity(commodityId, canonicalName, this.calendar.now())).getId();
	}

}
