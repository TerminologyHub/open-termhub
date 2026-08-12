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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.wci.termhub.ReadOnlyMode;
import com.wci.termhub.syndication.SyndicationManager;
import com.wci.termhub.syndication.SyndicationSchedulerService;

/**
 * Unit test for SyndicationSchedulerService.
 */
public class SyndicationSchedulerServiceUnitTest {

  /** The scheduler. */
  private SyndicationSchedulerService scheduler;

  /** The mock manager. */
  private SyndicationManager mockManager;

  /** The read only mode. */
  private ReadOnlyMode readOnlyMode;

  /**
   * Sets the up.
   */
  @BeforeEach
  public void setUp() {
    mockManager = mock(SyndicationManager.class);
    readOnlyMode = mock(ReadOnlyMode.class);
    scheduler = new SyndicationSchedulerService();

    ReflectionTestUtils.setField(scheduler, "syndicationManager", mockManager);
    ReflectionTestUtils.setField(scheduler, "readOnlyMode", readOnlyMode);

    final SyndicationManager injectedManager =
        (SyndicationManager) ReflectionTestUtils.getField(scheduler, "syndicationManager");
    if (injectedManager != mockManager) {
      throw new RuntimeException(
          "Mock injection failed - expected mock but got: " + injectedManager);
    }
  }

  /**
   * Test constructor.
   */
  @Test
  public void testConstructor() {
    final SyndicationSchedulerService newScheduler = new SyndicationSchedulerService();
    assertNotNull(newScheduler);
  }

  /**
   * Skips syndication when read-only mode is enabled.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSkipsSyndicationWhenReadOnly() throws Exception {
    when(readOnlyMode.isEnabled()).thenReturn(true);

    scheduler.syndicationCheck();

    verify(mockManager, never()).performSyndicationCheck();
  }

  /**
   * Runs syndication when read-only mode is disabled.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRunsSyndicationWhenNotReadOnly() throws Exception {
    when(readOnlyMode.isEnabled()).thenReturn(false);
    when(mockManager.performSyndicationCheck())
        .thenReturn(new com.wci.termhub.syndication.SyndicationResults(true, "ok", 0, 0, 0, 0L));

    scheduler.syndicationCheck();

    verify(mockManager).performSyndicationCheck();
  }

}
