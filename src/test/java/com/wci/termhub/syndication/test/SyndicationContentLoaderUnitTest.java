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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.model.Subset;
import com.wci.termhub.model.Mapset;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.syndication.SyndicationContentLoader;
import com.wci.termhub.syndication.SyndicationContentTracker;
import com.wci.termhub.syndication.SyndicationFeed;
import com.wci.termhub.syndication.SyndicationFeedEntry;
import com.wci.termhub.syndication.SyndicationLink;
import com.wci.termhub.syndication.SyndicationResults;

/**
 * Unit tests for SyndicationContentLoader.
 *
 * These tests use mocked dependencies to test the syndication content loading
 * functionality without requiring actual network downloads or database
 * operations.
 */
// @ExtendWith(MockitoExtension.class)
public class SyndicationContentLoaderUnitTest {

  /** The content loader. */
  private SyndicationContentLoader contentLoader;

  /** Auto-closeable for Mockito openMocks. */
  private AutoCloseable mocks;

  /** The mock search service. */
  @Mock
  private EntityRepositoryService mockSearchService;

  /** The mock syndication client. */
  @Mock
  private MockSyndicationClient mockSyndicationClient;

  /** The mock content tracker. */
  @Mock
  private SyndicationContentTracker mockContentTracker;

  /**
   * Sets up the test.
   */
  @BeforeEach
  public void setUp() {
    mocks = MockitoAnnotations.openMocks(this);
    contentLoader = new SyndicationContentLoader();

    // Inject mock dependencies using reflection
    try {
      final java.lang.reflect.Field searchServiceField = SyndicationContentLoader.class
          .getDeclaredField("searchService");
      searchServiceField.setAccessible(true);
      searchServiceField.set(contentLoader, mockSearchService);

      final java.lang.reflect.Field syndicationClientField = SyndicationContentLoader.class
          .getDeclaredField("syndicationClient");
      syndicationClientField.setAccessible(true);
      syndicationClientField.set(contentLoader, mockSyndicationClient);

      final java.lang.reflect.Field contentTrackerField = SyndicationContentLoader.class
          .getDeclaredField("contentTracker");
      contentTrackerField.setAccessible(true);
      contentTrackerField.set(contentLoader, mockContentTracker);

    } catch (final Exception e) {
      throw new RuntimeException("Failed to inject mock dependencies", e);
    }
  }

  /**
   * Tears down mocks.
   *
   * @throws Exception the exception
   */
  @AfterEach
  public void tearDown() throws Exception {
    if (mocks != null) {
      mocks.close();
    }
  }

  /**
   * Helper to create a typed empty ResultList without mocks.
   *
   * @param <T> the type
   * @return the empty result list
   */
  private static <T> ResultList<T> emptyResultList() {
    return new ResultList<>();
  }

  /**
   * Copy resource to temp.
   *
   * @param resource the resource
   * @return the path
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private Path copyResourceToTemp(final String resource) throws IOException {
    final Path tempFile = Files.createTempFile("syndication-test-", ".json");
    try (InputStream in = getClass().getResourceAsStream("/data/" + resource)) {
      if (in == null) {
        throw new IOException("Missing classpath resource: data/" + resource);
      }
      Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
    }
    return tempFile;
  }

  /**
   * Copy resource for entry.
   *
   * @param entry the entry
   * @return the sets the
   */
  private Set<String> copyResourceForEntry(final SyndicationFeedEntry entry) {
    final SyndicationLink zipLink = entry.getZipLink();
    if (zipLink == null || zipLink.getHref() == null) {
      throw new RuntimeException("Entry has no downloadable resource: " + entry.getTitle());
    }

    String href = zipLink.getHref();
    if (href.startsWith("classpath:")) {
      href = href.substring("classpath:".length());
    }
    if (href.startsWith("/")) {
      href = href.substring(1);
    }
    if (href.startsWith("data/")) {
      href = href.substring("data/".length());
    }

    try {
      final Path tempFile = copyResourceToTemp(href);
      try {
        normalizeResourceVersion(tempFile, entry);
      } catch (final IOException e) {
        throw new RuntimeException("Failed to normalize resource for entry " + entry.getTitle(), e);
      }
      return Collections.singleton(tempFile.toString());
    } catch (final IOException e) {
      throw new RuntimeException("Failed to copy resource for entry " + entry.getTitle(), e);
    }
  }

