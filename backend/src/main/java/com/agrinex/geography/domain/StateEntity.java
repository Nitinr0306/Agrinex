package com.agrinex.geography.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/** A state (table {@code states}; reference data loaded by migrations, never written by the API). */
@Entity
@Immutable
@Table(name = "states")
public class StateEntity {

	@Id
	private UUID id;

	private String name;

	@Column(name = "name_hi")
	private String nameHi;

	@Column(name = "name_mr")
	private String nameMr;

	private String code;

	@Column(name = "lgd_code")
	private Integer lgdCode;

	protected StateEntity() {
	}

	public UUID getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public String getNameHi() {
		return this.nameHi;
	}

	public String getNameMr() {
		return this.nameMr;
	}

	public String getCode() {
		return this.code;
	}

}
