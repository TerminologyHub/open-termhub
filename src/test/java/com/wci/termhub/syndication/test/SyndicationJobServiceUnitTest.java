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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import com.wci.termhub.syndication.SyndicationJobService;
import com.wci.termhub.syndication.SyndicationJobStatus;
import com.wci.termhub.syndication.SyndicationManager;

/**
 * Unit test for {@link SyndicationJobService}.
 */
public class SyndicationJobServiceUnitTest {

  /** The service. */
  private SyndicationJobService service;

  /** The manager provider. */
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
  }

  /**
   * Test configured status.
   */
  @Test
  public void testIsSyndicationConfigured() {
    when(provider.getIfAvailable()).thenReturn(null);
    assertFalse(service.isSyndicationConfigured());

    when(provider.getIfAvailable()).thenReturn(manager);
    assertTrue(service.isSyndicationConfigured());
  }

  /**
   * Test start job.
   */
  @Test
  public void testStartJob() {
    final SyndicationJobStatus status = service.startJob();

    assertNotNull(status);
    assertNotNull(status.getProcessId());
    assertEquals(SyndicationJobStatus.Status.QUEUED, status.getStatus());
    assertEquals(status, service.getStatus(status.getProcessId()));
    assertNull(service.startJob());
  }

  /**
   * Test complete job clears active job.
   */
  @Test
  public void testMarkCompletedClearsActiveJob() {
    final SyndicationJobStatus status = service.startJob();

    service.markCompleted(status.getProcessId(), null);

    assertEquals(SyndicationJobStatus.Status.COMPLETED,
        service.getStatus(status.getProcessId()).getStatus());
    assertNotNull(service.startJob());
  }

  /**
   * Test manager in-progress status.
   */
  @Test
  public void testIsSyndicationInProgressFromManager() {
    when(provider.getIfAvailable()).thenReturn(manager);
    when(manager.isSyndicationCheckInProgress()).thenReturn(true);

    assertTrue(service.isSyndicationInProgress());
  }
}
