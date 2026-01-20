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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Registers a cron-based syndication check only when a cron is configured.
 */
@Service
@Profile("!test")
@ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${syndication.check.cron:}')")
public class SyndicationCronScheduler {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(SyndicationCronScheduler.class);

  /** The cron value. */
  @Value("${syndication.check.cron}")
  private String cron;

  /** The scheduler service. */
  private final SyndicationSchedulerService syndicationSchedulerService;

  /**
   * Instantiates a new syndication cron scheduler.
   *
   * @param syndicationSchedulerService the scheduler service
   */
  public SyndicationCronScheduler(final SyndicationSchedulerService syndicationSchedulerService) {
    this.syndicationSchedulerService = syndicationSchedulerService;
  }

  /**
   * Scheduled syndication check using cron expression.
   */
  @Scheduled(cron = "${syndication.check.cron}")
  public void scheduledCheck() {
    final String c = cron;
    logger.info("Starting scheduled syndication check (cron: {})", c);
    syndicationSchedulerService.checkSyndicationCron();
  }
}


