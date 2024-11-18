/*
 * Copyright 2024 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract superclass for source code tests.
 */
public class AbstractTest {

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(AbstractTest.class);

	/** The setup. */
	private static boolean setup = false;

	/**
	 * Setup once. NOTE: Using @BeforeAll means the PropertyUtility is not yet
	 * configured.
	 *
	 * @throws Exception the exception
	 */
	@BeforeEach
	public void setupOnce() throws Exception {
//        if (!setup) {
//            setup = true;
//            if (!PropertyUtility.getProperties().getProperty("hibernate.hbm2ddl.auto")
//                    .equals("create")) {
//                MigrationUtility.migrateDatabase("MODEL");
//            } else {
//                try (final RootService service = new RootServiceImpl()) {
//                    service.close();
//                }
//            }
//
//        }
	}

	/**
	 * Sets the up.
	 *
	 * @param testInfo the up
	 */
	@BeforeEach
	void setUp(final TestInfo testInfo) {
		logger.info("Starting test - {}", testInfo.getDisplayName());
	}

	/**
	 * Tear down.
	 *
	 * @param testInfo the test info
	 */
	@AfterEach
	void tearDown(final TestInfo testInfo) {
		logger.info("Finish test - {}", testInfo.getDisplayName());
	}

	/**
	 * Teardown once.
	 *
	 * @throws Exception the exception
	 */
	@AfterAll
	public static void teardownOnce() throws Exception {
		// n/a - use in memory db, this just goes away
	}

}
