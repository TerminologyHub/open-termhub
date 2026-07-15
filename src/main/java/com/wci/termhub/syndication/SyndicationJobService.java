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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * Tracks asynchronous syndication jobs.
 */
@Service
public class SyndicationJobService {

  /** The syndication manager provider. */
  private final ObjectProvider<SyndicationManager> syndicationManagerProvider;

  /** The statuses. */
  private final Map<String, SyndicationJobStatus> statuses = new ConcurrentHashMap<>();

  /** The active process id. */
  private final AtomicReference<String> activeProcessId = new AtomicReference<>();

  /**
   * Instantiates a {@link SyndicationJobService} from the specified parameters.
   *
   * @param syndicationManagerProvider the syndication manager provider
   */
  public SyndicationJobService(
      final ObjectProvider<SyndicationManager> syndicationManagerProvider) {
    this.syndicationManagerProvider = syndicationManagerProvider;
  }

  /**
   * Indicates whether syndication is configured.
   *
   * @return true, if syndication is configured
   */
  public boolean isSyndicationConfigured() {
    return syndicationManagerProvider.getIfAvailable() != null;
  }

  /**
   * Indicates whether a syndication job is in progress.
   *
   * @return true, if syndication is in progress
   */
  public boolean isSyndicationInProgress() {
    final String processId = activeProcessId.get();
    if (processId != null) {
      return true;
    }
    final SyndicationManager manager = syndicationManagerProvider.getIfAvailable();
    return manager != null && manager.isSyndicationCheckInProgress();
  }

  /**
   * Starts a job record.
   *
   * @return the job status, or null if a job is active
   */
  public SyndicationJobStatus startJob() {
    final String processId = UUID.randomUUID().toString();
    if (!activeProcessId.compareAndSet(null, processId)) {
      return null;
    }
    final SyndicationJobStatus status = new SyndicationJobStatus(processId);
    statuses.put(processId, status);
    return status;
  }

  /**
   * Returns the job status.
   *
   * @param processId the process id
   * @return the job status
   */
  public SyndicationJobStatus getStatus(final String processId) {
    return statuses.get(processId);
  }

  /**
   * Mark running.
   *
   * @param processId the process id
   */
  public void markRunning(final String processId) {
    final SyndicationJobStatus status = statuses.get(processId);
    if (status != null) {
      status.markRunning();
    }
  }

  /**
   * Mark completed.
   *
   * @param processId the process id
   * @param result the result
   */
  public void markCompleted(final String processId, final SyndicationResults result) {
    final SyndicationJobStatus status = statuses.get(processId);
    if (status != null) {
      status.markCompleted(result);
    }
    activeProcessId.compareAndSet(processId, null);
  }

  /**
   * Mark failed.
   *
   * @param processId the process id
   * @param message the message
   * @param result the result
   */
  public void markFailed(final String processId, final String message,
    final SyndicationResults result) {
    final SyndicationJobStatus status = statuses.get(processId);
    if (status != null) {
      status.markFailed(message, result);
    }
    activeProcessId.compareAndSet(processId, null);
  }
}
