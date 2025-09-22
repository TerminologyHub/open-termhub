/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.syndication.text;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.wci.termhub.syndication.SyndicationManager;
import com.wci.termhub.syndication.SyndicationResults;
import com.wci.termhub.syndication.SyndicationSchedulerService;

/**
 * Unit test for SyndicationSchedulerService.
 */
public class SyndicationSchedulerServiceUnitTest {

  /** The scheduler. */
  private SyndicationSchedulerService scheduler;

  /** The mock manager. */
  private SyndicationManager mockManager;

  /**
   * Sets the up.
   */
  @BeforeEach
  public void setUp() {
    mockManager = mock(SyndicationManager.class);
    scheduler = new SyndicationSchedulerService();

    // Inject mock using reflection - ensure the field is properly set
    ReflectionTestUtils.setField(scheduler, "syndicationManager", mockManager);
    ReflectionTestUtils.setField(scheduler, "syndicationCheckEnabled", true);

    // Reset syndication completion state for each test
    java.io.File completionFile = new java.io.File("syndication.completed");
    if (completionFile.exists()) {
      completionFile.delete();
    }

    // Verify the mock was injected correctly
    SyndicationManager injectedManager =
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
    SyndicationSchedulerService scheduler = new SyndicationSchedulerService();
    assertNotNull(scheduler);
  }

  /**
   * Test check syndication fixed rate when enabled.
   *
   * @throws Exception the exception
   */
  @Test
  public void testCheckSyndicationFixedRateWhenEnabled() throws Exception {
    // Setup mock
    SyndicationResults mockResults = mock(SyndicationResults.class);
    when(mockManager.performSyndicationCheck()).thenReturn(mockResults);

    // Execute
    scheduler.checkSyndicationFixedRate();

    // Verify
    verify(mockManager).performSyndicationCheck();
  }

  /**
   * Test check syndication fixed rate when disabled.
   *
   * @throws Exception the exception
   */
  @Test
  public void testCheckSyndicationFixedRateWhenDisabled() throws Exception {
    // Disable syndication
    ReflectionTestUtils.setField(scheduler, "syndicationCheckEnabled", false);

    // Execute
    scheduler.checkSyndicationFixedRate();

    // Verify
    verify(mockManager, never()).performSyndicationCheck();
  }

  /**
   * Test check syndication fixed rate with exception.
   *
   * @throws Exception the exception
   */
  @Test
  public void testCheckSyndicationFixedRateWithException() throws Exception {
    // Setup mock to throw exception
    when(mockManager.performSyndicationCheck()).thenThrow(new RuntimeException("Test exception"));

    // Execute - should not throw exception
    scheduler.checkSyndicationFixedRate();

    // Verify
    verify(mockManager).performSyndicationCheck();
  }

  /**
   * Test check syndication fixed rate one-time behavior.
   *
   * @throws Exception the exception
   */
  @Test
  public void testCheckSyndicationFixedRateOneTimeBehavior() throws Exception {
    // Setup mock
    SyndicationResults mockResults = mock(SyndicationResults.class);
    when(mockManager.performSyndicationCheck()).thenReturn(mockResults);

    // Execute first time - should run
    scheduler.checkSyndicationFixedRate();
    verify(mockManager).performSyndicationCheck();

    // Execute second time - should skip
    scheduler.checkSyndicationFixedRate();
    verify(mockManager).performSyndicationCheck(); // Still only called once
  }

}
