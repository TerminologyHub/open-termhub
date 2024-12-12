/*
 * Copyright 2024 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.util;

/**
 * Custom claims for JWT.
 */
public enum Claims {

	/** The id. */
	ID("termhub:id"),
	/** The role. */
	ROLE("termhub:role"),
	/** The plan. */
	PLAN("termhub:plan"),
	/** The project id. */
	PROJECT_ID("termhub:projectId"),
	/** The org id. */
	ORG_ID("termhub:orgId"),
	/** The salt. */
	SALT("termhub:salt");

	/** The value. */
	private String value;

	/**
	 * Instantiates a {@link Claims} from the specified parameters.
	 *
	 * @param value the value
	 */
	Claims(final String value) {
		this.value = value;
	}

	/**
	 * Returns the value.
	 *
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
}
