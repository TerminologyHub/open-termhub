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

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Generically represents something with name/value attribute pairs.
 */
public interface HasAttributes {

	/**
	 * Returns the attributes.
	 *
	 * @return the attributes
	 */
	@Schema(description = "Key/value pairs associated with this object")
	public Map<String, String> getAttributes();

	/**
	 * Sets the attributes.
	 *
	 * @param attributes the attributes
	 */
	public void setAttributes(Map<String, String> attributes);
}
