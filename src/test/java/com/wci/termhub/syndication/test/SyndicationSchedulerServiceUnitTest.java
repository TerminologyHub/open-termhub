/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

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

  /**
   * Sets the up.
   */
  @BeforeEach
  public void setUp() {
    mockManager = mock(SyndicationManager.class);
    scheduler = new SyndicationSchedulerService();

    // Inject mock using reflection - ensure the field is properly set
    ReflectionTestUtils.setField(scheduler, "syndicationManager", mockManager);

    // Verify the mock was injected correctly
    SyndicationManager injectedManager = (SyndicationManager) ReflectionTestUtils.getField(scheduler,
        "syndicationManager");
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

}
