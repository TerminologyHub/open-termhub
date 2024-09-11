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

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Generically represents something that can be tracked as it changes.
 */
public interface HasModified extends HasId, HasActive {

	/**
	 * Returns the modified.
	 *
	 * @return the modified
	 */
	@Schema(description = "Last modified date")
	public Date getModified();

	/**
	 * Sets the modified.
	 *
	 * @param modified the modified
	 */
	public void setModified(final Date modified);

	/**
	 * Returns the created.
	 *
	 * @return the created
	 */
	@Schema(description = "Created date")
	public Date getCreated();

	/**
	 * Sets the created.
	 *
	 * @param created the created
	 */
	public void setCreated(final Date created);

	/**
	 * Returns the modified by.
	 *
	 * @return the modified by
	 */
	@Schema(description = "Last modified by")
	public String getModifiedBy();

	/**
	 * Sets the modified by.
	 *
	 * @param modifiedBy the modified by
	 */
	public void setModifiedBy(final String modifiedBy);

	/**
	 * Indicates whether or not local is the case.
	 *
	 * @return <code>true</code> if so, <code>false</code> otherwise
	 */
	@Schema(description = "Indicates whether this data element is locally created")
	public Boolean getLocal();

	/**
	 * Sets the local flag.
	 *
	 * @param local the local flag
	 */
	public void setLocal(Boolean local);

	/**
	 * Clear tracking fields.
	 */
	public void clearTrackingFields();

}
