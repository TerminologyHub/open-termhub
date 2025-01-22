/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.util;

import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;

import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;

/**
 * FHIRServerResponseException.
 */
public class FHIRServerResponseException extends BaseServerResponseException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 205783615885628228L;

	/**
	 * Instantiates a new FHIR server response exception.
	 *
	 * @param theStatusCode           the the status code
	 * @param theMessage              the the message
	 * @param theBaseOperationOutcome the the base operation outcome
	 */
	public FHIRServerResponseException(final int theStatusCode, final String theMessage,
			final IBaseOperationOutcome theBaseOperationOutcome) {
		super(theStatusCode, theMessage, theBaseOperationOutcome);
	}

	/**
	 * Instantiates a new FHIR server response exception.
	 *
	 * @param theStatusCode           the the status code
	 * @param theMessage              the the message
	 * @param theBaseOperationOutcome the the base operation outcome
	 * @param e                       the e
	 */
	public FHIRServerResponseException(final int theStatusCode, final String theMessage,
			final IBaseOperationOutcome theBaseOperationOutcome, final Throwable e) {
		super(theStatusCode, theMessage, e, theBaseOperationOutcome);
	}
}
