/*
 * Copyright 2024 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.handler;

/**
 * Handler for dealing with in-memory file data. Used for unzipping files.
 */
public interface FileCallbackHandler {

	/**
	 * Callback.
	 *
	 * @param filename the filename
	 * @param mimeType the mime type
	 * @param data     the data
	 * @throws Exception the exception
	 */
	public void callback(final String filename, final String mimeType, final byte[] data) throws Exception;
}
