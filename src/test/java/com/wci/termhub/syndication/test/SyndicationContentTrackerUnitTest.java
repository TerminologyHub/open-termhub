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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.SyndicatedContent;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.syndication.SyndicationContentTracker;

/**
 * Unit test for SyndicationContentTracker.
 */
public class SyndicationContentTrackerUnitTest {

  /** The tracker. */
  private SyndicationContentTracker tracker;

  /** The mock search service. */
  private EntityRepositoryService mockSearchService;

  /**
   * Sets the up.
   */
  @BeforeEach
  public void setUp() {
    tracker = new SyndicationContentTracker();
    mockSearchService = mock(EntityRepositoryService.class);

    // Inject mock using reflection
    ReflectionTestUtils.setField(tracker, "searchService", mockSearchService);
  }

  /**
   * Test constructor.
   */
  @Test
  public void testConstructor() {
    SyndicationContentTracker tracker = new SyndicationContentTracker();
    assertNotNull(tracker);
  }

  /**
   * Test is content loaded when not loaded.
   *
   * @throws Exception the exception
   */
  @Test
  public void testIsContentLoadedWhenNotLoaded() throws Exception {
    // Setup mock
    @SuppressWarnings("unchecked")
    ResultList<SyndicatedContent> mockResults = mock(ResultList.class);
    when(mockResults.getItems()).thenReturn(new ArrayList<>());
    when(mockSearchService.find(any(SearchParameters.class), eq(SyndicatedContent.class)))
        .thenReturn(mockResults);

    // Execute
    boolean isLoaded = tracker.isContentLoaded("test-entry-id");

    // Verify
    assertFalse(isLoaded);
    verify(mockSearchService).find(any(SearchParameters.class), eq(SyndicatedContent.class));
  }

  /**
   * Test is content loaded when loaded.
   *
   * @throws Exception the exception
   */
  @Test
  public void testIsContentLoadedWhenLoaded() throws Exception {
    // Setup mock
    SyndicatedContent mockContent = mock(SyndicatedContent.class);
    List<SyndicatedContent> contentList = new ArrayList<>();
    contentList.add(mockContent);

    @SuppressWarnings("unchecked")
    ResultList<SyndicatedContent> mockResults = mock(ResultList.class);
    when(mockResults.getItems()).thenReturn(contentList);
    when(mockSearchService.find(any(SearchParameters.class), eq(SyndicatedContent.class)))
        .thenReturn(mockResults);

    // Execute
    boolean isLoaded = tracker.isContentLoaded("test-entry-id");

    // Verify
    assertTrue(isLoaded);
    verify(mockSearchService).find(any(SearchParameters.class), eq(SyndicatedContent.class));
  }

  /**
   * Test mark content as loaded.
   *
   * @throws Exception the exception
   */
  @Test
  public void testMarkContentAsLoaded() throws Exception {
    // Execute
    tracker.markContentAsLoaded("test-entry-id", "test-identifier", "1.0", "CodeSystem",
        "Test Title", "https://api.example.com");

    // Verify - method should complete without exception
    // The actual implementation uses add() method internally
  }

  /**
   * Test mark content as loaded with null values.
   *
   * @throws Exception the exception
   */
  @Test
  public void testMarkContentAsLoadedWithNullValues() throws Exception {
    // Execute with null values
    tracker.markContentAsLoaded(null, null, null, null, null, null);

    // Should not throw exception
  }

  /**
   * Test initialize.
   */
  @Test
  public void testInitialize() {
    // Execute
    tracker.initialize();

    // Should not throw exception
    // This method currently just logs, so we just verify it completes
  }

}