  /**
   * Normalize resource version.
   *
   * @param file  the file
   * @param entry the entry
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void normalizeResourceVersion(final Path file, final SyndicationFeedEntry entry)
      throws IOException {
    final ObjectMapper mapper = new ObjectMapper();
    final JsonNode root = mapper.readTree(file.toFile());
    if (root == null || !root.isObject()) {
      return;
    }

    final ObjectNode object = (ObjectNode) root;
    if (object.has("version")) {
      object.put("version", entry.getContentItemVersion());
    }
    if (object.has("fhirVersion")) {
      final JsonNode fhirVersionNode = object.get("fhirVersion");
      if (fhirVersionNode != null && fhirVersionNode.isTextual()) {
        object.put("fhirVersion", entry.getContentItemVersion());
      }
    }
    Files.writeString(file, mapper.writeValueAsString(object));
  }

  /**
   * Test loading content with entries that have no zip link.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLoadContentWithEntriesNoZipLink() throws Exception {
    final SyndicationFeedEntry entry = SyndicationTestDataHelper.createCodeSystemEntry("urn:uuid:icd10cm-2023",
        "ICD10CM 2023",
        "SANDBOX", "2023", "ICD10CM", "CodeSystem-icd10cm-sandbox-2023-r5.json");
    entry.setLinks(null);

    assertThrows(NullPointerException.class,
        () -> contentLoader.loadContent(Arrays.asList(entry), new SyndicationFeed()));
  }

  /**
   * Test loading content with successful download and processing.
   *
   * @throws Exception the exception
   */
  public void testLoadContentWithSuccessfulProcessing() throws Exception {
    // Given
    final SyndicationFeedEntry entry = SyndicationTestDataHelper.createCodeSystemEntry(
        "urn:uuid:rxnorm-04012024", "RXNORM 04012024", "SANDBOX", "04012024", "RXNORM",
        "CodeSystem-rxnorm-sandbox-04012024-r5.json");

    final List<SyndicationFeedEntry> entries = Arrays.asList(entry);
    final SyndicationFeed feed = new SyndicationFeed();

    // Type-safe stubs for searchService
    final ResultList<Terminology> emptyTerminologies = emptyResultList();
    lenient().when(mockSearchService.find(any(SearchParameters.class), eq(Terminology.class)))
        .thenReturn(emptyTerminologies);

    final ResultList<Subset> emptySubsets = emptyResultList();
    lenient().when(mockSearchService.find(any(SearchParameters.class), eq(Subset.class)))
        .thenReturn(emptySubsets);

    final ResultList<Mapset> emptyMapsets = emptyResultList();
    lenient().when(mockSearchService.find(any(SearchParameters.class), eq(Mapset.class)))
        .thenReturn(emptyMapsets);

    // Ensure Mockito matchers are type-safe
    doAnswer(invocation -> copyResourceForEntry((SyndicationFeedEntry) invocation.getArgument(0)))
        .when(mockSyndicationClient)
        .downloadPackages(any(SyndicationFeedEntry.class), any(SyndicationFeed.class));

    when(mockContentTracker.isContentLoaded(anyString())).thenReturn(false);
    doNothing().when(mockContentTracker).markContentAsLoaded(any(), any(), anyString(), anyString(),
        anyString(), anyString());

    // When
    final SyndicationResults results = contentLoader.loadContent(entries, feed);

    // Then
    assertNotNull(results);
    // Note: The actual loading will fail because we're not mocking the file
    // operations,
    // but the structure should be correct
    assertEquals(1, results.getTotalProcessed());
  }

  /**
   * Test loading content with download failure.
   *
   * @throws Exception the exception
   */
  public void testLoadContentWithDownloadFailure() throws Exception {
    // Given
    final SyndicationFeedEntry entry = SyndicationTestDataHelper.createCodeSystemEntry(
        "urn:uuid:snomedct-20240101", "SNOMEDCT 20240101", "SANDBOX", "20240101", "SNOMEDCT",
        "CodeSystem-snomedct-sandbox-20240101-r5.json");

    final List<SyndicationFeedEntry> entries = Arrays.asList(entry);
    final SyndicationFeed feed = new SyndicationFeed();
    feed.setEntries(new ArrayList<>(entries));

    doThrow(new RuntimeException("Download failed")).when(mockSyndicationClient)
        .downloadPackages(eq(entry), any(SyndicationFeed.class));

    // When
    final SyndicationResults results = contentLoader.loadContent(entries, feed);

    // Then
    assertNotNull(results);
    assertEquals(1, results.getTotalProcessed());
    assertEquals(1, results.getTotalErrors());
    assertTrue(
        results.getErrorMessages().stream().anyMatch(msg -> msg.contains("Download failed")));
  }

