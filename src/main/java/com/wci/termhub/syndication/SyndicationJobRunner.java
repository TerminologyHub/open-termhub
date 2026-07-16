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

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Runs syndication jobs asynchronously.
 */
@Service
public class SyndicationJobRunner {

  /** The syndication manager provider. */
  private final ObjectProvider<SyndicationManager> syndicationManagerProvider;

  /** The job service. */
  private final SyndicationJobService syndicationJobService;

  /**
   * Instantiates a {@link SyndicationJobRunner} from the specified parameters.
   *
   * @param syndicationManagerProvider the syndication manager provider
   * @param syndicationJobService the syndication job service
   */
  public SyndicationJobRunner(final ObjectProvider<SyndicationManager> syndicationManagerProvider,
      final SyndicationJobService syndicationJobService) {
    this.syndicationManagerProvider = syndicationManagerProvider;
    this.syndicationJobService = syndicationJobService;
  }

  /**
   * Runs the syndication job.
   *
   * @param processId the process id
   */
  @Async
  public void runSyndication(final String processId) {
    syndicationJobService.markRunning(processId);

    try {
      final SyndicationManager manager = syndicationManagerProvider.getIfAvailable();
      if (manager == null) {
        syndicationJobService.markFailed(processId, "Syndication is not configured", null);
        return;
      }

      final SyndicationResults results = manager.triggerSyndicationCheck();
      if (results.isSuccess()) {
        syndicationJobService.markCompleted(processId, results);
      } else {
        syndicationJobService.markFailed(processId, results.getMessage(), results);
      }
    } catch (final Exception e) {
      syndicationJobService.markFailed(processId, "Syndication check failed: " + e.getMessage(),
          null);
    }
  }
}
