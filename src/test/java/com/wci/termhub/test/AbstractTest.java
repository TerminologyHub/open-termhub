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

import com.wci.termhub.util.PropertyUtility;

/**
 * Abstract superclass for source code tests.
 */
public abstract class AbstractTest {

  /** The logger. */
  @SuppressWarnings("unused")
  private static Logger logger = LoggerFactory.getLogger(AbstractTest.class);

  /** The setup. */
  private static boolean setup = false;

  /**
   * Setup once. NOTE: Using @BeforeAll means the PropertyUtility is not yet configured.
   *
   * @throws Exception the exception
   */
  @BeforeEach
  public void setupOnce() throws Exception {
    if (!setup) {
      setup = true;
      PropertyUtility.getProperties();
    }
  }

}
