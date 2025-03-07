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

import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.wci.termhub.model.Concept;

/**
 * Unit test for {@link Concept}.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class LoggerUnitTest extends AbstractTest {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(LoggerUnitTest.class);

  /**
   * Test.
   *
   * @throws Exception the exception
   */
  @Test
  public void test() throws Exception {
    // Change appender to "AppConsole" to see USERID interpolation and JSON
    ThreadContext.put("user-id", "USERID");
    logger.info("test message");

  }

}
