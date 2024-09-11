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

/**
 * Generically represents something that can have its model minimized for return
 * of search results or cleaned of admin features for sending over the wire.
 */
public interface HasMinimize {

	/**
	 * Minimize contents for retrieval through REST API.
	 */
	public void minimize();

	/**
	 * Clean for api.
	 */
	public void cleanForApi();

}
