/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.rest.client;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wci.termhub.model.HealthCheck;

/**
 * Top level class for all REST clients.
 */
public class RootClientRestMock extends RootClientRestImpl {

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(RootClientRestMock.class);

	/** The service. */
	private final String service;

	/**
	 * Instantiates a {@link RootClientRestMock} from the specified parameters.
	 *
	 * @param service the service
	 * @throws Exception the exception
	 */
	public RootClientRestMock(final String service) throws Exception {
		super(service);
		this.service = service;
	}

	/* see superclass */
	@Override
	public void admin(final String task, final String adminKey, final String payload) throws Exception {
		logger.info("  MOCK admin");
	}

	/* see superclass */
	@Override
	public HealthCheck health(final Boolean dependencies) throws Exception {
		final HealthCheck check = new HealthCheck();
		check.setStatus(true);
		check.setTimestamp(new Date());
		check.setName("termhub-" + service + "-service");
		return check;
	}

}
