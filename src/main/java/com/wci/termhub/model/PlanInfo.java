/*
 * Copyright 2024 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.model;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Represents a license for access to content.
 */
@Entity
@Table(name = "plan_info")
@Schema(description = "Represents a content plan from which data can be obtained")
@Document(indexName = "plan-info-v1")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanInfo extends AbstractHasModified implements HasLazyInit, Copyable<PlanInfo> {

	/** The type. e.g. UNLIMITED, STARTER. */
	@Column(nullable = false, length = 256)
	@Field(type = FieldType.Keyword)
	private String type;

	/** The term - # of <unit>s from the starting date the plan is valid for. */
	@Transient
	@Field(type = FieldType.Integer)
	private Integer term;

	/** The term unit - e.g. "day", "month", "year". */
	@Transient
	@Field(type = FieldType.Keyword)
	private String termUnit;

	/** The uri. e.g. main website */
	@Transient
	@Field(type = FieldType.Text)
	private String description;

	// LATER: cost, features

	/**
	 * Instantiates an empty {@link PlanInfo}.
	 */
	public PlanInfo() {
		// n/a
	}

	/**
	 * Instantiates a {@link PlanInfo} from the specified parameters.
	 *
	 * @param other the other
	 */
	public PlanInfo(final PlanInfo other) {
		populateFrom(other);
	}

	/**
	 * Populate from.
	 *
	 * @param other the other
	 */
	@Override
	public void populateFrom(final PlanInfo other) {
		super.populateFrom(other);
		type = other.getType();
		term = other.getTerm();
		termUnit = other.getTermUnit();
		description = other.getDescription();
	}

	/**
	 * Patch from.
	 *
	 * @param other the other
	 */
	@Override
	public void patchFrom(final PlanInfo other) {
		super.patchFrom(other);
		if (other.getType() != null) {
			type = other.getType();
		}
		if (other.getTerm() != null) {
			term = other.getTerm();
		}
		if (other.getTermUnit() != null) {
			termUnit = other.getTermUnit();
		}

		if (other.getDescription() != null) {
			description = other.getDescription();
		}

	}

//	/* see superclass */
//	@Override
//	@PostLoad
//	public void unmarshall() throws Exception {
//		final PlanInfo resource = ModelUtility.fromJson(getData(), this.getClass());
//		if (resource != null) {
//			populateFrom(resource);
//		}
//	}

	/* see superclass */
	@Override
	public void lazyInit() {
		// n/a
	}

	/**
	 * Returns the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type.
	 *
	 * @param type the type
	 */
	public void setType(final String type) {
		this.type = type;
	}

	/**
	 * Gets the term.
	 *
	 * @return the term
	 */
	public Integer getTerm() {
		return term;
	}

	/**
	 * Sets the term.
	 *
	 * @param term the new term
	 */
	public void setTerm(final Integer term) {
		this.term = term;
	}

	/**
	 * Gets the term unit.
	 *
	 * @return the term unit
	 */
	public String getTermUnit() {
		return termUnit;
	}

	/**
	 * Sets the term unit.
	 *
	 * @param termUnit the new term unit
	 */
	public void setTermUnit(final String termUnit) {
		this.termUnit = termUnit;
	}

	/**
	 * Returns the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the description
	 */
	public void setDescription(final String description) {
		this.description = description;
	}

}
