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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
import com.wci.termhub.syndication.SyndicationLink;
import com.wci.termhub.syndication.SyndicationResults;

/**
 * Unit test for SyndicationContentLoader.
 */
public class SyndicationContentLoaderUnitTest {

  /** The loader. */
  private SyndicationContentLoader loader;

  /** The mock client. */
  private SyndicationClient mockClient;

  /** The mock tracker. */
  private SyndicationContentTracker mockTracker;

  /**
   * Sets the up.
   */
  @BeforeEach
  public void setUp() {
    loader = new SyndicationContentLoader();
    mockClient = mock(SyndicationClient.class);
    mockTracker = mock(SyndicationContentTracker.class);

    // Inject mocks using reflection
    ReflectionTestUtils.setField(loader, "syndicationClient", mockClient);
    ReflectionTestUtils.setField(loader, "contentTracker", mockTracker);
  }

  /**
   * Test constructor.
   */
  @Test
  public void testConstructor() {
    SyndicationContentLoader loader = new SyndicationContentLoader();
    assertNotNull(loader);
  }

  /**
   * Test load content with empty list.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLoadContentWithEmptyList() throws Exception {
    SyndicationFeed mockFeed = mock(SyndicationFeed.class);
    List<SyndicationFeedEntry> entries = new ArrayList<>();

    SyndicationResults result = loader.loadContent(entries, mockFeed);

    assertNotNull(result);
    assertEquals(0, result.getTotalProcessed());
    assertEquals(0, result.getTotalLoaded());
    assertEquals(0, result.getTotalErrors());
  }

  /**
   * Test load content with null list.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLoadContentWithNullList() throws Exception {
    SyndicationFeed mockFeed = mock(SyndicationFeed.class);

    assertThrows(Exception.class, () -> {
      loader.loadContent(null, mockFeed);
    });
  }

  /**
   * Test load content with null feed.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLoadContentWithNullFeed() throws Exception {
    List<SyndicationFeedEntry> entries = new ArrayList<>();
    entries.add(mock(SyndicationFeedEntry.class));

    // The method doesn't actually check for null feed, so it should complete
    // without exception
    SyndicationResults result = loader.loadContent(entries, null);
    assertNotNull(result);
  }

  /**
   * Test load content with already loaded entry.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLoadContentWithAlreadyLoadedEntry() throws Exception {
    // Setup mocks
    SyndicationFeed mockFeed = mock(SyndicationFeed.class);
    SyndicationFeedEntry mockEntry = mock(SyndicationFeedEntry.class);
    List<SyndicationFeedEntry> entries = new ArrayList<>();
    entries.add(mockEntry);

    when(mockEntry.getId()).thenReturn("test-entry-id");
    when(mockEntry.getContentItemIdentifier()).thenReturn("test-identifier");
    when(mockEntry.getContentItemVersion()).thenReturn("1.0");
    when(mockEntry.getTitle()).thenReturn("Test Title");
    // Mock zipLink to avoid null pointer exception
    SyndicationLink mockZipLink = mock(SyndicationLink.class);
    when(mockZipLink.getHref()).thenReturn("https://api.example.com/terminology/123/export");
    when(mockEntry.getZipLink()).thenReturn(mockZipLink);
    com.wci.termhub.syndication.SyndicationCategory mockCategory =
        mock(com.wci.termhub.syndication.SyndicationCategory.class);
    when(mockCategory.getTerm()).thenReturn("CODESYSTEM_FHIRR5_ALL");
    when(mockEntry.getCategory()).thenReturn(mockCategory);
    when(mockClient.getSyndicationUrl()).thenReturn("https://api.example.com");
    when(mockTracker.isContentLoaded("test-entry-id")).thenReturn(true);

    // Execute
    SyndicationResults result = loader.loadContent(entries, mockFeed);

    // Verify
    assertNotNull(result);
    assertEquals(1, result.getTotalProcessed());
    assertEquals(0, result.getTotalLoaded());
    assertEquals(0, result.getTotalErrors());
    verify(mockTracker, times(1)).isContentLoaded("test-entry-id");
  }

  /**
   * Test load content with new entry.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLoadContentWithNewEntry() throws Exception {
    // Setup mocks
    SyndicationFeed mockFeed = mock(SyndicationFeed.class);
    SyndicationFeedEntry mockEntry = mock(SyndicationFeedEntry.class);
    SyndicationLink mockLink = mock(SyndicationLink.class);
    List<SyndicationFeedEntry> entries = new ArrayList<>();
    entries.add(mockEntry);

    when(mockEntry.getId()).thenReturn("test-entry-id");
    when(mockEntry.getZipLink()).thenReturn(mockLink);
    when(mockLink.getHref()).thenReturn("https://api.example.com/terminology/123/export");
    when(mockEntry.getContentItemIdentifier()).thenReturn("test-identifier");
    when(mockEntry.getContentItemVersion()).thenReturn("1.0");
    when(mockEntry.getTitle()).thenReturn("Test Title");
    when(mockClient.getSyndicationUrl()).thenReturn("https://api.example.com");
    when(mockTracker.isContentLoaded("test-entry-id")).thenReturn(false);
    when(mockClient.downloadPackages(any(SyndicationFeedEntry.class), any(SyndicationFeed.class)))
        .thenReturn(new java.util.HashSet<>());

    // Mock the content type detection
    when(mockEntry.getCategory())
        .thenReturn(mock(com.wci.termhub.syndication.SyndicationCategory.class));

    // Execute
    SyndicationResults result = loader.loadContent(entries, mockFeed);

    // Verify
    assertNotNull(result);
    assertEquals(1, result.getTotalProcessed());
    verify(mockTracker, times(2)).isContentLoaded("test-entry-id");
    verify(mockClient, times(2)).downloadPackages(any(SyndicationFeedEntry.class),
        any(SyndicationFeed.class));
  }

  /**
   * Test load content with retry.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLoadContentWithRetry() throws Exception {
    // Setup mocks for retry scenario
    SyndicationFeed mockFeed = mock(SyndicationFeed.class);
    SyndicationFeedEntry mockEntry = mock(SyndicationFeedEntry.class);
    SyndicationLink mockLink = mock(SyndicationLink.class);
    List<SyndicationFeedEntry> entries = new ArrayList<>();
    entries.add(mockEntry);

    when(mockEntry.getId()).thenReturn("test-entry-id");
    when(mockEntry.getZipLink()).thenReturn(mockLink);
    when(mockLink.getHref()).thenReturn("https://api.example.com/terminology/123/export");
    when(mockEntry.getCategory())
        .thenReturn(mock(com.wci.termhub.syndication.SyndicationCategory.class));
    when(mockEntry.getContentItemIdentifier()).thenReturn("test-identifier");
    when(mockEntry.getContentItemVersion()).thenReturn("1.0");
    when(mockEntry.getTitle()).thenReturn("Test Title");
    when(mockClient.getSyndicationUrl()).thenReturn("https://api.example.com");
    when(mockTracker.isContentLoaded("test-entry-id")).thenReturn(false);
    when(mockClient.downloadPackages(any(SyndicationFeedEntry.class), any(SyndicationFeed.class)))
        .thenThrow(new RuntimeException("First attempt fails"))
        .thenReturn(new java.util.HashSet<>()); // Second attempt succeeds

    // Execute
    SyndicationResults result = loader.loadContent(entries, mockFeed);

    // Verify retry occurred
    assertNotNull(result);
    verify(mockClient, times(2)).downloadPackages(any(SyndicationFeedEntry.class),
        any(SyndicationFeed.class));
  }
}
