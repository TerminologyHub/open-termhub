/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for adhoc operations.
 */
public final class AdhocUtility {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(AdhocUtility.class);

  /**
   * Instantiates an empty {@link AdhocUtility}.
   */
  private AdhocUtility() {
    // n/a
  }

  /**
   * Execute.
   *
   * @param mode the mode
   * @throws Exception the exception
   */
  public static void execute(final String mode) throws Exception {

    switch (mode) {
      case "test":
        test();
        break;
      default:
        logger.info("  NO adhoc handler for mode = " + (mode.isEmpty() ? "<blank>" : mode));
        break;
    }

  }

  /**
   * Adds the organization.
   *
   * @throws Exception the exception
   */
  private static void test() throws Exception {

    logger.info("START test");
    logger.info("  do something...");
    logger.info("FINISH test");
  }

}
