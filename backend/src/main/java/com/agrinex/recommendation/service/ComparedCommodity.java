package com.agrinex.recommendation.service;

import java.util.UUID;

import com.agrinex.commodity.CommodityInfo;
import com.agrinex.commodity.NamedRef;

/** The commodity of a comparison with its optional variety and grade filters (null when not set). */
public record ComparedCommodity(CommodityInfo commodity, NamedRef variety, NamedRef grade) {

	UUID varietyId() {
		return (this.variety != null) ? this.variety.id() : null;
	}

	UUID gradeId() {
		return (this.grade != null) ? this.grade.id() : null;
	}

}
