/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.syndication.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import com.wci.termhub.syndication.SyndicationJobRunner;
import com.wci.termhub.syndication.SyndicationJobService;
import com.wci.termhub.syndication.SyndicationJobStatus;
import com.wci.termhub.syndication.SyndicationManager;
import com.wci.termhub.syndication.SyndicationResults;

/**
 * Unit test for {@link SyndicationJobRunner}.
 */
public class SyndicationJobRunnerUnitTest {

  /** The runner. */
  private SyndicationJobRunner runner;

  /** The service. */
  private SyndicationJobService service;

  /** The provider. */
  private ObjectProvider<SyndicationManager> provider;

  /** The manager. */
  private SyndicationManager manager;

  /**
   * Sets up the test.
   */
  @SuppressWarnings("unchecked")
  @BeforeEach
  public void setUp() {
    provider = mock(ObjectProvider.class);
    manager = mock(SyndicationManager.class);
    service = new SyndicationJobService(provider);
    runner = new SyndicationJobRunner(provider, service);
  }

  /**
   * Test successful run.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRunSyndicationSuccess() throws Exception {
    when(provider.getIfAvailable()).thenReturn(manager);
    when(manager.triggerSyndicationCheck())
        .thenReturn(new SyndicationResults(true, "done", 1, 1, 0, 100L));
    final SyndicationJobStatus status = service.startJob();

    runner.runSyndication(status.getProcessId());

    final SyndicationJobStatus updated = service.getStatus(status.getProcessId());
    assertEquals(SyndicationJobStatus.Status.COMPLETED, updated.getStatus());
    assertEquals("done", updated.getMessage());
    assertEquals(1, updated.getResult().getTotalLoaded());
  }

  /**
   * Test failed run.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRunSyndicationFailedResult() throws Exception {
    when(provider.getIfAvailable()).thenReturn(manager);
    when(manager.triggerSyndicationCheck())
        .thenReturn(new SyndicationResults(false, "failed", 0, 0, 1, 100L));
    final SyndicationJobStatus status = service.startJob();

    runner.runSyndication(status.getProcessId());

    final SyndicationJobStatus updated = service.getStatus(status.getProcessId());
    assertEquals(SyndicationJobStatus.Status.FAILED, updated.getStatus());
    assertEquals("failed", updated.getMessage());
    assertEquals(1, updated.getResult().getTotalErrors());
  }
}
