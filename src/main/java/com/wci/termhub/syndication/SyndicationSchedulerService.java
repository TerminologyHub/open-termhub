/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.syndication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service for scheduled syndication checks.
 */
@Service
@ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${syndication.token:}')")
public class SyndicationSchedulerService {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(SyndicationSchedulerService.class);

  /** The syndication manager. */
  @Autowired
  private SyndicationManager syndicationManager;

  /** The syndication check interval. */
  @Value("${syndication.check.interval}")
  private String syndicationCheckInterval;

  /** The syndication completed flag file. */
  private static final String SYNDICATION_COMPLETED_FILE = "syndication.completed";

  /**
   * Scheduled syndication check using fixed rate interval. This method runs at
   * the configured
   * interval for testing purposes.
   */
  @Scheduled(fixedRateString = "${syndication.check.interval}", initialDelayString = "0")
  public void checkSyndicationFixedRate() {
    // Check if syndication was already completed (persistent check)
    if (isSyndicationCompleted()) {
      logger.debug("Initial syndication already completed, skipping scheduled check");
      return;
    }

    logger.info("Starting initial syndication check (fixed rate: {})", syndicationCheckInterval);
    performSyndicationCheck();
    setSyndicationCompleted();
  }

  /**
   * Perform the actual syndication check.
   */
  private void performSyndicationCheck() {
    try {

      // Use the syndication manager to perform the complete check and load
      // process
      final SyndicationResults results = syndicationManager.performSyndicationCheck();

      if (results.isSuccess()) {
        logger.info(
            "Syndication check completed successfully - Processed: {}, Loaded: {}, Errors: {}, Duration: {} ms",
            results.getTotalProcessed(), results.getTotalLoaded(), results.getTotalErrors(),
            results.getDurationMs());
      } else {
        if ("Syndication check already in progress".equals(results.getMessage())) {
          logger.info("Syndication check skipped - already in progress");
        } else {
          logger.error("Syndication check failed: {}", results.getMessage());
        }
      }

    } catch (final Exception e) {
      logger.error("Error during syndication check", e);
    }
  }

  /**
   * Manual trigger for syndication check. This method can be called
   * programmatically or via REST
   * endpoint.
   */
  public void triggerSyndicationCheck() {
    logger.info("Manual syndication check triggered");
    performSyndicationCheck();
  }

  /**
   * Get syndication check status.
   *
   * @return the status information
   */
  public SyndicationManager.SyndicationStatus getSyndicationStatus() {
    return syndicationManager.getSyndicationStatus();
  }

  /**
   * Check if syndication was already completed (persistent check).
   *
   * @return true if syndication was completed
   */
  private boolean isSyndicationCompleted() {
    try {
      return new java.io.File(SYNDICATION_COMPLETED_FILE).exists();
    } catch (final Exception e) {
      logger.warn("Error checking syndication completion status", e);
      return false;
    }
  }

  /**
   * Mark syndication as completed (persistent).
   */
  private void setSyndicationCompleted() {
    // NOTE: it should not be necessary to mark this
    // especially with syndication
    // try {
    // new java.io.File(SYNDICATION_COMPLETED_FILE).createNewFile();
    // logger.info("Syndication completion marked as persistent");
    // } catch (final Exception e) {
    // logger.error("Error marking syndication as completed", e);
    // }
  }
}