  /**
   * Test loading content with retry mechanism.
   *
   * @throws Exception the exception
   */
  public void testLoadContentWithRetry() throws Exception {
    // Given
    final SyndicationFeedEntry entry = SyndicationTestDataHelper.createCodeSystemEntry(
        "urn:uuid:snomedct-20240101", "SNOMEDCT 20240101", "SANDBOX", "20240101", "SNOMEDCT",
        "CodeSystem-snomedct-sandbox-20240101-r5.json");

    final List<SyndicationFeedEntry> entries = Arrays.asList(entry);
    final SyndicationFeed feed = new SyndicationFeed();
    feed.setEntries(new ArrayList<>(entries));

    doThrow(new RuntimeException("First attempt failed"))
        .doAnswer(
            invocation -> copyResourceForEntry((SyndicationFeedEntry) invocation.getArgument(0)))
        .when(mockSyndicationClient)
        .downloadPackages(any(SyndicationFeedEntry.class), any(SyndicationFeed.class));

    // Mock content tracker
    when(mockContentTracker.isContentLoaded(anyString())).thenReturn(false);
    doNothing().when(mockContentTracker).markContentAsLoaded(any(), any(), anyString(), anyString(),
        anyString(), anyString());

    // Mock search service
    // Type-safe stubs for searchService
    final ResultList<Terminology> emptyTerminologies = emptyResultList();
    lenient().when(mockSearchService.find(any(SearchParameters.class), eq(Terminology.class)))
        .thenReturn(emptyTerminologies);

    final ResultList<Subset> emptySubsets = emptyResultList();
    lenient().when(mockSearchService.find(any(SearchParameters.class), eq(Subset.class)))
        .thenReturn(emptySubsets);

    final ResultList<Mapset> emptyMapsets = emptyResultList();
    lenient().when(mockSearchService.find(any(SearchParameters.class), eq(Mapset.class)))
        .thenReturn(emptyMapsets);

    // When
    final SyndicationResults results = contentLoader.loadContent(entries, feed);

    // Then
    assertNotNull(results);
    assertEquals(1, results.getTotalProcessed());
    // The retry mechanism should be triggered
    verify(mockSyndicationClient, times(2)).downloadPackages(any(SyndicationFeedEntry.class),
        any(SyndicationFeed.class));
  }

