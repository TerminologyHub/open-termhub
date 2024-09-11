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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * Abstractly represents something that changes over time.
 */
@MappedSuperclass
public abstract class AbstractHasModified extends AbstractHasId implements HasModified {

	/** The logger. */
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(AbstractHasModified.class);

	/** The modified. */
	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@Field(type = FieldType.Date, format = DateFormat.epoch_millis)
	private Date modified;

	/** The created. */
	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@Field(type = FieldType.Date, format = DateFormat.epoch_millis)
	private Date created;

	/** The modified by. */
	@Column(nullable = false)
	@Field(type = FieldType.Keyword)
	private String modifiedBy;

	/** The local. */
	@Column(nullable = false)
	@Field(type = FieldType.Boolean)
	private Boolean local = false;

	/** The active. */
	@Column(nullable = false)
	@Field(type = FieldType.Boolean)
	private Boolean active = true;

	/**
	 * Instantiates an empty {@link AbstractHasModified}.
	 */
	protected AbstractHasModified() {
		super();
	}

	/**
	 * Instantiates a {@link AbstractHasModified} from the specified parameters.
	 *
	 * @param other the other
	 */
	protected AbstractHasModified(final HasModified other) {
		populateFrom(other);
	}

	/**
	 * Populate from.
	 *
	 * @param other the other
	 */
	public void populateFrom(final HasModified other) {
		// Only copy this stuff if the object has an id
		if (other.getId() != null) {
			super.populateFrom(other);
			created = other.getCreated();
			modified = other.getModified();
			modifiedBy = other.getModifiedBy();
		}
		if (other.getLocal() != null) {
			local = other.getLocal();
		}
		if (other.getActive() != null) {
			active = other.getActive();
		}
	}

	/**
	 * Patch from.
	 *
	 * @param other the other
	 */
	public void patchFrom(final HasModified other) {
		// Only these field can be patched
		if (other.getLocal() != null) {
			local = other.getLocal();
		}
		if (other.getActive() != null) {
			active = other.getActive();
		}
	}

	/* see superclass */
	@Override
	public Boolean getActive() {
		return active;
	}

	/* see superclass */
	@Override
	public void setActive(final Boolean active) {
		this.active = active;
	}

	/* see superclass */
	@Override
	public Boolean getLocal() {
		return local;
	}

	/* see superclass */
	@Override
	public void setLocal(final Boolean local) {
		this.local = local;
	}

	/* see superclass */
	@Override
	public Date getModified() {
		return modified;
	}

	/* see superclass */
	@Override
	public void setModified(final Date modified) {
		this.modified = modified;
	}

	/* see superclass */
	@Override
	public Date getCreated() {
		return created;
	}

	/* see superclass */
	@Override
	public void setCreated(final Date created) {
		this.created = created;
	}

	/* see superclass */
	@Override
	public String getModifiedBy() {
		return modifiedBy;
	}

	/* see superclass */
	@Override
	public void setModifiedBy(final String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	/**
	 * Clear tracking fields.
	 */
	@Override
	public void clearTrackingFields() {
		setId(null);
		created = null;
		modified = null;
		modifiedBy = null;
		active = true;
	}

	/**
	 * Minimize.
	 */
	public void minimize() {
		modified = null;
		created = null;
		modifiedBy = null;
		local = null;
		active = null;
	}

	// equals/hashcode by superclass
}
