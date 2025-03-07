/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.test;

import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract superclass for source code tests.
 */
public class AbstractTerminologyServerTest extends AbstractTest {

  /** The logger. */
  @SuppressWarnings("unused")
  private final Logger logger = LoggerFactory.getLogger(AbstractTerminologyServerTest.class);

  /** The setup. */
  private static boolean setup2 = false;

  /**
   * Setup once. Use @BeforeEach because with @BeforeAll, PropertyUtility will
   * not be setup.
   *
   * @throws Exception the exception
   */
  @BeforeEach
  public void setupOnce2() throws Exception {
    if (!setup2) {
      setup2 = true;
    }
  }

}
