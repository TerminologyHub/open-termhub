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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.wci.termhub.syndication.SyndicationClient;
import com.wci.termhub.syndication.SyndicationContentLoader;
import com.wci.termhub.syndication.SyndicationContentTracker;
import com.wci.termhub.syndication.SyndicationFeed;
import com.wci.termhub.syndication.SyndicationFeedEntry;
import com.wci.termhub.syndication.SyndicationManager;
import com.wci.termhub.syndication.SyndicationResults;

/**
 * Unit test for SyndicationManager.
 */
public class SyndicationManagerUnitTest {

  /** The manager. */
  private SyndicationManager manager;

  /** The mock client. */
  private SyndicationClient mockClient;

  /** The mock tracker. */
  private SyndicationContentTracker mockTracker;

  /** The mock loader. */
  private SyndicationContentLoader mockLoader;

  /**
   * Sets the up.
   */
  @BeforeEach
  public void setUp() {
    manager = new SyndicationManager();
    mockClient = mock(SyndicationClient.class);
    mockTracker = mock(SyndicationContentTracker.class);
    mockLoader = mock(SyndicationContentLoader.class);

    // Inject mocks using reflection
    ReflectionTestUtils.setField(manager, "syndicationClient", mockClient);
    ReflectionTestUtils.setField(manager, "contentTracker", mockTracker);
    ReflectionTestUtils.setField(manager, "contentLoader", mockLoader);
    ReflectionTestUtils.setField(manager, "syndicationCheckEnabled", true);
  }

  /**
   * Test constructor.
   */
  @Test
  public void testConstructor() {
    SyndicationManager manager = new SyndicationManager();
    assertNotNull(manager);
  }

  /**
   * Test check syndication success.
   *
   * @throws Exception the exception
   */
  @Test
  public void testCheckSyndicationSuccess() throws Exception {
    // Setup mocks
    SyndicationFeed mockFeed = mock(SyndicationFeed.class);
    SyndicationFeedEntry mockEntry = mock(SyndicationFeedEntry.class);
    List<SyndicationFeedEntry> entries = new ArrayList<>();
    entries.add(mockEntry);
    when(mockFeed.getEntries()).thenReturn(entries);

    // Mock the entry with a valid category
    when(mockEntry.getCategory()).thenReturn(mock(com.wci.termhub.syndication.SyndicationCategory.class));
    when(mockEntry.getCategory().getTerm()).thenReturn("CODESYSTEM_FHIRR5_ALL");

    when(mockClient.getFeed()).thenReturn(mockFeed);
    when(mockClient.getNewEntries(any(SyndicationFeed.class), any(SyndicationContentTracker.class)))
        .thenReturn(entries);
    when(mockLoader.loadContent(anyList(), any(SyndicationFeed.class)))
        .thenReturn(mock(com.wci.termhub.syndication.SyndicationResults.class));

    // Execute
    SyndicationResults result = manager.performSyndicationCheck();

    // Verify
    assertNotNull(result);
    assertTrue(result.isSuccess());
    verify(mockClient).getFeed();
    verify(mockLoader).loadContent(anyList(), any(SyndicationFeed.class));
  }

  /**
   * Test check syndication when disabled.
   *
   * @throws Exception the exception
   */
  @Test
  public void testCheckSyndicationWhenDisabled() throws Exception {
    // Disable syndication
    ReflectionTestUtils.setField(manager, "syndicationCheckEnabled", false);

    // Execute
    SyndicationResults result = manager.performSyndicationCheck();

    // Verify
    assertNotNull(result);
    assertFalse(result.isSuccess());
    assertEquals("Syndication check is disabled", result.getMessage());
    verify(mockClient, never()).getFeed();
    verify(mockLoader, never()).loadContent(anyList(), any(SyndicationFeed.class));
  }

  /**
   * Test check syndication when in progress.
   *
   * @throws Exception the exception
   */
  @Test
  public void testCheckSyndicationWhenInProgress() throws Exception {
    // Set in progress
    ReflectionTestUtils.setField(manager, "syndicationCheckInProgress", true);

    // Execute
    SyndicationResults result = manager.performSyndicationCheck();

    // Verify
    assertNotNull(result);
    assertFalse(result.isSuccess());
    assertEquals("Syndication check already in progress", result.getMessage());
    verify(mockClient, never()).getFeed();
  }

  /**
   * Test check syndication with exception.
   *
   * @throws Exception the exception
   */
  @Test
  public void testCheckSyndicationWithException() throws Exception {
    // Setup mocks to throw exception
    when(mockClient.getFeed()).thenThrow(new RuntimeException("Network error"));

    // Execute
    SyndicationResults result = manager.performSyndicationCheck();

    // Verify
    assertNotNull(result);
    assertFalse(result.isSuccess());
    assertTrue(result.getMessage().contains("Network error"));
    verify(mockClient).getFeed();
    verify(mockLoader, never()).loadContent(anyList(), any(SyndicationFeed.class));
  }

  /**
   * Test check syndication with empty feed.
   *
   * @throws Exception the exception
   */
  @Test
  public void testCheckSyndicationWithEmptyFeed() throws Exception {
    // Setup mocks
    SyndicationFeed mockFeed = mock(SyndicationFeed.class);
    when(mockFeed.getEntries()).thenReturn(new ArrayList<>());
    when(mockClient.getFeed()).thenReturn(mockFeed);

    // Execute
    SyndicationResults result = manager.performSyndicationCheck();

    // Verify
    assertNotNull(result);
    assertTrue(result.isSuccess());
    assertEquals("No syndication feed entries found", result.getMessage());
    verify(mockClient).getFeed();
    verify(mockLoader, never()).loadContent(anyList(), any(SyndicationFeed.class));
  }

  /**
   * Test is syndication check in progress.
   */
  @Test
  public void testIsSyndicationCheckInProgress() {
    // Initially false
    assertFalse(manager.isSyndicationCheckInProgress());

    // Set to true
    ReflectionTestUtils.setField(manager, "syndicationCheckInProgress", true);
    assertTrue(manager.isSyndicationCheckInProgress());
  }

  /**
   * Test concurrent syndication checks.
   *
   * @throws Exception the exception
   */
  @Test
  public void testConcurrentSyndicationChecks() throws Exception {
    // Setup mocks
    SyndicationFeed mockFeed = mock(SyndicationFeed.class);
    when(mockFeed.getEntries()).thenReturn(new ArrayList<>());
    when(mockClient.getFeed()).thenReturn(mockFeed);

    // Start first check
    Thread firstCheck = new Thread(() -> {
      try {
        manager.performSyndicationCheck();
      } catch (Exception e) {
        // Ignore
      }
    });

    // Start second check immediately
    Thread secondCheck = new Thread(() -> {
      try {
        manager.performSyndicationCheck();
      } catch (Exception e) {
        // Ignore
      }
    });

    firstCheck.start();
    secondCheck.start();

    firstCheck.join();
    secondCheck.join();

    // Verify only one call was made to the client
    verify(mockClient, times(1)).getFeed();
  }
}
