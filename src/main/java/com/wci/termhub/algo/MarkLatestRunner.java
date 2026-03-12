/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.algo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Helper for running {@link MarkLatestAlgorithm} after load operations complete.
 */
@Component
public class MarkLatestRunner {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(MarkLatestRunner.class);

  /** The application context. */
  @Autowired
  private ApplicationContext applicationContext;

  /**
   * Run the mark-latest algorithm for the specified terminology abbreviation
   * and publisher.
   *
   * Any exceptions are logged but not propagated, so that load operations are
   * not aborted solely due to mark-latest failures.
   *
   * @param abbreviation the terminology/mapset/subset abbreviation
   * @param publisher the publisher
   */
  public void run(final String abbreviation, final String publisher) {

    try {
      final MarkLatestAlgorithm algo = applicationContext.getBean(MarkLatestAlgorithm.class);
      algo.setTerminology(abbreviation);
      algo.setPublisher(publisher);
      algo.compute();
    } catch (final Exception e) {
      LOGGER.error("Failed to mark latest for {}/{}", abbreviation, publisher, e);
    }
  }

}
