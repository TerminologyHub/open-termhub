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
 * System Report utility.
 */
public final class SystemReportUtility {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(SystemReportUtility.class);

  /**
   * Instantiates an empty {@link SystemReportUtility}.
   */
  private SystemReportUtility() {
    // n/a
  }

  /**
   * Log memory.
   */
  public static void logMemory() {

    logger.info("   MEMORY: ({} - {}) = {}", Runtime.getRuntime().totalMemory(),
        Runtime.getRuntime().freeMemory(),
        (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
  }

}
