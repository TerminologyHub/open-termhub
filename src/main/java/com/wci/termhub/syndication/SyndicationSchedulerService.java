/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
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
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Service for scheduled syndication checks.
 */
@Service
@Profile("!test")
@ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${syndication.token:}')")
public class SyndicationSchedulerService {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(SyndicationSchedulerService.class);

  /** The syndication manager. */
  @Autowired
  private SyndicationManager syndicationManager;

  /** The syndication check cron (overrides interval when present). */
  @Value("${syndication.check.cron:}")
  private String syndicationCheckCron;

  /** Whether to trigger a syndication check on application startup. */
  @Value("${syndication.check.on-startup:#{null}}")
  private Boolean syndicationCheckOnStartup;

  /**
   * Syndication check triggered by scheduler (when configured) or manually.
   */
  public void checkSyndicationCron() {
    logger.info("Starting scheduled syndication check (cron: {})", syndicationCheckCron);
    performSyndicationCheck();
  }

  /**
   * Trigger a syndication check on application startup.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationReady() {
    if (Boolean.TRUE.equals(syndicationCheckOnStartup)) {
      logger.info("Syndication check triggered at application startup");
      performSyndicationCheck();
    } else {
      logger.info("Syndication check on startup disabled or not set");
    }
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
            "Syndication check completed successfully - Processed: {}, Loaded: {}, Errors: {}, Duration: {} ms "
            + "| CodeSystems: loaded={}, errors={} "
            + "| ValueSets: loaded={}, errors={} "
            + "| ConceptMaps: loaded={}, errors={}",
            results.getTotalProcessed(), results.getTotalLoaded(), results.getTotalErrors(),
            results.getDurationMs(), results.getCodeSystemLoaded(), results.getCodeSystemErrors(),
            results.getValueSetLoaded(), results.getValueSetErrors(), results.getConceptMapLoaded(),
            results.getConceptMapErrors());
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
  public void syndicationCheck() {
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

}