  /**
   * Test loading content with duplicate entries.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLoadContentWithDuplicateEntries() throws Exception {
    // Given
    final SyndicationFeedEntry entry1 = SyndicationTestDataHelper.createCodeSystemEntry(
        "urn:uuid:snomedct-20240101", "SNOMEDCT 20240101", "SANDBOX", "20240101", "SNOMEDCT",
        "CodeSystem-snomedct-sandbox-20240101-r5.json");
    final SyndicationFeedEntry entry2 = SyndicationTestDataHelper.createCodeSystemEntry(
        "urn:uuid:snomedct-20240101-dup", "SNOMEDCT 20240101", "SANDBOX", "20240101", "SNOMEDCT",
        "CodeSystem-snomedct-sandbox-20240101-r5.json");

    final List<SyndicationFeedEntry> entries = Arrays.asList(entry1, entry2);
    final SyndicationFeed feed = new SyndicationFeed();
    feed.setEntries(new ArrayList<>(entries));

    doAnswer(invocation -> copyResourceForEntry((SyndicationFeedEntry) invocation.getArgument(0)))
        .when(mockSyndicationClient)
        .downloadPackages(any(SyndicationFeedEntry.class), any(SyndicationFeed.class));

    // Mock content tracker
    when(mockContentTracker.isContentLoaded(anyString())).thenReturn(false);
    doNothing().when(mockContentTracker).markContentAsLoaded(any(), any(), anyString(), anyString(),
        anyString(), anyString());

    // Mock search service
    // Type-safe stubs for searchService
    final ResultList<Terminology> emptyTerminologies = emptyResultList();
    lenient().when(mockSearchService.find(any(SearchParameters.class), eq(Terminology.class)))
        .thenReturn(emptyTerminologies);

    final ResultList<Subset> emptySubsets = emptyResultList();
    lenient().when(mockSearchService.find(any(SearchParameters.class), eq(Subset.class)))
        .thenReturn(emptySubsets);

    final ResultList<Mapset> emptyMapsets = emptyResultList();
    lenient().when(mockSearchService.find(any(SearchParameters.class), eq(Mapset.class)))
        .thenReturn(emptyMapsets);

    // When
    final SyndicationResults results = contentLoader.loadContent(entries, feed);

    // Then
    assertNotNull(results);
    assertEquals(2, results.getTotalProcessed());
    verify(mockSyndicationClient, atLeast(1)).downloadPackages(any(SyndicationFeedEntry.class),
        any(SyndicationFeed.class));
  }

  /**
   * Test loading content with already loaded entries.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLoadContentWithAlreadyLoadedEntries() throws Exception {
    // Given
    final SyndicationFeedEntry entry = SyndicationTestDataHelper.createCodeSystemEntry(
        "urn:uuid:snomedct-20240101", "SNOMEDCT 20240101", "SANDBOX", "20240101", "SNOMEDCT",
        "CodeSystem-snomedct-sandbox-20240101-r5.json");

    final List<SyndicationFeedEntry> entries = Arrays.asList(entry);
    final SyndicationFeed feed = new SyndicationFeed();
    feed.setEntries(new ArrayList<>(entries));

    // Mock content tracker to return true for already loaded
    when(mockContentTracker.isContentLoaded(anyString())).thenReturn(true);

    // When
    final SyndicationResults results = contentLoader.loadContent(entries, feed);

    // Then
    assertNotNull(results);
    assertEquals(1, results.getTotalProcessed());
    assertEquals(0, results.getTotalLoaded()); // Should not load again
    verify(mockSyndicationClient, never()).downloadPackages(any(SyndicationFeedEntry.class),
        any(SyndicationFeed.class));
  }

  /**
   * Test loading content with different content types.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLoadContentWithDifferentContentTypes() throws Exception {
    // Given
    final SyndicationFeedEntry codeSystemEntry = SyndicationTestDataHelper.createCodeSystemEntry(
        "urn:uuid:icd10cm-2023", "ICD10CM 2023",
        "SANDBOX", "2023", "ICD10CM", "CodeSystem-icd10cm-sandbox-2023-r5.json");
    final SyndicationFeedEntry valueSetEntry = SyndicationTestDataHelper.createValueSetEntry(
        "urn:uuid:valueset-old", "HL7 Body Site 20240301", "FHIR Project team", "20240301",
        "HL7 Body Site", "ValueSet-hl7-body-site-r5.json");
    final SyndicationFeedEntry conceptMapEntry = SyndicationTestDataHelper.createConceptMapEntry(
        "urn:uuid:conceptmap-old", "CPT to HCPCS 20240301", "Example Healthcare Systems",
        "20240301", "CPT to HCPCS", "ConceptMap-cpt-hcpcs-fake-r5.json");

    final List<SyndicationFeedEntry> entries = Arrays.asList(codeSystemEntry, valueSetEntry, conceptMapEntry);
    final SyndicationFeed feed = new SyndicationFeed();
    feed.setEntries(new ArrayList<>(entries));

    doAnswer(invocation -> copyResourceForEntry((SyndicationFeedEntry) invocation.getArgument(0)))
        .when(mockSyndicationClient)
        .downloadPackages(any(SyndicationFeedEntry.class), any(SyndicationFeed.class));

    // Mock content tracker
    when(mockContentTracker.isContentLoaded(anyString())).thenReturn(false);
    doNothing().when(mockContentTracker).markContentAsLoaded(any(), any(), anyString(), anyString(),
        anyString(), anyString());

    // Type-safe stubs for search service
    final ResultList<Terminology> emptyTerminologies3 = emptyResultList();
    lenient().when(mockSearchService.find(any(SearchParameters.class), eq(Terminology.class)))
        .thenReturn(emptyTerminologies3);
    final ResultList<Subset> emptySubsets3 = emptyResultList();
    lenient().when(mockSearchService.find(any(SearchParameters.class), eq(Subset.class)))
        .thenReturn(emptySubsets3);
    final ResultList<Mapset> emptyMapsets3 = emptyResultList();
    lenient().when(mockSearchService.find(any(SearchParameters.class), eq(Mapset.class)))
        .thenReturn(emptyMapsets3);
    final ResultList<com.wci.termhub.model.SyndicatedContent> emptySc3 = emptyResultList();
    lenient().when(mockSearchService.find(any(SearchParameters.class),
        eq(com.wci.termhub.model.SyndicatedContent.class))).thenReturn(emptySc3);

    // When
    final SyndicationResults results = contentLoader.loadContent(entries, feed);

    // Then
    assertNotNull(results);
    assertEquals(3, results.getTotalProcessed());
    verify(mockSyndicationClient, atLeast(3)).downloadPackages(any(SyndicationFeedEntry.class),
        any(SyndicationFeed.class));
  }

  /**
   * Test loading content with null feed.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLoadContentWithNullFeed() throws Exception {
    // Given
    final SyndicationFeedEntry entry = SyndicationTestDataHelper.createCodeSystemEntry(
        "urn:uuid:snomedct-20240101", "SNOMEDCT 20240101", "SANDBOX", "20240101", "SNOMEDCT",
        "CodeSystem-snomedct-sandbox-20240101-r5.json");

    final List<SyndicationFeedEntry> entries = Arrays.asList(entry);
    final SyndicationFeed feed = null;

    // Mock successful download with null feed
    doAnswer(invocation -> copyResourceForEntry((SyndicationFeedEntry) invocation.getArgument(0)))
        .when(mockSyndicationClient)
        .downloadPackages(any(SyndicationFeedEntry.class), eq((SyndicationFeed) null));

    // Mock content tracker
    lenient().when(mockContentTracker.isContentLoaded(anyString())).thenReturn(false);
    lenient().doNothing().when(mockContentTracker).markContentAsLoaded(any(), any(), anyString(),
        anyString(), anyString(), anyString());

    // Type-safe stubs by class
    @SuppressWarnings("unchecked")
    final ResultList<Terminology> mockTerminologyList = mock(ResultList.class);
    final Terminology terminology = new Terminology();
    terminology.setAbbreviation("SNOMEDCT");
    terminology.setPublisher("SANDBOX");
    terminology.setVersion("20240101");
    terminology.setAttributes(new HashMap<>());
    lenient().when(mockTerminologyList.getItems())
        .thenReturn(Collections.singletonList(terminology));
    lenient().when(mockSearchService.find(any(SearchParameters.class), eq(Terminology.class)))
        .thenReturn(mockTerminologyList);

    final ResultList<com.wci.termhub.model.SyndicatedContent> emptySc = emptyResultList();
    lenient().when(mockSearchService.find(any(SearchParameters.class),
        eq(com.wci.termhub.model.SyndicatedContent.class))).thenReturn(emptySc);

    final ResultList<Subset> emptySubsets2 = emptyResultList();
    lenient().when(mockSearchService.find(any(SearchParameters.class), eq(Subset.class)))
        .thenReturn(emptySubsets2);

    final ResultList<Mapset> emptyMapsets2 = emptyResultList();
    lenient().when(mockSearchService.find(any(SearchParameters.class), eq(Mapset.class)))
        .thenReturn(emptyMapsets2);

    // When
    final SyndicationResults results = contentLoader.loadContent(entries, feed);

    // Then
    assertNotNull(results);
    assertEquals(1, results.getTotalProcessed());
    verify(mockSyndicationClient, atLeast(1)).downloadPackages(any(SyndicationFeedEntry.class),
        eq((SyndicationFeed) null));
  }

  /**
   * Test loading content with empty download results.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLoadContentWithEmptyDownloadResults() throws Exception {
    // Given
    final SyndicationFeedEntry entry = SyndicationTestDataHelper.createCodeSystemEntry(
        "urn:uuid:snomedct-20240101", "SNOMEDCT 20240101", "SANDBOX", "20240101", "SNOMEDCT",
        "CodeSystem-snomedct-sandbox-20240101-r5.json");

    final List<SyndicationFeedEntry> entries = Arrays.asList(entry);
    final SyndicationFeed feed = new SyndicationFeed();

    // Mock empty download results
    when(mockSyndicationClient.downloadPackages(any(SyndicationFeedEntry.class),
        any(SyndicationFeed.class))).thenReturn(Collections.emptySet());

    // Mock search service to prevent NullPointerException in
    // logAllSyndicatedContentRecords
    @SuppressWarnings("unchecked")
    final com.wci.termhub.model.ResultList<com.wci.termhub.model.SyndicatedContent> mockResultList = mock(
        com.wci.termhub.model.ResultList.class);
    when(mockSearchService.find(any(SearchParameters.class),
        eq(com.wci.termhub.model.SyndicatedContent.class))).thenReturn(mockResultList);
    when(mockResultList.getTotal()).thenReturn(0L);

    // When
    final SyndicationResults results = contentLoader.loadContent(entries, feed);

    // Then
    assertNotNull(results);
    assertEquals(1, results.getTotalProcessed());
    assertEquals(1, results.getTotalErrors());
    assertTrue(results.getErrorMessages().stream()
        .anyMatch(msg -> msg.contains("No packages downloaded")));
  }
}
